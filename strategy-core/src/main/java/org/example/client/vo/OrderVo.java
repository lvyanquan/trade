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

import org.example.client.dto.OrderResponseInfo;
import org.example.enums.Exchange;
import org.example.enums.OrderStatus;
import org.example.model.currency.Currency;
import org.example.model.enums.OrderSide;

import java.util.List;

public class OrderVo extends OrderResponseInfo {


    List<TradeVo> trades;


    //总的手续费
    private double commission;

    //成交的币种数量
    private double tradeQuantity;

    //成交的总金额
    private double tradeAmount;


    public OrderVo(String id, String exchangeOrderId, Exchange exchang, Currency currency, OrderStatus orderStatus, OrderSide orderSide, long time, double qty, double amount, double price, List<TradeVo> trades, double tradAmount, double tradeQuantity, double commission) {
        super(id, exchangeOrderId, exchang, currency, orderStatus, orderSide, time,qty,price,amount);
        this.trades = trades;
        this.commission = commission;
        this.tradeQuantity = tradeQuantity;
        this.tradeAmount = tradAmount;
    }

    public List<TradeVo> getTrades() {
        return trades;
    }


    public void setTrades(List<TradeVo> trades) {
        this.trades = trades;
    }

    public double getCommission() {
        return commission;
    }

    public void setCommission(double commission) {
        this.commission = commission;
    }

    public double getTradeQuantity() {
        return tradeQuantity;
    }

    public void setTradeQuantity(double tradeQuantity) {
        this.tradeQuantity = tradeQuantity;
    }

    public double getTradeAmount() {
        return tradeAmount;
    }

    public void setTradeAmount(double tradeAmount) {
        this.tradeAmount = tradeAmount;
    }

    @Override
    public String toString() {
        return "OrderVo{" +
                "trades=" + trades +
                ", commission=" + commission +
                ", tradeQty=" + tradeQuantity +
                ", tradeAmount=" + tradeAmount +
                '}';
    }
}
