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

package org.example.core.order;

import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.client.impl.spot.Trade;
import org.example.core.handler.notify.DingTemplateNotify;
import org.example.core.strategy.JdbcTest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class OrderManager {
    //构建一个线程，每隔20s更新一次状态即可

    private Consumer<Order> listener;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private Trade tradeClient;

    public OrderManager(String apiKey, String secretKey) {
        this.tradeClient = new SpotClientImpl(apiKey, secretKey).createTrade();
        init();
        executor.scheduleWithFixedDelay(
                () -> {
                    List<Order> orders = selectNotTradeOrder();
                    for (Order order : orders) {
                        try {
                            updateOrderAndNotify(order);
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }
                },
                10,
                15,
                TimeUnit.SECONDS);
    }

    public void registerListener(Consumer<Order> listener) {
        this.listener = listener;
    }

    public void init() {
        for (Order order : selectAllWorkerOrder()) {
            updateOrderAndNotify(order);
        }
    }

    //从交易所获取订单数据
    //部分成交的 要先取消
    // 再查看所有的trader进行更新
    public void updateOrderAndNotify(Order order) {
        OrderResponseInfo orderResponseInfo = queryOrderFromExchange(order);
        if(OrderState.NEW.name().equalsIgnoreCase(orderResponseInfo.getStatus() ) ){
            return;
        }
        //如果是部分成交订单，先取消，再查询更新一次 取消失败还是取消成功 都在查询直接更新即可，更新失败，需要发送钉钉信息通知，正好看下部分成交不能取消还是取消之后，状态是取消还是部分成交
        if (orderResponseInfo.getStatus().equals(OrderState.PARTIALLY_FILLED.name())) {
            boolean success = cancelOrder(order);
            if (!success) {
                DingTemplateNotify.DEFAULT_NOTIFY.notify(orderResponseInfo.getClientOrderId() + " 取消失败\n, 触发取消原因：部分成交");
            }
            orderResponseInfo = queryOrderFromExchange(order);
        }
        //cancel等状态直接删除
        //卖单成立 则删除此订单即可
        //否则买单成立进行更新状态
        OrderState orderState = OrderState.orderState(orderResponseInfo.getStatus());
        if (orderState.isInvalid()) {
            JdbcTest.deleteOrder(orderResponseInfo.getClientOrderId());
        } else if (orderState == OrderState.PARTIALLY_FILLED || orderState == OrderState.FILLED) {
            if ("BUY".equalsIgnoreCase(orderResponseInfo.getSide())) {
                JdbcTest.updateOrder(orderResponseInfo.getClientOrderId(),
                        orderResponseInfo.getCummulativeQuoteQty() / orderResponseInfo.getExecutedQty(),
                        orderResponseInfo.getExecutedQty(),
                        OrderState.orderState(orderResponseInfo.getStatus()).statue);

            } else {
                JdbcTest.deleteOrder(orderResponseInfo.getClientOrderId());
            }
        }

        if (listener != null) {
            Order newOrder = new Order(orderResponseInfo.getClientOrderId(),
                    orderResponseInfo.getSymbol(),
                    order.getGridIndex(),
                    orderResponseInfo.getCummulativeQuoteQty() / orderResponseInfo.getExecutedQty(),
                    orderResponseInfo.getExecutedQty(),
                    OrderState.orderState(orderResponseInfo.getStatus()),
                    "BUY".equalsIgnoreCase(orderResponseInfo.getSide()) ? 0 : 2);
            listener.accept(newOrder);
        }
    }


    public List<Order> selectNotTradeOrder() {
        return JdbcTest.selectNotTradeOrder();
    }

    public boolean cancelOrder(Order order) {
       for (int i = 0; i < 4; i++) {
          try{
              LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
              parameters.put("symbol", order.getSymbol());
              parameters.put("origClientOrderId", order.getOrderId());
              String s = tradeClient.cancelOrder(parameters);
              System.out.println("cancel order response: " + s);
              return true;
          }catch (Exception e){
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException ex) {
                    //
                }
          }
       }
        return false;
    }

    public List<Order> selectAllWorkerOrder() {
        return JdbcTest.selectAllWorkerOrder();
    }

    public void insertNewOrder(Order order) {
        JdbcTest.insertAndDeleteBeforeOrder(order);
    }

    public OrderResponseInfo queryOrderFromExchange(Order order) {
        Exception e = null;
       for(int i = 0; i < 3; i++){
          try{
              LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
              parameters.put("symbol", order.getSymbol());
              parameters.put("origClientOrderId", order.getOrderId());
              String orderString = tradeClient.getOrder(parameters);
              return BinanceTradeJsonParse.parseOrder(orderString);
          }catch (Exception e1){
              e = e1;
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException ex) {
                    //
                }

          }
       }
        throw new RuntimeException(e);
    }

    public Trade getTradeClient() {
        return tradeClient;
    }
}
