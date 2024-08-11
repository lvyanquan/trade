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

import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.client.impl.spot.Trade;
import org.example.core.Constant;
import org.example.core.order.BinanceGridTradingStats;
import org.example.core.order.BinanceTradeJsonParse;
import org.example.core.order.OrderResponseInfo;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;

public class TradeRecordTest {
    Trade trade = new SpotClientImpl(Constant.API_KEY, Constant.SECRET_KEY).createTrade();

    @Test
    public void getOrders() throws Exception {

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", "BTCUSDT");
        parameters.put("limit", "10");
        String orderString = trade.getOrders(parameters);
        System.out.println(orderString);
        List<OrderResponseInfo> orderResponseInfos = BinanceTradeJsonParse.parseOrders(orderString);

        System.out.println(orderResponseInfos);
    }

    @Test
    public void getTrades() throws Exception {

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", "BTCUSDT");
        parameters.put("limit", "10");
        String s = trade.myTrades(parameters);
        String orderString = trade.getOrders(parameters);
        System.out.println(orderString);
        List<OrderResponseInfo> orderResponseInfos = BinanceTradeJsonParse.parseOrders(orderString);

        System.out.println(orderResponseInfos);
    }

    @Test
    public void profile() throws Exception {
        BinanceGridTradingStats binanceGridTradingStats = new BinanceGridTradingStats(trade);
        binanceGridTradingStats.calculateStats();
    }
}
