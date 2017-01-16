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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

import org.smartloli.kafka.eagle.domain.OffsetDomain;
import org.smartloli.kafka.eagle.domain.OffsetZkDomain;
import org.smartloli.kafka.eagle.domain.TupleDomain;
import org.smartloli.kafka.eagle.ipc.RpcClient;
import org.smartloli.kafka.eagle.util.CalendarUtils;
import org.smartloli.kafka.eagle.util.ZKDataUtils;
import org.smartloli.kafka.eagle.util.KafkaClusterUtils;
import org.smartloli.kafka.eagle.util.LRUCacheUtils;

/**
 * Offsets consumer data.
 *
 * @author smartloli.
 *
 *         Created by Aug 16, 2016
 */
public class OffsetService {

	private static LRUCacheUtils<String, TupleDomain> lruCache = new LRUCacheUtils<String, TupleDomain>(100000);

	public static String getLogSize(String topic, String group, String ip) {
		List<String> hosts = getBrokers(topic, group, ip);
		List<String> partitions = KafkaClusterUtils.findTopicPartition(topic);
		List<OffsetDomain> list = new ArrayList<OffsetDomain>();
		for (String partition : partitions) {
			int partitionInt = Integer.parseInt(partition);
			OffsetZkDomain offsetZk = KafkaClusterUtils.getOffset(topic, group, partitionInt);
			OffsetDomain offset = new OffsetDomain();
			long logSize = KafkaClusterUtils.getLogSize(hosts, topic, partitionInt);
			offset.setPartition(partitionInt);
			offset.setLogSize(logSize);
			offset.setCreate(offsetZk.getCreate());
			offset.setModify(offsetZk.getModify());
			offset.setOffset(offsetZk.getOffset());
			offset.setLag(offsetZk.getOffset() == -1 ? 0 : logSize - offsetZk.getOffset());
			offset.setOwner(offsetZk.getOwners());
			list.add(offset);
		}
		return list.toString();
	}
	
	public static String getLogSize(String formatter,String topic, String group, String ip) {
		if("kafka".equals(formatter)){
			return getKafkaLogSize(topic, group, ip);
		}else{
			return getLogSize(topic, group, ip);
		}
	}

	public static OffsetZkDomain getKafkaOffset(String topic,String group,int partition){
		JSONArray array = JSON.parseArray(RpcClient.getOffset());
		OffsetZkDomain offsetZk = new OffsetZkDomain();
		for(Object obj : array){
			JSONObject object = (JSONObject) obj;
			String _topic=object.getString("topic");
			String _group = object.getString("group");
			int _partition = object.getInteger("partition");
			long timestamp = object.getLong("timestamp");
			long offset = object.getLong("offset");
			String owner = object.getString("owner");
			if(topic.equals(_topic)&&group.equals(_group)&&partition==_partition){
				offsetZk.setOffset(offset);
				offsetZk.setOwners(owner);
				offsetZk.setCreate(CalendarUtils.convertUnixTime2Date(timestamp));
				offsetZk.setModify(CalendarUtils.convertUnixTime2Date(timestamp));
			}
		}
		return offsetZk;
	}
	
	private static String getKafkaLogSize(String topic, String group, String ip) {
		List<String> hosts = getBrokers(topic, group, ip);
		List<String> partitions = KafkaClusterUtils.findTopicPartition(topic);
		List<OffsetDomain> list = new ArrayList<OffsetDomain>();
		for (String partition : partitions) {
			int partitionInt = Integer.parseInt(partition);
			OffsetZkDomain offsetZk = getKafkaOffset(topic, group, partitionInt);
			OffsetDomain offset = new OffsetDomain();
			long logSize = KafkaClusterUtils.getLogSize(hosts, topic, partitionInt);
			offset.setPartition(partitionInt);
			offset.setLogSize(logSize);
			offset.setCreate(offsetZk.getCreate());
			offset.setModify(offsetZk.getModify());
			offset.setOffset(offsetZk.getOffset());
			offset.setLag(offsetZk.getOffset() == -1 ? 0 : logSize - offsetZk.getOffset());
			offset.setOwner(offsetZk.getOwners());
			list.add(offset);
		}
		return list.toString();
	}

	private static List<String> getBrokers(String topic, String group, String ip) {
		// Add LRUCache per 3 min
		String key = group + "_" + topic + "_consumer_brokers_" + ip;
		String brokers = "";
		if (lruCache.containsKey(key)) {
			TupleDomain tuple = lruCache.get(key);
			brokers = tuple.getRet();
			long end = System.currentTimeMillis();
			if ((end - tuple.getTimespan()) / (1000 * 60.0) > 3) {// 1 mins
				lruCache.remove(key);
			}
		} else {
			brokers = KafkaClusterUtils.getAllBrokersInfo();
			TupleDomain tuple = new TupleDomain();
			tuple.setRet(brokers);
			tuple.setTimespan(System.currentTimeMillis());
			lruCache.put(key, tuple);
		}
		JSONArray arr = JSON.parseArray(brokers);
		List<String> list = new ArrayList<String>();
		for (Object object : arr) {
			JSONObject obj = (JSONObject) object;
			String host = obj.getString("host");
			int port = obj.getInteger("port");
			list.add(host + ":" + port);
		}
		return list;
	}

	public static boolean isGroupTopic(String group, String topic, String ip) {
		String key = group + "_" + topic + "_consumer_owners_" + ip;
		boolean status = false;
		if (lruCache.containsKey(key)) {
			TupleDomain tuple = lruCache.get(key);
			status = tuple.isStatus();
			long end = System.currentTimeMillis();
			if ((end - tuple.getTimespan()) / (1000 * 60.0) > 3) {// 1 mins
				lruCache.remove(key);
			}
		} else {
			status = KafkaClusterUtils.findTopicIsConsumer(topic, group);
			TupleDomain tuple = new TupleDomain();
			tuple.setStatus(status);
			tuple.setTimespan(System.currentTimeMillis());
			lruCache.put(key, tuple);
		}
		return status;
	}

	public static boolean isGroupTopic(String formatter, String group, String topic, String ip) {
		if ("kafka".equals(formatter)) {
			return isKafkaGroupTopic(group, topic, ip);
		} else {
			return isGroupTopic(group, topic, ip);
		}
	}

	private static boolean isKafkaGroupTopic(String group, String topic, String ip) {
		String key = group + "_" + topic + "_kafka_consumer_owners_" + ip;
		boolean status = false;
		if (lruCache.containsKey(key)) {
			TupleDomain tuple = lruCache.get(key);
			status = tuple.isStatus();
			long end = System.currentTimeMillis();
			if ((end - tuple.getTimespan()) / (1000 * 60.0) > 3) {// 1 mins
				lruCache.remove(key);
			}
		} else {
			Map<String, List<String>> type = new HashMap<String, List<String>>();
			Gson gson = new Gson();
			Map<String, List<String>> map = gson.fromJson(RpcClient.getConsumer(), type.getClass());
			if (map.containsKey(group)) {
				for (String _topic : map.get(group)) {
					if (_topic.equals(topic)) {
						status = true;
						break;
					}
				}
			}
			TupleDomain tuple = new TupleDomain();
			tuple.setStatus(status);
			tuple.setTimespan(System.currentTimeMillis());
			lruCache.put(key, tuple);
		}
		return status;
	}

	public static String getOffsetsGraph(String group, String topic) {
		String ret = ZKDataUtils.getOffsets(group, topic);
		if (ret.length() > 0) {
			ret = JSON.parseObject(ret).getString("data");
		}
		return ret;
	}
	
}
