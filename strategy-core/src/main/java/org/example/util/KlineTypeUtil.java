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

package org.example.util;

import org.example.enums.KlineType;
import org.ta4j.core.Bar;
import org.ta4j.core.num.Num;


public class KlineTypeUtil {
    public static KlineType shadowOr(Bar bar) {
        Num lowPrice = bar.getLowPrice();
        Num highPrice = bar.getHighPrice();
        Num closePrice = bar.getClosePrice();
        Num openPrice = bar.getOpenPrice();
        //振幅
        Num amplitude = highPrice.minus(lowPrice);
        //实体长度
        Num abs = closePrice.minus(openPrice).abs();

        boolean uper = closePrice.minus(openPrice).isGreaterThanOrEqual(closePrice.zero());
        //下影线
        Num lowerShadow = uper ? openPrice.minus(lowPrice) : closePrice.minus(lowPrice);
        //上影线
        Num upperShadow = uper ? highPrice.minus(closePrice) : highPrice.minus(openPrice);

        Num num = amplitude
                .multipliedBy(closePrice.numOf(0.618));

        if (lowerShadow.isGreaterThan(num)) {
            return KlineType.LOWERSHADOW;
        }

        if (upperShadow.isGreaterThan(num)) {
            return KlineType.UPPERSHADOW;
        }

        if (lowerShadow.plus(upperShadow).isGreaterThan(num) &&
                (uper ? closePrice : openPrice)
                        .multipliedBy(closePrice.numOf(0.03)).isGreaterThan(abs)) {
            return KlineType.DOJI;
        }
        return null;
    }
}
