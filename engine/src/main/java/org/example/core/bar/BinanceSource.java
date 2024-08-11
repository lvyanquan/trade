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

import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.client.impl.WebsocketClientImpl;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.binance.connector.futures.client.impl.UMWebsocketClientImpl;
import org.apache.commons.collections.CollectionUtils;
import org.example.core.bar.util.BinanceKlineJsonParse;
import org.example.core.bar.util.SpotKlineUtil;
import org.example.core.bar.util.UmfutureKlineUtil;
import org.example.core.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BinanceSource extends KlineSource {
    private static final Logger LOG = LoggerFactory.getLogger(BinanceSource.class);

    private UMWebsocketClientImpl umWebsocketClient;
    private WebsocketClientImpl websocketClient;

    private int connectionId;

    public BinanceSource(String symbol, TradeType tradeType, KlineInterval interval) {
        super(symbol, tradeType, interval);
        if (tradeType == TradeType.USDT_MARGINED_CONTRACT && interval == KlineInterval.ONE_SECOND && endTime <= 0) {
            //u本位合约websocket实时数据最快也只支持分钟级别的，不支持秒级
            throw new IllegalArgumentException("USDT_MARGINED_CONTRACT streaming data not support ONE_SECOND interval");
        }
    }

    @Override
    public void open() {

    }


    @Override
    public void readData() {
        //执行完 org.example.binance.kline.UmFutureHttpCLient.klines
        //实时和离线是一起执行的，但是实时读取数据的时候，会有一个过滤操作，会等将离线任务的endtime为起止时间的数据存在一个临时内存里，然后离线数据结束之后，发送给下游
        if (tradeType == TradeType.SPOT) {
            List<Bar> bars = null;
            if (startTime > 0) {
                SpotClientImpl spotClient = new SpotClientImpl();
                //结束时间为空 则先获取当前时间作为截止时间
                long endTime = this.endTime > 0 ? this.endTime : System.currentTimeMillis();
                boolean isFirst = true;
                do {
                    long limit = 1000;
                    bars = SpotKlineUtil.klines(spotClient, symbol, interval, startTime, endTime, limit);
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        processData(bars);
                    }
                    while (CollectionUtils.isNotEmpty(bars) && bars.size() == limit) {
                        processData(bars);
                        Long startTime = DateUtil.convent(bars.get(bars.size() - 1).getEndTime());
                        bars.clear();
                        bars = SpotKlineUtil.klines(spotClient, symbol, interval, startTime, endTime, limit);
                    }

                } while (this.endTime <= 0 && DateUtil.convent(interval.getNextTimePoint(DateUtil.convent(endTime), 1)) <= System.currentTimeMillis());
            }


            if (endTime <= 0) {
                LOG.info("Source [{}] start read streamIng data", symbol);

                List<Bar> finalBars = bars;
                websocketClient = new WebsocketClientImpl();
                streamData(websocketClient, finalBars, symbol, interval);
            } else if (CollectionUtils.isNotEmpty(bars)) {
                processData(bars);
            }
        } else if (tradeType == TradeType.USDT_MARGINED_CONTRACT) {
            List<Bar> bars = null;
            if (startTime > 0) {
                UMFuturesClientImpl umFuturesClient = new UMFuturesClientImpl();
                //结束时间为空 则先获取当前时间作为截止时间
                long endTime = this.endTime > 0 ? this.endTime : System.currentTimeMillis();
                boolean isFirst = true;
                do {
                    long limit = 1000;
                    bars = UmfutureKlineUtil.klines(umFuturesClient, symbol, interval, startTime, endTime, limit);
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        processData(bars);
                    }
                    while (CollectionUtils.isNotEmpty(bars) && bars.size() == limit) {
                        processData(bars);
                        Long startTime = DateUtil.convent(bars.get(bars.size() - 1).getEndTime());
                        bars.clear();
                        bars = UmfutureKlineUtil.klines(umFuturesClient, symbol, interval, startTime, endTime, limit);
                    }

                } while (this.endTime <= 0 && DateUtil.convent(interval.getNextTimePoint(DateUtil.convent(endTime), 1)) <= System.currentTimeMillis());
            }


            if (endTime <= 0) {
                LOG.info("start streaming data");
                umWebsocketClient = new UMWebsocketClientImpl();
                streamUmData(umWebsocketClient, bars, symbol, interval);
            } else if (CollectionUtils.isNotEmpty(bars)) {
                processData(bars);
            }

        } else {
            throw new IllegalArgumentException(tradeType + " not supported");
        }


    }

    public void streamData(WebsocketClientImpl websocketClient, List<Bar> finalBars, String symbol, KlineInterval interval) {

        connectionId = websocketClient.klineStream(symbol, interval.getInterval(), msg -> {
                    LOG.info("Source [{}] connect receive open msg.", symbol);
                },
                (kline) -> {
                    if (CollectionUtils.isNotEmpty(finalBars)) {
                        processData(finalBars);
                        finalBars.clear();
                    }
                    processData(BinanceKlineJsonParse.parseStreamKline(kline, interval));
                }
                , msg -> {},
                msg -> {
                    LOG.info("Source [{}] connect error, retry...", symbol);
                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException e) {
//                          throw new RuntimeException(e);
                    }
                    websocketClient.closeConnection(connectionId);
                    this.streamData(websocketClient, finalBars, symbol, interval);
                });
    }

    public void streamUmData(UMWebsocketClientImpl umWebsocketClient, List<Bar> finalBars, String symbol, KlineInterval interval) {

        connectionId = umWebsocketClient.klineStream(symbol, interval.getInterval(), msg -> {
                }, (kline) -> {
                    if (CollectionUtils.isNotEmpty(finalBars)) {
                        processData(finalBars);
                        finalBars.clear();
                    }
                    processData(BinanceKlineJsonParse.parseStreamKline(kline, interval));
                }
                , msg ->{},
                msg -> {
                    LOG.info("Source [{}] connect error, retry...", symbol);
                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException e) {
//                          throw new RuntimeException(e);
                    }
                    umWebsocketClient.closeConnection(connectionId);
                    this.streamUmData(umWebsocketClient, finalBars, symbol, interval);
                });
    }

    @Override
    public void close() {
        if (umWebsocketClient != null) {
            umWebsocketClient.closeAllConnections();
        }
        if (websocketClient != null) {
            websocketClient.closeAllConnections();
        }
    }
}
