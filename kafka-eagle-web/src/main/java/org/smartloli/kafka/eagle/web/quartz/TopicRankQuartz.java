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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicRank;
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
			LOG.error("Collector topic logsize has error, msg is " + e.getMessage());
			e.printStackTrace();
		}

		try {
			topicCapacityStats();
		} catch (Exception e) {
			LOG.error("Collector topic capacity has error, msg is " + e.getMessage());
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

}
