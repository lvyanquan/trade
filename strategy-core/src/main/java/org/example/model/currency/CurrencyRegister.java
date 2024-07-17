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

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

public class CurrencyRegister {
    protected static final HashMap<String, Currency> CURRENCY_HASH_MAP = new HashMap<>(500);


    public static Collection<Currency> currencys(){
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
