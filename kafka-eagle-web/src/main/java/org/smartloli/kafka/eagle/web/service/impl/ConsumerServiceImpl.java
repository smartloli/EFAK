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
package org.smartloli.kafka.eagle.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.common.protocol.ConsumerInfo;
import org.smartloli.kafka.eagle.common.protocol.DisplayInfo;
import org.smartloli.kafka.eagle.common.protocol.TopicConsumerInfo;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.web.service.ConsumerService;
import org.springframework.stereotype.Service;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Kafka consumer data interface, and set up the return data set.
 *
 * @author smartloli.
 *
 *         Created by Aug 15, 2016.
 * 
 *         Update by hexiang 20170216
 */
@Service
public class ConsumerServiceImpl implements ConsumerService {

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** Get active topic graph data from kafka cluster. */
	public String getActiveGraph(String clusterAlias) {
		JSONObject target = new JSONObject();
		target.put("active", getActiveGraphDatasets(clusterAlias));
		return target.toJSONString();
	}

	/** Get active graph from zookeeper. */
	private String getActiveGraphDatasets(String clusterAlias) {
		Map<String, List<String>> activeTopics = kafkaService.getActiveTopic(clusterAlias);
		JSONObject target = new JSONObject();
		JSONArray targets = new JSONArray();
		target.put("name", "Active Topics");
		int count = 0;
		for (Entry<String, List<String>> entry : activeTopics.entrySet()) {
			JSONObject subTarget = new JSONObject();
			JSONArray subTargets = new JSONArray();
			if (count > KConstants.D3.SIZE) {
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
	private int getActiveNumber(String clusterAlias, String group, List<String> topics) {
		Map<String, List<String>> activeTopics = kafkaService.getActiveTopic(clusterAlias);
		int sum = 0;
		for (String topic : topics) {
			if (activeTopics.containsKey(group + "_" + topic)) {
				sum++;
			}
		}
		return sum;
	}

	/** Storage offset in kafka or zookeeper. */
	public String getActiveTopic(String clusterAlias, String formatter) {
		if ("kafka".equals(formatter)) {
			return getKafkaActiveTopic(clusterAlias);
		} else {
			return getActiveGraph(clusterAlias);
		}
	}

	/** Get consumers from zookeeper. */
	private Tuple2<String, Integer> getConsumerZk(String clusterAlias, DisplayInfo page) {
		Map<String, List<String>> consumers = kafkaService.getConsumers(clusterAlias, page);
		List<ConsumerInfo> consumerPages = new ArrayList<ConsumerInfo>();
		int id = 0;
		for (Entry<String, List<String>> entry : consumers.entrySet()) {
			ConsumerInfo consumer = new ConsumerInfo();
			consumer.setGroup(entry.getKey());
			consumer.setNode("");
			consumer.setTopics(entry.getValue().size());
			consumer.setId(++id);
			consumer.setActiveNumber(getActiveNumber(clusterAlias, entry.getKey(), entry.getValue()));
			consumerPages.add(consumer);
		}
		return new Tuple2<String,Integer>(consumerPages.toString(),consumers.size());
	}

	/** Judge consumers storage offset in kafka or zookeeper. */
	public Tuple2<String,Integer> getConsumer(String clusterAlias, String formatter, DisplayInfo page) {
		if ("kafka".equals(formatter)) {
			return getKafkaConsumer(page, clusterAlias);
		} else {
			return getConsumerZk(clusterAlias, page);
		}
	}

	/** Get consumer size from kafka topic. */
	public int getConsumerCount(String clusterAlias, String formatter) {
		if ("kafka".equals(formatter)) {
			return kafkaService.getKafkaConsumerGroups(clusterAlias);
		} else {
			return kafkaService.getConsumers(clusterAlias).size();
		}
	}

	/** List the name of the topic in the consumer detail information. */
	private String getConsumerDetail(String clusterAlias, String group) {
		Map<String, List<String>> consumers = kafkaService.getConsumers(clusterAlias);
		Map<String, List<String>> actvTopics = kafkaService.getActiveTopic(clusterAlias);
		List<TopicConsumerInfo> kafkaConsumerDetails = new ArrayList<TopicConsumerInfo>();
		int id = 0;
		for (String topic : consumers.get(group)) {
			TopicConsumerInfo consumerDetail = new TopicConsumerInfo();
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
	public String getConsumerDetail(String clusterAlias, String formatter, String group) {
		if ("kafka".equals(formatter)) {
			return getKafkaConsumerDetail(clusterAlias, group);
		} else {
			return getConsumerDetail(clusterAlias, group);
		}
	}

	/** Get active grahp data & storage offset in kafka topic. */
	private Object getKafkaActive(String clusterAlias) {
		JSONArray consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumer(clusterAlias));
		JSONObject target = new JSONObject();
		JSONArray targets = new JSONArray();
		target.put("name", "Active Topics");
		int count = 0;
		for (Object object : consumerGroups) {
			JSONObject consumerGroup = (JSONObject) object;
			JSONObject subTarget = new JSONObject();
			JSONArray subTargets = new JSONArray();
			if (count > KConstants.D3.SIZE) {
				subTarget.put("name", "...");
				JSONObject subInSubTarget = new JSONObject();
				subInSubTarget.put("name", "...");
				subTargets.add(subInSubTarget);
				subTarget.put("children", subTargets);
				targets.add(subTarget);
				break;
			} else {
				subTarget.put("name", consumerGroup.getString("group"));
				for (String str : kafkaService.getKafkaActiverTopics(clusterAlias, consumerGroup.getString("group"))) {
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

	/** Get active topic from kafka cluster & storage offset in kafka topic. */
	private String getKafkaActiveTopic(String clusterAlias) {
		JSONObject target = new JSONObject();
		target.put("active", getKafkaActive(clusterAlias));
		return target.toJSONString();
	}

	/** Get kafka consumer & storage offset in kafka topic. */
	private Tuple2<String,Integer> getKafkaConsumer(DisplayInfo page, String clusterAlias) {
		List<ConsumerInfo> kafkaConsumerPages = new ArrayList<ConsumerInfo>();
		JSONArray consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumer(clusterAlias));
		int offset = 0;
		int id = 0;
		for (Object object : consumerGroups) {
			JSONObject consumerGroup = (JSONObject) object;
			String group = consumerGroup.getString("group");
			if (page.getSearch().length() > 0 && group.contains(page.getSearch())) {
				if (offset < (page.getiDisplayLength() + page.getiDisplayStart()) && offset >= page.getiDisplayStart()) {
					ConsumerInfo consumer = new ConsumerInfo();
					consumer.setGroup(group);
					consumer.setId(++id);
					consumer.setNode(consumerGroup.getString("node"));
					consumer.setActiveNumber(JSON.parseObject(kafkaService.getKafkaActiverSize(clusterAlias, group)).getInteger("activers"));
					consumer.setTopics(JSON.parseObject(kafkaService.getKafkaActiverSize(clusterAlias, group)).getInteger("topics"));
					kafkaConsumerPages.add(consumer);
				}
				offset++;
			} else if (page.getSearch().length() == 0) {
				if (offset < (page.getiDisplayLength() + page.getiDisplayStart()) && offset >= page.getiDisplayStart()) {
					ConsumerInfo consumer = new ConsumerInfo();
					consumer.setGroup(group);
					consumer.setId(++id);
					consumer.setNode(consumerGroup.getString("node"));
					consumer.setActiveNumber(JSON.parseObject(kafkaService.getKafkaActiverSize(clusterAlias, group)).getInteger("activers"));
					consumer.setTopics(JSON.parseObject(kafkaService.getKafkaActiverSize(clusterAlias, group)).getInteger("topics"));
					kafkaConsumerPages.add(consumer);
				}
				offset++;
			}
		}
		return new Tuple2<>(kafkaConsumerPages.toString(),offset);
	}

	/** Get consumer detail from kafka topic. */
	private String getKafkaConsumerDetail(String clusterAlias, String group) {
		Set<String> consumerTopics = kafkaService.getKafkaConsumerTopic(clusterAlias, group);
		Set<String> activerTopics = kafkaService.getKafkaActiverTopics(clusterAlias, group);
		List<TopicConsumerInfo> kafkaConsumerPages = new ArrayList<TopicConsumerInfo>();
		int id = 0;
		for (String topic : consumerTopics) {
			TopicConsumerInfo consumerDetail = new TopicConsumerInfo();
			consumerDetail.setId(++id);
			consumerDetail.setTopic(topic);
			if (activerTopics.contains(topic)) {
				consumerDetail.setConsumering(true);
			} else {
				consumerDetail.setConsumering(false);
			}
			kafkaConsumerPages.add(consumerDetail);
		}
		return kafkaConsumerPages.toString();
	}

}
