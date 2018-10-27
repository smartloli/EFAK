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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.smartloli.kafka.eagle.common.protocol.AlertInfo;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.web.dao.MetricsDao;
import org.smartloli.kafka.eagle.web.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Alarm implements service to get configure info.
 *
 * @Author smartloli.
 *
 *         Created by Oct 27, 2018.
 * 
 */
@Service
public class AlertServiceImpl implements AlertService {

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	@Autowired
	private MetricsDao metrics;

	@Override
	public int add(AlertInfo alert) {
		return metrics.insertAlert(alert);
	}

	@Override
	public void delete(int id) {
		// TODO Auto-generated method stub

	}

	public String get(String clusterAlias, String formatter) {
		if ("kafka".equals(formatter)) {
			return getKafka(clusterAlias);
		} else {
			return get(clusterAlias);
		}
	}

	/** Get consumer topics to alert. */
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

	@Override
	public List<AlertInfo> list(Map<String, Object> params) {
		return metrics.query(params);
	}

	@Override
	public int alertCount() {
		return metrics.alertCount();
	}

	@Override
	public int findAlertByCGT(Map<String, Object> params) {
		return metrics.findAlertByCGT(params);
	}

	@Override
	public int deleteAlertById(int id) {
		return metrics.deleteAlertById(id);
	}

}
