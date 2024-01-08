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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.RecursiveCachedIndicator;
import org.ta4j.core.num.Num;

public class BollingWidthDescriptiveIndicator extends CachedIndicator<Num> {
    private int percent;
    private int num;
    Indicator<Num> indicator;

    public BollingWidthDescriptiveIndicator(Indicator<Num> indicator, int num, int percent) {
        super(indicator.getBarSeries());
        this.num = num;
        this.percent = percent;
        this.indicator = indicator;
    }

    @Override
    protected Num calculate(int i) {
        int start = Math.max(0, i - num + 1);
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(num);
        for (; start <= i; start++) {
            descriptiveStatistics.addValue(indicator.getValue(start).doubleValue());
        }
        return getBarSeries().numOf(descriptiveStatistics.getPercentile(percent));
    }
}
