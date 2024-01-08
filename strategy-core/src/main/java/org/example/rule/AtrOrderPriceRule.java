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

import org.ta4j.core.Indicator;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.AbstractRule;

public class AtrOrderPriceRule extends AbstractRule {
    private Indicator<Num> indicator;
    private ClosePriceIndicator closePriceIndicator;

    private int multuty;

    public AtrOrderPriceRule(Indicator<Num> indicator, ClosePriceIndicator closePriceIndicator,int multuty) {
        this.indicator = indicator;
        this.closePriceIndicator = closePriceIndicator;
        this.multuty = multuty;
    }

    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
        if (tradingRecord.getCurrentPosition().isOpened()) {
            Trade entry = tradingRecord.getCurrentPosition().getEntry();
            Num value = indicator.getValue(entry.getIndex());
            return closePriceIndicator.getValue(i).isGreaterThan(tradingRecord.getLastEntry().getPricePerAsset().plus((value.multipliedBy(indicator.getBarSeries().numOf(multuty)))));
        }
        return false;
    }
}
