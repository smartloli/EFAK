/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.sysout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * // NOTE
 *
 * @author smartloli.
 * <p>
 * Created by Sep 12, 2020
 */
public class TestFastJson {

    public static void main(String[] args) {

        List<JSONArray> msg = new ArrayList<>();
        JSONArray array = new JSONArray();
        JSONObject o1 = new JSONObject();
        o1.put("id", 1);
        array.add(o1);

        JSONObject o2 = new JSONObject();
        o2.put("id", 2);
        array.add(o2);

        msg.add(array);
        System.out.println(msg.toString());

        String msgJson = "[[{\"partition\":1,\"offset\":0,\"msg\":\"{\\\"id\\\":0,\\\"time\\\":1599754181586}\",\"timespan\":1599754181894,\"date\":\"2020-09-11 00:09:41\"},{\"partition\":1,\"offset\":1,\"msg\":\"{\\\"id\\\":3,\\\"time\\\":1599754183407}\",\"timespan\":1599754183407,\"date\":\"2020-09-11 00:09:43\"}]]";
        List<JSONArray> newMsg = JSON.parseArray(msgJson, JSONArray.class);
        System.out.println("new: " + newMsg.toString());


    }
}
