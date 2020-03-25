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
package org.smartloli.kafka.eagle.web.service;

import java.util.List;
import java.util.Map;

import org.smartloli.kafka.eagle.common.protocol.MetadataInfo;
import org.smartloli.kafka.eagle.common.protocol.PartitionsInfo;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicConfig;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicRank;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicSqlHistory;

/**
 * Kafka topic service interface.
 * 
 * @author smartloli.
 *
 *         Created by Jan 17, 2017.
 * 
 *         Update by hexiang 20170216
 */
public interface TopicService {

	/** Find topic name in all topics. */
	public boolean hasTopic(String clusterAlias, String topicName);

	/** Get metadata in topic. */
	public List<MetadataInfo> metadata(String clusterAlias, String topicName, Map<String, Object> params);

	/** Get topic numbers. */
	public long getTopicNumbers(String clusterAlias);

	/** Get topic numbers with match name. */
	public long getTopicNumbers(String clusterAlias, String topic);

	/** Get topic partition numbers. */
	public long getPartitionNumbers(String clusterAlias, String topic);

	/** Get topic list. */
	public List<PartitionsInfo> list(String clusterAlias, Map<String, Object> params);

	/** Execute kafka query sql. */
	public String execute(String clusterAlias, String sql);

	/** Get mock topics. */
	public String mockTopics(String clusterAlias, String name);

	/** Get manager topic property keys. */
	public String getTopicProperties(String clusterAlias, String name);

	/** Alter topic config. */
	public String changeTopicConfig(String clusterAlias, TopicConfig topicConfig);

	/** Send mock message to topic. */
	public boolean mockSendMsg(String clusterAlias, String topic, String message);

	/** Get topic metrics from brokers. */
	public String getTopicMBean(String clusterAlias, String topic);

	/** Get topic logsize, topicsize. */
	public String getTopicSizeAndCapacity(String clusterAlias, String topic);

	/** Get topic producer logsize chart datasets. */
	public String queryTopicProducerChart(Map<String, Object> params);

	/** Get select topic list. */
	public String getSelectTopics(String clusterAlias, String prefixTopic);

	/** Get select filter topic logsize. */
	public String getSelectTopicsLogSize(String clusterAlias, Map<String, Object> params);

	/** Write topic sql history data into table. */
	public int writeTopicSqlHistory(List<TopicSqlHistory> topicSqlHistorys);

	/** Read topic sql history data. */
	public List<TopicSqlHistory> readTopicSqlHistory(Map<String, Object> params);

	/** Read topic sql history data by admin. */
	public List<TopicSqlHistory> readTopicSqlHistoryByAdmin(Map<String, Object> params);

	/** Count topic sql history. */
	public long countTopicSqlHistory(Map<String, Object> params);

	/** Count topic sql history by admin. */
	public long countTopicSqlHistoryByAdmin(Map<String, Object> params);

	/** Find topic sql history by id. */
	public TopicSqlHistory findTopicSqlByID(Map<String, Object> params);

	/** Add clean topic logsize data. */
	public int addCleanTopicData(List<TopicRank> topicRanks);

	/** Get clean topic state. */
	public List<TopicRank> getCleanTopicState(Map<String, Object> params);
}
