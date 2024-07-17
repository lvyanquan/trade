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

package org.example.model.position;

public class AmountPosition {
    private double amount;

    private double usedAmount;

    public AmountPosition(double amount) {
        this.amount = amount;
    }

    public boolean isAvaliable() {
        return amount - usedAmount > 0;
    }

    public double avaliableAmount() {
        return amount - usedAmount;
    }


    public double userAmount() {
        return  usedAmount;
    }



    public synchronized boolean preOrder(double amount) {
        if (amount - usedAmount - amount >= 0) {
            usedAmount += amount;
            check();
            return true;
        }
        return false;
    }

    public synchronized boolean release(double amount) {
        usedAmount -= amount;
        check();
        return true;
    }

    public void check(){
        if(this.amount < usedAmount || usedAmount < 0){
            throw new RuntimeException("current position is not ok");
        }
    }
}
