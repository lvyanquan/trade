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

package org.example.binance.trade;

import org.example.binance.BinanceCurrencyRegister;
import org.example.binance.kline.BinanceClientFactory;
import org.example.binance.parse.BinanceJsonParse;
import org.example.binance.util.TradeUtil;
import org.example.client.dto.OrderDto;
import org.example.client.dto.OrderResponseInfo;
import org.example.client.vo.OrderVo;
import org.example.client.vo.TradeVo;
import org.example.enums.Exchange;
import org.example.enums.OrderStatus;
import org.example.model.currency.Currency;
import org.example.model.currency.CurrencyRegister;
import org.example.model.enums.OrderSide;
import org.example.model.enums.OrderType;
import org.example.responsity.JdbcTest;
import org.example.trade.TradeClient;
import org.example.util.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpotTrade implements TradeClient {
    private final Map<Currency, List<OrderDto>> orders = new HashMap<>();
    private Set<Currency> currencies = new HashSet<>();

    private List<OrderDto> orderIds = new ArrayList<>();

    public SpotTrade() {

//        initOrderIds();
    }


    //因为价格大多数时不是最新的 需要根据订单薄的信息 找到比较合适的下单时机
    @Override
    public synchronized void newOrderAsync(OrderDto orderDto) {
        if (orderDto.getOrderType() != OrderType.LIMIT) {
            throw new UnsupportedOperationException("just support OrderType is LIMIT");
        }
        if (orderDto.getOrderSide() == OrderSide.BUY_SHORT || orderDto.getOrderSide() == OrderSide.SELL_SHORT) {
            throw new UnsupportedOperationException("not  support OrderSide " + orderDto.getOrderSide());
        }

        if (orders.containsKey(orderDto.getCurrency())) {
            orders.get(orderDto.getCurrency()).add(orderDto);
        } else {
            List<OrderDto> queue = new ArrayList<>(20);
            queue.add(orderDto);
            orders.put(orderDto.getCurrency(), queue);
        }

        if (!currencies.contains(orderDto.getCurrency())) {
            doTrade(orderDto.getCurrency());
            currencies.add(orderDto.getCurrency());
        }
    }

    @Override
    public void cancelOrder(OrderDto orderDto) {
        //如果成交了 就不能取消，
        // 部分成交，取消挂单 设置状态为已完成
        //没有挂单 设置状态为取消
        //有挂单 就取消 设置状态为取消
        boolean remove = false;
        for (List<OrderDto> next : orders.values()) {

            for (int i = 0; i < next.size(); i++) {
                if (next.get(i).getVirtualId().equals(orderDto.getVirtualId())) {
                    next.remove(i);
                    remove = true;
                    break;
                }
            }
            if (remove) {
                break;
            }
        }
        if (!remove) {
            //todo 从挂单里取消即可 更新数据库状态
        }
    }


    @Override
    public void init() {

    }

    private void doTrade(Currency currency) {

        BinanceClientFactory.spotWsClientNokey()
                .partialDepthStream(currency.symbol(), 20, 1000, ((event) -> {
                    //解析当前最新价格
                    List<OrderDto> orderDtos = orders.get(currency);
                    //下单接口完善 加上我们自己定义的id哦 包括数据库的设计
                    // paimon 湖仓进行分析 以及notify 完善
                    // 各种指标分析的完善
                    //这个地方要进行排序的
                    if (!orderDtos.isEmpty()) {
                        Map parse = JsonUtil.parse(event, Map.class);
                        Iterator<OrderDto> iterator = orderDtos.iterator();
                        while (iterator.hasNext()) {
                            OrderDto order = iterator.next();
                            if (order.getOrderSide() == OrderSide.BUY_LONG) {
                                List data = (List) parse.get("bids");
                                List o = (List) data.get(5);
                                double v = Double.parseDouble(o.get(0).toString());
                                if (v < order.getPrice() || v < order.getPrice() * 1.001) {
                                    TradeUtil.orderLimitDuo(order.getCurrency(), v, order.getAmount(), order.getVirtualId(), BinanceClientFactory.spotHttpClient().createTrade());
                                    orderIds.add(order);
                                    iterator.remove();
                                }

                            } else if (order.getOrderSide() == OrderSide.SELL_LONG) {

                                List data = (List) parse.get("asks");
                                List o = (List) data.get(5);
                                double v = Double.parseDouble(o.get(0).toString());
                                if (v - order.getPrice() > 0 || v > order.getPrice() * 0.999) {
                                    TradeUtil.orderLimitPingDuo(order.getCurrency(), v, order.getQuantity(), order.getVirtualId(), BinanceClientFactory.spotHttpClient().createTrade());
                                    orderIds.add(order);
                                    iterator.remove();
                                }
                            }
                        }
                    }
                }));
    }




//    private void initOrderIds() {
//        //数据里找到所有的orderId
//        for (OrderVo orderVo : JdbcTest.getOnlyOrderWithNotTrade()) {
//            OrderDto orderDto = new OrderDto();
//            orderDto.setVirtualId(orderVo.getId());
//            orderDto.setExchange(Exchange.BINANCE);
//            orderDto.setOrderType(OrderType.LIMIT);
//            orderDto.setOrderSide(orderVo.getOrderSide());
//            orderDto.setCurrency(orderVo.getCurrency());
//            orderDto.setPrice(orderVo.getPrice());
//            orderDto.setAmount(orderVo.getAmount());
//            orderDto.setQuantity(orderVo.getOrigQty());
//            if (orderVo.getOrderStatus() == OrderStatus.NEW) {
//                newOrderAsync(orderDto);
//            }
//
//            orderIds.add(orderDto);
//        }
//
//    }
}
