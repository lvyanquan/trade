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

package org.example.binance.parse;

import org.example.client.dto.OrderResponseInfo;
import org.example.client.vo.TradeVo;
import org.example.enums.Exchange;
import org.example.enums.OrderStatus;
import org.example.model.currency.CurrencyRegister;
import org.example.model.enums.OrderSide;
import org.example.model.market.KlineModule;
import org.example.util.JsonUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BinanceJsonParse {
    public static KlineModule parseStreamKline(String event) {
        Map map = JsonUtil.parseForMap(event);
        Map<String, Object> data = (Map<String, Object>) map.get("k");
        long closeTime = Long.parseLong(data.get("T").toString());
        long openTime = Long.parseLong(data.get("t").toString());
        Double o = Double.valueOf(data.get("o").toString());
        Double c = Double.valueOf(data.get("c").toString());
        Double h = Double.valueOf(data.get("h").toString());
        Double l = Double.valueOf(data.get("l").toString());
        Double quality = Double.valueOf(data.get("v").toString());
        Double amount = Double.valueOf(data.get("q").toString());
        Double buyQuality = Double.valueOf(data.get("V").toString());
        Double buyAmount = Double.valueOf(data.get("Q").toString());
        return new KlineModule(openTime, closeTime, o, h, l, c, quality,amount,buyQuality,buyAmount);
    }


    public static List<KlineModule> parseHttpKline(String result) {
        List<List> lists = JsonUtil.parse(result, List.class);
        LinkedList<KlineModule> klineModules = new LinkedList<>();

        lists.forEach(i -> {
            List<Object> data = (List<Object>) i;
            long openTime = Long.parseLong(data.get(0).toString());
            long closeTime = Long.parseLong(data.get(6).toString());

            double o = Double.parseDouble(data.get(1).toString());
            double h = Double.parseDouble(data.get(2).toString());
            double l = Double.parseDouble(data.get(3).toString());
            double c = Double.parseDouble(data.get(4).toString());
            double quantity = Double.parseDouble(data.get(5).toString());
            double amount = Double.parseDouble(data.get(7).toString());
            double takerBuyQuantity  = Double.parseDouble(data.get(9).toString());
            double takerBuyAmount = Double.parseDouble(data.get(10).toString());

            klineModules.add(new KlineModule(openTime, closeTime, o, h, l, c, quantity,amount,takerBuyQuantity,takerBuyAmount));
        });

        return klineModules;
    }

//    {
//        "symbol": "BTCUSDT",
//            "orderId": 9325354954,
//            "orderListId": -1,
//            "clientOrderId": "ios_dda48abceadf4172b57a553a4456e01b",
//            "price": "43165.00000000",
//            "origQty": "0.02541000",
//            "executedQty": "0.02541000",
//            "cummulativeQuoteQty": "1096.82265000",
//            "status": "FILLED",
//            "timeInForce": "GTC",
//            "type": "LIMIT",
//            "side": "BUY",
//            "stopPrice": "0.00000000",
//            "icebergQty": "0.00000000",
//            "time": 1644417223271,
//            "updateTime": 1644539036450,
//            "isWorking": true,
//            "workingTime": 1644417223271,
//            "origQuoteOrderQty": "0.00000000",
//            "selfTradePreventionMode": "NONE"
//    }

    public static OrderResponseInfo parseOrder(String s) {
        Map<String, Object> orderMap = JsonUtil.parseForMap(s);
        String id = orderMap.get("clientOrderId").toString();
        String orderId = orderMap.get("orderId").toString();
        String crrency = orderMap.get("symbol").toString();
        String status = orderMap.get("status").toString();
        String conventOrderSide = orderMap.get("side").toString();
        long time = Long.parseLong(orderMap.get("time").toString());
        // 累计交易的金额 实际金额
        double cummulativeQuoteQty = Double.parseDouble(orderMap.get("cummulativeQuoteQty").toString());
        // 交易的仓位数量
        double executedQty = Double.parseDouble(orderMap.get("executedQty").toString());
        //原始的仓位数量
        double origQty = Double.parseDouble(orderMap.get("origQty").toString());
        double price = Double.parseDouble(orderMap.get("price").toString());


        return new OrderResponseInfo(id, orderId, Exchange.BINANCE, CurrencyRegister.getCurrency(crrency).get(),
                cpnventStatus(status), conventOrderSide(conventOrderSide), time, executedQty, origQty,price);
    }

    /**
     * [
     *   {
     *     "symbol": "BNBBTC", // 交易对
     *     "id": 28457, // trade ID
     *     "orderId": 100234, // 订单ID
     *     "orderListId": -1, // OCO订单的ID，不然就是-1
     *     "price": "4.00000100", // 成交价格
     *     "qty": "12.00000000", // 成交量
     *     "quoteQty": "48.000012", // 成交金额
     *     "commission": "10.10000000", // 交易费金额
     *     "commissionAsset": "BNB", // 交易费资产类型
     *     "time": 1499865549590, // 交易时间
     *     "isBuyer": true, // 是否是买家
     *     "isMaker": false, // 是否是挂单方
     *     "isBestMatch": true
     *   }
     * ]
     * @param s
     * @return
     */
    public static List<TradeVo> parseTradeOrder(String s) {
        List<Map<String, Object>> tradeOrdes = JsonUtil.parse(s, List.class);
        ArrayList<TradeVo> tradeVos = new ArrayList<>();

        for (Map<String, Object> tradeOrde : tradeOrdes) {
            String tradeId = tradeOrde.get("id").toString();
            String exchangeOrderId = tradeOrde.get("orderId").toString();
            double price = Double.parseDouble(tradeOrde.get("price").toString());
            double qty = Double.parseDouble(tradeOrde.get("qty").toString());
            double quoteQty = Double.parseDouble(tradeOrde.get("quoteQty").toString());
            double commission = Double.parseDouble(tradeOrde.get("commission").toString());
            long time = Long.parseLong(tradeOrde.get("time").toString());
            tradeVos.add(new TradeVo(tradeId,exchangeOrderId,price,qty,quoteQty,commission,time));
        }
        return tradeVos;
    }

    public static OrderStatus cpnventStatus(String status) {
        if (status.equalsIgnoreCase("NEW")) {
            return OrderStatus.NEW;
        } else if (status.equalsIgnoreCase("PARTIALLY_FILLED")) {
            return OrderStatus.PARTIALLY_FILLED;
        } else if (status.equalsIgnoreCase("FILLED")) {
            return OrderStatus.FILLED;
        } else if (status.equalsIgnoreCase("CANCELED") || status.equalsIgnoreCase("PENDING_CANCEL") || status.equalsIgnoreCase("REJECTED") || status.equalsIgnoreCase("EXPIRED")) {
            return OrderStatus.CANCEL;
        }
        throw new RuntimeException("not parse status: " + status);
    }

    public static OrderSide conventOrderSide(String side) {
        if (side.equalsIgnoreCase("BUY")) {
            return OrderSide.BUY_LONG;
        } else {
            return OrderSide.SELL_LONG;
        }
    }
}
