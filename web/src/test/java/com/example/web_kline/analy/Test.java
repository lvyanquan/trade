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

package com.example.web_kline.analy;

import org.example.criteria.ReturnCriterion;
import org.example.data.PriceBean;
import org.example.indicators.SupertrendIndicator;
import org.example.rule.SupertrendRule;
import org.example.util.KlineUtil;
import org.ta4j.core.AnalysisCriterion;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.aroon.AroonDownIndicator;
import org.ta4j.core.indicators.aroon.AroonUpIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) {

        BaseBarSeries series = new BaseBarSeriesBuilder().withNumTypeOf(DoubleNum::valueOf).withName("Aroon data").build();

        // The analysis criterion
        AnalysisCriterion returnCriterion = new ReturnCriterion();


        // Building the map of strategies
        Map<Strategy, String> strategies = buildStrategiesMap(series);

        loadHistory(series);

        // For each sub-series...
        System.out.println("Sub-series: " + series.getSeriesPeriodDescription());
        BarSeriesManager sliceManager = new BarSeriesManager(series);
        for (Map.Entry<Strategy, String> entry : strategies.entrySet()) {
            Strategy strategy = entry.getKey();
            String name = entry.getValue();
            // For each strategy...
            TradingRecord tradingRecord = sliceManager.run(strategy);
            Num profit = returnCriterion.calculate(series, tradingRecord);
            System.out.println("\tProfit for " + name + ": " + profit);
        }
        Strategy bestStrategy = returnCriterion.chooseBest(sliceManager, Trade.TradeType.BUY,
                new ArrayList<Strategy>(strategies.keySet()));
        System.out.println("\t\t--> Best strategy: " + strategies.get(bestStrategy) + "\n");
    }

    private static void loadHistory(BaseBarSeries series) {
        long e = System.currentTimeMillis();
        long s = e - (1000 * 60 * 1000);
        for (PriceBean priceBean : KlineUtil.getBar("BTCUSDT", "5m", s, e)) {
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
        }

    }

    public static Map<Strategy, String> buildStrategiesMap(BarSeries series) {
        HashMap<Strategy, String> strategies = new HashMap<>();

        ClosePriceIndicator closePriceIndicator1 = new ClosePriceIndicator(series);
        int atrPeriod = 9;

        // Define the Supertrend indicator using ATR
        Indicator<Num> supertrendUpIndicator1 = new SupertrendIndicator(
                series,
                atrPeriod,
                3d,
                true// Multiplier
        );

        Indicator<Num> supertrendDnIndicator1 = new SupertrendIndicator(
                series,
                atrPeriod,
                3d,
                false// Multiplier
        );


        Strategy strategy = new BaseStrategy(
                new SupertrendRule(supertrendUpIndicator1, supertrendDnIndicator1, closePriceIndicator1, true),
                new SupertrendRule(supertrendUpIndicator1, supertrendDnIndicator1, closePriceIndicator1, false));

        // 初始化Aroon指标
        AroonUpIndicator aroonUp = new AroonUpIndicator(series, 25);  // 使用25周期
        AroonDownIndicator aroonDown = new AroonDownIndicator(series, 25);  // 使用25周期

        // Aroon策略规则
        Rule  aroonBullish = new OverIndicatorRule(aroonUp, aroonDown);
        Rule aroonBearish = new UnderIndicatorRule(aroonUp, aroonDown);

        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);

        // Define the Supertrend indicator using ATR
        Indicator<Num> supertrendUpIndicator = new SupertrendIndicator(
                series,
                atrPeriod,
                3d,
                true// Multiplier
        );

        Indicator<Num> supertrendDnIndicator = new SupertrendIndicator(
                series,
                atrPeriod,
                3d,
                false// Multiplier
        );



        Strategy strategy2 = new BaseStrategy(
                new SupertrendRule(supertrendUpIndicator, supertrendDnIndicator, closePriceIndicator, true).and(aroonBullish),
                new SupertrendRule(supertrendUpIndicator, supertrendDnIndicator, closePriceIndicator, false).and(aroonBearish));
        // 初始化RSI指标
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);  // 使用14周期

        // RSI策略规则
        Rule rsiOverbought = new OverIndicatorRule(rsi, 70);
        Rule rsiOversold = new UnderIndicatorRule(rsi, 30);



        ClosePriceIndicator closePriceIndicator3 = new ClosePriceIndicator(series);

        // Define the Supertrend indicator using ATR
        Indicator<Num> supertrendUpIndicator3 = new SupertrendIndicator(
                series,
                atrPeriod,
                3d,
                true// Multiplier
        );



        Indicator<Num> supertrendDnIndicator3 = new SupertrendIndicator(
                series,
                atrPeriod,
                3d,
                false// Multiplier
        );

        Strategy strategy3 = new BaseStrategy(
                new SupertrendRule(supertrendUpIndicator3, supertrendDnIndicator3, closePriceIndicator3, true).and(rsiOverbought),
                new SupertrendRule(supertrendUpIndicator3, supertrendDnIndicator3, closePriceIndicator3, false).and(rsiOversold));


        strategies.put(strategy, "a");
        strategies.put(strategy2, "b");
        strategies.put(strategy3, "c");
        return strategies;
    }

}
