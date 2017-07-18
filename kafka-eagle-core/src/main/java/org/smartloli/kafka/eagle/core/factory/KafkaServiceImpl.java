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
package org.smartloli.kafka.eagle.core.factory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.I0Itec.zkclient.ZkClient;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.JaasUtils;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.domain.*;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.common.util.ZKPoolUtils;
import org.smartloli.kafka.eagle.common.util.Constants.Kafka;
import org.smartloli.kafka.eagle.common.util.KafkaPartitioner;
import org.smartloli.kafka.eagle.core.ipc.KafkaOffsetGetter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import kafka.admin.AdminClient;
import kafka.admin.AdminClient.ConsumerGroupSummary;
import kafka.admin.AdminClient.ConsumerSummary;
import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.admin.TopicCommand;
import kafka.api.OffsetRequest;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.cluster.BrokerEndPoint;
import kafka.common.OffsetAndMetadata;
import kafka.common.TopicAndPartition;
import kafka.consumer.ConsumerThreadId;
import kafka.coordinator.GroupOverview;
import kafka.coordinator.GroupTopicPartition;
import kafka.javaapi.OffsetResponse;
import kafka.javaapi.PartitionMetadata;
import kafka.javaapi.TopicMetadata;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.TopicMetadataResponse;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.utils.ZkUtils;
import scala.Option;
import scala.Tuple2;
import scala.collection.Iterator;
import scala.collection.JavaConversions;
import scala.collection.Seq;

/**
 * Implements KafkaService all method.
 * 
 * @author smartloli.
 *
 *         Created by Jan 18, 2017.
 * 
 *         Update by hexiang 20170216
 * 
 * @see org.smartloli.kafka.eagle.core.factory.KafkaService
 */
public class KafkaServiceImpl implements KafkaService {

