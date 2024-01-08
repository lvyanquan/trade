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

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.RecursiveCachedIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
import org.ta4j.core.num.Num;

/**
 * https://school.stockcharts.com/doku.php?id=technical_indicators:balance_of_power
 * 结果指标在 -1 和 +1 之间振荡。正值表示证券收盘价高于开盘价；值越高，正价格变化越极端。最大值 +1 表示在移动平均线中包含的每个时间段内，证券以最低价开盘并以最高价收盘。
 * 使用 14 周期 SMA 进行平滑
 */
public class BopIndicators extends RecursiveCachedIndicator<Num> {
    private SMAIndicator open;
    private SMAIndicator high;
    private SMAIndicator low;
    private SMAIndicator close;

    public BopIndicators(BarSeries series, int num) {
        super(series);
        this.open = new SMAIndicator(new OpenPriceIndicator(series), num);
        this.high = new SMAIndicator(new HighPriceIndicator(series), num);
        this.low = new SMAIndicator(new LowPriceIndicator(series), num);
        this.close = new SMAIndicator(new ClosePriceIndicator(series), num);
    }

    public BopIndicators(BarSeries series) {
        super(series);
        this.open = new SMAIndicator(new OpenPriceIndicator(series), 14);
        this.high = new SMAIndicator(new HighPriceIndicator(series), 14);
        this.low = new SMAIndicator(new LowPriceIndicator(series), 14);
        this.close = new SMAIndicator(new ClosePriceIndicator(series), 14);
    }

    @Override
    protected Num calculate(int i) {
        return (close.getValue(i).minus(open.getValue(i))).dividedBy((high.getValue(i).minus(low.getValue(i))));
    }
}
