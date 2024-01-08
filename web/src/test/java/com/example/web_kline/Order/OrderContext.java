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

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.example.web_kline.test2.TraderTemplate;
import jdk.nashorn.internal.runtime.ListAdapter;
import org.example.data.currency.Currency;
import org.example.util.JsonUtil;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.cost.FixedTransactionCostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderContext {
    private List<TradingRecord> orders = new ArrayList<>();
    private List<TradingRecord> openOrders = new ArrayList<>();

    private Trade.TradeType tradeType;
    private TradingRecord tradingRecord;
    private int orderSize;

    private double baseAmount;

    private int amountPrecision;

    public OrderContext(int orderSize, double baseAmount, Trade.TradeType tradeType, Currency currency, boolean init) {
        this.orderSize = orderSize;
        this.baseAmount = baseAmount;
        this.tradeType = tradeType;
        this.amountPrecision = currency.getQuantityPrecision();
        this.tradingRecord = new BaseTradingRecord(tradeType, new FixedTransactionCostModel(0.0002), new ZeroCostModel());
        if (init) {
            init(currency);
        }
    }

    public TradingRecord enter(int index, Num price) {
        if (openOrders.size() < orderSize) {
            TradingRecord tradingRecord1;
            if (openOrders.size() == 0) {
                BaseTradingRecord baseTradingRecord = new BaseTradingRecord(tradeType, new FixedTransactionCostModel(0.0002), new ZeroCostModel());
                openOrders.add(baseTradingRecord);
                baseTradingRecord.enter(index, price, price.numOf(BigDecimal.valueOf(baseAmount * (openOrders.size() + 1)).divide(BigDecimal.valueOf(price.doubleValue()),amountPrecision, RoundingMode.DOWN).setScale(amountPrecision, RoundingMode.DOWN).doubleValue()));
                return baseTradingRecord;
            } else {
                tradingRecord1 = openOrders.get(openOrders.size() - 1);
                if (index - tradingRecord1.getLastEntry().getIndex() > 10 && price.isLessThan(tradingRecord1.getLastEntry().getPricePerAsset().multipliedBy(tradingRecord1.getLastEntry().getPricePerAsset().numOf(0.99)))) {
                    BaseTradingRecord baseTradingRecord = new BaseTradingRecord(tradeType, new FixedTransactionCostModel(0.0002), new ZeroCostModel());
                    openOrders.add(baseTradingRecord);
                    baseTradingRecord.enter(index, price, price.numOf(BigDecimal.valueOf(baseAmount * (openOrders.size() + 1)).divide(BigDecimal.valueOf(price.doubleValue()),amountPrecision, RoundingMode.DOWN).setScale(amountPrecision, RoundingMode.DOWN).doubleValue()));
                    return baseTradingRecord;
                }
            }
        }
        return null;
    }

    public TradingRecord exit(int index, int num, Num price) {
        if (openOrders.size() > 0) {
            TradingRecord tradingRecord = openOrders.remove(num);
            tradingRecord.exit(index, price, tradingRecord.getLastEntry().getAmount());
            return tradingRecord;
        }
        return null;
    }

    public TradingRecord getTradingRecord() {
        return openOrders.size() == 0 ? tradingRecord : openOrders.get(openOrders.size() - 1);
    }

    public List<TradingRecord> exit(int index, Bar bar, Strategy strategy) {
        ArrayList<TradingRecord> tradingRecords = new ArrayList<>();
        if (openOrders.size() == 0) {
            strategy.shouldExit(index, tradingRecord);
            return tradingRecords;
        }
        Iterator<TradingRecord> iterator = openOrders.iterator();
        while (iterator.hasNext()) {
            TradingRecord next = iterator.next();
            boolean b = strategy.shouldExit(index, next);
            if (b) {
                next.exit(index, bar.getClosePrice(), next.getLastEntry().getAmount());
                tradingRecords.add(next);
                iterator.remove();
            }
        }
        return tradingRecords;
    }

    public void init(Currency currency) {
        String s = TraderTemplate.client.account().accountInformation(new LinkedHashMap<>());
        Map<String, Object> accountMap = JsonUtil.parseForMap(s);
        List<Map<String, Object>> positions = (List<Map<String, Object>>) accountMap.get("positions");
        for (Map<String, Object> position : positions) {

            Double positionAmt = Double.valueOf(position.get("positionAmt").toString());
            String symbol = position.get("symbol").toString();
            String positionSide = position.get("positionSide").toString();
            //做多的单子
            if (positionAmt != 0 && currency.symbol().equalsIgnoreCase(symbol) && positionSide.equalsIgnoreCase("LONG")) {
                Double price = Double.valueOf(position.get("entryPrice").toString());
                BaseTradingRecord baseTradingRecord = new BaseTradingRecord(tradeType, new FixedTransactionCostModel(0.0002), new ZeroCostModel());
                openOrders.add(baseTradingRecord);
                baseTradingRecord.enter(0, DoubleNum.valueOf(price), DoubleNum.valueOf(positionAmt));
            }
        }
    }


}
