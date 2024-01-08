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

package org.example.order;

import org.example.data.currency.Currency;
import org.example.enums.OrderSide;
import org.example.enums.OrderType;

import java.math.BigDecimal;

public class Order {
    private String virtualId;

    /**交易所订单id**/
    private String exchangEid;

    //订单类型是market 还是 limit限价单
    private OrderType orderType;

    //订单方向
    private OrderSide orderSide;

    private Currency currency;

    private BigDecimal price;

    private BigDecimal amount;


    public OrderType getOrderType() {
        return orderType;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public OrderSide getOrderSide() {
        return orderSide;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getVirtualId() {
        return virtualId;
    }
}
