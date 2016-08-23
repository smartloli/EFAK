package com.smartloli.kafka.eagle.service;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartloli.kafka.eagle.domain.OffsetDomain;
import com.smartloli.kafka.eagle.domain.OffsetZkDomain;
import com.smartloli.kafka.eagle.domain.TupleDomain;
import com.smartloli.kafka.eagle.utils.CalendarUtils;
import com.smartloli.kafka.eagle.utils.KafkaClusterUtils;
import com.smartloli.kafka.eagle.utils.LRUCacheUtils;

/**
 * @Date Aug 16, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
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

	public static String getOffsetsGraph(String group, String topic, String ip) {
		String key = group + "_" + topic + "_consumer_offsets_graph_" + ip;
		String ret = "";
		if (lruCache.containsKey(key)) {
			TupleDomain tuple = lruCache.get(key);
			ret = tuple.getRet();
			long end = System.currentTimeMillis();
			if ((end - tuple.getTimespan()) / (1000 * 60.0) > 3) {// 1 mins
				lruCache.remove(key);
			}
		} else {
			String sql = "select * from offsets where groups='" + group + "' and topic='" + topic + "' and created between '" + CalendarUtils.getCurrentStartDate() + "' and '" + CalendarUtils.getCurrentEndDate() + "'";
			ret = SQLiteService.query(sql).toString();
			TupleDomain tuple = new TupleDomain();
			tuple.setRet(ret);
			tuple.setTimespan(System.currentTimeMillis());
			lruCache.put(key, tuple);
		}

		return ret;
	}

	public static void main(String[] args) {
		System.out.println(getLogSize("words", "group1", "127.0.0.1"));
	}

}
