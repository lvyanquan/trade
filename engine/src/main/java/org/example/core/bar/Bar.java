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

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;


public class Bar {
    private final ZonedDateTime beginTime;
    private final ZonedDateTime endTime;
    private final double openPrice;
    private final double highPrice;
    private final double lowPrice;
    private final double closePrice;
    private final double volume;
    private @Nullable double amount;
    private final Duration timePeriod;


    private Bar(ZonedDateTime beginTime, ZonedDateTime endTime, Duration timePeriod, double openPrice, double highPrice, double lowPrice, double closePrice, double volume, double amount) {
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
        this.timePeriod = timePeriod;
        this.amount = amount;
    }

    private Bar(Builder builder) {
        this.endTime = builder.endTime;
        this.timePeriod = builder.timePeriod;
        this.beginTime = builder.endTime.minus(builder.timePeriod).plus(1, ChronoUnit.MILLIS);
        this.openPrice = builder.openPrice;
        this.highPrice = builder.highPrice;
        this.lowPrice = builder.lowPrice;
        this.closePrice = builder.closePrice;
        this.volume = builder.volume;
        this.amount = builder.amount;
    }

    public ZonedDateTime getBeginTime() {
        return beginTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public double getHighPrice() {
        return highPrice;
    }

    public double getLowPrice() {
        return lowPrice;
    }

    public double getClosePrice() {
        return closePrice;
    }

    public double getVolume() {
        return volume;
    }

    public Duration getTimePeriod() {
        return timePeriod;
    }

    public double getAmount() {
        return amount;
    }

    public double getAmplitude() {
        return (highPrice - lowPrice) / openPrice * 100;
    }

    public double getChangePercentage() {
        return (closePrice - openPrice) / openPrice * 100;
    }

    public Bar merge(Bar bar) {
        ZonedDateTime newBeginTime = this.beginTime.isBefore(bar.beginTime) ? this.beginTime : bar.beginTime;
        ZonedDateTime newEndTime = this.endTime.isAfter(bar.endTime) ? this.endTime : bar.endTime;
        double newOpenPrice = this.beginTime.isBefore(bar.beginTime) ? this.openPrice : bar.openPrice;
        double newClosePrice = this.endTime.isAfter(bar.endTime) ? this.closePrice : bar.closePrice;
        double newHighPrice = Math.max(this.highPrice, bar.highPrice);
        double newLowPrice = Math.min(this.lowPrice, bar.lowPrice);
        double newVolume = this.volume + bar.volume;
        double newAmount = this.amount + bar.amount;
        //更新持续时间
        Duration newTimePeriod = Duration.between(newBeginTime, newEndTime);
        return new Bar(newBeginTime, newEndTime, newTimePeriod, newOpenPrice, newHighPrice, newLowPrice, newClosePrice, newVolume, newAmount);
    }

    @Override
    public String toString() {
        return String.format("{startTime: %1s," +
                        " endTime: %2s," +
                        " openPrice: %3$f, " +
                        " highPrice: %4$f," +
                        " lowPrice: %5$f," +
                        " close price: %6$f, " +
                        " volume: %7$f, " +
                        "amount: %8$f}",
                this.beginTime.withZoneSameInstant(ZoneId.systemDefault()),
                this.endTime.withZoneSameInstant(ZoneId.systemDefault()),
                this.openPrice,
                this.highPrice,
                this.lowPrice,
                this.closePrice,
                this.volume,
                this.amount);

    }


    @Override
    public int hashCode() {
        return Objects.hash(new Object[]{this.beginTime, this.endTime, this.timePeriod, this.openPrice, this.highPrice, this.lowPrice, this.closePrice, this.volume, this.amount});
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Bar)) {
            return false;
        } else {
            Bar other = (Bar) obj;
            return Objects.equals(this.beginTime, other.getBeginTime()) && Objects.equals(this.endTime, other.getEndTime())
                    && Objects.equals(this.timePeriod, other.getTimePeriod())
                    && Objects.equals(this.openPrice, other.getOpenPrice())
                    && Objects.equals(this.highPrice, other.getHighPrice())
                    && Objects.equals(this.lowPrice, other.getLowPrice())
                    && Objects.equals(this.closePrice, other.getClosePrice())
                    && Objects.equals(this.volume, other.getVolume())
                    && Objects.equals(this.amount, other.getAmount());
        }
    }


    public static class Builder {
        private ZonedDateTime endTime;
        private Duration timePeriod;
        private double openPrice;
        private double highPrice;
        private double lowPrice;
        private double closePrice;
        private double volume;
        private @Nullable double amount;

        public Builder endTime(ZonedDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder timePeriod(Duration timePeriod) {
            this.timePeriod = timePeriod;
            return this;
        }

        public Builder openPrice(double openPrice) {
            this.openPrice = openPrice;
            return this;
        }

        public Builder highPrice(double highPrice) {
            this.highPrice = highPrice;
            return this;
        }

        public Builder lowPrice(double lowPrice) {
            this.lowPrice = lowPrice;
            return this;
        }

        public Builder closePrice(double closePrice) {
            this.closePrice = closePrice;
            return this;
        }

        public Builder volume(double volume) {
            this.volume = volume;
            return this;
        }

        public Builder amount(double amount) {
            this.amount = amount;
            return this;
        }

        public Bar build() {
            checkTimeArguments(timePeriod, endTime);
            return new Bar(this);
        }

        private static void checkTimeArguments(Duration timePeriod, ZonedDateTime endTime) {
            if (timePeriod == null) {
                throw new IllegalArgumentException("Time period cannot be null");
            } else if (endTime == null) {
                throw new IllegalArgumentException("End time cannot be null");
            }
        }
    }


}
