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

package org.example.client.dto;

import org.example.enums.Exchange;
import org.example.enums.OrderStatus;
import org.example.model.currency.Currency;
import org.example.model.enums.ContractType;
import org.example.model.enums.OrderSide;

public class OrderResponseInfo {

    //客户端自己构建的唯一ID
    private  String id;

    //交易所订单id
    private String exchangeOrderId;

    /**交易所**/
    private Exchange exchang;

    private Currency currency;

    private OrderStatus orderStatus;

    private OrderSide orderSide;

    //创建时间
    private long time;

    //原始数量
    private double quantity;

    //下单价格
    private double price;

    private  double amount;

    private ContractType contractType;
    public String getId() {
        return id;
    }

    public Currency getCurrency() {
        return currency;
    }


    private String strategyId;

    public long getTime() {
        return time;
    }

    public OrderResponseInfo() {
    }

    public OrderResponseInfo(String id, String exchangeOrderId, Exchange exchang, Currency currency, OrderStatus orderStatus, OrderSide orderSide, long time, double quantity, double price, double amount) {
        this.id = id;
        this.exchangeOrderId = exchangeOrderId;
        this.exchang = exchang;
        this.currency = currency;
        this.orderStatus = orderStatus;
        this.orderSide = orderSide;
        this.time = time;
        this.price = price;
        this.quantity = quantity;
        this.amount = amount;
    }


    public String getExchangeOrderId() {
        return exchangeOrderId;
    }

    public Exchange getExchang() {
        return exchang;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public OrderSide getOrderSide() {
        return orderSide;
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

    public void setId(String id) {
        this.id = id;
    }

    public void setExchangeOrderId(String exchangeOrderId) {
        this.exchangeOrderId = exchangeOrderId;
    }

    public void setExchang(Exchange exchang) {
        this.exchang = exchang;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setOrderSide(OrderSide orderSide) {
        this.orderSide = orderSide;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public ContractType getContractType() {
        return contractType;
    }

    public void setContractType(ContractType contractType) {
        this.contractType = contractType;
    }


    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
    }

    @Override
    public String toString() {
        return "OrderResponseInfo{" +
                "id='" + id + '\'' +
                ", exchangeOrderId='" + exchangeOrderId + '\'' +
                ", exchang=" + exchang +
                ", currency=" + currency +
                ", orderStatus=" + orderStatus +
                ", orderSide=" + orderSide +
                ", time=" + time +
                ", origQty=" + quantity +
                ", price=" + price +
                ", amount=" + amount +
                '}';
    }
}
