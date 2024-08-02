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

import com.binance.connector.client.impl.SpotClientImpl;
import org.example.core.bar.Bar;
import org.example.core.bar.KlineInterval;

import java.util.LinkedHashMap;
import java.util.List;

public class SpotKlineUtil {

    public static List<Bar> klines (SpotClientImpl client, String symbol, KlineInterval interval, long statrTime, long endTime, long limit){
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        //每次循环 将最后一条数据的close时间作为下一次请求的start时间即可 直到数据返回为空即[]
        //当前时间 小于 最后一条数据的时间 加上 间隔时间 即可
        parameters.put("symbol", symbol);
        parameters.put("interval", interval.getInterval());
        parameters.put("startTime", statrTime);
        parameters.put("endTime", endTime);
        parameters.put("limit", limit);
        String result = client.createMarket().klines(parameters);
        return BinanceJsonParse.parseHttpKline(result,interval);
    }
}
