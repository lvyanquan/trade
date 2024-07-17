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

import java.util.List;
import java.util.Map;


/**
 * 搞一个服务一直监听订单信息 然后进行更新 大概是一个更好方式
 * 这里的接口都是直接查询数据库即可
 * 需要 进行数据库的设计即可
 * <p>
 * 钉钉推送这部分代码也调整好即可
 */
public interface OrderManager {
    void init(Map<String,Object> parametres) throws Exception;

    void createOrderVo(OrderDto orderDto);

    OrderVo getOrderInfoById(OrderQueryDto orderDto);

    //获取从某个时间段开始的所有订单 有效/已取消/已完成
    //GET /api/v3/allOrders
    List<OrderVo> getOrderVoList(OrderQueryDto orderQueryDto);


    List<OrderResponseInfo> getOrderList(OrderQueryDto orderQueryDto);

}
