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

package org.example.trade;

import org.example.client.dto.OrderDto;
import org.example.client.dto.OrderResponseInfo;
import org.example.client.vo.OrderVo;

public interface TradeClient {

    void newOrderAsync(OrderDto orderDto);

    /**
     * 取消订单 内存里的 挂单的
     */
    void cancelOrder(OrderDto orderDto);


    void init();


}
