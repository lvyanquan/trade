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
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.AbstractIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.num.Num;

public class RsiWeightIndicator extends AbstractIndicator<Num> {

    private RSIIndicator rsiIndicator;

    private Num lower;
    private Num high;

    public RsiWeightIndicator(RSIIndicator rsiIndicator, int lower, int high) {
        super(rsiIndicator.getBarSeries());
        this.rsiIndicator = rsiIndicator;
        this.lower = numOf(lower);
        this.high = numOf(high);
    }


    @Override
    public Num getValue(int i) {
        if (rsiIndicator.getValue(i).isLessThan(lower)) {
            return numOf(1);
        } else if (rsiIndicator.getValue(i).isGreaterThan(high)) {
            return numOf(-1);
        }
        return numOf(0);
    }
}
