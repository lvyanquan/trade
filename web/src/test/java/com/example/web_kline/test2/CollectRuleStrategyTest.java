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

package com.example.web_kline.test2;

import org.example.ExitTimeRule;
import org.example.StrateFactory;
import org.example.data.currency.CurrencyRegister;
import org.example.enums.OrderSide;
import org.example.indicators.AtrLowerIndicator;
import org.example.indicators.BopIndicators;
import org.example.indicators.macd.DEAIndicator;
import org.example.rule.AroonRule;
import org.example.rule.AtrOrderPriceRule;
import org.example.rule.BollingerBandsWidthRule;
import org.example.rule.CrossedDownCacheIndicatorRule;
import org.example.rule.CrossedPushCacheIndicatorRule;
import org.example.rule.EmaRule;
import org.example.rule.HightRule;
import org.example.rule.NumericRule;
import org.example.rule.BearishEngulfingRule;
import org.example.rule.BullishHaramiRule;
import org.example.rule.CoefficientWeightRule;
import org.example.strategy.CollectRuleStrategy;
import org.example.rule.WeightRule;
import org.example.rule.WeightRuleWrapper;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.aroon.AroonDownIndicator;
import org.ta4j.core.indicators.aroon.AroonUpIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class CollectRuleStrategyTest {

    public static void main(String[] args) throws InterruptedException {

        StrateFactory strateFactory = new StrateFactory() {
            @Override
            public Strategy buildStrategy(BarSeries series) {
                ArrayList<WeightRule> exitRules = new ArrayList<>();
                ArrayList<WeightRule> enterRules = new ArrayList<>();

                ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
                HighPriceIndicator highPrice = new HighPriceIndicator(series);
                RSIIndicator rsiIndicator = new RSIIndicator(closePrice, 10);//建议是14
                ATRIndicator atrIndicator = new ATRIndicator(series, 10);
                AtrLowerIndicator atrLowerIndicator = new AtrLowerIndicator(series, closePrice, 10);
                //ema 交叉
                MACDIndicator difIndicator = new MACDIndicator(closePrice);
                DEAIndicator deaIndicator = new DEAIndicator(series, difIndicator);


                EMAIndicator eemaIndicator25 = new EMAIndicator(closePrice, 25);
                EMAIndicator emaIndicator7 = new EMAIndicator(closePrice, 7);
                EMAIndicator emaIndicator99 = new EMAIndicator(closePrice, 99);


                WeightRuleWrapper bullishHarami = new WeightRuleWrapper("看涨孕育", 1, new BullishHaramiRule(series));
                CoefficientWeightRule coefficientWeightBearishEngulfingRule = new CoefficientWeightRule("看跌孕育形态", 1, 2, new BearishEngulfingRule(series), new CrossedUpIndicatorRule(rsiIndicator, 70));

                WeightRuleWrapper bopCrossUpWrapper = new WeightRuleWrapper("Bop零轴上穿", 1, new CrossedUpIndicatorRule(new BopIndicators(series), 0));
//                WeightRuleWrapper bopCrossDownWrapper = new WeightRuleWrapper("Bop零轴下穿", 1, new CrossedDownIndicatorRule(new BopIndicators(series), 0));

//                EmaRule emaRule = new EmaRule("ema均线策略",
//                        emaIndicator7,
//                        eemaIndicator25,
//                        emaIndicator99,
//                        closePrice);

                WeightRuleWrapper arooWeightRuleWrapper = new WeightRuleWrapper("阿隆过滤器", 1, new AroonRule(series));

                int barCount = 3;
                final Indicator<Num> sma = new SMAIndicator(closePrice, barCount);

                final BollingerBandsMiddleIndicator middleBB = new BollingerBandsMiddleIndicator(sma);
                final StandardDeviationIndicator standardDeviation = new StandardDeviationIndicator(closePrice,
                        barCount);

                final BollingerBandsLowerIndicator lowerBB = new BollingerBandsLowerIndicator(middleBB, standardDeviation);
                final BollingerBandsUpperIndicator upperBB = new BollingerBandsUpperIndicator(middleBB, standardDeviation);

                WeightRuleWrapper rsiEnterWeightRuleWrapper = new WeightRuleWrapper("rsi低于20", 1, new NumericRule(rsiIndicator, new ConstantIndicator(series, series.numOf(30)), Num::isLessThan));
                WeightRuleWrapper rsiExitWeightRuleWrapper = new WeightRuleWrapper("rsi超过85", 1, new NumericRule(rsiIndicator, new ConstantIndicator(series, series.numOf(70)), Num::isGreaterThan));


                BollingerBandsWidthRule bollingEnter = new BollingerBandsWidthRule(upperBB, middleBB, lowerBB, closePrice, 5, 0.0006, OrderSide.BUY_LONG);
                BollingerBandsWidthRule bollingExit = new BollingerBandsWidthRule(upperBB, middleBB, lowerBB, closePrice, 5, 0.0006, OrderSide.BUY_SHORT);

                WeightRuleWrapper weightBollingEnterWrapper = new WeightRuleWrapper("布林带宽度指标达到下轨做多", 3, bollingEnter);
                WeightRuleWrapper weightBollingExitWrapper = new WeightRuleWrapper("布林带宽度指标达到上轨退出", 3, bollingExit);


                WeightRuleWrapper atrUper = new WeightRuleWrapper("3atr止盈", 3, new AtrOrderPriceRule(atrIndicator, closePrice, 3));

                //https://school.stockcharts.com/doku.php?id=technical_indicators:chandelier_exit  最近10日最高价减去3个atr单位
                NumericIndicator chandelier_exit = NumericIndicator.of(new HighestValueIndicator(new HighPriceIndicator(series), 10))
                        .minus(NumericIndicator.of(atrIndicator).multipliedBy(3));
                NumericIndicator min = NumericIndicator.of(chandelier_exit).min(NumericIndicator.of(atrLowerIndicator));


                WeightRuleWrapper exitTimeRule = new WeightRuleWrapper("30根k线盈利不到0.4.止盈", 3, new ExitTimeRule(30, 0.004, closePrice));


                Rule macdDeathCross = new CrossedDownCacheIndicatorRule(emaIndicator7, eemaIndicator25);
                Rule macdGoldenCross = new CrossedPushCacheIndicatorRule(emaIndicator7, eemaIndicator25);


                WeightRuleWrapper macd金叉 = new WeightRuleWrapper("macd金叉", 1, new CrossedDownCacheIndicatorRule(deaIndicator, difIndicator));
                WeightRuleWrapper macd死叉 = new WeightRuleWrapper("macd死叉", 1, new CrossedPushCacheIndicatorRule(deaIndicator, difIndicator));

                WeightRuleWrapper ema7日线上穿25日线 = new WeightRuleWrapper("ema7日线上穿25日线", 2, macdGoldenCross);
                WeightRuleWrapper ema7日线下穿25日线 = new WeightRuleWrapper("ema7日线下穿25日线", 2, macdDeathCross);

//                WeightRuleWrapper ema7日线上穿99日线 = new WeightRuleWrapper("ema7日线上穿99日线", 2, macdGoldenCross2);
//                WeightRuleWrapper ema7日线下穿99日线 = new WeightRuleWrapper("ema7日线下穿99日线", 2, macdDeathCross2);


//                WeightRuleWrapper ema25日线上穿99日线 = new WeightRuleWrapper("ema25日线上穿99日线", 2, macdGoldenCross3);
//                WeightRuleWrapper ema25日线下穿99日线 = new WeightRuleWrapper("ema25日线下穿99日线", 2, macdDeathCross3);

                enterRules.add(bullishHarami);
                enterRules.add(weightBollingEnterWrapper);
//                enterRules.add(bopCrossUpWrapper);
                enterRules.add(rsiEnterWeightRuleWrapper);
                enterRules.add(ema7日线上穿25日线);
//                enterRules.add(ema25日线上穿99日线);
//                enterRules.add(ema7日线上穿99日线);
                enterRules.add(macd金叉);
//                enterRules.add(emaRule);
                enterRules.add(arooWeightRuleWrapper);


                exitRules.add(weightBollingExitWrapper);
//                exitRules.add(hightRule);
//                exitRules.add(bopCrossDownWrapper);
                exitRules.add(coefficientWeightBearishEngulfingRule);
                exitRules.add(rsiExitWeightRuleWrapper);
                exitRules.add(ema7日线下穿25日线);
//                exitRules.add(ema7日线下穿99日线);
//                exitRules.add(ema25日线下穿99日线);
                exitRules.add(macd死叉);
                exitRules.add(exitTimeRule);
                //止损
                NumericRule numericRule = new NumericRule(closePrice, min, Num::isLessThan);
                exitRules.add(new WeightRuleWrapper("止损", 8, numericRule));
                //临时假如一个3atr止盈
                exitRules.add(atrUper);

                HashSet<Integer> enterTargets = new HashSet<>();
//
                enterTargets.add(3);
                enterTargets.add(4);
                enterTargets.add(5);
                enterTargets.add(6);
                enterTargets.add(7);
                enterTargets.add(8);
                enterTargets.add(9);
                HashSet<Integer> exitTargets = new HashSet<>();
                exitTargets.add(9);
                exitTargets.add(5);
                exitTargets.add(4);
//                exitTargets.add(3);
                exitTargets.add(6);
                exitTargets.add(7);
                exitTargets.add(8);

                return new CollectRuleStrategy(enterRules, exitRules, 20, enterTargets, exitTargets);
            }
        };

        test("PYTHUSDT", "15m", 800, false, strateFactory);
        Thread.sleep(10000);
        test("SUIUSDT", "15m", 800, false, strateFactory);
        Thread.sleep(10000);
        test("BNTUSDT", "15m", 1000, false, strateFactory);
    }

    private static void test(String currency, String interval, int amount, boolean mock, StrateFactory strateFactory) {
        new Thread(() -> {
            //BTCUSDT_240329
            CurrencyRegister.getCurrency(currency).ifPresent(i -> {
                AtomicBoolean running = new AtomicBoolean(false);
                while (true) {
                    if (running.compareAndSet(false, true)) {
                        try {
                            TraderTemplate test = new TraderTemplate(mock, Trade.TradeType.BUY, amount, interval, i, strateFactory, 0.0002);
                            test.test(interval);
                        } catch (Exception e1) {
                            System.out.println("----" + e1);
                            e1.printStackTrace();
                            running.set(false);
                        }
                    }
                }
            });
        }).start();
    }


}
