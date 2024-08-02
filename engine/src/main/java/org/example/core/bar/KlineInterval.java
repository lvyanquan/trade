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

package org.example.core.bar;


import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public enum KlineInterval {


    ONE_SECOND("1s", 1, ChronoUnit.SECONDS),
    ONE_MINUTE("1m", 60, ChronoUnit.MINUTES),
    FIVE_MINUTES("5m", 300, ChronoUnit.MINUTES),
    FIFTEEN_MINUTES("15m", 900, ChronoUnit.MINUTES),
    THIRTY_MINUTES("30m", 1800, ChronoUnit.MINUTES),
    ONE_HOUR("1h", 3600, ChronoUnit.HOURS),
    FOUR_HOURS("4h", 14400, ChronoUnit.HOURS),
    ONE_DAY("1d", 86400, ChronoUnit.DAYS);

    private final String interval;
    private final long seconds;
    private final ChronoUnit chronoUnit;

    KlineInterval(String interval, long seconds, ChronoUnit chronoUnit) {
        this.interval = interval;
        this.seconds = seconds;
        this.chronoUnit = chronoUnit;
    }

    public String getInterval() {
        return interval;
    }

    public long getSeconds() {
        return seconds;
    }

    public ChronoUnit getChronoUnit() {
        return chronoUnit;
    }


    public LocalDateTime[] getTimeWindow(long timestamp, int window) {
        // Convert the timestamp to LocalDateTime


        // Calculate total units
        long unitSize = window * seconds;
        long startTime = timestamp / unitSize / 1000 * unitSize;
        LocalDateTime start = LocalDateTime.ofEpochSecond(startTime, 0, ZoneOffset.UTC);
        LocalDateTime end = LocalDateTime.ofEpochSecond(startTime + unitSize, 0, ZoneOffset.UTC)
                .minus(1, ChronoUnit.MILLIS);

        return new LocalDateTime[]{start, end};
    }

    public LocalDateTime getNextTimePoint(LocalDateTime timePoint, int window) {
        long totalSeconds = window * getSeconds();
        return timePoint.plusSeconds(totalSeconds).withNano(0);
    }

    @Override
    public String toString() {
        return interval;
    }
}

