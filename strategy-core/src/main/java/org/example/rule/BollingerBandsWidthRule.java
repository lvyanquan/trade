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
import org.example.indicators.BollingWidthDescriptiveIndicator;
import org.example.indicators.BollingerBandsWidthIndicator;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

public class BollingerBandsWidthRule implements Rule {
    private BollingerBandsUpperIndicator up;
    private BollingerBandsLowerIndicator low;

    private BollingerBandsWidthIndicator widthIndicator;
    private BollingWidthDescriptiveIndicator widthDescriptiveIndicator;

    private int increasingBandwidthCount = 0;
    private int declineCount = 0;
    private int increasingBandwidthCountLimit;
    private double thresholdPercentage;

    private ClosePriceIndicator closePriceIndicator;

    private OrderSide orderSide;

    public BollingerBandsWidthRule(BollingerBandsUpperIndicator up, BollingerBandsMiddleIndicator mid, BollingerBandsLowerIndicator low, ClosePriceIndicator closePriceIndicator,
                                   int increasingBandwidthCountLimit, double thresholdPercentage, OrderSide orderSide) {
        this.up = up;
        this.low = low;
        this.widthIndicator = new BollingerBandsWidthIndicator(up, mid, low);
        this.widthDescriptiveIndicator = new BollingWidthDescriptiveIndicator(widthIndicator, 100, 90);
        this.increasingBandwidthCountLimit = increasingBandwidthCountLimit;
        this.thresholdPercentage = thresholdPercentage;
        this.closePriceIndicator = closePriceIndicator;
        this.orderSide = orderSide;
    }

    @Override
    public boolean isSatisfied(int i, TradingRecord tradingRecord) {
        if (i < 1) {
            return false;
        }
        Num currentValue = widthIndicator.getValue(i);
        if (currentValue.isGreaterThan(widthIndicator.getValue(i - 1))) {
            increasingBandwidthCount++;
        } else if (increasingBandwidthCount > 0) {
            Num minus = widthIndicator.getValue(i - 1).minus(currentValue);
            Num minus2 = widthIndicator.getValue(i - 1).minus(widthIndicator.getValue(i - 2));
            if (minus.isLessThan(minus2)) {
                declineCount++;
            }
            if (declineCount > 1) {
                increasingBandwidthCount = 0;
                declineCount = 0;
            }
        } else {
            increasingBandwidthCount = 0;
            declineCount = 0;
        }
        if (orderSide == OrderSide.BUY_LONG) {
            return increasingBandwidthCount >= increasingBandwidthCountLimit
                    && closePriceIndicator.getValue(i).isLessThanOrEqual(low.getValue(i).multipliedBy(closePriceIndicator.getBarSeries().numOf(1 + thresholdPercentage)))
                    && currentValue.isGreaterThanOrEqual(widthDescriptiveIndicator.getValue(i));
        }else if (orderSide == OrderSide.SELL_SHORT){
            return increasingBandwidthCount >= increasingBandwidthCountLimit
                    && closePriceIndicator.getValue(i).isGreaterThan(up.getValue(i).multipliedBy(closePriceIndicator.getBarSeries().numOf(1 - thresholdPercentage)))
                    && currentValue.isGreaterThanOrEqual(widthDescriptiveIndicator.getValue(i));
        }
        return false;
    }
}
