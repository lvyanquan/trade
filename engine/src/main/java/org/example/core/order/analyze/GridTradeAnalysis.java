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

package org.example.core.order.analyze;

import org.example.core.enums.Side;
import org.example.core.order.BaseOrder;
import org.example.core.strategy.JdbcTest;
import org.example.core.util.GsonUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GridTradeAnalysis {
    private static BigDecimal commition = BigDecimal.valueOf(60 * 0.00075);
    // 存储每个网格点的订单
    private Map<Integer, List<BaseOrder>> gridOrders = new HashMap<>();
    private List<TradeResult> tradeResults = new ArrayList<>();
    private List<BaseOrder> holdOn = new ArrayList<>();

    private String stragegyId;

    public GridTradeAnalysis(String stragegyId) {
        this.stragegyId = stragegyId;
    }

    // 从数据库中获取所有与 grid-01 策略相关的订单
    public void analyzeOrders(List<BaseOrder> orders) {
        for (BaseOrder order : orders) {
            if (stragegyId != null && order.getClientOrderId().startsWith(stragegyId)) {
                int gridIndex = extractGridIndex(order.getClientOrderId());
                gridOrders.computeIfAbsent(gridIndex, k -> new ArrayList<>()).add(order);
            }else{
                gridOrders.computeIfAbsent(0, k -> new ArrayList<>()).add(order);
            }
        }

        // 进行匹配交易分析
        for (Map.Entry<Integer, List<BaseOrder>> entry : gridOrders.entrySet()) {
            analyzeGrid(entry.getKey(), entry.getValue());
        }

        // 输出分析结果
        printAnalysis();
    }

    private int extractGridIndex(String clientOrderId) {
        // 从 clientOrderId 提取网格点
        String[] parts = clientOrderId.split("_");
        return Integer.parseInt(parts[1]);
    }

    private void analyzeGrid(int gridIndex, List<BaseOrder> orders) {
        orders.sort(Comparator.comparing(BaseOrder::getTime));

        BaseOrder buyOrder = null;

        for (BaseOrder order : orders) {
            if (order.getStatus().isInvalid()) {
                continue;
            }
            if (order.getSide().equals(Side.BUY)) {
                buyOrder = order;
            } else if (order.getSide().equals(Side.SELL) && buyOrder != null) {
                // 匹配成功，计算利润和净利润
                BigDecimal profit = order.getCummulativeQuoteQty().subtract(buyOrder.getCummulativeQuoteQty());
                BigDecimal netProfit = profit.subtract(commition).subtract(commition);

                TradeResult result = new TradeResult(gridIndex, buyOrder, order, profit, netProfit);
                tradeResults.add(result);

                // 重置 buyOrder 以进行新的匹配
                buyOrder = null;
            }
        }
        if (buyOrder != null) {
            holdOn.add(buyOrder);
        }

    }

    private void printAnalysis() {
        // 匹配成功的次数
        long matchedTrades = tradeResults.size();

        // 总利润和净利润
        BigDecimal totalProfit = tradeResults.stream()
                .map(TradeResult::getProfit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalNetProfit = tradeResults.stream()
                .map(TradeResult::getNetProfit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 总手续费
        BigDecimal totalCommission = tradeResults.stream()
                .flatMap(result -> Stream.of(commition, commition))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 最大和最小利润
        if(!tradeResults.isEmpty()){
            TradeResult maxProfitTrade = Collections.max(tradeResults, Comparator.comparing(TradeResult::getProfit));
            TradeResult maxLossTrade = Collections.min(tradeResults, Comparator.comparing(TradeResult::getProfit));

            // 平均利润和手续费
            BigDecimal averageProfit = totalProfit.divide(BigDecimal.valueOf(matchedTrades), RoundingMode.HALF_UP);
            BigDecimal averageCommission = totalCommission.divide(BigDecimal.valueOf(matchedTrades * 2), RoundingMode.HALF_UP);

            // 盈利和亏损的交易次数
            long profitableTrades = tradeResults.stream().filter(result -> result.getProfit().compareTo(BigDecimal.ZERO) > 0).count();
            long lossTrades = matchedTrades - profitableTrades;

            List<TradeResult> lossTradeList = tradeResults.stream().filter(result -> result.getProfit().compareTo(BigDecimal.ZERO) < 0).collect(Collectors.toList());
            // 输出结果
            System.out.println("匹配交易成功的次数: " + matchedTrades);
            System.out.println("总利润: " + totalProfit);
            System.out.println("总净利润: " + totalNetProfit);
            System.out.println("总手续费: " + totalCommission);
            System.out.println("最大盈利: " + maxProfitTrade.getProfit() + " 订单信息: " + maxProfitTrade);
            System.out.println("最大亏损: " + maxLossTrade.getProfit() + " 订单信息: " + maxLossTrade);
            System.out.println("平均利润: " + averageProfit);
            System.out.println("平均手续费: " + averageCommission);
            System.out.println("盈利的交易次数: " + profitableTrades);
            System.out.println("亏损的交易次数: " + lossTrades);
            System.out.println("持有的订单数量: " + holdOn.size());
            System.out.println("亏损的订单详情: " +  GsonUtil.GSON.toJson(lossTradeList));
            System.out.println("持有的订单: " + GsonUtil.GSON.toJson(holdOn));
        }else{
            System.out.println("持有的订单数量: " + holdOn.size());
            System.out.println("持有的订单: " + GsonUtil.GSON.toJson(holdOn));
        }

    }


    private class TradeResult {
        private int gridIndex;
        private BaseOrder buyOrder;
        private BaseOrder order;
        private BigDecimal profit;
        private BigDecimal netProfit;

        public TradeResult(int gridIndex, BaseOrder buyOrder, BaseOrder order, BigDecimal profit, BigDecimal netProfit) {
            this.gridIndex = gridIndex;
            this.buyOrder = buyOrder;
            this.order = order;
            this.profit = profit;
            this.netProfit = netProfit;
        }

        public int getGridIndex() {
            return gridIndex;
        }

        public BaseOrder getBuyOrder() {
            return buyOrder;
        }

        public BaseOrder getOrder() {
            return order;
        }

        public BigDecimal getProfit() {
            return profit;
        }

        public BigDecimal getNetProfit() {
            return netProfit;
        }

        @Override
        public String toString() {
            return "TradeResult{" +
                    "gridIndex=" + gridIndex +
                    ", buyOrder=" + buyOrder +
                    ", order=" + order +
                    ", profit=" + profit +
                    ", netProfit=" + netProfit +
                    '}';
        }
    }
}
