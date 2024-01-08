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
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.RecursiveCachedIndicator;
import org.ta4j.core.num.Num;

public class AtrLowerIndicator extends RecursiveCachedIndicator <Num>{

    private Indicator<Num> indicator;
    private ATRIndicator atrIndicator;
    private int multy;

    public AtrLowerIndicator(BarSeries series, Indicator<Num> indicator,int num,int multy) {
        super(series);
        this.indicator = indicator;
        this.atrIndicator = new ATRIndicator(series,num);
        this.multy = multy;

    }

    public AtrLowerIndicator(BarSeries series, Indicator<Num> indicator,int num) {
        super(series);
        this.indicator = indicator;
        this.atrIndicator = new ATRIndicator(series,num);
        this.multy = 1;

    }

    @Override
    protected Num calculate(int i) {
       return indicator.getValue(i).minus((atrIndicator.getValue(i).multipliedBy(getBarSeries().numOf(multy))));
    }
}
