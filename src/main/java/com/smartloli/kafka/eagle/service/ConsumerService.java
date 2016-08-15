package com.smartloli.kafka.eagle.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartloli.kafka.eagle.utils.KafkaClusterUtils;

/**
 * @Date Aug 15, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
 */
public class ConsumerService {

	public static String getActiveTopic(){
		JSONObject obj = new JSONObject();
		obj.put("active", getActive());
		obj.put("consumer", getConsumer());

		return obj.toJSONString();
	}

	private static String getConsumer() {
		return "";
	}

	private static String getActive() {
		String kafka = KafkaClusterUtils.getActiveTopic();
		JSONObject obj = new JSONObject();
		obj.put("name", "Active Topics");
		JSONArray arr = JSON.parseArray(kafka);
		JSONArray arr2 = new JSONArray();
		for (Object tmp : arr) {
			JSONObject obj1 = (JSONObject) tmp;
			JSONObject obj2 = new JSONObject();
			obj2.put("name", obj1.getString("topic"));
			arr2.add(obj2);
		}
		obj.put("children", arr2);
		return obj.toJSONString();
	}
	
	public static void main(String[] args) {
		System.out.println(KafkaClusterUtils.getActiveTopic());
	}
	
}
