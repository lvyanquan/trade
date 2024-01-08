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

import org.example.data.PriceBean;
import org.example.indicators.SupertrendIndicator;
import org.example.rule.SupertrendRule;
import org.example.util.KlineUtil;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
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

public class AtrTest extends BaseTest {


    public AtrTest() {
        super();
    }

    public static void main(String[] args) throws Exception {
        AtrTest test = new AtrTest();

        test.test("BTCUSDT", "5m");
    }


    @Override
    public Strategy buildStrategy(BarSeries series) {
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
        AroonUpIndicator aroonUp = new AroonUpIndicator(series, 25);  // 使用25周期
        AroonDownIndicator aroonDown = new AroonDownIndicator(series, 25);  // 使用25周期

        // Aroon策略规则
        Rule aroonBullish = new OverIndicatorRule(aroonUp, aroonDown);
        Rule aroonBearish = new UnderIndicatorRule(aroonUp, aroonDown);

        // Create a trading strategy
        Strategy strategy = new BaseStrategy(
                new SupertrendRule(supertrendUpIndicator, supertrendDnIndicator, closePriceIndicator, true).and(aroonBullish),
                new SupertrendRule(supertrendUpIndicator, supertrendDnIndicator, closePriceIndicator, false).or(aroonBearish)
        );

        return strategy;
    }

    @Override
    public void loadHistory() {
        long e = System.currentTimeMillis();
        long s = e - (3000 * 60 * 1000);
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
            handler(newBar);
        }
    }
}


