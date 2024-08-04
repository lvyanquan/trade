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
import org.example.core.handler.notify.DingTemplateNotify;
import org.example.core.indicator.ta4j.SimpleMovingAverageIndicator;
import org.junit.Test;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

/**
 * 后期可以注册多个symbol。handler注册的时候，需要绑定对应的symbol
 * <p>
 * 指标计算对接
 */
public class BarEngineTest {
    String exchange = "binance";

    @Test
    public void notifyKline() throws Exception {

        BarEngineBuilder.SymbolDescribe btcSymbol = new BarEngineBuilder.SymbolDescribe(
                "BTCUSDT",
                TradeType.USDT_MARGINED_CONTRACT,
                KlineInterval.ONE_MINUTE
        );
        BarEngineBuilder.SymbolDescribe ethSymbol = btcSymbol.of("ETHUSDT");

        new BarEngineBuilder<Bar>()
                .exchange(exchange)

                .subscribe(btcSymbol)
                .addHandler(btcSymbol, barHandler(btcSymbol, exchange))

                .subscribe(ethSymbol)
                .addHandler(ethSymbol, barHandler(ethSymbol, exchange))

                .window(1)
                .skipWindowData(1)
                .build()
                .run();

        Thread.currentThread().join();
    }


    @Test
    public void indicator() throws InterruptedException {

        String symbol = "BTCUSDT";
        String interValue = "15分钟";
        BarPipeline.BarHandler<org.ta4j.core.Bar> barBarHandler = new BarPipeline.BarHandler<org.ta4j.core.Bar>() {
            boolean windowDataApply = false;
            BaseBarSeries baseBarSeries;
            ClosePriceIndicator closePriceIndicator;
            EMAIndicator emaIndicatorShort;
            EMAIndicator emaIndicatorLong;
            ATRIndicator atrIndicator;
            VolumeIndicator volumeIndicator;
            // 计算成交量均值
            SimpleMovingAverageIndicator volumeSMA;
            // 计算成交量标准差
            StandardDeviationIndicator volumeStdDev;
            CrossedUpIndicatorRule crossedUpIndicatorRule;


            @Override
            public void open() {
                baseBarSeries = new BaseBarSeries(symbol, DoubleNum::valueOf);
                closePriceIndicator = new ClosePriceIndicator(baseBarSeries);
                emaIndicatorShort = new EMAIndicator(closePriceIndicator, 7);
                emaIndicatorLong = new EMAIndicator(closePriceIndicator, 25);

                atrIndicator = new ATRIndicator(baseBarSeries, 30);
                int period = 15;
                volumeIndicator = new VolumeIndicator(baseBarSeries);
                volumeSMA = new SimpleMovingAverageIndicator(volumeIndicator, period);
                volumeStdDev = new StandardDeviationIndicator(volumeIndicator, period);

                crossedUpIndicatorRule = new CrossedUpIndicatorRule(emaIndicatorShort, emaIndicatorLong);
            }

            @Override
            public void applyWindow(org.ta4j.core.Bar bar) {
                baseBarSeries.addBar(bar, true);
                windowDataApply = true;
                int endIndex = baseBarSeries.getEndIndex();
                if (crossedUpIndicatorRule.isSatisfied(endIndex)) {
                    System.out.println(String.format("[%s] [%s]\\n %s周期策略：ema7金叉ema25",
                            symbol,
                            interValue,
                            symbol, baseBarSeries.getBar(endIndex).getEndTime()));
                }
                double change = Math.abs(bar.getClosePrice().minus(bar.getClosePrice()).doubleValue());
                if (change > atrIndicator.getValue(endIndex).doubleValue() * 2) {
                    System.out.println(String.format("[%s] [%s]\\n %s周期策略：波动大于2倍atr",
                            symbol,
                            interValue,
                            symbol, baseBarSeries.getBar(endIndex).getEndTime()));
                }
                if (volumeIndicator.getValue(endIndex).doubleValue() -( volumeSMA.getValue(endIndex).doubleValue() + volumeStdDev.getValue(endIndex).doubleValue() * 2) > 0) {
                    System.out.println(String.format("[%s] [%s]\\n %s周期策略：成交量放大",
                            symbol,
                            interValue,
                            symbol, baseBarSeries.getBar(endIndex).getEndTime()));
                }
            }

            @Override
            public void apply(org.ta4j.core.Bar bar) {
                int endIndex = baseBarSeries.getEndIndex();
                if (windowDataApply) {
                    baseBarSeries.addBar(bar);
                    windowDataApply = false;
                } else {
                    baseBarSeries.addBar(bar, true);
                }
                double change = Math.abs(bar.getClosePrice().minus(bar.getClosePrice()).doubleValue());
                if (change > atrIndicator.getValue(endIndex).doubleValue() * 2) {
                    System.out.println(String.format("[%s] [%s]\\n %s周期策略：波动大于2倍atr",
                            symbol,
                            interValue,
                            symbol, baseBarSeries.getBar(endIndex).getEndTime()));
                }

                if (volumeIndicator.getValue(endIndex).doubleValue() -( volumeSMA.getValue(endIndex).doubleValue() + volumeStdDev.getValue(endIndex).doubleValue() * 2) > 0) {
                    System.out.println(String.format("[%s] [%s]\\n %s周期策略：成交量放大",
                            symbol,
                            interValue,
                            symbol, baseBarSeries.getBar(endIndex).getEndTime()));
                }
            }
        };

        BarEngineBuilder.SymbolDescribe btcSymbol = new BarEngineBuilder.SymbolDescribe(
                symbol,
                TradeType.USDT_MARGINED_CONTRACT,
                KlineInterval.ONE_MINUTE,
                System.currentTimeMillis() - 24 * 60 * 60 * 1000,
               -1
        );

        new BarEngineBuilder<org.ta4j.core.Bar>()
                .exchange(exchange)
                .convert(BarConvent::convent)

                .subscribe(btcSymbol)
                .addHandler(btcSymbol, barBarHandler)

                .window(15)
                .skipWindowData(1)
                .build()
                .run();

        Thread.currentThread().join();
    }

    public BarPipeline.BarHandler<Bar> barHandler(BarEngineBuilder.SymbolDescribe symbolDescribe, String exchange) {
        return new BarPipeline.BarHandler<Bar>() {
            @Override
            public void applyWindow(Bar bar) {
                System.out.println("window: " + bar);
                DingTemplateNotify.DEFAULT_NOTIFY.notifyPrice(bar, symbolDescribe.getSymbol(), symbolDescribe.getTradeType(), exchange);
            }
        };
    }
}
