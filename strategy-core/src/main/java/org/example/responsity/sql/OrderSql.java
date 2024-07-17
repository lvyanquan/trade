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

import org.example.client.dto.OrderQueryDto;
import org.example.client.dto.OrderResponseInfo;
import org.example.enums.Exchange;
import org.example.enums.OrderStatus;
import org.example.model.currency.CurrencyRegister;
import org.example.model.enums.ContractType;
import org.example.model.enums.OrderSide;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrderSql {

    public static List<OrderResponseInfo> selectOrderList(OrderQueryDto orderQueryDto, Connection conn) {

        try {  // JDBC连接信息
            // SQL语句
            String sql = "select `order`.id,\n" +
                    "       `order`.exchange_order_id,\n" +
                    "       `order`.currency,\n" +
                    "       `order`.order_status,\n" +
                    "       `order`.strategy_id,\n" +
                    "       `order`.exchange_id,\n" +
                    "       `order`.order_side,\n" +
                    "       `order`.amount,\n" +
                    "       `order`.quantity,\n" +
                    "       `order`.price,\n" +
                    "       `order`.create_time\n" +
                    "       from  trade.`order` where 1= 1";

            if (orderQueryDto != null) {
                if (orderQueryDto.getVirtualId() != null) {
                    sql += " and `order`.id = ?";
                }
                if (orderQueryDto.getExchangeOrderId() != null) {
                    sql += " and `order`.exchange_order_id = ?";
                }
                if (orderQueryDto.getCurrency() != null) {
                    sql += " and `order`.currency = ?";
                }
                if (orderQueryDto.getExchange() != null) {
                    sql += " and `order`.exchange_id = ?";
                }

                if (orderQueryDto.getOrderStatus() != null) {
                    String a = orderQueryDto.getOrderStatus().stream().map(OrderStatus::getStatus)
                            .map(String::valueOf) // 将Integer转换为String
                            .collect(Collectors.joining(","));
                    sql += " and `order`.order_status in (" + a + ")";
                }

                if (orderQueryDto.getContractType() != null) {
                    sql += " and `order`.contract_type = ?";
                }

                if (orderQueryDto.getStrategyId() != null) {
                    sql += " and `order`.strategy_id = ?";
                }

                if (orderQueryDto.getOrderSide() != null) {
                    String a = orderQueryDto.getOrderSide().stream().map(Enum::name).map(i -> "'" + i + "'")
                            .collect(Collectors.joining(","));
                    sql += " and `order`.order_side in (" + a + ")";
                }

                if (orderQueryDto.getFromTime() > 0) {
                    sql += " and `order`.create_time >= ?";
                }

                if (orderQueryDto.getEndTime() > 0) {
                    sql += " and `order`.create_time <= ?";
                }
            }

            // 创建Statement对象并执行SQL语句
            try (PreparedStatement statement = conn.prepareStatement(sql)) {

                int i = 0;
                if (orderQueryDto != null) {

                    if (orderQueryDto.getVirtualId() != null) {
                        i++;
                        statement.setObject(i, orderQueryDto.getVirtualId());
                    }

                    if (orderQueryDto.getExchangeOrderId() != null) {
                        i++;
                        statement.setObject(i, orderQueryDto.getExchangeOrderId());
                    }

                    if (orderQueryDto.getStrategyId() != null) {
                        i++;
                        statement.setObject(i, orderQueryDto.getStrategyId());
                    }
                    if (orderQueryDto.getExchange() != null) {

                        i++;
                        statement.setObject(i, orderQueryDto.getExchange().getId());
                    }

                    if (orderQueryDto.getCurrency() != null) {
                        i++;
                        statement.setObject(i, orderQueryDto.getCurrency().symbol());
                    }

                    if (orderQueryDto.getContractType() != null) {
                        i++;
                        statement.setObject(i, orderQueryDto.getContractType().getType());
                    }


                    if (orderQueryDto.getFromTime() > 0) {
                        i++;
                        statement.setObject(i, orderQueryDto.getFromTime());
                    }

                    if (orderQueryDto.getEndTime() > 0) {
                        i++;
                        statement.setObject(i, orderQueryDto.getEndTime());
                    }
                }


                ArrayList<OrderResponseInfo> orderResponseInfos = new ArrayList<>();
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String clientId = resultSet.getString("id");
                        String exchangeOrderId = resultSet.getString("exchange_order_id");
                        int exchangeId = resultSet.getInt("exchange_id");
                        String strategyTd = resultSet.getString("strategy_id");
                        String currency = resultSet.getString("currency");
                        int orderStatus = resultSet.getInt("order_status");
                        int contractType = resultSet.getInt("contract_type");
                        int orderSide = resultSet.getInt("order_side");
                        //订单的币种下单数量
                        double quantity = resultSet.getDouble("quantity");
                        long createTime = resultSet.getLong("create_time");
                        double price = resultSet.getDouble("price");
                        //订单的金额
                        double amount = resultSet.getDouble("amount");
                        OrderResponseInfo orderResponseInfo = new OrderResponseInfo();
                        orderResponseInfo.setId(clientId);
                        orderResponseInfo.setExchangeOrderId(exchangeOrderId);
                        orderResponseInfo.setExchang(Exchange.getExchange(exchangeId));
                        orderResponseInfo.setCurrency(CurrencyRegister.getCurrency(currency).get());
                        orderResponseInfo.setOrderStatus(OrderStatus.getOrderStatus(orderStatus));
                        orderResponseInfo.setContractType(ContractType.getContractType(contractType));
                        orderResponseInfo.setOrderSide(OrderSide.getOrderSide(orderSide));
                        orderResponseInfo.setAmount(amount);
                        orderResponseInfo.setQuantity(quantity);
                        orderResponseInfo.setPrice(price);
                        orderResponseInfos.add(orderResponseInfo);
                        orderResponseInfo.setStrategyId(strategyTd);

                    }
                    return orderResponseInfos;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
