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

import org.example.binance.BinanceCurrencyRegister;
import org.example.binance.factory.KlineFactory;
import org.example.binance.order.BinanceOrderManager;
import org.example.client.dto.OrderQueryDto;
import org.example.client.vo.OrderVo;
import org.example.enums.OrderStatus;
import org.example.model.currency.Btc;
import org.example.model.currency.Currency;
import org.example.model.enums.OrderSide;
import org.example.model.enums.Server;
import org.example.model.enums.ContractType;
import org.example.model.market.KlineModule;
import org.example.responsity.JdbcTest;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.num.DoubleNum;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class Test {
    protected static Bar lastBar = null;

    protected static BaseBarSeries series = new BaseBarSeries("mySeries", DoubleNum::valueOf);
    ;

    public static BinanceOrderManager binanceOrderManager = new BinanceOrderManager(JdbcTest.conn,new BinanceCurrencyRegister());

    private static List<String> orderClientId = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        Currency currency = Btc.BTC;
        double ammount = 500;


        BaseBarSeries series2 = new BaseBarSeries("mySeries", DoubleNum::valueOf);
        for (KlineModule i : KlineFactory.Create(Server.BINANCE, ContractType.SPOT).getHistoryKlineData(currency, "15m", System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000, System.currentTimeMillis())) {
            Bar newBar = BaseBar.builder(DoubleNum::valueOf, Double.class)
                    .timePeriod(Duration.ofMinutes(1))
                    .endTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(i.getEndTime()), ZoneId.systemDefault()))
                    .openPrice(i.getOpen())
                    .highPrice(i.getHigh())
                    .lowPrice(i.getLow())
                    .closePrice(i.getClose())
                    .volume(i.getQuantity())
                    .build();

            series2.addBar(newBar);
        }


        ATRIndicator atrIndicator = new ATRIndicator(series2, 10);
        double atr = atrIndicator.getValue(series2.getEndIndex()).doubleValue();
        Box box = new Box(currency, "1h", 59000, 68000, atr, ammount);

        Executor executor = new Executor(currency, box);

        new Thread(() -> {
            update(executor);
        }).start();

        KlineFactory.Create(Server.BINANCE, ContractType.SPOT).handlerStreamingKlineData(currency, "15m", t -> {

            Bar newBar = BaseBar.builder(DoubleNum::valueOf, Double.class)
                    .timePeriod(Duration.ofMinutes(1))
                    .endTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(t.getEndTime()), ZoneId.systemDefault()))
                    .openPrice(t.getOpen())
                    .highPrice(t.getHigh())
                    .lowPrice(t.getLow())
                    .closePrice(t.getClose())
                    .volume(t.getQuantity())
                    .build();

            if (lastBar == null) {
                try {
                    series.addBar(newBar, true);
                } catch (Exception e) {
                    System.out.println(lastBar.getEndTime() + "   " + newBar.getEndTime());
                    throw e;
                }
            } else if (lastBar.getEndTime().isEqual((newBar.getEndTime()))) {
                lastBar = newBar;
                try {
                    series.addBar(newBar, true);
                } catch (Exception e) {
                    System.out.println(lastBar.getEndTime() + "   " + newBar.getEndTime());
                    throw e;
                }

            } else {
                lastBar = newBar;
                try {
                    series.addBar(newBar, false);
                } catch (Exception e) {
                    System.out.println(lastBar.getEndTime() + "   " + newBar.getEndTime());
                    throw e;
                }
            }


            double currentPrice = newBar.getClosePrice().doubleValue();

            double highPrice = newBar.getHighPrice().doubleValue();

            box.setOrder(null, currentPrice);

            List<Integer> ids = executor.box.getBuyOrder(currentPrice);
            if (ids != null && !ids.isEmpty()) {
                // 判断下当前是否值得买入
                if (executor.buyNum >= 3) {
                    //1.连续买入3笔以上 价格必须为订单的触发价减去一个atr 且只买入1笔
                    if (currentPrice < executor.nextPrice) {
                        //购买即可
                        orderClientId.addAll(executor.buy(ids, executor.box, currentPrice));
                        executor.buyNum = 0;
                    }
                } else {
                    // 3. 买入价格尽量在bolling 和 ema均线附近
                    //看下订单触发价格最低的订单 和当前价格较低的下面 哪个 ema bolling均线价格更接近，（2个atr之间的就将价格定为这个） 否则就是当前价格买入
                    double price = executor.price(currentPrice, executor.initAtr);
                    if (currentPrice <= price) {
                        orderClientId.addAll(executor.buy(ids, executor.box, price));

                        executor.buyNum += 1;
                        if (executor.buyNum >= 3) {
                            executor.nextPrice = currentPrice - executor.initAtr;
                        }
                    }
                }
            }

            //如果卖出 buyNum = buyNum -2
            int sellOrder = executor.box.getSellOrder(currentPrice);
            if (sellOrder != -1) {
                orderClientId.add(executor.sell(sellOrder, highPrice));
            }
        });

        Thread.currentThread().join();
    }

    public static void update(Executor executor) {
        while (true) {
            try {
                ArrayList<String> remove = new ArrayList<>();
                for (String id : orderClientId) {
                    OrderQueryDto orderDto = new OrderQueryDto();
                    orderDto.setVirtualId(id);
                    OrderVo orderInfo = binanceOrderManager.getOrderInfoById(orderDto);
                    if (orderInfo.getOrderStatus() == OrderStatus.FILLED) {
                        remove.add(id);
                        if (orderInfo.getOrderSide() == OrderSide.BUY_LONG) {
                            executor.box.updateBuyOrder(id, orderInfo.getTradeAmount());
                        } else {
                            executor.box.updateSellOrder(id);
                        }
                    }
                }
                orderClientId.removeAll(remove);
                Thread.sleep(10000);
            } catch (Exception e) {
                //ignore
                e.printStackTrace();
            }
        }
    }
}
