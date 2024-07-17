/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.web_kline.test2;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.example.web_kline.Order.OrderContext;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taobao.api.TaobaoResponse;
import org.apache.commons.codec.binary.Base64;
import org.example.StrateFactory;
import org.example.binance.factory.KlineFactory;
import org.example.kline.KlineClient;
import org.example.model.currency.Currency;
import org.example.model.enums.OrderSide;
import org.example.model.enums.Server;
import org.example.model.enums.ContractType;
import org.example.model.market.KlineModule;
import org.example.strategy.CollectRuleStrategy;
import org.example.trade.TradeClient;
import org.example.trade.TradeClientFactory;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Position;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.DoubleNum;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.LinkedBlockingDeque;

public class TraderTemplate {
    public static final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    LinkedBlockingDeque<Order> queue = new LinkedBlockingDeque<Order>(2);

    private Strategy strategy;

    protected OrderContext orderContext;
    protected Bar lastBar = null;
    protected BaseBarSeries series;

    protected boolean loadHistory = true;
    protected int continueIndex = 0;
    protected boolean mock = false;

    protected int amount = 1200;

    protected double profit = 0;

    protected Position position = null;

    protected Currency currency;

    protected String interval = "30m";

    double range2 = 5;

    protected long e = System.currentTimeMillis();
    protected long s = e - (5 * 24 * 60 * 60 * 1000);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Thread thread;

    private boolean init = true;

    KlineClient binanceKlineClient = KlineFactory.Create(Server.BINANCE, ContractType.UMFUTURE);
    TradeClient tradeClient = TradeClientFactory.Create(Server.BINANCE, ContractType.UMFUTURE);

    public TraderTemplate(boolean mock, Trade.TradeType tradeType, int amount, String interval, Currency currency, StrateFactory strategy, double range) {
        this.range2 = range;
        this.amount = amount;
        this.currency = currency;
        this.interval = interval;
//        thread = new Thread(this::trade);
        thread.setName("trade-thread");
        thread.start();

//        Tradetest tradetest = new Tradetest(currency);
//        try {
//            tradetest.initTrend(interval);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        this.tradetest = tradetest;

        this.orderContext = new OrderContext(2, amount, tradeType, currency, false);
        this.series = new BaseBarSeries("mySeries", DoubleNum::valueOf);
        this.strategy = strategy.buildStrategy(series);
        loadHistory = true;
        this.mock = mock;
        loadHistory();
        loadHistory = false;
        this.orderContext = new OrderContext(2, amount, tradeType, currency, mock);
        System.out.println("历史数据加载结束 " + lastBar);
    }

    public void test(String interval) throws Exception {

        try {
            for (KlineModule t : binanceKlineClient.getHistoryKlineData(currency, interval, System.currentTimeMillis() - 5 * 24 * 60 * 1000, System.currentTimeMillis())) {
                Bar newBar = BaseBar.builder(DoubleNum::valueOf, Double.class)
                        .timePeriod(Duration.ofMinutes(1))
                        .endTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(t.getEndTime()), ZoneId.systemDefault()))
                        .openPrice(t.getOpen())
                        .highPrice(t.getHigh())
                        .lowPrice(t.getLow())
                        .closePrice(t.getClose())
                        .volume(t.getQuantity())
                        .build();
                handler(newBar);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    protected void handler(Bar newBar) {
        if (continueIndex > 3) {
//            throw new RuntimeException("连续3次亏损");
        }
        boolean replace = false;
        if (lastBar == null || lastBar.getEndTime().isEqual((newBar.getEndTime()))) {
            lastBar = newBar;
            replace = true;
            return;
        }
        try {
            series.addBar(newBar, replace);
        } catch (Exception e) {
            System.out.println(lastBar.getEndTime() + "   " + newBar.getEndTime());
            throw e;
        }

        int endIndex = series.getEndIndex();
        if (strategy.shouldEnter(endIndex, orderContext.getTradingRecord())) {
            tradeBuy(newBar);
        }

        if (strategy instanceof CollectRuleStrategy) {
            System.out.println(currency.symbol() + " newBar：" + newBar + " 当前入场有效策略 " + ((CollectRuleStrategy) strategy).getEnterEffectiveRuleName()
                    + "入场分数" + ((CollectRuleStrategy) strategy).getEffectiveWeight());
            if (((CollectRuleStrategy) strategy).getEffectiveWeight() > 2 && !loadHistory) {
                sendMessageWebhook(currency.symbol() + " newBar：" + newBar + " 当前入场有效策略 " + ((CollectRuleStrategy) strategy).getEnterEffectiveRuleName()
                        + "入场分数" + ((CollectRuleStrategy) strategy).getEffectiveWeight());
            }
        }

        for (TradingRecord tradingRecord : orderContext.exit(endIndex, newBar, strategy)) {

            Trade exit = tradingRecord.getLastExit();
            Trade entry = tradingRecord.getLastEntry();
            System.out.println(currency.symbol() + "Exit on " + exit.getIndex() + "[" + newBar + "]" + " (price=" + exit.getNetPrice().doubleValue()
                    + ", amount=" + exit.getAmount().doubleValue() + ")");
            if (!loadHistory && !mock) {
//                orderLimitPingDuo(entry.getNetPrice().doubleValue() + range2);
                queue.add(new Order(entry.getNetPrice().doubleValue(), exit.getAmount().doubleValue(), OrderSide.SELL_LONG, currency.symbol()));
            }

            if (strategy instanceof CollectRuleStrategy) {
                System.out.println("离场有效策略 " + ((CollectRuleStrategy) strategy).getExitEffectiveRuleName()
                        + "离场分数" + ((CollectRuleStrategy) strategy).getEffectiveWeight());

                if (!loadHistory && ((CollectRuleStrategy) strategy).getEffectiveWeight() > 2) {
                    sendMessageWebhook(currency + " 离场有效策略 " + ((CollectRuleStrategy) strategy).getExitEffectiveRuleName()
                            + "离场分数" + ((CollectRuleStrategy) strategy).getEffectiveWeight());
                }

            }

            Position position = tradingRecord.getLastPosition();
            if (position != null && this.position != position) {
                this.position = tradingRecord.getLastPosition();
                double v = calcitePosition(position) * 0.9998 * (position.getEntry().getAmount().doubleValue());
                profit += v;
                System.out.println("本次盈利 " + v + " 一共盈利" + profit);
                if (v < 0) {
                    continueIndex++;
                } else {
                    continueIndex = 0;
                }
            }

        }


        if (strategy instanceof CollectRuleStrategy) {
            System.out.println("newBar：" + newBar + "当前离场有效策略 " + ((CollectRuleStrategy) strategy).getExitEffectiveRuleName()
                    + "离场分数" + ((CollectRuleStrategy) strategy).getEffectiveWeight());
        }

        lastBar = newBar;
    }

    public double calcitePosition(Position position) {
        if (position.getStartingType() == Trade.TradeType.BUY) {
            return position.getExit().getPricePerAsset().doubleValue() - position.getEntry().getPricePerAsset().doubleValue();
        } else {
            return position.getEntry().getPricePerAsset().doubleValue() - position.getExit().getPricePerAsset().doubleValue();
        }
    }


    public void loadHistory() {
        for (KlineModule d : binanceKlineClient.getHistoryKlineData(currency, interval, s, e)) {
            Bar newBar = BaseBar.builder(DoubleNum::valueOf, Double.class)
                    .timePeriod(Duration.ofMinutes(1))
                    .endTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(d.getEndTime()), ZoneId.systemDefault()))
                    .openPrice(d.getOpen())
                    .highPrice(d.getHigh())
                    .lowPrice(d.getLow())
                    .closePrice(d.getClose())
                    .volume(d.getQuantity())
                    .build();
            handler(newBar);
        }

    }

