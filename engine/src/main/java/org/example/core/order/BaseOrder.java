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

import org.example.core.enums.OrderState;
import org.example.core.enums.Side;

import java.math.BigDecimal;

public class BaseOrder {

    private Long orderId;  // 订单的唯一标识符，由 Binance 分配
    private String symbol;  // 交易对的符号，如 LTCBTC
    private String clientOrderId;  // 用户自定义的订单标识符
    private BigDecimal price;  // 订单的价格
    private BigDecimal origQty;  // 订单的原始数量，即用户下单时请求的数量
    private BigDecimal executedQty;  // 订单已执行的数量，即已成交的部分
    private BigDecimal cummulativeQuoteQty;  // 已累计执行的报价资产数量
    private OrderState status;  // 订单的当前状态，例如 0 (NEW), 1(PARTIALLY_FILLED), 2(FILLED), 3(CANCELED), 4(REJECTED), 5(EXPIRED) 等
    private Side side;  // 订单方向，表示是买入 0(BUY) 还是卖出 1(SELL)
    private Long time;  // 订单创建的时间戳，以毫秒为单位
    private Long updateTime;  // 订单最近一次更新的时间戳，以毫秒为单位

    public BaseOrder(Long orderId, String symbol, String clientOrderId, BigDecimal price, BigDecimal origQty, BigDecimal executedQty, BigDecimal cummulativeQuoteQty, OrderState status, Side side, Long time, Long updateTime) {
        this.orderId = orderId;
        this.symbol = symbol;
        this.clientOrderId = clientOrderId;
        this.price = price;
        this.origQty = origQty;
        this.executedQty = executedQty;
        this.cummulativeQuoteQty = cummulativeQuoteQty;
        this.status = status;
        this.side = side;
        this.time = time;
        this.updateTime = updateTime;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getSymbol() {
        return symbol;
    }


    public String getClientOrderId() {
        return clientOrderId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getOrigQty() {
        return origQty;
    }

    public BigDecimal getExecutedQty() {
        return executedQty;
    }

    public BigDecimal getCummulativeQuoteQty() {
        return cummulativeQuoteQty;
    }

    public OrderState getStatus() {
        return status;
    }

    public Side getSide() {
        return side;
    }

    public Long getTime() {
        return time;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    @Override
    public String toString() {
        return "BaseOrder{" +
                "orderId=" + orderId +
                ", symbol='" + symbol + '\'' +
                ", clientOrderId='" + clientOrderId + '\'' +
                ", price=" + price.toPlainString() +
                ", origQty=" + origQty.toPlainString() +
                ", executedQty=" + executedQty.toPlainString() +
                ", cummulativeQuoteQty=" + cummulativeQuoteQty.toPlainString() +
                ", status=" + status +
                ", side=" + side +
                ", time=" + time +
                ", updateTime=" + updateTime +
                '}';
    }
}
