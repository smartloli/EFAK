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
package org.smartloli.kafka.eagle.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.domain.AlarmDomain;
import org.smartloli.kafka.eagle.factory.KafkaFactory;
import org.smartloli.kafka.eagle.factory.KafkaService;
import org.smartloli.kafka.eagle.factory.ZkFactory;
import org.smartloli.kafka.eagle.factory.ZkService;
import org.smartloli.kafka.eagle.service.AlarmService;
import org.springframework.stereotype.Service;

/**
 * Alarm implements service to get configure info.
 *
 * @Author smartloli.
 *
 *         Created by Sep 6, 2016
 */
@Service
public class AlarmServiceImpl implements AlarmService {

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();
	
	/** Zookeeper service interface. */
	private ZkService zkService = new ZkFactory().create();
	
	/**
	 * Add alarmer information.
	 * 
	 * @param alarm
	 *            AlarmDomain object
	 * @return Map.
	 * 
	 * @see org.smartloli.kafka.eagle.domain.AlarmDomain
	 */
	public Map<String, Object> add(AlarmDomain alarm) {
		Map<String, Object> map = new HashMap<String, Object>();
		int status = zkService.insertAlarmConfigure(alarm);
		if (status == -1) {
			map.put("status", "error");
			map.put("info", "insert [" + alarm + "] has error!");
		} else {
			map.put("status", "success");
			map.put("info", "insert success!");
		}
		return map;
	}

	/**
	 * Group and topic as a condition to delete alarmer.
	 * 
	 * @param group
	 * @param topic
	 */
	public void delete(String group, String topic) {
		zkService.remove(group, topic, "alarm");
	}

	/** Get alarmer topics. */
	public String get() {
		Map<String, List<String>> tmp = kafkaService.getConsumers();
		JSONArray retArray = new JSONArray();
		for (Entry<String, List<String>> entry : tmp.entrySet()) {
			JSONObject retObj = new JSONObject();
			retObj.put("group", entry.getKey());
			retObj.put("topics", entry.getValue());
			retArray.add(retObj);
		}
		return retArray.toJSONString();
	}

	/** List alarmer datasets. */
	public String list() {
		return zkService.getAlarm();
	}

}
