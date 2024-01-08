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

package org.example.strategy;

import org.example.rule.WeightRule;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.TradingRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

public class CollectRuleStrategy extends BaseStrategy {
    List<WeightRule> enterRules;
    List<WeightRule> exitRules;
    boolean addAll;

    int enterTarget;

    int exitTarget;

    private Set<Integer> enterTargetLists;
    private Set<Integer> exitTargetLists;

    Map<String, Integer> enterEffectiveRuleName;
    Map<String, Integer> exitEffectiveRuleName;

    int effectiveWeight = 0;

    public CollectRuleStrategy(List<WeightRule> enterRules, boolean addAll, int enterTarget, int exitTarget) {
        this(enterRules, enterRules, addAll, enterTarget, exitTarget);
    }

    public CollectRuleStrategy(List<WeightRule> enterRules, List<WeightRule> exitRules, boolean addAll, int enterTarget, int exitTarget) {
        this(enterRules, exitRules, 0, addAll, enterTarget, exitTarget);
    }

    public CollectRuleStrategy(List<WeightRule> enterRules, List<WeightRule> exitRules, int unstablePeriod, Set<Integer> enterTargetLists, Set<Integer> exitTargetLists) {
        super(enterRules.get(0), enterRules.get(0), unstablePeriod);
        this.enterRules = enterRules;
        this.exitRules = exitRules;
        this.enterTargetLists = enterTargetLists;
        this.exitTargetLists = exitTargetLists;
        this.enterEffectiveRuleName = new HashMap<>(enterRules.size());
        this.exitEffectiveRuleName = new HashMap<>(exitRules.size());
        this.addAll = false;
    }


    public CollectRuleStrategy(List<WeightRule> enterRules, List<WeightRule> exitRules, int unstablePeriod, boolean addAll, int enterTarget, int exitTarget) {
        super(enterRules.get(0), enterRules.get(0), unstablePeriod);
        this.enterRules = enterRules;
        this.exitRules = exitRules;
        this.addAll = addAll;
        this.enterTarget = enterTarget;
        this.exitTarget = exitTarget;
        this.enterEffectiveRuleName = new HashMap<>(enterRules.size());
        this.exitEffectiveRuleName = new HashMap<>(exitRules.size());
    }

    public CollectRuleStrategy(String name, List<WeightRule> enterRules, List<WeightRule> exitRules, int unstablePeriod, boolean addAll, int enterTarget, int exitTarget) {
        super(name, enterRules.get(0), enterRules.get(0), unstablePeriod);
        this.enterRules = enterRules;
        this.exitRules = exitRules;
        this.addAll = addAll;
        this.enterTarget = enterTarget;
        this.exitTarget = exitTarget;
        this.enterEffectiveRuleName = new HashMap<>(enterRules.size());
        this.exitEffectiveRuleName = new HashMap<>(exitRules.size());
    }

    @Override
    public boolean shouldEnter(int index, TradingRecord tradingRecord) {
        if (this.isUnstableAt(index)) {
            return false;
        }
        effectiveWeight = 0;
        enterEffectiveRuleName.clear();
        exitEffectiveRuleName.clear();
        OptionalInt reduce = enterRules.stream().parallel()
                .filter(rule -> {
                    boolean satisfied = rule.isSatisfied(index, tradingRecord);
                    if (satisfied) {
                        enterEffectiveRuleName.put(rule.name(), rule.weigh());
                    }
                    return satisfied;
                })
                .mapToInt(WeightRule::weigh)
                .reduce(Integer::sum);

        int enterWeight = reduce.orElse(0);

        if (addAll) {
            int exitWeight = exitRules.stream().parallel()
                    .filter(rule -> {
                        boolean satisfied = rule.isSatisfied(index, tradingRecord);
                        if (satisfied) {
                            exitEffectiveRuleName.put(rule.name(), rule.weigh());
                        }
                        return satisfied;
                    })
                    .mapToInt(WeightRule::weigh)
                    .reduce(Integer::sum)
                    .orElse(0);
            enterWeight = enterWeight - exitWeight;
        }
        this.effectiveWeight = enterWeight;
        if (enterTargetLists != null && enterTargetLists.size() > 0) {
            return enterTargetLists.contains(enterWeight);
        }
        return enterWeight > this.enterTarget;
    }

    @Override
    public boolean shouldExit(int index, TradingRecord tradingRecord) {
        if (this.isUnstableAt(index)) {
            return false;
        }
        effectiveWeight = 0;
        enterEffectiveRuleName.clear();
        exitEffectiveRuleName.clear();

        OptionalInt reduce = exitRules.stream().parallel()
                .filter(rule -> {
                    boolean satisfied = rule.isSatisfied(index, tradingRecord);
                    if (satisfied) {
                        exitEffectiveRuleName.put(rule.name(), rule.weigh());
                    }
                    return satisfied;
                })
                .mapToInt(WeightRule::weigh)
                .reduce(Integer::sum);
        int exitWeight = reduce.orElse(0);

        if (addAll) {
            OptionalInt reduce1 = enterRules.stream().parallel()
                    .filter(rule -> {
                        boolean satisfied = rule.isSatisfied(index, tradingRecord);
                        if (satisfied) {
                            enterEffectiveRuleName.put(rule.name(), rule.weigh());
                        }
                        return satisfied;
                    })
                    .mapToInt(WeightRule::weigh)
                    .reduce(Integer::sum);
            int enterWeight =  reduce1.orElse(0);
            exitWeight = exitWeight - enterWeight;
        }

        this.effectiveWeight = exitWeight;
        if (exitTargetLists != null && exitTargetLists.size() > 0) {
            return exitTargetLists.contains(exitWeight);
        }
        return exitWeight > this.exitTarget;
    }

    public Map<String, Integer> getEnterEffectiveRuleName() {
        return enterEffectiveRuleName;
    }

    public Map<String, Integer> getExitEffectiveRuleName() {
        return exitEffectiveRuleName;
    }

    public int getEffectiveWeight() {
        return effectiveWeight;
    }
}
