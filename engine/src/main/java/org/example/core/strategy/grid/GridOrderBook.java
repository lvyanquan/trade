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

package org.example.core.strategy.grid;

import org.example.core.AccountModel;
import org.example.core.Constant;
import org.example.core.enums.OrderState;
import org.example.core.order.GridOrderManager;
import org.example.core.strategy.GridOrder;
import org.example.core.util.ProceCalcuteUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GridOrderBook {
    private int gridNumber;
    //网格比例，在中心价格下方的网格数量占据整体网格数量的百分比。默认下方占据60%，上方占据40%
    //后续这个比例可以根据价格在ema 120均线上方还是下方确定60% 还是 40%，强势时 下方40%，弱势时下方60%
    private double gridRatio = 0.6D;

    private GridOrderManager gridOrderManager;
    private final ArrayList<GridOrder> gridGridOrders = new ArrayList<>(gridNumber);

    private double centralPrice;
    private double atrPrice;

    private AccountModel accountModel;

    //====triggerBuyTradePrice 和 triggerSellTradePrice 触发之后。不是一定要卖，需要找到对应的订单是否能卖出和买入，并且更新价格
    //触发买入的价格
    private org.example.core.strategy.GridOrder nextBuyGridOrder;
    //触发卖出的价格
    private org.example.core.strategy.GridOrder nextSellGridOrder;


    public GridOrderBook(AccountModel accountModel, int gridNumber, double gridRatio, double centralPrice, double atrPrice) {
        this.accountModel = accountModel;
        this.gridNumber = gridNumber;
        this.gridRatio = gridRatio;
        if (Constant.API_KEY != null) {
            this.gridOrderManager = new GridOrderManager(Constant.API_KEY, Constant.SECRET_KEY);
        } else {
            //todo mockOrderManager 回归时用到
        }
        updateOrdersByCentralPrice(centralPrice, atrPrice);
        revovery();
    }

    public void updateOrdersByCentralPrice(double centralPrice, double atrPrice) {
        this.centralPrice = centralPrice;
        this.atrPrice = atrPrice;
        int downGridNumber = Double.valueOf(gridNumber * gridRatio).intValue();
        for (int i = 0; i < gridNumber; i++) {
            org.example.core.strategy.GridOrder gridOrder = new org.example.core.strategy.GridOrder(i);
            double gridPriceByAtr = centralPrice - ((downGridNumber - i) * (atrPrice));
            //固定atr确定好买入价格之后，越往下，网格间距会越来越大，这样在暴跌时，买入的价格会越来越低，这样可以减少风险
            double buyProce = gridPriceByAtr - ((downGridNumber - i) * 0.1 < 5 ? downGridNumber * 0.1 : 5) * atrPrice;
            gridOrder.updateTriggerPrice(buyProce);
            gridGridOrders.add(gridOrder);
        }
    }

    public void revovery() {
        //丁单薄的更新 最后加上下单操作即可上线了
        List<org.example.core.order.GridOrder> gridOrders = gridOrderManager.selectAllWorkerOrder();
        HashMap<Integer, org.example.core.order.GridOrder> orderMap = new HashMap<>();
        for (org.example.core.order.GridOrder gridOrder : gridOrders) {
            orderMap.put(gridOrder.getGridIndex(), gridOrder);
        }

        orderMap.forEach((k, v) -> {
            update(v);
        });

    }

    public GridOrder getOrder(int sequence){
        return gridGridOrders.get(sequence);
    }

    public boolean hasTrade() {
        return gridGridOrders.stream().anyMatch(k -> !k.canBuy());
    }

    public void update(org.example.core.order.GridOrder order) {
        org.example.core.strategy.GridOrder gridOrder = gridGridOrders.get(order.getGridIndex());

        if (order.getOrderState() == OrderState.FILLED || order.getOrderState() == OrderState.PARTIALLY_FILLED || (order.getOrderState() == OrderState.CANCELED && order.getExecutedQuantity() > 0)) {
            //买单成交，代表这个网格点可以卖出了
            if (order.getSide() == 0) {
                gridOrder.updateCanSell(order.getExecutedQuantity(), order.getAvgPrice(), getTriggerSellPrice(gridOrder), order.getUpdateTime());
            } else if (order.getSide() == 2) {
                //如果卖单成交，代表这个网格点可以买入了
                gridOrder.updateCanBuy();
            }
        } else if (order.getOrderState() == OrderState.NEW) {
            gridOrder.updateTradIng();
        } else if (order.getOrderState().isInvalid()) {
            gridOrder.updateCanBuy();
        }
    }

    public void updateTriggerOrder(double closePrice) {
        updateBuyOrder(closePrice);
        updateSellOrder();
    }

    public void updateBuyOrder(double closePrice) {
        org.example.core.strategy.GridOrder tempnextBuyGridOrder = null;
        for (int i = gridNumber - 1; i >= 0; i--) {
            org.example.core.strategy.GridOrder gridOrder = gridGridOrders.get(i);
            if (gridOrder.canBuy() && gridOrder.getTriggerBuyPrice() < closePrice) {
                tempnextBuyGridOrder = gridGridOrders.get(i);
                break;
            }
        }
        //当前价格下方没有一个可以买入的网格点，所以价格置为-1，永远不会触发买入
        //只有等到有卖出了，即价格回到网格范围内，才会重新更新 或者centerPrice变更，价格回到网格范围内
        if (tempnextBuyGridOrder == null) {
            nextBuyGridOrder = new org.example.core.strategy.GridOrder(-1);
            nextBuyGridOrder.updateTriggerPrice(-1);
        } else {
            nextBuyGridOrder = tempnextBuyGridOrder;
        }
    }

    //找到买入价格最低的一次成交记录，作为卖出价格触发
    //如果没有的话 就是当前价格最近的上方单子,有可能这个单子没持有。没货卖
    public void updateSellOrder() {
        org.example.core.strategy.GridOrder tempnextSellGridOrder = null;
        for (int i = 0; i < gridNumber; i++) {
            org.example.core.strategy.GridOrder gridOrder = gridGridOrders.get(i);
            if (gridOrder.canSell()) {
                if (tempnextSellGridOrder == null) {
                    tempnextSellGridOrder = gridGridOrders.get(i);
                } else {
                    tempnextSellGridOrder = tempnextSellGridOrder.getTriggerBuyPrice() < gridOrder.getTriggerBuyPrice() ? tempnextSellGridOrder : gridOrder;
                }
            }
        }
        //当前价格下方没有一个可以买入的网格点，所以价格置为-1，永远不会触发买入
        //只有等到有卖出了，即价格回到网格范围内，才会重新更新 或者centerPrice变更，价格回到网格范围内
        if (tempnextSellGridOrder == null) {
            nextSellGridOrder = new org.example.core.strategy.GridOrder(-1);
            nextSellGridOrder.setTriggerSellPrice(10000000000d);
        } else {
            nextSellGridOrder = tempnextSellGridOrder;
        }
    }

    public GridOrder getNextSellGridOrder() {
        return nextSellGridOrder;
    }

    public GridOrder getNextBuyGridOrder() {
        return nextBuyGridOrder;
    }
    public GridOrder getUpCanBuyOrderByPrice(double closePrice){
        for (int i = 0; i < gridNumber; i++) {
            org.example.core.strategy.GridOrder gridOrder = gridGridOrders.get(i);
            if (gridOrder.canBuy() && gridOrder.getTriggerBuyPrice() > closePrice) {
                return gridOrder;
            }
        }
        return null;
    }


    public GridOrderBookMetric getMetric() {
        return new GridOrderBookMetric(nextBuyGridOrder == null ? -1 : nextBuyGridOrder.getTriggerBuyPrice(),
                nextSellGridOrder == null ? -1 : nextSellGridOrder.getTriggerBuyPrice(),
                centralPrice,
                atrPrice,
                nextBuyGridOrder.getTriggerBuyPrice() - atrPrice,
                nextBuyGridOrder.getTriggerBuyPrice() + atrPrice);
    }


    //centralPrice下方有几个网格点,卖出的价格应该是越往下，卖出的间距越高
    public double getTriggerSellPrice(GridOrder gridOrder) {
        //每个订单设置买入价格
        //卖出价格 为买入价格 + atr *1.1 计算得出
        int downGridNumber = Double.valueOf(gridNumber * gridRatio).intValue();
        if (downGridNumber - gridOrder.getSequnce() > 0) {
            double multity = (downGridNumber - gridOrder.getSequnce()) * 0.08 < 5 ? (downGridNumber - gridOrder.getSequnce()) * 0.08 : 5;
            return gridOrder.getTriggerBuyPrice() + atrPrice + multity * atrPrice;
        } else {
            return gridOrder.getTriggerBuyPrice() + (atrPrice * 1.1);
        }
    }

    // 如果超过24小时没有卖出，就尝试最小价格卖出
    public double getlowSellPrice(GridOrder gridOrder) {
        return ProceCalcuteUtil.calculateBreakEvenSellPrice(accountModel.getFeeRate(), gridOrder.getQuantity(), gridOrder.getQuantity() * gridOrder.getOrderBuyPrice(), 4);
    }


    public static class GridOrderBookMetric {
        private double triggerBuyPrice;
        private double triggerSellPrice;
        private double centralPrice;
        private double atrPrice;
        private double lowBuyPrice;
        private double highBuyPrice;

        public GridOrderBookMetric(double triggerBuyPrice, double triggerSellPrice, double centralPrice, double atrPrice, double lowBuyPrice, double highBuyPrice) {
            this.triggerBuyPrice = triggerBuyPrice;
            this.triggerSellPrice = triggerSellPrice;
            this.centralPrice = centralPrice;
            this.atrPrice = atrPrice;
            this.lowBuyPrice = lowBuyPrice;
            this.highBuyPrice = highBuyPrice;
        }

        public double getTriggerBuyPrice() {
            return triggerBuyPrice;
        }

        public double getTriggerSellPrice() {
            return triggerSellPrice;
        }

        public double getCentralPrice() {
            return centralPrice;
        }

        public double getAtrPrice() {
            return atrPrice;
        }

        public double getLowBuyPrice() {
            return lowBuyPrice;
        }

        public double getHighBuyPrice() {
            return highBuyPrice;
        }
    }
}
