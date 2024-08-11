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

package org.example.core.handler.notify;

import org.example.core.bar.Bar;
import org.example.core.bar.TradeType;

import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public abstract class baseTemplateNotify implements Notify {
    private final String priceTemplate = "币种: %s - %s - %s\n" +
            "时间: %s --- %s\n" +
            "开：%s\n" +
            "高：%s\n" +
            "低：%s\n" +
            "收：%s\n" +
            "成交量: %s\n" +
            "成交额: %s\n" +
            "振幅：%s\n" +
            "涨跌幅：%s\n";
    // Define the desired format
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private final DecimalFormat df = new DecimalFormat("#");

    public void notifyPrice(Bar bar, String symbol, TradeType tradeType, String exchangeName) {

        String msg = String.format(priceTemplate, symbol, tradeType.getChinaDescription(), exchangeName,
                bar.getBeginTime().format(formatter),
                bar.getEndTime().format(formatter),
                bar.getOpenPrice(),
                bar.getHighPrice(),
                bar.getLowPrice(),
                bar.getClosePrice(),
                df.format(bar.getVolume()),
                df.format(bar.getAmount()),
                String.format("%.4f%%", bar.getAmplitudePercent()),
                String.format("%.4f%%", bar.getChangePercentage()));

        notify(msg);
    }

    public void notifySignal(String symbol, TradeType tradeType, String exchangeName, String interVal, String indicatorName, ZonedDateTime time, boolean buy) {
        String msg = String.format("币种: %s - %s - %s\n" +
                "指标: %s\n" +
                "买入: %s\n" +
                "周期: %s\n" +
                "更新时间: %s\n",
                symbol,
                tradeType.getChinaDescription(),
                exchangeName,
                indicatorName,
                buy? "是" : "否",
                interVal,
                time.format(formatter));
        notify(msg);
    }

}
