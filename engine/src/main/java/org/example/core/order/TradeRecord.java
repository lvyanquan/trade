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

package org.example.core.order;

public class TradeRecord {


    private String id;
    private String orderId;
    private String symbol;
    private double price;
    private double qty;
    private double quoteQty;
    private double commission;
    private String commissionAsset;
    private boolean isBuyer;
    private boolean isMaker;
    private boolean isBestMatch;



    public TradeRecord(String id, String orderId, String symbol, double price, double qty, double quoteQty, double commission, String commissionAsset, boolean isBuyer, boolean isMaker, boolean isBestMatch) {
        this.id = id;
        this.orderId = orderId;
        this.symbol = symbol;
        this.price = price;
        this.qty = qty;
        this.quoteQty = quoteQty;
        this.commission = commission;
        this.commissionAsset = commissionAsset;
        this.isBuyer = isBuyer;
        this.isMaker = isMaker;
        this.isBestMatch = isBestMatch;
    }

    public String getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public double getQty() {
        return qty;
    }

    public double getQuoteQty() {
        return quoteQty;
    }

    public double getCommission() {
        return commission;
    }

    public String getCommissionAsset() {
        return commissionAsset;
    }

    public boolean isBuyer() {
        return isBuyer;
    }

    public boolean isMaker() {
        return isMaker;
    }

    public boolean isBestMatch() {
        return isBestMatch;
    }
}
