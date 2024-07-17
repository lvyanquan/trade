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

package org.example.enums;

public enum OrderStatus {
    //初始化 还未挂单
    NEW(0),
    //挂单
    PENDING(1),

    //部分成交
    PARTIALLY_FILLED(2),


    //完全成交
    FILLED(3),

    //取消
    CANCEL(4)
    ;

    int status;

    OrderStatus(int status) {
        this.status = status;
    }

    public static OrderStatus getOrderStatus(int status){
        for (OrderStatus value : OrderStatus.values()) {
            if(value.status == status){
                return value;
            }
        }
        throw new IllegalArgumentException("error order status: " + status);
    }

    public int getStatus() {
        return status;
    }
}
