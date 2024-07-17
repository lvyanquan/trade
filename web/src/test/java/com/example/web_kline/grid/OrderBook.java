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

package com.example.web_kline.grid;

import com.example.web_kline.grid.order.Order;
import com.example.web_kline.grid.order.OrderStateEnum;
import com.example.web_kline.grid.order.VirtualOrder;

import java.util.ArrayList;
import java.util.List;

public class OrderBook {
    private List<Order> orders = new ArrayList();


    public OrderBook(List<Order> orders) {
        this.orders = orders;
    }

    public Order getOrder(int index) {
        return orders.get(index);
    }

    public Order getOrder(String vlientId) {
        for (Order order : orders) {
            if(order instanceof VirtualOrder &&  ((VirtualOrder)order).getVirtualId().equalsIgnoreCase(vlientId)){
               return order;
            }
        }
     return null;
    }


    public void replaceOrder(int index, Order order) {
        orders.set(index, order);
    }


    public int buyLatestOrder(double price) {
        for (int i = orders.size() - 1; i >= 0; i--) {
            if (orders.get(i).getState() == OrderStateEnum.INIT && price <= orders.get(i).getBuyPrice()) {
                return i;
            }
        }
        return -1;
    }

    public int nextBuyId(int i) {
        for (int index = i; index >= 0; index--) {
            Order order = orders.get(index);
            if (order.getState() == OrderStateEnum.INIT) {
                return index;
            }
        }
        return -1;
    }


    public int sellLatestOrder(double price) {
        for (int i = orders.size() - 1; i >= 0; i--) {
            if (orders.get(i).getState() == OrderStateEnum.BUYED && price >= orders.get(i).getSellPrice()) {
                return i;

            }
        }
        return -1;
    }

}
