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
package org.smartloli.kafka.eagle.web.dao;

import java.util.List;
import java.util.Map;

import org.smartloli.kafka.eagle.common.protocol.bscreen.BScreenBarInfo;
import org.smartloli.kafka.eagle.common.protocol.bscreen.BScreenConsumerInfo;
import org.smartloli.kafka.eagle.common.protocol.consumer.ConsumerGroupsInfo;
import org.smartloli.kafka.eagle.common.protocol.consumer.ConsumerSummaryInfo;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicLogSize;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicRank;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicSqlHistory;

/**
 * TopicDao interface definition
 * 
 * @author smartloli.
 *
 *         Created by Jul 27, 2019
 */
public interface TopicDao {

	/** Write statistics topic rank data from kafka jmx & insert into table. */
	public int writeTopicRank(List<TopicRank> topicRanks);

	/** Crontab clean topic rank history data. */
	public void cleanTopicRank(int tm);

	/** Read topic rank data. */
	public List<TopicRank> readTopicRank(Map<String, Object> params);

	/** Get clean topic state. */
	public List<TopicRank> getCleanTopicState(Map<String, Object> params);

	/** Get all clean topic list. */
	public List<TopicRank> getCleanTopicList(Map<String, Object> params);

	/** Read topic spread, skewed, leader skewed data. */
	public TopicRank readBrokerPerformance(Map<String, Object> params);

	/** Get topic total capacity. */
	public long getTopicCapacity(Map<String, Object> params);

	/**
	 * Write statistics topic logsize data from kafka jmx & insert into table.
	 */
	public int writeTopicLogSize(List<TopicLogSize> topicLogSize);

	/** Crontab clean topic logsize history data. */
	public void cleanTopicLogSize(int tm);

	/** Read topic lastest logsize diffval data. */
	public TopicLogSize readLastTopicLogSize(Map<String, Object> params);

	/** Get topic producer logsize chart datasets. */
	public List<TopicLogSize> queryTopicProducerChart(Map<String, Object> params);

	/** Get topic producer logsize by alarm. */
	public List<TopicLogSize> queryTopicProducerByAlarm(Map<String, Object> params);

	/** Get producer history bar data. */
	public List<BScreenBarInfo> queryProducerHistoryBar(Map<String, Object> params);

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

	/** Crontab clean topic sql history data. */
	public void cleanTopicSqlHistory(int tm);

	/** Get bscreen topic total records. */
	public long getBScreenTotalRecords(Map<String, Object> params);

	/**
	 * Write statistics big screen consumer topic.
	 */
	public int writeBSreenConsumerTopic(List<BScreenConsumerInfo> bscreenConsumers);

	/** Crontab clean big screen topic history data. */
	public void cleanBScreenConsumerTopic(int tm);

	/** Read big screen topic lastest diffval data. */
	public BScreenConsumerInfo readBScreenLastTopic(Map<String, Object> params);

	/** Get consumer history bar data. */
	public List<BScreenBarInfo> queryConsumerHistoryBar(Map<String, Object> params);

	/** Get bscreen consumer by today, such logsize offset and lag diff. */
	public List<BScreenConsumerInfo> queryTodayBScreenConsumer(Map<String, Object> params);

	/** Get lastest lag used to alarm consumer. */
	public long queryLastestLag(Map<String, Object> params);

	/** Write consumer group topic. */
	public int writeConsumerGroupTopics(List<ConsumerGroupsInfo> consumerGroups);

	/** Write consumer group summary topic. */
	public int writeConsumerSummaryTopics(List<ConsumerSummaryInfo> consumerSummarys);

	/** Clean consumer group topics. */
	public int cleanConsumerGroupTopic(Map<String, Object> params);

	/** Clean consumer group summary topics. */
	public int cleanConsumerSummaryTopic(Map<String, Object> params);

	/** Get all consumer groups. */
	public List<ConsumerGroupsInfo> getAllConsumerGroups(Map<String, Object> params);

	/** Get all consumer groups summary. */
	public List<ConsumerSummaryInfo> getAllConsumerSummary(Map<String, Object> params);

	/** Get consumer group pages. */
	public List<ConsumerGroupsInfo> getConsumerGroupPages(Map<String, Object> params);

	/** Get consumer group summary pages. */
	public List<ConsumerSummaryInfo> getConsumerSummaryPages(Map<String, Object> params);

	/** Count consumer group pages. */
	public long countConsumerGroupPages(Map<String, Object> params);

	/** Count consumer group summary pages. */
	public long countConsumerSummaryPages(Map<String, Object> params);

}
