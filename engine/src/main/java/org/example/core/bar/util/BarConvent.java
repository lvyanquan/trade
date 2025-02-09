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

package org.example.core.bar.util;

import org.example.core.bar.BaseBarExtend;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.num.DoubleNum;

public class BarConvent {
    public static Bar convent(org.example.core.bar.Bar bar) {

        return BaseBar.builder(DoubleNum::valueOf, Double.class)
                .timePeriod(bar.getTimePeriod())
                .endTime(bar.getEndTime())
                .openPrice(bar.getOpenPrice())
                .highPrice(bar.getHighPrice())
                .lowPrice(bar.getLowPrice())
                .closePrice(bar.getClosePrice())
                .volume(bar.getVolume())
                .amount(bar.getAmount())
                .build();
    }

    public static BaseBarExtend conventBaseBarExtend(org.example.core.bar.Bar bar) {

        BaseBar newBar = (BaseBar)convent(bar);

        return new BaseBarExtend(newBar,bar.getCreateTime());
    }
}
