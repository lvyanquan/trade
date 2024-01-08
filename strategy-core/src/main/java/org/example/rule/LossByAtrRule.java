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

package org.example.rule;

import org.example.enums.OrderSide;
import org.ta4j.core.Rule;
import org.ta4j.core.Trade;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

public class LossByAtrRule implements Rule {
    private Trade lastEntry;
    private Num price;

    private ATRIndicator atrIndicator;

    private ClosePriceIndicator closePriceIndicator;

    private OrderSide orderSide;

    public LossByAtrRule(ATRIndicator atrIndicator, ClosePriceIndicator closePriceIndicator, OrderSide orderSide) {
        this.atrIndicator = atrIndicator;
        this.closePriceIndicator = closePriceIndicator;
        this.orderSide = orderSide;
    }

    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
        if (tradingRecord.getCurrentPosition().isOpened()) {
            Trade lastEntry = tradingRecord.getLastEntry();
            if (this.lastEntry == null || this.lastEntry.equals(lastEntry)) {
                this.lastEntry = lastEntry;
                if (orderSide == OrderSide.BUY_LONG) {
                    this.price = closePriceIndicator.getValue(i).minus(atrIndicator.getValue(i));
                } else {
                    this.price = closePriceIndicator.getValue(i).plus(atrIndicator.getValue(i));
                }
            }
            Num currentPrice = closePriceIndicator.getValue(i);
            //多单
            if (orderSide == OrderSide.BUY_LONG && currentPrice.isLessThan(price)) {
                return true;
            }
            //空单
            return orderSide == OrderSide.SELL_SHORT && currentPrice.isGreaterThan(price);
        } else {
            this.lastEntry = null;
            this.price = null;
        }
        return false;
    }
}
