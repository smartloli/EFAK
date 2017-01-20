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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.domain.ConsumerDetailDomain;
import org.smartloli.kafka.eagle.domain.ConsumerDomain;
import org.smartloli.kafka.eagle.domain.ConsumerPageDomain;
import org.smartloli.kafka.eagle.factory.KafkaFactory;
import org.smartloli.kafka.eagle.factory.KafkaService;
import org.smartloli.kafka.eagle.ipc.RpcClient;
import org.smartloli.kafka.eagle.service.ConsumerService;
import org.smartloli.kafka.eagle.util.ConstantUtils;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer data interface, and set up the return data set.
 *
 * @author smartloli.
 *
 *         Created by Aug 15, 2016
 */
@Service
public class ConsumerServiceImpl implements ConsumerService {

	private final Logger LOG = LoggerFactory.getLogger(ConsumerServiceImpl.class);

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** Get active topic graph data from kafka cluster. */
	public String getActiveGraph() {
		JSONObject target = new JSONObject();
		target.put("active", getActiveGraphDatasets());
		return target.toJSONString();
	}

	/** Get active graph from zookeeper. */
	private String getActiveGraphDatasets() {
		Map<String, List<String>> activeTopics = kafkaService.getActiveTopic();
		JSONObject target = new JSONObject();
		JSONArray targets = new JSONArray();
		target.put("name", "Active Topics");
		int count = 0;
		for (Entry<String, List<String>> entry : activeTopics.entrySet()) {
			JSONObject subTarget = new JSONObject();
			JSONArray subTargets = new JSONArray();
			if (count > ConstantUtils.D3.SIZE) {
				subTarget.put("name", "...");
				JSONObject subInSubTarget = new JSONObject();
				subInSubTarget.put("name", "...");
				subTargets.add(subInSubTarget);
				subTarget.put("children", subTargets);
				targets.add(subTarget);
				break;
			} else {
				subTarget.put("name", entry.getKey());
				for (String str : entry.getValue()) {
					JSONObject subInSubTarget = new JSONObject();
					subInSubTarget.put("name", str);
					subTargets.add(subInSubTarget);
				}
			}
			count++;
			subTarget.put("children", subTargets);
			targets.add(subTarget);
		}
		target.put("children", targets);
		return target.toJSONString();
	}

	/** Get kafka active number & storage offset in zookeeper. */
	private int getActiveNumber(String group, List<String> topics) {
		Map<String, List<String>> activeTopics = kafkaService.getActiveTopic();
		int sum = 0;
		for (String topic : topics) {
			if (activeTopics.containsKey(group + "_" + topic)) {
				sum++;
			}
		}
		return sum;
	}

	/** Storage offset in kafka or zookeeper. */
	public String getActiveTopic(String formatter) {
		if ("kafka".equals(formatter)) {
			return getKafkaActiveTopic();
		} else {
			return getActiveGraph();
		}
	}

	/** Get consumers from zookeeper. */
	private String getConsumer(ConsumerPageDomain page) {
		Map<String, List<String>> consumers = kafkaService.getConsumers(page);
		List<ConsumerDomain> consumerPages = new ArrayList<ConsumerDomain>();
		int id = 0;
		for (Entry<String, List<String>> entry : consumers.entrySet()) {
			ConsumerDomain consumer = new ConsumerDomain();
			consumer.setGroup(entry.getKey());
			consumer.setConsumerNumber(entry.getValue().size());
			consumer.setTopic(entry.getValue());
			consumer.setId(++id);
			consumer.setActiveNumber(getActiveNumber(entry.getKey(), entry.getValue()));
			consumerPages.add(consumer);
		}
		return consumerPages.toString();
	}

	/** Judge consumers storage offset in kafka or zookeeper. */
	public String getConsumer(String formatter, ConsumerPageDomain page) {
		if ("kafka".equals(formatter)) {
			return getKafkaConsumer(page);
		} else {
			return getConsumer(page);
		}
	}

	/** Get consumer size from kafka topic. */
	public int getConsumerCount(String formatter) {
		if ("kafka".equals(formatter)) {
			Map<String, List<String>> consumers = new HashMap<>();
			try {
				Map<String, List<String>> type = new HashMap<String, List<String>>();
				Gson gson = new Gson();
				consumers = gson.fromJson(RpcClient.getConsumer(), type.getClass());
			} catch (Exception e) {
				LOG.error("Get Kafka topic offset has error,msg is " + e.getMessage());
			}
			return consumers.size();
		} else {
			return kafkaService.getConsumers().size();
		}
	}

