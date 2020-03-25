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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.admin.ConfigEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicLogSize;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicRank;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.Topic;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerFactory;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerService;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsFactory;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.DashboardServiceImpl;

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
			for (String bType : Topic.BROKER_PERFORMANCE_LIST) {
				brokerPerformanceByTopicStats(bType);
			}
		} catch (Exception e) {
			LOG.error("Collector broker spread by topic has error, msg is ", e);
			e.printStackTrace();
		}

		try {
			topicCleanTask();
		} catch (Exception e) {
			LOG.error("Clean topic logsize has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}
	}

	private void topicCleanTask() {
		DashboardServiceImpl dashboardServiceImpl = null;
		try {
			dashboardServiceImpl = StartupListener.getBean("dashboardServiceImpl", DashboardServiceImpl.class);
		} catch (Exception e) {
			LOG.error("Create clean topic object has error,msg is ", e);
		}
		String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
		for (String clusterAlias : clusterAliass) {
			Map<String, Object> params = new HashMap<>();
			params.put("cluster", clusterAlias);
			List<TopicRank> allCleanTopics = dashboardServiceImpl.getCleanTopicList(params);
			if (allCleanTopics != null) {
				for (TopicRank tr : allCleanTopics) {
					long logSize = brokerService.getTopicRealLogSize(clusterAlias, tr.getTopic());
					if (logSize > 0) {
						String cleanUpPolicyLog = kafkaMetricsService.changeTopicConfig(clusterAlias, tr.getTopic(), Topic.ADD, new ConfigEntry(Topic.CLEANUP_POLICY_KEY, Topic.CLEANUP_POLICY_VALUE));
						String retentionMsLog = kafkaMetricsService.changeTopicConfig(clusterAlias, tr.getTopic(), Topic.ADD, new ConfigEntry(Topic.RETENTION_MS_KEY, Topic.RETENTION_MS_VALUE));
						LOG.info("Add [" + Topic.CLEANUP_POLICY_KEY + "] topic[" + tr.getTopic() + "] property result," + cleanUpPolicyLog);
						LOG.info("Add [" + Topic.RETENTION_MS_KEY + "] topic[" + tr.getTopic() + "] property result," + retentionMsLog);
					} else {
						// delete znode
						String cleanUpPolicyLog = kafkaMetricsService.changeTopicConfig(clusterAlias, tr.getTopic(), Topic.DELETE, new ConfigEntry(Topic.CLEANUP_POLICY_KEY, ""));
						String retentionMsLog = kafkaMetricsService.changeTopicConfig(clusterAlias, tr.getTopic(), Topic.DELETE, new ConfigEntry(Topic.RETENTION_MS_KEY, ""));
						LOG.info("Delete [" + Topic.CLEANUP_POLICY_KEY + "] topic[" + tr.getTopic() + "] property result," + cleanUpPolicyLog);
						LOG.info("Delete [" + Topic.RETENTION_MS_KEY + "] topic[" + tr.getTopic() + "] property result," + retentionMsLog);
						// update db state
						tr.setTvalue(1);
						dashboardServiceImpl.writeTopicRank(Arrays.asList(tr));
					}
				}
			}
		}
	}

	private void brokerPerformanceByTopicStats(String bType) {
		DashboardServiceImpl dashboardServiceImpl = null;
		try {
			dashboardServiceImpl = StartupListener.getBean("dashboardServiceImpl", DashboardServiceImpl.class);
		} catch (Exception e) {
			LOG.error("Create topic spread, skewed, leader skewed object has error,msg is ", e);
		}

		List<TopicRank> topicRanks = new ArrayList<>();
		String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
		for (String clusterAlias : clusterAliass) {
			List<String> topics = brokerService.topicList(clusterAlias);
			for (String topic : topics) {
				int tValue = 0;
				if (bType.equals(Topic.BROKER_SPREAD)) {
					tValue = brokerService.getBrokerSpreadByTopic(clusterAlias, topic);
				} else if (bType.equals(Topic.BROKER_SKEWED)) {
					tValue = brokerService.getBrokerSkewedByTopic(clusterAlias, topic);
				} else if (bType.equals(Topic.BROKER_LEADER_SKEWED)) {
					tValue = brokerService.getBrokerLeaderSkewedByTopic(clusterAlias, topic);
				}
				TopicRank topicRank = new TopicRank();
				topicRank.setCluster(clusterAlias);
				topicRank.setTopic(topic);
				topicRank.setTkey(bType);
				topicRank.setTvalue(tValue);
				topicRanks.add(topicRank);
				if (topicRanks.size() > Topic.BATCH_SIZE) {
					try {
						dashboardServiceImpl.writeTopicRank(topicRanks);
						topicRanks.clear();
					} catch (Exception e) {
						e.printStackTrace();
						LOG.error("Write topic [" + bType + "] has error, msg is " + e.getMessage());
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
			LOG.error("Write topic [" + bType + "] end data has error,msg is " + e.getMessage());
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

}
