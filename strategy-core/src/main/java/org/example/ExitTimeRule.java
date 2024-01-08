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

import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

public class ExitTimeRule implements Rule {
    private int indexNum;
    private double range;
    private ClosePriceIndicator closePriceIndicator;

    public ExitTimeRule(int indexNum, double range, ClosePriceIndicator closePriceIndicator) {
        this.indexNum = indexNum;
        this.range = (1+range);
        this.closePriceIndicator = closePriceIndicator;
    }

    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
        if (tradingRecord.getCurrentPosition() != null && tradingRecord.getCurrentPosition().isOpened()) {
            Num pricePerAsset = tradingRecord.getCurrentPosition().getEntry().getPricePerAsset();
            int index = tradingRecord.getCurrentPosition().getEntry().getIndex();
            return i - index > indexNum && closePriceIndicator.getValue(i).isGreaterThan(pricePerAsset) && closePriceIndicator.getValue(i).isLessThan(pricePerAsset.multipliedBy(closePriceIndicator.getBarSeries().numOf(range)));
        }
        return false;
    }
}
