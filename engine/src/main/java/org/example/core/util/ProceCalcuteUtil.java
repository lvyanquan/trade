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

package org.example.core.util;

public class ProceCalcuteUtil {

    /**
     * 计算不亏本的最低卖出价
     *
     * @param totalBuyAmount 买入金额，不含手续费
     * @return 最低卖出价
     */
    public static double calculateBreakEvenSellPrice(double feeRate, double quantity, double totalBuyAmount,int feeRateMultity) {
        // 计算买入时的实际成本（包括手续费）
        double totalBuyCost = totalBuyAmount * (1 + feeRate);

        // 目标利润，等于买入成本的手续费
        double targetProfit = totalBuyCost * feeRate * feeRateMultity;

        // 目标收入，既要覆盖成本，又要包含目标利润
        double targetRevenue = totalBuyCost + targetProfit;

        // 计算不亏本且利润为手续费相同的最低卖出价
        double sellPrice = targetRevenue / (quantity * (1 - feeRate));

        return sellPrice;
    }
}
