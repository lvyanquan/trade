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

package org.example.strategy.macd;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

public class MacdCrossRule {

    public void test(BarSeries series){
        MACDIndicator macdIndicator = new MACDIndicator(new ClosePriceIndicator(series));
        EMAIndicator macdLine = new EMAIndicator(macdIndicator, 9);

        //死叉，慢速线（DEA）黄线穿越了快速线（DIF）白线
        CrossedDownIndicatorRule macdDeathCross = new CrossedDownIndicatorRule(macdLine, macdIndicator);
        //金叉 快速线（DIF）白线突破了慢速线（DEA）黄线
        CrossedUpIndicatorRule macdGoldenCross = new CrossedUpIndicatorRule(macdLine, macdIndicator);

    }
}