//做多信号
public void tradeBuy(Bar bar) {
    int endIndex = series.getEndIndex();
    double v = amount / (bar.getClosePrice().doubleValue());

//        System.out.println("Entered on " +endIndex + "[" + bar + "]" + " (price=" + series.getBar(endIndex).getClosePrice().doubleValue());
    //        if (tradetest.getType4h() == TrendType.NEUTRAL || tradetest.getType4h() == TrendType.GOOD || tradetest.getType4h() == TrendType.VERY_GOOD) {
    TradingRecord tradingRecord = orderContext.enter(endIndex, bar.getClosePrice());
    if (tradingRecord != null) {
        Trade entry = tradingRecord.getLastEntry();
        System.out.println("Entered on " + entry.getIndex() + "[" + bar + "]" + " (price=" + entry.getNetPrice().doubleValue()
                + ", amount=" + entry.getAmount().doubleValue() + ")");
        if (!loadHistory && !mock) {
            queue.add(new Order(entry.getNetPrice().doubleValue(), entry.getAmount().doubleValue(), OrderSide.BUY_LONG, currency.symbol()));
        }
        if (strategy instanceof CollectRuleStrategy) {
            System.out.println("入场有效策略 " + ((CollectRuleStrategy) strategy).getEnterEffectiveRuleName()
                    + "入场分数" + ((CollectRuleStrategy) strategy).getEffectiveWeight());
        }
//            }
    }

}



public static class Order {
    private int id;
    private double price;
    private double amount;
    private OrderSide orderSide;

    private String symbol;

    public Order(int id, double price, double amount, OrderSide orderSide, String symbol) {
        this.price = price;
        this.orderSide = orderSide;
        this.symbol = symbol;
        this.amount = amount;
        this.id = id;
    }

    public Order(double price, double amount, OrderSide orderSide, String symbol) {
        this.price = price;
        this.orderSide = orderSide;
        this.symbol = symbol;
        this.amount = amount;
    }

    public double getPrice() {
        return price;
    }

    public OrderSide getOrderSide() {
        return orderSide;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getAmount() {
        return amount;
    }

    public int getId() {
        return id;
    }
}

public String getDIndinUrl() {
    try {
        String url = "https://oapi.dingtalk.com/robot/send?access_token=793336df0ed161c1b9c082e7ff22ed360a2a1d3730fad8a753826ecc77a3d107";
        Long timestamp = System.currentTimeMillis();
        String secret = "SEC565e84ba79af6dc34b977b08a7b7bca1669f394ed36b7e1f2da377b6ff22195d";

        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
        String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
        return url + "&timestamp=" + timestamp + "&sign=" + sign;

    } catch (Exception e) {
        return null;
    }
}

public void sendMessageWebhook(String msg) {
    try {
        DingTalkClient client = new DefaultDingTalkClient(getDIndinUrl());
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("text");
        OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
        text.setContent(msg);
        request.setText(text);
        OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
        at.setIsAtAll(false);
        request.setAt(at);
        TaobaoResponse response = client.execute(request);
        System.out.println(response.getBody());
    } catch (Exception e) {
    }
}
}
