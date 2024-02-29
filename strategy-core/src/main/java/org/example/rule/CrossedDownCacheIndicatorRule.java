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
import org.ta4j.core.rules.CrossedDownIndicatorRule;

public class CrossedDownCacheIndicatorRule extends CrossedDownIndicatorRule {
    private boolean isCross = false;
    private int index;
    private Indicator<Num> first;
    private Indicator<Num> second;
    private int continueEffective;
    public CrossedDownCacheIndicatorRule(Indicator<Num> first, Indicator<Num> second) {
        super(first, second);
        this.first = first;
        this.second = second;
        this.continueEffective = 4;
    }

    public CrossedDownCacheIndicatorRule(Indicator<Num> first, Indicator<Num> second, int continueEffective) {
        super(first, second);
        this.first = first;
        this.second = second;
        this.continueEffective = continueEffective;
    }
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = super.isSatisfied(index, tradingRecord);
        if(satisfied){
            isCross = satisfied;
            this.index = index;
            return isCross;
        }
        if(((Num)this.first.getValue(index)).isLessThan((Num)this.second.getValue(index)) && isCross && index-this.index <continueEffective){
            return true;
        }
        return false;
    }

}
