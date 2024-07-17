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

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

public class ddateUtil {

    public static LocalDateTime[] getInterval(long timestamp, String interval) {
        // 使用正则表达式匹配数字和后面的字符


        // 将时间戳转换为LocalDateTime
        LocalDateTime startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        LocalDateTime intervalStart, intervalEnd;
        char intervalType = interval.charAt(interval.length() - 1); // 获取最后一个字符作为时间单位
        int intervalLength = Integer.parseInt(interval.substring(0, interval.length() - 1)); // 获取剩余部分作为长度


        switch (intervalType) {
            case 'm': // 分钟级别
                int minuteOfDay = startTime.getHour() * 60 + startTime.getMinute();
                int startMinute = (minuteOfDay / intervalLength) * intervalLength;
                intervalStart = startTime.truncatedTo(ChronoUnit.DAYS).plusMinutes(startMinute);
                intervalEnd = intervalStart.plusMinutes(intervalLength).minusSeconds(1);
                break;
            case 'h': // 小时级别
                int startHour = (startTime.getHour() / intervalLength) * intervalLength;
                intervalStart = startTime.truncatedTo(ChronoUnit.DAYS).plusHours(startHour);
                intervalEnd = intervalStart.plusHours(intervalLength).minusSeconds(1);
                break;
            case 'd': // 天级别
                intervalStart = startTime.truncatedTo(ChronoUnit.DAYS);
                intervalEnd = intervalStart.plusDays(intervalLength).minusSeconds(1);
                break;
            case 'w': // 周级别
                // 调整到当前周的周一
                intervalStart = startTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                intervalEnd = intervalStart.plusWeeks(intervalLength).minusSeconds(1);
                break;
            default:
                throw new IllegalArgumentException("Invalid interval type"+intervalType);
        }

        return new LocalDateTime[]{intervalStart, intervalEnd};
    }

    public static void main(String[] args) {
        long currentTimeMillis = System.currentTimeMillis();
        char[] intervalTypes = {'m', 'h', 'd','w'};
        int[] intervalLengths = {4, 4, 1,2}; // 示例：15分钟、1小时、1天

        for (int i = 0; i < intervalTypes.length; i++) {
            LocalDateTime[] interval = getInterval(currentTimeMillis, intervalLengths[i] + String.valueOf(intervalTypes[i]));
            System.out.println(intervalLengths[i] + "" + intervalTypes[i] + " Interval: Start = " + interval[0] + ", End = " + interval[1]);
        }
    }

}
