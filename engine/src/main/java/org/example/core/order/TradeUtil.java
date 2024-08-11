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

package org.example.core.order;

import com.binance.connector.client.impl.spot.Trade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;

public class TradeUtil {

    public static void orderLimitDuo(String symbol, double price, double amount, int pricePrecision, int quantityPrecision, String clientId, Trade trade) {
       Exception e2 = null;
        for (int i = 0; i < 3; i++) {
            try {
                price = BigDecimal.valueOf(price).setScale(pricePrecision, RoundingMode.DOWN).doubleValue();
                LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
                //下多单，28000美金，下单的数量是 price * quantity
                parameters.put("symbol", symbol);
                parameters.put("newClientOrderId", clientId);
                parameters.put("side", "BUY");
                parameters.put("type", "LIMIT");
                parameters.put("timeInForce", "GTC");
                parameters.put("quantity", BigDecimal.valueOf(amount / price).setScale(quantityPrecision, RoundingMode.DOWN));
                parameters.put("price", price);
                String result = trade.newOrder(parameters);
                System.out.println("买入订单信息：" + result);
                break;
            } catch (Exception e) {
                e2 = e;
                System.out.println("买多单失败重试：" + e.getMessage());
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException ex) {
                    //
                }
            }
            throw new RuntimeException(e2);
        }
    }


    public static void orderLimitPingDuo(String symbol, double price, double quanlity, int pricePrecision, String clientId, Trade trade) {
        for (int i = 0; i < 3; i++) {
            try {
                price = BigDecimal.valueOf(price).setScale(pricePrecision, RoundingMode.DOWN).doubleValue();
                LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
                parameters.put("symbol", symbol);
                parameters.put("newClientOrderId", clientId);
                parameters.put("side", "SELL");
                parameters.put("type", "LIMIT");
                parameters.put("timeInForce", "GTC");
                parameters.put("quantity", quanlity);
                parameters.put("price", price);
                String result = trade.newOrder(parameters);
                System.out.println("平多单" + result);
                break;
            } catch (Exception e) {
                System.out.println("平多单失败重试：" + e.getMessage());
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException ex) {
                    //
                }
            }
        }
    }
}
