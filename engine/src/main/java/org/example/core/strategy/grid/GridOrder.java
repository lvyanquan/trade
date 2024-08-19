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

import org.example.core.enums.OrderState;

public class GridOrder {
    //    private double feeRate = 0.00075;
    //这是第几个单子
    private int sequnce;
    //触发价格买入
    private double triggerBuyPrice;

    //买入成交价格
    private double orderBuyPrice;
    //卖出价
    private double triggerSellPrice;

    //需要卖出的数量
    private double quantity;

    //0canBug  1交易中 2canSell
    private int status;

    private long lastBuyUpdateTime;


    public GridOrder(int sequnce) {
        this.sequnce = sequnce;
    }

    public void updateTriggerPrice(double price) {
        this.triggerBuyPrice = price;
    }

    //下单了 就直接更新
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getOrderBuyPrice() {
        return orderBuyPrice;
    }

    public double getTriggerBuyPrice() {
        return triggerBuyPrice;
    }

    public double getQuantity() {
        return quantity;
    }


    public void updateCanSell(double quantity, double orderBuyPrice, double sellPrice, long buyTime) {
        this.status = 2;
        this.quantity = quantity;
        this.orderBuyPrice = orderBuyPrice;
        this.triggerSellPrice = sellPrice;
        this.lastBuyUpdateTime = buyTime;
    }

    public void updateCanBuy() {
        this.status = 0;
        this.quantity = 0;
    }

    public void updateTradIng() {
        this.status = 1;
    }

    public boolean canSell() {
        return status == 2;
    }


    public boolean canBuy() {
        return status == 0;
    }

    public int getSequnce() {
        return sequnce;
    }

    public double getTriggerSellPrice() {
        return triggerSellPrice;
    }

    public void setTriggerSellPrice(double triggerSellPrice) {
        this.triggerSellPrice = triggerSellPrice;
        this.lastBuyUpdateTime = System.currentTimeMillis();
    }


    public long getLastBuyUpdateTime() {
        return lastBuyUpdateTime;
    }

    @Override
    public String toString() {
        return "GridOrder{" +
                "sequnce=" + sequnce +
                ", triggerBuyPrice=" + triggerBuyPrice +
                ", orderBuyPrice=" + orderBuyPrice +
                ", triggerSellPrice=" + triggerSellPrice +
                ", quantity=" + quantity +
                ", status=" + status +
                ", lastBuyUpdateTime=" + lastBuyUpdateTime +
                '}';
    }
}
