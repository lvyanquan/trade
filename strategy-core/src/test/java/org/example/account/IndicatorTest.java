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

package org.example.account;

import org.example.data.PriceBean;
import org.example.util.KlineUtil;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DoubleNum;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class IndicatorTest {
    public static void main(String[] args) {

        BaseBarSeries series = new BaseBarSeries("mySeries", DoubleNum::valueOf);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator eemaIndicator25 = new EMAIndicator(closePrice, 25);
        EMAIndicator emaIndicator7 = new EMAIndicator(closePrice, 7);
        EMAIndicator emaIndicator99 = new EMAIndicator(closePrice, 99);


        int i = 0;
         long e = System.currentTimeMillis();
         long s = e - (5 * 24 * 60 * 60 * 1000);
        for (PriceBean priceBean : KlineUtil.getBar2("BTCUSDT", "15m", s, e)) {
            Bar newBar = BaseBar.builder(DoubleNum::valueOf, Double.class)
                    .timePeriod(Duration.ofMinutes(1))
                    .endTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(priceBean.getEndTime()), ZoneId.systemDefault()))
                    .openPrice(priceBean.getOpen().doubleValue())
                    .highPrice(priceBean.getHigh().doubleValue())
                    .lowPrice(priceBean.getLow().doubleValue())
                    .closePrice(priceBean.getClose().doubleValue())
                    .volume(priceBean.getVolume().doubleValue())
                    .build();
            series.addBar(newBar);

            System.out.println("bar" + newBar + " emaIndicator7：" + emaIndicator7.getValue(i) + " emaIndicator25：" + eemaIndicator25.getValue(i) + " emaIndicator99：" + emaIndicator99.getValue(i));
            i++;
        }


    }
}
