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

import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;

public class CoefficientWeightRule implements WeightRule {
    int baseWeight;
    int weight = baseWeight;

    double coefficient = 1;

    //基本策略
    private Rule baseRule;

    //辅助策略
    private Rule subsidiaryRule;

    private String name;

    private String baseName;

    public CoefficientWeightRule(String name, int baseWeight, double coefficient, Rule baseRule, Rule subsidiaryRule) {
        this.baseWeight = baseWeight;
        this.weight = baseWeight;
        this.coefficient = coefficient;
        this.baseRule = baseRule;
        this.subsidiaryRule = subsidiaryRule;
        this.baseName = name;
    }

    @Override
    public int weigh() {
        return weight;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
        boolean satisfied = baseRule.isSatisfied(i, tradingRecord);
        this.weight = this.baseWeight;
        this.name = baseName;
        if (subsidiaryRule.isSatisfied(i, tradingRecord)) {
            weight = (int)(baseWeight * coefficient);
            this.name = baseName + "(coefficiented)";
        }
        return satisfied;
    }
}
