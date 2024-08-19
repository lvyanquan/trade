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

package org.example.core.strategy;

import org.example.core.AccountModel;
import org.example.core.Constant;
import org.example.core.TradeContext;
import org.example.core.TradeContextAble;
import org.example.core.bar.BarEngineBuilder;
import org.example.core.bar.BarPipeline;
import org.example.core.bar.BaseBarExtend;
import org.example.core.bar.KlineInterval;
import org.example.core.bar.KlineSource;
import org.example.core.bar.TradeType;
import org.example.core.bar.util.BarConvent;
import org.example.core.order.GridOrder;
import org.example.core.order.GridOrderManager;
import org.example.core.order.TradeUtil;
import org.example.core.strategy.grid.GridOrderBook;
import org.example.core.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DoubleNum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//2748 初始仓位
public class GridModel implements BarPipeline.BarHandler<BaseBarExtend>, TradeContextAble {
    private static final Logger LOG = LoggerFactory.getLogger(GridModel.class);


    String name;

    //0初始化 1更新中 2 可交易状态
    private AtomicInteger state = new AtomicInteger(0);


    private BarEngineBuilder.SymbolDescribe symbol;

    //网格数量
    private int gridNumber;

    //当前中心价格
    protected double centralPrice;
    protected double atrPrice;
    private final double minAtrPrice = 200;

    //是否交易过
    protected boolean hasTrade = false;

    //初始化时，买入5个网格数量
    protected int firstTradeAmount = 3;
    protected double firstTradePrice;

    //每个网格交易的usdt仓位
    protected double gridAmount;

    protected LocalDateTime updateTime = LocalDateTime.now();

    protected ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private GridOrderBook gridOrderBook;
    private TradeContext context;

    private GridOrderManager gridOrderManager;

    boolean windowDataApply = false;
    BaseBarSeries barSeries;
    ClosePriceIndicator closePriceIndicator;
    ATRIndicator atrIndicator;
    EMAIndicator emaIndicatorLong;

    private int sellContinues = 0;
    private int buyContinues = 0;

    private long forzenBuyTime = 0;

    private long tradeBarIndex;

    public static void main(String[] args) throws Exception {
        String symbol = "BTCUSDT";
        String exchange = "binance";
        BarEngineBuilder.SymbolDescribe btcSymbol = new BarEngineBuilder.SymbolDescribe(
                symbol,
                TradeType.SPOT,
                KlineInterval.ONE_MINUTE,
                System.currentTimeMillis() - 24 * 60 * 60 * 1000,
                -1
        );

        //网格数量
        int gridNumber = 30;
        //每个网格仓位60u
        double gridAmount = 60;

        GridModel gridModel = new GridModel("grid-01", gridNumber, gridAmount, btcSymbol);
        //手续费
        double feeRate = 0.00075;
        TradeContext tradeContext = new TradeContext(new AccountModel(feeRate, gridAmount * gridNumber));
        gridModel.initTradeContext(tradeContext);

        new BarEngineBuilder<BaseBarExtend>()
                .exchange(exchange)
                .convert(BarConvent::conventBaseBarExtend)

                .subscribe(btcSymbol)
                .addHandler(btcSymbol, gridModel)

                .window(10)
                .skipWindowData(1)
                .build()
                .run();

        Thread.currentThread().join();
    }


    public GridModel(String name, int gridNumber, double gridAmount, BarEngineBuilder.SymbolDescribe symbol) {
        this.name = name;
        this.gridNumber = gridNumber;
        this.gridAmount = gridAmount;
        this.symbol = symbol;
        this.barSeries = new BaseBarSeries(symbol.getSymbol(), DoubleNum::valueOf);
        if (Constant.API_KEY != null) {
            this.gridOrderManager = new GridOrderManager(Constant.API_KEY, Constant.SECRET_KEY);
        } else {
            //todo mockOrderManager 回测时用到
        }
    }

