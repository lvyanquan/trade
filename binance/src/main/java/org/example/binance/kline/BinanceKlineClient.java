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
import org.example.client.MarketWebSocketClient;
import org.example.client.dto.KlineDto;
import org.example.kline.KlineClient;
import org.example.model.currency.Currency;
import org.example.model.enums.ContractType;
import org.example.model.market.KlineModule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class BinanceKlineClient implements KlineClient {


    public BinanceKlineClient(ContractType ContractType) {
        if (ContractType == ContractType.SPOT) {
            this.client = new SpotWsClient();
            this.httpClient = new SpotHttpClient();
        } else {
            this.client = new UmFutureWsClient();
        }
    }

    public final Map<String, Integer> connectionCache = new HashMap<>();

    private MarketWebSocketClient client;

    private HttpClient httpClient = new UmFutureHttpCLient();


    @Override
    public void handlerStreamingKlineData(Currency symbol, String interval, Consumer<KlineModule> callback) {
        this.handlerStreamingKlineData(symbol, interval, callback, e -> {
        });
    }

    @Override
    public void handlerStreamingKlineData(Currency symbol, String interval, Consumer<KlineModule> callback, Consumer<KlineModule> intervalDataCallback) {
        String key =  symbol.symbol() + "_" + interval;
        synchronized (connectionCache) {
            if (connectionCache.containsKey(key)) {
                return;
            }
            AtomicReference<KlineModule> lastKline = new AtomicReference<>();
            int connectionId = client.klineStream(symbol.symbol(), interval,
                    t -> {
                        if (lastKline.get() == null || lastKline.get().getEndTime() != t.getEndTime()) {
                            intervalDataCallback.accept(t);
                        }
                        callback.accept(t);
                        lastKline.set(t);
                    },
                    msg -> {
                    }
                    , msg -> this.unSubscribe(symbol, interval), msg -> {
                        System.out.println("出现异常,重新连接" + msg);
                        try {
                            Thread.sleep(5000L);
                        } catch (InterruptedException e) {
//                          throw new RuntimeException(e);
                        }
                        this.unSubscribe(symbol, interval);
                        this.handlerStreamingKlineData(symbol, interval, callback,intervalDataCallback);
                    });
            connectionCache.put(key, connectionId);
        }
    }

    @Override
    public List<KlineModule> getHistoryKlineData(Currency symbol, String interval, long startTime, long endTime) {
        return httpClient.klines(new KlineDto(symbol, interval, startTime, endTime));
    }

    @Override
    public void unSubscribe(Currency symbol, String interval) {
        synchronized (connectionCache) {
            Integer connectionId = connectionCache.remove(symbol.symbol() + "_" + interval);
            if (connectionId != null) {
                client.closeConnection(connectionId);
            }
        }
    }
}
