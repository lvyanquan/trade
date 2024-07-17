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

package org.example.binance;

import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import org.example.binance.kline.BinanceClientFactory;
import org.example.model.currency.BaseCurrency;
import org.example.model.currency.Currency;
import org.example.model.currency.CurrencyRegister;
import org.example.util.JsonUtil;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BinanceCurrencyRegister extends CurrencyRegister {
    private static final SpotClientImpl client = BinanceClientFactory.spotHttpClient();


    static {
        String s = client.createMarket().exchangeInfo(new LinkedHashMap<>());
        Map<String, Object> data = JsonUtil.parseForMap(s);

        for (Object symbols : (List) data.get("symbols")) {
            Map<String, Object> symbol = (Map<String, Object>) symbols;
            String symbolName = symbol.get("symbol").toString();
            String status = symbol.get("status").toString();
            int pricePrecision = -1;
            int quantityPrecision = -1;
            if ("TRADING".equals(status)) {
                if (symbol.containsKey("filters")) {
                    for (Map<String, Object> stringObjectMap : (List<Map<String, Object>>) symbol.get("filters")) {
                        if ("LOT_SIZE".equals(stringObjectMap.get("filterType")) && stringObjectMap.containsKey("stepSize")) {
                            BigDecimal bigDecimal = BigDecimal.valueOf(Double.parseDouble(stringObjectMap.get("stepSize").toString()));
                            quantityPrecision = bigDecimal.scale();
                        }

                        if ("PRICE_FILTER".equals(stringObjectMap.get("filterType")) && stringObjectMap.containsKey("tickSize")) {
                            BigDecimal bigDecimal = BigDecimal.valueOf(Double.parseDouble(stringObjectMap.get("tickSize").toString()));
                            pricePrecision = bigDecimal.scale();
                        }
                    }
                }

                if (pricePrecision == -1) {
                    pricePrecision = Integer.valueOf(symbol.get("quotePrecision").toString());
                }
                if (quantityPrecision == -1) {
                    quantityPrecision = Integer.valueOf(symbol.get("baseAssetPrecision").toString());
                }

                if(symbolName.equals("BTCUSDT")){
                    pricePrecision = 2;
                    quantityPrecision = 5;
                }
                String baseAsset = symbol.get("baseAsset").toString();
                String quoteAsset = symbol.get("quoteAsset").toString();

                register(new BaseCurrency(symbolName, baseAsset, quoteAsset, quantityPrecision, pricePrecision));
            }
        }
    }

    public static Collection<Currency> currencys() {
        return CURRENCY_HASH_MAP.values();
    }

    private static void register(Currency currency) {
        CURRENCY_HASH_MAP.put(currency.symbol(), currency);
    }

    public static Optional<Currency> getCurrency(String symbol) {
        return Optional.ofNullable(CURRENCY_HASH_MAP.get(symbol));
    }

    public static boolean supportCurrency(String symbol) {
        return CURRENCY_HASH_MAP.containsKey(symbol);
    }
}