	/** List the name of the topic in the consumer detail information. */
	private String getConsumerDetail(String group) {
		Map<String, List<String>> consumers = kafkaService.getConsumers();
		Map<String, List<String>> actvTopics = kafkaService.getActiveTopic();
		List<ConsumerDetailDomain> kafkaConsumerDetails = new ArrayList<ConsumerDetailDomain>();
		int id = 0;
		for (String topic : consumers.get(group)) {
			ConsumerDetailDomain consumerDetail = new ConsumerDetailDomain();
			consumerDetail.setId(++id);
			consumerDetail.setTopic(topic);
			if (actvTopics.containsKey(group + "_" + topic)) {
				consumerDetail.setConsumering(true);
			} else {
				consumerDetail.setConsumering(false);
			}
			kafkaConsumerDetails.add(consumerDetail);
		}
		return kafkaConsumerDetails.toString();
	}

	/** Judge consumer storage offset in kafka or zookeeper. */
	public String getConsumerDetail(String formatter, String group) {
		if ("kafka".equals(formatter)) {
			return getKafkaConsumerDetail(group);
		} else {
			return getConsumerDetail(group);
		}
	}

	/** Get active grahp data & storage offset in kafka topic. */
	private Object getKafkaActive() {
		Map<String, List<String>> type = new HashMap<String, List<String>>();
		Gson gson = new Gson();
		Map<String, List<String>> activerConsumers = gson.fromJson(RpcClient.getActiverConsumer(), type.getClass());
		JSONObject target = new JSONObject();
		JSONArray targets = new JSONArray();
		target.put("name", "Active Topics");
		int count = 0;
		for (Entry<String, List<String>> entry : activerConsumers.entrySet()) {
			JSONObject subTarget = new JSONObject();
			JSONArray subTargets = new JSONArray();
			if (count > ConstantUtils.D3.SIZE) {
				subTarget.put("name", "...");
				JSONObject subInSubTarget = new JSONObject();
				subInSubTarget.put("name", "...");
				subTargets.add(subInSubTarget);
				subTarget.put("children", subTargets);
				targets.add(subTarget);
				break;
			} else {
				subTarget.put("name", entry.getKey());
				for (String str : entry.getValue()) {
					JSONObject subInSubTarget = new JSONObject();
					subInSubTarget.put("name", str);
					subTargets.add(subInSubTarget);
				}
			}
			count++;
			subTarget.put("children", subTargets);
			targets.add(subTarget);
		}
		target.put("children", targets);
		return target.toJSONString();
	}

	/** Get kafka active number & storage offset in kafka topic. */
	private int getKafkaActiveNumber(String group, List<String> topics) {
		Map<String, List<String>> type = new HashMap<String, List<String>>();
		Gson gson = new Gson();
		Map<String, List<String>> activerConsumers = gson.fromJson(RpcClient.getActiverConsumer(), type.getClass());
		int sum = 0;
		for (String topic : topics) {
			String key = group + "_" + topic;
			if (activerConsumers.containsKey(key)) {
				sum++;
			}
		}
		return sum;
	}

	/** Get active topic from kafka cluster & storage offset in kafka topic. */
	private String getKafkaActiveTopic() {
		JSONObject target = new JSONObject();
		target.put("active", getKafkaActive());
		return target.toJSONString();
	}

	/** Get kafka consumer & storage offset in kafka topic. */
	private String getKafkaConsumer(ConsumerPageDomain page) {
		List<ConsumerDomain> kafkaConsumerPages = new ArrayList<ConsumerDomain>();
		Map<String, List<String>> type = new HashMap<String, List<String>>();
		Gson gson = new Gson();
		Map<String, List<String>> consumers = gson.fromJson(RpcClient.getConsumerPage(page), type.getClass());
		int id = 0;
		for (Entry<String, List<String>> entry : consumers.entrySet()) {
			ConsumerDomain consumer = new ConsumerDomain();
			consumer.setGroup(entry.getKey());
			consumer.setConsumerNumber(entry.getValue().size());
			consumer.setTopic(entry.getValue());
			consumer.setId(++id);
			consumer.setActiveNumber(getKafkaActiveNumber(entry.getKey(), entry.getValue()));
			kafkaConsumerPages.add(consumer);
		}
		return kafkaConsumerPages.toString();
	}

	/** Get consumer detail from kafka topic. */
	private String getKafkaConsumerDetail(String group) {
		Map<String, List<String>> type = new HashMap<String, List<String>>();
		Gson gson = new Gson();
		Map<String, List<String>> consumers = gson.fromJson(RpcClient.getConsumer(), type.getClass());
		Map<String, List<String>> actvTopics = gson.fromJson(RpcClient.getActiverConsumer(), type.getClass());
		List<ConsumerDetailDomain> kafkaConsumerPages = new ArrayList<ConsumerDetailDomain>();
		int id = 0;
		for (String topic : consumers.get(group)) {
			ConsumerDetailDomain consumerDetail = new ConsumerDetailDomain();
			consumerDetail.setId(++id);
			consumerDetail.setTopic(topic);
			if (actvTopics.containsKey(group + "_" + topic)) {
				consumerDetail.setConsumering(true);
			} else {
				consumerDetail.setConsumering(false);
			}
			kafkaConsumerPages.add(consumerDetail);
		}
		return kafkaConsumerPages.toString();
	}

}
