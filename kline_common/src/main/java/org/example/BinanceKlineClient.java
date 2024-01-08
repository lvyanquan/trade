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

package org.example;


import com.binance.connector.futures.client.impl.UMWebsocketClientImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;


public class BinanceKlineClient implements KlineClient {

    private  UMWebsocketClientImpl client = new UMWebsocketClientImpl();
    private final Map<String, Integer> connectionCache = new HashMap<>();

    private final Set<String> historySyncSymbols = new HashSet<>();

    @Override
    public void subscribe(String symbol, String interval, Consumer callback) {
        synchronized (connectionCache) {
            if (connectionCache.containsKey(symbol + "_" + interval)) {
                return;
            }
            int connectionId = client.klineStream(symbol, interval,
                    msg -> {
                        System.out.println("123" + msg);
                    }, event -> callback.accept(event)
                    , msg -> {
                        System.out.println("abc" + msg);
                    }, msg -> {
                        System.out.println("出现异常,重新连接" + msg);
                        this.unSubscribe(symbol,interval);
                        this.client = new UMWebsocketClientImpl();
                        try {
                            Thread.sleep(30000L);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        this.subscribe(symbol, interval, callback);
                    });
            connectionCache.put(symbol + "_" + interval, connectionId);
        }
    }

    @Override
    public void subscribeHistory(String symbol, String interval, long startTime, long endTime) {
        if (historySyncSymbols.contains(symbol)) {
            throw new UnsupportedOperationException(String.format("can not support sync %s because %s is currently being synchronized ", symbol, symbol));
        }
        historySyncSymbols.add(symbol);
    }

    @Override
    public void unSubscribe(String symbol, String interval) {
        synchronized (connectionCache) {
            Integer connectionId = connectionCache.remove(symbol + "_" + interval);
            client.closeConnection(connectionId);
        }
    }
}
