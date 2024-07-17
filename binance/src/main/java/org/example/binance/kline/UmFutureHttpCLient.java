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

package org.example.binance.kline;

import org.example.client.HttpClient;
import org.example.client.dto.KlineDto;
import org.example.model.market.KlineModule;
import org.example.binance.parse.BinanceJsonParse;

import java.util.LinkedHashMap;
import java.util.List;

public class UmFutureHttpCLient implements HttpClient {


    @Override
    public List<KlineModule> klines(KlineDto klineDto) {

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        //每次循环 将最后一条数据的close时间作为下一次请求的start时间即可 直到数据返回为空即[]
        parameters.put("symbol", klineDto.getCurrency().symbol());
        parameters.put("interval", klineDto.getInterval());
        if (klineDto.getStartTime() != null && klineDto.getEndTime() != null) {
            parameters.put("startTime", klineDto.getStartTime());
            parameters.put("endTime", klineDto.getEndTime());
        }
        //Default 500; max 1000.
        parameters.put("limit", 1000);
        List<KlineModule> klineModules = doGetKlines(parameters);
        if (klineDto.getStartTime() != null && klineDto.getEndTime() != null) {
            while (klineModules.get(klineModules.size() - 1).getEndTime() < klineDto.getEndTime()) {
                parameters.put("startTime", klineModules.get(klineModules.size() - 1).getEndTime());
                List<KlineModule> data = doGetKlines(parameters);
                klineModules.addAll(data);
                if (data.size() <= 1) {
                    break;
                }
            }
        }
        return klineModules;
    }

    private List<KlineModule> doGetKlines(LinkedHashMap<String, Object> parameters) {
        String result = BinanceClientFactory.umHttpClientNokey().market().klines(parameters);
        return BinanceJsonParse.parseHttpKline(result);
    }

}
