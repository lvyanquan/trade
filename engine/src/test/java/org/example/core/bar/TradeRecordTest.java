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
import com.google.common.collect.Lists;
import org.example.core.Constant;
import org.example.core.order.BaseOrder;
import org.example.core.order.OrderManager;
import org.example.core.order.analyze.BinanceGridTradingStats;
import org.example.core.order.analyze.GridTradeAnalysis;
import org.example.core.strategy.JdbcTest;
import org.junit.Test;

import java.util.List;

public class TradeRecordTest {
    Trade trade = new SpotClientImpl(Constant.API_KEY, Constant.SECRET_KEY).createTrade();

    @Test
    public void profile() throws Exception {
        BinanceGridTradingStats binanceGridTradingStats = new BinanceGridTradingStats(trade);
        binanceGridTradingStats.calculateStats();
    }


    @Test
    public void selectOrder() throws Exception {
        System.out.println(JdbcTest.selectNotTradeOrders());
        System.out.println(JdbcTest.selectTradeOrders().size());
    }

    @Test
    public void syncOrder() throws Exception {
        new OrderManager(Constant.API_KEY, Constant.SECRET_KEY).init(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000, System.currentTimeMillis(), "BTCUSDT");
    }

    @Test
    public void analyzeAllOrder() {
//        new OrderManager(Constant.API_KEY, Constant.SECRET_KEY)
//                .streamingSync(Lists.newArrayList("BTCUSDT"));
        // 从数据库加载数据，并创建 BaseOrder 和 Trade 实例列表
        List<BaseOrder> orders = JdbcTest.findAllOrder("BTCUSDT");

        GridTradeAnalysis analysis = new GridTradeAnalysis("grid-01");
        analysis.analyzeOrders(orders);
    }

}
