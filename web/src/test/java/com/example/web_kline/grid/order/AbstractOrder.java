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

package com.example.web_kline.grid.order;

public abstract class AbstractOrder implements Order {
    //触发买入价
    protected double price;

    //买入固定数量
    protected double amount;
    //触发卖出价
    protected double SellingPrice;

    protected OrderStateEnum state;

    public double getAmount() {
        return amount;
    }

    public double getBuyPrice() {
        return price;
    }

    public double getSellPrice() {
        return SellingPrice;
    }

    @Override
    public OrderStateEnum getState() {
        return state;
    }

    @Override
    public void updateState(OrderStateEnum orderStateEnum) {
        this.state = orderStateEnum;
    }
}
