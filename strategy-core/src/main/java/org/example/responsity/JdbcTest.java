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

package org.example.responsity;

import org.example.client.dto.OrderDto;
import org.example.client.dto.OrderQueryDto;
import org.example.client.dto.OrderResponseInfo;
import org.example.client.vo.OrderVo;
import org.example.client.vo.TradeVo;
import org.example.enums.Exchange;
import org.example.enums.OrderStatus;
import org.example.model.currency.CurrencyRegister;
import org.example.model.enums.OrderSide;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class JdbcTest {
    static String url = "jdbc:mysql://localhost:3306/dujie";
    static String username = "root";
    static String password = "rootroot";

    public static Connection conn;

    static {
        try {
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<OrderVo> getOnlyOrderWithNotTrade() {
        String sql = "select * from trade.`order` where order_status in (0,1,2)";
        try {
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    ArrayList<OrderVo> orderVos = new ArrayList<>();
                    while (resultSet.next()) {
                        String id = resultSet.getString("id");
                        String currency = resultSet.getString("currency");
                        int status = resultSet.getInt("order_status");
                        double price = resultSet.getDouble("price");
                        double qty = resultSet.getDouble("qty");
                        double amount = resultSet.getDouble("amount");
                        String orderSide = resultSet.getString("order_side");
                        OrderVo orderVo = new OrderVo(id, null, Exchange.BINANCE, CurrencyRegister.getCurrency(currency).get(), OrderStatus.getOrderStatus(status), OrderSide.valueOf(orderSide), -1, qty, amount, price, null, 0, 0, 0);
                        orderVos.add(orderVo);
                    }
                    return orderVos;
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void insert(double high, double low, double averg, Timestamp timestamp) throws Exception {
        // JDBC连接信息
        // SQL语句
        String sql = " INSERT INTO trade.usdt_price (high, low, average, create_time) VALUES (?, ?, ?, ?)";
        // 创建Statement对象并执行SQL语句
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setObject(1, high);
            statement.setObject(2, low);
            statement.setObject(3, averg);
            statement.setObject(4, timestamp.getTime());
            statement.execute();
        }
    }


    public static OrderVo selectOrderByCliendId(String id, CurrencyRegister currencyRegister, Connection conn) {

        try { // JDBC连接信息
            // SQL语句
            String sql = "select order.exchange_order_id, order.exchange_id,order.currency,order.order_status ,order.qty,order.order_side,order.create_time,order.price,order.amount from trade.order  where id = ? ";
            // 创建Statement对象并执行SQL语句
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setObject(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String exchangeOrderId = resultSet.getString("exchange_order_id");
                        int exchangeId = resultSet.getInt("exchange_id");
                        String currency = resultSet.getString("currency");
                        int orderStatus = resultSet.getInt("order_status");
                        String orderSide = resultSet.getString("order_side");
                        long createTime = resultSet.getLong("create_time");
                        double price = resultSet.getDouble("price");
                        double amount = resultSet.getDouble("amount");
                        double orderQty = resultSet.getDouble("qty");
                        String orderDetail = "select trade_id, price,qty,quote_qty, commission,trade_time from trade.order_detail  where exchange_order_id = ? ";

                        try (PreparedStatement statement1 = conn.prepareStatement(orderDetail)) {
                            statement1.setObject(1, exchangeOrderId);
                            try (ResultSet resultSet1 = statement1.executeQuery()) {
                                ArrayList<TradeVo> tradeVos = new ArrayList<>();
                                while (resultSet1.next()) {
                                    double tradePrice = resultSet1.getDouble("price");
                                    String tradeId = resultSet1.getString("trade_id");
                                    double qty = resultSet1.getDouble("qty");
                                    double quoteQty = resultSet1.getDouble("quote_qty");
                                    double commission = resultSet1.getDouble("commission");
                                    long tradeTime = resultSet1.getLong("trade_time");

                                    TradeVo tradeVo = new TradeVo(tradeId, exchangeOrderId, tradePrice, qty, quoteQty, commission, tradeTime);
                                    tradeVos.add(tradeVo);
                                }

                                if (tradeVos.isEmpty()) {
                                    //此时还未成交
                                    return new OrderVo(id, exchangeOrderId, Exchange.getExchange(exchangeId), currencyRegister.getCurrency(currency).get(), OrderStatus.getOrderStatus(orderStatus), OrderSide.valueOf(orderSide), createTime, orderQty, amount, price, null, 0, 0, 0);

                                } else {
                                    double countQty = 0;
                                    double countQuoteQty = 0;
                                    double countCommion = 0;
                                    for (TradeVo tradeVo : tradeVos) {
                                        countQty += tradeVo.getQuantity();
                                        countQuoteQty += tradeVo.getAmount();
                                        countCommion += tradeVo.getCommission();
                                    }

                                    return new OrderVo(id, exchangeOrderId, Exchange.getExchange(exchangeId), CurrencyRegister.getCurrency(currency).get(), OrderStatus.getOrderStatus(orderStatus), OrderSide.valueOf(orderSide), createTime, orderQty, amount, price, tradeVos, countQty, countQuoteQty, countCommion);

                                }


                            }

                        }


                    }
                    return null;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertOrder(OrderDto orderDto, Connection conn) {
        String sql = "INSERT INTO trade.order (id,strategy_id, price, amount, quantity,order_side, currency, exchange_id, create_time,contract_type) VALUES (?, ?,?, ?, ?, ?, ?, ?,?)";
        try {
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setObject(1, orderDto.getVirtualId());
                statement.setObject(2, orderDto.getStrategyId());
                statement.setObject(3, orderDto.getPrice());
                statement.setObject(4, orderDto.getAmount());
                statement.setObject(5, orderDto.getQuantity());
                statement.setObject(6, orderDto.getOrderSide().name());
                statement.setObject(7, orderDto.getCurrency().symbol());
                statement.setObject(8, orderDto.getExchange().getId());
                statement.setObject(9, orderDto.getTime());
                statement.setObject(10, orderDto.getContractType().getType());
                statement.execute();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertOrderMap(OrderDto orderDto, Connection conn) {
        String sql = "INSERT INTO trade.order_map (order_client_id_a, order_client_id_b) VALUES (?, ?)";
        try {
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setObject(1, orderDto.getVirtualId());
                statement.setObject(2, orderDto.getVirtualIdB());
                statement.execute();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static OrderVo selectOrderByExchangeOrderId(String id, Connection conn) {

        try {  // JDBC连接信息
            // SQL语句
            String sql = "select order.id,order.exchange_order_id, order.exchange_id,order.qty,order.currency,order.order_status ,order.order_side,order.create_time,order.price,order.amount from trade.order  where exchange_order_id = ? ";
            // 创建Statement对象并执行SQL语句
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setObject(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String clientId = resultSet.getString("id");
                        String exchangeOrderId = resultSet.getString("exchange_order_id");
                        int exchangeId = resultSet.getInt("exchange_id");
                        String currency = resultSet.getString("currency");
                        int orderStatus = resultSet.getInt("order_status");
                        String orderSide = resultSet.getString("order_side");
                        long createTime = resultSet.getLong("create_time");
                        double price = resultSet.getDouble("price");
                        double amount = resultSet.getDouble("amount");
                        double orderQty = resultSet.getDouble("qty");
                        String orderDetail = "select trade_id, price,qty,quote_qty, commission,trade_time from trade.order_detail  where exchange_order_id = ? ";

                        try (PreparedStatement statement1 = conn.prepareStatement(orderDetail)) {
                            statement1.setObject(1, exchangeOrderId);
                            try (ResultSet resultSet1 = statement1.executeQuery()) {
                                ArrayList<TradeVo> tradeVos = new ArrayList<>();
                                while (resultSet1.next()) {
                                    double tradePrice = resultSet1.getDouble("price");
                                    String tradeId = resultSet1.getString("trade_id");
                                    double qty = resultSet1.getDouble("qty");
                                    double quoteQty = resultSet1.getDouble("quote_qty");
                                    double commission = resultSet1.getDouble("commission");
                                    long tradeTime = resultSet.getLong("trade_time");

                                    TradeVo tradeVo = new TradeVo(tradeId, exchangeOrderId, tradePrice, qty, quoteQty, commission, tradeTime);
                                    tradeVos.add(tradeVo);
                                }

                                if (tradeVos.isEmpty()) {
                                    //此时还未成交
                                    return new OrderVo(clientId, exchangeOrderId, Exchange.getExchange(exchangeId), CurrencyRegister.getCurrency(currency).get(), OrderStatus.getOrderStatus(orderStatus), OrderSide.valueOf(orderSide), createTime, orderQty, amount, price, null, 0, 0, 0);

                                } else {
                                    double countQty = 0;
                                    double countQuoteQty = 0;
                                    double countCommion = 0;
                                    for (TradeVo tradeVo : tradeVos) {
                                        countQty += tradeVo.getQuantity();
                                        countQuoteQty += tradeVo.getAmount();
                                        countCommion += tradeVo.getCommission();
                                    }

                                    return new OrderVo(clientId, exchangeOrderId, Exchange.getExchange(exchangeId), CurrencyRegister.getCurrency(currency).get(), OrderStatus.getOrderStatus(orderStatus), OrderSide.valueOf(orderSide), createTime, orderQty, amount, price, tradeVos, countQty, countQuoteQty, countCommion);

                                }
                            }
                        }


                    }
                    return null;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static List<OrderVo> selectOrderList(OrderQueryDto orderQueryDto, Connection conn, CurrencyRegister currencyRegister) {

        try {  // JDBC连接信息
            // SQL语句
            String sql = "select `order`.id,\n" +
                    "       `order`.exchange_order_id,\n" +
                    "       `order`.currency,\n" +
                    "       `order`.order_status,\n" +
                    "       `order`.order_side,\n" +
                    "       `order`.amount,\n" +
                    "       `order`.create_time,\n" +
                    "       `order`.qty as oqty,\n" +
                    "       od.trade_id,\n" +
                    "       od.price,\n" +
                    "       od.qty,\n" +
                    "       od.quote_qty,\n" +
                    "       od.trade_time,\n" +
                    "       od.commission" +
                    "       from  trade.`order` left join trade.order_detail od on `order`.id = od.order_id where 1= 1";

            if (orderQueryDto != null) {
                if (orderQueryDto.getVirtualId() != null) {
                    sql += " and `order`.id like '"+orderQueryDto.getVirtualId()+"%'";
                }
                if (orderQueryDto.getExchangeOrderId() != null) {
                    sql += " and `order`.exchange_order_id = ?";
                }
                if (orderQueryDto.getCurrency() != null) {
                    sql += " and `order`.currency = ?";
                }

                if (orderQueryDto.getOrderStatus() != null) {
                    String a = orderQueryDto.getOrderStatus().stream().map(OrderStatus::getStatus)
                            .map(String::valueOf) // 将Integer转换为String
                            .collect(Collectors.joining(","));
                    sql += " and `order`.order_status in (" + a + ")";
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

                    if (orderQueryDto.getExchangeOrderId() != null) {
                        i++;
                        statement.setObject(i, orderQueryDto.getExchangeOrderId());
                    }
                    if (orderQueryDto.getCurrency() != null) {
                        i++;
                        statement.setObject(i, orderQueryDto.getCurrency().symbol());
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

                HashMap<String, OrderVo> orderMaps = new HashMap<>();

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String clientId = resultSet.getString("id");
                        String exchangeOrderId = resultSet.getString("exchange_order_id");
                        String currency = resultSet.getString("currency");
                        int orderStatus = resultSet.getInt("order_status");
                        String orderSide = resultSet.getString("order_side");
                        //订单的币种下单数量
                        double orderQty = resultSet.getDouble("oqty");
                        long createTime = resultSet.getLong("create_time");
                        double price = resultSet.getDouble("price");
                        //订单的金额
                        double amount = resultSet.getDouble("amount");
                        OrderVo orderVo = null;
                        if (orderMaps.containsKey(clientId)) {
                            orderVo = orderMaps.get(clientId);
                        } else {
                            orderVo = new OrderVo(clientId, exchangeOrderId, Exchange.BINANCE, CurrencyRegister.getCurrency(currency).get(), OrderStatus.getOrderStatus(orderStatus), OrderSide.valueOf(orderSide), createTime, orderQty, amount, price, new ArrayList<>(), 0, 0, 0);
                            orderMaps.put(clientId, orderVo);
                        }

                        String tradeId = resultSet.getString("trade_id");
                        double tradePrice = resultSet.getDouble("price");
                        double tradeQty = resultSet.getDouble("qty");
                        double tradeAmount = resultSet.getDouble("quote_qty");
                        double commission = resultSet.getDouble("commission");
                        long tradeTime = resultSet.getLong("trade_time");

                        TradeVo tradeVo = new TradeVo(tradeId, exchangeOrderId, tradePrice, tradeQty, tradeAmount, commission, tradeTime);
                        orderVo.getTrades().add(tradeVo);
                        orderVo.setCommission(orderVo.getCommission() + tradeVo.getCommission());
                        orderVo.setTradeAmount(orderVo.getTradeAmount() + tradeAmount);
                        orderVo.setTradeQuantity(orderVo.getTradeQuantity() + tradeQty);
                    }
                    return new ArrayList<>(orderMaps.values());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateOrderCancel(String id) {
        //.getId(),orderResponseInfo.getExchangeOrderId(), orderResponseInfo.getOrderStatus()
        String sql = "update trade.order set order_status = ?  where id = ?";
        try {
            try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setObject(1,OrderStatus.CANCEL);
                preparedStatement.setObject(2,id);

                preparedStatement.execute();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateOrderStatus(OrderResponseInfo orderResponseInfo) {
        //.getId(),orderResponseInfo.getExchangeOrderId(), orderResponseInfo.getOrderStatus()
        String sql = "update trade.order set order_status = ? , exchange_order_id =? ,qty = ? where id = ?";
        try {
            try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setObject(1, orderResponseInfo.getOrderStatus().getStatus());
                preparedStatement.setObject(2, orderResponseInfo.getExchangeOrderId());
                preparedStatement.setObject(3, orderResponseInfo.getQuantity());
                preparedStatement.setObject(4, orderResponseInfo.getId());

                preparedStatement.execute();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertOrderDetail(TradeVo orderVo) {
        String sql = "INSERT INTO trade.order_detail (order_id,trade_id,exchange_order_id,price,quantity,amount,commission,trade_time) VALUES (?,?,?,?,?,?,?,?) ";
        try {
            try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setObject(1, orderVo.getOrderId());
                preparedStatement.setObject(2, orderVo.getTradeId());
                preparedStatement.setObject(3, orderVo.getExchangeOrderId());
                preparedStatement.setObject(4, orderVo.getPrice());
                preparedStatement.setObject(5, orderVo.getQuantity());
                preparedStatement.setObject(6, orderVo.getAmount());
                preparedStatement.setObject(7, orderVo.getCommission());
                preparedStatement.setObject(8, orderVo.getTrade_time());

                preparedStatement.execute();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
