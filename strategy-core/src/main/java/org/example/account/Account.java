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

import org.example.model.currency.Currency;
import org.example.order.OrderManager;

public abstract class Account {
    protected OrderManager orderManager;

    //获取当前的仓位信息即可
    public Account(OrderManager orderManager) {
        this.orderManager = orderManager;
    }

    //账户系统 管理仓位 不同的仓位管理 待定
    //最多同时持有仓数量   每个数组
    //展示当前Account的信息
    //搞一个线程管理即可 抽取一个方法作为当前accunt管理的订单信息
    //在init方法里进行管理即可 this.clientIdPrefix = clientIdPrefix;
    public abstract void init();

    //获取下一单仓位
   public abstract double nextAvaliableAmount(Currency currency);

   //当前已经持有的仓位
   public abstract double currentUsedAmount(Currency currency);

   public abstract double firstUsedAmount(Currency currency);

    public abstract boolean order(Currency currency, double amount);

   public abstract boolean releas(Currency currency, double amount);

}
