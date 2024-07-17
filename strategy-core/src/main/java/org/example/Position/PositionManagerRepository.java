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

package org.example.Position;

import org.example.enums.Exchange;
import org.example.model.currency.Currency;
import org.example.model.enums.ContractType;
import org.example.model.enums.OrderSide;
import org.example.model.position.PositionVo;
import org.example.responsity.JdbcTest;
import org.example.responsity.sql.PositionSql;
import org.example.util.JsonUtil;
import org.example.util.ThreadFactoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PositionManagerRepository implements PositionManagerRecoveryable<PositionInfo> {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());
    private final ConcurrentHashMap<Currency, PositionInfo> POSIITONS = new ConcurrentHashMap<>(16);

    protected ScheduledExecutorService scheduler;

    protected String strategyId;

    protected ContractType contractType;
    protected Exchange exchange;
    protected OrderSide orderSide;

    public PositionManagerRepository(String strategyId, ContractType contractType, Exchange exchange, OrderSide orderSide) {
        this.strategyId = strategyId;
        this.exchange = exchange;
        this.contractType = contractType;
        this.orderSide = orderSide;
        this.scheduler =
                new ScheduledThreadPoolExecutor(
                        1, new ThreadFactoryUtil("timer-update-posiiton-thread"));
        recovery();
        this.scheduler.scheduleWithFixedDelay(
                () -> {
                    try {
                        recovery();
                    } catch (Exception e) {
                        LOG.warn("update position task failed...", e);
                    }
                },
                20000,
                10000,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public List<PositionInfo> getAllPosition() {
        ArrayList<PositionInfo> positionInfos = new ArrayList<>();
        positionInfos.addAll(POSIITONS.values());
        return positionInfos;
    }

    @Override
    public double getQuantity(Currency currency) {
        if (POSIITONS.containsKey(currency)) {
            return POSIITONS.get(currency).getQuantity();
        } else {
            return -1;
        }
    }

    @Override
    public double getCost(Currency currency) {
        if (POSIITONS.containsKey(currency)) {
            return POSIITONS.get(currency).getCost();
        } else {
            return -1;
        }
    }

    @Override
    public void recovery() {
        //从仓位表里获取即可
        try {
            List<PositionVo> positionVos = PositionSql.queryPositionsByStrategyId(strategyId, exchange, orderSide, contractType, JdbcTest.conn);
            for (PositionVo positionVo : positionVos) {
                POSIITONS.computeIfAbsent(positionVo.getCurrency(), k -> new PositionInfo(positionVo.getCurrency(), positionVo.getQuantity(), positionVo.getCost()));
            }
            POSIITONS.forEach((k, v) -> {
                if (v.getQuantity() > 0) {
                    System.out.println(String.format("仓位信息：\n" +
                            "币种： [%s]\n" +
                            "交易类型： %s\n" +
                            "持仓方向： %s\n" +
                            "持仓平均价：%s\n" +
                            "持仓数量： %s",k.symbol(),contractType.toString(),orderSide.toString(),v.getCost(),v.getQuantity()));
                }
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
