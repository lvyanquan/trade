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
import org.ta4j.core.indicators.candles.BullishHaramiIndicator;


/**
 * 看涨孕育形态是一种烛台图表指标，表明看跌趋势可能即将结束。一些投资者可能将看涨孕育视为他们应该建立资产多头头寸的好兆头。
 */
public class BullishHaramiRule implements Rule {
    private BullishHaramiIndicator indicator;
    public BullishHaramiRule(BarSeries series) {
        indicator = new BullishHaramiIndicator(series);
    }

    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
        return indicator.getValue(i);
    }
}
