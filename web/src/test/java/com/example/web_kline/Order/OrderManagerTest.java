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

package com.example.web_kline.Order;

import com.example.web_kline.util.BinanceOrderUtil;
import org.example.model.enums.OrderSide;
import org.example.util.JsonUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class OrderManagerTest {
    //获取所有订单 解析每笔订单盈利情况。进行统计

    public static void main(String[] args) {
        String data = "";
        List parse = JsonUtil.parse(data, List.class);
        //成交多单的成交总额
        BigDecimal cumQuoteCountBuyLong = BigDecimal.ZERO;
        //多单的成交总量
        BigDecimal cumQuoteCountBuyLongQty = BigDecimal.ZERO;

        //成交空单的数量
        BigDecimal cumQuoteCountSellShort = BigDecimal.ZERO;
        BigDecimal cumQuoteCountSellShortQty = BigDecimal.ZERO;

        //平多单的数量
        BigDecimal cumQuoteCountSellLong = BigDecimal.ZERO;
        BigDecimal cumQuoteCountSellLongQty = BigDecimal.ZERO;
        //平空单的数量
        BigDecimal cumQuoteCountBuyShort = BigDecimal.ZERO;
        BigDecimal cumQuoteCountBuyShortQty = BigDecimal.ZERO;

        //todo 过滤掉相同的symbol才行
        for (int i = 0; i < parse.size(); i++) {
            Map<String, Object> o = (Map<String, Object>) parse.get(i);
            //还有其他状态 比如只成交了一部分
            if (o.get("status").toString().equalsIgnoreCase("FILLED")) {
                //成交总额
                BigDecimal cumQuote = new BigDecimal(o.get("cumQuote").toString());
                OrderSide orderSide = BinanceOrderUtil.orderSide(o.get("side").toString(), o.get("positionSide").toString());

                BigDecimal avgPrice = new BigDecimal(o.get("avgPrice").toString());
                //成交数量
                BigDecimal executedQty = new BigDecimal(o.get("executedQty").toString());

                if (OrderSide.BUY_LONG == orderSide) {
                    cumQuoteCountBuyLong = cumQuoteCountBuyLong.add(cumQuote);
                    cumQuoteCountBuyLongQty = cumQuoteCountBuyLongQty.add(executedQty);
                } else if (OrderSide.SELL_SHORT == orderSide) {
                    cumQuoteCountSellShort = cumQuoteCountSellShort.add(cumQuote);
                    cumQuoteCountSellShortQty = cumQuoteCountSellShortQty.add(executedQty);
                } else if (OrderSide.SELL_LONG == orderSide) {
                    cumQuoteCountSellLong = cumQuoteCountSellLong.add(cumQuote);
                    cumQuoteCountSellLongQty = cumQuoteCountSellLongQty.add(executedQty);
                } else if (OrderSide.BUY_SHORT == orderSide) {
                    cumQuoteCountBuyShort = cumQuoteCountBuyShort.add(cumQuote);
                    cumQuoteCountBuyShortQty = cumQuoteCountBuyShortQty.add(executedQty);
                }
            }

            //（平多单的平均价格 - 开多单的平均价格）* 成交额 = 盈利钱
            BigDecimal subtract1 = (cumQuoteCountSellLong.divide(cumQuoteCountSellLongQty)).subtract((cumQuoteCountBuyLong.divide(cumQuoteCountBuyLongQty)));
            BigDecimal multiply = subtract1.multiply(cumQuoteCountSellLongQty);
            //多单持有的仓位 = 开单数量总和-平单的总和
            BigDecimal subtract3 = cumQuoteCountBuyLongQty.subtract(cumQuoteCountSellLongQty);

            //-----空单
            BigDecimal subtract4 = (cumQuoteCountBuyShort.divide(cumQuoteCountBuyShortQty)).subtract((cumQuoteCountSellShort.divide(cumQuoteCountSellShortQty)));
            BigDecimal multiply5 = subtract4.multiply(cumQuoteCountBuyShortQty);
            //空单持有的仓位 = 开单数量总和-平单的总和
            BigDecimal subtract6 = cumQuoteCountSellShortQty.subtract(cumQuoteCountBuyShortQty);
            //减去手续费(挂单和吃单的手续费是不一样的)


            //计算每一笔订单的亏损情况  应该是一笔开单  对应多个平单计算

            //todo binance有一个trade接口，查看所有的成交信息，根据这个来就行

            //查看最近7天有没有订单对应的成交单 内存里置为结束状态
            //{
            //        "symbol":"BTCUSDT",
            //        "id":4197667927,
            //        "orderId":201285704119,
            //        "side":"SELL",
            //        "price":"29882.20",
            //        "qty":"0.040",
            //        "realizedPnl":"0",
            //        "marginAsset":"USDT",
            //        "quoteQty":"1195.28800",
            //        "commission":"0.59764399",
            //        "commissionAsset":"USDT",
            //        "time":1698000902634,
            //        "positionSide":"SHORT",
            //        "maker":false,
            //        "buyer":false
            //    }
            List trades = JsonUtil.parse(data, List.class);
            for (int i1 = 0; i1 < parse.size(); i1++) {
                Map<String, Object> trade = (Map<String, Object>) parse.get(i1);

                //id
                String id = trade.get("id").toString();
                //订单id
                String orderId = trade.get("orderId").toString();
                //symbol
                String symbol = trade.get("symbol").toString();
                //成交量
                String qty = trade.get("qty").toString();
                //成交价格
                String price = trade.get("price").toString();
                //成交总额
                String quoteQty = trade.get("quoteQty").toString();
                //盈亏
                String realizedPnl = trade.get("realizedPnl").toString();
                //手续费
                String commission = trade.get("commission").toString();
            }
        }
    }


    //判断下单是否成交


    //获取account信息 解析当前持仓情况
}
