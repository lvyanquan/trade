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

import org.example.model.enums.OrderSide;

public class BinanceOrderUtil {


    public static OrderSide orderSide(String side, String positionSide) {
        if ("BUY".equalsIgnoreCase(side) && "SHORT".equalsIgnoreCase(positionSide)) {
            return OrderSide.BUY_SHORT;
        }
        if ("BUY".equalsIgnoreCase(side) && "LONG".equalsIgnoreCase(positionSide)) {
            return OrderSide.BUY_LONG;
        }

        if ("SELL".equalsIgnoreCase(side) && "SHORT".equalsIgnoreCase(positionSide)) {
            return OrderSide.SELL_SHORT;
        }

        if ("SELL".equalsIgnoreCase(side) && "LONG".equalsIgnoreCase(positionSide)) {
            return OrderSide.SELL_LONG;
        }
        throw new RuntimeException(String.format("The order type cannot be recognized, side: [%s], positionSide: [%s]"));
    }
}
