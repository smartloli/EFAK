package com.smartloli.kafka.eagle.utils;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import kafka.admin.TopicCommand;

/**
 * @Date Aug 15, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
 */
public class KafkaCommandUtils {

	public static void list() {
		String[] options = new String[] { "--list", "--zookeeper", "master:2181" };
		TopicCommand.main(options);
	}

	public static void describe() {
		String[] options = new String[] { "--describe", "--zookeeper", "dn1:2181,dn2:2181,dn3:2181", "--topic=boyaa_mf_test12345" };
		TopicCommand.main(options);
	}

	public static Map<String, Object> create(String topicName, String partitions, String replic) {
		Map<String, Object> map = new HashMap<String, Object>();
		int brokers = JSON.parseArray(KafkaClusterUtils.getAllBrokersInfo()).size();
		if (Integer.parseInt(replic) > brokers) {
			map.put("status", "error");
			map.put("info", "replication factor: " + replic + " larger than available brokers: " + brokers);
			return map;
		}
		String zks = SystemConfigUtils.getProperty("kafka.zk.list");
		String[] options = new String[] { "--create", "--zookeeper", zks, "--partitions", partitions, "--topic", topicName, "--replication-factor", replic };
		TopicCommand.main(options);
		map.put("status", "success");
		map.put("info", "Create topic[" + topicName + "] has successed,partitions numbers is [" + partitions + "],replication-factor numbers is [" + replic + "]");
		return map;
	}

	public static void main(String[] args) {
		describe();
	}

}
