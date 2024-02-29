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

package com.example.web_kline.util;

import org.example.data.PriceBean;
import org.example.util.KlineUtil;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.num.DoubleNum;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class KlineTest {
    public static void main(String[] args) {
        List<PriceBean> btcusdt = KlineUtil.getBar2("BTCUSDT", "1m", System.currentTimeMillis() - 24 * 60 * 60 * 1000, System.currentTimeMillis());

        for (PriceBean priceBean : btcusdt) {
            Bar newBar = BaseBar.builder(DoubleNum::valueOf, Double.class)
                    .timePeriod(Duration.ofMinutes(1))
                    .endTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(priceBean.getEndTime()), ZoneId.systemDefault()))
                    .openPrice(priceBean.getOpen().doubleValue())
                    .highPrice(priceBean.getHigh().doubleValue())
                    .lowPrice(priceBean.getLow().doubleValue())
                    .closePrice(priceBean.getClose().doubleValue())
                    .volume(priceBean.getVolume().doubleValue())
                    .build();

            ///开始时间小于结束时间 如果结束时间比当前时间还要大 应该默认设置为当前时间
            //做一个时间的过滤判断即可  开始opentime>=startTime  结束是endTime>=closeTime
            //如果当前最后一条数据不符合结束，则直接进行循环，最后一条数据的opentime作为下次开始的openTime 然后进行一次丢弃即可 或者内部判断下 openTIme相同就丢弃

            //通过这个http请求进行数据的插入到paimon中  是直接写代码 还是flink任务调度上去 实时采集吗？5分钟级别的

        }
    }
}
