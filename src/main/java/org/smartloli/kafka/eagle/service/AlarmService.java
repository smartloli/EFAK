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
package org.smartloli.kafka.eagle.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.domain.AlarmDomain;
import org.smartloli.kafka.eagle.domain.TupleDomain;
import org.smartloli.kafka.eagle.util.ZKDataUtils;
import org.smartloli.kafka.eagle.util.KafkaClusterUtils;
import org.smartloli.kafka.eagle.util.LRUCacheUtils;

/**
 * Alarm service to get configure info.
 *
 * @Author smartloli.
 *
 *         Created by Sep 6, 2016
 */
public class AlarmService {

	/** Cache to the specified map collection to prevent frequent refresh. */
	private static LRUCacheUtils<String, TupleDomain> map = new LRUCacheUtils<String, TupleDomain>(100000);

	/** Get alarmer topics. */
	public static String getTopics(String ip) {
		String key = "alarm_topic_" + ip;
		String ret = "";
		if (map.containsKey(key)) {
			TupleDomain tuple = map.get(key);
			ret = tuple.getRet();
			long end = System.currentTimeMillis();
			if ((end - tuple.getTimespan()) / (1000 * 60.0) > 1) {// 1 mins
				map.remove(key);
			}
		} else {
			Map<String, List<String>> tmp = KafkaClusterUtils.getConsumers();
			JSONArray retArray = new JSONArray();
			for (Entry<String, List<String>> entry : tmp.entrySet()) {
				JSONObject retObj = new JSONObject();
				retObj.put("group", entry.getKey());
				retObj.put("topics", entry.getValue());
				retArray.add(retObj);
			}
			ret = retArray.toJSONString();
			TupleDomain tuple = new TupleDomain();
			tuple.setRet(ret);
			tuple.setTimespan(System.currentTimeMillis());
			map.put(key, tuple);
		}
		return ret;
	}

	/**
	 * Add alarmer information.
	 * 
	 * @param alarm
	 *            AlarmDomain object
	 * @return Map.
	 * 
	 * @see org.smartloli.kafka.eagle.domain.AlarmDomain
	 */
	public static Map<String, Object> addAlarm(AlarmDomain alarm) {
		Map<String, Object> map = new HashMap<String, Object>();
		int status = ZKDataUtils.insertAlarmConfigure(alarm);
		if (status == -1) {
			map.put("status", "error");
			map.put("info", "insert [" + alarm + "] has error!");
		} else {
			map.put("status", "success");
			map.put("info", "insert success!");
		}
		return map;
	}

	/** Get alarmer datasets. */
	public static String list() {
		return ZKDataUtils.getAlarm();
	}

	/**
	 * Group and topic as a condition to delete alarmer.
	 * 
	 * @param group
	 * @param topic
	 */
	public static void delete(String group, String topic) {
		ZKDataUtils.delete(group, topic, "alarm");
	}

}
