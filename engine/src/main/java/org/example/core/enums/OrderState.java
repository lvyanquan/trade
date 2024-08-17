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

package org.example.core.enums;

public enum OrderState {
    NEW(0),
    PARTIALLY_FILLED(1),
    FILLED(2),
    CANCELED(3),
    REJECTED(4),
    EXPIRED(5);

    int statue;
    OrderState(int statue) {

        this.statue = statue;
    }

    public int getState() {
        return statue;
    }

    public static OrderState orderState(String name){
        return OrderState.valueOf(name);
    }

    public static OrderState orderState(int statue){
        for (OrderState value : OrderState.values()) {
            if(value.statue == statue){
                return value;
            }
        }
        throw new IllegalArgumentException("not has this statue: " + statue);
    }

    public static boolean isInvalid(int statue){
        return statue == CANCELED.statue || statue == REJECTED.statue || statue == EXPIRED.statue;
    }

    public boolean isInvalid(){
        return this == CANCELED || this == REJECTED || this == EXPIRED;
    }

    public boolean isTrade(){
        return this == FILLED || this == PARTIALLY_FILLED;
    }
}
