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

package org.example.indicators;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.AbstractIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.MedianPriceIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

public  class SupertrendIndicator extends AbstractIndicator<Num> {
    private DoubleNum multiplier;
    private ATRIndicator atrIndicator;
    private MedianPriceIndicator highIndicator;
    private ClosePriceIndicator closePriceIndicator;

    private boolean up;

    public SupertrendIndicator(BarSeries series, int period, double multiplier, boolean up) {
        super(series);
        this.multiplier = DoubleNum.valueOf(multiplier);
        this.atrIndicator = new ATRIndicator(series, period);
        this.highIndicator = new MedianPriceIndicator(series);
        this.closePriceIndicator = new ClosePriceIndicator(series);
        this.up = up;
    }

    public Num getValue(int index) {
        if (up) {
            Num minus = highIndicator.getValue(index).minus(multiplier.multipliedBy(atrIndicator.getValue(index)));
            if (index <= 0) {
                return minus;
            }
            index--;
            Num prevClosePrice = closePriceIndicator.getValue(index);
            Num prevPrice = this.getValue(index);
            return prevClosePrice.isGreaterThan(minus) ? prevPrice.max(minus) : minus;

        } else {
            Num plus = highIndicator.getValue(index).plus(multiplier.multipliedBy(atrIndicator.getValue(index)));
            if (index <= 0) {
                return plus;
            }
            index--;
            Num prevClosePrice = closePriceIndicator.getValue(index);
            Num prevPrice = this.getValue(index);
            return prevClosePrice.isLessThan(plus) ? prevPrice.min(plus) : plus;
        }
    }
}