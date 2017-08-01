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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.smartloli.kafka.eagle.common.protocol.AlarmInfo;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.ZkFactory;
import org.smartloli.kafka.eagle.core.factory.ZkService;
import org.smartloli.kafka.eagle.web.service.AlarmService;
import org.springframework.stereotype.Service;

/**
 * Alarm implements service to get configure info.
 *
 * @Author smartloli.
 *
 *         Created by Sep 6, 2016.
 * 
 *         Update by hexiang 20170216
 */
@Service
public class AlarmServiceImpl implements AlarmService {

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** Zookeeper service interface. */
	private ZkService zkService = new ZkFactory().create();

	/**
	 * Add alarmer information.
	 * 
	 * @param alarm
	 *            AlarmDomain object
	 * @return Map.
	 * 
	 * @see org.smartloli.kafka.eagle.domain.AlarmDomain
	 */
	public Map<String, Object> add(String clusterAlias, AlarmInfo alarm) {
		Map<String, Object> alarmStates = new HashMap<String, Object>();
		int status = zkService.insertAlarmConfigure(clusterAlias, alarm);
		if (status == -1) {
			alarmStates.put("status", "error");
			alarmStates.put("info", "insert [" + alarm + "] has error!");
		} else {
			alarmStates.put("status", "success");
			alarmStates.put("info", "insert success!");
		}
		return alarmStates;
	}

	/**
	 * Group and topic as a condition to delete alarmer.
	 * 
	 * @param group
	 * @param topic
	 */
	public void delete(String clusterAlias, String group, String topic) {
		zkService.remove(clusterAlias, group, topic, "alarm");
	}

	public String get(String clusterAlias, String formatter) {
		if ("kafka".equals(formatter)) {
			return getKafka(clusterAlias);
		} else {
			return get(clusterAlias);
		}
	}

	/** Get alarmer topics. */
	private String get(String clusterAlias) {
		Map<String, List<String>> consumers = kafkaService.getConsumers(clusterAlias);
		JSONArray topics = new JSONArray();
		for (Entry<String, List<String>> entry : consumers.entrySet()) {
			JSONObject groupAndTopics = new JSONObject();
			groupAndTopics.put("group", entry.getKey());
			groupAndTopics.put("topics", entry.getValue());
			topics.add(groupAndTopics);
		}
		return topics.toJSONString();
	}

	private String getKafka(String clusterAlias) {
		JSONArray topics = new JSONArray();
		JSONArray consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumer(clusterAlias));
		for (Object object : consumerGroups) {
			JSONObject consumerGroup = (JSONObject) object;
			JSONObject groupAndTopics = new JSONObject();
			groupAndTopics.put("group", consumerGroup.getString("group"));
			groupAndTopics.put("topics", kafkaService.getKafkaConsumerTopic(clusterAlias, consumerGroup.getString("group")));
			topics.add(groupAndTopics);
		}
		return topics.toJSONString();
	}

	/** List alarmer datasets. */
	public String list(String clusterAlias) {
		return zkService.getAlarm(clusterAlias);
	}

}
