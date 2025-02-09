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

import org.example.core.enums.OrderState;

public class GridOrder {
    private String orderId;
    private String symbol;

    private int gridIndex;

    //下单价格
    private double price;
    //下单数量qty
    private double quantity;

    //成交价格
    private double avgPrice;
    //成交数量qty
    private double executedQuantity;

    private OrderState orderState;

    private int side;

    private long updateTime;

    public GridOrder(String orderId, String symbol, int gridIndex, double avgPrice, double executedQuantity, OrderState orderState, int side,long time) {
        this.orderId = orderId;
        this.symbol = symbol;
        this.gridIndex = gridIndex;
        this.avgPrice = avgPrice;
        this.executedQuantity = executedQuantity;
        this.orderState = orderState;
        this.side = side;
        this.updateTime = time;
    }

    public GridOrder(String orderId, String symbol, int gridIndex, double price, double quantity, int side,long time) {
        this.orderId = orderId;
        this.symbol = symbol;
        this.gridIndex = gridIndex;
        this.price = price;
        this.quantity = quantity;
        this.orderState = OrderState.NEW;
        this.side = side;
        this.updateTime = time;
    }

    public String getOrderId() {
        return orderId;
    }

    public double getAvgPrice() {
        return avgPrice;
    }

    public double getExecutedQuantity() {
        return executedQuantity;
    }

    public OrderState getOrderState() {
        return orderState;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getGridIndex() {
        return gridIndex;
    }

    public int getSide() {
        return side;
    }

    public double getPrice() {
        return price;
    }

    public double getQuantity() {
        return quantity;
    }

    public long getUpdateTime() {
        return updateTime;
    }
}
