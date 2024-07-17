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

package org.example.binance.account;

import com.binance.connector.client.impl.spot.Trade;
import org.example.binance.kline.BinanceClientFactory;
import org.example.binance.parse.BinanceJsonParse;
import org.example.client.dto.OrderQueryDto;
import org.example.client.dto.OrderResponseInfo;
import org.example.client.vo.TradeVo;
import org.example.enums.Exchange;
import org.example.enums.OrderStatus;
import org.example.model.currency.CurrencyRegister;
import org.example.model.position.PositionModel;
import org.example.model.position.PositionVo;
import org.example.order.OrderManager;
import org.example.order.OrderMonitor;
import org.example.responsity.JdbcTest;
import org.example.responsity.sql.PositionSql;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

public class BinanceOrderMonitor extends OrderMonitor {
    Trade trade;

    public BinanceOrderMonitor(OrderManager orderManager) {
        super(orderManager);
        trade = BinanceClientFactory.spotHttpClient().createTrade();
        //定时线程每隔10s查询一次完成即可
    }

    @Override
    public void updateOrderStatus() {
        //如果是非成交等状态 直接置为取消 如果是部分成交 就调用api置为取消看行不行 本地更新为部分成交即可
        OrderQueryDto orderQueryDto = new OrderQueryDto();
        ArrayList<OrderStatus> orderStatuses = new ArrayList<>();
        orderStatuses.add(OrderStatus.NEW);
        orderStatuses.add(OrderStatus.PENDING);
        orderStatuses.add(OrderStatus.PARTIALLY_FILLED);
        orderQueryDto.setOrderStatus(orderStatuses);
        orderQueryDto.setExchange(Exchange.BINANCE);
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        //寻找非取消 非成交状态的全部取消 如果是部分成交 取消掉剩余订单 并设置状态为部分成交即可
        for (OrderResponseInfo orderVo : orderManager.getOrderList(orderQueryDto)) {
            try {
                parameters.put("symbol", orderVo.getCurrency().symbol());
                parameters.put("origClientOrderId", orderVo.getId());
                String orderString = trade.getOrder(parameters);
                OrderResponseInfo orderResponseInfo = BinanceJsonParse.parseOrder(orderString);
                //更新订单信息
                JdbcTest.updateOrderStatus(orderResponseInfo);
                //如果是部分成交 取消掉掉剩余订单
                if (orderResponseInfo.getOrderStatus() == OrderStatus.PARTIALLY_FILLED) {
                    LinkedHashMap<String, Object> cancelOrder = new LinkedHashMap<>();
                    cancelOrder.put("symbol", orderVo.getCurrency().symbol());
                    cancelOrder.put("origClientOrderId", orderVo.getId());
                    trade.cancelOrder(cancelOrder);
                }
                //如果是成交状态或者部分成交 就查看交易单 进行detail的插入
                LinkedHashMap<String, Object> queryTradeParameter = new LinkedHashMap<>();
                queryTradeParameter.put("symbol", orderResponseInfo.getCurrency().symbol());
                queryTradeParameter.put("orderId", orderResponseInfo.getExchangeOrderId());
                queryTradeParameter.put("limit", 100);
                String s = trade.myTrades(queryTradeParameter);
                for (TradeVo tradeVo : BinanceJsonParse.parseTradeOrder(s)) {
                    tradeVo.setOrderId(orderVo.getId());
                    if (tradeVo.getExchangeOrderId().equalsIgnoreCase(orderResponseInfo.getExchangeOrderId())) {
                        try {
                            JdbcTest.insertOrderDetail(tradeVo);
                            //todo 更新下仓位信息 暂时是来一条个订单就计算一次 后期再看下如何优化下，正常情况下都是一次性成交，也就一个交易记录
                            //先查询下最新的仓位信息 然后更新，如果不存在就直接插入
                            PositionVo positionVo = PositionSql.queryPositionsByStrategyIdAndCurrency(orderResponseInfo.getStrategyId(),orderResponseInfo.getExchang(),orderResponseInfo.getOrderSide(),orderResponseInfo.getContractType(), orderResponseInfo.getCurrency(), JdbcTest.conn);

                            if (positionVo != null) {
                                double totalQuantity = positionVo.getQuantity() + tradeVo.getQuantity();
                                double totalPrice = (positionVo.getCost() * positionVo.getQuantity()) + (tradeVo.getAmount());
                                double cost = totalPrice / totalQuantity;
                                //更新下 cost 和 totalQuantity
                                PositionSql.update(new PositionModel(positionVo.getId(),orderResponseInfo.getCurrency().symbol(), orderResponseInfo.getStrategyId(), totalQuantity, cost, orderResponseInfo.getExchang().getId(), orderResponseInfo.getOrderSide().getType(), orderResponseInfo.getContractType().getType(), System.currentTimeMillis()),JdbcTest.conn);
                            } else {
                                PositionSql.insert(new PositionModel(orderResponseInfo.getCurrency().symbol(), orderResponseInfo.getStrategyId(), positionVo.getQuantity(), positionVo.getCost(), orderResponseInfo.getExchang().getId(), orderResponseInfo.getOrderSide().getType(), orderResponseInfo.getContractType().getType(), System.currentTimeMillis()),JdbcTest.conn);
                            }
                        } catch (Exception e) {
                            if (e.getMessage().toLowerCase(Locale.ENGLISH).contains("duplicate")) {
                                //ignore
                            } else {
                                throw e;
                            }
                        }
                    }
                }

                //如果是部分成交 就在查看下状态是否一致 如果不一致  就更新
                if (orderResponseInfo.getOrderStatus() == OrderStatus.PARTIALLY_FILLED) {
                    orderString = trade.getOrder(parameters);
                    orderResponseInfo = BinanceJsonParse.parseOrder(orderString);
                    if (orderResponseInfo.getOrderStatus() != OrderStatus.PARTIALLY_FILLED) {
                        JdbcTest.updateOrderStatus(orderResponseInfo);
                    }
                }
            } catch (Exception e) {
                if (e.getMessage().contains("Order does not exist")) {
                    OrderResponseInfo orderResponseInfo = new OrderResponseInfo();
                    orderResponseInfo.setOrderStatus(OrderStatus.CANCEL);
                    //更新订单信息为取消即可
                    JdbcTest.updateOrderCancel(orderVo.getId());
                }
            }
        }
    }
}