	private final String BROKER_IDS_PATH = "/brokers/ids";
	private final String BROKER_TOPICS_PATH = "/brokers/topics";
	private final String CONSUMERS_PATH = "/consumers";
	private final String TOPIC_ISR = "/brokers/topics/%s/partitions/%s/state";
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
		loop : for (String seed : a_seedBrokers) {
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
	public boolean findTopicAndGroupExist(String clusterAlias, String topic, String group) {
		ZkClient zkc = zkPool.getZkClient(clusterAlias);
		String ownersPath = CONSUMERS_PATH + "/" + group + "/owners/" + topic;
		boolean status = ZkUtils.apply(zkc, false).pathExists(ownersPath);
		if (zkc != null) {
			zkPool.release(clusterAlias, zkc);
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
	public List<String> findTopicPartition(String clusterAlias, String topic) {
		ZkClient zkc = zkPool.getZkClient(clusterAlias);
		Seq<String> brokerTopicsPaths = ZkUtils.apply(zkc, false).getChildren(BROKER_TOPICS_PATH + "/" + topic + "/partitions");
		List<String> topicAndPartitions = JavaConversions.seqAsJavaList(brokerTopicsPaths);
		if (zkc != null) {
			zkPool.release(clusterAlias, zkc);
			zkc = null;
			brokerTopicsPaths = null;
		}
		return topicAndPartitions;
	}

	/** Get kafka active consumer topic. */
	public Map<String, List<String>> getActiveTopic(String clusterAlias) {
		ZkClient zkc = zkPool.getZkClientSerializer(clusterAlias);
		Map<String, List<String>> actvTopics = new HashMap<String, List<String>>();
		try {
			Seq<String> subConsumerPaths = ZkUtils.apply(zkc, false).getChildren(CONSUMERS_PATH);
			List<String> groups = JavaConversions.seqAsJavaList(subConsumerPaths);
			JSONArray groupsAndTopics = new JSONArray();
			for (String group : groups) {
				scala.collection.mutable.Map<String, scala.collection.immutable.List<ConsumerThreadId>> topics = ZkUtils.apply(zkc, false).getConsumersPerTopic(group, false);
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
				zkPool.releaseZKSerializer(clusterAlias, zkc);
				zkc = null;
			}
		}
		return actvTopics;
	}

	/** Get all broker list from zookeeper. */
	public String getAllBrokersInfo(String clusterAlias) {
		ZkClient zkc = zkPool.getZkClientSerializer(clusterAlias);
		List<BrokersDomain> targets = new ArrayList<BrokersDomain>();
		if (ZkUtils.apply(zkc, false).pathExists(BROKER_IDS_PATH)) {
			Seq<String> subBrokerIdsPaths = ZkUtils.apply(zkc, false).getChildren(BROKER_IDS_PATH);
			List<String> brokerIdss = JavaConversions.seqAsJavaList(subBrokerIdsPaths);
			int id = 0;
			for (String ids : brokerIdss) {
				try {
					Tuple2<Option<String>, Stat> tuple = ZkUtils.apply(zkc, false).readDataMaybeNull(BROKER_IDS_PATH + "/" + ids);
					BrokersDomain broker = new BrokersDomain();
					broker.setCreated(CalendarUtils.convertUnixTime2Date(tuple._2.getCtime()));
					broker.setModify(CalendarUtils.convertUnixTime2Date(tuple._2.getMtime()));
					if (SystemConfigUtils.getBooleanProperty("kafka.eagle.sasl.enable")) {
						String endpoints = JSON.parseObject(tuple._1.get()).getString("endpoints");
						String tmp = endpoints.split(File.separator + File.separator)[1];
						broker.setHost(tmp.substring(0, tmp.length() - 2).split(":")[0]);
						broker.setPort(Integer.valueOf(tmp.substring(0, tmp.length() - 2).split(":")[1]));
					} else {
						String host = JSON.parseObject(tuple._1.get()).getString("host");
						int port = JSON.parseObject(tuple._1.get()).getInteger("port");
						broker.setHost(host);
						broker.setPort(port);

					}
					broker.setJmxPort(JSON.parseObject(tuple._1.get()).getInteger("jmx_port"));
					broker.setId(++id);
					targets.add(broker);
				} catch (Exception ex) {
					LOG.error(ex.getMessage());
				}
			}
		}
		if (zkc != null) {
			zkPool.releaseZKSerializer(clusterAlias, zkc);
			zkc = null;
		}
		return targets.toString();
	}

	/** Get all topic info from zookeeper. */
	public String getAllPartitions(String clusterAlias) {
		ZkClient zkc = zkPool.getZkClientSerializer(clusterAlias);
		List<PartitionsDomain> targets = new ArrayList<PartitionsDomain>();
		if (ZkUtils.apply(zkc, false).pathExists(BROKER_TOPICS_PATH)) {
			Seq<String> subBrokerTopicsPaths = ZkUtils.apply(zkc, false).getChildren(BROKER_TOPICS_PATH);
			List<String> topics = JavaConversions.seqAsJavaList(subBrokerTopicsPaths);
			int id = 0;
			for (String topic : topics) {
				try {
					Tuple2<Option<String>, Stat> tuple = ZkUtils.apply(zkc, false).readDataMaybeNull(BROKER_TOPICS_PATH + "/" + topic);
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
			zkPool.releaseZKSerializer(clusterAlias, zkc);
			zkc = null;
		}
		// Sort topic by create time.
		Collections.sort(targets, new Comparator<PartitionsDomain>() {
			public int compare(PartitionsDomain arg0, PartitionsDomain arg1) {
				try {
					long hits0 = CalendarUtils.convertDate2UnixTime(arg0.getCreated());
					long hits1 = CalendarUtils.convertDate2UnixTime(arg1.getCreated());

					if (hits1 > hits0) {
						return 1;
					} else if (hits1 == hits0) {
						return 0;
					} else {
						return -1;
					}
				} catch (Exception e) {
					LOG.error("Convert date to unix time has error,msg is " + e.getMessage());
					return 0;
				}
			}
		});
		return targets.toString();
	}

	/** Obtaining kafka consumer information from zookeeper. */
	public Map<String, List<String>> getConsumers(String clusterAlias) {
		ZkClient zkc = zkPool.getZkClient(clusterAlias);
		Map<String, List<String>> consumers = new HashMap<String, List<String>>();
		try {
			Seq<String> subConsumerPaths = ZkUtils.apply(zkc, false).getChildren(CONSUMERS_PATH);
			List<String> groups = JavaConversions.seqAsJavaList(subConsumerPaths);
			for (String group : groups) {
				String path = CONSUMERS_PATH + "/" + group + "/owners";
				if (ZkUtils.apply(zkc, false).pathExists(path)) {
					Seq<String> owners = ZkUtils.apply(zkc, false).getChildren(path);
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
				zkPool.release(clusterAlias, zkc);
				zkc = null;
			}
		}
		return consumers;
	}

	/** Obtaining kafka consumer page information from zookeeper. */
	public Map<String, List<String>> getConsumers(String clusterAlias, PageParamDomain page) {
		ZkClient zkc = zkPool.getZkClient(clusterAlias);
		Map<String, List<String>> consumers = new HashMap<String, List<String>>();
		try {
			if (page.getSearch().length() > 0) {
				String path = CONSUMERS_PATH + "/" + page.getSearch() + "/owners";
				if (ZkUtils.apply(zkc, false).pathExists(path)) {
					Seq<String> owners = ZkUtils.apply(zkc, false).getChildren(path);
					List<String> ownersSerialize = JavaConversions.seqAsJavaList(owners);
					consumers.put(page.getSearch(), ownersSerialize);
				} else {
					LOG.error("Consumer Path[" + path + "] is not exist.");
				}
			} else {
				Seq<String> subConsumersPaths = ZkUtils.apply(zkc, false).getChildren(CONSUMERS_PATH);
				List<String> groups = JavaConversions.seqAsJavaList(subConsumersPaths);
				int offset = 0;
				for (String group : groups) {
					if (offset < (page.getiDisplayLength() + page.getiDisplayStart()) && offset >= page.getiDisplayStart()) {
						String path = CONSUMERS_PATH + "/" + group + "/owners";
						if (ZkUtils.apply(zkc, false).pathExists(path)) {
							Seq<String> owners = ZkUtils.apply(zkc, false).getChildren(path);
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
				zkPool.release(clusterAlias, zkc);
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
	public OffsetZkDomain getOffset(String clusterAlias, String topic, String group, int partition) {
		ZkClient zkc = zkPool.getZkClientSerializer(clusterAlias);
		OffsetZkDomain offsetZk = new OffsetZkDomain();
		String offsetPath = CONSUMERS_PATH + "/" + group + "/offsets/" + topic + "/" + partition;
		String ownersPath = CONSUMERS_PATH + "/" + group + "/owners/" + topic + "/" + partition;
		Tuple2<Option<String>, Stat> tuple = null;
		try {
			if (ZkUtils.apply(zkc, false).pathExists(offsetPath)) {
				tuple = ZkUtils.apply(zkc, false).readDataMaybeNull(offsetPath);
			} else {
				LOG.info("Partition[" + partition + "],OffsetPath[" + offsetPath + "] is not exist!");
				if (zkc != null) {
					zkPool.releaseZKSerializer(clusterAlias, zkc);
					zkc = null;
				}
				return offsetZk;
			}
		} catch (Exception ex) {
			LOG.error("Partition[" + partition + "],get offset has error,msg is " + ex.getMessage());
			if (zkc != null) {
				zkPool.releaseZKSerializer(clusterAlias, zkc);
				zkc = null;
			}
			return offsetZk;
		}
		long offsetSize = Long.parseLong(tuple._1.get());
		if (ZkUtils.apply(zkc, false).pathExists(ownersPath)) {
			Tuple2<String, Stat> tuple2 = ZkUtils.apply(zkc, false).readData(ownersPath);
			offsetZk.setOwners(tuple2._1 == null ? "" : tuple2._1);
		} else {
			offsetZk.setOwners("");
		}
		offsetZk.setOffset(offsetSize);
		offsetZk.setCreate(CalendarUtils.convertUnixTime2Date(tuple._2.getCtime()));
		offsetZk.setModify(CalendarUtils.convertUnixTime2Date(tuple._2.getMtime()));
		if (zkc != null) {
			zkPool.releaseZKSerializer(clusterAlias, zkc);
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
	private String getReplicasIsr(String clusterAlias, String topic, int partitionid) {
		ZkClient zkc = zkPool.getZkClientSerializer(clusterAlias);
		Seq<Object> repclicasAndPartition = ZkUtils.apply(zkc, false).getInSyncReplicasForPartition(topic, partitionid);
		List<Object> targets = JavaConversions.seqAsJavaList(repclicasAndPartition);
		if (zkc != null) {
			zkPool.releaseZKSerializer(clusterAlias, zkc);
			zkc = null;
		}
		return targets.toString();
	}

	/** Get zookeeper cluster information. */
	public String zkCluster(String clusterAlias) {
		String[] zks = SystemConfigUtils.getPropertyArray(clusterAlias + ".zk.list", ",");
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
	public JSONObject zkCliStatus(String clusterAlias) {
		JSONObject target = new JSONObject();
		ZkClient zkc = zkPool.getZkClient(clusterAlias);
		if (zkc != null) {
			target.put("live", true);
			target.put("list", SystemConfigUtils.getProperty(clusterAlias + ".zk.list"));
		} else {
			target.put("live", false);
			target.put("list", SystemConfigUtils.getProperty(clusterAlias + ".zk.list"));
		}
		if (zkc != null) {
			zkPool.release(clusterAlias, zkc);
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
	public Map<String, Object> create(String clusterAlias, String topicName, String partitions, String replic) {
		Map<String, Object> targets = new HashMap<String, Object>();
		int brokers = JSON.parseArray(getAllBrokersInfo(clusterAlias)).size();
		if (Integer.parseInt(replic) > brokers) {
			targets.put("status", "error");
			targets.put("info", "replication factor: " + replic + " larger than available brokers: " + brokers);
			return targets;
		}
		String formatter = SystemConfigUtils.getProperty("kafka.eagle.offset.storage");
		String zks = SystemConfigUtils.getProperty(clusterAlias + ".zk.list");
		if ("kafka".equals(formatter)) {
			ZkUtils zkUtils = ZkUtils.apply(zks, 30000, 30000, JaasUtils.isZkSecurityEnabled());
			AdminUtils.createTopic(zkUtils, topicName, Integer.parseInt(partitions), Integer.parseInt(replic), new Properties(), RackAwareMode.Enforced$.MODULE$);
			if (zkUtils != null) {
				zkUtils.close();
			}
		} else {
			String[] options = new String[]{"--create", "--zookeeper", zks, "--partitions", partitions, "--topic", topicName, "--replication-factor", replic};
			TopicCommand.main(options);
		}
		targets.put("status", "success");
		targets.put("info", "Create topic[" + topicName + "] has successed,partitions numbers is [" + partitions + "],replication-factor numbers is [" + replic + "]");
		return targets;
	}

	/** Delete topic to kafka cluster. */
	public Map<String, Object> delete(String clusterAlias, String topicName) {
		Map<String, Object> targets = new HashMap<String, Object>();
		ZkClient zkc = zkPool.getZkClient(clusterAlias);
		String formatter = SystemConfigUtils.getProperty("kafka.eagle.offset.storage");
		String zks = SystemConfigUtils.getProperty(clusterAlias + ".zk.list");
		if ("kafka".equals(formatter)) {
			ZkUtils zkUtils = ZkUtils.apply(zks, 30000, 30000, JaasUtils.isZkSecurityEnabled());
			AdminUtils.deleteTopic(zkUtils, topicName);
			if (zkUtils != null) {
				zkUtils.close();
			}
		} else {
			String[] options = new String[]{"--delete", "--zookeeper", zks, "--topic", topicName};
			TopicCommand.main(options);
		}
		targets.put("status", zkc.deleteRecursive(ZkUtils.getTopicPath(topicName)) == true ? "success" : "failed");
		if (zkc != null) {
			zkPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return targets;
	}

	/**
	 * Find leader through topic.
	 * 
	 * When we use kafka sasl, this method and not meet the requirements, from
	 * the kafka eagle v1.1.5 began to repeal the use of this method.
	 * 
	 * @param topic
	 * @return List
	 * @see org.smartloli.kafka.eagle.domain.MetadataDomain
	 */
	@Deprecated
	public List<MetadataDomain> findLeader(String clusterAlias, String topic) {
		List<MetadataDomain> targets = new ArrayList<>();

		SimpleConsumer consumer = null;
		for (HostsDomain broker : getBrokers(clusterAlias)) {
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
				metadata.setIsr(getReplicasIsr(clusterAlias, topic, part.partitionId()));
				metadata.setLeader(part.leader() == null ? -1 : part.leader().id());
				metadata.setPartitionId(part.partitionId());
				List<Integer> replicases = new ArrayList<>();
				for (BrokerEndPoint repli : part.replicas()) {
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
	private List<HostsDomain> getBrokers(String clusterAlias) {
		List<HostsDomain> targets = new ArrayList<HostsDomain>();
		JSONArray brokers = JSON.parseArray(getAllBrokersInfo(clusterAlias));
		for (Object object : brokers) {
			JSONObject broker = (JSONObject) object;
			HostsDomain host = new HostsDomain();
			host.setHost(broker.getString("host"));
			host.setPort(broker.getInteger("port"));
			targets.add(host);
		}
		return targets;
	}

	private String parseBrokerServer(String clusterAlias) {
		String brokerServer = "";
		JSONArray brokers = JSON.parseArray(getAllBrokersInfo(clusterAlias));
		for (Object object : brokers) {
			JSONObject broker = (JSONObject) object;
			brokerServer += broker.getString("host") + ":" + broker.getInteger("port") + ",";
		}
		return brokerServer.substring(0, brokerServer.length() - 1);
	}

	/** Convert query sql to object. */
	public KafkaSqlDomain parseSql(String clusterAlias, String sql) {
		return segments(clusterAlias, prepare(sql));
	}

	private String prepare(String sql) {
		sql = sql.trim();
		sql = sql.replaceAll("\\s+", " ");
		return sql;
	}

	private void sasl(Properties props, String bootstrapServers) {
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SystemConfigUtils.getProperty("kafka.eagle.sasl.protocol"));
		props.put(SaslConfigs.SASL_MECHANISM, SystemConfigUtils.getProperty("kafka.eagle.sasl.mechanism"));
	}

	private KafkaSqlDomain segments(String clusterAlias, String sql) {
		KafkaSqlDomain kafkaSql = new KafkaSqlDomain();
		kafkaSql.setMetaSql(sql);
		sql = sql.toLowerCase();
		kafkaSql.setSql(sql);
		if (sql.contains("and")) {
			sql = sql.split("and")[0];
		} else if (sql.contains("group by")) {
			sql = sql.split("group")[0];
		} else if (sql.contains("limit")) {
			sql = sql.split("limit")[0];
		}
		kafkaSql.getSchema().put("partition", "integer");
		kafkaSql.getSchema().put("offset", "bigint");
		kafkaSql.getSchema().put("msg", "varchar");
		if (!sql.startsWith("select")) {
			kafkaSql.setStatus(false);
			return kafkaSql;
		} else {
			kafkaSql.setStatus(true);
			Matcher matcher = Pattern.compile("select\\s.+from\\s(.+)where\\s(.+)").matcher(sql);
			if (matcher.find()) {
				kafkaSql.setTableName(matcher.group(1).trim().replaceAll("\"", ""));
				if (matcher.group(2).trim().startsWith("\"partition\"")) {
					String[] columns = matcher.group(2).trim().split("in")[1].replace("(", "").replace(")", "").trim().split(",");
					for (String column : columns) {
						try {
							kafkaSql.getPartition().add(Integer.parseInt(column));
						} catch (Exception e) {
							LOG.error("Parse parition[" + column + "] has error,msg is " + e.getMessage());
						}
					}
				}
			}
			kafkaSql.setSeeds(getBrokers(clusterAlias));
		}
		return kafkaSql;
	}

	/** Get kafka 0.10.x activer topics. */
	public Set<String> getKafkaActiverTopics(String clusterAlias, String group) {
		JSONArray consumerGroups = getKafkaMetadata(parseBrokerServer(clusterAlias), group);
		Set<String> topics = new HashSet<>();
		for (Object object : consumerGroups) {
			JSONObject consumerGroup = (JSONObject) object;
			for (Object topicObject : consumerGroup.getJSONArray("topicSub")) {
				JSONObject topic = (JSONObject) topicObject;
				if (consumerGroup.getString("owner") != "" || consumerGroup.getString("owner") != null) {
					topics.add(topic.getString("topic"));
				}
			}
		}
		return topics;
	}

	/** Get kafka 0.10.x consumer metadata. */
	public String getKafkaConsumer(String clusterAlias) {
		Properties prop = new Properties();
		JSONArray consumerGroups = new JSONArray();
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, parseBrokerServer(clusterAlias));

		if (SystemConfigUtils.getBooleanProperty("kafka.eagle.sasl.enable")) {
			sasl(prop, getKafkaBrokerServer(clusterAlias));
		}

		try {
			AdminClient adminClient = AdminClient.create(prop);
			scala.collection.immutable.Map<Node, scala.collection.immutable.List<GroupOverview>> opts = adminClient.listAllConsumerGroups();
			Iterator<Tuple2<Node, scala.collection.immutable.List<GroupOverview>>> groupOverview = opts.iterator();
			while (groupOverview.hasNext()) {
				Tuple2<Node, scala.collection.immutable.List<GroupOverview>> tuple = groupOverview.next();
				String node = tuple._1.host() + ":" + tuple._1.port();
				Iterator<GroupOverview> groups = tuple._2.iterator();
				while (groups.hasNext()) {
					GroupOverview group = groups.next();
					JSONObject consumerGroup = new JSONObject();
					String groupId = group.groupId();
					if (!groupId.contains("kafka.eagle")) {
						consumerGroup.put("group", groupId);
						consumerGroup.put("node", node);
						consumerGroup.put("meta", getKafkaMetadata(parseBrokerServer(clusterAlias), groupId));
						consumerGroups.add(consumerGroup);
					}
				}
			}
			adminClient.close();
		} catch (Exception e) {
			LOG.error("Get kafka consumer has error,msg is " + e.getMessage());
		}
		return consumerGroups.toJSONString();
	}

	/** Get kafka 0.10.x consumer metadata. */
	private JSONArray getKafkaMetadata(String bootstrapServers, String group) {
		Properties prop = new Properties();
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

		if (SystemConfigUtils.getBooleanProperty("kafka.eagle.sasl.enable")) {
			sasl(prop, bootstrapServers);
		}

		JSONArray consumerGroups = new JSONArray();
		try {
			AdminClient adminClient = AdminClient.create(prop);
			ConsumerGroupSummary cgs = adminClient.describeConsumerGroup(group);
			Option<scala.collection.immutable.List<ConsumerSummary>> opts = cgs.consumers();
			Iterator<ConsumerSummary> consumerSummarys = opts.get().iterator();
			while (consumerSummarys.hasNext()) {
				ConsumerSummary consumerSummary = consumerSummarys.next();
				Iterator<TopicPartition> topics = consumerSummary.assignment().iterator();
				JSONObject topicSub = new JSONObject();
				JSONArray topicSubs = new JSONArray();
				while (topics.hasNext()) {
					JSONObject object = new JSONObject();
					TopicPartition topic = topics.next();
					object.put("topic", topic.topic());
					object.put("partition", topic.partition());
					topicSubs.add(object);
				}
				topicSub.put("owner", consumerSummary.consumerId());
				topicSub.put("node", consumerSummary.host().replaceAll("/", ""));
				topicSub.put("topicSub", topicSubs);
				consumerGroups.add(topicSub);
			}
			adminClient.close();
		} catch (Exception e) {
			LOG.error("Get kafka consumer metadata has error, msg is " + e.getMessage());
		}
		return consumerGroups;
	}

	/** Get kafka 0.10.x consumer pages. */
	public String getKafkaActiverSize(String clusterAlias, String group) {
		JSONArray consumerGroups = getKafkaMetadata(parseBrokerServer(clusterAlias), group);
		int activerCounter = 0;
		Set<String> topics = new HashSet<>();
		for (Object object : consumerGroups) {
			JSONObject consumerGroup = (JSONObject) object;
			if (consumerGroup.getString("owner") != "" || consumerGroup.getString("owner") != null) {
				activerCounter++;
			}
			for (Object topicObject : consumerGroup.getJSONArray("topicSub")) {
				JSONObject topic = (JSONObject) topicObject;
				topics.add(topic.getString("topic"));
			}
		}
		JSONObject activerAndTopics = new JSONObject();
		activerAndTopics.put("activers", activerCounter);
		activerAndTopics.put("topics", topics.size());
		return activerAndTopics.toJSONString();
	}

	/** Get kafka 0.10.x consumer groups. */
	public int getKafkaConsumerGroups(String clusterAlias) {
		Properties prop = new Properties();
		int counter = 0;
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, parseBrokerServer(clusterAlias));

		if (SystemConfigUtils.getBooleanProperty("kafka.eagle.sasl.enable")) {
			sasl(prop, parseBrokerServer(clusterAlias));
		}

		try {
			AdminClient adminClient = AdminClient.create(prop);
			scala.collection.immutable.Map<Node, scala.collection.immutable.List<GroupOverview>> opts = adminClient.listAllConsumerGroups();
			Iterator<Tuple2<Node, scala.collection.immutable.List<GroupOverview>>> groupOverview = opts.iterator();
			while (groupOverview.hasNext()) {
				Tuple2<Node, scala.collection.immutable.List<GroupOverview>> tuple = groupOverview.next();
				Iterator<GroupOverview> groups = tuple._2.iterator();
				while (groups.hasNext()) {
					GroupOverview group = groups.next();
					String groupId = group.groupId();
					if (!groupId.contains("kafka.eagle")) {
						counter++;
					}
				}
			}
			adminClient.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return counter;
	}

	/** Get kafka 0.10.x consumer topic information. */
	public Set<String> getKafkaConsumerTopic(String clusterAlias, String group) {
		JSONArray consumerGroups = getKafkaMetadata(parseBrokerServer(clusterAlias), group);
		Set<String> topics = new HashSet<>();
		for (Object object : consumerGroups) {
			JSONObject consumerGroup = (JSONObject) object;
			for (Object topicObject : consumerGroup.getJSONArray("topicSub")) {
				JSONObject topic = (JSONObject) topicObject;
				topics.add(topic.getString("topic"));
			}
		}
		return topics;
	}

	/** Get kafka 0.10.x consumer group and topic. */
	public String getKafkaConsumerGroupTopic(String clusterAlias, String group) {
		return getKafkaMetadata(parseBrokerServer(clusterAlias), group).toJSONString();
	}

	/** Get kafka 0.10.x offset from topic. */
	public String getKafkaOffset(String clusterAlias) {
		if (KafkaOffsetGetter.multiKafkaConsumerOffsets.containsKey(clusterAlias)) {
			JSONArray targets = new JSONArray();
			for (Entry<GroupTopicPartition, OffsetAndMetadata> entry : KafkaOffsetGetter.multiKafkaConsumerOffsets.get(clusterAlias).entrySet()) {
				JSONObject object = new JSONObject();
				object.put("group", entry.getKey().group());
				object.put("topic", entry.getKey().topicPartition().topic());
				object.put("partition", entry.getKey().topicPartition().partition());
				object.put("offset", entry.getValue().offset());
				object.put("timestamp", entry.getValue().commitTimestamp());
				targets.add(object);
			}
			return targets.toJSONString();
		}
		return "";
	}

	/** Get kafka 0.10.x broker bootstrap server. */
	public String getKafkaBrokerServer(String clusterAlias) {
		return parseBrokerServer(clusterAlias);
	}

	/** Get kafka 0.10.x sasl logsize. */
	public long getKafkaLogSize(String clusterAlias, String topic, int partitionid) {
		Properties props = new Properties();
		props.put(ConsumerConfig.GROUP_ID_CONFIG, Kafka.KAFKA_EAGLE_SYSTEM_GROUP);
		props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getKafkaBrokerServer(clusterAlias));
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SystemConfigUtils.getProperty("kafka.eagle.sasl.protocol"));
		props.put(SaslConfigs.SASL_MECHANISM, SystemConfigUtils.getProperty("kafka.eagle.sasl.mechanism"));

		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		TopicPartition tp = new TopicPartition(topic, partitionid);
		consumer.assign(Collections.singleton(tp));
		java.util.Map<TopicPartition, Long> logsize = consumer.endOffsets(Collections.singleton(tp));
		consumer.close();
		return logsize.get(tp).longValue();
	}

	/** Get kafka 0.10.x sasl topic metadata. */
	public List<MetadataDomain> findKafkaLeader(String clusterAlias, String topic) {
		List<MetadataDomain> targets = new ArrayList<>();
		ZkClient zkc = zkPool.getZkClientSerializer(clusterAlias);
		if (ZkUtils.apply(zkc, false).pathExists(BROKER_TOPICS_PATH)) {
			Seq<String> subBrokerTopicsPaths = ZkUtils.apply(zkc, false).getChildren(BROKER_TOPICS_PATH);
			List<String> topics = JavaConversions.seqAsJavaList(subBrokerTopicsPaths);
			if (topics.contains(topic)) {
				Tuple2<Option<String>, Stat> tuple = ZkUtils.apply(zkc, false).readDataMaybeNull(BROKER_TOPICS_PATH + "/" + topic);
				JSONObject partitionObject = JSON.parseObject(tuple._1.get()).getJSONObject("partitions");
				for (String partition : partitionObject.keySet()) {
					String path = String.format(TOPIC_ISR, topic, Integer.valueOf(partition));
					Tuple2<Option<String>, Stat> tuple2 = ZkUtils.apply(zkc, false).readDataMaybeNull(path);
					JSONObject topicMetadata = JSON.parseObject(tuple2._1.get());
					MetadataDomain metadate = new MetadataDomain();
					metadate.setIsr(topicMetadata.getString("isr"));
					metadate.setLeader(topicMetadata.getInteger("leader"));
					metadate.setPartitionId(Integer.valueOf(partition));
					metadate.setReplicas(getReplicasIsr(clusterAlias, topic, Integer.valueOf(partition)));
					targets.add(metadate);
				}
			}
		}
		if (zkc != null) {
			zkPool.releaseZKSerializer(clusterAlias, zkc);
			zkc = null;
		}
		return targets;
	}

	/** Send mock message to kafka topic . */
	public boolean mockMessage(String clusterAlias, String topic, String message) {
		Properties props = new Properties();
		props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, getKafkaBrokerServer(clusterAlias));
		props.put(Kafka.KEY_SERIALIZER, StringSerializer.class.getCanonicalName());
		props.put(Kafka.VALUE_SERIALIZER, StringSerializer.class.getCanonicalName());
		props.put(Kafka.PARTITION_CLASS, KafkaPartitioner.class.getName());

		if (SystemConfigUtils.getBooleanProperty("kafka.eagle.sasl.enable")) {
			props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SystemConfigUtils.getProperty("kafka.eagle.sasl.protocol"));
			props.put(SaslConfigs.SASL_MECHANISM, SystemConfigUtils.getProperty("kafka.eagle.sasl.mechanism"));
		}

		Producer<String, String> producer = new KafkaProducer<>(props);
		JSONObject msg = new JSONObject();
		msg.put("date", CalendarUtils.getDate());
		msg.put("msg", message);

		producer.send(new ProducerRecord<String, String>(topic, new Date().getTime() + "", msg.toJSONString()));
		producer.close();

		return true;
	}
}
