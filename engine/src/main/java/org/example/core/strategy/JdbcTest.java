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

import com.google.common.collect.Lists;
import org.example.core.enums.Side;
import org.example.core.order.BaseOrder;
import org.example.core.order.GridOrder;
import org.example.core.enums.OrderState;
import org.example.core.order.Trade;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JdbcTest {
    static String url = "jdbc:mysql://localhost:3306/trade";
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

    public static GridVo selectGridVo(String name) {
        String sql = "select * from trade.`grid` where `name`=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new GridVo(resultSet.getString("name"),
                        resultSet.getInt("gridNumber"),
                        resultSet.getDouble("centralPrice"),
                        resultSet.getDouble("gridAmount"),
                        resultSet.getTimestamp("updateTime"));
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertAndDeleteBeforeGridVo(GridVo gridVo) {
        try {
            try (PreparedStatement statement = conn.prepareStatement("DELETE FROM trade.grid WHERE name = ?")) {
                statement.setString(1, gridVo.getName());
                statement.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String sql = "insert into trade.`grid`  (name, centralPrice, gridAmount, updateTime, gridNumber) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, gridVo.getName());
            statement.setDouble(2, gridVo.getCentralPrice());
            statement.setDouble(3, gridVo.getGridAmount());
            statement.setTimestamp(4, gridVo.getUpdateTime());
            statement.setInt(5, gridVo.getGridNumber());
            statement.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }

    public static List<GridOrder> selectNotTradeOrder() {
        String sql = "select * from trade.`virtualOrder` where status = 0";
        try {
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    ArrayList<GridOrder> gridOrderVos = new ArrayList<>();
                    while (resultSet.next()) {
                        String id = resultSet.getString("id");
                        String symbol = resultSet.getString("symbol");
                        int gridIndex = resultSet.getInt("gridIndex");
                        double avgPrice = resultSet.getDouble("avgPrice");
                        double executedQuantity = resultSet.getDouble("executedQuantity");
                        int orderSide = resultSet.getInt("side");
                        long updateTime = resultSet.getLong("updateTime");
                        GridOrder gridOrderVo = new GridOrder(id, symbol, gridIndex, avgPrice, executedQuantity, OrderState.NEW, orderSide, updateTime);
                        gridOrderVos.add(gridOrderVo);
                    }
                    return gridOrderVos;
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateOrder(String id, double avgPrice, double executedQuantity, int orderState) {
        String sql = "update trade.virtualOrder set avgPrice = ? , executedQuantity = ? , status = ? where id = ?";
        try {
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setObject(1, avgPrice);
                statement.setObject(2, executedQuantity);
                statement.setObject(3, orderState);
                statement.setObject(4, id);
                statement.execute();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<GridOrder> selectAllWorkerOrder() {
        String sql = "select * from trade.`virtualOrder`";
        try {
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    ArrayList<GridOrder> gridOrderVos = new ArrayList<>();
                    while (resultSet.next()) {
                        String id = resultSet.getString("id");
                        String symbol = resultSet.getString("symbol");
                        int gridIndex = resultSet.getInt("gridIndex");
                        double avgPrice = resultSet.getDouble("avgPrice");
                        double executedQuantity = resultSet.getDouble("executedQuantity");
                        int orderSide = resultSet.getInt("side");
                        int status = resultSet.getInt("status");
                        long updateTime = resultSet.getLong("updateTime");
                        GridOrder gridOrderVo = new GridOrder(id, symbol, gridIndex, avgPrice, executedQuantity, OrderState.orderState(status), orderSide, updateTime);
                        gridOrderVos.add(gridOrderVo);
                    }
                    return gridOrderVos;
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteOrder(String id) {
        try {
            try (PreparedStatement statement = conn.prepareStatement("DELETE FROM trade.virtualOrder WHERE id = ?")) {
                statement.setString(1, id);
                statement.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static void insertAndDeleteBeforeOrder(GridOrder gridOrder) {
        int gridIndex = gridOrder.getGridIndex();
        String sql = "DELETE FROM trade.virtualOrder WHERE gridIndex = ?";
        try {
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, gridIndex);
                statement.execute();
            }
            insertOrder(gridOrder);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertOrder(GridOrder gridOrder) {
        String sql = "insert into trade.virtualOrder (id, symbol, gridIndex, price, quantity, side, status,updateTime) VALUES (?, ?, ?, ?, ?, ?, ?,?)";
        try {
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setObject(1, gridOrder.getOrderId());
                statement.setObject(2, gridOrder.getSymbol());
                statement.setObject(3, gridOrder.getGridIndex());
                statement.setObject(4, gridOrder.getPrice());
                statement.setObject(5, gridOrder.getQuantity());
                statement.setObject(6, gridOrder.getSide());
                statement.setObject(7, gridOrder.getOrderState().getState());
                statement.setObject(8, gridOrder.getUpdateTime());
                statement.execute();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 查找所有未成交的订单
     * NEW(0),
     * PARTIALLY_FILLED(1)
     *
     * @return
     */
    public static List<BaseOrder> selectNotTradeOrders() {
        return selectOrdersByStatus(Lists.newArrayList(OrderState.NEW.getState(), OrderState.PARTIALLY_FILLED.getState()));
    }

    /**
     * 查找所有成交的订单
     * PARTIALLY_FILLED(1)
     * FILLED(2)
     */
    public static List<BaseOrder> selectTradeOrders() {
        return selectOrdersByStatus(Lists.newArrayList(OrderState.FILLED.getState(), OrderState.PARTIALLY_FILLED.getState()));
    }


    public static List<BaseOrder> selectOrdersByStatus(List<Integer> orderStatus) {
        String sql = "select * from trade.binance_orders where status in (%s)";

        try {
            try (PreparedStatement statement = conn.prepareStatement(String.format(sql, orderStatus.stream().map(i -> "?").collect(Collectors.joining(","))))) {
                for (int i = 0; i < orderStatus.size(); i++) {
                    statement.setInt(i + 1, orderStatus.get(i));
                }
                try (ResultSet resultSet = statement.executeQuery()) {
                    ArrayList<BaseOrder> baseOrders = new ArrayList<>();
                    while (resultSet.next()) {
                        Long orderId = resultSet.getLong("order_id");
                        String symbol = resultSet.getString("symbol");
                        String clientOrderId = resultSet.getString("client_order_id");
                        BigDecimal price = resultSet.getBigDecimal("price");
                        BigDecimal origQty = resultSet.getBigDecimal("orig_qty");
                        BigDecimal executedQty = resultSet.getBigDecimal("executed_qty");
                        BigDecimal cummulativeQuoteQty = resultSet.getBigDecimal("cummulative_quote_qty");
                        OrderState status = OrderState.orderState(resultSet.getInt("status"));
                        Side side = Side.side(resultSet.getInt("side"));
                        Long time = resultSet.getLong("time");
                        Long updateTime = resultSet.getLong("update_time");

                        BaseOrder baseOrder = new BaseOrder(
                                orderId,
                                symbol,
                                clientOrderId,
                                price,
                                origQty,
                                executedQty,
                                cummulativeQuoteQty,
                                status,
                                side,
                                time,
                                updateTime
                        );
                        baseOrders.add(baseOrder);
                    }
                    return baseOrders;
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void insertOrders(List<Map<String, Object>> orders) {
        String sql = "INSERT INTO binance_orders (order_id, symbol, order_list_id, client_order_id, price, orig_qty, executed_qty, " +
                "cummulative_quote_qty, status, time_in_force, type, side, stop_price, iceberg_qty, time, update_time, " +
                "is_working, orig_quote_order_qty, working_time, self_trade_prevention_mode) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            for (Map<String, Object> order : orders) {
                initStateMent(statement, order);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertOrder(Map<String, Object> order) {
        String sql = "INSERT INTO binance_orders (order_id, symbol, order_list_id, client_order_id, price, orig_qty, executed_qty, " +
                "cummulative_quote_qty, status, time_in_force, type, side, stop_price, iceberg_qty, time, update_time, " +
                "is_working, orig_quote_order_qty, working_time, self_trade_prevention_mode) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            initStateMent(statement, order);
            statement.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void initStateMent(PreparedStatement statement, Map order) throws SQLException {
        statement.setLong(1, (Long) order.get("orderId"));
        statement.setString(2, (String) order.get("symbol"));
        statement.setLong(3, ((Number) order.get("orderListId")).longValue());
        statement.setString(4, (String) order.get("clientOrderId"));
        statement.setBigDecimal(5, new BigDecimal((String) order.get("price")));
        statement.setBigDecimal(6, new BigDecimal((String) order.get("origQty")));
        statement.setBigDecimal(7, new BigDecimal((String) order.get("executedQty")));
        statement.setBigDecimal(8, new BigDecimal((String) order.get("cummulativeQuoteQty")));
        statement.setInt(9, OrderState.orderState((String) order.get("status")).getState());
        statement.setString(10, (String) order.get("timeInForce"));
        statement.setString(11, (String) order.get("type"));
        statement.setInt(12, Side.side((String) order.get("side")).getSide());
        statement.setBigDecimal(13, new BigDecimal((String) order.get("stopPrice")));
        statement.setBigDecimal(14, new BigDecimal((String) order.get("icebergQty")));
        statement.setLong(15, ((Number) order.get("time")).longValue());
        statement.setLong(16, ((Number) order.get("updateTime")).longValue());
        statement.setBoolean(17, (Boolean) order.get("isWorking"));
        statement.setBigDecimal(18, new BigDecimal((String) order.get("origQuoteOrderQty")));
        statement.setLong(19, ((Number) order.get("workingTime")).longValue());
        statement.setString(20, (String) order.get("selfTradePreventionMode"));

    }

    public static void deleteOrders(String symbol, long start, long end) {
        String sql = "DELETE FROM binance_orders WHERE symbol = ? ";

        // 如果 start 不为空，则添加时间范围的上限条件
        if (start > 0) {
            sql += " AND time >= ?";
        }

        // 如果 end 不为空，则添加时间范围的上限条件
        if (end > 0) {
            sql += " AND time <= ?";
        }

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, symbol);

            if (start > 0) {
                statement.setLong(2, start);
            }
            if (end > 0) {
                statement.setLong(3, end);
            }
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting orders: " + e.getMessage(), e);
        }
    }

    public static void deleteTrades(String symbol, long start, long end) {
        String sql = "DELETE FROM binance_trades WHERE symbol = ? ";

        // 如果 start 不为空，则添加时间范围的上限条件
        if (start > 0) {
            sql += " AND time >= ?";
        }

        // 如果 end 不为空，则添加时间范围的上限条件
        if (end > 0) {
            sql += " AND time <= ?";
        }

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, symbol);

            if (start > 0) {
                statement.setLong(2, start);
            }
            if (end > 0) {
                statement.setLong(3, end);
            }
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting orders: " + e.getMessage(), e);
        }
    }


    public static List<BaseOrder> findAllOrder(String symbol) {
        String sql = "SELECT * FROM binance_orders WHERE symbol = ? ORDER BY time";

        ArrayList<BaseOrder> baseOrders = new ArrayList<>();
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, symbol);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    baseOrders.add(new BaseOrder(
                            resultSet.getLong("order_id"),
                            resultSet.getString("symbol"),
                            resultSet.getString("client_order_id"),
                            resultSet.getBigDecimal("price"),
                            resultSet.getBigDecimal("orig_qty"),
                            resultSet.getBigDecimal("executed_qty"),
                            resultSet.getBigDecimal("cummulative_quote_qty"),
                            OrderState.orderState(resultSet.getInt("status")),  // 假设 OrderState 是一个枚举类型
                            Side.side(resultSet.getInt("side")),          // 假设 Side 是一个枚举类型
                            resultSet.getLong("time"),
                            resultSet.getLong("update_time")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding last order: " + e.getMessage(), e);
        }

        return baseOrders;  // 如果没有找到任何订单，返回 null
    }


    public static BaseOrder findLastOrder(String symbol) {
        String sql = "SELECT * FROM binance_orders WHERE symbol = ? ORDER BY time DESC LIMIT 1";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, symbol);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new BaseOrder(
                            resultSet.getLong("order_id"),
                            resultSet.getString("symbol"),
                            resultSet.getString("client_order_id"),
                            resultSet.getBigDecimal("price"),
                            resultSet.getBigDecimal("orig_qty"),
                            resultSet.getBigDecimal("executed_qty"),
                            resultSet.getBigDecimal("cummulative_quote_qty"),
                            OrderState.orderState(resultSet.getInt("status")),  // 假设 OrderState 是一个枚举类型
                            Side.side(resultSet.getInt("side")),          // 假设 Side 是一个枚举类型
                            resultSet.getLong("time"),
                            resultSet.getLong("update_time")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding last order: " + e.getMessage(), e);
        }

        return null;  // 如果没有找到任何订单，返回 null
    }


    //===========================================Trade================================================
    // 批量插入 Trade 数据
    public static void insertTrades(List<Map<String, Object>> trades) {
        String sql = "INSERT INTO binance_trades (symbol, id, order_id, order_list_id, price, qty, quote_qty, commission, commission_asset, time, is_buyer, is_maker, is_best_match) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            for (Map<String, Object> trade : trades) {
                statement.setString(1, (String) trade.get("symbol"));
                statement.setLong(2, ((Number) trade.get("id")).longValue());
                statement.setLong(3, ((Number) trade.get("orderId")).longValue());
                statement.setLong(4, ((Number) trade.get("orderListId")).longValue());
                statement.setBigDecimal(5, new BigDecimal((String) trade.get("price")));
                statement.setBigDecimal(6, new BigDecimal((String) trade.get("qty")));
                statement.setBigDecimal(7, new BigDecimal((String) trade.get("quoteQty")));
                statement.setBigDecimal(8, new BigDecimal((String) trade.get("commission")));
                statement.setString(9, (String) trade.get("commissionAsset"));
                statement.setLong(10, ((Number) trade.get("time")).longValue());
                statement.setBoolean(11, (Boolean) trade.get("isBuyer"));
                statement.setBoolean(12, (Boolean) trade.get("isMaker"));
                statement.setBoolean(13, (Boolean) trade.get("isBestMatch"));
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // 插入单个 Trade 数据
    public static void insertOne(Map<String, Object> trade) {
        String sql = "INSERT INTO binance_trades (symbol, id, order_id, order_list_id, price, qty, quote_qty, commission, commission_asset, time, is_buyer, is_maker, is_best_match) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, (String) trade.get("symbol"));
            statement.setLong(2, ((Number) trade.get("id")).longValue());
            statement.setLong(3, ((Number) trade.get("orderId")).longValue());
            statement.setLong(4, ((Number) trade.get("orderListId")).longValue());
            statement.setBigDecimal(5, new BigDecimal((String) trade.get("price")));
            statement.setBigDecimal(6, new BigDecimal((String) trade.get("qty")));
            statement.setBigDecimal(7, new BigDecimal((String) trade.get("quoteQty")));
            statement.setBigDecimal(8, new BigDecimal((String) trade.get("commission")));
            statement.setString(9, (String) trade.get("commissionAsset"));
            statement.setLong(10, ((Number) trade.get("time")).longValue());
            statement.setBoolean(11, (Boolean) trade.get("isBuyer"));
            statement.setBoolean(12, (Boolean) trade.get("isMaker"));
            statement.setBoolean(13, (Boolean) trade.get("isBestMatch"));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Trade findLastTrader(String symbol) {
        String sql = "SELECT * FROM binance_trades WHERE symbol = ? ORDER BY time DESC LIMIT 1";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, symbol);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return createTrade(resultSet);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // 根据 orderId 查询对应的 Trade
    public static List<Trade> selectTradesByOrderId(String orderId) {
        String sql = "SELECT * FROM binance_trades WHERE order_id = ?";
        List<Trade> trades = new ArrayList<>();

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    trades.add(createTrade(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return trades;
    }

    private static Trade createTrade(ResultSet resultSet) throws SQLException {
        return new Trade(resultSet.getString("symbol"),
                resultSet.getLong("id"),
                resultSet.getLong("order_id"),
                resultSet.getLong("order_list_id"),
                resultSet.getBigDecimal("price"),
                resultSet.getBigDecimal("qty"),
                resultSet.getBigDecimal("quote_qty"),
                resultSet.getBigDecimal("commission"),
                resultSet.getString("commission_asset"),
                resultSet.getLong("time"),
                resultSet.getBoolean("is_buyer"),
                resultSet.getBoolean("is_maker"),
                resultSet.getBoolean("is_best_match"));
    }

}
