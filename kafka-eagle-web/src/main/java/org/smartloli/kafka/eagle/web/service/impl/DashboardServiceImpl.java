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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.smartloli.kafka.eagle.common.protocol.DashboardInfo;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.web.service.DashboardService;
import org.springframework.stereotype.Service;

/**
 * Kafka Eagle dashboard data generator.
 * 
 * @author smartloli.
 *
 *         Created by Aug 12, 2016.
 * 
 *         Update by hexiang 20170216
 */
@Service
public class DashboardServiceImpl implements DashboardService {

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** Get consumer number from zookeeper. */
	private int getConsumerNumbers(String clusterAlias) {
		Map<String, List<String>> consumers = kafkaService.getConsumers(clusterAlias);
		int count = 0;
		for (Entry<String, List<String>> entry : consumers.entrySet()) {
			count += entry.getValue().size();
		}
		return count;
	}

	/** Get kafka & dashboard dataset. */
	public String getDashboard(String clusterAlias) {
		JSONObject target = new JSONObject();
		target.put("kafka", kafkaBrokersGraph(clusterAlias));
		target.put("dashboard", panel(clusterAlias));
		return target.toJSONString();
	}

	/** Get kafka data. */
	private String kafkaBrokersGraph(String clusterAlias) {
		String kafka = kafkaService.getAllBrokersInfo(clusterAlias);
		JSONObject target = new JSONObject();
		target.put("name", "Kafka Brokers");
		JSONArray targets1 = JSON.parseArray(kafka);
		JSONArray targets2 = new JSONArray();
		int count = 0;
		for (Object object : targets1) {
			JSONObject subTarget = (JSONObject) object;
			if (count > KConstants.D3.SIZE) {
				JSONObject subTarget2 = new JSONObject();
				subTarget2.put("name", "...");
				targets2.add(subTarget2);
				break;
			} else {
				JSONObject subTarget2 = new JSONObject();
				subTarget2.put("name", subTarget.getString("host") + ":" + subTarget.getInteger("port"));
				targets2.add(subTarget2);
			}
			count++;
		}
		target.put("children", targets2);
		return target.toJSONString();
	}

	/** Get dashboard data. */
	private String panel(String clusterAlias) {
		int zks = SystemConfigUtils.getPropertyArray(clusterAlias + ".zk.list", ",").length;
		String topciAndPartitions = kafkaService.getAllPartitions(clusterAlias);
		int topicSize = JSON.parseArray(topciAndPartitions).size();
		String kafkaBrokers = kafkaService.getAllBrokersInfo(clusterAlias);
		int brokerSize = JSON.parseArray(kafkaBrokers).size();
		DashboardInfo dashboard = new DashboardInfo();
		dashboard.setBrokers(brokerSize);
		dashboard.setTopics(topicSize);
		dashboard.setZks(zks);
		String formatter = SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage");
		if ("kafka".equals(formatter)) {
			dashboard.setConsumers(kafkaService.getKafkaConsumerGroups(clusterAlias));
		} else {
			dashboard.setConsumers(getConsumerNumbers(clusterAlias));
		}
		return dashboard.toString();
	}

}
