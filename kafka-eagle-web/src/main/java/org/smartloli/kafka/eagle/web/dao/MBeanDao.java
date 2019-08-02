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

import org.smartloli.kafka.eagle.common.protocol.KpiInfo;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicOffsetsInfo;

/**
 * MBeanDao interface definition
 * 
 * @author smartloli.
 *
 *         Created by Jul 19, 2017
 */
public interface MBeanDao {

	/** Collection statistics data from kafka jmx & insert into table. */
	public int insert(List<KpiInfo> kpi);

	/** Query collector data. */
	public List<KpiInfo> query(Map<String, Object> params);

	/** Crontab clean data. */
	public void remove(int tm);

	/** @deprecated Get consumer topic metrics. */
	public List<TopicOffsetsInfo> getConsumerTopic(Map<String, Object> params);

	/** Get consumer lag topic metrics. */
	public List<TopicOffsetsInfo> getConsumerLagTopic(Map<String, Object> params);

	/** Get consumer rate topic metrics. */
	public List<TopicOffsetsInfo> getConsumerRateTopic(Map<String, Object> params);

	/** Set consumer topic metrics. */
	public int setConsumerTopic(List<TopicOffsetsInfo> consumerTopic);

	/** Clean consumer topic data. */
	public void cleanConsumerTopic(int tm);

	/** Query os memory data. */
	public List<KpiInfo> getOsMem(Map<String, Object> params);

}
