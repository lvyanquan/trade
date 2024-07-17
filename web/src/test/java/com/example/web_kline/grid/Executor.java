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
import com.example.web_kline.grid.order.VirtualOrder;
import org.example.binance.trade.SpotTrade;
import org.example.client.dto.OrderDto;
import org.example.enums.Exchange;
import org.example.model.currency.Currency;
import org.example.model.currency.CurrencyRegister;
import org.example.model.enums.OrderSide;
import org.example.model.enums.OrderType;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Executor {

    Currency currency;

    //连续买入次数
    public int buyNum;
    public double nextPrice;
    public double initAtr;

    public Indicator<Num> ema7;
    public Indicator<Num> ema30;
    public Indicator<Num> ema60;

    public Indicator<Num> bollingUp;
    public Indicator<Num> bollingMid;
    public Indicator<Num> bollingDown;

    public Box box;

    public SpotTrade spotTrade;

    String virtualIdPrefix = "grrid_";
    long index = 0;


    public Executor(Currency currency, Box box) {
        this.currency = currency;
        this.box = box;
        this.spotTrade = new SpotTrade();
        spotTrade.init();
    }

    public List<String> buy(List<Integer> ids, Box box, double price) {
        ArrayList<String> orderIds = new ArrayList<>();
        //去最低的一个价格即可
        double lowPrice = box.getOrder(ids.get(0)).getBuyPrice();
        for (Integer id : ids) {
            box.updateBuying(id);
            lowPrice = Math.min(lowPrice, box.getOrder(ids.get(0)).getBuyPrice());
        }
        lowPrice = Math.min(price, lowPrice);
        for (Integer id : ids) {
            String orderId= virtualIdPrefix + "buu" + index++;
            Order order = box.getOrder(id);
            OrderDto orderDto = new OrderDto();
            orderDto.setVirtualId(orderId);
            orderDto.setExchange(Exchange.BINANCE);
            orderDto.setOrderType(OrderType.LIMIT);
            orderDto.setOrderSide(OrderSide.BUY_LONG);
            orderDto.setCurrency(currency);
            orderDto.setPrice(lowPrice);
            orderDto.setAmount(order.getAmount());
            spotTrade.newOrderAsync(orderDto);
            if(order instanceof VirtualOrder){
                ((VirtualOrder)order).setVirtualId(orderId);
            }
            orderIds.add(orderId);
        }

        return orderIds;
    }

    public String sell(int id, double price) {
        String orderid = virtualIdPrefix + "sell" + index++;
        Order order = box.getOrder(id);
        box.updateSellIngOrder(id);
        double max = Math.max(price, order.getSellPrice());
        OrderDto orderDto = new OrderDto();
        orderDto.setVirtualId(orderid);
        orderDto.setExchange(Exchange.BINANCE);
        orderDto.setOrderType(OrderType.LIMIT);
        orderDto.setOrderSide(OrderSide.SELL_LONG);
        orderDto.setCurrency(currency);
        orderDto.setPrice(max);
        orderDto.setAmount(order.getActualAmount());
        if(order instanceof VirtualOrder){
            ((VirtualOrder)order).setVirtualId(orderid);
        }
        spotTrade.newOrderAsync(orderDto);

        return orderid;
    }


    //prices

    public double price(double price, double atr) {
        double ema7Value = ema7.getValue(ema7.getBarSeries().getEndIndex()).doubleValue();
        double ema30Value = ema30.getValue(ema30.getBarSeries().getEndIndex()).doubleValue();
        double ema60Value = ema60.getValue(ema60.getBarSeries().getEndIndex()).doubleValue();
        double bollingUpValue = bollingUp.getValue(bollingUp.getBarSeries().getEndIndex()).doubleValue();
        double bollingMidValue = bollingMid.getValue(bollingMid.getBarSeries().getEndIndex()).doubleValue();
        double bollingDownValue = bollingDown.getValue(bollingDown.getBarSeries().getEndIndex()).doubleValue();

        ArrayList<Double> prices = new ArrayList<>();
        prices.add(ema7Value);
        prices.add(ema30Value);
        prices.add(ema60Value);
        prices.add(bollingUpValue);
        prices.add(bollingMidValue);
        prices.add(bollingDownValue);
        prices.sort((k, k2) -> Double.valueOf(k2 - k + "").intValue());

        for (double priceBack : prices) {
            if (price > priceBack && price - atr * 1.5 < priceBack) {
                return priceBack;
            }
        }
        return price;
    }


}
