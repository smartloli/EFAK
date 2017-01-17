/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.util;

import java.util.ArrayList;
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
import org.smartloli.kafka.eagle.domain.BrokersDomain;
import org.smartloli.kafka.eagle.domain.ConsumerPageDomain;
import org.smartloli.kafka.eagle.domain.OffsetZkDomain;
import org.smartloli.kafka.eagle.domain.PartitionsDomain;

import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.Seq;

/**
 * Get kafka cluster info,include topic,broker list and partitions from
 * zookeeper.
 *
 * @author smartloli.
 *
 *         Created by Mar 8, 2016
 */
public class KafkaClusterUtils {

	private final static String BROKER_IDS_PATH = "/brokers/ids";
	private final static String BROKER_TOPICS_PATH = "/brokers/topics";
	private final static String CONSUMERS_PATH = "/consumers";
	private final static Logger LOG = LoggerFactory.getLogger(KafkaClusterUtils.class);
	/** Instance Zookeeper client pool. */
	private static ZKPoolUtils zkPool = ZKPoolUtils.getInstance();

	/**
	 * Use Kafka low level consumer API to find leader.
	 * 
	 * @param a_seedBrokers
	 * @param a_topic
	 * @param a_partition
	 * @return PartitionMetadata.
	 * @see kafka.javaapi.PartitionMetadata
	 */
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

	/**
	 * According to topic and group to find out whether the conditions are
	 * consumers.
	 * 
	 * @param topic
	 *            Filter topic.
	 * @param group
	 *            Filter group
	 * @return Boolean.
	 */
	public static boolean findTopicIsConsumer(String topic, String group) {
		ZkClient zkc = zkPool.getZkClient();
		String ownersPath = CONSUMERS_PATH + "/" + group + "/owners/" + topic;
		boolean status = ZkUtils.pathExists(zkc, ownersPath);
		if (zkc != null) {
			zkPool.release(zkc);
			zkc = null;
		}
		return status;
	}

	/**
	 * Topic and partition information in the zookeeper cluster are selected by
	 * topic.
	 * 
	 * @param topic
	 *            Selected condition.
	 * @return List.
	 */
	public static List<String> findTopicPartition(String topic) {
		ZkClient zkc = zkPool.getZkClient();
		Seq<String> seq = ZkUtils.getChildren(zkc, BROKER_TOPICS_PATH + "/" + topic + "/partitions");
		List<String> listSeq = JavaConversions.seqAsJavaList(seq);
		if (zkc != null) {
			zkPool.release(zkc);
			zkc = null;
			seq = null;
		}
		return listSeq;
	}

