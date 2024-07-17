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

package com.example.web_kline;

import org.example.Position.PositionManagerRepository;
import org.example.StrateFactory;
import org.example.indicators.AtrLowerIndicator;
import org.example.model.currency.Currency;
import org.example.rule.NumericRule;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.num.Num;

public class AtrStrateFactory implements StrateFactory {

    private PositionManagerRepository positionManagerRepository;
    private Currency currency;

    public AtrStrateFactory(PositionManagerRepository positionManagerRepository, Currency currency) {
        this.positionManagerRepository = positionManagerRepository;
        this.currency = currency;
    }

    @Override
    public BaseStrategy buildStrategy(BarSeries series) {
        //止盈策略 1.5atr 且大于我的持仓均价0.05%
        //入场策略 1atr

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsiIndicator = new RSIIndicator(closePrice, 14);//建议是14
        Rule rsiEnterWeightRuleWrapper = new NumericRule(rsiIndicator, new ConstantIndicator(series, series.numOf(25)), Num::isLessThan);
        Rule rsiExitWeightRuleWrapper = new NumericRule(rsiIndicator, new ConstantIndicator(series, series.numOf(80)), Num::isGreaterThan);

        AtrLowerIndicator atrLowerIndicator = new AtrLowerIndicator(series, closePrice, 10);
        Rule enter = new Rule() {
            @Override
            public boolean isSatisfied(int i, TradingRecord tradingRecord) {
                if (i < 3) {
                    return false;
                }
                if (positionManagerRepository.getQuantity(currency) <= 0) {
                    //取前10根线的close价格的平均价格
                    int min = Math.min(10, series.getEndIndex() - 1);
                    double price = 0;
                    for (int start = 0; start < min; start++) {
                        price += closePrice.getValue(start).doubleValue();
                    }
                    price = price / min;
                    if (rsiEnterWeightRuleWrapper.isSatisfied(i) && price - closePrice.getValue(i).doubleValue() > atrLowerIndicator.getValue(i).doubleValue()) {
                        return true;
                    }
                }
                return false;
            }
        };
        Rule exit = new Rule() {
            @Override
            public boolean isSatisfied(int i, TradingRecord tradingRecord) {
                double cost = positionManagerRepository.getCost(currency);
                return cost > 0 && closePrice.getValue(i).doubleValue() - cost > atrLowerIndicator.getValue(i).doubleValue() && rsiExitWeightRuleWrapper.isSatisfied(i) && rsiExitWeightRuleWrapper.isSatisfied(i);
            }
        };

        return new BaseStrategy(enter, exit);
    }
}