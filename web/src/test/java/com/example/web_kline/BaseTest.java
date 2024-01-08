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

import org.example.BinanceKlineClient;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.DoubleNum;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

public abstract class BaseTest {
    public static final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private Strategy strategy;
    private TradingRecord tradingRecord;
    protected Bar lastBar = null;
    protected BaseBarSeries series;

    private double price2 = 0;

    private long lastTrandeTIme;
    private boolean shouldSleep = false;
    private boolean stop = false;

    private int continueIndex = 0;

    public BaseTest() {
        this.series = new BaseBarSeries("mySeries", DoubleNum::valueOf);
        this.strategy = buildStrategy(series);
        loadHistory();
    }

    protected void test(String symbol, String interval) throws Exception {
        BinanceKlineClient binanceKlineClient = new BinanceKlineClient();
        binanceKlineClient.subscribe(symbol, interval, t -> {
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
    }


    protected void handler(Bar newBar) {

        if (lastBar == null || lastBar.getEndTime().isEqual((newBar.getEndTime()))) {
            lastBar = newBar;
            return;
        }
        series.addBar(newBar);

        int endIndex = series.getEndIndex();
        if (strategy.shouldEnter(endIndex)) {
            if (shouldSleep && Timestamp.valueOf(newBar.getEndTime().toLocalDateTime()).getTime() - lastTrandeTIme < 300000) {
                return;
            }
            shouldSleep = false;
            // Our strategy should enter
//                System.out.println("Strategy should ENTER on " + endIndex);
            TradingRecord tradingRecordTemp = new BaseTradingRecord();
            boolean entered = tradingRecordTemp.enter(endIndex, newBar.getClosePrice(), DoubleNum.valueOf(10));
            if (entered && tradingRecord == null) {
                Trade entry = tradingRecordTemp.getLastEntry();
                tradingRecord = tradingRecordTemp;
                System.out.println("Entered on " + entry.getIndex() + "[" + newBar + "]" + " (price=" + entry.getNetPrice().doubleValue()
                        + ", amount=" + entry.getAmount().doubleValue() + ")");
            }
        }

        if (strategy.shouldExit(endIndex)) {
            if (shouldSleep && Timestamp.valueOf(newBar.getEndTime().toLocalDateTime()).getTime() - lastTrandeTIme < 300000) {
                return;
            }
            shouldSleep = false;
            // Our strategy should exit
//                System.out.println("Strategy should EXIT on " + endIndex);
            if (tradingRecord != null) {
                boolean exited = tradingRecord.exit(endIndex, newBar.getClosePrice(), DoubleNum.valueOf(10));
                if (exited) {
                    Trade exit = tradingRecord.getLastExit();
                    Trade entry = tradingRecord.getLastEntry();
                    double v = exit.getNetPrice().doubleValue() - entry.getNetPrice().doubleValue();
                    price2 += v;
                    if (v < 0) {
                        continueIndex++;
                    }else{
                        continueIndex = 0;
                    }

                    /// TODO: 再加一个如果出现了止损单 就停止交易5分钟
                    if (v > 100 || continueIndex > 3) {
                        lastTrandeTIme = Timestamp.valueOf(newBar.getEndTime().toLocalDateTime()).getTime();
                        shouldSleep = true;
                    }
//                    if(price2 < -200){
//                        stop = true;
//                        throw new RuntimeException("结束任务，目前亏损" + price2);
//                    }
                    System.out.println("Exited on " + exit.getIndex() + "[" + newBar + "]" + " (price=" + exit.getNetPrice().doubleValue()
                            + ", amount=" + exit.getAmount().doubleValue() + " 盈利" + v + " 总盈利" + price2 + ")");
                }
                tradingRecord = null;
            }
        }

        lastBar = newBar;
    }

    public abstract Strategy buildStrategy(BarSeries series);

    public void loadHistory() {
    }

    ;
}
