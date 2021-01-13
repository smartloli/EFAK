/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.admin;

import java.util.ArrayList;
import java.util.List;

import org.smartloli.kafka.eagle.core.factory.hub.KafkaHubFactory;
import org.smartloli.kafka.eagle.core.factory.hub.KafkaHubService;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * TODO
 * 
 * @author smartloli.
 *
 *         Created by May 18, 2020
 */
public class TestKafkaAdminServer {

	private static KafkaHubService khb = new KafkaHubFactory().create();

	public static void main(String[] args) {
		JSONObject topics = new JSONObject();
		JSONArray values = new JSONArray();
		JSONObject topic01 = new JSONObject();
		topic01.put("topic", "k20200326");
		values.add(topic01);
		topics.put("topics", values);
		topics.put("version", 1);
		List<Object> ids = new ArrayList<Object>();
		ids.add(0);
		System.out.println(khb.generate("cluster1", topics.toJSONString(), ids));
	}
}
