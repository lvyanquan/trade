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

import org.example.core.Constant;
import org.example.core.bar.BarEngineBuilder;
import org.example.core.bar.BarPipeline;
import org.example.core.bar.BaseBarExtend;
import org.example.core.bar.KlineSource;
import org.example.core.bar.util.BarConvent;
import org.example.core.order.Order;
import org.example.core.order.OrderManager;
import org.example.core.order.OrderState;
import org.example.core.order.TradeUtil;
import org.example.core.util.DateUtil;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DoubleNum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//2748 初始仓位
public class GridModel implements BarPipeline.BarHandler<BaseBarExtend> {

    String name;

    //0初始化 1更新中 2 可交易状态
    private AtomicInteger state = new AtomicInteger(0);


    private BarEngineBuilder.SymbolDescribe symbol;

    //网格数量
    private int gridNumber;

    //当前中心价格
    protected double centralPrice;
    protected double atrPrice;

    //是否交易过
    protected boolean hasTrade = true;

    //初始化时，买入5个网格数量
    protected int firstTradeAmount = 3;
    protected double firstTradePrice;

    //总金额上限
    protected double gridAmount;

    //====triggerBuyTradePrice 和 triggerSellTradePrice 触发之后。不是一定要卖，需要找到对应的订单是否能卖出和买入，并且更新价格
    //触发买入的价格
    protected GridOrder nextBuyOrder;
    //触发卖出的价格
    protected GridOrder nextSellOrder;

    protected LocalDateTime updateTime;

    protected ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    ArrayList<GridOrder> gridOrders = new ArrayList<>(gridNumber);
    private OrderManager orderManager;

    boolean windowDataApply = false;
    BaseBarSeries barSeries;
    ClosePriceIndicator closePriceIndicator;
    ATRIndicator atrIndicator;
    EMAIndicator emaIndicatorLong;

    private int sellContinues = 0;
    private int buyContinues = 0;

    private long forzenBuyTime = 0;

    public GridModel(String name, int gridNumber, double gridAmount, BarEngineBuilder.SymbolDescribe symbol) {
        this.name = name;
        this.gridNumber = gridNumber;
        this.gridAmount = gridAmount;
        this.symbol = symbol;
        if (Constant.API_KEY != null) {
            this.orderManager = new OrderManager(Constant.API_KEY, Constant.SECRET_KEY);
        } else {
            //mockOrderManager
        }
    }

