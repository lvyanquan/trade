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

package com.example.web_kline.test2;

import org.example.StrateFactory;
import org.example.model.currency.CurrencyRegister;
import org.ta4j.core.Trade;

import java.util.concurrent.atomic.AtomicBoolean;

public class TraderTest {

    public static void main(String[] args) throws Exception {

        test(StrateFactory.TREND_FACTORY, true);
        Thread.currentThread().join();
    }

    public static void test(StrateFactory strateFactory, boolean mock) {
        new Thread(() -> {
            CurrencyRegister.getCurrency("BTCUSDT").ifPresent(i -> {
                AtomicBoolean running = new AtomicBoolean(false);
                while (true) {
                    if (running.compareAndSet(false, true)) {
                        try {
                            TraderTemplate test = new TraderTemplate(mock, Trade.TradeType.BUY, 800,"30m", i, strateFactory, 5);
                            test.test("30m");
                        } catch (Exception e1) {
                            System.out.println("----" + e1);
                            running.set(false);
                        }
                    }
                }
            });
        }).start();
    }
}


