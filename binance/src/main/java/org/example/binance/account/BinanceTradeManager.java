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

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.binance.connector.futures.client.impl.UMWebsocketClientImpl;
import org.example.account.TradeManager;
import org.example.account.TradeOptions;
import org.example.client.dto.OrderDto;
import org.example.model.enums.OrderSide;
import org.example.model.enums.OrderType;
import org.example.model.market.KlineModule;
import org.example.model.currency.Currency;
import org.example.util.JsonUtil;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BinanceTradeManager extends TradeManager {

    //KlineUtil.API_KEY, KlineUtil.SECRET_KEY, KlineUtil.UM_BASE_URL
    private UMFuturesClientImpl client = new UMFuturesClientImpl("","","");

    private final UMWebsocketClientImpl websocketClient = new UMWebsocketClientImpl();
    private final Map<String, Integer> suscripe = new ConcurrentHashMap<>(12);

    private final Map<String, BigDecimal> HANDLING_CHARGE = new ConcurrentHashMap<>();

    private final Map<String, LinkedList<OrderInfo>> tradingOrders = new ConcurrentHashMap<>();

    @Override
    public void init() {

    }


    @Override
    public BigDecimal handlingCharge(Currency currency, OrderType orderType) {

        if (HANDLING_CHARGE.containsKey(currency.symbol() + orderType.name())) {
            return HANDLING_CHARGE.get(currency.symbol() + orderType.name());
        }

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", currency.symbol());
        String result = client.account().getCommissionRate(parameters);
        //{"symbol":"BTCUSDT","":"0.000200","takerCommissionRate":"0.000500"}
        Map<String, Object> stringObjectMap = JsonUtil.parseForMap(result);
        //限价单
        BigDecimal makerCommissionRate = new BigDecimal(stringObjectMap.get("makerCommissionRate").toString());
        //市价单
        BigDecimal takerCommissionRate = new BigDecimal(stringObjectMap.get("takerCommissionRate").toString());
        HANDLING_CHARGE.put(currency.symbol() + OrderType.LIMIT.name(), makerCommissionRate);
        HANDLING_CHARGE.put(currency.symbol() + OrderType.TAKE.name(), takerCommissionRate);
        if (orderType == OrderType.LIMIT) {
            return makerCommissionRate;
        }
        return takerCommissionRate;
    }

    @Override
    public CompletableFuture<String> trade(OrderDto orderDto, KlineModule position, TradeOptions options) {
        CompletableFuture<String> tradeId = new CompletableFuture<String>();
        OrderInfo orderInfo = new OrderInfo(orderDto, options, position, tradeId);
        if (tradingOrders.containsKey(orderDto.getCurrency().symbol())) {
            tradingOrders.get(orderDto.getCurrency().symbol()).add(orderInfo);
        } else {
            tradingOrders.put(orderDto.getCurrency().symbol(), new LinkedList<>());
            tradingOrders.get(orderDto.getCurrency().symbol()).add(orderInfo);
        }

        if (suscripe.containsKey(orderDto.getCurrency().symbol())) {
            websocketClient.miniTickerStream(orderDto.getCurrency().symbol(), event -> {
                if (tradingOrders.containsKey(orderDto.getCurrency().symbol()) && !tradingOrders.get(orderDto.getCurrency().symbol()).isEmpty()) {

                    Map<String, Object> eventInfo = JsonUtil.parse(event, Map.class);
                    long time = Long.parseLong(eventInfo.get("E").toString());
                    BigDecimal priceC = new BigDecimal(eventInfo.get("c").toString());

                    LinkedList<OrderInfo> orderInfos = tradingOrders.get((orderDto.getCurrency().symbol()));
                    Iterator<OrderInfo> iterator = orderInfos.iterator();
                    while (iterator.hasNext()) {
                        OrderInfo orderIns = iterator.next();
                        if (time < orderIns.getTradeOptions().getActiveTime()) {
                            if (priceC.doubleValue() -orderIns.getOrder().getPrice() - orderInfo.getTradeOptions().getPriceRangerUp().doubleValue() < 0
                                    && priceC.doubleValue() - orderIns.getOrder().getPrice() - orderInfo.getTradeOptions().getPriceRangerDown().doubleValue() > 0) {
                                //开多单
                                double price = priceC.doubleValue() - 2;
                                LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
                                parameters.put("symbol", orderIns.getOrder().getCurrency().symbol());
                                parameters.put("type", orderIns.getOrder().getOrderType() == OrderType.LIMIT ? "LIMIT" : "MARKET");

                                parameters.put("quantity", orderIns.getOrder().getAmount() /priceC.doubleValue());
                                if (orderIns.getOrder().getOrderType() == OrderType.LIMIT) {
                                    parameters.put("price", price);
                                    parameters.put("timeInForce", "GTC");
                                }
                                if (orderIns.getOrder().getOrderSide() == OrderSide.BUY_LONG) {
                                    parameters.put("side", "BUY");
                                    parameters.put("positionSide", "LONG");
                                } else if (orderIns.getOrder().getOrderSide() == OrderSide.SELL_SHORT) {
                                    parameters.put("side", "SELL");
                                    parameters.put("positionSide", "SHORT");
                                } else if (orderIns.getOrder().getOrderSide() == OrderSide.SELL_LONG) {
                                    parameters.put("side", "SELL");
                                    parameters.put("positionSide", "LONG");
                                } else if (orderIns.getOrder().getOrderSide() == OrderSide.BUY_SHORT) {
                                    parameters.put("side", "BUY");
                                    parameters.put("positionSide", "SHORT");
                                }
                                String result = client.account().newOrder(parameters);
                                iterator.remove();
                                orderIns.getTradeId().complete(result);
                            }
                        } else {
                            iterator.remove();
                            orderIns.getTradeId().complete(null);
                        }
                    }
                }
            });
        }
        return tradeId;
    }

    @Override
    public boolean cancelTradeByVirtualId(String virtualId) {
        boolean removed = false;
        for (LinkedList<OrderInfo> orderInfos : tradingOrders.values()) {
            Iterator<OrderInfo> iterator1 = orderInfos.iterator();
            while (iterator1.hasNext()) {
                OrderInfo next = iterator1.next();
                if (next.orderDto.getVirtualId().equals(virtualId)) {
                    iterator1.remove();
                    removed = true;
                    break;
                }
            }
            if (removed) {
                break;
            }
        }
        return false;
    }


    public class OrderInfo {
        private OrderDto orderDto;
        private TradeOptions tradeOptions;
        private KlineModule klineModule;

        private CompletableFuture<String> tradeId;

        public OrderInfo(OrderDto orderDto, TradeOptions tradeOptions, KlineModule klineModule, CompletableFuture<String> tradeId) {
            this.orderDto = orderDto;
            this.tradeOptions = tradeOptions;
            this.klineModule = klineModule;
            this.tradeId = tradeId;
        }

        public OrderDto getOrder() {
            return orderDto;
        }

        public TradeOptions getTradeOptions() {
            return tradeOptions;
        }

        public KlineModule getPriceBean() {
            return klineModule;
        }

        public CompletableFuture<String> getTradeId() {
            return tradeId;
        }
    }
}



