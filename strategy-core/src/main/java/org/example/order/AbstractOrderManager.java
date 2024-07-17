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

package org.example.order;

import org.example.client.dto.OrderDto;
import org.example.client.dto.OrderQueryDto;
import org.example.client.dto.OrderResponseInfo;
import org.example.client.vo.OrderVo;
import org.example.model.currency.CurrencyRegister;
import org.example.responsity.JdbcTest;
import org.example.responsity.sql.OrderSql;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public abstract class AbstractOrderManager implements OrderManager {
    public Connection conn;

    public CurrencyRegister currencyRegister;

    public AbstractOrderManager(Connection conn, CurrencyRegister currencyRegister) {
        this.conn = conn;
        this.currencyRegister = currencyRegister;
    }

    public void init(Map<String, Object> parametres) throws Exception {

    }


    @Override
    public void createOrderVo(OrderDto orderDto) {
        JdbcTest.insertOrder(orderDto, conn);
        if (orderDto.getVirtualIdB() != null) {
            JdbcTest.insertOrderMap(orderDto, conn);
        }
    }

    @Override
    public OrderVo getOrderInfoById(OrderQueryDto orderDto) {
        if (orderDto.getVirtualId() != null) {
            return JdbcTest.selectOrderByCliendId(orderDto.getVirtualId(), currencyRegister, conn);
        } else if (orderDto.getExchangeOrderId() != null) {
            return JdbcTest.selectOrderByExchangeOrderId(orderDto.getExchangeOrderId(), conn);

        }
        throw new IllegalArgumentException("virtualId or exchangEid is not null");
    }

    @Override
    public List<OrderVo> getOrderVoList(OrderQueryDto orderQueryDto){

        return JdbcTest.selectOrderList(orderQueryDto, conn,currencyRegister);

    }

    @Override
    public List<OrderResponseInfo> getOrderList(OrderQueryDto orderQueryDto) {
      return OrderSql.selectOrderList(orderQueryDto,conn);
    }
}
