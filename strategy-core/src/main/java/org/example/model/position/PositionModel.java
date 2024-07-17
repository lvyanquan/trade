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

package org.example.model.position;

public class PositionModel {
    private long id;
    private String symbol;

    private String strategyId;

    private double quantity;
    private double cost;

    private int exchangeId;
    private int orderSide;
    private int contractType;

    private long lastUpdated;

    public PositionModel(String symbol, String strategyId, double quantity, double cost, int exchangeId, int orderSide, int contractType, long lastUpdated) {
        this.symbol = symbol;
        this.strategyId = strategyId;
        this.quantity = quantity;
        this.cost = cost;
        this.exchangeId = exchangeId;
        this.orderSide = orderSide;
        this.contractType = contractType;
        this.lastUpdated = lastUpdated;
    }

    public PositionModel(long id, String symbol, String strategyId, double quantity, double cost, int exchangeId, int orderSide, int contractType, long lastUpdated) {
        this.symbol = symbol;
        this.strategyId = strategyId;
        this.quantity = quantity;
        this.cost = cost;
        this.exchangeId = exchangeId;
        this.orderSide = orderSide;
        this.contractType = contractType;
        this.lastUpdated = lastUpdated;
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getStrategyId() {
        return strategyId;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getCost() {
        return cost;
    }

    public int getExchangeId() {
        return exchangeId;
    }

    public int getOrderSide() {
        return orderSide;
    }

    public int getContractType() {
        return contractType;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public long getId() {
        return id;
    }
}
