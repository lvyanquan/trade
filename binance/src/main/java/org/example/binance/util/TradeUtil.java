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

package org.example.binance.util;

import com.binance.connector.client.impl.spot.Trade;
import org.example.model.currency.Currency;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

public class TradeUtil {

    public static void orderLimitDuo(Currency currency, double price, double amount,String clientId, Trade trade) {
        for (int i = 0; i < 3; i++) {
            try {
                LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
                //下多单，28000美金，下单的数量是 price * quantity
                parameters.put("symbol", currency.symbol());
                parameters.put("newClientOrderId", clientId);
                parameters.put("side", "BUY");
                parameters.put("type", "LIMIT");
                parameters.put("timeInForce", "GTC");
                parameters.put("quantity",currency.quanlityConvent(amount/price));
                parameters.put("price", currency.priceConvent(price));
                String result = trade.newOrder(parameters);
                System.out.println("买入订单信息：" + result);
                break;
            } catch (Exception e) {
                System.out.println("买多单失败重试：" + e.getMessage());
            }
        }
    }




    public static void orderLimitPingDuo(Currency currency, double price, double quanlity,String clientId, Trade trade) {
        for (int i = 0; i < 3; i++) {
            try {

                LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
                parameters.put("symbol", currency.symbol());
                parameters.put("newClientOrderId", clientId);
                parameters.put("side", "SELL");
                parameters.put("type", "LIMIT");
                parameters.put("timeInForce", "GTC");
                parameters.put("quantity", currency.quanlityConvent(quanlity));
                parameters.put("price", currency.priceConvent(price));
                String result = trade.newOrder(parameters);
                System.out.println("平多单" + result);
                break;
            } catch (Exception e) {
                System.out.println("平多单失败重试：" + e.getMessage());
            }
        }
    }




//
//    protected void orderLimitKong(double price) {
//        for (int i = 0; i < 3; i++) {
//            try {
//                LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
//                parameters.put("symbol", currency.symbol());
//                parameters.put("side", "SELL");
//                parameters.put("positionSide", "SHORT");
//                parameters.put("type", "LIMIT");
//                parameters.put("timeInForce", "GTC");
//                parameters.put("quantity", BigDecimal.valueOf(amount / price).setScale(currency.getQuantityPrecision(), RoundingMode.DOWN));
//                parameters.put("price", BigDecimal.valueOf(price).setScale(currency.getPricePrecision(), RoundingMode.DOWN));
//                String result = client.account().newOrder(parameters);
//                System.out.println("买入订单信息：" + result);
//                break;
//            } catch (Exception e) {
//                System.out.println("平多单失败重试：" + e.getMessage());
//            }
//        }
//    }
//
//
//    protected void orderMarketPingDuo(double price) {
//
//        for (int i = 0; i < 3; i++) {
//            try {
//                LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
//                //下多单，28000美金，下单的数量是 price * quantity
//                parameters.put("symbol", currency.symbol());
//                parameters.put("side", "SELL");
//                parameters.put("positionSide", "long");
//                parameters.put("type", "MARKET");
//                parameters.put("quantity", BigDecimal.valueOf(amount / price).setScale(currency.getQuantityPrecision(), RoundingMode.DOWN));
//                String result = client.account().newOrder(parameters);
//                System.out.println("平多单信息：" + result);
//                break;
//            } catch (Exception e) {
//                System.out.println("平多单失败重试：" + e.getMessage());
//            }
//        }
//    }
//
//
//    protected void orderMarketKong(double price) {
//        for (int i = 0; i < 3; i++) {
//            try {
//                LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
//                parameters.put("symbol", currency.symbol());
//                parameters.put("side", "SELL");
//                parameters.put("positionSide", "SHORT");
//                parameters.put("type", "MARKET");
//                parameters.put("quantity", BigDecimal.valueOf(amount / price).setScale(currency.getQuantityPrecision(), RoundingMode.DOWN));
//                String result = client.account().newOrder(parameters);
//                System.out.println("买入订单信息：" + result);
//                break;
//            } catch (Exception e) {
//                System.out.println("平多单失败重试：" + e.getMessage());
//            }
//        }
//    }
//
//    protected void orderLimitPingkong(double price) {
//        for (int i = 0; i < 3; i++) {
//            try {
//                LinkedHashMap<String, Object> parameters3 = new LinkedHashMap<>();
//                parameters3.put("symbol", currency.symbol());
//                parameters3.put("side", "BUY");
//                parameters3.put("positionSide", "SHORT");
//                parameters3.put("type", "LIMIT");
//                parameters3.put("timeInForce", "GTC");
//                parameters3.put("quantity", BigDecimal.valueOf(amount / price).setScale(currency.getQuantityPrecision(), RoundingMode.DOWN));
//                parameters3.put("price", BigDecimal.valueOf(price).setScale(currency.getPricePrecision(), 1));
//                String result = client.account().newOrder(parameters3);
//                System.out.println("平空单" + result);
//                break;
//            } catch (Exception e) {
//                System.out.println("平空单失败重试：" + e.getMessage());
//            }
//        }
//    }
//
//    protected void orderMarketPingKong(double price) {
//        for (int i = 0; i < 3; i++) {
//            try {
//                LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
//                parameters.put("symbol", currency.symbol());
//                parameters.put("side", "BUY");
//                parameters.put("positionSide", "SHORT");
//                parameters.put("type", "MARKET");
//                parameters.put("quantity", BigDecimal.valueOf(amount / price).setScale(currency.getQuantityPrecision(), RoundingMode.DOWN));
//                String result = client.account().newOrder(parameters);
//                System.out.println("平空单信息：" + result);
//                break;
//            } catch (Exception e) {
//                System.out.println("平空单失败重试：" + e.getMessage());
//            }
//        }
//    }
//
//    protected void orderLimitBuy(double price) {
//        for (int i = 0; i < 3; i++) {
//            try {
//                LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
//                //下多单，28000美金，下单的数量是 price * quantity
//                parameters.put("symbol", currency.symbol());
//                parameters.put("side", "BUY");
//                parameters.put("type", "LIMIT");
//                parameters.put("timeInForce", "GTC");
//                parameters.put("quantity", BigDecimal.valueOf(amount / price).setScale(currency.getQuantityPrecision(), RoundingMode.DOWN));
//                parameters.put("price", BigDecimal.valueOf(price).setScale(currency.getPricePrecision(), RoundingMode.DOWN));
//                String result = spotClient.createTrade().newOrder(parameters);
//                System.out.println("买入订单信息：" + result);
//                break;
//            } catch (Exception e) {
//                System.out.println("买多单失败重试：" + e.getMessage());
//            }
//        }
//    }
//
//    protected void orderLimitSell(double price) {
//        for (int i = 0; i < 3; i++) {
//            try {
//                LinkedHashMap<String, Object> parameters3 = new LinkedHashMap<>();
//                parameters3.put("symbol", currency.symbol());
//                parameters3.put("side", "SELL");
//                parameters3.put("type", "LIMIT");
//                parameters3.put("timeInForce", "GTC");
//                parameters3.put("quantity", BigDecimal.valueOf(amount / price).setScale(currency.getQuantityPrecision(), RoundingMode.DOWN));
//                parameters3.put("price", BigDecimal.valueOf(price).setScale(currency.getPricePrecision(), RoundingMode.DOWN));
//                String result = spotClient.createTrade().newOrder(parameters3);
//                System.out.println("平多单" + result);
//                break;
//            } catch (Exception e) {
//                System.out.println("平多单失败重试：" + e.getMessage());
//            }
//        }
//    }

}