    @Override
    public void open() {
        if (state.compareAndSet(0, 1)) {
            initBarseriesAndIndicator(barSeries);

            this.centralPrice = emaIndicatorLong.getValue(barSeries.getEndIndex()).doubleValue();
            this.atrPrice = atrIndicator.getValue(barSeries.getEndIndex()).doubleValue();
            if (this.atrPrice < minAtrPrice) {
                this.atrPrice = minAtrPrice;
            }

            this.gridOrderBook = new GridOrderBook(context.getAccountModel(), gridNumber, 0.6, centralPrice, atrPrice);
            gridOrderBook.revovery(gridOrderManager);
            gridOrderManager.registerListener(gridOrder -> {
                gridOrderBook.update(gridOrder);
                gridOrderBook.updateTriggerOrder(closePriceIndicator.getValue(barSeries.getEndIndex()).doubleValue());
            });
            //是否有持仓的单子即可
            this.hasTrade = gridOrderBook.hasTrade();
            this.firstTradePrice = centralPrice - 3 * atrPrice;


            //周期线程创建，进行更新操作逻辑
            //每个4小时更新一次 atr，就会更新一次丁单薄 对 gridOrders 进行一次循环，把买入价格更新下
            //每隔24小时更新一次中心价格
            executor.scheduleWithFixedDelay(
                    () -> {
                        LocalDateTime now = LocalDateTime.now();

                        if (Duration.between(updateTime, now).compareTo(Duration.ofHours(6)) >= 0) {
                            state.set(1);
                            this.centralPrice = emaIndicatorLong.getValue(barSeries.getEndIndex()).doubleValue();
                            update();
                            state.set(2);
                        } else if (Duration.between(updateTime, now).compareTo(Duration.ofHours(4)) >= 0) {
                            state.set(1);
                            update();
                            state.set(2);
                        }
                    },
                    15,
                    1,
                    TimeUnit.MINUTES);
            state.set(2);
        }
    }


    private void initBarseriesAndIndicator(BaseBarSeries barSeries) {
        //barSeries先初始化一定数量的k线数据
        BarEngineBuilder.SymbolDescribe historySymbol = new BarEngineBuilder.SymbolDescribe(symbol.getSymbol(), symbol.getTradeType(), symbol.getInterval(), System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000, System.currentTimeMillis());
        KlineSource historySOurce = new BarEngineBuilder<BaseBarExtend>()
                .exchange("binance")
                .subscribe(historySymbol)
                .window(10)
                .skipWindowData(1)
                .convert(BarConvent::conventBaseBarExtend)
                .addHandler(historySymbol, new BarPipeline.BarHandler<BaseBarExtend>() {
                    @Override
                    public void applyWindow(BaseBarExtend bar) {
                        barSeries.addBar(bar);
                    }
                })
                .build();
        historySOurce.run();
        historySOurce.close();

        closePriceIndicator = new ClosePriceIndicator(barSeries);
        //15分钟级别的交易，用最近48个小时的atr值做判断
        atrIndicator = new ATRIndicator(barSeries, 48 * 4);
        // 15分钟级别的交易，用最近4天的ema值做判断
        emaIndicatorLong = new EMAIndicator(closePriceIndicator, 4 * 24 * 4);

    }

    @Override
    public void applyWindow(BaseBarExtend bar) {
        barSeries.addBar(bar, true);
        windowDataApply = true;
        if (System.currentTimeMillis() - DateUtil.convent(bar.getEndTime()) > 30000) {
            return;
        }
        //todo 这个属于orderBook的内容吗
        gridOrderBook.updateSellOrderPrice();
        //todo 打印当前的grid信息
        print();
    }

