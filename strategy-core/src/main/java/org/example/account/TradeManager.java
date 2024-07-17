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

package org.example.account;

import org.example.client.dto.OrderDto;
import org.example.model.currency.Currency;
import org.example.model.enums.OrderType;
import org.example.model.market.KlineModule;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public abstract class TradeManager {

    public abstract void init();

    //根据api获取当前的手续费
    public abstract BigDecimal handlingCharge(Currency currency, OrderType orderType);


    //下单 异步返回订单id
    // 如果有效期间下单失败 返回null
    public abstract CompletableFuture<String> trade(OrderDto orderDto, KlineModule position, TradeOptions options);


    //取消订单
    public abstract boolean cancelTradeByVirtualId(String virtualId);


    //下单接口 有不同的实现 内存级别 实际交易所下单（定时器更新 加个定时参数配置时间即可）
    //下单接口是否是异步的（不同的下单接口  所以下单接口要抽象出来了 不同的交易所有不同实现也行） 下单有一个过期时间(10s内不成交直接挂掉)
    //下单接口做个list 排队下单？ 或者当前有在等待下单的接口不能下单即可
    //平单接口也设计一个 即可
}
