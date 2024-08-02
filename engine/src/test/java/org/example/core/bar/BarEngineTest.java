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

import org.example.core.handler.notify.DingTemplateNotify;

/**
 * 后期可以注册多个symbol。handler注册的时候，需要绑定对应的symbol
 * <p>
 * 指标计算对接
 */
public class BarEngineTest {

    private BarEngineTest() {
    }

    public static void main(String[] args) throws Exception {

        BarEngineBuilder.SymbolDescribe btcSymbol = new BarEngineBuilder.SymbolDescribe(
                "BTCUSDT",
                TradeType.USDT_MARGINED_CONTRACT,
                KlineInterval.ONE_MINUTE
        );

        BarEngineBuilder.SymbolDescribe ethSymbol = btcSymbol.of("ETHUSDT");

        String exchange = "binance";

        new BarEngineBuilder<Bar>()
                .exchange(exchange)

                .subscribe(btcSymbol)
                .addHandler(btcSymbol, barHandler(btcSymbol, exchange))

                .subscribe(ethSymbol)
                .addHandler(ethSymbol, barHandler(ethSymbol, exchange))

                .window(5)
                .skipWindowData(1)
                .build()
                .run();


//        BaseBarSeries baseBarSeries = new BaseBarSeries("mySeries2", DoubleNum::valueOf);
//        btcusdt.registerConsumer(t -> baseBarSeries.addBar(BarConvent.convent(t)));

        //架构设计 source 生成数据到 distributer，distributer 下方是多个pipeline对其进行消费处理
        //notify完成了 各种格式模板，比如价格提示 指标提示 策略提示
        //indicator计算

    }

    public static BarPipeline.BarHandler<Bar> barHandler(BarEngineBuilder.SymbolDescribe symbolDescribe, String exchange) {
        return new BarPipeline.BarHandler<Bar>() {
            @Override
            public void applyWindow(Bar bar) {
                //System.out.println("window: " + bar);
                DingTemplateNotify.DEFAULT_NOTIFY.notifyPrice(bar, symbolDescribe.getSymbol(), symbolDescribe.getTradeType(), exchange);
            }
        };
    }

    ;
}
