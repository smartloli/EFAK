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
package org.smartloli.kafka.eagle.core.factory.v2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.protocol.MetadataInfo;
import org.smartloli.kafka.eagle.common.protocol.PartitionsInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.Kafka;
import org.smartloli.kafka.eagle.common.util.KafkaZKPoolUtils;
import org.smartloli.kafka.eagle.common.util.MathUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Strings;

import kafka.zk.KafkaZkClient;
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.Seq;

/**
 * Implements {@link BrokerService} all method.
 * 
 * @author smartloli.
 *
 *         Created by Jun 13, 2019
 */
public class BrokerServiceImpl implements BrokerService {

	private final String BROKER_IDS_PATH = "/brokers/ids";
	private final String BROKER_TOPICS_PATH = "/brokers/topics";
	private final String TOPIC_ISR = "/brokers/topics/%s/partitions/%s/state";
	private final Logger LOG = LoggerFactory.getLogger(BrokerServiceImpl.class);

	/** Instance Kafka Zookeeper client pool. */
	private KafkaZKPoolUtils kafkaZKPool = KafkaZKPoolUtils.getInstance();

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** Statistics topic total used as page. */
	public long topicNumbers(String clusterAlias) {
		return topicList(clusterAlias).size();
	}

	/** Exclude kafka topic(__consumer_offsets). */
	private void excludeTopic(List<String> topics) {
		if (topics.contains(Kafka.CONSUMER_OFFSET_TOPIC)) {
			topics.remove(Kafka.CONSUMER_OFFSET_TOPIC);
		}
	}

	/** Get search topic list numbers. */
	public long topicNumbers(String clusterAlias, String topic) {
		long count = 0L;
		List<String> topics = topicList(clusterAlias);
		for (String name : topics) {
			if (topic != null && name.contains(topic)) {
				count++;
			}
		}
		return count;
	}

