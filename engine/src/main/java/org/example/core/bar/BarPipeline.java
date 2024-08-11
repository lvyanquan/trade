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

import org.example.core.util.DateUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class BarPipeline<V> implements Consumer<Bar> {
    private List<BarHandler<V>> handlers;
    private int windoSize;
    private KlineInterval interval;
    private Function<Bar, V> converter;

    private int skipWindowData;
    private int skipNum;

    private LocalDateTime cureentHighWindowTime;
    private Bar mergeBar;
    private Bar lastBar;

    private boolean init = false;

    public BarPipeline(List<BarHandler<V>> handlers, int windoSize, KlineInterval interval, Function<Bar, V> converter, int skipWindowData) {
        this.handlers = handlers;
        this.windoSize = windoSize;
        this.interval = interval;
        this.converter = converter;
        this.skipWindowData = skipWindowData;
    }

    public void init() {
        if (!init) {
            for (BarHandler<V> handler : handlers) {
                handler.open();
            }
            init = true;
        }
    }

    @Override
    public void accept(Bar bar) {


        applyData(bar, false);

        if (windoSize > 0) {
            if (cureentHighWindowTime == null) {
                LocalDateTime[] timeWindow = interval.getTimeWindow(DateUtil.convent(bar.getEndTime()), windoSize);
                cureentHighWindowTime = timeWindow[1];
            }
            if (DateUtil.convent(bar.getEndTime()) - (DateUtil.convent(cureentHighWindowTime)) <= 0) {
                if (lastBar == null || lastBar.getEndTime().equals(bar.getEndTime())) {
                    lastBar = bar;
                } else {
                    if (mergeBar == null) {
                        mergeBar = lastBar;
                    } else {
                        mergeBar = mergeBar.merge(lastBar);
                    }
                    lastBar = bar;
                }
            } else {
                if (mergeBar == null) {
                    mergeBar = lastBar;
                } else {
                    mergeBar = mergeBar.merge(lastBar);
                }
                if (skipWindowData <= 0 || skipNum++ >= skipWindowData) {
                    applyData(mergeBar, true);
                }

                lastBar = bar;
                mergeBar = null;
                LocalDateTime[] timeWindow = interval.getTimeWindow(DateUtil.convent(bar.getEndTime()), windoSize);
                cureentHighWindowTime = timeWindow[1];
            }
        }
    }

    private void applyData(Bar bar, boolean windowBar) {
        if (converter != null) {
            V v = converter.apply(bar);
            for (BarHandler<V> handler : handlers) {
                if (windowBar) {
                    handler.applyWindow(v);
                } else {
                    handler.apply(v);
                }
            }
        } else {
            for (BarHandler<V> handler : handlers) {
                if (windowBar) {
                    handler.applyWindow((V) bar);
                } else {
                    handler.apply((V) bar);
                }
            }
        }
    }

    public  interface BarHandler<V> {
        default void open() {
        }

        ;

        default void apply(V bar) {
        }

        ;

        default void applyWindow(V bar) {
        }
    }
}
