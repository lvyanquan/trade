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

package com.example.web_kline.datasource.repository;

import com.example.web_kline.datasource.entity.Kline;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface KlineRepository  {


    /**
     * 注意，startDate和endDate都是java.util.Date类型的参数。
     * 如果要使用java.time.LocalDateTime类型的参数，
     * 可以在查询方法中使用@Temporal注解进行转换  @Temporal(TemporalType.TIMESTAMP)
     *
     * @return
     */
    List<Kline> findByTime(
            long open,
            String symbol,
            String interval,
            long limit);

}