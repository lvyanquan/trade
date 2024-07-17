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

package com.example.web_kline.pool;

import org.example.binance.BinanceCurrencyRegister;
import org.example.binance.factory.KlineFactory;
import org.example.kline.KlineClient;
import org.example.model.currency.Currency;
import org.example.model.currency.CurrencySupplierRegister;
import org.example.model.enums.ContractType;
import org.example.model.enums.Server;
import org.example.model.market.KlineModule;
import org.example.notify.DinDinNotify;
import org.example.rule.GenericAverageIndicator;
import org.example.rule.RsiWeightIndicator;
import org.example.rule.volume.IncreasingVolumeIndicator;
import org.example.util.JsonUtil;
import org.example.util.KlineUtil;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.AbstractIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.helpers.AmountIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class CurrencyPool {
    //现货的交易量  流通市值 交易量 和市值的比值 一起来缩小范围 （不一定是现货 合约也行）
    //交易数量 和 流通值比较即可
    //交易量 是用来过滤2000w美金以上的币种 或者看下前100的排名呗 才能确定2000w这个数字
    //流通市值要在2000w美金以上 排名
    //交易数量 和 流通值比值 做一个排序 比如比值大于0。3的

    //如果有合约 看下合约持仓量是否变大
    //买入点 连续上移就直接买入 获取最近一段时间平均价格等买入
    private final KlineClient binanceKlineClient = KlineFactory.Create(Server.BINANCE, ContractType.SPOT);

    private DinDinNotify dinDinNotify = new DinDinNotify();

    public static void main(String[] args) throws InterruptedException {
        CurrencyPool currencyPool = new CurrencyPool();
        HashMap<String, CurrencyWeight> data = new HashMap<>();

        for (Currency currency : BinanceCurrencyRegister.currencys()) {
            boolean b = currency.quoteAsset().equalsIgnoreCase("USDT");
            if (b) {
                //每个品种的分数 当前时间 然后输出一个表格即可
                //定义一个类
                try {
                    currencyPool.ma60(currency, data);
                } catch (Exception e) {
                    //ignore
                    e.printStackTrace();
                }
            }
        }

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        Thread.sleep(300000);
        executorService.scheduleAtFixedRate(() -> {
//            double tenthWeight = data.values().stream()
//                    .min(Comparator.comparingDouble(CurrencyWeight::getWeight))
//                    .get()
//                    .getWeight();
//            List<CurrencyWeight> top10AndSameWeight = data.values().stream()
//                    .filter(c -> c.getWeight() >= tenthWeight)
//                    .collect(Collectors.toList());
            List<CurrencyWeight> top10AndSameWeight = data.values().stream()
                    .sorted(Comparator.comparingDouble(CurrencyWeight::getWeight).reversed())
                    .distinct()
                    .limit(1000)
                    .collect(Collectors.toList());
            System.out.println("排行榜" + JsonUtil.toJson(top10AndSameWeight));
        }, 0, 30, TimeUnit.SECONDS);


    }

    public void currencyWeight(Currency currency, Map<String, CurrencyWeight> weights) {
        BarSeries series = new BaseBarSeries("mySeries2", DoubleNum::valueOf);
        long endTime = System.currentTimeMillis();
        long startTime = System.currentTimeMillis() - 20 * 24 * 60 * 60 * 1000;

        Optional<Double> supplie = CurrencySupplierRegister.getSupplie(currency.baseAsset());
        if (!supplie.isPresent()) {
            System.out.println(currency.baseAsset() + " not exists");
            return;
        }
        for (KlineModule historyKlineDatum : binanceKlineClient.getHistoryKlineData(currency, "1d", startTime, endTime)) {
            series.addBar(KlineUtil.convertKlineModuleToBar(historyKlineDatum));
        }
        Rule weight = getWeight(series, supplie.get());
        Indicator<Num> indicator = indicator(series);

        if (weight.isSatisfied(series.getEndIndex())) {
            Num value = indicator.getValue(series.getEndIndex());
            weights.put(currency.symbol(), new CurrencyWeight(currency, value.doubleValue(), System.currentTimeMillis()));
        }
//
//        binanceKlineClient.handlerStreamingKlineData(currency, "4h", t -> {
//
//        }, t -> {
//            if (weight.isSatisfied(series.getEndIndex())) {
//                Num value = indicator.getValue(series.getEndIndex());
//                weights.put(currency.symbol(), new CurrencyWeight(currency, value.doubleValue(), System.currentTimeMillis()));
//            }
//        });
    }

    public void ma60(Currency currency, Map<String, CurrencyWeight> weights) {
        AtomicLong lastNotify = new AtomicLong();
        BarSeries series = new BaseBarSeries("mySeries2", DoubleNum::valueOf);
        long endTime = System.currentTimeMillis();
        long startTime = System.currentTimeMillis() - 20 * 24 * 60 * 60 * 1000;

        Optional<Double> supplie = CurrencySupplierRegister.getSupplie(currency.baseAsset());
        if (!supplie.isPresent()) {
            System.out.println(currency.baseAsset() + " not exists");
            return;
        }
        for (KlineModule historyKlineDatum : binanceKlineClient.getHistoryKlineData(currency, "1d", startTime, endTime)) {
            series.addBar(KlineUtil.convertKlineModuleToBar(historyKlineDatum));
        }
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);
        EMAIndicator emaIndicator = new EMAIndicator(closePriceIndicator, 60);
        Rule weight = getWeight(series, supplie.get());


        binanceKlineClient.handlerStreamingKlineData(currency, "1d", t -> {
            try {
                series.addBar(KlineUtil.convertKlineModuleToBar(t), true);
            } catch (Exception e) {
                try {
                    series.addBar(KlineUtil.convertKlineModuleToBar(t), false);
                } catch (Exception e1) {
                }
            }

            if (weight.isSatisfied(series.getEndIndex()) && test(closePriceIndicator.getValue(series.getEndIndex()).doubleValue(), emaIndicator.getValue(series.getEndIndex()).doubleValue(), 0.5) && System.currentTimeMillis() - lastNotify.get() > 5 * 60 * 1000) {
                lastNotify.set(System.currentTimeMillis());
                dinDinNotify.send(currency.symbol() + "距离60日均线价格在0.5%以内，当前价格 + " + closePriceIndicator.getValue(series.getEndIndex()).doubleValue() + " 60日均线价格" + emaIndicator.getValue(series.getEndIndex()).doubleValue());
            }
        }, t -> {
            try {
                series.addBar(KlineUtil.convertKlineModuleToBar(t), false);
            } catch (Exception e) {
                try {
                    series.addBar(KlineUtil.convertKlineModuleToBar(t), true);
                } catch (Exception e1) {
                }
            }
            if (weight.isSatisfied(series.getEndIndex()) && test(closePriceIndicator.getValue(series.getEndIndex()).doubleValue(), emaIndicator.getValue(series.getEndIndex()).doubleValue(), 1) && System.currentTimeMillis() - lastNotify.get() > 5 * 60 * 1000) {
                lastNotify.set(System.currentTimeMillis());
                dinDinNotify.send(currency.symbol() + "距离60日均线价格在1%以内，当前价格 + " + closePriceIndicator.getValue(series.getEndIndex()).doubleValue() + " 60日均线价格" + emaIndicator.getValue(series.getEndIndex()).doubleValue());
            }
        });
    }

    public boolean test(double close, double ema, double diff) {

        // 计算差异百分比
        double difference = Math.abs(close - ema) / close * 100;

        // 如果差异在1%以内，进行提示
        return difference <= diff;
    }

    public Rule getWeight(BarSeries series, double supplyQuanlity) {
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);
        //2. 交易的数量必须是供应量20%以上（参数可配置）
        Rule volumn1 = new OverIndicatorRule(new VolumeIndicator(series), supplyQuanlity * 0.01);
        //4小时交易额超过500w U （参数可配置）
        Rule volumn2 = new OverIndicatorRule(new AmountIndicator(series), 3000000);
        //市值必须是大于3000 wU（参数可配置，收盘价 * 供应量（indicator构造函数里提供））
        Rule volumn3 = new OverIndicatorRule(NumericIndicator.of(closePriceIndicator).multipliedBy(supplyQuanlity), 3000000);
        return volumn1.or(volumn2).and(volumn3);

    }

    public Indicator<Num> indicator(BarSeries series) {
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);
        //k线对应的成交量必须是逐渐增大的，或者都是在近期是较大的，比如是平均交易量以上 IncreasingVolumeWeightRule
        IncreasingVolumeIndicator increasingVolumeIndicator = new IncreasingVolumeIndicator(series, 9);

        RSIIndicator rsiIndicator = new RSIIndicator(closePriceIndicator, 14);

        //rsi小于30  不能高于70
        RsiWeightIndicator rsiWeightIndicator = new RsiWeightIndicator(rsiIndicator, 30, 70);


        return NumericIndicator.of(rsiWeightIndicator).plus(increasingVolumeIndicator).plus(atrIndicator(series)).plus(bollingerIndicator(series, closePriceIndicator)).plus(emaIndicator(series, closePriceIndicator));

    }

    //最近几根k线ATR（3根）应该是最近30根k线平均atr之上
    public Indicator<Num> atrIndicator(BarSeries series) {
        ATRIndicator atrIndicator = new ATRIndicator(series, 14);
        GenericAverageIndicator genericAverageIndicator = new GenericAverageIndicator(atrIndicator, 9);
        return new AbstractIndicator<Num>(series) {
            @Override
            public Num getValue(int i) {
                if (getBarSeries().getEndIndex() < 4) {
                    return numOf(0);
                }
                Num value = genericAverageIndicator.getValue(i);
                if (atrIndicator.getValue(i).isGreaterThan(value) && atrIndicator.getValue(i - 1).isGreaterThan(value) && atrIndicator.getValue(i - 2).isGreaterThan(value)) {
                    return numOf(1);
                }
                return numOf(0);
            }
        };
    }

    //8.布林带 在中轨之下
    public Indicator<Num> bollingerIndicator(BarSeries series, ClosePriceIndicator closePriceIndicator) {
        final Indicator<Num> sma = new SMAIndicator(closePriceIndicator, 9);

        final BollingerBandsMiddleIndicator middleBB = new BollingerBandsMiddleIndicator(sma);
        return new AbstractIndicator<Num>(series) {
            @Override
            public Num getValue(int i) {
                if (closePriceIndicator.getValue(i).isLessThan(middleBB.getValue(i))) {
                    return numOf(1);
                }

                return numOf(0);
            }
        };
    }

    public Indicator<Num> emaIndicator(BarSeries series, ClosePriceIndicator closePriceIndicator) {

        EMAIndicator emaIndicator20 = new EMAIndicator(closePriceIndicator, 20);

        return new AbstractIndicator<Num>(series) {
            @Override
            public Num getValue(int i) {
                if (closePriceIndicator.getValue(i).isGreaterThan(emaIndicator20.getValue(i))) {
                    return numOf(1);
                }

                return numOf(0);
            }
        };
    }
}

class CurrencyWeight {
    private String currency;
    private double weight;
    private long timestamp;

    public CurrencyWeight(Currency currency, double weight, long timestamp) {
        this.currency = currency.symbol();
        this.weight = weight;
        this.timestamp = timestamp;
    }

    public String getCurrency() {
        return currency;
    }

    public double getWeight() {
        return weight;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "CurrencyWeight{" +
                "currency=" + currency +
                ", weight=" + weight +
                ", timestamp=" + timestamp +
                '}';
    }
}
