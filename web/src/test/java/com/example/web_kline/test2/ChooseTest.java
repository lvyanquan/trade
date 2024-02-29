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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.BinanceKlineClient;
import org.example.data.PriceBean;
import org.example.data.currency.Currency;
import org.example.data.currency.CurrencyRegister;
import org.example.indicators.macd.DEAIndicator;
import org.example.rule.CrossedDownCacheIndicatorRule;
import org.example.rule.CrossedPushCacheIndicatorRule;
import org.example.rule.NumericRule;
import org.example.rule.WeightRule;
import org.example.rule.WeightRuleWrapper;
import org.example.strategy.CollectRuleStrategy;
import org.example.util.KlineUtil;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ChooseTest {

   static BinanceKlineClient binanceKlineClient = new BinanceKlineClient();
    protected long e = System.currentTimeMillis();
    protected long s = e - (10 * 24 * 60 * 60 * 1000);
    public  Bar lastBar;
    static Map<Currency, Data> currencyIntegerHashMap = new HashMap<>();

    public static void main(String[] args) throws Exception {
        AtomicInteger i1 = new AtomicInteger();
        CurrencyRegister.currencys().forEach(i->{
            i1.getAndIncrement();
            if(i1.get() > 50){
                return;
            }
            new ChooseTest().test2(i,"15m");

        });

      while (true){
          List<Data> collect = currencyIntegerHashMap.values().stream().sorted(Comparator.comparingInt(Data::getWeight)).collect(Collectors.toList());
          String s = TraderTemplate.objectMapper.writeValueAsString(collect);
          System.out.println(s);
          Thread.sleep(15 * 60 * 1000);
      }
    }

    public void test2(Currency currency, String interval) {
        BaseBarSeries series = new BaseBarSeries("mySeries", DoubleNum::valueOf);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        EMAIndicator eemaIndicator25 = new EMAIndicator(closePrice, 25);
        EMAIndicator emaIndicator7 = new EMAIndicator(closePrice, 7);
        Rule emaGoldenCross = new CrossedPushCacheIndicatorRule(emaIndicator7, eemaIndicator25);
        WeightRuleWrapper ema7日线上穿25日线 = new WeightRuleWrapper("ema7日线上穿25日线", 3, emaGoldenCross);

        //ema 交叉
        MACDIndicator difIndicator = new MACDIndicator(closePrice);
        DEAIndicator deaIndicator = new DEAIndicator(series, difIndicator);
        WeightRuleWrapper macd金叉 = new WeightRuleWrapper("macd金叉", 2, new CrossedDownCacheIndicatorRule(deaIndicator, difIndicator, 3));


        RSIIndicator rsiIndicator = new RSIIndicator(closePrice, 14);
        WeightRuleWrapper rsiEnterWeightRuleWrapper = new WeightRuleWrapper("rsi低于30", 1, new NumericRule(rsiIndicator, new ConstantIndicator(series, series.numOf(30)), Num::isLessThan));

        ArrayList<WeightRule> weightRules = new ArrayList<>();
        weightRules.add(rsiEnterWeightRuleWrapper);
        weightRules.add(macd金叉);
        weightRules.add(ema7日线上穿25日线);
        CollectRuleStrategy collectRuleStrategy = new CollectRuleStrategy(weightRules, true, 20, 3);

        test(currency, interval, series, collectRuleStrategy);
    }

    public void test(Currency currency, String interval, BaseBarSeries series, CollectRuleStrategy collectRuleStrategy) {

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

        }

        try {
            binanceKlineClient.subscribe(currency.symbol(), interval, t -> {
                Map map = null;
                try {
                    map = TraderTemplate.objectMapper.readValue(t.toString(), Map.class);
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

                if (lastBar == null || lastBar.getEndTime().isEqual((newBar.getEndTime()))) {
                    lastBar = newBar;
                    return;
                }
                try {
                    series.addBar(lastBar);
                    lastBar = newBar;
                } catch (Exception e) {
                    System.out.println(lastBar.getEndTime() + "   " + newBar.getEndTime());
                    throw e;
                }

                collectRuleStrategy.shouldEnter(series.getEndIndex());
                currencyIntegerHashMap.put(currency, new Data(currency.symbol(), collectRuleStrategy.getEffectiveWeight()));

            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static class Data {
        private String currency;
        private int weight;

        public Data(String currency, int weight) {
            this.currency = currency;
            this.weight = weight;
        }

        public String getCurrency() {
            return currency;
        }

        public int getWeight() {
            return weight;
        }
    }
}
