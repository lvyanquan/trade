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

package org.example.data.currency;

public class BaseCurrency implements Currency {


    private String symbol;
    private String baseAsset;
    private String quoteAsset;

    //订单数量精度,如果 quantityPrecision 为 0，则表示交易数量只能是整数。
    private int quantityPrecision;

    // 订单价格精度,如果 pricePrecision 为 2，则表示交易价格可以有两位小数。
    private int pricePrecision;

    public BaseCurrency(String symbol, String baseAsset, String quoteAsset, int quantityPrecision, int pricePrecision) {
        this.symbol = symbol;
        this.baseAsset = baseAsset;
        this.quoteAsset = quoteAsset;
        this.quantityPrecision = quantityPrecision;
        this.pricePrecision = pricePrecision;
    }

    public BaseCurrency(String baseAsset, String quoteAsset, int quantityPrecision, int pricePrecision) {
        this.baseAsset = baseAsset;
        this.quoteAsset = quoteAsset;
        this.symbol = baseAsset + quoteAsset;
        this.quantityPrecision = quantityPrecision;
        this.pricePrecision = pricePrecision;
    }

    private BaseCurrency(String symbol, int quantityPrecision, int pricePrecision) {
        this.symbol = symbol;
        this.quantityPrecision = quantityPrecision;
        this.pricePrecision = pricePrecision;
    }

    public static BaseCurrency of(String symbol, int quantityPrecision, int pricePrecision) {
        return new BaseCurrency(symbol, quantityPrecision, pricePrecision);
    }

    @Override
    public String symbol() {
        return symbol;
    }

    @Override
    public String baseAsset() {
        return baseAsset;
    }

    @Override
    public String quoteAsset() {
        return quoteAsset;
    }


    @Override
    public int getQuantityPrecision() {
        return quantityPrecision;
    }

    @Override
    public int getPricePrecision() {
        return pricePrecision;
    }

    @Override
    public String toString() {
        return "BaseCurrency{" +
                "symbol='" + symbol + '\'' +
                '}';
    }
}
