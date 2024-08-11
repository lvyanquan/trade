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

import java.util.List;
import java.util.function.Consumer;

public class MultipleSource extends KlineSource {
    private final List<KlineSource> klineSources;

    public MultipleSource(List<KlineSource> klineSources) {
        super(klineSources.get(0).symbol, klineSources.get(0).tradeType, klineSources.get(0).interval);
        this.klineSources = klineSources;
    }




    @Override
    public void run() {
        klineSources.forEach(KlineSource::run);
    }


    @Override
    public void close() {
        klineSources.forEach(KlineSource::close);
    }

    @Override
    public void registerConsumer(Consumer<Bar> consumer) {

    }

    @Override
    public void processData(List<Bar> bars) {
    }

    @Override
    public void processData(Bar t) {

    }

    @Override
    public void startPosition(long startTime) {

    }

    @Override
    public void endPosition(long endTime) {

    }

    @Override
    public void open() {

    }

    @Override
    public void readData() {

    }
}
