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

package org.example.core.strategy;

public class GridOrder {
    private double feeRate = 0.00075;
    //触发价格买入
    private double price;
    private double amount;
    private double lowPrice;
    //这是第几个单子
    private int sequnce;
    //下单成交后的数量
    private double quantity;
    //理论每个单子的成交额

    //0挂单 1 成交
    private int status;

    public GridOrder(int sequnce, double feeRate,double amount) {
        this.sequnce = sequnce;
        this.feeRate = feeRate;
        this.amount = amount;
    }

    public GridOrder(int sequnce) {
        this.sequnce = sequnce;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setPriceAndCalcuteLowPrice(double price) {
        this.price = price;
        this.lowPrice = calculateBreakEvenSellPrice(amount);
    }

    //下单了 就直接更新
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    //回调，对这个订单进行更新
    public void setStatus(int status) {
        this.status = status;
    }

    public double getPrice() {
        return price;
    }

    public double getQuantity() {
        return quantity;
    }

    //如果可以卖出的话，价格就是 price + atr计算出来的
    public boolean canSell() {
        return quantity > 0 && status == 1;
    }

    public boolean canBuy() {
        return quantity <= 0;
    }

    public int getSequnce() {
        return sequnce;
    }

    public double getLowPrice() {
        return lowPrice;
    }

    /**
     * 计算不亏本的最低卖出价
     *
     * @param totalBuyAmount 买入金额，不含手续费
     * @return 最低卖出价
     */
    public double calculateBreakEvenSellPrice(double totalBuyAmount) {
        // 计算买入数量：总买入金额 / 每单位买入价
        double quantity = totalBuyAmount / price;

        // 计算买入时的实际成本（包括手续费）
        double totalBuyCost = totalBuyAmount * (1 + feeRate);

        // 目标利润，等于买入成本的手续费
        double targetProfit = totalBuyCost * feeRate * 4;

        // 目标收入，既要覆盖成本，又要包含目标利润
        double targetRevenue = totalBuyCost + targetProfit;

        // 计算不亏本且利润为手续费相同的最低卖出价
        double sellPrice = targetRevenue / (quantity * (1 - feeRate));

        return sellPrice;
    }

    @Override
    public String toString() {
        return "GridOrder{" +
                "price=" + price +
                ", sequnce=" + sequnce +
                ", quantity=" + quantity +
                ", status=" + status +
                '}';
    }
}
