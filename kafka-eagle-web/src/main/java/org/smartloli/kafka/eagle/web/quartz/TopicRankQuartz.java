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
import org.smartloli.kafka.eagle.common.protocol.topic.TopicLogSize;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicRank;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.Topic;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerFactory;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerService;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsFactory;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.DashboardServiceImpl;
import org.smartloli.kafka.eagle.web.service.impl.MetricsServiceImpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Collector topic logsize, capacity etc.
 * 
 * @author smartloli.
 *
 *         Created by Jul 27, 2019
 */
public class TopicRankQuartz {

	private final Logger LOG = LoggerFactory.getLogger(TopicRankQuartz.class);

	/** Kafka service interface. */
	private KafkaMetricsService kafkaMetricsService = new KafkaMetricsFactory().create();

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** Broker service interface. */
	private static BrokerService brokerService = new BrokerFactory().create();

	public void topicRankQuartz() {
		try {
			topicLogsizeStats();
		} catch (Exception e) {
			LOG.error("Collector topic logsize has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}

		try {
			topicCapacityStats();
		} catch (Exception e) {
			LOG.error("Collector topic capacity has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}

		try {
			topicProducerLogSizeStats();
		} catch (Exception e) {
			LOG.error("Collector topic logsize has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}

		try {
			bscreenConsumerTopicStats();
		} catch (Exception e) {
			LOG.error("Collector bscreen consumer topic has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}
	}

	private void topicCapacityStats() {
		DashboardServiceImpl dashboardServiceImpl = null;
		try {
			dashboardServiceImpl = StartupListener.getBean("dashboardServiceImpl", DashboardServiceImpl.class);
		} catch (Exception e) {
			LOG.error("Create topic rank capacity object has error,msg is " + e.getMessage());
			e.printStackTrace();
		}

		List<TopicRank> topicRanks = new ArrayList<>();
		String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
		for (String clusterAlias : clusterAliass) {
			List<String> topics = brokerService.topicList(clusterAlias);
			for (String topic : topics) {
				long capacity = kafkaMetricsService.topicCapacity(clusterAlias, topic);
				TopicRank topicRank = new TopicRank();
				topicRank.setCluster(clusterAlias);
				topicRank.setTopic(topic);
				topicRank.setTkey(Topic.CAPACITY);
				topicRank.setTvalue(capacity);
				topicRanks.add(topicRank);
				if (topicRanks.size() > Topic.BATCH_SIZE) {
					try {
						dashboardServiceImpl.writeTopicRank(topicRanks);
						topicRanks.clear();
					} catch (Exception e) {
						e.printStackTrace();
						LOG.error("Write topic rank capacity has error, msg is " + e.getMessage());
					}
				}
			}
		}
		try {
			if (topicRanks.size() > 0) {
				dashboardServiceImpl.writeTopicRank(topicRanks);
				topicRanks.clear();
			}
		} catch (Exception e) {
			LOG.error("Write topic rank capacity end data has error,msg is " + e.getMessage());
			e.printStackTrace();
		}

	}

	private void topicLogsizeStats() {
		DashboardServiceImpl dashboardServiceImpl = null;
		try {
			dashboardServiceImpl = StartupListener.getBean("dashboardServiceImpl", DashboardServiceImpl.class);
		} catch (Exception e) {
			LOG.error("Create topic rank logsize object has error,msg is " + e.getMessage());
			e.printStackTrace();
		}

		List<TopicRank> topicRanks = new ArrayList<>();
		String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
		for (String clusterAlias : clusterAliass) {
			List<String> topics = brokerService.topicList(clusterAlias);
			for (String topic : topics) {
				long logsize = brokerService.getTopicRealLogSize(clusterAlias, topic);
				TopicRank topicRank = new TopicRank();
				topicRank.setCluster(clusterAlias);
				topicRank.setTopic(topic);
				topicRank.setTkey(Topic.LOGSIZE);
				topicRank.setTvalue(logsize);
				topicRanks.add(topicRank);
				if (topicRanks.size() > Topic.BATCH_SIZE) {
					try {
						dashboardServiceImpl.writeTopicRank(topicRanks);
						topicRanks.clear();
					} catch (Exception e) {
						e.printStackTrace();
						LOG.error("Write topic rank logsize has error, msg is " + e.getMessage());
					}
				}
			}
		}
		try {
			if (topicRanks.size() > 0) {
				dashboardServiceImpl.writeTopicRank(topicRanks);
				topicRanks.clear();
			}
		} catch (Exception e) {
			LOG.error("Write topic rank logsize end data has error,msg is " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void topicProducerLogSizeStats() {
		DashboardServiceImpl dashboardServiceImpl = null;
		try {
			dashboardServiceImpl = StartupListener.getBean("dashboardServiceImpl", DashboardServiceImpl.class);
		} catch (Exception e) {
			LOG.error("Create topic producer logsize object has error,msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}

		List<TopicLogSize> topicLogSizes = new ArrayList<>();
		String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
		for (String clusterAlias : clusterAliass) {
			List<String> topics = brokerService.topicList(clusterAlias);
			for (String topic : topics) {
				long logsize = brokerService.getTopicProducerLogSize(clusterAlias, topic);
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("cluster", clusterAlias);
				params.put("topic", topic);
				TopicLogSize lastTopicLogSize = dashboardServiceImpl.readLastTopicLogSize(params);
				TopicLogSize topicLogSize = new TopicLogSize();
				if (lastTopicLogSize == null || lastTopicLogSize.getLogsize() == 0) {
					topicLogSize.setDiffval(0);
				} else {
					topicLogSize.setDiffval(logsize - lastTopicLogSize.getLogsize());
				}
				topicLogSize.setCluster(clusterAlias);
				topicLogSize.setTopic(topic);
				topicLogSize.setLogsize(logsize);
				topicLogSize.setTimespan(CalendarUtils.getTimeSpan());
				topicLogSize.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
				topicLogSizes.add(topicLogSize);
				if (topicLogSizes.size() > Topic.BATCH_SIZE) {
					try {
						dashboardServiceImpl.writeTopicLogSize(topicLogSizes);
						topicLogSizes.clear();
					} catch (Exception e) {
						e.printStackTrace();
						LOG.error("Write topic producer logsize has error, msg is " + e.getCause().getMessage());
					}
				}
			}
		}
		try {
			if (topicLogSizes.size() > 0) {
				dashboardServiceImpl.writeTopicLogSize(topicLogSizes);
				topicLogSizes.clear();
			}
		} catch (Exception e) {
			LOG.error("Write topic producer logsize end data has error,msg is " + e.getCause().getMessage());
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

}
