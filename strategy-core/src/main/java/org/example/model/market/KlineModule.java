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

package org.example.model.market;

public class KlineModule {
    private long openTime;
    private long endTime;
    private double open;
    private double high;
    private double low;
    private double close;
    //数量
    private double quantity;

    //交易额
    private double amount;

    private  double takerBuyQuantity;
    private  double takerBuyAmount;


    //后续可以使用builder模式
    public KlineModule(long openTime, long endTime, double open, double high, double low, double close, double quantity) {
      this.openTime = openTime;
        this.endTime = endTime;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.quantity = quantity;
    }

    public KlineModule(long openTime, long endTime, double open, double high, double low, double close, double quantity,double amount,double takerBuyQuantity,double takerBuyAmount) {
        this.openTime = openTime;
        this.endTime = endTime;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.quantity = quantity;
        this.amount = amount;
        this.takerBuyQuantity = takerBuyQuantity;
        this.takerBuyAmount = takerBuyAmount;
    }

    public long getOpenTime() {
        return openTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getClose() {
        return close;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getAmount() {
        return amount;
    }

    public double getTakerBuyQuantity() {
        return takerBuyQuantity;
    }

    public double getTakerBuyAmount() {
        return takerBuyAmount;
    }

    @Override
    public String toString() {
        return "KlineModule{" +
                "openTime=" + openTime +
                ", endTime=" + endTime +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", quantity=" + quantity +
                ", amount=" + amount +
                ", takerBuyQuantity=" + takerBuyQuantity +
                ", takerBuyAmount=" + takerBuyAmount +
                '}';
    }
}
