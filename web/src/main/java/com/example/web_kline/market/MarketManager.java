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

package com.example.web_kline.market;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import org.example.model.market.Tiket24Bean;
import org.example.model.currency.CurrencyRegister;
import org.example.util.JsonUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MarketManager {
    private static final UMFuturesClientImpl client = new UMFuturesClientImpl();

    public static void main(String[] args) {
        //获取每个任务的精度等信息，写入一个文件，读取更新当前环境里的currenty信息即可
        //下单时就可以按照这个精度来
        System.out.println(client.market().exchangeInfo());
    }

    public static List<Tiket24Bean> getTicket24h() {
        ArrayList<Tiket24Bean> tiket24Beans = new ArrayList<>();
        String Ticket24 = client.market().ticker24H(new LinkedHashMap<>());
        for (Object o : JsonUtil.parse(Ticket24, List.class)) {
            Map map = (Map) o;
            CurrencyRegister.getCurrency(map.get("symbol").toString()).ifPresent(c -> {
                Tiket24Bean tiket24Bean = new Tiket24Bean(c,
                        Long.parseLong(map.get("openTime").toString()),
                        Long.parseLong(map.get("closeTime").toString()),
                        new BigDecimal(map.get("quoteVolume").toString()),
                        Long.parseLong(map.get("count").toString())
                );
                tiket24Beans.add(tiket24Bean);
            });
        }

        //降序排序 取前100位
        tiket24Beans.sort(Comparator.comparing(Tiket24Bean::getQuoteVolume).reversed());
        return tiket24Beans;
    }
}
