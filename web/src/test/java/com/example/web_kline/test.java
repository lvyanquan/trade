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

import org.example.data.PriceBean;
import org.example.indicators.SuperTrendIndicator2;
import org.example.indicators.SuperTrendLowerBandIndicator;
import org.example.indicators.SuperTrendUpperBandIndicator;
import org.example.indicators.SupertrendIndicator;
import org.example.util.JsonUtil;
import org.example.util.KlineUtil;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.aroon.AroonFacade;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class test {
    public static void main(String[] args) {

//        String data = "[{\"orderId\":200712223097,\"symbol\":\"BTCUSDT\",\"status\":\"FILLED\",\"clientOrderId\":\"ios_2HmsU1U4niUUCNC7Ym1b\",\"price\":\"29525\",\"avgPrice\":\"29525.00000\",\"origQty\":\"0.033\",\"executedQty\":\"0.033\",\"cumQuote\":\"974.32500\",\"timeInForce\":\"GTC\",\"type\":\"LIMIT\",\"reduceOnly\":true,\"closePosition\":false,\"side\":\"BUY\",\"positionSide\":\"SHORT\",\"stopPrice\":\"0\",\"workingType\":\"CONTRACT_PRICE\",\"priceMatch\":\"NONE\",\"selfTradePreventionMode\":\"NONE\",\"goodTillDate\":0,\"priceProtect\":false,\"origType\":\"LIMIT\",\"time\":1697841766495,\"updateTime\":1697853240572},{\"orderId\":200712246522,\"symbol\":\"BTCUSDT\",\"status\":\"FILLED\",\"clientOrderId\":\"ios_qL3tzoWTO6QHfBn005qr\",\"price\":\"29725\",\"avgPrice\":\"29725.00000\",\"origQty\":\"0.020\",\"executedQty\":\"0.020\",\"cumQuote\":\"594.50000\",\"timeInForce\":\"GTC\",\"type\":\"LIMIT\",\"reduceOnly\":true,\"closePosition\":false,\"side\":\"SELL\",\"positionSide\":\"LONG\",\"stopPrice\":\"0\",\"workingType\":\"CONTRACT_PRICE\",\"priceMatch\":\"NONE\",\"selfTradePreventionMode\":\"NONE\",\"goodTillDate\":0,\"priceProtect\":false,\"origType\":\"LIMIT\",\"time\":1697841784399,\"updateTime\":1697843010827},{\"orderId\":200720856660,\"symbol\":\"BTCUSDT\",\"status\":\"EXPIRED\",\"clientOrderId\":\"ios_UNATisfni69By1i7SRgA\",\"price\":\"29845\",\"avgPrice\":\"0.00000\",\"origQty\":\"0.020\",\"executedQty\":\"0\",\"cumQuote\":\"0\",\"timeInForce\":\"GTC\",\"type\":\"LIMIT\",\"reduceOnly\":true,\"closePosition\":false,\"side\":\"SELL\",\"positionSide\":\"LONG\",\"stopPrice\":\"0\",\"workingType\":\"CONTRACT_PRICE\",\"priceMatch\":\"NONE\",\"selfTradePreventionMode\":\"NONE\",\"goodTillDate\":0,\"priceProtect\":false,\"origType\":\"LIMIT\",\"time\":1697843836870,\"updateTime\":1697846459578},{\"orderId\":200720953957,\"symbol\":\"BTCUSDT\",\"status\":\"FILLED\",\"clientOrderId\":\"ios_st_au6DnGEK0QpkWcR\",\"price\":\"29608\",\"avgPrice\":\"29608.00000\",\"origQty\":\"0.027\",\"executedQty\":\"0.027\",\"cumQuote\":\"799.41600\",\"timeInForce\":\"GTC\",\"type\":\"LIMIT\",\"reduceOnly\":false,\"closePosition\":false,\"side\":\"BUY\",\"positionSide\":\"LONG\",\"stopPrice\":\"0\",\"workingType\":\"CONTRACT_PRICE\",\"priceMatch\":\"NONE\",\"selfTradePreventionMode\":\"NONE\",\"goodTillDate\":0,\"priceProtect\":false,\"origType\":\"LIMIT\",\"time\":1697843856132,\"updateTime\":1697849088283},{\"orderId\":200720997409,\"symbol\":\"BTCUSDT\",\"status\":\"CANCELED\",\"clientOrderId\":\"ios_st_0MC1qBqoXmQjx80\",\"price\":\"29515\",\"avgPrice\":\"0.00000\",\"origQty\":\"0.016\",\"executedQty\":\"0\",\"cumQuote\":\"0\",\"timeInForce\":\"GTC\",\"type\":\"LIMIT\",\"reduceOnly\":false,\"closePosition\":false,\"side\":\"BUY\",\"positionSide\":\"LONG\",\"stopPrice\":\"0\",\"workingType\":\"CONTRACT_PRICE\",\"priceMatch\":\"NONE\",\"selfTradePreventionMode\":\"NONE\",\"goodTillDate\":0,\"priceProtect\":false,\"origType\":\"LIMIT\",\"time\":1697843864987,\"updateTime\":1697851917660},{\"orderId\":200729733661,\"symbol\":\"BTCUSDT\",\"status\":\"FILLED\",\"clientOrderId\":\"ios_oHaXtj9o6YdJuzj9W9do\",\"price\":\"29678\",\"avgPrice\":\"29678.00000\",\"origQty\":\"0.020\",\"executedQty\":\"0.020\",\"cumQuote\":\"593.56000\",\"timeInForce\":\"GTC\",\"type\":\"LIMIT\",\"reduceOnly\":true,\"closePosition\":false,\"side\":\"SELL\",\"positionSide\":\"LONG\",\"stopPrice\":\"0\",\"workingType\":\"CONTRACT_PRICE\",\"priceMatch\":\"NONE\",\"selfTradePreventionMode\":\"NONE\",\"goodTillDate\":0,\"priceProtect\":false,\"origType\":\"LIMIT\",\"time\":1697846459577,\"updateTime\":1697846952460},{\"orderId\":200737141039,\"symbol\":\"BTCUSDT\",\"status\":\"CANCELED\",\"clientOrderId\":\"ios_st_XZivU5PEMkbG2Zr\",\"price\":\"0\",\"avgPrice\":\"0.00000\",\"origQty\":\"0.027\",\"executedQty\":\"0\",\"cumQuote\":\"0\",\"timeInForce\":\"GTE_GTC\",\"type\":\"TAKE_PROFIT_MARKET\",\"reduceOnly\":true,\"closePosition\":false,\"side\":\"SELL\",\"positionSide\":\"LONG\",\"stopPrice\":\"29785\",\"workingType\":\"MARK_PRICE\",\"priceMatch\":\"NONE\",\"selfTradePreventionMode\":\"NONE\",\"goodTillDate\":0,\"priceProtect\":true,\"origType\":\"TAKE_PROFIT_MARKET\",\"time\":1697849088307,\"updateTime\":1697874330779},{\"orderId\":200744066247,\"symbol\":\"BTCUSDT\",\"status\":\"FILLED\",\"clientOrderId\":\"ios_PTVrxuEcpBSP8ZICiKoG\",\"price\":\"29656\",\"avgPrice\":\"29656.00000\",\"origQty\":\"0.027\",\"executedQty\":\"0.027\",\"cumQuote\":\"800.71200\",\"timeInForce\":\"GTC\",\"type\":\"LIMIT\",\"reduceOnly\":true,\"closePosition\":false,\"side\":\"SELL\",\"positionSide\":\"LONG\",\"stopPrice\":\"0\",\"workingType\":\"CONTRACT_PRICE\",\"priceMatch\":\"NONE\",\"selfTradePreventionMode\":\"NONE\",\"goodTillDate\":0,\"priceProtect\":false,\"origType\":\"LIMIT\",\"time\":1697851945752,\"updateTime\":1697869759045},{\"orderId\":200752137556,\"symbol\":\"BTCUSDT\",\"status\":\"FILLED\",\"clientOrderId\":\"ios_st_FHzCbh6KUUHbljm\",\"price\":\"29585\",\"avgPrice\":\"29585.00000\",\"origQty\":\"0.027\",\"executedQty\":\"0.027\",\"cumQuote\":\"798.79500\",\"timeInForce\":\"GTC\",\"type\":\"LIMIT\",\"reduceOnly\":false,\"closePosition\":false,\"side\":\"SELL\",\"positionSide\":\"SHORT\",\"stopPrice\":\"0\",\"workingType\":\"CONTRACT_PRICE\",\"priceMatch\":\"NONE\",\"selfTradePreventionMode\":\"NONE\",\"goodTillDate\":0,\"priceProtect\":false,\"origType\":\"LIMIT\",\"time\":1697854231627,\"updateTime\":1697859380747},{\"orderId\":200768470586,\"symbol\":\"BTCUSDT\",\"status\":\"NEW\",\"clientOrderId\":\"ios_st_siaaP3ldLQ3cCyy\",\"price\":\"0\",\"avgPrice\":\"0.00000\",\"origQty\":\"0.027\",\"executedQty\":\"0\",\"cumQuote\":\"0\",\"timeInForce\":\"GTE_GTC\",\"type\":\"TAKE_PROFIT_MARKET\",\"reduceOnly\":true,\"closePosition\":false,\"side\":\"BUY\",\"positionSide\":\"SHORT\",\"stopPrice\":\"29425\",\"workingType\":\"MARK_PRICE\",\"priceMatch\":\"NONE\",\"selfTradePreventionMode\":\"NONE\",\"goodTillDate\":0,\"priceProtect\":true,\"origType\":\"TAKE_PROFIT_MARKET\",\"time\":1697859380784,\"updateTime\":1697859380784}]";
//
//        List<Map<String,Object>> datas = JsonUtil.parse(data, List.class);
//        ArrayList<Long> longs = new ArrayList<>();
//        ArrayList<Long> longs2 = new ArrayList<>();
//        datas.forEach(i->{
//            //time 根据这个时间设置的
//            longs.add((Long) i.get("time"));
//            longs2.add((Long) i.get("updateTime"));
//        });

        BaseBarSeries series = new BaseBarSeriesBuilder().withNumTypeOf(DoubleNum::valueOf).withName("Aroon data").build();
        final AroonFacade facade = new AroonFacade(series, 5);

        int atrPeriod = 9;

        // Define the Supertrend indicator using ATR
        Indicator<Num> supertrendUpIndicator = new SupertrendIndicator(
                series,
                atrPeriod,
                3d,
                true// Multiplier
        );

        Indicator<Num> supertrendDnIndicator = new SupertrendIndicator(
                series,
                atrPeriod,
                3d,
                false// Multiplier
        );


        ATRIndicator atrIndicator = new ATRIndicator(series, atrPeriod);
        SuperTrendUpperBandIndicator superTrendUpperBandIndicator = new SuperTrendUpperBandIndicator(series, atrIndicator, 3d);
        SuperTrendLowerBandIndicator superTrendLowerBandIndicator = new SuperTrendLowerBandIndicator(series, atrIndicator, 3d);

        SuperTrendIndicator2 superTrendIndicator2 = new SuperTrendIndicator2(series,atrPeriod, 3d);

        long e = System.currentTimeMillis();
        long s = e - (600 * 60 * 1000);
        int i = 0;
        for (PriceBean priceBean : KlineUtil.getBar("BTCUSDT", "5m", s, e)) {

            Bar newBar = BaseBar.builder(DoubleNum::valueOf, Double.class)
                    .timePeriod(Duration.ofMinutes(1))
                    .endTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(priceBean.getEndTime()), ZoneId.systemDefault()))
                    .openPrice(priceBean.getOpen().doubleValue())
                    .highPrice(priceBean.getHigh().doubleValue())
                    .lowPrice(priceBean.getLow().doubleValue())
                    .closePrice(priceBean.getClose().doubleValue())
                    .volume(priceBean.getVolume().doubleValue())
                    .build();
            series.addBar(newBar);

            NumericIndicator oscillator = facade.oscillator();
            Num value = superTrendIndicator2.getValue(i);
//            System.out.println(oscillator.getValue(i));
//            System.out.println("superTrendIndicator2: "+value);

            //上升趋势
            if(priceBean.getClose().doubleValue() - value.doubleValue() >0 && oscillator.getValue(i).doubleValue()>0){
                System.out.println("--大于-" +( priceBean.getClose().doubleValue() - value.doubleValue()) + " "+oscillator.getValue(i));
                System.out.println(priceBean.getClose().doubleValue() + " " + superTrendUpperBandIndicator.getValue(i) + "---up---" + supertrendUpIndicator.getValue(i));
                System.out.println(priceBean.getClose().doubleValue() + "  "+superTrendLowerBandIndicator.getValue(i) + "---down---" + supertrendDnIndicator.getValue(i));
           //下降趋势
            }else if (priceBean.getClose().doubleValue() - value.doubleValue() <0 && oscillator.getValue(i).doubleValue()<0){
                System.out.println("--小于-"+( priceBean.getClose().doubleValue() - value.doubleValue()) + " "+oscillator.getValue(i));
                System.out.println(priceBean.getClose().doubleValue() + " " + superTrendUpperBandIndicator.getValue(i) + "---up---" + supertrendUpIndicator.getValue(i));
                System.out.println(priceBean.getClose().doubleValue() + "  "+superTrendLowerBandIndicator.getValue(i) + "---down---" + supertrendDnIndicator.getValue(i));
            }else{
                //中性
            }
            i++;
        }
        //4小时级别多还是空
        //1 小时级别多还是空    veryggod入场 good 加上4小时级别相同入场
        //1分钟k线入场 信号

    }
}
