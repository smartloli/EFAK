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

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.smartloli.kafka.eagle.common.protocol.KpiInfo;
import org.smartloli.kafka.eagle.common.protocol.MBeanOfflineInfo;
import org.smartloli.kafka.eagle.common.protocol.bscreen.BScreenConsumerInfo;
import org.smartloli.kafka.eagle.common.protocol.consumer.ConsumerGroupsInfo;
import org.smartloli.kafka.eagle.common.protocol.consumer.ConsumerSummaryInfo;

/**
 * Define access to the kafka monitoring data interface via jmx.
 * 
 * @author smartloli.
 *
 *         Created by Jul 17, 2017
 */
public interface MetricsService {

	/** Gets summary monitoring data for all broker. */
	public String getAllBrokersMBean(String clusterAlias);

	/** Collection statistics data from kafka jmx & insert into table. */
	public int insert(List<KpiInfo> kpi);

	/** Collection statistics data from kafka jmx & insert into table. */
	public int mbeanOfflineInsert(List<MBeanOfflineInfo> kpis);

	/** Query MBean data in different dimensions. */
	public String query(Map<String, Object> params) throws ParseException;

	/** Crontab clean data. */
	public void remove(int tm);

	/** Crontab clean topic logsize history data. */
	public void cleanTopicLogSize(int tm);

	/** Crontab clean topic rank history data. */
	public void cleanTopicRank(int tm);

	/** Crontab clean topic sql history data. */
	public void cleanTopicSqlHistory(int tm);

	/** Crontab clean big screen topic history data. */
	public void cleanBScreenConsumerTopic(int tm);

	/** Write statistics big screen consumer topic. */
	public int writeBSreenConsumerTopic(List<BScreenConsumerInfo> bscreenConsumers);

	/** Read big screen topic lastest diffval data. */
	public BScreenConsumerInfo readBScreenLastTopic(Map<String, Object> params);

	/** Write consumer group topics. */
	public int writeConsumerGroupTopics(List<ConsumerGroupsInfo> consumerGroups);

	/** Write consumer group summary topics. */
	public int writeConsumerSummaryTopics(List<ConsumerSummaryInfo> consumerSummarys);

	/** Clean consumer group topics. */
	public int cleanConsumerGroupTopic(Map<String, Object> params);

	/** Clean consumer group topics. */
	public int cleanConsumerSummaryTopic(Map<String, Object> params);

	/** Get all consumer groups. */
	public List<ConsumerGroupsInfo> getAllConsumerGroups(Map<String, Object> params);

	/** Get all consumer groups summary. */
	public List<ConsumerSummaryInfo> getAllConsumerSummary(Map<String, Object> params);
}
