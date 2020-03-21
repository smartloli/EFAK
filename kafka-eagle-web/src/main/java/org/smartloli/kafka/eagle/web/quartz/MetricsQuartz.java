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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.protocol.bscreen.BScreenConsumerInfo;
import org.smartloli.kafka.eagle.common.protocol.consumer.ConsumerGroupsInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.Topic;
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
			bscreenConsumerTopicStats();
		} catch (Exception e) {
			LOG.error("Collector consumer topic data has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}

		try {
			// fixed by v1.4.6
			// consumerGroupsTopicStats();
		} catch (Exception e) {
			LOG.error("Collector consumer groups topic data has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}

	}

	private void bscreenConsumerTopicStats() {
		MetricsServiceImpl metricsServiceImpl = null;
		try {
			metricsServiceImpl = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
		} catch (Exception e) {
			LOG.error("Get metricsServiceImpl bean has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
			return;
		}

		List<BScreenConsumerInfo> bscreenConsumers = new ArrayList<>();
		String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
		for (String clusterAlias : clusterAliass) {
			if ("kafka".equals(SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage"))) {
				JSONArray consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumer(clusterAlias));
				for (Object object : consumerGroups) {
					JSONObject consumerGroup = (JSONObject) object;
					String group = consumerGroup.getString("group");
					for (String topic : kafkaService.getKafkaConsumerTopics(clusterAlias, group)) {
						BScreenConsumerInfo bscreenConsumer = new BScreenConsumerInfo();
						bscreenConsumer.setCluster(clusterAlias);
						bscreenConsumer.setGroup(group);
						bscreenConsumer.setTopic(topic);

						List<String> partitions = kafkaService.findTopicPartition(clusterAlias, topic);
						Set<Integer> partitionsInts = new HashSet<>();
						for (String partition : partitions) {
							try {
								partitionsInts.add(Integer.parseInt(partition));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						Map<Integer, Long> partitionOffset = kafkaService.getKafkaOffset(bscreenConsumer.getCluster(), bscreenConsumer.getGroup(), bscreenConsumer.getTopic(), partitionsInts);
						Map<TopicPartition, Long> tps = kafkaService.getKafkaLogSize(bscreenConsumer.getCluster(), bscreenConsumer.getTopic(), partitionsInts);
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

						Map<String, Object> params = new HashMap<String, Object>();
						params.put("cluster", clusterAlias);
						params.put("group", group);
						params.put("topic", topic);
						BScreenConsumerInfo lastBScreenConsumerTopic = metricsServiceImpl.readBScreenLastTopic(params);
						if (lastBScreenConsumerTopic == null || lastBScreenConsumerTopic.getLogsize() == 0) {
							bscreenConsumer.setDifflogsize(0);
						} else {
							bscreenConsumer.setDifflogsize(logsize - lastBScreenConsumerTopic.getLogsize());
						}
						if (lastBScreenConsumerTopic == null || lastBScreenConsumerTopic.getOffsets() == 0) {
							bscreenConsumer.setDiffoffsets(0);
						} else {
							bscreenConsumer.setDiffoffsets(offsets - lastBScreenConsumerTopic.getOffsets());
						}
						bscreenConsumer.setLogsize(logsize);
						bscreenConsumer.setOffsets(offsets);
						bscreenConsumer.setLag(logsize - offsets);
						bscreenConsumer.setTimespan(CalendarUtils.getTimeSpan());
						bscreenConsumer.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
						bscreenConsumers.add(bscreenConsumer);
						if (bscreenConsumers.size() > Topic.BATCH_SIZE) {
							try {
								metricsServiceImpl.writeBSreenConsumerTopic(bscreenConsumers);
								bscreenConsumers.clear();
							} catch (Exception e) {
								e.printStackTrace();
								LOG.error("Write bsreen kafka topic consumer has error, msg is " + e.getCause().getMessage());
							}
						}
					}
				}
			} else {
				Map<String, List<String>> consumerGroups = kafkaService.getConsumers(clusterAlias);
				for (Entry<String, List<String>> entry : consumerGroups.entrySet()) {
					String group = entry.getKey();
					for (String topic : kafkaService.getActiveTopic(clusterAlias, group)) {
						BScreenConsumerInfo bscreenConsumer = new BScreenConsumerInfo();
						bscreenConsumer.setCluster(clusterAlias);
						bscreenConsumer.setGroup(group);
						bscreenConsumer.setTopic(topic);
						long logsize = brokerService.getTopicLogSizeTotal(clusterAlias, topic);
						bscreenConsumer.setLogsize(logsize);
						long lag = kafkaService.getLag(clusterAlias, group, topic);

						Map<String, Object> params = new HashMap<String, Object>();
						params.put("cluster", clusterAlias);
						params.put("group", group);
						params.put("topic", topic);
						BScreenConsumerInfo lastBScreenConsumerTopic = metricsServiceImpl.readBScreenLastTopic(params);
						if (lastBScreenConsumerTopic == null || lastBScreenConsumerTopic.getLogsize() == 0) {
							bscreenConsumer.setDifflogsize(0);
						} else {
							bscreenConsumer.setDifflogsize(logsize - lastBScreenConsumerTopic.getLogsize());
						}
						if (lastBScreenConsumerTopic == null || lastBScreenConsumerTopic.getOffsets() == 0) {
							bscreenConsumer.setDiffoffsets(0);
						} else {
							bscreenConsumer.setDiffoffsets((logsize - lag) - lastBScreenConsumerTopic.getOffsets());
						}
						bscreenConsumer.setLag(lag);
						bscreenConsumer.setOffsets(logsize - lag);
						bscreenConsumer.setTimespan(CalendarUtils.getTimeSpan());
						bscreenConsumer.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
						bscreenConsumers.add(bscreenConsumer);
						if (bscreenConsumers.size() > Topic.BATCH_SIZE) {
							try {
								metricsServiceImpl.writeBSreenConsumerTopic(bscreenConsumers);
								bscreenConsumers.clear();
							} catch (Exception e) {
								e.printStackTrace();
								LOG.error("Write bsreen topic consumer has error, msg is " + e.getCause().getMessage());
							}
						}
					}
				}
			}
		}
		try {
			if (bscreenConsumers.size() > 0) {
				try {
					metricsServiceImpl.writeBSreenConsumerTopic(bscreenConsumers);
					bscreenConsumers.clear();
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("Write bsreen final topic consumer has error, msg is " + e.getCause().getMessage());
				}
			}
		} catch (Exception e) {
			LOG.error("Collector consumer lag data has error,msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}
	}

	private void consumerGroupsTopicStats() {
		MetricsServiceImpl metricsServiceImpl = null;
		try {
			metricsServiceImpl = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
		} catch (Exception e) {
			LOG.error("Get metricsServiceImpl bean has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
			return;
		}

		List<ConsumerGroupsInfo> consumerGroupTopics = new ArrayList<>();
		String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
		for (String clusterAlias : clusterAliass) {
			Map<String, Object> params = new HashMap<>();
			params.put("cluster", clusterAlias);
			List<ConsumerGroupsInfo> allConsumerGroups = metricsServiceImpl.getAllConsumerGroups(params);
			if ("kafka".equals(SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage"))) {
				JSONArray consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumer(clusterAlias));
				cleanUnExistKafkaConsumerGroup(clusterAlias, allConsumerGroups, consumerGroups, metricsServiceImpl);
				for (Object object : consumerGroups) {
					JSONObject consumerGroup = (JSONObject) object;
					String group = consumerGroup.getString("group");
					for (String topic : kafkaService.getKafkaConsumerTopics(clusterAlias, group)) {
						ConsumerGroupsInfo consumerGroupTopic = new ConsumerGroupsInfo();
						consumerGroupTopic.setCluster(clusterAlias);
						consumerGroupTopic.setGroup(group);
						consumerGroupTopic.setTopic(topic);

						consumerGroupTopics.add(consumerGroupTopic);
						if (consumerGroupTopics.size() > Topic.BATCH_SIZE) {
							try {
								metricsServiceImpl.writeConsumerGroupTopics(consumerGroupTopics);
								consumerGroupTopics.clear();
							} catch (Exception e) {
								e.printStackTrace();
								LOG.error("Write kafka consumer group topic has error, msg is " + e.getCause().getMessage());
							}
						}
					}
				}
			} else {
				Map<String, List<String>> consumerGroups = kafkaService.getConsumers(clusterAlias);
				cleanUnExistConsumerGroup(clusterAlias, allConsumerGroups, consumerGroups, metricsServiceImpl);
				for (Entry<String, List<String>> entry : consumerGroups.entrySet()) {
					String group = entry.getKey();
					for (String topic : kafkaService.getActiveTopic(clusterAlias, group)) {
						ConsumerGroupsInfo consumerGroupTopic = new ConsumerGroupsInfo();
						consumerGroupTopic.setCluster(clusterAlias);
						consumerGroupTopic.setGroup(group);
						consumerGroupTopic.setTopic(topic);

						consumerGroupTopics.add(consumerGroupTopic);
						if (consumerGroupTopics.size() > Topic.BATCH_SIZE) {
							try {
								metricsServiceImpl.writeConsumerGroupTopics(consumerGroupTopics);
								consumerGroupTopics.clear();
							} catch (Exception e) {
								e.printStackTrace();
								LOG.error("Write kafka consumer group topic has error, msg is " + e.getCause().getMessage());
							}
						}
					}
				}
			}
		}
		try {
			if (consumerGroupTopics.size() > 0) {
				try {
					metricsServiceImpl.writeConsumerGroupTopics(consumerGroupTopics);
					consumerGroupTopics.clear();
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("Write final consumer group topic has error, msg is " + e.getCause().getMessage());
				}
			}
		} catch (Exception e) {
			LOG.error("Collector consumer lag data has error,msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}
	}

	private void cleanUnExistKafkaConsumerGroup(String cluster, List<ConsumerGroupsInfo> allConsumerGroups, JSONArray consumerGroups, MetricsServiceImpl metricsServiceImpl) {
		if (allConsumerGroups != null && consumerGroups != null) {
			Map<String, Set<String>> allConsumerGroupMap = new HashMap<>();
			for (ConsumerGroupsInfo allConsumerGroup : allConsumerGroups) {
				if (allConsumerGroupMap.containsKey(allConsumerGroup.getGroup())) {
					allConsumerGroupMap.get(allConsumerGroup.getGroup()).add(allConsumerGroup.getTopic());
				} else {
					Set<String> topics = new HashSet<>();
					topics.add(allConsumerGroup.getTopic());
					allConsumerGroupMap.put(allConsumerGroup.getGroup(), topics);
				}
			}
			try {
				List<String> realGroups = new ArrayList<>();
				for (Object object : consumerGroups) {
					JSONObject consumerGroup = (JSONObject) object;
					realGroups.add(consumerGroup.getString("group"));
				}
				for (Entry<String, Set<String>> group : allConsumerGroupMap.entrySet()) {
					if (realGroups.contains(group.getKey())) {
						for (String topic : allConsumerGroupMap.get(group.getKey())) {
							if (!kafkaService.getKafkaConsumerTopics(cluster, group.getKey()).contains(topic)) {
								Map<String, Object> cleanParams = new HashMap<>();
								cleanParams.put("cluster", cluster);
								cleanParams.put("group", group);
								cleanParams.put("topic", topic);
								try {
									metricsServiceImpl.cleanConsumerGroupTopic(cleanParams);
								} catch (Exception e) {
									e.printStackTrace();
									LOG.error("Clean kafka consumer cluster[" + cluster + "] group[" + group + "] has error, msg is ", e);
								}
							}
						}
					} else {
						Map<String, Object> cleanParams = new HashMap<>();
						cleanParams.put("cluster", cluster);
						cleanParams.put("group", group.getKey());
						try {
							metricsServiceImpl.cleanConsumerGroupTopic(cleanParams);
						} catch (Exception e) {
							e.printStackTrace();
							LOG.error("Clean kafka consumer cluster[" + cluster + "] group[" + group + "] has error, msg is ", e);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void cleanUnExistConsumerGroup(String cluster, List<ConsumerGroupsInfo> allConsumerGroups, Map<String, List<String>> consumerGroups, MetricsServiceImpl metricsServiceImpl) {
		if (allConsumerGroups != null && consumerGroups != null) {
			Map<String, Set<String>> allConsumerGroupMap = new HashMap<>();
			for (ConsumerGroupsInfo allConsumerGroup : allConsumerGroups) {
				if (allConsumerGroupMap.containsKey(allConsumerGroup.getGroup())) {
					allConsumerGroupMap.get(allConsumerGroup.getGroup()).add(allConsumerGroup.getTopic());
				} else {
					Set<String> topics = new HashSet<>();
					topics.add(allConsumerGroup.getTopic());
					allConsumerGroupMap.put(allConsumerGroup.getGroup(), topics);
				}
			}
			try {
				List<String> realGroups = new ArrayList<>();
				for (Entry<String, List<String>> entry : consumerGroups.entrySet()) {
					realGroups.add(entry.getKey());
				}
				for (Entry<String, Set<String>> group : allConsumerGroupMap.entrySet()) {
					if (realGroups.contains(group.getKey())) {
						for (String topic : allConsumerGroupMap.get(group.getKey())) {
							if (!kafkaService.getActiveTopic(cluster, group.getKey()).contains(topic)) {
								Map<String, Object> cleanParams = new HashMap<>();
								cleanParams.put("cluster", cluster);
								cleanParams.put("group", group);
								cleanParams.put("topic", topic);
								try {
									metricsServiceImpl.cleanConsumerGroupTopic(cleanParams);
								} catch (Exception e) {
									e.printStackTrace();
									LOG.error("Clean consumer cluster[" + cluster + "] group[" + group + "] has error, msg is ", e);
								}
							}
						}
					} else {
						Map<String, Object> cleanParams = new HashMap<>();
						cleanParams.put("cluster", cluster);
						cleanParams.put("group", group.getKey());
						try {
							metricsServiceImpl.cleanConsumerGroupTopic(cleanParams);
						} catch (Exception e) {
							e.printStackTrace();
							LOG.error("Clean consumer cluster[" + cluster + "] group[" + group + "] has error, msg is ", e);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
