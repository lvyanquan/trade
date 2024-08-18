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

package org.example.core.indicator.ta4j;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.AbstractIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

public class AtrRatioIndicator extends AbstractIndicator<Num> {
    private ClosePriceIndicator closePrice;
    private ATRIndicator atr;

    public AtrRatioIndicator(ClosePriceIndicator closePriceIndicator, ATRIndicator atrIndicator) {
        super(closePriceIndicator.getBarSeries());
        this.closePrice = closePriceIndicator;
        this.atr = atrIndicator;
    }

    @Override
    public Num getValue(int i) {
        BarSeries barSeries = closePrice.getBarSeries();
        Num atrValue = atr.getValue(barSeries.getEndIndex());
        Num closePriceValue = closePrice.getValue(barSeries.getEndIndex());
        return this.numOf(atrValue.dividedBy(closePriceValue).doubleValue());
    }
}
