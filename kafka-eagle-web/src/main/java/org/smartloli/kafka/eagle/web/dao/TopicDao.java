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

	/** Get producer history bar data. */
	public List<BScreenBarInfo> queryProducerHistoryBar(Map<String, Object> params);

	/** Write topic sql history data into table. */
	public int writeTopicSqlHistory(List<TopicSqlHistory> topicSqlHistorys);

	/** Read topic sql history data. */
	public List<TopicSqlHistory> readTopicSqlHistory(Map<String, Object> params);

	/** Count topic sql history. */
	public long countTopicSqlHistory();

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

}
