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

package com.example.web_kline.util;

import org.example.binance.factory.KlineFactory;
import org.example.model.currency.Btc;
import org.example.model.enums.Server;
import org.example.model.enums.ContractType;
import org.example.model.market.KlineModule;

public class KlineTest {
    public static void main(String[] args) {
        for (KlineModule historyKlineDatum : KlineFactory.Create(Server.BINANCE, ContractType.SPOT)
                .getHistoryKlineData(Btc.BTC, "1m", System.currentTimeMillis() - 24 * 60 * 60 * 1000, System.currentTimeMillis())) {

         System.out.println(historyKlineDatum);
        }


    }
}
