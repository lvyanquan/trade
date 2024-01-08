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

package org.example.indicators.macd;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.num.Num;

public class DEAIndicator extends CachedIndicator<Num> {
    private MACDIndicator macdIndicator;

    private Num multity;
    private Num multity1;

    public DEAIndicator(BarSeries series, MACDIndicator macdIndicator) {
        super(series);
        this.macdIndicator = macdIndicator;
        this.multity = this.numOf(0.2);
        this.multity1 = this.numOf(0.8);
    }

    @Override
    protected Num calculate(int i) {
        if (i == 0) {
            return macdIndicator.getValue(i).multipliedBy(multity);
        }
        return (macdIndicator.getValue(i - 1).multipliedBy(multity1))
                .plus(macdIndicator.getValue(i).multipliedBy(multity));
    }
}
