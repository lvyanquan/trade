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

package org.example.core.indicator.ta4j;

import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

/**
 * 平均值指标
 */
public class SimpleMovingAverageIndicator extends CachedIndicator<Num> {
    private final CachedIndicator<Num> indicator;
    private final int barCount;

    public SimpleMovingAverageIndicator(CachedIndicator<Num> indicator, int barCount) {
        super(indicator);
        this.indicator = indicator;
        this.barCount = barCount;
    }

    @Override
    protected Num calculate(int index) {
        Num sum = numOf(0);
        int start = Math.max(0, index - barCount + 1);
        for (int i = start; i <= index; i++) {
            sum = sum.plus(indicator.getValue(i));
        }
        return sum.dividedBy(numOf(index - start + 1));
    }
}
