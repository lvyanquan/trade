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

import org.example.core.util.GsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BinanceTradeJsonParse {

    public static List<TradeRecord> parseTrade(String s) {
        List<Map<String, Object>> orderMap = GsonUtil.GSON.fromJson(s, List.class);
        ArrayList<TradeRecord> orderResponseInfos = new ArrayList<>();
        for (Map<String, Object> stringObjectMap : orderMap) {
            orderResponseInfos.add(parseTrade(stringObjectMap));
        }
        return orderResponseInfos;

    }

    public static List<OrderResponseInfo> parseOrders(String s) {
        List<Map<String, Object>> orderMap = GsonUtil.GSON.fromJson(s, List.class);
        ArrayList<OrderResponseInfo> orderResponseInfos = new ArrayList<>();
        for (Map<String, Object> stringObjectMap : orderMap) {
            orderResponseInfos.add(parseOrder(stringObjectMap));
        }
        return orderResponseInfos;

    }


    public static OrderResponseInfo parseOrder(String s) {
        Map<String, Object> orderMap = GsonUtil.GSON.fromJson(s, Map.class);
        return parseOrder(orderMap);
    }

    public static OrderResponseInfo parseOrder(Map<String, Object> orderMap) {
        String id = orderMap.get("clientOrderId").toString();
        String orderId = orderMap.get("orderId").toString();
        String crrency = orderMap.get("symbol").toString();
        String status = orderMap.get("status").toString();
        long updateTime = Long.parseLong(orderMap.get("updateTime").toString());

        // 累计交易的金额 实际金额
        double cummulativeQuoteQty = Double.parseDouble(orderMap.get("cummulativeQuoteQty").toString());
        // 交易的仓位数量
        double executedQty = Double.parseDouble(orderMap.get("executedQty").toString());
        //原始的仓位数量
        double origQty = Double.parseDouble(orderMap.get("origQty").toString());
        double price = Double.parseDouble(orderMap.get("price").toString());
        String side = orderMap.get("side").toString();


        return new OrderResponseInfo(id, orderId, crrency, status, cummulativeQuoteQty, executedQty, origQty, price, side,updateTime);

    }

    public static TradeRecord parseTrade(Map<String, Object> orderMap) {
        String id = orderMap.get("id").toString();
        String orderId = orderMap.get("orderId").toString();
        String symbol = orderMap.get("symbol").toString();
        double price = Double.parseDouble(orderMap.get("price").toString());
        double qty = Double.parseDouble(orderMap.get("qty").toString());
        double quoteQty = Double.parseDouble(orderMap.get("quoteQty").toString());
        double commission = Double.parseDouble(orderMap.get("commission").toString());
        String commissionAsset = orderMap.get("commissionAsset").toString();
        boolean isBuyer = Boolean.parseBoolean(orderMap.get("isBuyer").toString());
        boolean isMaker = Boolean.parseBoolean(orderMap.get("isMaker").toString());
        boolean isBestMatch = Boolean.parseBoolean(orderMap.get("isBestMatch").toString());
        return new TradeRecord(id, orderId, symbol, price, qty, quoteQty, commission, commissionAsset, isBuyer, isMaker, isBestMatch);
    }
}
