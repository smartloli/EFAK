package com.smartloli.kafka.eagle.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartloli.kafka.eagle.domain.ConsumerDetailDomain;
import com.smartloli.kafka.eagle.domain.ConsumerDomain;
import com.smartloli.kafka.eagle.domain.TupleDomain;
import com.smartloli.kafka.eagle.utils.KafkaClusterUtils;
import com.smartloli.kafka.eagle.utils.LRUCacheUtils;

/**
 * @Date Aug 15, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note Get active topic and assembly data
 */
public class ConsumerService {

	private static LRUCacheUtils<String, TupleDomain> lruCache = new LRUCacheUtils<String, TupleDomain>(100000);

	public static String getActiveTopic() {
		JSONObject obj = new JSONObject();
		obj.put("active", getActive());
		return obj.toJSONString();
	}

	/**
	 * List the name of the topic in the consumer and whether the topic is
	 * consuming.
	 * 
	 * @return
	 */
	public static String getConsumerDetail(String group, String ip) {
		String key = group + "_consumer_detail_" + ip;
		String ret = "";
		if (lruCache.containsKey(key)) {
			TupleDomain tuple = lruCache.get(key);
			ret = tuple.getRet();
			long end = System.currentTimeMillis();
			if ((end - tuple.getTimespan()) / (1000 * 60.0) > 1) {// 1 mins
				lruCache.remove(key);
			}
		} else {
			Map<String, List<String>> map = KafkaClusterUtils.getConsumers();
			Map<String, List<String>> actvTopics = KafkaClusterUtils.getActiveTopic();
			List<ConsumerDetailDomain> list = new ArrayList<ConsumerDetailDomain>();
			int id = 0;
			for (String topic : map.get(group)) {
				ConsumerDetailDomain consumerDetail = new ConsumerDetailDomain();
				consumerDetail.setId(++id);
				consumerDetail.setTopic(topic);
				if (actvTopics.containsKey(group + "_" + topic)) {
					consumerDetail.setConsumering(true);
				} else {
					consumerDetail.setConsumering(false);
				}
				list.add(consumerDetail);
			}
			ret = list.toString();
			TupleDomain tuple = new TupleDomain();
			tuple.setRet(ret);
			tuple.setTimespan(System.currentTimeMillis());
			lruCache.put(key, tuple);
		}

		return ret;
	}

	public static String getConsumer() {
		Map<String, List<String>> map = KafkaClusterUtils.getConsumers();
		List<ConsumerDomain> list = new ArrayList<ConsumerDomain>();
		int id = 0;
		for (Entry<String, List<String>> entry : map.entrySet()) {
			ConsumerDomain consumer = new ConsumerDomain();
			consumer.setGroup(entry.getKey());
			consumer.setConsumerNumber(entry.getValue().size());
			consumer.setTopic(entry.getValue());
			consumer.setId(++id);
			list.add(consumer);
		}
		return list.toString();
	}

	public static int getConsumerNumbers() {
		Map<String, List<String>> map = KafkaClusterUtils.getConsumers();
		int count = 0;
		for (Entry<String, List<String>> entry : map.entrySet()) {
			count += entry.getValue().size();
		}
		return count;
	}

	private static String getActive() {
		Map<String, List<String>> kafka = KafkaClusterUtils.getActiveTopic();
		JSONObject obj = new JSONObject();
		JSONArray arrParent = new JSONArray();
		obj.put("name", "Active Topics");
		for (Entry<String, List<String>> entry : kafka.entrySet()) {
			JSONObject object = new JSONObject();
			object.put("name", entry.getKey());
			JSONArray arrChild = new JSONArray();
			for (String str : entry.getValue()) {
				JSONObject objectChild = new JSONObject();
				objectChild.put("name", str);
				arrChild.add(objectChild);
			}
			object.put("children", arrChild);
			arrParent.add(object);
		}
		obj.put("children", arrParent);
		return obj.toJSONString();
	}

}
