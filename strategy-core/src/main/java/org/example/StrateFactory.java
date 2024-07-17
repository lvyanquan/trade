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

package org.example;

import org.example.indicators.SupertrendIndicator;
import org.example.model.enums.OrderSide;
import org.example.rule.SupertrendRule;
import org.example.rule.CrossedDownCacheIndicatorRule;
import org.example.rule.CrossedPushCacheIndicatorRule;
import org.example.rule.LossByAtrRule;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.aroon.AroonDownIndicator;
import org.ta4j.core.indicators.aroon.AroonUpIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

public interface StrateFactory {

    public static StrateFactory TREND_FACTORY = new StrateFactory() {
        @Override
        public BaseStrategy buildStrategy(BarSeries series) {

            int atrPeriod = 9;
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
            ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);

            // 初始化Aroon指标
            AroonUpIndicator aroonUp = new AroonUpIndicator(series, 25);  // 使用25周期
            AroonDownIndicator aroonDown = new AroonDownIndicator(series, 25);  // 使用25周期
            // Aroon策略规则
//            Rule aroonBullish = new OverIndicatorRule(aroonUp, aroonDown);
            Rule aroonBearish = new UnderIndicatorRule(aroonUp, aroonDown);

            BaseStrategy strategy = new BaseStrategy(
                    new SupertrendRule(supertrendUpIndicator, supertrendDnIndicator, closePriceIndicator, true),
                    new LossByAtrRule(new ATRIndicator(series,9),new ClosePriceIndicator(series), OrderSide.BUY_LONG)
                            .or((new SupertrendRule(supertrendUpIndicator, supertrendDnIndicator, closePriceIndicator, false).and(aroonBearish)))
            );
            return strategy;
        }
    };

    public static StrateFactory TREND_SELL_FACTORY = new StrateFactory() {
        @Override
        public BaseStrategy buildStrategy(BarSeries series) {

            if (series == null) {
                throw new IllegalArgumentException("Series cannot be null");
            }

            ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);
            int atrPeriod = 9;

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


            // 初始化Aroon指标
            AroonUpIndicator aroonUp = new AroonUpIndicator(series, 20);  // 使用25周期
            AroonDownIndicator aroonDown = new AroonDownIndicator(series, 20);  // 使用25周期

            // Aroon策略规则
            Rule aroonBullish = new OverIndicatorRule(aroonUp, aroonDown);
            Rule aroonBearish = new UnderIndicatorRule(aroonUp, aroonDown);


            // Create a trading strategy
            BaseStrategy strategy = new BaseStrategy(
                    new SupertrendRule(supertrendUpIndicator, supertrendDnIndicator, closePriceIndicator, false).and(aroonBearish),
                    new SupertrendRule(supertrendUpIndicator, supertrendDnIndicator, closePriceIndicator, true).and(aroonBullish)
            );

            return strategy;
        }
    };


    public static StrateFactory MACD_CROSS_FACTORY = new StrateFactory() {
        @Override
        public BaseStrategy buildStrategy(BarSeries series) {
            ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);

            MACDIndicator macdIndicator = new MACDIndicator(closePriceIndicator);
            EMAIndicator macdLine = new EMAIndicator(macdIndicator, 9);

            EMAIndicator emaIndicator = new EMAIndicator(closePriceIndicator, 12);
            EMAIndicator emaIndicator1 = new EMAIndicator(closePriceIndicator, 26);

            //死叉，慢速线（DEA）黄线穿越了快速线（DIF）白线
            Rule macdDeathCross = new CrossedDownCacheIndicatorRule(macdLine, macdIndicator).
                    and(new CrossedDownCacheIndicatorRule(emaIndicator, emaIndicator1));
            //金叉 快速线（DIF）白线突破了慢速线（DEA）黄线
            Rule macdGoldenCross = new CrossedPushCacheIndicatorRule(macdLine, macdIndicator).and(new CrossedPushCacheIndicatorRule(emaIndicator, emaIndicator1));

            // Create a trading strategy
            BaseStrategy strategy = new BaseStrategy(
                    macdGoldenCross,
                    macdDeathCross
            );
            return strategy;
        }
    };

    BaseStrategy buildStrategy(BarSeries series);
}
