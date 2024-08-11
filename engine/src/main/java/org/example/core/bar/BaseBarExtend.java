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

package org.example.core.bar;

import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.num.Num;

import java.time.Duration;
import java.time.ZonedDateTime;

public class BaseBarExtend implements Bar {
   private final BaseBar baseBar;

   //构建时间，如果是历史数据，就是 baseBar的openTime，如果是实时数据，就是binance里的实时时间
   private final ZonedDateTime createTime;

    public BaseBarExtend(BaseBar baseBar, ZonedDateTime createTime) {
        this.baseBar = baseBar;
        this.createTime = createTime;
    }

    @Override
    public Num getOpenPrice() {
        return baseBar.getOpenPrice();
    }

    @Override
    public Num getLowPrice() {
        return baseBar.getLowPrice();
    }

    @Override
    public Num getHighPrice() {
        return baseBar.getHighPrice();
    }

    @Override
    public Num getClosePrice() {
        return baseBar.getClosePrice();
    }

    @Override
    public Num getVolume() {
        return baseBar.getVolume();
    }

    @Override
    public long getTrades() {
        return baseBar.getTrades();
    }

    @Override
    public Num getAmount() {
        return baseBar.getAmount();
    }

    @Override
    public Duration getTimePeriod() {
        return baseBar.getTimePeriod();
    }

    @Override
    public ZonedDateTime getBeginTime() {
        return baseBar.getBeginTime();
    }

    @Override
    public ZonedDateTime getEndTime() {
        return baseBar.getEndTime();
    }

    @Override
    public void addTrade(Num num, Num num1) {
         baseBar.addTrade(num,num1);
    }

    @Override
    public void addPrice(Num num) {
        baseBar.addPrice(num);
    }

    public ZonedDateTime getCreateTime() {
        return createTime;
    }
}
