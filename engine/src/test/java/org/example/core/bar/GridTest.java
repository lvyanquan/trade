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
import org.example.core.strategy.GridModel;
import org.junit.Test;

public class GridTest {
    @Test
    public void gridTest() throws Exception {

        String symbol = "BTCUSDT";
        String exchange = "binance";
        BarEngineBuilder.SymbolDescribe btcSymbol = new BarEngineBuilder.SymbolDescribe(
                symbol,
                TradeType.SPOT,
                KlineInterval.ONE_MINUTE,
                System.currentTimeMillis() - 24 * 60 * 60 * 1000,
                -1
        );

        GridModel gridModel = new GridModel("grid-01", 30, 60, btcSymbol);

        new BarEngineBuilder<BaseBarExtend>()
                .exchange(exchange)
                .convert(BarConvent::conventBaseBarExtend)

                .subscribe(btcSymbol)
                .addHandler(btcSymbol, gridModel)

                .window(45)
                .skipWindowData(1)
                .build()
                .run();

        Thread.currentThread().join();
    }
}
