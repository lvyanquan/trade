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
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

public class GenericAverageIndicator extends CachedIndicator<Num> {
    private final Indicator<Num> indicator;
    private final int length;

    public GenericAverageIndicator(Indicator<Num> indicator, int length) {
        super(indicator.getBarSeries());
        this.indicator = indicator;
        this.length = length;
    }

    @Override
    protected Num calculate(int index) {
        Num sum = numOf(0);
        int actualLength = 0;
        for (int i = Math.max(0, index - length + 1); i <= index; i++) {
            sum = sum.plus(indicator.getValue(i));
            actualLength++;
        }
        return actualLength > 0 ? sum.dividedBy(numOf(actualLength)) : sum;
    }
}

