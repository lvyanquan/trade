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

package org.example.core.bar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * //后期可以注册多个symbol。handler注册的时候，需要绑定对应的symbol
 */
public class BarEngineBuilder<V> {

    private List<SymbolDescribe> symbols = new ArrayList<>(8);

    private String exchange;

    private int window;
    private int skipWindowData;

    private Function<Bar, V> convert;

    public BarEngineBuilder() {

    }

    private Map<SymbolDescribe, List<BarPipeline.BarHandler<V>>> handlers = new HashMap<>();

    //入参是哪个交易所
    public BarEngineBuilder<V> exchange(String exchange) {
        this.exchange = exchange;
        return this;
    }


    public BarEngineBuilder<V> skipWindowData(int skipWindowData) {
        this.skipWindowData = skipWindowData;
        return this;
    }


    public BarEngineBuilder<V> window(int window) {
        this.window = window;
        return this;
    }


    public BarEngineBuilder<V> subscribe(SymbolDescribe symbol) {
        this.symbols.add(symbol);
        return this;
    }


    public BarEngineBuilder<V> convert(Function<Bar, V> convert) {
        this.convert = convert;
        return this;
    }


    public BarEngineBuilder<V> addHandler(SymbolDescribe symbolDescribe, BarPipeline.BarHandler<V> pipeline) {
        List<BarPipeline.BarHandler<V>> barHandlers = handlers.computeIfAbsent(symbolDescribe, k -> new ArrayList<>());
        barHandlers.add(pipeline);
        return this;
    }

    public KlineSource build() {
        if (symbols.isEmpty()) {
            throw new IllegalArgumentException("no symbol to subscribe");
        }

        if (symbols.size() == 1) {
            return buildSource(symbols.get(0));
        }
        ArrayList<KlineSource> sources = new ArrayList<>();
        for (SymbolDescribe symbol : symbols) {
            sources.add(buildSource(symbol));
        }
        return new MultipleSource(sources);


    }

    private KlineSource buildSource(SymbolDescribe symbol) {
        if (exchange != null && !exchange.equalsIgnoreCase("binance")) {
            throw new IllegalArgumentException("not support exchange type: " + exchange);
        }

        KlineSource klineSource = new BinanceSource(symbol.getSymbol(), symbol.getTradeType(), symbol.getInterval());
        if (symbol.startTime > 0) {
            klineSource.startPosition(symbol.startTime);
            if (symbol.endTime > 0) {
                klineSource.endPosition(symbol.endTime);
            }
        }
        List<BarPipeline.BarHandler<V>> barHandlers = handlers.get(symbol);
        if (barHandlers != null) {
            BarPipeline<V> barPipeline = new BarPipeline<V>(barHandlers, window, symbol.getInterval(), convert, skipWindowData);
            barPipeline.init();
            klineSource.registerConsumer(barPipeline);
        } else {
            throw new IllegalArgumentException("no handler for symbol: " + symbol.getSymbol());
        }
        return klineSource;
    }


    public static class SymbolDescribe {
        private String symbol;
        private TradeType tradeType;
        private KlineInterval interval;
        private long startTime;
        private long endTime;

        public SymbolDescribe(String symbol, TradeType tradeType, KlineInterval interval) {
            this(symbol, tradeType, interval, -1, -1);
        }

        public SymbolDescribe(String symbol, TradeType tradeType, KlineInterval interval, long startTime, long endTime) {
            this.symbol = symbol;
            this.tradeType = tradeType;
            this.interval = interval;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public SymbolDescribe of(String symbol) {
            return new SymbolDescribe(symbol, tradeType, interval, startTime, endTime);
        }

        public String getSymbol() {
            return symbol;
        }

        public TradeType getTradeType() {
            return tradeType;
        }

        public KlineInterval getInterval() {
            return interval;
        }
    }

}
