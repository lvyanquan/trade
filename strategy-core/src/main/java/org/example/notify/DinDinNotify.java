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

package org.example.notify;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.taobao.api.TaobaoResponse;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;

public class DinDinNotify {

    private String access_token = "793336df0ed161c1b9c082e7ff22ed360a2a1d3730fad8a753826ecc77a3d107";
    private String secret = "SEC565e84ba79af6dc34b977b08a7b7bca1669f394ed36b7e1f2da377b6ff22195d";

    public DinDinNotify(String access_token, String secret) {
        this.access_token = access_token;
        this.secret = secret;
    }

    public DinDinNotify() {
    }


    public void send(String msg) {
        try {
            DingTalkClient client = new DefaultDingTalkClient(getDIndinUrl());
            OapiRobotSendRequest request = new OapiRobotSendRequest();
            request.setMsgtype("text");
            OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
            text.setContent(msg);
            request.setText(text);
            OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
            at.setIsAtAll(false);
            request.setAt(at);
            TaobaoResponse response = client.execute(request);
//            System.out.println(response.getBody());
        } catch (Exception e) {
        }
    }

    public String getDIndinUrl() {
        try {
            String url = "https://oapi.dingtalk.com/robot/send?access_token=" + access_token;
            Long timestamp = System.currentTimeMillis();
            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
            String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
            return url + "&timestamp=" + timestamp + "&sign=" + sign;

        } catch (Exception e) {
            return null;
        }
    }
}
