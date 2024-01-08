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

package org.example.data;

import org.example.data.currency.Currency;

import java.math.BigDecimal;

public class Tiket24Bean {
    private Currency currency;

    private long openTime;
    private long closeTime;
    //成交总额 usdt单位
    private BigDecimal quoteVolume;
    //成交次数
    private long count;

    public Tiket24Bean(Currency currency, long openTime, long closeTime, BigDecimal quoteVolume, long count) {
        this.currency = currency;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.quoteVolume = quoteVolume;
        this.count = count;
    }

    public Currency getCurrency() {
        return currency;
    }

    public long getOpenTime() {
        return openTime;
    }

    public long getCloseTime() {
        return closeTime;
    }

    public BigDecimal getQuoteVolume() {
        return quoteVolume;
    }

    public long getCount() {
        return count;
    }
}
