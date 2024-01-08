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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.BinanceKlineClient;
import org.example.data.PriceBean;
import org.example.data.currency.Currency;
import org.example.indicators.SuperTrendIndicator2;
import org.example.strategy.TrendType;
import org.example.util.KlineUtil;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.aroon.AroonFacade;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.Map;

public class TrendTest {

    private Currency currency;

    public TrendTest(Currency currency) {
        this.currency = currency;
    }

    public static final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    private TrendType type4h = TrendType.NEUTRAL;

    private Bar lastBar;

    private BaseBarSeries series = new BaseBarSeriesBuilder().withNumTypeOf(DoubleNum::valueOf).withName("Aroon data").build();

    SuperTrendIndicator2 trend = new SuperTrendIndicator2(series, 9, 3d);
    final AroonFacade facade = new AroonFacade(series, 5);

    private LinkedList<Integer> trends4h = new LinkedList<>();

    public TrendType getType4h() {
        return type4h;
    }

    public void initTrend(String intervel) throws Exception {

        long e = System.currentTimeMillis();
        long s = e - (120 * 60 * 60 * 1000);

        //4小时k线级别趋势
        for (PriceBean priceBean : KlineUtil.getBar(currency.symbol(), intervel, s, e)) {
            Bar newBar = BaseBar.builder(DoubleNum::valueOf, Double.class)
                    .timePeriod(Duration.ofMinutes(1))
                    .endTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(priceBean.getEndTime()), ZoneId.systemDefault()))
                    .openPrice(priceBean.getOpen().doubleValue())
                    .highPrice(priceBean.getHigh().doubleValue())
                    .lowPrice(priceBean.getLow().doubleValue())
                    .closePrice(priceBean.getClose().doubleValue())
                    .volume(priceBean.getVolume().doubleValue())
                    .build();
            series.addBar(newBar);
            trends4h.addLast(currentTrend(newBar, trend, facade));
            if (trends4h.size() > 20) {
                trends4h.removeFirst();
            }
            TrendType update = update(trends4h);
            if (update != type4h) {
                System.out.println("当前趋势发生了变化 " + type4h + " ->" + update);
            }
            type4h = update;
        }

        BinanceKlineClient binanceKlineClient = new BinanceKlineClient();

        binanceKlineClient.subscribe(currency.symbol(), intervel, t -> {
            Map map = null;
            try {
                map = objectMapper.readValue(t.toString(), Map.class);
            } catch (JsonProcessingException e1) {
                throw new RuntimeException(e1);
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
            handler(newBar, trend, facade);
        });
    }

    public static TrendType update(LinkedList<Integer> trends) {
        //20根k线 80%以上就是verygood 且最近3根k线是上升的 60%以上就是good 50%中性（看当前最新k线信号）
        if (trends.size() < 3) {
            return TrendType.NEUTRAL;
        }
        int up = 0;
        int down = 0;
        int size = trends.size();
        int i = trends.get(size - 1) + trends.get(size - 2) + trends.get(size - 3);
        boolean latest3 = i > 0;
        boolean latest3Sell = trends.get(size - 1) + trends.get(size - 2) + trends.get(size - 3) < 0;


        double v = size * 0.8;
        double v1 = size * 0.6;
        for (Integer trend : trends) {
            if (trend > 0) {
                up++;
            } else if (trend < 0) {
                down++;
            }
        }
        if (up > v) {
            if (latest3) {
                return TrendType.VERY_GOOD;
            } else if (trends.get(size - 1) + trends.get(size - 2) >= 0) {
                return TrendType.GOOD;
            } else {
                return TrendType.NEUTRAL;
            }
        } else if (up > v1 && i >= 0) {
            return TrendType.GOOD;
        } else if (down > v) {
            if (latest3Sell) {
                return TrendType.VERRY_SELL;
            } else if (trends.get(size - 1) + trends.get(size - 2) <= 0) {
                return TrendType.SELL;
            } else {
                return TrendType.NEUTRAL;
            }
        } else if (down > v1 && trends.get(size - 1) + trends.get(size - 2) <= 0) {
            return TrendType.SELL;
        } else if (i <= -4) {
            return TrendType.SELL;
        }
        return TrendType.NEUTRAL;
    }

    public int currentTrend(Bar newBar, SuperTrendIndicator2 i1, AroonFacade i2) {
        Num value = i1.getValue(series.getEndIndex());
        Num value1 = i2.oscillator().getValue(series.getEndIndex());
        int sup = 0;
        int sup1 = 0;
        if (newBar.getClosePrice().minus(value).doubleValue() > 0) {
            sup = 1;
        } else {
            sup = -1;
        }

        if (value1.doubleValue() > 0) {
            if (value1.doubleValue() > 80) {
                sup1 = 2;
            } else {
                sup1 = 1;
            }
        } else {
            if (value1.doubleValue() < -80) {
                sup1 = -2;
            } else {
                sup1 = -1;
            }
        }
        return sup1 + sup;
    }

    protected void handler(Bar newBar, SuperTrendIndicator2 i1, AroonFacade i2) {

        if (lastBar == null || lastBar.getEndTime().isEqual((newBar.getEndTime()))) {
            lastBar = newBar;
            return;
        }
        try {
            series.addBar(newBar);
        } catch (Exception e) {
            System.out.println(lastBar.getEndTime() + "   " + newBar.getEndTime());
            throw e;
        }

        trends4h.addLast(currentTrend(newBar, i1, i2));
        if (trends4h.size() > 20) {
            trends4h.removeFirst();
        }
        TrendType update = update(trends4h);
        if (update != type4h) {
            System.out.println("当前趋势发生了变化 " + type4h + " ->" + update);
        }
        type4h = update;
        lastBar = newBar;
    }


}
