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

import java.math.BigDecimal;

public class Trade {
    private String symbol;          // 交易对符号，如 "BNBBTC"
    private Long id;                // 交易的唯一标识符
    private Long orderId;           // 相关联订单的唯一标识符
    private Long orderListId;       // 如果订单属于一个 OCO 订单组，这个字段会提供订单组的 ID。对于单个订单，值为 -1
    private BigDecimal price;       // 交易价格
    private BigDecimal qty;         // 交易数量
    private BigDecimal quoteQty;    // 交易的报价资产数量（即 price * qty）
    private BigDecimal commission;  // 交易的手续费
    private String commissionAsset; // 手续费的资产类型，如 "BNB"
    private Long time;              // 交易的时间戳，以毫秒为单位
    private Boolean isBuyer;        // 是否为买方
    private Boolean isMaker;        // 是否为挂单方
    private Boolean isBestMatch;    // 是否为最佳匹配


    public Trade(String symbol, Long id, Long orderId, Long orderListId, BigDecimal price, BigDecimal qty, BigDecimal quoteQty, BigDecimal commission, String commissionAsset, Long time, Boolean isBuyer, Boolean isMaker, Boolean isBestMatch) {
        this.symbol = symbol;
        this.id = id;
        this.orderId = orderId;
        this.orderListId = orderListId;
        this.price = price;
        this.qty = qty;
        this.quoteQty = quoteQty;
        this.commission = commission;
        this.commissionAsset = commissionAsset;
        this.time = time;
        this.isBuyer = isBuyer;
        this.isMaker = isMaker;
        this.isBestMatch = isBestMatch;
    }

    public String getSymbol() {
        return symbol;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getOrderListId() {
        return orderListId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public BigDecimal getQuoteQty() {
        return quoteQty;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public String getCommissionAsset() {
        return commissionAsset;
    }

    public Long getTime() {
        return time;
    }

    public Boolean getBuyer() {
        return isBuyer;
    }

    public Boolean getMaker() {
        return isMaker;
    }

    public Boolean getBestMatch() {
        return isBestMatch;
    }
}
