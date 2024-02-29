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

import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.data.PriceBean;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class KlineUtil {
    public static final String API_KEY = "";
    public static final String SECRET_KEY = "";
    public static final String UM_BASE_URL = "https://fapi.binance.com";


    public static List<PriceBean> getBar(String name, String interval, long startTime, long endTime) {

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();

        SpotClientImpl client = new SpotClientImpl(API_KEY, SECRET_KEY);

        //每次循环 将最后一条数据的close时间作为下一次请求的start时间即可 直到数据返回为空即[]
        parameters.put("symbol", name);
        parameters.put("interval", interval);
        parameters.put("startTime", startTime);
        parameters.put("endTime", endTime);
        //Default 500; max 1000.
        parameters.put("limit", "1000");

        String result = client.createMarket().klines(parameters);

        List<List> lists = JsonUtil.parse(result, List.class);
        LinkedList<PriceBean> priceBeans = new LinkedList<>();

        lists.forEach(i -> {
            List<Object> data = (List<Object>) i;
            long openTime = Long.parseLong(data.get(0).toString());
            long closeTime = Long.parseLong(data.get(6).toString());

            BigDecimal o = BigDecimal.valueOf(Double.parseDouble(data.get(1).toString()));
            BigDecimal h = BigDecimal.valueOf(Double.parseDouble(data.get(2).toString()));
            BigDecimal l = BigDecimal.valueOf(Double.parseDouble(data.get(3).toString()));
            BigDecimal c = BigDecimal.valueOf(Double.parseDouble(data.get(4).toString()));
            BigDecimal v = BigDecimal.valueOf(Double.parseDouble(data.get(5).toString()));

            priceBeans.add(new PriceBean(openTime,closeTime, o, h, l, c, v));

        });

        return priceBeans;

    }


    public static List<PriceBean> getBar2(String name, String interval, long startTime, long endTime) {

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();

        UMFuturesClientImpl client = new UMFuturesClientImpl();

        //每次循环 将最后一条数据的close时间作为下一次请求的start时间即可 直到数据返回为空即[]
        parameters.put("symbol", name);
        parameters.put("interval", interval);
        parameters.put("startTime", startTime);
        parameters.put("endTime", endTime);
        //Default 500; max 1000.
        parameters.put("limit", "1000");

        String result = client.market().klines(parameters);

        List<List> lists = JsonUtil.parse(result, List.class);

        LinkedList<PriceBean> priceBeans = new LinkedList<>();

        lists.forEach(i -> {
            List<Object> data = (List<Object>) i;
            long openTime = Long.parseLong(data.get(0).toString());
            long closeTime = Long.parseLong(data.get(6).toString());
            BigDecimal o = BigDecimal.valueOf(Double.parseDouble(data.get(1).toString()));
            BigDecimal h = BigDecimal.valueOf(Double.parseDouble(data.get(2).toString()));
            BigDecimal l = BigDecimal.valueOf(Double.parseDouble(data.get(3).toString()));
            BigDecimal c = BigDecimal.valueOf(Double.parseDouble(data.get(4).toString()));
            BigDecimal v = BigDecimal.valueOf(Double.parseDouble(data.get(5).toString()));

            priceBeans.add(new PriceBean(openTime,closeTime, o, h, l, c, v));

        });

        return priceBeans;

    }
}
