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

package org.example.responsity.sql;

import org.example.enums.Exchange;
import org.example.model.currency.Currency;
import org.example.model.currency.CurrencyRegister;
import org.example.model.enums.ContractType;
import org.example.model.enums.OrderSide;
import org.example.model.position.PositionModel;
import org.example.model.position.PositionVo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PositionSql {
    private static String queryAllPositionSql = "select * from trade.positions where strategy_id=? and exchange_id =? and order_side=? and contract_type = ?";

    private static String queryPositionSql = "select * from trade.positions where strategy_id=? and exchange_id =? and order_side=? and contract_type = ? and symbol =?";

    private static String insertPositionSql = " INSERT INTO trade.positions (symbol, strategy_id, quantity, cost, last_updated, exchange_id, order_side, contract_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static String updatePositionSql = "UPDATE trade.positions t SET t.quantity = ?,t.cost=?,last_updated=?  WHERE t.id = ?";


    public static List<PositionVo> queryPositionsByStrategyId(String strategyId, Exchange exchange, OrderSide orderSide, ContractType contractType, Connection connection) throws Exception{
        ArrayList<PositionVo> datas = new ArrayList<>();

            try (PreparedStatement preparedStatement = connection.prepareStatement(queryAllPositionSql)) {
                preparedStatement.setObject(1, strategyId);
                preparedStatement.setObject(2, exchange.getId());
                preparedStatement.setObject(3, orderSide.getType());
                preparedStatement.setObject(4, contractType.getType());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        double cost = resultSet.getDouble("cost");
                        double quantity = resultSet.getDouble("quantity");
                        String symbol = resultSet.getString("symbol");
                        datas.add(new PositionVo(CurrencyRegister.getCurrency(symbol).get(), strategyId, quantity, cost));
                    }
                }
            }

        return datas;
    }

    public static PositionVo queryPositionsByStrategyIdAndCurrency(String strategyId, Exchange exchange, OrderSide orderSide, ContractType contractType, Currency currency, Connection connection) throws  Exception{

            try (PreparedStatement preparedStatement = connection.prepareStatement(queryPositionSql)) {
                preparedStatement.setObject(1, strategyId);
                preparedStatement.setObject(2, exchange.getId());
                preparedStatement.setObject(3, orderSide.getType());
                preparedStatement.setObject(4, contractType.getType());
                preparedStatement.setObject(5, currency.symbol());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        double cost = resultSet.getDouble("cost");
                        double quantity = resultSet.getDouble("quantity");
                        long id = resultSet.getLong("id");
                        PositionVo positionVo = new PositionVo(currency, strategyId, quantity, cost);
                        positionVo.setId(id);
                        return positionVo;
                    }
                }
            }

        return null;
    }

    public static void insert(PositionModel positionModel, Connection connection) throws Exception {
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertPositionSql)) {
            preparedStatement.setObject(1,positionModel.getSymbol());
            preparedStatement.setObject(2,positionModel.getStrategyId());
            preparedStatement.setObject(3,positionModel.getQuantity());
            preparedStatement.setObject(4,positionModel.getCost());
            preparedStatement.setObject(5,positionModel.getLastUpdated());
            preparedStatement.setObject(6,positionModel.getExchangeId());
            preparedStatement.setObject(7,positionModel.getOrderSide());
            preparedStatement.setObject(8,positionModel.getContractType());
            preparedStatement.execute();

        }

    }

    public static void update(PositionModel positionModel, Connection connection) throws Exception {

        try (PreparedStatement preparedStatement = connection.prepareStatement(updatePositionSql)) {
            preparedStatement.setObject(1,positionModel.getQuantity());
            preparedStatement.setObject(2,positionModel.getCost());
            preparedStatement.setObject(3,positionModel.getLastUpdated());
            preparedStatement.setObject(4,positionModel.getId());
            preparedStatement.execute();
        }
    }


}
