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

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.candles.BearishEngulfingIndicator;

/**
 * 看跌孕育形态是牛市价格走势反转的烛台图表指标。
 * 它通常由价格小幅下跌（由黑色蜡烛表示）表示，该下跌可以包含在过去一两天给定股票的向上价格变动（由白色蜡烛表示）中。
 * 交易者可以使用技术指标，例如相对强弱指数 (RSI) 和带有看跌孕育形态的随机震荡指标来增加交易成功的机会。
 */
public class BearishEngulfingRule implements Rule {
    private BearishEngulfingIndicator indicator;
    public BearishEngulfingRule(BarSeries series) {
        indicator = new BearishEngulfingIndicator(series);
    }

    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
        return indicator.getValue(i);
    }
}
