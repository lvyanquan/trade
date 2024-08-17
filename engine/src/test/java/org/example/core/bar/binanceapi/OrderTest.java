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

package org.example.core.bar.binanceapi;

import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.client.impl.spot.Trade;
import org.example.core.Constant;
import org.example.core.order.BinanceTradeJsonParse;
import org.example.core.order.OrderResponseInfo;
import org.example.core.util.GsonUtil;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderTest {
    Trade trade = new SpotClientImpl(Constant.API_KEY, Constant.SECRET_KEY).createTrade();

    @Test
    //返回的是[]区间
    public void getOrders() throws Exception {

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", "BTCUSDT");
        parameters.put("limit", "10");
        parameters.put("startTime", 1723810812155L);
        parameters.put("endTime", 1723839577627L);
        String s = trade.myTrades(parameters);
        String trades = trade.myTrades(parameters);
        System.out.println(trades);
    }


    @Test
    //返回的是[]区间
    public void orders(){
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", "BTCUSDT");
        parameters.put("startTime", 1723810790926L);
        parameters.put("endTime", 1723810790926L);
        parameters.put("limit", "10");

        // 获取所有的订单
        List<Map<String, Object>> orderMap = GsonUtil.GSON.fromJson(trade.getOrders(parameters), List.class);
        System.out.println(orderMap.get(0).get("time"));
        System.out.println(orderMap.get(0).get("time"));
    }
}
