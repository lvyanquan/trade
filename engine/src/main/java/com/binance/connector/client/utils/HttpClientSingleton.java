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

package com.binance.connector.client.utils;


import com.binance.connector.futures.client.utils.ProxyAuth;
import okhttp3.OkHttpClient;
import java.net.Proxy;

public final class HttpClientSingleton {
    private static OkHttpClient httpClient = null;

    private HttpClientSingleton() {
    }

    public static OkHttpClient getHttpClient() {
        if (httpClient == null) {
            createHttpClient(null);
        }
        return httpClient;
    }

    public static OkHttpClient getHttpClient(ProxyAuth proxy) {
        if (httpClient == null) {
            createHttpClient(proxy);
        } else {
            verifyHttpClient(proxy);
        }
        return httpClient;
    }

    private static void createHttpClient(ProxyAuth proxy) {
        if (proxy == null) {
            httpClient = new OkHttpClient();
        } else {
            if (proxy.getAuth() == null) {
                httpClient = new OkHttpClient.Builder().proxy(proxy.getProxy()).build();
            } else {
                httpClient = new OkHttpClient.Builder().proxy(proxy.getProxy()).proxyAuthenticator(proxy.getAuth()).build();
            }
        }
    }

    private static void verifyHttpClient(ProxyAuth proxy) {
        Proxy prevProxy = httpClient.proxy();

        if ((proxy != null && !proxy.getProxy().equals(prevProxy)) || (proxy == null && prevProxy != null)) {
            createHttpClient(proxy);
        }
    }
}