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


public class OrderResponseInfo {

   private String clientOrderId ;
    private  String orderId ;
    private  String symbol;
    private String status ;

    // 累计交易的金额 实际金额
    private double cummulativeQuoteQty ;
    // 交易的仓位数量
    private double executedQty ;
    //原始的仓位数量
    private double origQty ;
    private  double price ;
    //BUY SELL
    private String side;

    private long updateTime;

    public OrderResponseInfo(String clientOrderId, String orderId, String symbol, String status, double cummulativeQuoteQty, double executedQty, double origQty, double price,String side,long updateTime) {
        this.clientOrderId = clientOrderId;
        this.orderId = orderId;
        this.symbol = symbol;
        this.status = status;
        this.cummulativeQuoteQty = cummulativeQuoteQty;
        this.executedQty = executedQty;
        this.origQty = origQty;
        this.price = price;
        this.side = side;
        this.updateTime = updateTime;
    }


    public String getClientOrderId() {
        return clientOrderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getStatus() {
        return status;
    }

    public double getCummulativeQuoteQty() {
        return cummulativeQuoteQty;
    }

    public double getExecutedQty() {
        return executedQty;
    }

    public double getOrigQty() {
        return origQty;
    }

    public double getPrice() {
        return price;
    }

    public String getSide() {
        return side;
    }

    public long getUpdateTime() {
        return updateTime;
    }
}