    @Override
    public void open() {

        if (state.compareAndSet(0, 1)) {
            barSeries = new BaseBarSeries(symbol.getSymbol(), DoubleNum::valueOf);
            closePriceIndicator = new ClosePriceIndicator(barSeries);
            //15分钟级别的交易，用最近72个小时的atr值做判断
            atrIndicator = new ATRIndicator(barSeries, 48 * 4);

            //这个地方需要先从历史数据进行初始化
            // 15分钟级别的交易，用最近4天的ema值做判断
            emaIndicatorLong = new EMAIndicator(closePriceIndicator, 4 * 24 * 4);

            //barSeries先初始化一定数量的k线数据
            BarEngineBuilder.SymbolDescribe historySymbol = new BarEngineBuilder.SymbolDescribe(symbol.getSymbol(), symbol.getTradeType(), symbol.getInterval(), System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000, System.currentTimeMillis());
            KlineSource historySOurce = new BarEngineBuilder<BaseBarExtend>()
                    .exchange("binance")
                    .subscribe(historySymbol)
                    .convert(BarConvent::conventBaseBarExtend)
                    .addHandler(historySymbol, new BarPipeline.BarHandler<BaseBarExtend>() {
                        @Override
                        public void apply(BaseBarExtend bar) {
                            barSeries.addBar(bar);
                        }
                    })
                    .build();
            historySOurce.run();
            historySOurce.close();

            GridVo gridVo = JdbcTest.selectGridVo(name);
            if (gridVo != null) {
                //必须是当天的才能更新，否则认为就过时了
                if (Duration.between(gridVo.getUpdateTime().toLocalDateTime(),  LocalDateTime.now()).compareTo(Duration.ofHours(2)) <= 0) {
                    this.centralPrice = gridVo.getCentralPrice();
                    this.updateTime = gridVo.getUpdateTime().toLocalDateTime();
                } else {
                    this.centralPrice = emaIndicatorLong.getValue(barSeries.getEndIndex()).doubleValue();
                }
                this.gridNumber = gridVo.getGridNumber();
                this.gridAmount = gridVo.getGridAmount();
            } else {
                this.centralPrice = emaIndicatorLong.getValue(barSeries.getEndIndex()).doubleValue();
            }
            this.atrPrice = atrIndicator.getValue(barSeries.getEndIndex()).doubleValue();
            this.firstTradePrice = centralPrice - 5 * atrPrice;
            GridVo newGridVo = new GridVo(name,
                    gridNumber,
                    centralPrice,
                    gridAmount,
                    new Timestamp(System.currentTimeMillis()));
            JdbcTest.insertAndDeleteBeforeGridVo(newGridVo);
            System.out.println("start grid... " + newGridVo);
            //丁单薄的更新 最后加上下单操作即可上线了
            List<Order> orders = orderManager.selectAllWorkerOrder();
            HashMap<Integer, Order> orderMap = new HashMap<>();
            for (Order order : orders) {
                orderMap.put(order.getGridIndex(), order);
            }
            //是否有持仓的单子即可
            this.hasTrade = !orders.isEmpty();

            //centralPrice下方有几个网格点
            int downGridNumber = Double.valueOf(gridNumber * 0.6D).intValue();
            for (int i = 0; i < gridNumber; i++) {
                GridOrder gridOrder = new GridOrder(i, 0.00075, gridAmount);
                //每个订单设置买入价格
                //卖出价格 为买入价格 + atr *1.1 计算得出
                gridOrder.setPriceAndCalcuteLowPrice(centralPrice - (downGridNumber - i) * (atrPrice * 2) - (downGridNumber * 0.1 <5?downGridNumber * 0.1:5) * atrPrice);
                gridOrders.add(gridOrder);

                if (orderMap.get(i) != null) {
                    Order order = orderMap.get(i);
                    if (order.getOrderState() == OrderState.FILLED || order.getOrderState() == OrderState.PARTIALLY_FILLED) {
                        gridOrder.setPrice(order.getPrice());
                        gridOrder.setQuantity(order.getExecutedQuantity());
                        gridOrder.setStatus(1);
                    } else if (order.getOrderState() == OrderState.NEW) {
                        gridOrder.setQuantity(order.getQuantity());
                        gridOrder.setStatus(0);
                    }
                }
            }

            orderManager.registerListener(order -> {
                GridOrder gridOrder = gridOrders.get(order.getGridIndex());
                if (order.getOrderState() == OrderState.FILLED || order.getOrderState() == OrderState.PARTIALLY_FILLED) {
                    //买单成交，代表这个网格点可以卖出了
                    if (order.getSide() == 0) {
                        gridOrder.setQuantity(order.getExecutedQuantity());
                        gridOrder.setStatus(1);
                    } else if (order.getSide() == 2) {
                        //如果卖单成交，代表这个网格点可以买入了
                        gridOrder.setQuantity(0);
                        gridOrder.setStatus(0);
                    }
                } else if (order.getOrderState().isInvalid()) {
                    gridOrder.setQuantity(0);
                    gridOrder.setStatus(0);
                }
                updateTriggerOrder();
            });


            //周期线程创建，进行更新操作逻辑
            //每个4小时更新一次 atr，就会更新一次丁单薄 对 gridOrders 进行一次循环，把买入价格更新下
            //每隔24小时更新一次中心价格
            executor.scheduleWithFixedDelay(
                    () -> {
                        LocalDateTime now = LocalDateTime.now();

                        if (Duration.between(updateTime, now).compareTo(Duration.ofHours(8)) >= 0) {
                            state.set(1);
                            this.centralPrice = emaIndicatorLong.getValue(barSeries.getEndIndex()).doubleValue();
                            this.atrPrice = atrIndicator.getValue(barSeries.getEndIndex()).doubleValue();
                            this.updateTime = LocalDateTime.now();

                            JdbcTest.insertAndDeleteBeforeGridVo(new GridVo(name,
                                    gridNumber,
                                    centralPrice,
                                    gridAmount,
                                    new Timestamp(System.currentTimeMillis())));
                            updateOrdersPrice();
                            updateTriggerOrder();
                            state.set(2);
                        } else if (Duration.between(updateTime, now).compareTo(Duration.ofHours(4)) >= 0) {
                            state.set(1);
                            this.atrPrice = atrIndicator.getValue(barSeries.getEndIndex()).doubleValue();
                            this.updateTime = LocalDateTime.now();
                            updateOrdersPrice();
                            updateTriggerOrder();
                            state.set(2);
                        }
                    },
                    0,
                    1,
                    TimeUnit.HOURS);

            state.set(2);
        } else {
            //打印日志？
        }
    }

