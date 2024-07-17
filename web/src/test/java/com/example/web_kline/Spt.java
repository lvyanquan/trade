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

package com.example.web_kline;

import org.example.Position.PositionManagementStrategy;
import org.example.binance.BinanceCurrencyRegister;
import org.example.binance.order.BinanceOrderManager;
import org.example.binance.trade.SpotTrade;
import org.example.client.dto.OrderDto;
import org.example.enums.Exchange;
import org.example.model.currency.Currency;
import org.example.model.enums.ContractType;
import org.example.model.enums.OrderSide;
import org.example.model.enums.OrderType;
import org.example.model.market.KlineModule;
import org.example.notify.DinDinNotify;
import org.example.responsity.JdbcTest;
import org.example.util.IdGenerator;

public class Spt {

    private String strategyId;

    protected Currency currency;

    protected BinanceOrderManager orderManager;
    protected PositionManagementStrategy positionManagementStrategy;

    protected SpotTrade spotTrade;

    protected ContractType contractType;

    protected DinDinNotify dinDinNotify;

    public Spt(String strategyId, Currency currency, ContractType contractType, PositionManagementStrategy positionManagementStrategy) {
        this.contractType = contractType;
        this.strategyId = strategyId;
        this.currency = currency;
        this.orderManager = new BinanceOrderManager(JdbcTest.conn, new BinanceCurrencyRegister());
        this.spotTrade = new SpotTrade();
        this.positionManagementStrategy = positionManagementStrategy;
        this.dinDinNotify = new DinDinNotify("5a95ba6678842306bc1475c9404e7e95c31bff5cc77ba6197c2912d047aaa2a1", "SEC5cafd9e9fc716cf4d57db942a8d0aa0848d0cc4ba8ff14dbfcce3b35348d09c3");
    }



    public void tradeCallback(OrderDto orderDto) {
        if (orderDto.getOrderSide() == OrderSide.BUY_LONG) {
            String format = String.format("atr机器人下单信息：\n" +
                            "币种：%s\n" +
                            "买入价格：%s\n" +
                            "预计止盈价格：%s\n" +
                            "买入数量：%s\n" +
                            "交易量： %s",
                    orderDto.getCurrency().symbol(), orderDto.getPrice(),
                    orderDto.getPrice(),
                    orderDto.getQuantity(),
                    orderDto.getAmount());
            dinDinNotify.send(format);

        } else {
            String format = String.format("atr机器人下单信息：\n" +
                            "币种：%s\n" +
                            "卖出价格：%s\n" +
                            "卖出数量：%s\n" +
                            "交易量： %s",
                    orderDto.getCurrency().symbol(), orderDto.getPrice(),
                    orderDto.getPrice(),
                    orderDto.getQuantity(),
                    orderDto.getAmount()
            );
            dinDinNotify.send(format);
        }
    }


    public void buyOrder(double price) {
        double v = positionManagementStrategy.getNextAvaliableBuyAmount(currency);
        if (v > 0) {
            String virtualId = IdGenerator.generator(strategyId);

            OrderDto orderDto = new OrderDto();
            orderDto.setVirtualId(virtualId);
            orderDto.setExchange(Exchange.BINANCE);
            orderDto.setOrderType(OrderType.LIMIT);
            orderDto.setOrderSide(OrderSide.BUY_LONG);
            orderDto.setCurrency(currency);
            orderDto.setPrice(price);
            orderDto.setStrategyId(strategyId);
            orderDto.setAmount(v);
            orderDto.setContractType(contractType);
            orderDto.setQuantity(currency.quanlityConvent(orderDto.getAmount() / orderDto.getPrice()));
            //向account里申请仓位
            // 先插入数据库
            orderManager.createOrderVo(orderDto);

            //交给trade进行下单
            spotTrade.newOrderAsync(orderDto);

            positionManagementStrategy.orderAmount(currency,v);

            tradeCallback(orderDto);

        }
    }


    public void sellOrder(double price) {
        double sell = positionManagementStrategy.getNextAvaliableSellQuantity(currency);
        if (sell > 0) {
            String virtualId = IdGenerator.generator(strategyId);

            OrderDto orderDto = new OrderDto();
            orderDto.setVirtualId(virtualId);
            orderDto.setExchange(Exchange.BINANCE);
            orderDto.setOrderType(OrderType.LIMIT);
            orderDto.setOrderSide(OrderSide.SELL_LONG);
            orderDto.setCurrency(currency);
            orderDto.setStrategyId(strategyId);
            orderDto.setPrice(price);
            orderDto.setAmount(currency.quanlityConvent(price * sell));
            orderDto.setContractType(contractType);
            orderDto.setQuantity(sell);
            //向account里申请仓位
            // 先插入数据库
            orderManager.createOrderVo(orderDto);
            //交给trade进行下单
            spotTrade.newOrderAsync(orderDto);

            positionManagementStrategy.releaseAmount(currency,orderDto.getAmount());

            tradeCallback(orderDto);
        }
    }

}