    @Override
    public void apply(BaseBarExtend bar) {
        if (windowDataApply) {
            barSeries.addBar(bar);
            windowDataApply = false;
        } else {
            barSeries.addBar(bar, true);
        }

        if (System.currentTimeMillis() - DateUtil.convent(bar.getCreateTime()) > 3000) {
            return;
        }
        if (state.get() != 2) {
            LOG.info("更新中，暂时不交易");
            return;
        }
        //triggerBuyTradePrice 和 triggerSellTradePrice 进行更新;
        if (gridOrderBook.getNextBuyGridOrder() == null || gridOrderBook.getNextSellGridOrder() == null) {
            updateTriggerOrder();
        }


        if (forzenBuyTime > 0 && System.currentTimeMillis() - forzenBuyTime > 2 * 60 * 60 * 1000) {
            forzenBuyTime = 0;
            buyContinues--;
            buyContinues--;
        }

        double closePrice = bar.getClosePrice().doubleValue();
        if (forzenBuyTime <= 0 || System.currentTimeMillis() - forzenBuyTime > 2 * 60 * 60 * 1000) {
            if (!hasTrade && closePrice < firstTradePrice) {
                //第一次买入多单，构建多个单子即可
                for (int i = 0; i < firstTradeAmount; i++) {
                    org.example.core.strategy.grid.GridOrder upCanBuyOrderByPrice = gridOrderBook.getUpCanBuyOrderByPrice(closePrice);
                    if (upCanBuyOrderByPrice == null) {
                        break;
                    }
                    boolean buy = buyOrder(upCanBuyOrderByPrice.getSequnce(), closePrice);
                    if (buy) {
                        hasTrade = true;
                        buyContinues++;
                        sellContinues = 0;
                    }
                }
            } else if (hasTrade && closePrice < gridOrderBook.getNextBuyGridOrder().getTriggerBuyPrice()) {
                org.example.core.strategy.grid.GridOrder nextBuyGridOrder = gridOrderBook.getNextBuyGridOrder();
                //触发交易，查找最近的一个网格，买入
                //这部分就不进行状态恢复了 后续再完善吧
                //如果这段时间净买入单子数量的20%，就设置冷近期2小时 或者 triggerBuyTradePrice - 2.2*atr，满足就继续买入
                if (buyContinues < gridNumber * 0.1) {
                    buyOrder(nextBuyGridOrder.getSequnce(), closePrice);
                } else {
                    LOG.info(String.format("连续交易次数为 %s,暂停交易", buyContinues));
                    if (forzenBuyTime <= 0) {
                        forzenBuyTime = System.currentTimeMillis();
                    }
                }
                updateTriggerOrder();
            }

        }


        if (closePrice > gridOrderBook.getNextSellGridOrder().getTriggerSellPrice()
                && closePrice > gridOrderBook.getNextSellGridOrder().getOrderBuyPrice()) {
            org.example.core.strategy.grid.GridOrder nextSellGridOrder = gridOrderBook.getNextSellGridOrder();
            if (!nextSellGridOrder.canSell()) {
                return;
            }
            nextSellGridOrder.updateTradIng();
            //1 卖出操作
            //2 更新订单薄
            //如果这段时间连续卖出2单子，就设置触发价格为 3atr，满足就卖出
            double sellPrice = Math.max(gridOrderBook.getNextSellGridOrder().getTriggerSellPrice(), gridOrderBook.getNextSellGridOrder().getOrderBuyPrice());
            sellPrice = Math.max(sellPrice, closePrice);
            if (sellContinues >= 2) {
                sellPrice = sellPrice + atrPrice;
            }

            sellPrice = sellPrice + 10;
            double quantity = BigDecimal.valueOf(nextSellGridOrder.getQuantity()).setScale(5, RoundingMode.DOWN).doubleValue();

            String clientId = name + "_" + nextSellGridOrder.getSequnce() + "_" + System.currentTimeMillis();
            Exception e2 = null;
            try {
                TradeUtil.orderLimitPingDuo(symbol.getSymbol(),
                        sellPrice,
                        quantity,
                        2,
                        clientId,
                        gridOrderManager.getTradeClient());
            } catch (Exception e) {
                e2 = e;
            }
            if (e2 == null) {
                gridOrderManager.insertNewOrder(new org.example.core.order.GridOrder(clientId,
                        symbol.getSymbol(),
                        nextSellGridOrder.getSequnce(),
                        sellPrice,
                        nextSellGridOrder.getQuantity(),
                        2,
                        System.currentTimeMillis()));
                sellContinues++;
                buyContinues = 0;
            }
            updateTriggerOrder();
        }

    }


