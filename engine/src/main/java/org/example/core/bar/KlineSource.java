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

import org.example.core.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class KlineSource implements Source<Bar> {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());
    protected final String symbol;

    protected final TradeType tradeType;

    protected final KlineInterval interval;

    protected List<Consumer<Bar>> consumers = new ArrayList<>(8);


    protected long startTime;
    protected long endTime;

    public KlineSource(String symbol, TradeType tradeType, KlineInterval interval) {
        this.symbol = symbol;
        this.tradeType = tradeType;
        this.interval = interval;
    }


    protected abstract void open();

    protected abstract void readData();

    @Override
    public void run() {
        LOG.info("Source [{}] start to open", symbol);
        open();
        LOG.info("Source [{}] open end", symbol);

        LOG.info("Source [{}] start to readData", symbol);
        readData();
    }


    @Override
    public void registerConsumer(Consumer<Bar> consumer) {
        consumers.add(consumer);
    }

    public void processData(List<Bar> bars) {
        for (Bar t : bars) {
            processData(t);
        }
    }


    public void processData(Bar t) {
        consumers.forEach(i -> i.accept(t));
    }


    public void startPosition(long startTime) {
        this.startTime = startTime;
    }

    public void endPosition(long endTime) {
        this.endTime = endTime;
    }

}