	/** Statistics topic partitions total used as page. */
	public long partitionNumbers(String clusterAlias, String topic) {
		long count = 0L;
		if (Kafka.CONSUMER_OFFSET_TOPIC.equals(topic)) {
			return count;
		}
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		try {
			if (zkc.pathExists(BROKER_TOPICS_PATH + "/" + topic + "/partitions")) {
				Seq<String> subBrokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH + "/" + topic + "/partitions");
				count = JavaConversions.seqAsJavaList(subBrokerTopicsPaths).size();
			}
		} catch (Exception e) {
			LOG.error("Get topic partition numbers has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return count;
	}

	/** Get the number of page records for topic. */
	public List<PartitionsInfo> topicRecords(String clusterAlias, Map<String, Object> params) {
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		List<PartitionsInfo> targets = new ArrayList<PartitionsInfo>();
		List<String> topics = topicList(clusterAlias);
		try {
			int start = Integer.parseInt(params.get("start").toString());
			int length = Integer.parseInt(params.get("length").toString());
			if (params.containsKey("search") && params.get("search").toString().length() > 0) {
				String search = params.get("search").toString();
				int offset = 0;
				int id = start + 1;
				for (String topic : topics) {
					if (search != null && topic.contains(search)) {
						if (offset < (start + length) && offset >= start) {
							try {
								if (zkc.pathExists(BROKER_TOPICS_PATH + "/" + topic)) {
									Tuple2<Option<byte[]>, Stat> tuple = zkc.getDataAndStat(BROKER_TOPICS_PATH + "/" + topic);
									PartitionsInfo partition = new PartitionsInfo();
									partition.setId(id++);
									partition.setCreated(CalendarUtils.convertUnixTime2Date(tuple._2.getCtime()));
									partition.setModify(CalendarUtils.convertUnixTime2Date(tuple._2.getMtime()));
									partition.setTopic(topic);
									String tupleString = new String(tuple._1.get());
									JSONObject partitionObject = JSON.parseObject(tupleString).getJSONObject("partitions");
									partition.setPartitionNumbers(partitionObject.size());
									partition.setPartitions(partitionObject.keySet());
									targets.add(partition);
								}
							} catch (Exception ex) {
								ex.printStackTrace();
								LOG.error("Scan topic search from zookeeper has error, msg is " + ex.getMessage());
							}
						}
						offset++;
					}
				}
			} else {
				int offset = 0;
				int id = start + 1;
				for (String topic : topics) {
					if (offset < (start + length) && offset >= start) {
						try {
							if (zkc.pathExists(BROKER_TOPICS_PATH + "/" + topic)) {
								Tuple2<Option<byte[]>, Stat> tuple = zkc.getDataAndStat(BROKER_TOPICS_PATH + "/" + topic);
								PartitionsInfo partition = new PartitionsInfo();
								partition.setId(id++);
								partition.setCreated(CalendarUtils.convertUnixTime2Date(tuple._2.getCtime()));
								partition.setModify(CalendarUtils.convertUnixTime2Date(tuple._2.getMtime()));
								partition.setTopic(topic);
								String tupleString = new String(tuple._1.get());
								JSONObject partitionObject = JSON.parseObject(tupleString).getJSONObject("partitions");
								partition.setPartitionNumbers(partitionObject.size());
								partition.setPartitions(partitionObject.keySet());
								targets.add(partition);
							}
						} catch (Exception ex) {
							ex.printStackTrace();
							LOG.error("Scan topic page from zookeeper has error, msg is " + ex.getMessage());
						}
					}
					offset++;
				}
			}
		} catch (Exception e) {
			LOG.error("Get topic records has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return targets;
	}

	/** Get broker spread by topic. */
	public int getBrokerSpreadByTopic(String clusterAlias, String topic) {
		int spread = 0;
		try {
			List<MetadataInfo> topicMetas = topicMetadata(clusterAlias, topic);
			Set<Integer> brokerSizes = new HashSet<>();
			for (MetadataInfo meta : topicMetas) {
				List<Integer> replicasIntegers = new ArrayList<>();
				try {
					replicasIntegers = JSON.parseObject(meta.getReplicas(), new TypeReference<ArrayList<Integer>>() {
					});
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("Parse string to int list has error, msg is " + e.getCause().getMessage());
				}
				brokerSizes.addAll(replicasIntegers);
			}
			int brokerSize = kafkaService.getAllBrokersInfo(clusterAlias).size();
			spread = brokerSizes.size() * 100 / brokerSize;
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Get topic skewed info has error, msg is ", e);
		}
		return spread;
	}

	/** Get broker skewed by topic. */
	public int getBrokerSkewedByTopic(String clusterAlias, String topic) {
		int skewed = 0;
		try {
			List<MetadataInfo> topicMetas = topicMetadata(clusterAlias, topic);
			int partitionAndReplicaTopics = 0;
			Set<Integer> brokerSizes = new HashSet<>();
			Map<Integer, Integer> brokers = new HashMap<>();
			for (MetadataInfo meta : topicMetas) {
				List<Integer> replicasIntegers = new ArrayList<>();
				try {
					replicasIntegers = JSON.parseObject(meta.getReplicas(), new TypeReference<ArrayList<Integer>>() {
					});
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("Parse string to int list has error, msg is " + e.getCause().getMessage());
				}
				brokerSizes.addAll(replicasIntegers);
				partitionAndReplicaTopics += replicasIntegers.size();
				for (Integer brokerId : replicasIntegers) {
					if (brokers.containsKey(brokerId)) {
						int value = brokers.get(brokerId);
						brokers.put(brokerId, value + 1);
					} else {
						brokers.put(brokerId, 1);
					}
				}
			}
			int brokerSize = brokerSizes.size();
			int normalSkewedValue = MathUtils.ceil(brokerSize, partitionAndReplicaTopics);
			int brokerSkewSize = 0;
			for (Entry<Integer, Integer> entry : brokers.entrySet()) {
				if (entry.getValue() > normalSkewedValue) {
					brokerSkewSize++;
				}
			}
			skewed = brokerSkewSize * 100 / brokerSize;
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Get topic skewed info has error, msg is ", e);
		}
		return skewed;
	}

	/** Get broker skewed leader by topic. */
	public int getBrokerLeaderSkewedByTopic(String clusterAlias, String topic) {
		int leaderSkewed = 0;
		try {
			List<MetadataInfo> topicMetas = topicMetadata(clusterAlias, topic);
			Map<Integer, Integer> brokerLeaders = new HashMap<>();
			Set<Integer> brokerSizes = new HashSet<>();
			for (MetadataInfo meta : topicMetas) {
				List<Integer> replicasIntegers = new ArrayList<>();
				try {
					replicasIntegers = JSON.parseObject(meta.getReplicas(), new TypeReference<ArrayList<Integer>>() {
					});
				} catch (Exception e) {
					e.printStackTrace();
					LOG.error("Parse string to int list has error, msg is " + e.getCause().getMessage());
				}
				brokerSizes.addAll(replicasIntegers);
				if (brokerLeaders.containsKey(meta.getLeader())) {
					int value = brokerLeaders.get(meta.getLeader());
					brokerLeaders.put(meta.getLeader(), value + 1);
				} else {
					brokerLeaders.put(meta.getLeader(), 1);
				}
			}
			int brokerSize = brokerSizes.size();
			int brokerSkewLeaderNormal = MathUtils.ceil(brokerSize, topicMetas.size());
			int brokerSkewLeaderSize = 0;
			for (Entry<Integer, Integer> entry : brokerLeaders.entrySet()) {
				if (entry.getValue() > brokerSkewLeaderNormal) {
					brokerSkewLeaderSize++;
				}
			}
			leaderSkewed = brokerSkewLeaderSize * 100 / brokerSize;
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Get topic skewed info has error, msg is ", e);
		}
		return leaderSkewed;
	}

	/** Check topic from zookeeper metadata. */
	public boolean findKafkaTopic(String clusterAlias, String topic) {
		return topicList(clusterAlias).contains(topic);
	}

	/** Get kafka broker numbers from zookeeper. */
	public long brokerNumbers(String clusterAlias) {
		long count = 0;
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		try {
			if (zkc.pathExists(BROKER_IDS_PATH)) {
				Seq<String> subBrokerIdsPaths = zkc.getChildren(BROKER_IDS_PATH);
				count = JavaConversions.seqAsJavaList(subBrokerIdsPaths).size();
			}
		} catch (Exception e) {
			LOG.error("Get kafka broker numbers has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return count;
	}

	/** Get topic list from zookeeper. */
	public List<String> topicList(String clusterAlias) {
		List<String> topics = new ArrayList<>();
		if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.cgroup.enable")) {
			topics = SystemConfigUtils.getPropertyArrayList(clusterAlias + ".kafka.eagle.sasl.cgroup.topics", ",");
		} else {
			KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
			try {
				if (zkc.pathExists(BROKER_TOPICS_PATH)) {
					Seq<String> subBrokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH);
					topics = JavaConversions.seqAsJavaList(subBrokerTopicsPaths);
					excludeTopic(topics);
				}
			} catch (Exception e) {
				LOG.error("Get topic list has error, msg is " + e.getCause().getMessage());
				e.printStackTrace();
			}
			if (zkc != null) {
				kafkaZKPool.release(clusterAlias, zkc);
				zkc = null;
			}
		}
		return topics;
	}

	/** Get select topic list from zookeeper. */
	public String topicListParams(String clusterAlias, String search) {
		JSONArray targets = new JSONArray();
		int limit = 15;

		List<String> topics = topicList(clusterAlias);
		try {
			if (Strings.isNullOrEmpty(search)) {
				int id = 1;
				for (String topic : topics) {
					if (id <= limit) {
						JSONObject object = new JSONObject();
						object.put("id", id);
						object.put("name", topic);
						targets.add(object);
						id++;
					}
				}
			} else {
				int id = 1;
				for (String topic : topics) {
					if (topic.contains(search)) {
						if (id <= limit) {
							JSONObject object = new JSONObject();
							object.put("id", id);
							object.put("name", topic);
							targets.add(object);
							id++;
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Get topic list has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}
		return targets.toJSONString();
	}

	/** Scan topic meta page display from zookeeper and kafka. */
	public List<MetadataInfo> topicMetadataRecords(String clusterAlias, String topic, Map<String, Object> params) {
		List<MetadataInfo> targets = new ArrayList<>();
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		try {
			if (zkc.pathExists(BROKER_TOPICS_PATH)) {
				List<String> topics = topicList(clusterAlias);
				if (topics.contains(topic)) {
					int start = Integer.parseInt(params.get("start").toString());
					int length = Integer.parseInt(params.get("length").toString());
					int offset = 0;
					Tuple2<Option<byte[]>, Stat> tuple = zkc.getDataAndStat(BROKER_TOPICS_PATH + "/" + topic);
					String tupleString = new String(tuple._1.get());
					JSONObject partitionObject = JSON.parseObject(tupleString).getJSONObject("partitions");
					Set<Integer> partitionSet = new TreeSet<>();
					for (String partitionId : partitionObject.keySet()) {
						partitionSet.add(Integer.valueOf(partitionId));
					}
					Set<Integer> partitionSortSet = new TreeSet<>(new Comparator<Integer>() {
						@Override
						public int compare(Integer o1, Integer o2) {
							int diff = o1 - o2;// asc
							if (diff > 0) {
								return 1;
							} else if (diff < 0) {
								return -1;
							}
							return 0;
						}
					});
					partitionSortSet.addAll(partitionSet);
					for (int partition : partitionSortSet) {
						if (offset < (start + length) && offset >= start) {
							String path = String.format(TOPIC_ISR, topic, partition);
							Tuple2<Option<byte[]>, Stat> tuple2 = zkc.getDataAndStat(path);
							String tupleString2 = new String(tuple2._1.get());
							JSONObject topicMetadata = JSON.parseObject(tupleString2);
							MetadataInfo metadate = new MetadataInfo();
							metadate.setIsr(topicMetadata.getString("isr"));
							metadate.setLeader(topicMetadata.getInteger("leader"));
							metadate.setPartitionId(partition);
							metadate.setReplicas(kafkaService.getReplicasIsr(clusterAlias, topic, partition));
							long logSize = 0L;
							if ("kafka".equals(SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage"))) {
								logSize = kafkaService.getKafkaRealLogSize(clusterAlias, topic, partition);
							} else {
								logSize = kafkaService.getRealLogSize(clusterAlias, topic, partition);
							}
							List<Integer> isrIntegers = new ArrayList<>();
							List<Integer> replicasIntegers = new ArrayList<>();
							try {
								isrIntegers = JSON.parseObject(metadate.getIsr(), new TypeReference<ArrayList<Integer>>() {
								});
								replicasIntegers = JSON.parseObject(metadate.getReplicas(), new TypeReference<ArrayList<Integer>>() {
								});
							} catch (Exception e) {
								e.printStackTrace();
								LOG.error("Parse string to int list has error, msg is ", e);
							}
							if (isrIntegers.size() != replicasIntegers.size()) {
								// replicas lost
								metadate.setUnderReplicated(true);
							} else {
								// replicas normal
								metadate.setUnderReplicated(false);
							}
							if (replicasIntegers != null && replicasIntegers.size() > 0 && replicasIntegers.get(0) == metadate.getLeader()) {
								// partition preferred leader
								metadate.setPreferredLeader(true);
							} else {
								// partition occurs preferred leader exception
								metadate.setPreferredLeader(false);
							}
							metadate.setLogSize(logSize);
							targets.add(metadate);
						}
						offset++;
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Get topic metadata records has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return targets;
	}

	private List<MetadataInfo> topicMetadata(String clusterAlias, String topic) {
		List<MetadataInfo> targets = new ArrayList<>();
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		try {
			if (zkc.pathExists(BROKER_TOPICS_PATH)) {
				List<String> topics = topicList(clusterAlias);
				if (topics.contains(topic)) {
					Tuple2<Option<byte[]>, Stat> tuple = zkc.getDataAndStat(BROKER_TOPICS_PATH + "/" + topic);
					String tupleString = new String(tuple._1.get());
					JSONObject partitionObject = JSON.parseObject(tupleString).getJSONObject("partitions");
					for (String partition : partitionObject.keySet()) {
						String path = String.format(TOPIC_ISR, topic, Integer.valueOf(partition));
						Tuple2<Option<byte[]>, Stat> tuple2 = zkc.getDataAndStat(path);
						String tupleString2 = new String(tuple2._1.get());
						JSONObject topicMetadata = JSON.parseObject(tupleString2);
						MetadataInfo metadate = new MetadataInfo();
						metadate.setIsr(topicMetadata.getString("isr"));
						metadate.setLeader(topicMetadata.getInteger("leader"));
						metadate.setPartitionId(Integer.valueOf(partition));
						metadate.setReplicas(kafkaService.getReplicasIsr(clusterAlias, topic, Integer.valueOf(partition)));
						targets.add(metadate);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Get topic metadata records has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return targets;
	}

	/** Get topic producer logsize total. */
	public long getTopicLogSizeTotal(String clusterAlias, String topic) {
		long logSize = 0L;
		if (Kafka.CONSUMER_OFFSET_TOPIC.equals(topic)) {
			return logSize;
		}
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		try {
			if (zkc.pathExists(BROKER_TOPICS_PATH + "/" + topic)) {
				Tuple2<Option<byte[]>, Stat> tuple = zkc.getDataAndStat(BROKER_TOPICS_PATH + "/" + topic);
				String tupleString = new String(tuple._1.get());
				JSONObject partitionObject = JSON.parseObject(tupleString).getJSONObject("partitions");
				Set<Integer> partitions = new HashSet<>();
				for (String partition : partitionObject.keySet()) {
					try {
						partitions.add(Integer.valueOf(partition));
					} catch (Exception e) {
						LOG.error("Convert partition string to integer has error, msg is " + e.getCause().getMessage());
						e.printStackTrace();
					}
				}
				if ("kafka".equals(SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage"))) {
					logSize = kafkaService.getKafkaRealLogSize(clusterAlias, topic, partitions);
				} else {
					logSize = kafkaService.getLogSize(clusterAlias, topic, partitions);
				}
			}
		} catch (Exception e) {
			LOG.error("Get topic logsize total has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return logSize;
	}

	/** Get topic real logsize records. */
	public long getTopicRealLogSize(String clusterAlias, String topic) {
		long logSize = 0L;
		if (Kafka.CONSUMER_OFFSET_TOPIC.equals(topic)) {
			return logSize;
		}
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		try {
			if (zkc.pathExists(BROKER_TOPICS_PATH + "/" + topic)) {
				Tuple2<Option<byte[]>, Stat> tuple = zkc.getDataAndStat(BROKER_TOPICS_PATH + "/" + topic);
				String tupleString = new String(tuple._1.get());
				JSONObject partitionObject = JSON.parseObject(tupleString).getJSONObject("partitions");
				Set<Integer> partitions = new HashSet<>();
				for (String partition : partitionObject.keySet()) {
					try {
						partitions.add(Integer.valueOf(partition));
					} catch (Exception e) {
						LOG.error("Convert partition string to integer has error, msg is " + e.getCause().getMessage());
						e.printStackTrace();
					}
				}
				if ("kafka".equals(SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage"))) {
					logSize = kafkaService.getKafkaRealLogSize(clusterAlias, topic, partitions);
				} else {
					logSize = kafkaService.getLogSize(clusterAlias, topic, partitions);
				}
			}
		} catch (Exception e) {
			LOG.error("Get topic real logsize has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return logSize;
	}

	/** Get topic producer send logsize records. */
	public long getTopicProducerLogSize(String clusterAlias, String topic) {
		long logSize = 0L;
		if (Kafka.CONSUMER_OFFSET_TOPIC.equals(topic)) {
			return logSize;
		}
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		try {
			if (zkc.pathExists(BROKER_TOPICS_PATH + "/" + topic)) {
				Tuple2<Option<byte[]>, Stat> tuple = zkc.getDataAndStat(BROKER_TOPICS_PATH + "/" + topic);
				String tupleString = new String(tuple._1.get());
				JSONObject partitionObject = JSON.parseObject(tupleString).getJSONObject("partitions");
				Set<Integer> partitions = new HashSet<>();
				for (String partition : partitionObject.keySet()) {
					try {
						partitions.add(Integer.valueOf(partition));
					} catch (Exception e) {
						LOG.error("Convert partition string to integer has error, msg is " + e.getCause().getMessage());
						e.printStackTrace();
					}
				}
				if ("kafka".equals(SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage"))) {
					logSize = kafkaService.getKafkaProducerLogSize(clusterAlias, topic, partitions);
				} else {
					logSize = kafkaService.getLogSize(clusterAlias, topic, partitions);
				}
			}
		} catch (Exception e) {
			LOG.error("Get topic real logsize has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return logSize;
	}

	/** Add topic partitions. */
	public Map<String, Object> createTopicPartitions(String clusterAlias, String topic, int totalCount) {
		Map<String, Object> targets = new HashMap<String, Object>();
		int existPartitions = (int) partitionNumbers(clusterAlias, topic);
		Properties prop = new Properties();
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaService.getKafkaBrokerServer(clusterAlias));

		if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
			kafkaService.sasl(prop, clusterAlias);
		}

		AdminClient adminClient = null;
		try {
			adminClient = AdminClient.create(prop);
			Map<String, NewPartitions> newPartitions = new HashMap<String, NewPartitions>();
			newPartitions.put(topic, NewPartitions.increaseTo(existPartitions + totalCount));
			adminClient.createPartitions(newPartitions);
			targets.put("status", "success");
			targets.put("info", "Add topic[" + topic + "], before partition[" + existPartitions + "], after partition[" + (existPartitions + totalCount) + "] has successed.");
		} catch (Exception e) {
			LOG.info("Add kafka topic partitions has error, msg is " + e.getMessage());
			e.printStackTrace();
			targets.put("status", "failed");
			targets.put("info", "Add kafka topic partitions has error, msg is " + e.getMessage());
		} finally {
			adminClient.close();
		}
		return targets;
	}

}
