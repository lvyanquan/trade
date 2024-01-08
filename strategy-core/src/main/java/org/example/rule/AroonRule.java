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
import org.ta4j.core.indicators.aroon.AroonDownIndicator;
import org.ta4j.core.indicators.aroon.AroonUpIndicator;

public class AroonRule implements Rule {

    AroonUpIndicator aroonUp;  // 使用25周期
    AroonDownIndicator aroonDown;  // 使用25周期
    // Aroon策略规则
    // 当Aroon-Up高于50并接近100且Aroon-Down低于50时，这很好地表明了上升趋势。同样，当Aroon-Down高于50并接近100，而Aroon-Up低于50时，可能会出现下降趋势。

    public AroonRule(BarSeries series, int count) {
        aroonUp = new AroonUpIndicator(series, count);
        aroonDown = new AroonDownIndicator(series, count);  // 使用25周期
    }


    // 使用25周期
    public AroonRule(BarSeries series) {
        aroonUp = new AroonUpIndicator(series, 25);
        aroonDown = new AroonDownIndicator(series, 25);
    }

    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
        return aroonUp.getValue(i).doubleValue() > 60 && aroonDown.getValue(i).doubleValue() < 40;
    }
}
