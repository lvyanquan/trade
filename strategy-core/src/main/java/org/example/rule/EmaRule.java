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

import org.ta4j.core.Indicator;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

public class EmaRule implements WeightRule {
    private EMAIndicator shortEma;
    private EMAIndicator midEma;
    private EMAIndicator longEma;

    private ClosePriceIndicator closePriceIndicator;

    private int weight = 0;
    private String name;

    public EmaRule(String name, EMAIndicator shortEma, EMAIndicator midEma, EMAIndicator longEma, ClosePriceIndicator closePriceIndicator) {
        this.shortEma = shortEma;
        this.midEma = midEma;
        this.longEma = longEma;
        this.closePriceIndicator = closePriceIndicator;
        this.name = name;
    }

    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
        if (shortEma.getValue(i).isLessThanOrEqual(longEma.getValue(i)) && midEma.getValue(i).isLessThanOrEqual(longEma.getValue(i))) {
            weight = -3;
            return false;
        }
        if (closePriceIndicator.getValue(i).isLessThanOrEqual(longEma.getValue(i))) {
            weight = -2;
            return false;
        }
        if (i > 3) {
            int shortTrend = getTrend(shortEma, i);
            int midTrend = getTrend(midEma, i);
            int longTrend = getTrend(longEma, i);

            if (shortTrend + midTrend + longTrend == 3) {
                weight = 1;
                return true;
            }

            if (shortTrend + midTrend + longTrend == -3) {
                weight = -2;
                return true;
            }
            boolean a = shortTrend > 0;
            boolean b = midTrend > 0;
            boolean c = longTrend > 0;
            if (!a && b && c) {
                weight = 1;
                return true;
            }

            if (a && !b && c) {
                weight = 1;
                return true;
            }

            if (a && b && !c) {
                weight = 1;
                return true;
            }

            if (!a && !b && c) {
                weight = -1;
                return true;
            }

            if (!a && b && !c) {
                weight = -1;
                return true;
            }

            if (!b && !c) {
                weight = -1;
                return true;
            }

        } else {
            weight = 0;
            return false;
        }
        return false;
    }

    @Override
    public int weigh() {
        return weight;
    }

    @Override
    public String name() {
        return name;
    }

    private int getTrend(Indicator<Num> indicator, int num) {
        int start = Math.max(1, num - 10 + 1);

        int up = 0;
        int down = 0;
        //up 和down谁先到3，未到3之前，都是1个加1 另一个减一。
        //如果到了3，那么出现回调，就判断是否超过上上次的值，如果超过才会归0，否则不变

        for (; start < num; start++) {
            if (indicator.getValue(start).isLessThan(indicator.getValue(start - 1))) {
                if (up >= 3) {
                    if (indicator.getValue(start).isLessThan(indicator.getValue(start - 2))) {
                        up = 0;
                    }
                }

                if (++down >= 3) {
                    up = 0;
                } else {
                    if (up >= 3) {
                        up = 0;
                    } else {
                        up--;
                        if (up < 0) {
                            up = 0;
                        }
                    }
                }

            } else if (indicator.getValue(start).isGreaterThan(indicator.getValue(start - 1))) {

                if (down >= 3) {
                    if (indicator.getValue(start).isGreaterThan(indicator.getValue(start - 2))) {
                        down = 0;
                    }
                }


                if (++up >= 3) {
                    down = 0;
                } else {
                    down--;
                    if (down < 0) {
                        down = 0;
                    }
                }
            }
        }
        if (up >= 3) {
            return 1;
        }
        if (down >= 3) {
            return -1;
        }
        return 0;
    }
}
