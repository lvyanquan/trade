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
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.AbstractRule;

import java.util.LinkedList;

public class SupertrendRule extends AbstractRule {
    Indicator<Num> upindicator;
    Indicator<Num> downindicator;
    Indicator<Num> closePrice;
    boolean buy;

    int trend = 1;
    int continueIndex = 0;

    private LinkedList<Double> range = new LinkedList<>();

    public SupertrendRule(Indicator<Num> upindicator, Indicator<Num> downindicator, Indicator<Num> closePrice, boolean buy) {
        this.upindicator = upindicator;
        this.downindicator = downindicator;
        this.closePrice = closePrice;
        this.buy = buy;
    }

    public boolean isSatisfied(int i, TradingRecord tradingRecord) {

        int tempTrend = trend;
        if (trend == -1 && closePrice.getValue(i).isGreaterThan(downindicator.getValue(i == 0 ? i : i - 1))) {
            tempTrend = 1;
        } else if (trend == 1 && closePrice.getValue(i).isLessThan(upindicator.getValue(i == 0 ? i : i - 1))) {
            tempTrend = -1;
        }
        boolean isBuy;

        if (buy) {
            //买入
            isBuy = tempTrend == 1 && trend == -1;
            if (tempTrend == 1) {
                continueIndex++;
            } else {
                continueIndex = 0;
            }
        } else {
            //卖出
            isBuy = tempTrend == -1 && trend == 1;
            if (tempTrend == -1) {
                continueIndex++;
            } else {
                continueIndex = 0;
            }
        }
        trend = tempTrend;
        if (!isBuy && continueIndex > 5) {
            isBuy = true;
            continueIndex = 0;
        }

        range.addLast(upindicator.getValue(i).minus(downindicator.getValue(i)).doubleValue());
        if(range.size() > 5){
            range.removeFirst();
        }
        if(range.size() == 5){

        }
        return isBuy;
    }
}
