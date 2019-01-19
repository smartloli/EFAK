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
import org.smartloli.kafka.eagle.common.protocol.topic.TopicLagInfo;

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

	/** Query MBean data in different dimensions. */
	public String query(Map<String, Object> params) throws ParseException;

	/** Crontab clean data. */
	public void remove(int tm);
	
	/** Set consumer topic lag metrics. */
	public int setConsumerLag(List<TopicLagInfo> topicLag);
	
	/** Get consumer topic lag metrics. */
	public List<TopicLagInfo> getConsumerLag(Map<String, Object> params);
	
	/** Clean lag data. */
	public void cleanLagData(int tm);

}
