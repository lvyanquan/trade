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

import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.client.impl.spot.Trade;
import org.example.core.strategy.JdbcTest;
import org.example.core.util.GsonUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OrderManager {

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private Trade trade;

    public OrderManager(String apiKey, String secretKey) {
        this.trade = new SpotClientImpl(apiKey, secretKey).createTrade();
    }

    public void init(long start, long end, String symbol) {
        //删除 再insert
        JdbcTest.deleteOrders(symbol, start, end);
        JdbcTest.deleteTrades(symbol, start, end);
        sync(start, end, symbol);
        syncTrades(start, end, symbol);
    }

    public void streamingSync(List<String> symbols) {
        executor.scheduleWithFixedDelay(
                () -> {
                    for (String symbol : symbols) {
                        BaseOrder lastOrder = JdbcTest.findLastOrder(symbol);
                        if (lastOrder != null) {
                            //数据库里查询最后一条数据
                            sync(lastOrder.getTime() + 1, System.currentTimeMillis(), symbol);
                        } else {
                            //不存在就尝试加载最近7天内数据
                            sync(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000, System.currentTimeMillis(), symbol);
                        }
                    }
                    for (String symbol : symbols) {
                        org.example.core.order.Trade lastTrade = JdbcTest.findLastTrader(symbol);
                        if (lastTrade != null) {
                            //数据库里查询最后一条数据
                            syncTrades(lastTrade.getTime() + 1, System.currentTimeMillis(), symbol);
                        } else {
                            //不存在就尝试加载最近7天内数据
                            syncTrades(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000, System.currentTimeMillis(), symbol);
                        }
                    }
                },
                0,
                300,
                TimeUnit.SECONDS);
    }

    //从某个时间点开始从头刷新订单
    //[start,end]
    private void sync(long start, long end, String symbol) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("limit", "1000");
        parameters.put("symbol", symbol);

        long currentStartTime = start;
        long oneDayInMillis = 24 * 60 * 60 * 1000;  // 24小时的毫秒数

        while (true) {
            // 计算当前查询的endTime
            long currentEndTime = currentStartTime + oneDayInMillis;
            if (currentEndTime > end) {
                currentEndTime = end;
            }

            // 设置查询参数
            parameters.put("startTime", currentStartTime);
            parameters.put("endTime", currentEndTime);

            // 调用 API 获取订单数据
            List<Map<String, Object>> orderMap = GsonUtil.GSON.fromJson(trade.getOrders(parameters), List.class);

            // 如果没有更多订单，有可能是这段时间内没有交易，所以还是要继续查找
            if (orderMap == null || orderMap.isEmpty()) {
                // 确保查询范围不重复，更新 startTime 为最后一个订单的时间戳 +1
                currentStartTime = currentEndTime;
                // 如果已经查询到指定的end时间点，结束循环
                if (currentStartTime >= end) {
                    break;
                }
                continue;
            }

            // 插入获取的订单数据到数据库
            JdbcTest.insertOrders(orderMap);

            // 更新当前的startTime，继续查询下一个时间段的订单
            Map<String, Object> lastOrder = orderMap.get(orderMap.size() - 1);
            long lastOrderTime = ((Number) lastOrder.get("time")).longValue();

            // 确保查询范围不重复，更新 startTime 为最后一个订单的时间戳 +1
            currentStartTime = lastOrderTime + 1;

            // 如果已经查询到指定的end时间点，结束循环 查询范围是 [start,end]，binance接口返回的数据也是[start,end]，
            // 所以这里需要判断是否超过end，而不是 >=
            if (currentStartTime > end) {
                break;
            }
        }
    }


    private void syncTrades(long start, long end, String symbol) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("limit", "1000");
        parameters.put("symbol", symbol);

        long currentStartTime = start;
        long oneDayInMillis = 24 * 60 * 60 * 1000;  // 24小时的毫秒数

        while (true) {
            // 计算当前查询的endTime
            long currentEndTime = currentStartTime + oneDayInMillis;
            if (currentEndTime > end) {
                currentEndTime = end;
            }

            // 设置查询参数
            parameters.put("startTime", currentStartTime);
            parameters.put("endTime", currentEndTime);

            // 调用 API 获取交易数据
            List<Map<String, Object>> tradeMap = GsonUtil.GSON.fromJson(trade.myTrades(parameters), List.class);

            // 如果没有更多交易数据，有可能是这段时间内没有交易，所以还是要继续查找
            if (tradeMap == null || tradeMap.isEmpty()) {
                // 确保查询范围不重复，更新 startTime 为最后一个交易的时间戳 +1
                currentStartTime = currentEndTime;
                // 如果已经查询到指定的end时间点，结束循环
                if (currentStartTime >= end) {
                    break;
                }
                continue;
            }

            // 插入获取的交易数据到数据库
            JdbcTest.insertTrades(tradeMap);

            // 更新当前的startTime，继续查询下一个时间段的交易
            Map<String, Object> lastTrade = tradeMap.get(tradeMap.size() - 1);
            long lastTradeTime = ((Number) lastTrade.get("time")).longValue();

            // 确保查询范围不重复，更新 startTime 为最后一个交易的时间戳 +1
            currentStartTime = lastTradeTime + 1;

            // 如果已经查询到指定的end时间点，结束循环 查询范围是 [start,end]，binance接口返回的数据也是[start,end]，
            // 所以这里需要判断是否超过end，而不是 >=
            if (currentStartTime > end) {
                break;
            }
        }
    }


}
