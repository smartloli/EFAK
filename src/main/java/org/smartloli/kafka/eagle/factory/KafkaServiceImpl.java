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
package org.smartloli.kafka.eagle.factory;

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
import org.smartloli.kafka.eagle.domain.BrokersDomain;
import org.smartloli.kafka.eagle.domain.PageParamDomain;
import org.smartloli.kafka.eagle.domain.HostsDomain;
import org.smartloli.kafka.eagle.domain.MetadataDomain;
import org.smartloli.kafka.eagle.domain.OffsetZkDomain;
import org.smartloli.kafka.eagle.domain.PartitionsDomain;
import org.smartloli.kafka.eagle.util.CalendarUtils;
import org.smartloli.kafka.eagle.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.util.ZKPoolUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import kafka.admin.TopicCommand;
import kafka.api.OffsetRequest;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.cluster.Broker;
import kafka.common.TopicAndPartition;
import kafka.consumer.ConsumerThreadId;
import kafka.javaapi.OffsetResponse;
import kafka.javaapi.PartitionMetadata;
import kafka.javaapi.TopicMetadata;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.TopicMetadataResponse;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.utils.ZkUtils;
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.Seq;

/**
 * Implements KafkaService all method.
 * 
 * @author smartloli.
 *
 *         Created by Jan 18, 2017
 * 
 * @see org.smartloli.kafka.eagle.factory.KafkaService
 */
public class KafkaServiceImpl implements KafkaService {

	private final String BROKER_IDS_PATH = "/brokers/ids";
	private final String BROKER_TOPICS_PATH = "/brokers/topics";
	private final String CONSUMERS_PATH = "/consumers";
	private final Logger LOG = LoggerFactory.getLogger(KafkaServiceImpl.class);
	/** Instance Zookeeper client pool. */
	private ZKPoolUtils zkPool = ZKPoolUtils.getInstance();

	/** Zookeeper service interface. */
	private ZkService zkService = new ZkFactory().create();

