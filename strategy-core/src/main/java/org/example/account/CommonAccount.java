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

import org.example.client.dto.OrderQueryDto;
import org.example.client.vo.OrderVo;
import org.example.enums.OrderStatus;
import org.example.model.position.AmountPosition;
import org.example.model.currency.Currency;
import org.example.model.enums.OrderSide;
import org.example.order.OrderManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommonAccount extends Account {

    protected Map<Currency, List<AmountPosition>> taskPosition = new ConcurrentHashMap<>();
    protected AccountStragegy accountStragegy;

    protected String orderIdPrefix;

    protected OrderSide orderSide;

    public CommonAccount(OrderManager orderManager, AccountStragegy accountStragegy, OrderSide orderSide, String orderIdPrefix) {
        super(orderManager);
        this.accountStragegy = accountStragegy;
        this.orderIdPrefix = orderIdPrefix;
        this.orderSide = orderSide;
        if (orderSide == OrderSide.SELL_LONG || orderSide == OrderSide.BUY_SHORT) {
            throw new IllegalArgumentException("订单类型只支持买多单 或者 买空单 【BUY_LONG SELL_SHORT】");
        }
    }

    @Override
    public void init() {
        updateAmountPosition(true);
        new Thread(()->{
          while(true){
              try {
                  Thread.sleep(30000);
              } catch (InterruptedException e) {

              }
              updateAmountPosition(false);
          }
        }).start();
    }

    @Override
    public double nextAvaliableAmount(Currency currency) {
        List<AmountPosition> amountPosition = getAmountPosition(currency);
        for (AmountPosition position : amountPosition) {
            if (position.avaliableAmount() > 0) {
                return position.avaliableAmount();
            }
        }
        return 0;
    }

    @Override
    public double currentUsedAmount(Currency currency) {
        List<AmountPosition> amountPosition = getAmountPosition(currency);
        return amountPosition.stream().mapToDouble(AmountPosition::userAmount).sum();
    }

    @Override
    public double firstUsedAmount(Currency currency) {
        List<AmountPosition> amountPosition = getAmountPosition(currency);
        for (AmountPosition position : amountPosition) {
            if (position.userAmount() > 0) {
                return position.userAmount();
            }
        }
        return 0;
    }

    @Override
    public boolean order(Currency currency, double amount) {
        List<AmountPosition> amountPosition = getAmountPosition(currency);
        for (AmountPosition position : amountPosition) {
            if (position.avaliableAmount() >= amount) {
                position.preOrder(amount);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean releas(Currency currency, double amount) {
        List<AmountPosition> amountPosition = getAmountPosition(currency);
        for (AmountPosition position : amountPosition) {
            if (position.userAmount() >= amount) {
                position.release(amount);
                return true;
            }
        }
        return false;
    }

    private List<AmountPosition> getAmountPosition(Currency currency) {
        List<AmountPosition> amountPosition = taskPosition.get(currency);
        if (amountPosition == null) {
            List<AmountPosition> build = accountStragegy.build();
            taskPosition.put(currency, build);
            return taskPosition.get(currency);
        }
        return amountPosition;
    }


    protected void updateAmountPosition(boolean init) {
        if(init){
            //todo 从数据库里加载当前的仓位 后续就只对taskPosition里的仓位进行更新即可
        }
        for (Currency currency : taskPosition.keySet()) {
            List<AmountPosition> amountPositions = taskPosition.get(currency);
            double currencyAmount = getCurrencyAmount(currency);
            if (currencyAmount > 0) {
                double sum = amountPositions.stream().mapToDouble(AmountPosition::userAmount).sum();
                double next = currencyAmount - sum;
                if (currencyAmount > sum) {
                    for (AmountPosition amountPosition : amountPositions) {
                        boolean avaliable = amountPosition.isAvaliable();
                        if (avaliable) {
                            double min = Math.min(next, amountPosition.avaliableAmount());
                            amountPosition.preOrder(min);
                            next -= min;
                            if (next <= 0) {
                                break;
                            }
                        }
                    }
                } else if (currencyAmount < sum) {
                    next = sum - currencyAmount;
                    for (AmountPosition amountPosition : amountPositions) {
                        if (amountPosition.userAmount() > 0) {
                            double min = Math.min(next, amountPosition.userAmount());
                            amountPosition.release(min);
                            next -= min;
                            if (next <= 0) {
                                break;
                            }
                        }
                    }
                }
            }else{
                amountPositions.clear();
                taskPosition.remove(currency);
            }
        }
    }


    private double getCurrencyAmount(Currency currency) {
        OrderQueryDto orderQueryDto = new OrderQueryDto();
        orderQueryDto.setCurrency(currency);
        orderQueryDto.setVirtualId(orderIdPrefix);
        orderQueryDto.setFromTime(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 2000);
        ArrayList<OrderStatus> orderStatuses = new ArrayList<>();
        orderStatuses.add(OrderStatus.NEW);
        orderStatuses.add(OrderStatus.PENDING);
        orderStatuses.add(OrderStatus.PARTIALLY_FILLED);
        orderStatuses.add(OrderStatus.FILLED);
        orderQueryDto.setOrderStatus(orderStatuses);

        ArrayList<OrderSide> orderSides = new ArrayList<>();
        orderSides.add(orderSide);
        if (orderSide == OrderSide.BUY_LONG) {
            orderSides.add(OrderSide.SELL_LONG);
        } else {
            orderSides.add(OrderSide.BUY_SHORT);
        }
        orderQueryDto.setOrderSide(orderSides);

        List<OrderVo> orderList = orderManager.getOrderVoList(orderQueryDto);

        double buyAmount = 0L;
        double sellAmount = 0L;
        for (OrderVo orderVo : orderList) {
            if (orderVo.getOrderSide() == OrderSide.BUY_LONG) {
                buyAmount += orderVo.getTradeAmount();
            }
            if (orderVo.getOrderSide() == OrderSide.SELL_LONG && orderVo.getOrderStatus() == OrderStatus.FILLED) {
                sellAmount += orderVo.getTradeAmount();
            }
        }

        return buyAmount - sellAmount;
    }


}
