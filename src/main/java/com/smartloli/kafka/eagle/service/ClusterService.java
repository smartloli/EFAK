package com.smartloli.kafka.eagle.service;

import com.alibaba.fastjson.JSONObject;
import com.smartloli.kafka.eagle.utils.KafkaClusterUtils;
import com.smartloli.kafka.eagle.utils.ZKCliUtils;

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

	public static JSONObject zkCliIsLive() {
		return KafkaClusterUtils.zkCliIsLive();
	}

	public static String getZKMenu(String cmd, String type) {
		String ret = "";
		if ("ls".equals(type)) {
			JSONObject object = new JSONObject();
			object.put("result", ls(cmd));
			ret = object.toJSONString();
		} else if ("delete".equals(type)) {
			JSONObject object = new JSONObject();
			object.put("result", delete(cmd));
			ret = object.toJSONString();
		} else if ("get".equals(type)) {
			JSONObject object = new JSONObject();
			object.put("result", get(cmd));
			ret = object.toJSONString();
		} else {
			ret = "Invalid command";
		}
		return ret;
	}

	private static Object delete(String cmd) {
		String ret = "";
		String[] len = cmd.replaceAll(" ", "").split("delete");
		if (len.length == 0) {
			return cmd + " has error";
		} else {
			String command = len[1];
			ret = ZKCliUtils.delete(command);
		}
		return ret;
	}

	private static Object get(String cmd) {
		String ret = "";
		String[] len = cmd.replaceAll(" ", "").split("get");
		if (len.length == 0) {
			return cmd + " has error";
		} else {
			String command = len[1];
			ret = ZKCliUtils.get(command);
		}
		return ret;
	}

	private static String ls(String cmd) {
		String ret = "";
		String[] len = cmd.replaceAll(" ", "").split("ls");
		if (len.length == 0) {
			return cmd + " has error";
		} else {
			String command = len[1];
			ret = ZKCliUtils.ls(command);
		}
		return ret;
	}

}
