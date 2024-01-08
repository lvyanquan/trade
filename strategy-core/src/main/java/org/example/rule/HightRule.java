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

package org.example.rule;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;


/**
 * 当前是不是最近 count 根线内最高的
 */
public class HightRule implements Rule {
    HighestValueIndicator highestValueIndicator;
    ClosePriceIndicator closePriceIndicator;


    public HightRule(BarSeries barSeries, ClosePriceIndicator closePriceIndicator) {
        this.highestValueIndicator = new HighestValueIndicator(new HighPriceIndicator(barSeries), 7);
        this.closePriceIndicator = closePriceIndicator;
    }

    public HightRule(BarSeries barSeries, int count, ClosePriceIndicator closePriceIndicator) {
        this.highestValueIndicator = new HighestValueIndicator(new HighPriceIndicator(barSeries), count);
        this.closePriceIndicator = closePriceIndicator;
    }

    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
          return this.closePriceIndicator.getValue(i).isGreaterThanOrEqual(highestValueIndicator.getValue(i));
    }
}
