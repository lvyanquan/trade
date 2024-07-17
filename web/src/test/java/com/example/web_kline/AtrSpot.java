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

package com.example.web_kline;

import org.example.Position.PositionManagementStrategy;
import org.example.StrateFactory;
import org.example.binance.factory.KlineFactory;
import org.example.kline.KlineClient;
import org.example.model.currency.Currency;
import org.example.model.enums.Server;
import org.example.model.enums.ContractType;
import org.example.model.market.KlineModule;
import org.example.strategy.CollectRuleStrategy;
import org.example.util.JsonUtil;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.num.DoubleNum;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class AtrSpot extends Spt {

    //没进行一次购买 就把止盈的价格放进去即可
    protected List<Double> sellPrice;


    private String interval;

    private BarSeries barSeries;
    private final KlineClient binanceKlineClient = KlineFactory.Create(Server.BINANCE, ContractType.SPOT);


    protected BaseStrategy tradeStrategy;


    public AtrSpot(Currency currency, String interval, PositionManagementStrategy positionManagementStrategy, StrateFactory baseStrategy) {
        super("atr_01", currency, ContractType.SPOT, positionManagementStrategy);
        this.sellPrice = new ArrayList<>();
        this.currency = currency;

        this.interval = interval;

        barSeries = new BaseBarSeries("mySeries", DoubleNum::valueOf);
        this.tradeStrategy = baseStrategy.buildStrategy(barSeries);
        for (KlineModule t : binanceKlineClient.getHistoryKlineData(currency, interval, System.currentTimeMillis() - 5 * 60 * 1000 * 100, System.currentTimeMillis() - 5 * 60 * 1000)) {
            Bar newBar = BaseBar.builder(DoubleNum::valueOf, Double.class)
                    .timePeriod(Duration.ofMinutes(1))
                    .endTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(t.getEndTime()), ZoneId.systemDefault()))
                    .openPrice(t.getOpen())
                    .highPrice(t.getHigh())
                    .lowPrice(t.getLow())
                    .closePrice(t.getClose())
                    .volume(t.getQuantity())
                    .build();
            barSeries.addBar(newBar);
        }
    }

    public void trade() {
        binanceKlineClient.handlerStreamingKlineData(currency, interval, t -> {
            barSeries.addBar(conventKlineToBar(t), true);

                if (tradeStrategy.shouldEnter(barSeries.getEndIndex())) {
                    buyOrder(t.getLow());
                }
               if( tradeStrategy instanceof CollectRuleStrategy){
                  System.out.println("入场规则 " + JsonUtil.toJson( ((CollectRuleStrategy)tradeStrategy).getEnterEffectiveRuleName()));
               }

                if (tradeStrategy.shouldExit(barSeries.getEndIndex())) {
                    sellOrder(t.getHigh());
                }

                if( tradeStrategy instanceof CollectRuleStrategy){
                    System.out.println("离场规则 " + JsonUtil.toJson( ((CollectRuleStrategy)tradeStrategy).getExitEffectiveRuleName()));
                }

        }, t -> {
            Bar bar = conventKlineToBar(t);
            try {
                barSeries.addBar(bar);
            } catch (IllegalArgumentException e) {
                barSeries.addBar(bar, true);
            }
        });
    }




    protected Bar conventKlineToBar(KlineModule t) {
        return BaseBar.builder(DoubleNum::valueOf, Double.class)
                .timePeriod(Duration.ofMinutes(1))
                .endTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(t.getEndTime()), ZoneId.systemDefault()))
                .openPrice(t.getOpen())
                .highPrice(t.getHigh())
                .lowPrice(t.getLow())
                .closePrice(t.getClose())
                .volume(t.getQuantity())
                .build();
    }
}