    public void updateTriggerOrder() {
        gridOrderBook.updateTriggerOrder(closePriceIndicator.getValue(barSeries.getEndIndex()).doubleValue());
        print();
    }


    public boolean buyOrder(int sequence, double closePrice) {

        org.example.core.strategy.grid.GridOrder gridOrder = gridOrderBook.getOrder(sequence);
        //交易过一次之后，至少等2根k线之后才能交易 防止插针一直交易
        if (!gridOrder.canBuy() || barSeries.getEndIndex() - tradeBarIndex > 1) {
            return false;
        }
        gridOrder.updateTradIng();

        double tradePrice = closePrice - 10;
        gridOrder.setQuantity(BigDecimal.valueOf(gridAmount / tradePrice).setScale(5, RoundingMode.DOWN).doubleValue());
        String clientId = name + "_" + sequence + "_" + System.currentTimeMillis();
        Exception e2 = null;
        try {
            TradeUtil.orderLimitDuo(symbol.getSymbol(),
                    tradePrice,
                    gridAmount,
                    2,
                    5,
                    clientId,
                    gridOrderManager.getTradeClient());
        } catch (Exception e) {
            gridOrder.updateCanBuy();
            e2 = e;
        }
        if (e2 == null) {
            gridOrderManager.insertNewOrder(new GridOrder(clientId,
                    symbol.getSymbol(),
                    gridOrder.getSequnce(),
                    gridOrder.getTriggerBuyPrice(),
                    gridOrder.getQuantity(),
                    0,
                    System.currentTimeMillis()));

            if (buyContinues > 2) {
                buyContinues--;
                buyContinues--;
            } else {
                buyContinues = 0;
            }
            tradeBarIndex = barSeries.getEndIndex();
            sellContinues = 0;
            return true;
        }
        return false;

    }

    private void print() {
        GridOrderBook.GridOrderBookMetric metric = gridOrderBook.getMetric();
        String format = String.format("当前价格： %s, 当前触发买入价 ： %s, 当前触发卖出价 ： %s, centerPrice: %s, atr : %s, 当前网格最低点: %s,当前网格最高点: %s",
                closePriceIndicator.getValue(barSeries.getEndIndex()).doubleValue(),
                hasTrade ? metric.getTriggerBuyPrice() : firstTradePrice,
                metric.getTriggerSellPrice(),
                metric.getCentralPrice(),
                metric.getAtrPrice(),
                metric.getLowBuyPrice(),
                metric.getHighBuyPrice());
        LOG.info(format);

    }

    private void update() {
        this.atrPrice = atrIndicator.getValue(barSeries.getEndIndex()).doubleValue();
        if (this.atrPrice < minAtrPrice) {
            this.atrPrice = minAtrPrice;
        }
        //todo 择时 如果是 某某时间点，就需要增加
        ZonedDateTime endTime = barSeries.getLastBar().getEndTime();
        if (endTime.getHour() == 15 || endTime.getHour() == 22 || endTime.getHour() == 23) {
            atrPrice = atrPrice * 1.5;
        }
        if (endTime.getHour() == 10 || endTime.getHour() == 11) {
            atrPrice = atrPrice * 1.2;
        }
        LocalDateTime now = LocalDateTime.now();
        gridOrderBook.updateOrdersByCentralPrice(centralPrice, atrPrice);
        updateTriggerOrder();
        this.updateTime = now;
    }

    @Override
    public TradeContext getTradeContext() {
        return context;
    }

    @Override
    public void initTradeContext(TradeContext context) {
        this.context = context;
    }
}
