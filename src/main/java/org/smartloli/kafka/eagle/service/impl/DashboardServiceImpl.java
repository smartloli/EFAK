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
package org.smartloli.kafka.eagle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.smartloli.kafka.eagle.domain.DashboardDomain;
import org.smartloli.kafka.eagle.factory.KafkaFactory;
import org.smartloli.kafka.eagle.factory.KafkaService;
import org.smartloli.kafka.eagle.ipc.RpcClient;
import org.smartloli.kafka.eagle.service.DashboardService;
import org.smartloli.kafka.eagle.util.ConstantUtils;
import org.smartloli.kafka.eagle.util.SystemConfigUtils;
import org.springframework.stereotype.Service;

/**
 * Kafka Eagle dashboard data generator.
 * 
 * @author smartloli.
 *
 *         Created by Aug 12, 2016
 */
@Service
public class DashboardServiceImpl implements DashboardService {

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** Get dashboard data. */
	private String panel() {
		int zks = SystemConfigUtils.getPropertyArray("kafka.zk.list", ",").length;
		String topicObject = kafkaService.getAllPartitions();
		int topics = JSON.parseArray(topicObject).size();
		String kafkaObject = kafkaService.getAllBrokersInfo();
		int brokers = JSON.parseArray(kafkaObject).size();
		DashboardDomain dash = new DashboardDomain();
		dash.setBrokers(brokers);
		dash.setTopics(topics);
		dash.setZks(zks);
		String formatter = SystemConfigUtils.getProperty("kafka.eagle.offset.storage");
		if ("kafka".equals(formatter)) {
			dash.setConsumers(getKafkaConsumerNumbers());
		} else {
			dash.setConsumers(getConsumerNumbers());
		}
		return dash.toString();
	}

	/** Get consumer number from zookeeper. */
	private int getConsumerNumbers() {
		Map<String, List<String>> map = kafkaService.getConsumers();
		int count = 0;
		for (Entry<String, List<String>> entry : map.entrySet()) {
			count += entry.getValue().size();
		}
		return count;
	}

	/** Get kafka & dashboard dataset. */
	public String getDashboard() {
		JSONObject obj = new JSONObject();
		obj.put("kafka", kafkaBrokersGraph());
		obj.put("dashboard", panel());
		return obj.toJSONString();
	}

	/** Get consumer number from kafka topic. */
	private int getKafkaConsumerNumbers() {
		Map<String, List<String>> type = new HashMap<String, List<String>>();
		Gson gson = new Gson();
		Map<String, List<String>> map = gson.fromJson(RpcClient.getConsumer(), type.getClass());
		int count = 0;
		for (Entry<String, List<String>> entry : map.entrySet()) {
			count += entry.getValue().size();
		}
		return count;
	}

	/** Get kafka data. */
	private String kafkaBrokersGraph() {
		String kafka = kafkaService.getAllBrokersInfo();
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
