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

import org.smartloli.kafka.eagle.common.protocol.topic.TopicLogSize;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicRank;

import com.alibaba.fastjson.JSONArray;

/**
 * Kafka Eagle dashboard data generator interface.
 * 
 * @author smartloli.
 *
 *         Created by Jan 17, 2017.
 * 
 *         Update by hexiang 20170216
 */
public interface DashboardService {

	/** Get kafka & dashboard dataset interface. */
	public String getDashboard(String clusterAlias);

	/** Get topic logsize & capacity. */
	public JSONArray getTopicRank(Map<String, Object> params);

	/** Get all clean topic list. */
	public List<TopicRank> getCleanTopicList(Map<String, Object> params);

	/** Write statistics topic rank data from kafka jmx & insert into table. */
	public int writeTopicRank(List<TopicRank> topicRanks);

	/**
	 * Write statistics topic logsize data from kafka jmx & insert into table.
	 */
	public int writeTopicLogSize(List<TopicLogSize> topicLogSize);

	/** Read topic lastest logsize diffval data. */
	public TopicLogSize readLastTopicLogSize(Map<String, Object> params);

	/** Get os memory data. */
	public String getOSMem(Map<String, Object> params);
}
