package com.smartloli.kafka.eagle.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.TopicAndPartition;
import kafka.consumer.ConsumerThreadId;
import kafka.api.OffsetRequest;
import kafka.javaapi.OffsetResponse;
import kafka.javaapi.PartitionMetadata;
import kafka.javaapi.TopicMetadata;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.utils.ZkUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartloli.kafka.eagle.domain.BrokersDomain;
import com.smartloli.kafka.eagle.domain.OffsetZkDomain;
import com.smartloli.kafka.eagle.domain.PartitionsDomain;

import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.Seq;

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
		System.out.println(getLogSize(Arrays.asList("master:9092"), "words", 0));
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
			if (ZkUtils.pathExists(zkc, offsetPath)) {
				tuple = ZkUtils.readDataMaybeNull(zkc, offsetPath);
			} else {
				LOG.info("partition[" + partition + "],offsetPath[" + offsetPath + "] is not exist!");
				return offsetZk;
			}
		} catch (Exception ex) {
			LOG.error("partition[" + partition + "],get offset has error,msg is " + ex.getMessage());
			if (zkc != null) {
				zkPool.releaseZKSerializer(zkc);
				zkc = null;
			}
			try {
				zkPool.closePool();
			} catch (Exception exx) {
				LOG.error("Close zk pool has error,msg is " + exx.getMessage());
			}
			return offsetZk;
		}
		long offsetSize = Long.parseLong(tuple._1.get());
		if (ZkUtils.pathExists(zkc, ownersPath)) {
			Tuple2<String, Stat> tuple2 = ZkUtils.readData(zkc, ownersPath);
			offsetZk.setOwners(tuple2._1 == null ? "" : tuple2._1);
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
		LOG.info("Find leader hosts [" + hosts + "]");
		PartitionMetadata metadata = findLeader(hosts, topic, partition);
		if (metadata == null) {
			LOG.error("[KafkaClusterUtils.getLogSize()] - Can't find metadata for Topic and Partition. Exiting");
			return 0L;
		}
		if (metadata.leader() == null) {
			LOG.error("[KafkaClusterUtils.getLogSize()] - Can't find Leader for Topic and Partition. Exiting");
			return 0L;
		}

		String clientName = "Client_" + topic + "_" + partition;
		String reaHost = metadata.leader().host();
		int port = metadata.leader().port();

		long ret = 0L;
		try {
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
			ret = offsets[0];
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		}
		return ret;
	}

	private static PartitionMetadata findLeader(List<String> a_seedBrokers, String a_topic, int a_partition) {
		PartitionMetadata returnMetaData = null;
		loop: for (String seed : a_seedBrokers) {
			SimpleConsumer consumer = null;
			try {
				String ip = seed.split(":")[0];
				String port = seed.split(":")[1];
				consumer = new SimpleConsumer(ip, Integer.parseInt(port), 10000, 64 * 1024, "leaderLookup");
				List<String> topics = Collections.singletonList(a_topic);
				TopicMetadataRequest req = new TopicMetadataRequest(topics);
				kafka.javaapi.TopicMetadataResponse resp = consumer.send(req);

				List<TopicMetadata> metaData = resp.topicsMetadata();
				for (TopicMetadata item : metaData) {
					for (PartitionMetadata part : item.partitionsMetadata()) {
						if (part.partitionId() == a_partition) {
							returnMetaData = part;
							break loop;
						}
					}
				}
			} catch (Exception e) {
				LOG.error("Error communicating with Broker [" + seed + "] to find Leader for [" + a_topic + ", " + a_partition + "] Reason: " + e);
			} finally {
				if (consumer != null)
					consumer.close();
			}
		}
		return returnMetaData;
	}

	public static Map<String, List<String>> getConsumers() {
		if (zkc == null) {
			zkc = zkPool.getZkClient();
		}
		Map<String, List<String>> mapConsumers = new HashMap<String, List<String>>();
		try {
			String consumersPath = ZkUtils.ConsumersPath();
			Seq<String> seq = ZkUtils.getChildren(zkc, consumersPath);
			List<String> listSeq = JavaConversions.seqAsJavaList(seq);
			for (String group : listSeq) {
				Seq<String> tmp = ZkUtils.getChildren(zkc, consumersPath + "/" + group + "/owners");
				List<String> list = JavaConversions.seqAsJavaList(tmp);
				mapConsumers.put(group, list);
			}
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
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
		Map<String, List<String>> actvTopic = new HashMap<String, List<String>>();
		try {
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
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
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
	 * Get all broker list
	 * 
	 * @return
	 */
	public static String getAllBrokersInfo() {
		if (zkc == null) {
			zkc = zkPool.getZkClientSerializer();
		}
		List<BrokersDomain> list = new ArrayList<BrokersDomain>();
		if (ZkUtils.pathExists(zkc, ZkUtils.BrokerIdsPath())) {
			Seq<String> seq = ZkUtils.getChildren(zkc, ZkUtils.BrokerIdsPath());
			List<String> listSeq = JavaConversions.seqAsJavaList(seq);
			int id = 0;
			for (String ids : listSeq) {
				try {
					Tuple2<Option<String>, Stat> tuple = ZkUtils.readDataMaybeNull(zkc, ZkUtils.BrokerIdsPath() + "/" + ids);
					BrokersDomain broker = new BrokersDomain();
					broker.setCreated(CalendarUtils.timeSpan2StrDate(tuple._2.getCtime()));
					broker.setModify(CalendarUtils.timeSpan2StrDate(tuple._2.getMtime()));
					String host = JSON.parseObject(tuple._1.get()).getString("host");
					int port = JSON.parseObject(tuple._1.get()).getInteger("port");
					broker.setHost(host);
					broker.setPort(port);
					broker.setId(++id);
					list.add(broker);
				} catch (Exception ex) {
					LOG.error(ex.getMessage());
				}
			}
		}
		if (zkc != null) {
			zkPool.releaseZKSerializer(zkc);
			zkc = null;
		}
		return list.toString();
	}

	/**
	 * Get topic info from zookeeper
	 * 
	 * @return
	 */
	public static String getAllPartitions() {
		if (zkc == null) {
			zkc = zkPool.getZkClientSerializer();
		}
		List<PartitionsDomain> list = new ArrayList<PartitionsDomain>();
		if (ZkUtils.pathExists(zkc, ZkUtils.BrokerTopicsPath())) {
			Seq<String> seq = ZkUtils.getChildren(zkc, ZkUtils.BrokerTopicsPath());
			List<String> listSeq = JavaConversions.seqAsJavaList(seq);
			int id = 0;
			for (String topic : listSeq) {
				try {
					Tuple2<Option<String>, Stat> tuple = ZkUtils.readDataMaybeNull(zkc, ZkUtils.BrokerTopicsPath() + "/" + topic);
					PartitionsDomain partition = new PartitionsDomain();
					partition.setId(++id);
					partition.setCreated(CalendarUtils.timeSpan2StrDate(tuple._2.getCtime()));
					partition.setModify(CalendarUtils.timeSpan2StrDate(tuple._2.getMtime()));
					partition.setTopic(topic);
					JSONObject partitionObject = JSON.parseObject(tuple._1.get()).getJSONObject("partitions");
					partition.setPartitionNumbers(partitionObject.size());
					partition.setPartitions(partitionObject.keySet());
					list.add(partition);
				} catch (Exception ex) {
					LOG.error(ex.getMessage());
				}
			}
		}
		if (zkc != null) {
			zkPool.releaseZKSerializer(zkc);
			zkc = null;
		}
		return list.toString();
	}

}
