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

package org.example.core.bar;

import org.example.core.bar.util.BarConvent;
import org.example.core.indicator.ta4j.AtrRatioIndicator;
import org.example.core.util.TablePrintUtil;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DoubleNum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IndicatorTest {
    @Test
    public void atrRatio() throws InterruptedException {
        HashMap<String, AtrRatioIndicator> mpas = new HashMap<>();
        ArrayList<String> symbols = new ArrayList<>();
        symbols.add("BTCUSDT");
        symbols.add("ETHUSDT");
        symbols.add("SOLUSDT");

        BarEngineBuilder<Bar> binance = new BarEngineBuilder<Bar>()
                .exchange("binance")
                .convert(BarConvent::convent)
                .window(10)
                .skipWindowData(1);

        for (String symbol : symbols) {
            BarEngineBuilder.SymbolDescribe symbolDscribe = new BarEngineBuilder.SymbolDescribe(
                    symbol,
                    TradeType.USDT_MARGINED_CONTRACT,
                    KlineInterval.ONE_MINUTE,
                    System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000,
                    -1
            );
            binance.subscribe(symbolDscribe)
                    .addHandler(symbolDscribe, buildBarHandler(symbol, mpas));
        }

        binance.build()
                .run();

        ArrayList<String[]> rows = new ArrayList<>();
        for (Map.Entry<String, AtrRatioIndicator> entry : mpas.entrySet()) {
            AtrRatioIndicator value = entry.getValue();
            double max = 0;
            double min = 1;
            double averg = 0;
            double tempSum = 0;
            for (int i = value.getBarSeries().getEndIndex() - 200; i < value.getBarSeries().getEndIndex(); i++) {
                double v = value.getValue(i).doubleValue();
                max = Math.max(max, v);
                min = Math.min(min, v);
                tempSum += v;
            }
            averg = tempSum / 200;
            rows.add(new String[]{entry.getKey(), String.valueOf(min), String.valueOf(max), String.valueOf(averg)});
        }
        String[] fields = new String[4];
        fields[0] = "币种";
        fields[1] = "最低值";
        fields[2] = "最高值";
        fields[3] = "平均值";
        TablePrintUtil.printTable(rows, fields);

        // 过滤出波动率较大的交易对，假设波动率大于0.02
//        double threshold = 0.02;
//        Map<String, AtrRatioIndicator> highVolatilityPairs = mpas.entrySet().stream()
//                .filter(entry -> entry.getValue().getValue(entry.getValue().getBarSeries().getEndIndex()).doubleValue() > threshold)
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


//        Thread.currentThread().join();
    }


    private BarPipeline.BarHandler<org.ta4j.core.Bar> buildBarHandler(String symbol, HashMap<String, AtrRatioIndicator> mpas) {
        return new BarPipeline.BarHandler<org.ta4j.core.Bar>() {
            BaseBarSeries baseBarSeries;
            private AtrRatioIndicator atrRatioIndicator;

            @Override
            public void open() {
                baseBarSeries = new BaseBarSeries(symbol, DoubleNum::valueOf);
                atrRatioIndicator = new AtrRatioIndicator(new ClosePriceIndicator(baseBarSeries), new ATRIndicator(baseBarSeries, 30));
                mpas.put(symbol, atrRatioIndicator);
            }

            @Override
            public void applyWindow(org.ta4j.core.Bar bar) {
                baseBarSeries.addBar(bar);
            }
        };
    }
}
