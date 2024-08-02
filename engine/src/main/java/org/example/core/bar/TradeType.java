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

public enum TradeType {
    SPOT("Spot Trading","现货"),
    COIN_MARGINED_CONTRACT("Coin-Margined Contract","币本位合约"),
    USDT_MARGINED_CONTRACT("USDT-Margined Contract","USDT合约"),;

    private final String description;
    private final String chinaDescription;

    TradeType(String description,String chinaDescription) {
        this.description = description;
        this.chinaDescription = chinaDescription;
    }

    public String getDescription() {
        return description;
    }

    public String getChinaDescription() {
        return chinaDescription;
    }

    @Override
    public String toString() {
        return description;
    }
}

