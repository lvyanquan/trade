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

import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.client.impl.WebsocketClientImpl;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.binance.connector.futures.client.impl.UMWebsocketClientImpl;

public class BinanceClientFactory {

        private static final String API_KEY = "";
    private static final String SECRET_KEY = "";

    private static final WebsocketClientImpl websocketClient = new WebsocketClientImpl();

    private static final SpotClientImpl client = new SpotClientImpl();

    private static final SpotClientImpl client_auth = new SpotClientImpl(API_KEY,SECRET_KEY);

    private  static final UMWebsocketClientImpl umWebsocketClient =  new UMWebsocketClientImpl();

    private static UMFuturesClientImpl um_http_client = new UMFuturesClientImpl();

    public static WebsocketClientImpl spotWsClientNokey() {
        return websocketClient;
    }


    public static SpotClientImpl spotHttpClientNokey() {
        return client;
    }

    public static SpotClientImpl spotHttpClient() {
        return client_auth;
    }

    public static UMFuturesClientImpl umHttpClientNokey() {
        return um_http_client;
    }

    public static UMWebsocketClientImpl UMFuturesClientImpl() {
        return umWebsocketClient;
    }


}
