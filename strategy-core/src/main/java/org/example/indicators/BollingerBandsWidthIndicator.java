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

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.num.Num;


/**
 * https://school.stockcharts.com/doku.php?id=technical_indicators:bollinger_band_width
 * 布林线宽度，值越大 代表波动性越大，越接近上轨或者下轨
 */
public class BollingerBandsWidthIndicator extends CachedIndicator<Num> {
    private BollingerBandsUpperIndicator up;
    private BollingerBandsMiddleIndicator mid;
    private BollingerBandsLowerIndicator low;
    private Num hundred;


    public BollingerBandsWidthIndicator(BollingerBandsUpperIndicator up, BollingerBandsMiddleIndicator mid, BollingerBandsLowerIndicator low) {
        super(up.getBarSeries());
        this.up = up;
        this.mid = mid;
        this.low = low;
        this.hundred= getBarSeries().numOf(100);
    }

    @Override
    protected Num calculate(int i) {
        return (up.getValue(i).minus(low.getValue(i))).dividedBy(mid.getValue(i)).multipliedBy(hundred);
    }
}
