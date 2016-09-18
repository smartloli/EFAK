package com.smartloli.kafka.eagle.service;

import com.alibaba.fastjson.JSONObject;
import com.smartloli.kafka.eagle.utils.KafkaClusterUtils;

/**
 * @Date Aug 12, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
 */
public class ClusterService {

	public static String getCluster() {
		String zk = KafkaClusterUtils.getZkInfo();
		String kafka = KafkaClusterUtils.getAllBrokersInfo();
		JSONObject obj = new JSONObject();
		obj.put("zk", zk);
		obj.put("kafka", kafka);
		return obj.toJSONString();
	}
	
}
