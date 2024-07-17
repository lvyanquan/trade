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

import org.example.model.market.KlineModule;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.num.DoubleNum;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class KlineUtil {
    public static Bar convertKlineModuleToBar(KlineModule klineModule) {
        Bar newBar = BaseBar.builder(DoubleNum::valueOf, Double.class)
                .timePeriod(Duration.ofMinutes(1))
                .endTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(klineModule.getEndTime()), ZoneId.systemDefault()))
                .openPrice(klineModule.getOpen())
                .highPrice(klineModule.getHigh())
                .lowPrice(klineModule.getLow())
                .closePrice(klineModule.getClose())
                .volume(klineModule.getQuantity())
                .amount(klineModule.getAmount())
                .build();
        return newBar;
    }
}
