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

package org.example.model.currency;

import java.math.BigDecimal;
import java.math.RoundingMode;

public interface Currency {

    String symbol();

    String baseAsset();

    String quoteAsset();

    //订单数量精度,如果 quantityPrecision 为 0，则表示交易数量只能是整数。
    public int getQuantityPrecision();

    // 订单价格精度,如果 pricePrecision 为 2，则表示交易价格可以有两位小数。
    public int getPricePrecision();

    default BigDecimal quanlityConvent(BigDecimal quanlity) {
        return quanlity.setScale(getQuantityPrecision(), RoundingMode.DOWN);
    }

    default double quanlityConvent(double quanlity) {
        return BigDecimal.valueOf(quanlity).setScale(getQuantityPrecision(), RoundingMode.DOWN).doubleValue();
    }

    default double priceConvent(double price) {
        return BigDecimal.valueOf(price).setScale(getPricePrecision(), RoundingMode.DOWN).doubleValue();
    }
}
