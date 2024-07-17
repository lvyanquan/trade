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

package org.example.client.dto;

import org.example.model.currency.Currency;

public class KlineDto {
    //每次循环 将最后一条数据的close时间作为下一次请求的start时间即可 直到数据返回为空即[]
    private Currency currency;

    private String interval;

    private Long startTime;
    private Long endTime;

    public KlineDto(Currency currency, String interval, long startTime, long endTime) {
        this.currency = currency;
        this.interval = interval;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public KlineDto(Currency currency, String interval) {
        this.currency = currency;
        this.interval = interval;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getInterval() {
        return interval;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return endTime;
    }
}
