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

import org.example.model.currency.Currency;

import java.util.concurrent.ConcurrentHashMap;


/**
 * 每个品种具备一定的下单次数同时每次下单的金额都是固定的
 */
public class SameQuantifyPositionManagementStrategy implements PositionManagementStrategy {

    private final ConcurrentHashMap<Currency, Position> POSIITONS = new ConcurrentHashMap<>(16);
    private PositionManager<PositionInfo> positionManager;

    //下单金额
    private final double amount;
    //持有的下单的最大次数
    private final int maxTradeNumber;

    public SameQuantifyPositionManagementStrategy(double amount, int maxTradeNumber,PositionManager<PositionInfo> positionManager) {
        this.amount = amount;
        this.maxTradeNumber = maxTradeNumber;
        this.positionManager = positionManager;
    }

    @Override
    public double getNextAvaliableBuyAmount(Currency currency) {
        Position position = POSIITONS.computeIfAbsent(currency, k -> new Position(amount, maxTradeNumber));
        return position.getNextAvaliableSellQuantity();
    }

    @Override
    public double getNextAvaliableSellQuantity(Currency currency) {
        return positionManager.getQuantity(currency);
    }

    @Override
    public void orderAmount(Currency currency, double amount) {
        Position position = POSIITONS.computeIfAbsent(currency, k -> new Position(amount, maxTradeNumber));
        position.orderAmount();
    }

    @Override
    public void releaseAmount(Currency currency, double amount) {
        Position position = POSIITONS.computeIfAbsent(currency, k -> new Position(amount, maxTradeNumber));
        position.release();
    }
}


class Position {
    private final double amount;
    private final int maxTradeNumber;

    private int buyNumber;

    public Position(double amount, int maxTradeNumber) {
        this.amount = amount;
        this.maxTradeNumber = maxTradeNumber;
    }

    public double getNextAvaliableSellQuantity() {
        if (buyNumber < maxTradeNumber) {
            return amount;
        }
        return 0;
    }

    public void orderAmount() {
        if (buyNumber < maxTradeNumber) {
            buyNumber++;
        }
    }

    public void release() {
        if (buyNumber > 0) {
            buyNumber--;
        }
    }
}
