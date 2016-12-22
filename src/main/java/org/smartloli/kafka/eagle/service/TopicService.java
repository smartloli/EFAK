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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.domain.TupleDomain;
import org.smartloli.kafka.eagle.util.KafkaClusterUtils;
import org.smartloli.kafka.eagle.util.KafkaMetaUtils;
import org.smartloli.kafka.eagle.util.LRUCacheUtils;

/**
 * @author smartloli.
 *
 *         Created by Aug 14, 2016
 */
public class TopicService {

	private static LRUCacheUtils<String, TupleDomain> map = new LRUCacheUtils<String, TupleDomain>(100000);

	public static String list() {
		return KafkaClusterUtils.getAllPartitions();
	}

	public static String topicMeta(String topicName, String ip) {
		String key = topicName + "_meta_" + ip;
		String ret = "";
		if (map.containsKey(key)) {
			TupleDomain tuple = map.get(key);
			ret = tuple.getRet();
			long end = System.currentTimeMillis();
			if ((end - tuple.getTimespan()) / (1000 * 60.0) > 1) {// 1 mins
				map.remove(key);
			}
		} else {
			ret = KafkaMetaUtils.findLeader(topicName).toString();
			TupleDomain tuple = new TupleDomain();
			tuple.setRet(ret);
			tuple.setTimespan(System.currentTimeMillis());
			map.put(key, tuple);
		}
		return ret;
	}

	public static boolean findTopicName(String topicName, String ip) {
		String key = topicName + "_check_" + ip;
		boolean ret = false;
		if (map.containsKey(key)) {
			TupleDomain tuple = map.get(key);
			ret = tuple.isStatus();
			long end = System.currentTimeMillis();
			if ((end - tuple.getTimespan()) / (1000 * 60.0) > 1) {// 1 mins
				map.remove(key);
			}
		} else {
			JSONArray arr = JSON.parseArray(KafkaClusterUtils.getAllPartitions());
			for (Object object : arr) {
				JSONObject obj = (JSONObject) object;
				String topic = obj.getString("topic");
				if (topicName.equals(topic)) {
					ret = true;
					break;
				}
			}
			TupleDomain tuple = new TupleDomain();
			tuple.setStatus(ret);
			tuple.setTimespan(System.currentTimeMillis());
			map.put(key, tuple);
		}
		return ret;
	}

}
