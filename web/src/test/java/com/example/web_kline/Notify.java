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

package com.example.web_kline;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.codec.binary.Base64;
import org.example.binance.BinanceCurrencyRegister;
import org.example.binance.factory.KlineFactory;
import org.example.kline.KlineClient;
import org.example.model.currency.BaseCurrency;
import org.example.model.currency.CurrencySupplierRegister;
import org.example.model.enums.Server;
import org.example.model.enums.ContractType;
import org.example.notify.DinDinNotify;
import org.example.responsity.JdbcTest;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.AbstractIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.web_kline.Notify.dinDinNotify;
import static com.example.web_kline.TestIndicator.template;
import static com.example.web_kline.test2.TraderTemplate.objectMapper;

public class Notify {
    protected BarSeries barSeries = new BaseBarSeries("mySeries", DoubleNum::valueOf);
    static final OkHttpClient client = new OkHttpClient();

    final KlineClient binanceKlineClient = KlineFactory.Create(Server.BINANCE, ContractType.UMFUTURE);

    static OkHttpClient eagerClient = client.newBuilder()
            .readTimeout(10000, TimeUnit.MILLISECONDS)
            .build();

    static DinDinNotify dinDinNotify = new DinDinNotify();

    public static void main(String[] args) {
        CurrencySupplierRegister.getSupplie(";");
        BinanceCurrencyRegister.currencys();
        new Notify().test();
//        UMWebsocketClientImpl client = new UMWebsocketClientImpl();
        /**
         * [
         *   {
         *     "e": "24hrMiniTicker",  // 事件类型
         *     "E": 123456789,         // 事件时间(毫秒)
         *     "s": "BNBUSDT",          // 交易对
         *     "c": "0.0025",          // 最新成交价格
         *     "o": "0.0010",          // 24小时前开始第一笔成交价格
         *     "h": "0.0025",          // 24小时内最高成交价
         *     "l": "0.0010",          // 24小时内最低成交价
         *     "v": "10000",           // 成交量
         *     "q": "18"               // 成交额
         *   }
         * ]
         */
//        client.allMiniTickerStream(s -> {
//            System.out.println(s);
//        });

        /**
         * {
         *  感觉没啥意义
         *
         *     "e":"forceOrder",                   // 事件类型
         *     "E":1568014460893,                  // 事件时间
         *     "o":{
         *
         *         "s":"BTCUSDT",                   // 交易对
         *         "S":"SELL",                      // 订单方向
         *         "o":"LIMIT",                     // 订单类型
         *         "f":"IOC",                       // 有效方式
         *         "q":"0.014",                     // 订单数量
         *         "p":"9910",                      // 订单价格
         *         "ap":"9910",                     // 平均价格
         *         "X":"FILLED",                    // 订单状态
         *         "l":"0.014",                     // 订单最近成交量
         *         "z":"0.014",                     // 订单累计成交量
         *         "T":1568014460893,              // 交易时间
         *
         *     }
         *
         * }
         */
//        client.allForceOrderStream(s -> {
//            System.out.println(s);
//        });
//        new Notify().test2();
//        int i = 0;
//        for (Currency currency : CurrencyRegister.currencys()) {
//            if (currency.symbol().toUpperCase().contains("UNI") || currency.symbol().toUpperCase().contains("BSV") || currency.symbol().toUpperCase().contains("ORDI") || currency.symbol().toUpperCase().contains("WLD") || currency.symbol().toUpperCase().contains("SUI") || currency.symbol().toUpperCase().contains("ETH") || currency.symbol().toUpperCase().contains("BTC") || currency.symbol().toUpperCase().contains("PYTH") || currency.symbol().toUpperCase().contains("BNT")) {
//            } else {
//                if (i <= 20) {
//                    new Notify().notify(currency.symbol(), false, true);
//                }
//                i++;
//            }
//        }
//        new Notify().notify("1000PEPEUSDT", true, true);
//        new Notify().notify("STXUSDT", true, true);
//        new Notify().notify("ETHUSDT", true, true);
        new Notify().notify("BTCUSDT", true, true);
        new Notify().notify("ONDOUSDT", true, true);
//        new Notify().notify("WLDUSDT", true, true);
//        new Notify().notify("MASKUSDT", true, true);


//        new Notify().notify("PYTHUSDT", true, true);
//        new Notify().notify("BNTUSDT", true, true);
//        new Notify().notify("UNIUSDT", false, true);
//        new Notify().notify("BSVUSDT", false, true);
//        new Notify().notify("ORDIUSDT", false, true);
//        new Notify().notify("WLDUSDT", false, true);
//        new Notify().notify("SUIUSDT", false, true);
    }

