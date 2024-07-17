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

package com.example.web_kline.web;


import com.example.web_kline.datasource.entity.Kline;
import com.example.web_kline.dto.KlineFindDto;
import com.example.web_kline.dto.SubscribeKlineDto;
import com.example.web_kline.service.KlineService;
import org.example.model.currency.BaseCurrency;
import org.example.kline.KlineClient;
import com.example.web_kline.dto.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/trade/kline")
@CrossOrigin
@Component
public class KlineWeb {

    @Resource
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private KlineService klineService;

    @Resource
    private KlineClient client;

    @RequestMapping(value = "/subscribe", method = RequestMethod.POST)
    public ResponseData subscribe(@RequestBody SubscribeKlineDto subscribeKlineDto) {
        client.handlerStreamingKlineData(BaseCurrency.of(subscribeKlineDto.getSymbol(), 0, 0),subscribeKlineDto.getInterval(), t -> kafkaTemplate.send("kline_",t));
        return ResponseData.ResponseDataBuilder.OK().build();
    }

    @RequestMapping(value = "/klines", method = RequestMethod.GET)
    public ResponseData klines(@ModelAttribute KlineFindDto klineDto) {


        List<Kline> klineByTime = klineService.getKlineByTime(klineDto);

//        HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
//        objectObjectHashMap.put("time", "2019-04-11");
//        objectObjectHashMap.put("value", 80.01);
//        objectObjectHashMap.put("open", 80.01);
//        objectObjectHashMap.put("high", 82.01);
//        objectObjectHashMap.put("low", 70.01);
//        objectObjectHashMap.put("close", 81.01);
//
//        HashMap<Object, Object> objectObjectHashMap1 = new HashMap<>();
//        objectObjectHashMap1.put("time", "2019-04-12");
//        objectObjectHashMap1.put("value", 40.01);
//        objectObjectHashMap.put("open", 82.01);
//        objectObjectHashMap.put("high", 84.01);
//        objectObjectHashMap.put("low", 75.01);
//        objectObjectHashMap.put("close", 83.01);
//
//
//        objects.add(objectObjectHashMap);
//        objects.add(objectObjectHashMap1);

        return ResponseData.ResponseDataBuilder.OK().data(klineByTime).build();
    }
}
