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

import org.smartloli.kafka.eagle.common.protocol.BrokersInfo;
import org.smartloli.kafka.eagle.common.protocol.DashboardInfo;
import org.smartloli.kafka.eagle.common.protocol.KpiInfo;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicRank;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.KConstants.Topic;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerFactory;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerService;
import org.smartloli.kafka.eagle.web.dao.MBeanDao;
import org.smartloli.kafka.eagle.web.dao.TopicDao;
import org.smartloli.kafka.eagle.web.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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

	/** Broker service interface. */
	private static BrokerService brokerService = new BrokerFactory().create();

	@Autowired
	private TopicDao topicDao;

	@Autowired
	private MBeanDao mbeanDao;

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
		List<BrokersInfo> brokers = kafkaService.getAllBrokersInfo(clusterAlias);
		JSONObject target = new JSONObject();
		target.put("name", "Kafka Brokers");
		JSONArray targets = new JSONArray();
		int count = 0;
		for (BrokersInfo broker : brokers) {
			if (count > KConstants.D3.SIZE) {
				JSONObject subTarget = new JSONObject();
				subTarget.put("name", "...");
				targets.add(subTarget);
				break;
			} else {
				JSONObject subTarget = new JSONObject();
				subTarget.put("name", broker.getHost() + ":" + broker.getPort());
				targets.add(subTarget);
			}
			count++;
		}
		target.put("children", targets);
		return target.toJSONString();
	}

	/** Get dashboard data. */
	private String panel(String clusterAlias) {
		int zks = SystemConfigUtils.getPropertyArray(clusterAlias + ".zk.list", ",").length;
		DashboardInfo dashboard = new DashboardInfo();
		dashboard.setBrokers(brokerService.brokerNumbers(clusterAlias));
		dashboard.setTopics(brokerService.topicNumbers(clusterAlias));
		dashboard.setZks(zks);
		String formatter = SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage");
		if ("kafka".equals(formatter)) {
			dashboard.setConsumers(kafkaService.getKafkaConsumerGroups(clusterAlias));
		} else {
			dashboard.setConsumers(getConsumerNumbers(clusterAlias));
		}
		return dashboard.toString();
	}

	/** Get topic rank data,such as logsize and topic capacity. */
	public JSONArray getTopicRank(Map<String, Object> params) {
		List<TopicRank> topicRank = topicDao.readTopicRank(params);
		JSONArray array = new JSONArray();
		if (Topic.LOGSIZE.equals(params.get("tkey"))) {
			int index = 1;
			for (int i = 0; i < 10; i++) {
				JSONObject object = new JSONObject();
				if (i < topicRank.size()) {
					object.put("id", index);
					object.put("topic", "<a href='/ke/topic/meta/" + topicRank.get(i).getTopic() + "/'>" + topicRank.get(i).getTopic() + "</a>");
					object.put("logsize", topicRank.get(i).getTvalue());
				} else {
					object.put("id", index);
					object.put("topic", "");
					object.put("logsize", "");
				}
				index++;
				array.add(object);
			}
		} else if (Topic.CAPACITY.equals(params.get("tkey"))) {
			int index = 1;
			for (int i = 0; i < 10; i++) {
				JSONObject object = new JSONObject();
				if (i < topicRank.size()) {
					object.put("id", index);
					object.put("topic", "<a href='/ke/topic/meta/" + topicRank.get(i).getTopic() + "/'>" + topicRank.get(i).getTopic() + "</a>");
					object.put("capacity", StrUtils.stringify(topicRank.get(i).getTvalue()));
				} else {
					object.put("id", index);
					object.put("topic", "");
					object.put("capacity", "");
				}
				index++;
				array.add(object);
			}
		}
		return array;
	}

	/** Write statistics topic rank data from kafka jmx & insert into table. */
	public int writeTopicRank(List<TopicRank> topicRanks) {
		return topicDao.writeTopicRank(topicRanks);
	}

	/** Get os memory data. */
	public String getOSMem(Map<String, Object> params) {
		List<KpiInfo> kpis = mbeanDao.getOsMem(params);
		JSONObject object = new JSONObject();
		if (kpis.size() == 2) {
			long valueFirst = Long.parseLong(kpis.get(0).getValue());
			long valueSecond = Long.parseLong(kpis.get(1).getValue());
			if (valueFirst >= valueSecond) {
				object.put("mem", 100 * StrUtils.numberic(((valueFirst - valueSecond) * 1.0 / valueFirst) + "", "###.###"));
			} else {
				object.put("mem", 100 * StrUtils.numberic(((valueSecond - valueFirst) * 1.0 / valueSecond) + "", "###.###"));
			}
		} else {
			object.put("mem", "0.0");
		}
		return object.toJSONString();
	}

}