	/** Get kafka active consumer topic. */
	public static Map<String, List<String>> getActiveTopic() {
		ZkClient zkc = zkPool.getZkClientSerializer();
		Map<String, List<String>> actvTopic = new HashMap<String, List<String>>();
		try {
			Seq<String> seq = ZkUtils.getChildren(zkc, CONSUMERS_PATH);
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
				String group = obj.getString("group");
				String topic = obj.getString("topic");
				if (actvTopic.containsKey(group + "_" + topic)) {
					actvTopic.get(group + "_" + topic).add(topic);
				} else {
					List<String> topics = new ArrayList<String>();
					topics.add(topic);
					actvTopic.put(group + "_" + topic, topics);
				}
			}
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		} finally {
			if (zkc != null) {
				zkPool.releaseZKSerializer(zkc);
				zkc = null;
			}
		}
		return actvTopic;
	}

	/** Get all broker list from zookeeper. */
	public static String getAllBrokersInfo() {
		ZkClient zkc = zkPool.getZkClientSerializer();
		List<BrokersDomain> list = new ArrayList<BrokersDomain>();
		if (ZkUtils.pathExists(zkc, BROKER_IDS_PATH)) {
			Seq<String> seq = ZkUtils.getChildren(zkc, BROKER_IDS_PATH);
			List<String> listSeq = JavaConversions.seqAsJavaList(seq);
			int id = 0;
			for (String ids : listSeq) {
				try {
					Tuple2<Option<String>, Stat> tuple = ZkUtils.readDataMaybeNull(zkc, BROKER_IDS_PATH + "/" + ids);
					BrokersDomain broker = new BrokersDomain();
					broker.setCreated(CalendarUtils.convertUnixTime2Date(tuple._2.getCtime()));
					broker.setModify(CalendarUtils.convertUnixTime2Date(tuple._2.getMtime()));
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

	/** Get all topic info from zookeeper. */
	public static String getAllPartitions() {
		ZkClient zkc = zkPool.getZkClientSerializer();
		List<PartitionsDomain> list = new ArrayList<PartitionsDomain>();
		if (ZkUtils.pathExists(zkc, BROKER_TOPICS_PATH)) {
			Seq<String> seq = ZkUtils.getChildren(zkc, BROKER_TOPICS_PATH);
			List<String> listSeq = JavaConversions.seqAsJavaList(seq);
			int id = 0;
			for (String topic : listSeq) {
				try {
					Tuple2<Option<String>, Stat> tuple = ZkUtils.readDataMaybeNull(zkc, BROKER_TOPICS_PATH + "/" + topic);
					PartitionsDomain partition = new PartitionsDomain();
					partition.setId(++id);
					partition.setCreated(CalendarUtils.convertUnixTime2Date(tuple._2.getCtime()));
					partition.setModify(CalendarUtils.convertUnixTime2Date(tuple._2.getMtime()));
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

	/** Obtaining kafka consumer information from zookeeper. */
	public static Map<String, List<String>> getConsumers() {
		ZkClient zkc = zkPool.getZkClient();
		Map<String, List<String>> mapConsumers = new HashMap<String, List<String>>();
		try {
			Seq<String> seq = ZkUtils.getChildren(zkc, CONSUMERS_PATH);
			List<String> listSeq = JavaConversions.seqAsJavaList(seq);
			for (String group : listSeq) {
				String path = CONSUMERS_PATH + "/" + group + "/owners";
				if (ZkUtils.pathExists(zkc, path)) {
					Seq<String> tmp = ZkUtils.getChildren(zkc, path);
					List<String> list = JavaConversions.seqAsJavaList(tmp);
					mapConsumers.put(group, list);
				} else {
					LOG.error("Consumer Path[" + path + "] is not exist.");
				}
			}
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		} finally {
			if (zkc != null) {
				zkPool.release(zkc);
				zkc = null;
			}
		}
		return mapConsumers;
	}

	/** Obtaining kafka consumer page information from zookeeper. */
	public static Map<String, List<String>> getConsumers(ConsumerPageDomain page) {
		ZkClient zkc = zkPool.getZkClient();
		Map<String, List<String>> mapConsumers = new HashMap<String, List<String>>();
		try {
			if (page.getSearch().length() > 0) {
				String path = CONSUMERS_PATH + "/" + page.getSearch() + "/owners";
				if (ZkUtils.pathExists(zkc, path)) {
					Seq<String> tmp = ZkUtils.getChildren(zkc, path);
					List<String> list = JavaConversions.seqAsJavaList(tmp);
					mapConsumers.put(page.getSearch(), list);
				} else {
					LOG.error("Consumer Path[" + path + "] is not exist.");
				}
			} else {
				Seq<String> seq = ZkUtils.getChildren(zkc, CONSUMERS_PATH);
				List<String> listSeq = JavaConversions.seqAsJavaList(seq);
				int offset = 0;
				for (String group : listSeq) {
					if (offset < (page.getiDisplayLength() + page.getiDisplayStart()) && offset >= page.getiDisplayStart()) {
						String path = CONSUMERS_PATH + "/" + group + "/owners";
						if (ZkUtils.pathExists(zkc, path)) {
							Seq<String> tmp = ZkUtils.getChildren(zkc, path);
							List<String> list = JavaConversions.seqAsJavaList(tmp);
							mapConsumers.put(group, list);
						} else {
							LOG.error("Consumer Path[" + path + "] is not exist.");
						}
					}
					offset++;
				}
			}
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		} finally {
			if (zkc != null) {
				zkPool.release(zkc);
				zkc = null;
			}
		}
		return mapConsumers;
	}

	/**
	 * Use Kafka low consumer API & get logsize size from zookeeper.
	 * 
	 * @param hosts
	 *            Zookeeper host list.
	 * @param topic
	 *            Appoint topic.
	 * @param partition
	 *            Appoint partition.
	 * @return Long.
	 */
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
				LOG.error("Error fetching data Offset , Reason: " + response.errorCode(topic, partition));
				return 0;
			}
			long[] offsets = response.offsets(topic, partition);
			ret = offsets[0];
			if (simpleConsumer != null) {
				simpleConsumer.close();
			}
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		}
		return ret;
	}

	/**
	 * According to group, topic and partition to get offset from zookeeper.
	 * 
	 * @param topic
	 *            Filter topic.
	 * @param group
	 *            Filter group.
	 * @param partition
	 *            Filter partition.
	 * @return OffsetZkDomain.
	 * 
	 * @see org.smartloli.kafka.eagle.domain.OffsetZkDomain
	 */
	public static OffsetZkDomain getOffset(String topic, String group, int partition) {
		ZkClient zkc = zkPool.getZkClientSerializer();
		OffsetZkDomain offsetZk = new OffsetZkDomain();
		String offsetPath = CONSUMERS_PATH + "/" + group + "/offsets/" + topic + "/" + partition;
		String ownersPath = CONSUMERS_PATH + "/" + group + "/owners/" + topic + "/" + partition;
		Tuple2<Option<String>, Stat> tuple = null;
		try {
			if (ZkUtils.pathExists(zkc, offsetPath)) {
				tuple = ZkUtils.readDataMaybeNull(zkc, offsetPath);
			} else {
				LOG.info("Partition[" + partition + "],OffsetPath[" + offsetPath + "] is not exist!");
				if (zkc != null) {
					zkPool.releaseZKSerializer(zkc);
					zkc = null;
				}
				return offsetZk;
			}
		} catch (Exception ex) {
			LOG.error("Partition[" + partition + "],get offset has error,msg is " + ex.getMessage());
			if (zkc != null) {
				zkPool.releaseZKSerializer(zkc);
				zkc = null;
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
		offsetZk.setCreate(CalendarUtils.convertUnixTime2Date(tuple._2.getCtime()));
		offsetZk.setModify(CalendarUtils.convertUnixTime2Date(tuple._2.getMtime()));
		if (zkc != null) {
			zkPool.releaseZKSerializer(zkc);
			zkc = null;
		}
		return offsetZk;
	}

	/**
	 * According to topic and partition to obtain Replicas & Isr.
	 * 
	 * @param topic
	 * @param partitionid
	 * @return String.
	 */
	public static String geyReplicasIsr(String topic, int partitionid) {
		ZkClient zkc = zkPool.getZkClientSerializer();
		Seq<Object> seq = ZkUtils.getInSyncReplicasForPartition(zkc, topic, partitionid);
		List<Object> listSeq = JavaConversions.seqAsJavaList(seq);
		if (zkc != null) {
			zkPool.releaseZKSerializer(zkc);
			zkc = null;
		}
		return listSeq.toString();
	}

	/** Get zookeeper cluster information. */
	public static String getZkCluster() {
		String[] zks = SystemConfigUtils.getPropertyArray("kafka.zk.list", ",");
		JSONArray arr = new JSONArray();
		int id = 1;
		for (String zk : zks) {
			JSONObject obj = new JSONObject();
			obj.put("id", id++);
			obj.put("ip", zk.split(":")[0]);
			obj.put("port", zk.split(":")[1]);
			obj.put("mode", ZKUtils.serverStatus(zk.split(":")[0], zk.split(":")[1]));
			arr.add(obj);
		}
		return arr.toJSONString();
	}

	/** Judge whether the zkcli is active. */
	public static JSONObject zkCliStatus() {
		JSONObject object = new JSONObject();
		ZkClient zkc = zkPool.getZkClient();
		if (zkc != null) {
			object.put("live", true);
			object.put("list", SystemConfigUtils.getProperty("kafka.zk.list"));
		} else {
			object.put("live", false);
			object.put("list", SystemConfigUtils.getProperty("kafka.zk.list"));
		}
		if (zkc != null) {
			zkPool.release(zkc);
			zkc = null;
		}
		return object;
	}
}
