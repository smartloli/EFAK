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
package org.smartloli.kafka.eagle.web.quartz;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicLagInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.MetricsServiceImpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Collector kafka consumer topic lag trend, metrics broker topic.
 * 
 * @author smartloli.
 *
 *         Created by Jan 15, 2019
 */
public class MetricsQuartz {

	private final Logger LOG = LoggerFactory.getLogger(MetricsQuartz.class);

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	public void metricsJobQuartz() {
		List<TopicLagInfo> topicLags = new ArrayList<>();
		String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
		for (String clusterAlias : clusterAliass) {
			if ("kafka".equals(SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage"))) {
				JSONArray consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumer(clusterAlias));
				for (Object object : consumerGroups) {
					JSONObject consumerGroup = (JSONObject) object;
					String group = consumerGroup.getString("group");
					for (String topic : kafkaService.getKafkaActiverTopics(clusterAlias, group)) {
						TopicLagInfo topicLag = new TopicLagInfo();
						topicLag.setCluster(clusterAlias);
						topicLag.setGroup(group);
						topicLag.setTopic(topic);
						JSONObject value = new JSONObject();
						value.put("y", CalendarUtils.getDate());
						value.put("lag", kafkaService.getKafkaLag(clusterAlias, group, topic));
						topicLag.setLag(value.toJSONString());
						topicLag.setTimespan(CalendarUtils.getTimeSpan());
						topicLag.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
						topicLags.add(topicLag);
					}
				}
			} else {
				Map<String, List<String>> consumerGroups = kafkaService.getConsumers(clusterAlias);
				for (Entry<String, List<String>> entry : consumerGroups.entrySet()) {
					String group = entry.getKey();
					for (String topic : kafkaService.getActiveTopic(clusterAlias, group)) {
						TopicLagInfo topicLag = new TopicLagInfo();
						topicLag.setCluster(clusterAlias);
						topicLag.setGroup(group);
						topicLag.setTopic(topic);
						JSONObject value = new JSONObject();
						value.put("y", CalendarUtils.getDate());
						value.put("lag", kafkaService.getLag(clusterAlias, group, topic));
						topicLag.setLag(value.toJSONString());
						topicLag.setTimespan(CalendarUtils.getTimeSpan());
						topicLag.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
						topicLags.add(topicLag);
					}
				}
			}
		}
		try {
			MetricsServiceImpl metricsServiceImpl = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
			if (topicLags.size() > 0) {
				metricsServiceImpl.setConsumerLag(topicLags);
			}
		} catch (Exception e) {
			LOG.error("Collector consumer lag data has error,msg is " + e.getMessage());
			e.printStackTrace();
		}
	}

}