    @Override
    public void applyWindow(BaseBarExtend bar) {
        barSeries.addBar(bar, true);
        windowDataApply = true;

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
        //triggerBuyTradePrice 和 triggerSellTradePrice 进行更新;
        if (nextBuyOrder == null || nextSellOrder == null) {
            updateTriggerOrder();
        }
        if (state.get() != 2) {
            System.out.println("更新中，暂时不交易");
            return;
        }

        if (bar.getClosePrice().doubleValue() < nextBuyOrder.getPrice()) {
            if (nextBuyOrder.canBuy()) {
                //触发交易，查找最近的一个网格，买入
                if (!hasTrade) {
                    //第一次买入多单，构建多个单子即可
                    for (int i = nextBuyOrder.getSequnce(); i < nextBuyOrder.getSequnce() + firstTradeAmount && i < gridOrders.size(); i++) {
                        boolean buy = buyOrder(i, bar.getClosePrice().doubleValue(),true);
                        if (buy) {
                            hasTrade = true;
                        }
                    }
                    buyContinues++;
                    sellContinues = 0;
                } else {
                    //这部分就不进行状态恢复了 后续再完善吧
                    //如果这段时间净买入单子数量的20%，就设置冷近期2小时 或者 triggerBuyTradePrice - 2.2*atr，满足就继续买入
                    if (buyContinues < gridNumber * 0.2) {
                        buyOrder(nextBuyOrder.getSequnce(),bar.getClosePrice().doubleValue(), false);
                       if(buyContinues > 2){
                           buyContinues--;
                           buyContinues--;
                       }else{
                           buyContinues = 0;
                       }
                        sellContinues = 0;
                    } else {
                        System.out.println(String.format("连续交易次数为 %s,暂停交易", buyContinues));
                        if (forzenBuyTime <= 0) {
                            forzenBuyTime = System.currentTimeMillis();
                        } else {
                            if (System.currentTimeMillis() - forzenBuyTime > 2 * 60 * 60 * 1000) {
                                forzenBuyTime = 0;
                                buyContinues--;
                            }
                        }
                    }
                }
                updateTriggerOrder();
            }
        }


        if (nextSellOrder.canSell()
                && bar.getClosePrice().doubleValue() > nextSellOrder.getPrice() + (atrPrice * 3)
                && bar.getClosePrice().doubleValue() > nextSellOrder.getLowPrice()) {

            //1 卖出操作
            //2 更新订单薄
            //如果这段时间连续卖出2单子，就设置触发价格为 3atr，满足就卖出
            double sellPrice = Math.max(nextSellOrder.getLowPrice(), nextSellOrder.getPrice() + (atrPrice * 3));
            sellPrice = Math.max(bar.getClosePrice().doubleValue(), sellPrice);
            if (sellContinues >= 2) {
                sellPrice = sellPrice + atrPrice;
            }
            nextSellOrder.setStatus(0);
            String clientId = name + "_" + nextSellOrder.getSequnce() + "_" + System.currentTimeMillis();
            TradeUtil.orderLimitPingDuo(symbol.getSymbol(),
                    sellPrice + 10,
                    nextSellOrder.getQuantity(),
                    2,
                    clientId,
                    orderManager.getTradeClient());

            orderManager.insertNewOrder(new Order(clientId,
                    symbol.getSymbol(),
                    nextSellOrder.getSequnce(),
                    sellPrice,
                    nextSellOrder.getQuantity(),
                    2));
            sellContinues++;
            buyContinues = 0;
            updateTriggerOrder();
        }

    }


