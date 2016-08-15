package com.smartloli.kafka.eagle.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.I0Itec.zkclient.ZkClient;

import kafka.cluster.Broker;
import kafka.common.TopicAndPartition;
import kafka.consumer.ConsumerThreadId;
import kafka.utils.ZkUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.smartloli.kafka.eagle.domain.PartitionsDomain;
import com.smartloli.kafka.eagle.domain.TopicDomain;

import scala.collection.JavaConversions;
import scala.collection.Seq;
import scala.collection.Set;

/**
 * @Date Mar 8, 2016
 *
 * @Author smartloli
 *
 * @Note Get kafka cluster info,include topic,broker list and partitions
 */
public class KafkaClusterUtils {

	private static ZKPoolUtils zkPool = ZKPoolUtils.getInstance();
	private static ZkClient zkc = null;

	public static void main(String[] args) {
//		 System.out.println(getAllBrokersInfo());
		System.out.println(getActiveTopic());
	}

	public static String getConsumers() {
		return "";
	}

	public static String getActiveTopic() {
		if (zkc == null) {
			zkc = zkPool.getZkClientSerializer();
		}
		Seq<String> seq = ZkUtils.getChildren(zkc, ZkUtils.ConsumersPath());
		String ret = new Gson().toJson(seq);
		JsonObject json = (JsonObject) new JsonParser().parse(ret);
		String tmp = json.get("underlying").toString().replace("[", "");
		String[] str = tmp.replace("]", "").split(",");
		JSONArray arr = new JSONArray();
		for (String group : str) {
			scala.collection.mutable.Map<String, scala.collection.immutable.List<ConsumerThreadId>> map = ZkUtils.getConsumersPerTopic(zkc, group.replaceAll("\"", ""), true);
			for (Entry<String, ?> entry : JavaConversions.mapAsJavaMap(map).entrySet()) {
				JSONObject obj = new JSONObject();
				obj.put("topic", entry.getKey());
				obj.put("group", group.replaceAll("\"", ""));
				arr.add(obj);
			}
		}
		for(Object object : arr){
			JSONObject obj = (JSONObject) object;
		}
		if (zkc != null) {
			zkPool.releaseZKSerializer(zkc);
			zkc = null;
		}
		return arr.toJSONString();
	}

	/**
	 * Get zks info
	 * 
	 * @return
	 */
	public static String getZkInfo() {
		String[] zks = SystemConfigUtils.getPropertyArray("kafka.zk.list", ",");
		JSONArray arr = new JSONArray();
		int id = 1;
		for (String zk : zks) {
			JSONObject obj = new JSONObject();
			obj.put("id", id++);
			obj.put("ip", zk.split(":")[0]);
			obj.put("port", zk.split(":")[1]);
			arr.add(obj);
		}
		return arr.toJSONString();
	}

	/**
	 * Get all topic name
	 * 
	 * @return
	 */
	public static String getAllTopicInfo() {
		if (zkc == null) {
			zkc = zkPool.getZkClient();
		}
		Seq<String> seq = ZkUtils.getAllTopics(zkc);
		String ret = new Gson().toJson(seq);
		JsonObject json = (JsonObject) new JsonParser().parse(ret);
		List<TopicDomain> list = new ArrayList<TopicDomain>();
		String tmp = json.get("underlying").toString().replace("[", "");
		String[] str = tmp.replace("]", "").split(",");
		for (String s : str) {
			TopicDomain topic = new TopicDomain();
			topic.setTopic(s.replace("\"", ""));
			list.add(topic);
		}
		if (zkc != null) {
			zkPool.release(zkc);
			zkc = null;
		}
		return list.toString();
	}

	/**
	 * Get all broker list
	 * 
	 * @return
	 */
	public static String getAllBrokersInfo() {
		if (zkc == null) {
			zkc = zkPool.getZkClientSerializer();
		}
		Seq<Broker> seq = ZkUtils.getAllBrokersInCluster(zkc);

		String ret = new Gson().toJson(seq);
		JsonObject json = (JsonObject) new JsonParser().parse(ret);
		if (zkc != null) {
			zkPool.releaseZKSerializer(zkc);
			zkc = null;
		}
		return json.get("array").toString();
	}

	/**
	 * get all partition
	 * 
	 * @return
	 */
	public static List<TopicDomain> getAllPartitionsInfo() {
		if (zkc == null) {
			zkc = zkPool.getZkClient();
		}
		Set<TopicAndPartition> seq = ZkUtils.getAllPartitions(zkc);
		String ret = new Gson().toJson(seq);
		JsonObject json = (JsonObject) new JsonParser().parse(ret);
		JsonArray array = json.getAsJsonArray("elems");
		List<TopicDomain> list = new ArrayList<TopicDomain>();

		for (JsonElement jsonElement : array) {
			JsonObject obj = jsonElement.getAsJsonObject();
			if (obj.has("elems")) {
				JsonArray arr = obj.getAsJsonArray("elems");
				for (JsonElement jsone : arr) {
					JsonObject obj1 = jsone.getAsJsonObject();
					if (obj1.has("key")) {
						TopicDomain topic = new TopicDomain();
						topic.setTopic(obj1.get("key").getAsJsonObject().get("topic").getAsString());
						topic.setPartition(obj1.get("key").getAsJsonObject().get("partition").getAsInt());
						list.add(topic);
					}
				}
			} else {
				TopicDomain topic = new TopicDomain();
				topic.setTopic(obj.get("key").getAsJsonObject().get("topic").getAsString());
				topic.setPartition(obj.get("key").getAsJsonObject().get("partition").getAsInt());
				list.add(topic);
			}
		}
		if (zkc != null) {
			zkPool.release(zkc);
			zkc = null;
		}
		return list;
	}

	public static String getNewPartitionInfo() {
		List<TopicDomain> list = getAllPartitionsInfo();
		Map<String, java.util.Set<Integer>> map = new HashMap<String, java.util.Set<Integer>>();
		for (TopicDomain td : list) {
			if (map.containsKey(td.getTopic())) {
				java.util.Set<Integer> set = map.get(td.getTopic());
				set.add(td.getPartition());
			} else {
				java.util.Set<Integer> set = new java.util.HashSet<Integer>();
				set.add(td.getPartition());
				map.put(td.getTopic(), set);
			}
		}

		List<PartitionsDomain> listPartitions = new ArrayList<PartitionsDomain>();
		int id = 0;
		for (Entry<String, java.util.Set<Integer>> entry : map.entrySet()) {
			PartitionsDomain pd = new PartitionsDomain();
			pd.setTopic(entry.getKey());
			pd.setId(++id);
			pd.setPartitions(entry.getValue());
			pd.setPartitionNumbers(entry.getValue().size());
			listPartitions.add(pd);
		}
		return listPartitions.toString();
	}

	public static Map<String, List<TopicDomain>> getPartitionInfo() {
		List<TopicDomain> list = getAllPartitionsInfo();

		Map<String, List<TopicDomain>> map = new HashMap<String, List<TopicDomain>>();
		for (int i = 0; i < list.size(); i++) {
			Object key = map.get(list.get(i).getTopic());
			List<TopicDomain> tmp = null;
			if (key == null) {
				tmp = new ArrayList<TopicDomain>();
				tmp.add(list.get(i));
				map.put(list.get(i).getTopic(), tmp);
			} else {
				tmp = map.get(list.get(i).getTopic());
				tmp.add(list.get(i));
				map.put(list.get(i).getTopic(), tmp);
			}
		}

		return map;
	}

}
