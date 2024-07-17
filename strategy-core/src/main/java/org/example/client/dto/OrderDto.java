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
import org.example.model.currency.Currency;
import org.example.model.enums.ContractType;
import org.example.model.enums.OrderSide;
import org.example.model.enums.OrderType;

public class OrderDto {
    /**
     * 客户端虚拟id
     */
    private String virtualId;

    /**
     * 客户端虚拟id
     * virtualId 对应的订单 virtualIdB
     * 比如买单a  卖单b 卖了，那么virtualId= a，virtualIdB =b;
     * 比如买单a  卖单b和卖单c 卖了，那么
     * 数据库会存2个映射关系
     * virtualId= a，virtualIdB =b;
     * virtualId= a，virtualIdB =c;
     */
    private String virtualIdB;

    //触发下单的策略
    private String strategyId;

    /**交易所订单id**/
    private String exchangeOrderId;

    private Exchange exchange;

    //订单类型是market 还是 limit限价单
    private OrderType orderType;

    //订单方向
    private OrderSide orderSide;

    private Currency currency;

    //加个
    private double price;

    //币种数量
    private double quantity;

    //交易额
    private double amount;

    private ContractType contractType;

    private long time = System.currentTimeMillis();


    public OrderType getOrderType() {
        return orderType;
    }

    public Currency getCurrency() {
        return currency;
    }



    public OrderSide getOrderSide() {
        return orderSide;
    }

    public String getExchangeOrderId() {
        return exchangeOrderId;
    }

    public double getPrice() {
        return price;
    }

    public double getAmount() {
        return amount;
    }

    public String getVirtualId() {
        return virtualId;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public void setVirtualId(String virtualId) {
        this.virtualId = virtualId;
    }

    public void setExchangeOrderId(String exchangeOrderId) {
        this.exchangeOrderId = exchangeOrderId;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public void setOrderSide(OrderSide orderSide) {
        this.orderSide = orderSide;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getTime() {
        return time;
    }

    public String getVirtualIdB() {
        return virtualIdB;
    }

    public void setVirtualIdB(String virtualIdB) {
        this.virtualIdB = virtualIdB;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
    }

    public ContractType getContractType() {
        return contractType;
    }

    public void setContractType(ContractType contractType) {
        this.contractType = contractType;
    }
}
