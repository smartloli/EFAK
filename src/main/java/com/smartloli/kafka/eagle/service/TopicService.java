package com.smartloli.kafka.eagle.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartloli.kafka.eagle.domain.TupleDomain;
import com.smartloli.kafka.eagle.utils.KafkaClusterUtils;
import com.smartloli.kafka.eagle.utils.KafkaMetaUtils;
import com.smartloli.kafka.eagle.utils.LRUCacheUtils;

/**
 * @Date Aug 14, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
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

	public static void main(String[] args) {
		System.out.println(KafkaMetaUtils.findLeader("ip_login").toString());
	}

}