	/**
	 * Use Kafka low level consumer API to find leader.
	 * 
	 * @param a_seedBrokers
	 * @param a_topic
	 * @param a_partition
	 * @return PartitionMetadata.
	 * @see kafka.javaapi.PartitionMetadata
	 */
	private PartitionMetadata findLeader(List<String> a_seedBrokers, String a_topic, int a_partition) {
		PartitionMetadata returnMetaData = null;
		loop: for (String seed : a_seedBrokers) {
			SimpleConsumer consumer = null;
			try {
				String ip = seed.split(":")[0];
				String port = seed.split(":")[1];
				consumer = new SimpleConsumer(ip, Integer.parseInt(port), 10000, 64 * 1024, "leaderLookup");
				List<String> topics = Collections.singletonList(a_topic);
				TopicMetadataRequest topicMetaReqst = new TopicMetadataRequest(topics);
				kafka.javaapi.TopicMetadataResponse topicMetaResp = consumer.send(topicMetaReqst);

				List<TopicMetadata> topicMetadatas = topicMetaResp.topicsMetadata();
				for (TopicMetadata item : topicMetadatas) {
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
	 * Find topic and group exist in zookeeper.
	 * 
	 * @param topic
	 *            Filter topic.
	 * @param group
	 *            Filter group
	 * @return Boolean.
	 */
	public boolean findTopicAndGroupExist(String topic, String group) {
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
	 * Obtaining metadata in zookeeper by topic.
	 * 
	 * @param topic
	 *            Selected condition.
	 * @return List.
	 */
	public List<String> findTopicPartition(String topic) {
		ZkClient zkc = zkPool.getZkClient();
		Seq<String> brokerTopicsPaths = ZkUtils.getChildren(zkc, BROKER_TOPICS_PATH + "/" + topic + "/partitions");
		List<String> topicAndPartitions = JavaConversions.seqAsJavaList(brokerTopicsPaths);
		if (zkc != null) {
			zkPool.release(zkc);
			zkc = null;
			brokerTopicsPaths = null;
		}
		return topicAndPartitions;
	}

	/** Get kafka active consumer topic. */
	public Map<String, List<String>> getActiveTopic() {
		ZkClient zkc = zkPool.getZkClientSerializer();
		Map<String, List<String>> actvTopics = new HashMap<String, List<String>>();
		try {
			Seq<String> subConsumerPaths = ZkUtils.getChildren(zkc, CONSUMERS_PATH);
			List<String> groups = JavaConversions.seqAsJavaList(subConsumerPaths);
			JSONArray groupsAndTopics = new JSONArray();
			for (String group : groups) {
				scala.collection.mutable.Map<String, scala.collection.immutable.List<ConsumerThreadId>> topics = ZkUtils.getConsumersPerTopic(zkc, group, false);
				for (Entry<String, ?> entry : JavaConversions.mapAsJavaMap(topics).entrySet()) {
					JSONObject groupAndTopic = new JSONObject();
					groupAndTopic.put("topic", entry.getKey());
					groupAndTopic.put("group", group);
					groupsAndTopics.add(groupAndTopic);
				}
			}
			for (Object object : groupsAndTopics) {
				JSONObject groupAndTopic = (JSONObject) object;
				String group = groupAndTopic.getString("group");
				String topic = groupAndTopic.getString("topic");
				if (actvTopics.containsKey(group + "_" + topic)) {
					actvTopics.get(group + "_" + topic).add(topic);
				} else {
					List<String> topics = new ArrayList<String>();
					topics.add(topic);
					actvTopics.put(group + "_" + topic, topics);
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
		return actvTopics;
	}

	/** Get all broker list from zookeeper. */
	public String getAllBrokersInfo() {
		ZkClient zkc = zkPool.getZkClientSerializer();
		List<BrokersDomain> targets = new ArrayList<BrokersDomain>();
		if (ZkUtils.pathExists(zkc, BROKER_IDS_PATH)) {
			Seq<String> subBrokerIdsPaths = ZkUtils.getChildren(zkc, BROKER_IDS_PATH);
			List<String> brokerIdss = JavaConversions.seqAsJavaList(subBrokerIdsPaths);
			int id = 0;
			for (String ids : brokerIdss) {
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
					targets.add(broker);
				} catch (Exception ex) {
					LOG.error(ex.getMessage());
				}
			}
		}
		if (zkc != null) {
			zkPool.releaseZKSerializer(zkc);
			zkc = null;
		}
		return targets.toString();
	}

	/** Get all topic info from zookeeper. */
	public String getAllPartitions() {
		ZkClient zkc = zkPool.getZkClientSerializer();
		List<PartitionsDomain> targets = new ArrayList<PartitionsDomain>();
		if (ZkUtils.pathExists(zkc, BROKER_TOPICS_PATH)) {
			Seq<String> subBrokerTopicsPaths = ZkUtils.getChildren(zkc, BROKER_TOPICS_PATH);
			List<String> topics = JavaConversions.seqAsJavaList(subBrokerTopicsPaths);
			int id = 0;
			for (String topic : topics) {
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
					targets.add(partition);
				} catch (Exception ex) {
					LOG.error(ex.getMessage());
				}
			}
		}
		if (zkc != null) {
			zkPool.releaseZKSerializer(zkc);
			zkc = null;
		}
		return targets.toString();
	}

	/** Obtaining kafka consumer information from zookeeper. */
	public Map<String, List<String>> getConsumers() {
		ZkClient zkc = zkPool.getZkClient();
		Map<String, List<String>> consumers = new HashMap<String, List<String>>();
		try {
			Seq<String> subConsumerPaths = ZkUtils.getChildren(zkc, CONSUMERS_PATH);
			List<String> groups = JavaConversions.seqAsJavaList(subConsumerPaths);
			for (String group : groups) {
				String path = CONSUMERS_PATH + "/" + group + "/owners";
				if (ZkUtils.pathExists(zkc, path)) {
					Seq<String> owners = ZkUtils.getChildren(zkc, path);
					List<String> ownersSerialize = JavaConversions.seqAsJavaList(owners);
					consumers.put(group, ownersSerialize);
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
		return consumers;
	}

	/** Obtaining kafka consumer page information from zookeeper. */
	public Map<String, List<String>> getConsumers(PageParamDomain page) {
		ZkClient zkc = zkPool.getZkClient();
		Map<String, List<String>> consumers = new HashMap<String, List<String>>();
		try {
			if (page.getSearch().length() > 0) {
				String path = CONSUMERS_PATH + "/" + page.getSearch() + "/owners";
				if (ZkUtils.pathExists(zkc, path)) {
					Seq<String> owners = ZkUtils.getChildren(zkc, path);
					List<String> ownersSerialize = JavaConversions.seqAsJavaList(owners);
					consumers.put(page.getSearch(), ownersSerialize);
				} else {
					LOG.error("Consumer Path[" + path + "] is not exist.");
				}
			} else {
				Seq<String> subConsumersPaths = ZkUtils.getChildren(zkc, CONSUMERS_PATH);
				List<String> groups = JavaConversions.seqAsJavaList(subConsumersPaths);
				int offset = 0;
				for (String group : groups) {
					if (offset < (page.getiDisplayLength() + page.getiDisplayStart()) && offset >= page.getiDisplayStart()) {
						String path = CONSUMERS_PATH + "/" + group + "/owners";
						if (ZkUtils.pathExists(zkc, path)) {
							Seq<String> owners = ZkUtils.getChildren(zkc, path);
							List<String> ownersSerialize = JavaConversions.seqAsJavaList(owners);
							consumers.put(group, ownersSerialize);
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
		return consumers;
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
	public long getLogSize(List<String> hosts, String topic, int partition) {
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
	public OffsetZkDomain getOffset(String topic, String group, int partition) {
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
	public String geyReplicasIsr(String topic, int partitionid) {
		ZkClient zkc = zkPool.getZkClientSerializer();
		Seq<Object> repclicasAndPartition = ZkUtils.getInSyncReplicasForPartition(zkc, topic, partitionid);
		List<Object> targets = JavaConversions.seqAsJavaList(repclicasAndPartition);
		if (zkc != null) {
			zkPool.releaseZKSerializer(zkc);
			zkc = null;
		}
		return targets.toString();
	}

	/** Get zookeeper cluster information. */
	public String zkCluster() {
		String[] zks = SystemConfigUtils.getPropertyArray("kafka.zk.list", ",");
		JSONArray targets = new JSONArray();
		int id = 1;
		for (String zk : zks) {
			JSONObject object = new JSONObject();
			object.put("id", id++);
			object.put("ip", zk.split(":")[0]);
			object.put("port", zk.split(":")[1]);
			object.put("mode", zkService.status(zk.split(":")[0], zk.split(":")[1]));
			targets.add(object);
		}
		return targets.toJSONString();
	}

	/** Judge whether the zkcli is active. */
	public JSONObject zkCliStatus() {
		JSONObject target = new JSONObject();
		ZkClient zkc = zkPool.getZkClient();
		if (zkc != null) {
			target.put("live", true);
			target.put("list", SystemConfigUtils.getProperty("kafka.zk.list"));
		} else {
			target.put("live", false);
			target.put("list", SystemConfigUtils.getProperty("kafka.zk.list"));
		}
		if (zkc != null) {
			zkPool.release(zkc);
			zkc = null;
		}
		return target;
	}

	/**
	 * Create topic to kafka cluster, it is worth noting that the backup number
	 * must be less than or equal to brokers data.
	 * 
	 * @param topicName
	 *            Create topic name.
	 * @param partitions
	 *            Create topic partitions.
	 * @param replic
	 *            Replic numbers.
	 * @return Map.
	 */
	public Map<String, Object> create(String topicName, String partitions, String replic) {
		Map<String, Object> targets = new HashMap<String, Object>();
		int brokers = JSON.parseArray(getAllBrokersInfo()).size();
		if (Integer.parseInt(replic) > brokers) {
			targets.put("status", "error");
			targets.put("info", "replication factor: " + replic + " larger than available brokers: " + brokers);
			return targets;
		}
		String zks = SystemConfigUtils.getProperty("kafka.zk.list");
		String[] options = new String[] { "--create", "--zookeeper", zks, "--partitions", partitions, "--topic", topicName, "--replication-factor", replic };
		TopicCommand.main(options);
		targets.put("status", "success");
		targets.put("info", "Create topic[" + topicName + "] has successed,partitions numbers is [" + partitions + "],replication-factor numbers is [" + replic + "]");
		return targets;
	}

	/**
	 * Find leader through topic.
	 * 
	 * @param topic
	 * @return List
	 * @see org.smartloli.kafka.eagle.domain.MetadataDomain
	 */
	public List<MetadataDomain> findLeader(String topic) {
		List<MetadataDomain> targets = new ArrayList<>();

		SimpleConsumer consumer = null;
		for (HostsDomain broker : getBrokers()) {
			try {
				consumer = new SimpleConsumer(broker.getHost(), broker.getPort(), 100000, 64 * 1024, "leaderLookup");
				if (consumer != null) {
					break;
				}
			} catch (Exception ex) {
				LOG.error(ex.getMessage());
			}
		}

		if (consumer == null) {
			LOG.error("Connection [SimpleConsumer] has failed,please check brokers.");
			return targets;
		}

		List<String> topics = Collections.singletonList(topic);
		TopicMetadataRequest topicMetaReqst = new TopicMetadataRequest(topics);
		TopicMetadataResponse topicMetaRespn = consumer.send(topicMetaReqst);
		if (topicMetaRespn == null) {
			LOG.error("Get [TopicMetadataResponse] has null.");
			return targets;
		}
		List<TopicMetadata> topicsMeta = topicMetaRespn.topicsMetadata();
		for (TopicMetadata item : topicsMeta) {
			for (PartitionMetadata part : item.partitionsMetadata()) {
				MetadataDomain metadata = new MetadataDomain();
				metadata.setIsr(geyReplicasIsr(topic, part.partitionId()));
				metadata.setLeader(part.leader() == null ? -1 : part.leader().id());
				metadata.setPartitionId(part.partitionId());
				List<Integer> replicases = new ArrayList<>();
				for (Broker repli : part.replicas()) {
					replicases.add(repli.id());
				}
				metadata.setReplicas(replicases.toString());
				targets.add(metadata);
			}
		}
		if (consumer != null) {
			consumer.close();
		}
		return targets;
	}

	/** Get kafka brokers from zookeeper. */
	private List<HostsDomain> getBrokers() {
		List<HostsDomain> targets = new ArrayList<HostsDomain>();
		JSONArray brokers = JSON.parseArray(getAllBrokersInfo());
		for (Object object : brokers) {
			JSONObject broker = (JSONObject) object;
			HostsDomain host = new HostsDomain();
			host.setHost(broker.getString("host"));
			host.setPort(broker.getInteger("port"));
			targets.add(host);
		}
		return targets;
	}

}
