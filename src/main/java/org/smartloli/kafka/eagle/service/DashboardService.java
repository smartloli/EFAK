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
package org.smartloli.kafka.eagle.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.domain.DashboardDomain;
import org.smartloli.kafka.eagle.util.ConstantUtils;
import org.smartloli.kafka.eagle.util.KafkaClusterUtils;
import org.smartloli.kafka.eagle.util.SystemConfigUtils;

/**
 * @author smartloli.
 *
 *         Created by Aug 12, 2016
 */
public class DashboardService {

	public static String getDashboard() {
		JSONObject obj = new JSONObject();
		obj.put("kafka", getKafka());
		obj.put("dashboard", dashboard());

		return obj.toJSONString();
	}

	private static String dashboard() {
		int zks = SystemConfigUtils.getPropertyArray("kafka.zk.list", ",").length;
		String topicObject = KafkaClusterUtils.getAllPartitions();
		int topics = JSON.parseArray(topicObject).size();
		String kafkaObject = KafkaClusterUtils.getAllBrokersInfo();
		int brokers = JSON.parseArray(kafkaObject).size();
		DashboardDomain dash = new DashboardDomain();
		dash.setBrokers(brokers);
		dash.setTopics(topics);
		dash.setZks(zks);
		String formatter = SystemConfigUtils.getProperty("kafka.eagle.offset.storage");
		if ("kafka".equals(formatter)) {
			dash.setConsumers(ConsumerService.getKafkaConsumerNumbers());
		} else {
			dash.setConsumers(ConsumerService.getConsumerNumbers());
		}
		return dash.toString();
	}

	private static String getKafka() {
		String kafka = KafkaClusterUtils.getAllBrokersInfo();
		JSONObject obj = new JSONObject();
		obj.put("name", "Kafka Brokers");
		JSONArray arr = JSON.parseArray(kafka);
		JSONArray arr2 = new JSONArray();
		int count = 0;
		for (Object tmp : arr) {
			JSONObject obj1 = (JSONObject) tmp;
			if (count > ConstantUtils.D3.SIZE) {
				JSONObject obj2 = new JSONObject();
				obj2.put("name", "...");
				arr2.add(obj2);
				break;
			} else {
				JSONObject obj2 = new JSONObject();
				obj2.put("name", obj1.getString("host") + ":" + obj1.getInteger("port"));
				arr2.add(obj2);
			}
			count++;
		}
		obj.put("children", arr2);
		return obj.toJSONString();
	}

}