    //不是实时的 差了十几分钟 合约 多空比
    // {
    //          "buySellRatio":"1.5586",
    //          "buyVol": "387.3300", // 主动买入量
    //          "sellVol":"248.5030", // 主动卖出量
    //          "timestamp":"1585614900000"
    //    },


    void notify(String currency, boolean price, boolean print) {

        try {
            TestIndicator testIndicator = new TestIndicator(barSeries, 2, 1, currency);
            binanceKlineClient.handlerStreamingKlineData(BaseCurrency.of(currency, 0, 0), "5m", t -> {
                Bar newBar = BaseBar.builder(DoubleNum::valueOf, Double.class)
                        .timePeriod(Duration.ofMinutes(1))
                        .endTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(t.getEndTime()), ZoneId.systemDefault()))
                        .openPrice(t.getOpen())
                        .highPrice(t.getHigh())
                        .lowPrice(t.getLow())
                        .closePrice(t.getClose())
                        .volume(t.getQuantity())
                        .build();

                if (barSeries.getEndIndex() != -1 && newBar.getEndTime().isEqual(barSeries.getBar(barSeries.getEndIndex()).getEndTime())) {
                    barSeries.addBar(newBar, true);
                } else {
                    barSeries.addBar(newBar);
                }
                if (print) {
                    testIndicator.getValue(barSeries.getEndIndex());
                }
            }, t -> {
                Bar newBar = BaseBar.builder(DoubleNum::valueOf, Double.class)
                        .timePeriod(Duration.ofMinutes(1))
                        .endTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(t.getEndTime()), ZoneId.systemDefault()))
                        .openPrice(t.getOpen())
                        .highPrice(t.getHigh())
                        .lowPrice(t.getLow())
                        .closePrice(t.getClose())
                        .volume(t.getQuantity())
                        .build();
                if (price) {
                    dinDinNotify.send(currency + " 币安usdt永续合约价格通知\n" + template(newBar));
                }
            });
        } catch (Exception e) {
        }
    }

    public static String getDIndinUrl() {
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

    public void test() {
        new Thread(() -> {
            ArrayList<TestIndicator.C2CPRICE> c2CPRICES = new ArrayList<>();
            while (true) {
                try {
                    Thread.sleep(900000);
                } catch (InterruptedException e) {

                }

                ArrayList<Double> doubles = new ArrayList<>();
                for (int i = 1; i < 6; i++) {
                    final Request.Builder requestBuilder = new Request.Builder()
                            .addHeader("Content-Type", "application/json")
                            .addHeader("User-Agent", "PostmanRuntime/7.29.2")
                            .addHeader("Accept", "*/*")
                            .addHeader("Host", "p2p.binance.com")
                            .addHeader("Accept-Language", "zh,zh-CN;q=0.9,en;q=0.8")
//                .addHeader("Accept-Encoding", "gzip, deflate, br, zstd")
//                .addHeader("Content-Length", "200")

                            .url("https://p2p.binance.com/bapi/c2c/v2/friendly/c2c/adv/search")
                            .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "{\"fiat\":\"CNY\",\"page\":" + i + ",\"rows\":20,\"tradeType\":\"BUY\",\"asset\":\"USDT\",\"countries\":[],\"proMerchantAds\":false,\"shieldMerchantAds\":false,\"publisherType\":null,\"payTypes\":[],\"classifies\":[\"mass\",\"profession\"]}"));

                    try {
                        Response response = eagerClient.newCall(requestBuilder.build()).execute();
                        String string = response.body().string();
                        Map<String, Object> map = objectMapper.readValue(string, Map.class);
                        List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");
                        data.forEach(k -> {
                            Map<String, String> s = (Map<String, String>) k.get("adv");
                            doubles.add(Double.valueOf(s.get("price")));
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (doubles.size() <= 0) {
                    return;
                }
                //最高价
                double high = BigDecimal.valueOf(doubles.stream().mapToDouble(k -> k).max().getAsDouble()).setScale(4, 1).doubleValue();
                //最低价
                double low = BigDecimal.valueOf(doubles.stream().mapToDouble(k -> k).min().getAsDouble()).setScale(4, 1).doubleValue();
                //平均价
                double average = BigDecimal.valueOf(doubles.stream().mapToDouble(k -> k).sum() / doubles.size()).setScale(4, 1).doubleValue();
                //时间
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                dinDinNotify.send((String.format("c2c购买usdt价格:\n" +
                                "最高价: %s\n" +
                                "最低价: %s\n" +
                                "平均价: %s\n" +
                                "时间: %s",
                        high,
                        low,
                        average,
                        timestamp)));
                c2CPRICES.add(new TestIndicator.C2CPRICE(high, low, average, timestamp));
                if (c2CPRICES.size() == 4) {
                    double high1 = c2CPRICES.stream().mapToDouble(TestIndicator.C2CPRICE::getHigh).max().getAsDouble();
                    double low1 = c2CPRICES.stream().mapToDouble(TestIndicator.C2CPRICE::getLow).max().getAsDouble();
                    boolean up = c2CPRICES.get(0).getAverg() < c2CPRICES.get(c2CPRICES.size() - 1).getAverg();
                    String desc = "上升";
                    double s = c2CPRICES.get(0).getAverg();
                    double e = c2CPRICES.get(c2CPRICES.size() - 1).getAverg();
                    if (!up) {
                        desc = "下降";
                    }

                    dinDinNotify.send((String.format("c2c购买usdt价格变动情况：\n" +
                            "最高价：%s\n" +
                            "最低价：%s\n" +
                            "平均价【%s】 %s --> %s\n" +
                            "时间范围 %s --> %s", high1, low1, desc, s, e, c2CPRICES.get(0).getTimestamp(), c2CPRICES.get(c2CPRICES.size() - 1).getTimestamp())));
                    c2CPRICES.clear();
                }

                try {
                    JdbcTest.insert(high, low, average, timestamp);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }).start();
    }
}


class TestIndicator extends AbstractIndicator {
    String currency;
    private int count;
    private int interval;

    private int lastIndex = 0;

    protected TestIndicator(BarSeries series, int count, int interval, String currency) {
        super(series);
        this.count = count;
        this.interval = interval;
        this.currency = currency;
    }

    @Override
    public Object getValue(int i) {
        int start = Math.max(0, i - count + 1);
        Num highPrice = numOf(0);
        Num lowPrice = numOf(0);
        Num openPrice = getBarSeries().getBar(start).getOpenPrice();
        int highIndex = start;
        int lowIndex = start;
        for (; start <= i; start++) {
            Bar bar = getBarSeries().getBar(start);
            if (bar.getHighPrice().isGreaterThan(highPrice)) {
                highPrice = bar.getHighPrice();
                highIndex = start;
            }

            if (bar.getLowPrice().isLessThan(lowPrice)) {
                lowPrice = bar.getLowPrice();
                lowIndex = start;
            }
        }
        if (highIndex == i && i - lastIndex > interval && highPrice.minus(openPrice).dividedBy(openPrice).isGreaterThan(numOf(0.01))) {
            dinDinNotify.send((currency + "\n 类型：【迅速上升】" + highPrice.minus(openPrice).dividedBy(openPrice).doubleValue() * 100 + "%\n, 当前价格：" + getBarSeries().getBar(i).getClosePrice()));
            lastIndex = i;
        }

        if (lowIndex == i && i - lastIndex > interval && openPrice.minus(lowPrice).dividedBy(openPrice).isGreaterThan(numOf(0.01))) {
            dinDinNotify.send((currency + "\n 类型：【迅速下跌】" + openPrice.minus(lowPrice).dividedBy(openPrice).doubleValue() * 100 + "%\n, 当前价格：" + getBarSeries().getBar(i).getClosePrice()));
            lastIndex = i;
        }

        return null;
    }

    public static String template(Bar bar) {
        return String.format("开盘价：%s\n" +
                "最高价: %s\n" +
                "最低价: %s\n" +
                "收盘价: %s\n" +
                "收盘时间: %s ", bar.getOpenPrice(), bar.getHighPrice(), bar.getLowPrice(), bar.getClosePrice(), bar.getEndTime());
    }


    static class C2CPRICE {
        final private double high;
        final private double low;
        final private double average;
        final private Timestamp timestamp;

        public C2CPRICE(double high, double low, double average, Timestamp timestamp) {
            this.high = high;
            this.low = low;
            this.average = average;
            this.timestamp = timestamp;
        }

        public double getHigh() {
            return high;
        }

        public double getLow() {
            return low;
        }

        public double getAverg() {
            return average;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }
    }
}
