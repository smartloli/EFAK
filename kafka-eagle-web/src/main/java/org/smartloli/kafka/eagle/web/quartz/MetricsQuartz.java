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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicOffsetsInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerFactory;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerService;
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

	/** Broker service interface. */
	private static BrokerService brokerService = new BrokerFactory().create();

	public void metricsConsumerTopicQuartz() {
		try {
			consumerTopicQuartz();
		} catch (Exception e) {
			LOG.error("Collector consumer topic data has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}
	}

	private void consumerTopicQuartz() {
		List<TopicOffsetsInfo> topicOffsets = new ArrayList<>();
		String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
		for (String clusterAlias : clusterAliass) {
			if ("kafka".equals(SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage"))) {
				JSONArray consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumer(clusterAlias));
				for (Object object : consumerGroups) {
					JSONObject consumerGroup = (JSONObject) object;
					String group = consumerGroup.getString("group");
					for (String topic : kafkaService.getKafkaConsumerTopics(clusterAlias, group)) {
						TopicOffsetsInfo topicOffset = new TopicOffsetsInfo();
						topicOffset.setCluster(clusterAlias);
						topicOffset.setGroup(group);
						topicOffset.setTopic(topic);

						List<String> partitions = kafkaService.findTopicPartition(clusterAlias, topic);
						Set<Integer> partitionsInts = new HashSet<>();
						for (String partition : partitions) {
							try {
								partitionsInts.add(Integer.parseInt(partition));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						Map<Integer, Long> partitionOffset = kafkaService.getKafkaOffset(topicOffset.getCluster(), topicOffset.getGroup(), topicOffset.getTopic(), partitionsInts);
						Map<TopicPartition, Long> tps = kafkaService.getKafkaLogSize(topicOffset.getCluster(), topicOffset.getTopic(), partitionsInts);
						long logsize = 0L;
						long offsets = 0L;
						if (tps != null && partitionOffset != null) {
							for (Entry<TopicPartition, Long> entrySet : tps.entrySet()) {
								try {
									logsize += entrySet.getValue();
									offsets += partitionOffset.get(entrySet.getKey().partition());
								} catch (Exception e) {
									LOG.error("Get logsize and offsets has error, msg is " + e.getCause().getMessage());
									e.printStackTrace();
								}
							}
						}

						topicOffset.setLogsize(String.valueOf(logsize));
						topicOffset.setLag(String.valueOf(logsize - offsets));
						topicOffset.setOffsets(String.valueOf(offsets));
						topicOffset.setTimespan(CalendarUtils.getTimeSpan());
						topicOffset.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
						topicOffsets.add(topicOffset);
					}
				}
			} else {
				Map<String, List<String>> consumerGroups = kafkaService.getConsumers(clusterAlias);
				for (Entry<String, List<String>> entry : consumerGroups.entrySet()) {
					String group = entry.getKey();
					for (String topic : kafkaService.getActiveTopic(clusterAlias, group)) {
						TopicOffsetsInfo topicOffset = new TopicOffsetsInfo();
						topicOffset.setCluster(clusterAlias);
						topicOffset.setGroup(group);
						topicOffset.setTopic(topic);
						long logsize = brokerService.getTopicLogSizeTotal(clusterAlias, topic);
						topicOffset.setLogsize(String.valueOf(logsize));
						long lag = kafkaService.getLag(clusterAlias, group, topic);
						topicOffset.setLag(String.valueOf(lag));
						topicOffset.setOffsets(String.valueOf(logsize - lag));
						topicOffset.setTimespan(CalendarUtils.getTimeSpan());
						topicOffset.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
						topicOffsets.add(topicOffset);
					}
				}
			}
		}
		try {
			MetricsServiceImpl metricsServiceImpl = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
			if (topicOffsets.size() > 0) {
				metricsServiceImpl.setConsumerTopic(topicOffsets);
			}
		} catch (Exception e) {
			LOG.error("Collector consumer lag data has error,msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}
	}

}
