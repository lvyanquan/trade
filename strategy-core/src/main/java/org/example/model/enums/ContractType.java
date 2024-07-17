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

package org.example.model.enums;

public enum ContractType {
    //现货
    SPOT(0),

    //U本位合约
    UMFUTURE(1),
    ;

    private int type;

    ContractType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static ContractType getContractType(int type){
        for (ContractType value : ContractType.values()) {
            if(value.type == type){
                return value;
            }
        }
        throw new IllegalArgumentException("not support ContractType" + type);
    }
}
