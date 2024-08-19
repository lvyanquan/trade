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

package org.example.core.analyze;

import com.binance.connector.client.impl.spot.Trade;
import org.example.core.enums.OrderState;
import org.example.core.order.BinanceTradeJsonParse;
import org.example.core.order.OrderResponseInfo;
import org.example.core.order.TradeRecord;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BinanceGridTradingStats {

    private final Trade trade;  // 假设有一个接口实现类 TradeApi，用来与Binance API交互

    public BinanceGridTradingStats(Trade trade) {
        this.trade = trade;
    }

    public void calculateStats() {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", "BTCUSDT");
        parameters.put("limit", "500");

        // 获取所有的订单
        List<OrderResponseInfo> orderResponseInfos = BinanceTradeJsonParse.parseOrders(trade.getOrders(parameters));
        orderResponseInfos = orderResponseInfos.stream()
                .filter(i -> i.getClientOrderId().startsWith("grid"))
                .collect(Collectors.toList());

        parameters.put("limit", "500");

        // 统计变量初始化
        int totalOrderCount = 0;
        double totalCommission = 0;
        double totalNetProfit = 0;
        int totalNormalTrade = 0;
        double maxProfit = Double.MIN_VALUE;
        double minProfit = Double.MAX_VALUE;

        Map<String, Double> gridProfits = new HashMap<>();
        Map<String, Double> gridQuantities = new HashMap<>();
        Map<String, Double> gridPrices = new HashMap<>();
        Map<String, TradeInfo> gridHolding = new HashMap<>();

        for (OrderResponseInfo order : orderResponseInfos) {
            if (OrderState.orderState(order.getStatus()).isTrade()) {
                String clientOrderId = order.getClientOrderId();
                String[] parts = clientOrderId.split("_");
                String gridNumber = parts[1];

                parameters.put("orderId", order.getOrderId());
                // 获取并合并交易记录
                List<TradeRecord> trades = BinanceTradeJsonParse.parseTrade(trade.myTrades(parameters));
                TradeInfo mergedTrade = mergeTrades(trades);

                // 统计总手续费
                totalCommission += mergedTrade.getTotalCommission();

                // 判断是买入还是卖出，并进行匹配
                if (order.getSide().equals("BUY")) {
                    gridHolding.put(gridNumber, mergedTrade);
                } else if (order.getSide().equals("SELL")) {
                    TradeInfo buyTrade = gridHolding.remove(gridNumber);
                    if (buyTrade != null) {
                        totalNormalTrade++;
                        double profit = calculateProfit(buyTrade, mergedTrade);
                        gridProfits.put(gridNumber, profit);
                        totalNetProfit += profit;

                        // 更新最大和最小利润
                        maxProfit = Math.max(maxProfit, profit);
                        minProfit = Math.min(minProfit, profit);
                    }
                }

                totalOrderCount++;
            }
        }

        // 计算当前持有的均价、数量和总价值
        double currentQty = gridHolding.values().stream().mapToDouble(TradeInfo::getTotalQty).sum();
        double currentValue = gridHolding.values().stream().mapToDouble(t -> t.getAveragePrice() * t.getTotalQty()).sum();
        double averagePrice = currentQty != 0 ? currentValue / currentQty : 0;

        // 输出统计结果
        System.out.println("总成交订单数量: " + totalOrderCount);
        System.out.println("总匹配订单数量: " + totalNormalTrade);
        System.out.println("未匹配的订单数量: " + gridHolding.size());
        System.out.println("总手续费: " + totalCommission);
        System.out.println("总净利润: " + totalNetProfit);
        System.out.println("平均订单利润: " + (totalNetProfit / totalNormalTrade));
        System.out.println("最大订单利润: " + maxProfit);
        System.out.println("最小订单利润: " + minProfit);
        System.out.println("当前持有均价: " + averagePrice);
        System.out.println("当前持有数量: " + currentQty);
        System.out.println("当前持有总价值: " + currentValue);

    }

    // 合并订单的所有交易记录，返回一个TradeInfo对象
    private TradeInfo mergeTrades(List<TradeRecord> trades) {
        double totalQty = 0;
        double totalQuoteQty = 0;
        double totalCommission = 0;

        for (TradeRecord trade : trades) {
            totalQty += trade.getQty();
            totalQuoteQty += trade.getQuoteQty();
            totalCommission += trade.getCommission();
        }

        double averagePrice = totalQuoteQty / totalQty;
        return new TradeInfo(averagePrice, totalQty, totalQuoteQty, totalCommission);
    }

    // 计算买入和卖出之间的利润，考虑手续费
    private double calculateProfit(TradeInfo buyTrade, TradeInfo sellTrade) {
        double buyValue = buyTrade.getTotalQuoteQty();
        double sellValue = sellTrade.getTotalQuoteQty();
        return sellValue - buyValue - buyTrade.getTotalCommission() - sellTrade.getTotalCommission();
    }

}

// 辅助类TradeInfo，用于保存合并后的交易信息
class TradeInfo {
    private double averagePrice;
    private double totalQty;
    private double totalQuoteQty;
    private double totalCommission;

    public TradeInfo(double averagePrice, double totalQty, double totalQuoteQty, double totalCommission) {
        this.averagePrice = averagePrice;
        this.totalQty = totalQty;
        this.totalQuoteQty = totalQuoteQty;
        this.totalCommission = totalCommission;
    }

    public double getAveragePrice() {
        return averagePrice;
    }

    public double getTotalQty() {
        return totalQty;
    }

    public double getTotalQuoteQty() {
        return totalQuoteQty;
    }

    public double getTotalCommission() {
        return totalCommission;
    }
}