    public void updateOrdersPrice() {
        int downGridNumber = Double.valueOf(gridNumber * 0.6D).intValue();
        for (int i = 0; i < gridNumber; i++) {
            gridOrders.get(i).setPriceAndCalcuteLowPrice(centralPrice - (downGridNumber - i) * (atrPrice * 2) - (downGridNumber * 0.1 <5?downGridNumber * 0.1:5) * atrPrice);

        }
    }

    public void updateTriggerOrder() {
        double closePrice = closePriceIndicator.getValue(barSeries.getEndIndex()).doubleValue();
        updateBuyOrder(closePrice);
        updateSellOrder();

        print();
    }


    public void updateBuyOrder(double closePrice) {
        GridOrder tempnextBuyOrder = null;
        for (int i = gridNumber - 1; i >= 0; i--) {
            GridOrder gridOrder = gridOrders.get(i);
            if (gridOrder.canBuy() && gridOrder.getPrice() < closePrice) {
                tempnextBuyOrder = gridOrders.get(i);
                break;
            }
        }
        //当前价格下方没有一个可以买入的网格点，所以价格置为-1，永远不会触发买入
        //只有等到有卖出了，即价格回到网格范围内，才会重新更新 或者centerPrice变更，价格回到网格范围内
        if (tempnextBuyOrder == null) {
            nextBuyOrder = new GridOrder(-1);
            nextBuyOrder.setPrice(-1);
        } else {
            nextBuyOrder = tempnextBuyOrder;
        }
    }

    //找到买入价格最低的一次成交记录，作为卖出价格触发
    //如果没有的话 就是当前价格最近的上方单子,有可能这个单子没持有。没货卖
    public void updateSellOrder() {
        GridOrder tempnextSellOrder = null;
        for (int i = 0; i < gridNumber; i++) {
            GridOrder gridOrder = gridOrders.get(i);
            if (gridOrder.canSell()) {
                tempnextSellOrder = gridOrders.get(i);
                break;
            }
        }
        //当前价格下方没有一个可以买入的网格点，所以价格置为-1，永远不会触发买入
        //只有等到有卖出了，即价格回到网格范围内，才会重新更新 或者centerPrice变更，价格回到网格范围内
        if (tempnextSellOrder == null) {
            nextSellOrder = new GridOrder(-1);
            nextSellOrder.setPrice(10000000000d);
        } else {
            nextSellOrder = tempnextSellOrder;
        }
    }

    public boolean buyOrder(int sequence, double closePrice, boolean firstBuy) {
        GridOrder gridOrder = gridOrders.get(sequence);
        gridOrder.setStatus(0);
        gridOrder.setQuantity(BigDecimal.valueOf(gridAmount / (firstBuy ? firstTradePrice : gridOrder.getPrice())).setScale(5, RoundingMode.DOWN).doubleValue());
        String clientId = name + "_" + sequence + "_" + System.currentTimeMillis();
        Exception e2 = null;
        try {
            TradeUtil.orderLimitDuo(symbol.getSymbol(),
                    (firstBuy ? firstTradePrice : closePrice) - 10,
                    gridAmount,
                    2,
                    5,
                    clientId,
                    orderManager.getTradeClient());
        } catch (Exception e) {
            gridOrder.setQuantity(0);
            e2 = e;
        }
        if (e2 == null) {
            orderManager.insertNewOrder(new Order(clientId,
                    symbol.getSymbol(),
                    gridOrder.getSequnce(),
                    gridOrder.getPrice(),
                    gridOrder.getQuantity(),
                    0));
            return true;
        }
        return false;

    }

    private void print() {
        if (nextBuyOrder != null && nextSellOrder != null) {
                String format = String.format("当前价格： %s, 当前触发买入价 ： %s, 当前触发卖出价 ： %s, centerPrice: %s, atr : %s", closePriceIndicator.getValue(barSeries.getEndIndex()).doubleValue(), hasTrade ? nextBuyOrder.getPrice() : firstTradePrice, nextSellOrder.getLowPrice(), centralPrice, atrPrice);
            System.out.println(format);
        }
    }
}
