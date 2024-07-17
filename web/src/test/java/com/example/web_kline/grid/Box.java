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

package com.example.web_kline.grid;

import com.example.web_kline.grid.order.Order;
import com.example.web_kline.grid.order.OrderStateEnum;
import com.example.web_kline.grid.order.VirtualOrder;
import org.example.model.currency.Currency;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Box {
    private Currency currency;

    private String interval;

    private double lowPrice;
    private double UpPrice;

    /**
     * 网格数量
     */
    private int grids;

    //步长为初始化时的atr，每隔3天重新计算下Box重新初始化的
    private double step;

    private OrderBook orderBook;

    //总仓位
    private double allAmount;

    private double baseAmount;

    public Box(Currency currency, String interval, double lowPrice, double upPrice, double step, double allAmount) {
        this.currency = currency;
        this.interval = interval;
        this.lowPrice = lowPrice;
        this.UpPrice = upPrice;
        this.step = step;
        this.allAmount = allAmount;
        double d = (upPrice - lowPrice) / step;
        this.grids = (int) d;
    }

    public void setOrder(List<Order> orders, double latestPrice) {
        if (orderBook != null) {
            return;
        }
        if (orders == null || orders.isEmpty()) {
            orders = initOrder(latestPrice);
        } else {
            int i = Double.valueOf(grids * 0.3).intValue();
            int i1 = Double.valueOf(grids * 4).intValue();
            int i2 = grids - i - i1;

            double amount = allAmount / (1.2 * i + i1 + 0.8 * i2);
            baseAmount = currency.quanlityConvent(new BigDecimal(amount)).doubleValue();
            double amount1 = currency.quanlityConvent(new BigDecimal(baseAmount * 0.8)).doubleValue();
            double amount2 = baseAmount;
            double amount3 = currency.quanlityConvent(new BigDecimal(baseAmount * 1.2)).doubleValue();

            //todo 确定是倒叙还是循序，正常来说应该是顺序
            orders.sort((k, k2) -> Double.valueOf((k.getBuyPrice() - k2.getBuyPrice()) + "").intValue());
            ArrayList<Order> actualOrder = new ArrayList<>();
            for (int index = 0; index < grids; index++) {
                double actualAmount;
                if (index < i2) {
                    actualAmount = amount1;
                } else if (index < i2 + i1) {
                    actualAmount = amount2;
                } else {
                    actualAmount = amount3;
                }
                double sellPrice = lowPrice + ((index + 1) * step);
                if (!orders.isEmpty() && sellPrice > orders.get(0).getSellPrice() + step) {
                    actualOrder.add(orders.get(0));
                    orders.remove(0);
                } else {
                    actualOrder.add(new VirtualOrder(lowPrice + (index * step), actualAmount / (lowPrice + (index * step)), sellPrice));
                }
            }
            orders = actualOrder;
        }

        //orders 再根据ma booling挂些单子 比如当前价格是ma附近，买3个单子，即最近的上方三个单子 即index+3 如果都还未买入，那就一次性买入
        //如果是bolling中轨也是一样 买入多笔单子

        //上穿是多买 下穿是少买 尽量低一点位置买，比如下方第1.5个atr的地方买入
        this.orderBook = new OrderBook(orders);
    }


    public List<Order> initOrder(double latestPrice) {

        int i = Double.valueOf(grids * 0.3).intValue();
        int i1 = Double.valueOf(grids * 0.4).intValue();
        int i2 = grids - i - i1;

        double amount = allAmount / (1.2 * i + i1 + 0.8 * i2);
        baseAmount = currency.quanlityConvent(new BigDecimal(amount)).doubleValue();
        double amount1 = currency.quanlityConvent(new BigDecimal(baseAmount * 0.8)).doubleValue();
        double amount2 = baseAmount;
        double amount3 = currency.quanlityConvent(new BigDecimal(baseAmount * 1.2)).doubleValue();
        ArrayList<Order> orders = new ArrayList<>();
        for (int index = 0; index < grids; index++) {
            double actualAmount;
            if (index < i2) {
                actualAmount = amount1;
            } else if (index < i2 + i1) {
                actualAmount = amount2;
            } else {
                actualAmount = amount3;
            }
            orders.add(new VirtualOrder(lowPrice + (index * step), actualAmount / (lowPrice + (index * step)), lowPrice + ((index + 1) * step)));
        }
        return orders;
    }

    int getSellOrder(double currentPrice) {
        return orderBook.sellLatestOrder(currentPrice);
    }

    Order getOrder(int index) {
        return orderBook.getOrder(index);
    }

    //市价买入了
    List<Integer> getBuyOrder(double currentPrice) {
        return doJudgeBuyId(currentPrice);
    }

    private List<Integer> doJudgeBuyId(double currentPrice) {
        int i = orderBook.buyLatestOrder(currentPrice);
        if (i == -1) {
            return null;
        }
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(i);
        //如果下面单子也能买 那我就不买了 触发价必须是 这个地方要有一个循环，找到最下面一个符合要求的order 然后减去一个atr 才能触发购买？
        while (i != -1) {
            i = orderBook.nextBuyId(i);
            if (i != -1) {
                //此时进行购买 然后返回的id 应该是符合要求的最低的那个 并且购买的amount
                ids.add(i);
            }
        }
        return ids;
    }

    void updateBuying(int index) {
        orderBook.getOrder(index).updateState(OrderStateEnum.BUYING);
    }

    void updateInit(int index) {
        orderBook.getOrder(index).updateState(OrderStateEnum.INIT);
    }

    //状态为buyed的单子进行购买确认
    void updateBuyOrder(int index, double amount) {{
            if (amount == 0) {
                orderBook.getOrder(index).updateActualAmount(0);
                orderBook.getOrder(index).updateState(OrderStateEnum.INIT);
            } else {
                orderBook.getOrder(index).updateActualAmount(amount);
                orderBook.getOrder(index).updateState(OrderStateEnum.BUYED);
            }

        }
    }

    void updateBuyOrder(String orderId, double amount) {{
        if (amount == 0) {
            orderBook.getOrder(orderId).updateActualAmount(0);
            orderBook.getOrder(orderId).updateState(OrderStateEnum.INIT);
        } else {
            orderBook.getOrder(orderId).updateActualAmount(amount);
            orderBook.getOrder(orderId).updateState(OrderStateEnum.BUYED);
        }

    }
    }

    void updateSellIngOrder(int index) {
        orderBook.getOrder(index).updateState(OrderStateEnum.SELLING);
    }

    void updateSellOrder(int index) {
        orderBook.getOrder(index).updateState(OrderStateEnum.INIT);
    }

    void updateSellOrder(String clientId) {
        orderBook.getOrder(clientId).updateState(OrderStateEnum.INIT);
    }

    //reset方法，上下限不变，仅仅是step的重新更新，order就也需要重新更新了

    // 移动 即上下限的变动 其实就是new 新的box 然后执行上面的reset方法 进行重新订正，1. 上限移动，重新购买 2下限的话 就需要平单几个了


}
