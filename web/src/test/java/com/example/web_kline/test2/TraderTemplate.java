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

import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.binance.connector.futures.client.impl.UMWebsocketClientImpl;
import com.example.web_kline.Order.OrderContext;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.BinanceKlineClient;
import org.example.StrateFactory;
import org.example.data.PriceBean;
import org.example.data.currency.Currency;
import org.example.enums.OrderSide;
import org.example.strategy.CollectRuleStrategy;
import org.example.util.JsonUtil;
import org.example.util.KlineUtil;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Position;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.cost.FixedTransactionCostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    protected TrendTest trendTest;

    protected boolean loadHistory = true;
    protected int continueIndex = 0;
    protected boolean mock = false;

    protected int amount = 1200;

    protected double profit = 0;

    protected Position position = null;

    protected Currency currency;

    protected String interval = "30m";

    double range2 = 5;

    public static UMFuturesClientImpl client = new UMFuturesClientImpl(KlineUtil.API_KEY, KlineUtil.SECRET_KEY, KlineUtil.UM_BASE_URL);

    private UMWebsocketClientImpl websocketClient = new UMWebsocketClientImpl();
    SpotClientImpl spotClient = new SpotClientImpl(KlineUtil.API_KEY, KlineUtil.SECRET_KEY);

    protected long e = System.currentTimeMillis();
    protected long s = e - (5 * 24 * 60 * 60 * 1000);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Thread thread;

    public TraderTemplate(boolean mock, Trade.TradeType tradeType, int amount, String interval, Currency currency, StrateFactory strategy, double range) {
        this.range2 = range;
        this.amount = amount;
        this.currency = currency;
        this.interval = interval;
        thread = new Thread(this::trade);
        thread.setName("trade-thread");
        thread.start();

//        Tradetest tradetest = new Tradetest(currency);
//        try {
//            tradetest.initTrend(interval);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        this.tradetest = tradetest;

        this.orderContext = new OrderContext(2, 1200, tradeType, currency,false);
        this.series = new BaseBarSeries("mySeries", DoubleNum::valueOf);
        this.strategy = strategy.buildStrategy(series);
        if (mock) {
            loadHistory = false;
        }
        this.mock = mock;
        loadHistory();
        loadHistory = false;
        this.orderContext = new OrderContext(2, 1200, tradeType, currency,true);
        System.out.println("历史数据加载结束 " + lastBar);
    }

    public void test(String interval) throws Exception {
        BinanceKlineClient binanceKlineClient = new BinanceKlineClient();
        try {
            binanceKlineClient.subscribe(currency.symbol(), interval, t -> {
                Map map = null;
                try {
                    map = objectMapper.readValue(t.toString(), Map.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                Map<String, Object> data = (Map<String, Object>) map.get("k");

                long closeTime = Long.parseLong(data.get("T").toString());
                Double o = Double.valueOf(data.get("o").toString());
                Double h = Double.valueOf(data.get("c").toString());
                Double l = Double.valueOf(data.get("h").toString());
                Double c = Double.valueOf(data.get("l").toString());
                Double v = Double.valueOf(data.get("v").toString());


                Bar newBar = BaseBar.builder(DoubleNum::valueOf, Double.class)
                        .timePeriod(Duration.ofMinutes(1))
                        .endTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(closeTime), ZoneId.systemDefault()))
                        .openPrice(o)
                        .highPrice(c)
                        .lowPrice(h)
                        .closePrice(l)
                        .volume(v)
                        .build();
                handler(newBar);

            });
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
            System.out.println("newBar："+newBar+" 当前入场有效策略 " + ((CollectRuleStrategy) strategy).getEnterEffectiveRuleName()
                    + "入场分数" + ((CollectRuleStrategy) strategy).getEffectiveWeight());
        }

        for (TradingRecord tradingRecord : orderContext.exit(endIndex, newBar,strategy)) {

            Trade exit = tradingRecord.getLastExit();
            Trade entry = tradingRecord.getLastEntry();
            System.out.println("Exit on " + exit.getIndex() + "[" + newBar + "]" + " (price=" + exit.getNetPrice().doubleValue()
                    + ", amount=" + exit.getAmount().doubleValue() + ")");
            if (!loadHistory && !mock) {
//                orderLimitPingDuo(entry.getNetPrice().doubleValue() + range2);
                queue.add(new Order(entry.getNetPrice().doubleValue(), exit.getAmount().doubleValue(), OrderSide.SELL_LONG, currency.symbol()));
            }

            if (strategy instanceof CollectRuleStrategy) {
                System.out.println("离场有效策略 " + ((CollectRuleStrategy) strategy).getExitEffectiveRuleName()
                        + "离场分数" + ((CollectRuleStrategy) strategy).getEffectiveWeight());
            }

            Position position = tradingRecord.getLastPosition();
            if (position != null && this.position != position) {
                this.position = tradingRecord.getLastPosition();
                double v = calcitePosition(position) * 0.9998 *( position.getEntry().getAmount().doubleValue());
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
            System.out.println("newBar："+newBar+"当前离场有效策略 " + ((CollectRuleStrategy) strategy).getExitEffectiveRuleName()
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
        for (PriceBean priceBean : KlineUtil.getBar2(currency.symbol(), interval, s, e)) {
            Bar newBar = BaseBar.builder(DoubleNum::valueOf, Double.class)
                    .timePeriod(Duration.ofMinutes(1))
                    .endTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(priceBean.getEndTime()), ZoneId.systemDefault()))
                    .openPrice(priceBean.getOpen().doubleValue())
                    .highPrice(priceBean.getHigh().doubleValue())
                    .lowPrice(priceBean.getLow().doubleValue())
                    .closePrice(priceBean.getClose().doubleValue())
                    .volume(priceBean.getVolume().doubleValue())
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

    //做空信号
    public TradingRecord tradeSell(Bar bar, int i) {
        int endIndex = series.getEndIndex();
        double v = amount / (bar.getClosePrice().doubleValue() + range2);
//        if (tradingRecord.getLastEntry() != null && bar.getClosePrice().doubleValue() - tradingRecord.getLastEntry().getNetPrice().doubleValue() > range) {
        TradingRecord exited = orderContext.exit(endIndex, i, bar.getClosePrice());
        if (exited != null) {
            Trade exit = exited.getLastExit();
            Trade entry = exited.getLastEntry();
            System.out.println("Exit on " + exit.getIndex() + "[" + bar + "]" + " (price=" + exit.getNetPrice().doubleValue()
                    + ", amount=" + exit.getAmount().doubleValue() + ")");
            if (!loadHistory && !mock) {
//                orderLimitPingDuo(entry.getNetPrice().doubleValue() + range2);
                queue.add(new Order(entry.getNetPrice().doubleValue(), exit.getAmount().doubleValue(), OrderSide.SELL_LONG, currency.symbol()));
            }

            if (strategy instanceof CollectRuleStrategy) {
                System.out.println("离场有效策略 " + ((CollectRuleStrategy) strategy).getExitEffectiveRuleName()
                        + "离场分数" + ((CollectRuleStrategy) strategy).getEffectiveWeight());
            }
            return exited;
        }
        return null;
//        }
    }


    protected void orderLimitDuo(double price, double amount) {
        for (int i = 0; i < 3; i++) {
            try {
                LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
                //下多单，28000美金，下单的数量是 price * quantity
                parameters.put("symbol", currency.symbol());
                parameters.put("side", "BUY");
                parameters.put("positionSide", "long");
                parameters.put("type", "LIMIT");
                parameters.put("timeInForce", "GTC");
                parameters.put("quantity",BigDecimal.valueOf(amount).setScale(currency.getQuantityPrecision(), RoundingMode.DOWN));
                parameters.put("price", BigDecimal.valueOf(price).setScale(currency.getPricePrecision(), 1));
                String result = client.account().newOrder(parameters);
                System.out.println("买入订单信息：" + result);
                break;
            } catch (Exception e) {
                System.out.println("买多单失败重试：" + e.getMessage());
            }
        }
    }

    protected void orderLimitPingDuo(double price,double amount) {
        for (int i = 0; i < 3; i++) {
            try {
                LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
                LinkedHashMap<String, Object> parameters3 = new LinkedHashMap<>();
                parameters3.put("symbol", currency.symbol());
                parameters3.put("side", "SELL");
                parameters3.put("positionSide", "long");
                parameters3.put("type", "LIMIT");
                parameters3.put("timeInForce", "GTC");
                parameters3.put("quantity", BigDecimal.valueOf(amount).setScale(currency.getQuantityPrecision(), RoundingMode.DOWN));
                parameters3.put("price", BigDecimal.valueOf(price).setScale(currency.getPricePrecision(),  RoundingMode.DOWN));
                String result = client.account().newOrder(parameters3);
                System.out.println("平多单" + result);
                break;
            } catch (Exception e) {
                System.out.println("平多单失败重试：" + e.getMessage());
            }
        }
    }

    protected void orderLimitKong(double price) {
        for (int i = 0; i < 3; i++) {
            try {
                LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
                parameters.put("symbol", currency.symbol());
                parameters.put("side", "SELL");
                parameters.put("positionSide", "SHORT");
                parameters.put("type", "LIMIT");
                parameters.put("timeInForce", "GTC");
                parameters.put("quantity", BigDecimal.valueOf(amount / price).setScale(currency.getQuantityPrecision(), RoundingMode.DOWN));
                parameters.put("price", BigDecimal.valueOf(price).setScale(currency.getPricePrecision(),  RoundingMode.DOWN));
                String result = client.account().newOrder(parameters);
                System.out.println("买入订单信息：" + result);
                break;
            } catch (Exception e) {
                System.out.println("平多单失败重试：" + e.getMessage());
            }
        }
    }


    protected void orderMarketPingDuo(double price) {

        for (int i = 0; i < 3; i++) {
            try {
                LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
                //下多单，28000美金，下单的数量是 price * quantity
                parameters.put("symbol", currency.symbol());
                parameters.put("side", "SELL");
                parameters.put("positionSide", "long");
                parameters.put("type", "MARKET");
                parameters.put("quantity", BigDecimal.valueOf(amount / price).setScale(currency.getQuantityPrecision(), RoundingMode.DOWN));
                String result = client.account().newOrder(parameters);
                System.out.println("平多单信息：" + result);
                break;
            } catch (Exception e) {
                System.out.println("平多单失败重试：" + e.getMessage());
            }
        }
    }


    protected void orderMarketKong(double price) {
        for (int i = 0; i < 3; i++) {
            try {
                LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
                parameters.put("symbol", currency.symbol());
                parameters.put("side", "SELL");
                parameters.put("positionSide", "SHORT");
                parameters.put("type", "MARKET");
                parameters.put("quantity", BigDecimal.valueOf(amount / price).setScale(currency.getQuantityPrecision(), RoundingMode.DOWN));
                String result = client.account().newOrder(parameters);
                System.out.println("买入订单信息：" + result);
                break;
            } catch (Exception e) {
                System.out.println("平多单失败重试：" + e.getMessage());
            }
        }
    }

    protected void orderLimitPingkong(double price) {
        for (int i = 0; i < 3; i++) {
            try {
                LinkedHashMap<String, Object> parameters3 = new LinkedHashMap<>();
                parameters3.put("symbol", currency.symbol());
                parameters3.put("side", "BUY");
                parameters3.put("positionSide", "SHORT");
                parameters3.put("type", "LIMIT");
                parameters3.put("timeInForce", "GTC");
                parameters3.put("quantity", BigDecimal.valueOf(amount / price).setScale(currency.getQuantityPrecision(), RoundingMode.DOWN));
                parameters3.put("price", BigDecimal.valueOf(price).setScale(currency.getPricePrecision(), 1));
                String result = client.account().newOrder(parameters3);
                System.out.println("平空单" + result);
                break;
            } catch (Exception e) {
                System.out.println("平空单失败重试：" + e.getMessage());
            }
        }
    }

    protected void orderMarketPingKong(double price) {
        for (int i = 0; i < 3; i++) {
            try {
                LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
                parameters.put("symbol", currency.symbol());
                parameters.put("side", "BUY");
                parameters.put("positionSide", "SHORT");
                parameters.put("type", "MARKET");
                parameters.put("quantity", BigDecimal.valueOf(amount / price).setScale(currency.getQuantityPrecision(), RoundingMode.DOWN));
                String result = client.account().newOrder(parameters);
                System.out.println("平空单信息：" + result);
                break;
            } catch (Exception e) {
                System.out.println("平空单失败重试：" + e.getMessage());
            }
        }
    }

    protected void orderLimitBuy(double price) {
        for (int i = 0; i < 3; i++) {
            try {
                LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
                //下多单，28000美金，下单的数量是 price * quantity
                parameters.put("symbol", currency.symbol());
                parameters.put("side", "BUY");
                parameters.put("type", "LIMIT");
                parameters.put("timeInForce", "GTC");
                parameters.put("quantity", BigDecimal.valueOf(amount / price).setScale(currency.getQuantityPrecision(), RoundingMode.DOWN));
                parameters.put("price", BigDecimal.valueOf(price).setScale(currency.getPricePrecision(), RoundingMode.DOWN));
                String result = spotClient.createTrade().newOrder(parameters);
                System.out.println("买入订单信息：" + result);
                break;
            } catch (Exception e) {
                System.out.println("买多单失败重试：" + e.getMessage());
            }
        }
    }

    protected void orderLimitSell(double price) {
        for (int i = 0; i < 3; i++) {
            try {
                LinkedHashMap<String, Object> parameters3 = new LinkedHashMap<>();
                parameters3.put("symbol", currency.symbol());
                parameters3.put("side", "SELL");
                parameters3.put("type", "LIMIT");
                parameters3.put("timeInForce", "GTC");
                parameters3.put("quantity", BigDecimal.valueOf(amount / price).setScale(currency.getQuantityPrecision(), RoundingMode.DOWN));
                parameters3.put("price", BigDecimal.valueOf(price).setScale(currency.getPricePrecision(), RoundingMode.DOWN));
                String result = spotClient.createTrade().newOrder(parameters3);
                System.out.println("平多单" + result);
                break;
            } catch (Exception e) {
                System.out.println("平多单失败重试：" + e.getMessage());
            }
        }
    }

    protected void trade() {
        websocketClient.partialDepthStream(currency.symbol(), 20, 100, ((event) -> {
            Order order = queue.peekFirst();
            if (order != null) {
                Map parse = JsonUtil.parse(event, Map.class);
                if (Long.valueOf(parse.get("T").toString()) - System.currentTimeMillis() < 500) {
                    if (order.getOrderSide() == OrderSide.BUY_LONG) {
                        List data = (List) parse.get("a");
                        List o = (List) data.get(data.size() - 4);
                        double v = Double.parseDouble(o.get(0).toString());
                        if (v - order.getPrice() < 0) {
                            orderLimitDuo(v * (1-range2), order.amount);
                            queue.remove(order);
                            System.out.println(simpleDateFormat.format(new Date()) + "开多单, 价格:" + (v - range2));
                        }
                    } else if (order.getOrderSide() == OrderSide.SELL_LONG) {

                        List data = (List) parse.get("b");
                        List o = (List) data.get(3);
                        double v = Double.parseDouble(o.get(0).toString());
                        if (v - order.getPrice() > 0) {
                            orderLimitPingDuo(v * (1+range2),order.amount);
                            queue.remove(order);
                            System.out.println(simpleDateFormat.format(new Date()) + "平多单, 价格:" + (v + range2));
                        }
                    }
                }
            }


        }));
    }


    public static class Order {
        private double price;
        private double amount;
        private OrderSide orderSide;

        private String symbol;

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
    }
}
