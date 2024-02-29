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

package com.example.web_kline;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class JdbcTest {
    static String url = "jdbc:mysql://localhost:3306/dujie";
    static String username = "root";
    static String password = "rootroot";

    static Connection conn;

    static {
        try {
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void insert(double high, double low, double averg, Timestamp timestamp) throws Exception {
        // JDBC连接信息
        // SQL语句
        String sql = " INSERT INTO dujie.usdt_price (high, low, averg, create_time) VALUES (?, ?, ?, ?)";
        // 创建Statement对象并执行SQL语句
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setObject(1, high);
            statement.setObject(2, low);
            statement.setObject(3, averg);
            statement.setObject(4, timestamp.getTime());
            statement.execute();
        }


    }
}
