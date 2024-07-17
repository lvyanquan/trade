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

package org.example.rule.volume;

import org.example.indicators.volumn.AverageVolumeIndicator;
import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.AbstractRule;

public class VolumnUperFastRule extends AbstractRule {
    private BarSeries series;
    private AverageVolumeIndicator indicator;
    private Num multity;

    public VolumnUperFastRule(BarSeries series, AverageVolumeIndicator indicator,double multity) {
        this.series = series;
        this.multity = series.numOf(multity);
        this.indicator = indicator;
    }

    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
        return series.getBar(i).getVolume().isGreaterThanOrEqual(indicator.getValue(i).multipliedBy(multity));
    }
}
