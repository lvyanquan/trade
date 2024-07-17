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

package org.example.client.vo;

public class TradeVo {
    private String orderId;

    //交易id
    private String tradeId;

    /**交易所订单id**/
    private String exchangeOrderId;
    //成交价格
    private double price;

    //成交数量qty
    private double quantity;

    //成交总金额
    private double amount;

    //手续费
    private double commission;

    //成交时间
    private long trade_time;

    public TradeVo(String tradeId, String exchangeOrderId, double price, double quantity, double amount, double commission, long trade_time) {
        this.tradeId = tradeId;
        this.exchangeOrderId = exchangeOrderId;
        this.price = price;
        this.quantity = quantity;
        this.amount = amount;
        this.commission = commission;
        this.trade_time = trade_time;
    }

    public String getTradeId() {
        return tradeId;
    }

    public double getPrice() {
        return price;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getAmount() {
        return amount;
    }

    public double getCommission() {
        return commission;
    }

    public long getTrade_time() {
        return trade_time;
    }

    public String getExchangeOrderId() {
        return exchangeOrderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }


}
