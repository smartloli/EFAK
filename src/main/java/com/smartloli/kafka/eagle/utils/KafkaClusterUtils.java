package com.smartloli.kafka.eagle.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kafka.api.PartitionOffsetRequestInfo;
import kafka.cluster.Broker;
import kafka.common.TopicAndPartition;
import kafka.consumer.ConsumerThreadId;
import kafka.api.OffsetRequest;
import kafka.javaapi.OffsetResponse;
import kafka.javaapi.PartitionMetadata;
import kafka.javaapi.TopicMetadata;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.TopicMetadataResponse;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.utils.ZkUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.smartloli.kafka.eagle.domain.OffsetZkDomain;
import com.smartloli.kafka.eagle.domain.PartitionsDomain;
import com.smartloli.kafka.eagle.domain.TopicDomain;

import scala.Option;
import scala.Tuple2;
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

	private static Logger LOG = LoggerFactory.getLogger(KafkaClusterUtils.class);

	public static void main(String[] args) {
		// System.out.println(getAllBrokersInfo());
		System.out.println(getOffset("words", "group1", 0));
	}

	public static List<String> findTopicPartition(String topic) {
		if (zkc == null) {
			zkc = zkPool.getZkClient();
		}
		Seq<String> seq = ZkUtils.getChildren(zkc, ZkUtils.BrokerTopicsPath() + "/" + topic + "/partitions");
		List<String> listSeq = JavaConversions.seqAsJavaList(seq);
		if (zkc != null) {
			zkPool.release(zkc);
			zkc = null;
		}
		return listSeq;
	}

	public static boolean findTopicIsConsumer(String topic, String group) {
		if (zkc == null) {
			zkc = zkPool.getZkClient();
		}
		String ownersPath = ZkUtils.ConsumersPath() + "/" + group + "/owners/" + topic;
		boolean status = ZkUtils.pathExists(zkc, ownersPath);
		if (zkc != null) {
			zkPool.releaseZKSerializer(zkc);
			zkc = null;
		}
		return status;
	}

	public static OffsetZkDomain getOffset(String topic, String group, int partition) {
		if (zkc == null) {
			zkc = zkPool.getZkClientSerializer();
		}
		OffsetZkDomain offsetZk = new OffsetZkDomain();
		String offsetPath = ZkUtils.ConsumersPath() + "/" + group + "/offsets/" + topic + "/" + partition;
		String ownersPath = ZkUtils.ConsumersPath() + "/" + group + "/owners/" + topic + "/" + partition;
		Tuple2<Option<String>, Stat> tuple = null;
		try {
			tuple = ZkUtils.readDataMaybeNull(zkc, offsetPath);
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
			if (zkc != null) {
				zkPool.releaseZKSerializer(zkc);
				zkc = null;
			}
			return offsetZk;
		}
		long offsetSize = Long.parseLong(tuple._1.get());
		if (ZkUtils.pathExists(zkc, ownersPath)) {
			Tuple2<String, Stat> tuple2 = ZkUtils.readData(zkc, ownersPath);
			offsetZk.setOwners(tuple2._1);
		} else {
			offsetZk.setOwners("");
		}
		offsetZk.setOffset(offsetSize);
		offsetZk.setCreate(CalendarUtils.timeSpan2StrDate(tuple._2.getCtime()));
		offsetZk.setModify(CalendarUtils.timeSpan2StrDate(tuple._2.getMtime()));
		if (zkc != null) {
			zkPool.releaseZKSerializer(zkc);
			zkc = null;
		}
		return offsetZk;
	}

	public static long getLogSize(List<String> hosts, String topic, int partition) {
		String clientName = "Client_" + topic + "_" + partition;
		Broker leaderBroker = getLeaderBroker(hosts, topic, partition);
		String reaHost = null;
		int port = 9092;
		if (leaderBroker != null) {
			reaHost = leaderBroker.host();
			port = leaderBroker.port();
		} else {
			System.out.println("Partition of Host is not find");
			LOG.error("Partition of Host is not find");
		}
		SimpleConsumer simpleConsumer = new SimpleConsumer(reaHost, port, 100000, 64 * 1024, clientName);
		TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
		Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
		requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(OffsetRequest.LatestTime(), 1));
		kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(requestInfo, OffsetRequest.CurrentVersion(), clientName);
		OffsetResponse response = simpleConsumer.getOffsetsBefore(request);
		if (response.hasError()) {
			System.out.println("Error fetching data Offset , Reason: " + response.errorCode(topic, partition));
			LOG.error("Error fetching data Offset , Reason: " + response.errorCode(topic, partition));
			return 0;
		}
		long[] offsets = response.offsets(topic, partition);
		return offsets[0];
	}

	private static Broker getLeaderBroker(List<String> hosts, String topic, int partition) {
		String clientName = "Client_Leader_LookUp";
		SimpleConsumer consumer = null;
		PartitionMetadata partitionMetaData = null;
		try {
			for (String host : hosts) {
				String ip = host.split(":")[0];
				String port = host.split(":")[1];
				consumer = new SimpleConsumer(ip, Integer.parseInt(port), 10000, 64 * 1024, clientName);
				if (consumer != null) {
					break;
				}
			}
			List<String> topics = new ArrayList<String>();
			topics.add(topic);
			TopicMetadataRequest request = new TopicMetadataRequest(topics);
			TopicMetadataResponse reponse = consumer.send(request);
			List<TopicMetadata> topicMetadataList = reponse.topicsMetadata();
			for (TopicMetadata topicMetadata : topicMetadataList) {
				for (PartitionMetadata metadata : topicMetadata.partitionsMetadata()) {
					if (metadata.partitionId() == partition) {
						partitionMetaData = metadata;
						break;
					}
				}
			}
			if (partitionMetaData != null) {
				return partitionMetaData.leader();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Map<String, List<String>> getConsumers() {
		if (zkc == null) {
			zkc = zkPool.getZkClient();
		}
		String consumersPath = ZkUtils.ConsumersPath();
		Seq<String> seq = ZkUtils.getChildren(zkc, consumersPath);
		List<String> listSeq = JavaConversions.seqAsJavaList(seq);
		Map<String, List<String>> mapConsumers = new HashMap<String, List<String>>();
		for (String group : listSeq) {
			Seq<String> tmp = ZkUtils.getChildren(zkc, consumersPath + "/" + group + "/owners");
			List<String> list = JavaConversions.seqAsJavaList(tmp);
			mapConsumers.put(group, list);
		}
		if (zkc != null) {
			zkPool.release(zkc);
			zkc = null;
		}
		return mapConsumers;
	}

	/**
	 * Get kafka active consumer topic
	 * 
	 * @return
	 */
	public static Map<String, List<String>> getActiveTopic() {
		if (zkc == null) {
			zkc = zkPool.getZkClientSerializer();
		}
		Seq<String> seq = ZkUtils.getChildren(zkc, ZkUtils.ConsumersPath());
		List<String> listSeq = JavaConversions.seqAsJavaList(seq);
		JSONArray arr = new JSONArray();
		for (String group : listSeq) {
			scala.collection.mutable.Map<String, scala.collection.immutable.List<ConsumerThreadId>> map = ZkUtils.getConsumersPerTopic(zkc, group, false);
			for (Entry<String, ?> entry : JavaConversions.mapAsJavaMap(map).entrySet()) {
				JSONObject obj = new JSONObject();
				obj.put("topic", entry.getKey());
				obj.put("group", group);
				arr.add(obj);
			}
		}
		Map<String, List<String>> actvTopic = new HashMap<String, List<String>>();
		for (Object object : arr) {
			JSONObject obj = (JSONObject) object;
			if (actvTopic.containsKey(obj.getString("topic"))) {
				actvTopic.get(obj.getString("topic")).add(obj.getString("group"));
			} else {
				List<String> group = new ArrayList<String>();
				group.add(obj.getString("group"));
				actvTopic.put(obj.getString("topic"), group);
			}
		}
		if (zkc != null) {
			zkPool.releaseZKSerializer(zkc);
			zkc = null;
		}
		return actvTopic;
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
