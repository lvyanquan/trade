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

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.AbstractIndicator;
import org.ta4j.core.num.Num;

public class IncreasingVolumeIndicator extends AbstractIndicator<Num> {

    private final int length;

    public IncreasingVolumeIndicator(BarSeries series, int length) {
        super(series);
        this.length = length;
    }

    @Override
    public Num getValue(int index) {
        if (index < length - 1) {
            // 不足length根K线
            return numOf(0);
        }

        Num totalVolume = numOf(0);
        boolean isIncreasing = true;
        for (int i = index - length + 1; i <= index; i++) {
            Num currentVolume = getBarSeries().getBar(i).getVolume();
            totalVolume = totalVolume.plus(currentVolume);
            if (i > index - length + 1) { // 排除第一个，无法比较
                Num previousVolume = getBarSeries().getBar(i - 1).getVolume();
                if (currentVolume.isLessThan(previousVolume)) {
                    isIncreasing = false;
                    break;
                }
            }
        }

        Num averageVolume = totalVolume.dividedBy(numOf(length));
        Num lastVolume = getBarSeries().getBar(index).getVolume();

        // 判断最后一根K线的成交量是否大于平均值，并且成交量是否逐渐增大
        if (lastVolume.isGreaterThan(averageVolume) && isIncreasing) {
            return numOf(2); // 满足条件
        } else if (lastVolume.isGreaterThan(averageVolume) || isIncreasing) {
            return numOf(1); // 满足条件
        } else {
            return numOf(0); // 不满足条件
        }
    }
}
