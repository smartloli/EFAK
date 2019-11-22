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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.protocol.MetadataInfo;
import org.smartloli.kafka.eagle.common.protocol.PartitionsInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.Kafka;
import org.smartloli.kafka.eagle.common.util.KafkaZKPoolUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
		long count = 0L;
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		try {
			if (zkc.pathExists(BROKER_TOPICS_PATH)) {
				Seq<String> subBrokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH);
				List<String> topics = JavaConversions.seqAsJavaList(subBrokerTopicsPaths);
				excludeTopic(topics);
				count = topics.size();
			}
		} catch (Exception e) {
			LOG.error("Get topic numbers has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return count;
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
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		try {
			if (zkc.pathExists(BROKER_TOPICS_PATH)) {
				Seq<String> subBrokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH);
				List<String> topics = JavaConversions.seqAsJavaList(subBrokerTopicsPaths);
				excludeTopic(topics);
				for (String name : topics) {
					if (topic != null && name.contains(topic)) {
						count++;
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Get search topic list numbers has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
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
		try {
			int start = Integer.parseInt(params.get("start").toString());
			int length = Integer.parseInt(params.get("length").toString());
			if (params.containsKey("search") && params.get("search").toString().length() > 0) {
				if (zkc.pathExists(BROKER_TOPICS_PATH)) {
					Seq<String> subBrokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH);
					List<String> topics = JavaConversions.seqAsJavaList(subBrokerTopicsPaths);
					excludeTopic(topics);
					String search = params.get("search").toString();
					int offset = 0;
					int id = 1;
					for (String topic : topics) {
						if (search != null && topic.contains(search)) {
							if (offset < (start + length) && offset >= start) {
								try {
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
								} catch (Exception ex) {
									ex.printStackTrace();
									LOG.error("Scan topic search from zookeeper has error, msg is " + ex.getMessage());
								}
							}
							offset++;
						}
					}
				}
			} else {
				if (zkc.pathExists(BROKER_TOPICS_PATH)) {
					Seq<String> subBrokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH);
					List<String> topics = JavaConversions.seqAsJavaList(subBrokerTopicsPaths);
					excludeTopic(topics);
					int offset = 0;
					int id = 1;
					for (String topic : topics) {
						if (offset < (start + length) && offset >= start) {
							try {
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
							} catch (Exception ex) {
								ex.printStackTrace();
								LOG.error("Scan topic page from zookeeper has error, msg is " + ex.getMessage());
							}
						}
						offset++;
					}
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

	@Override
	public String partitionRecords(String clusterAlias, String topic, Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String consumerTPNumbers(String clusterAlias, String group, String topic) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String consumerTPRecords(String clusterAlias, String group, String topic, Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	/** Check topic from zookeeper metadata. */
	public boolean findKafkaTopic(String clusterAlias, String topic) {
		boolean status = false;
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		try {
			status = zkc.pathExists(BROKER_TOPICS_PATH + "/" + topic);
		} catch (Exception e) {
			LOG.error("Find kafka topic has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return status;
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
		return topics;
	}

	/** Get select topic list from zookeeper. */
	public String topicListParams(String clusterAlias, String search) {
		JSONArray targets = new JSONArray();
		int limit = 15;

		List<String> topics = new ArrayList<>();
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		try {
			if (zkc.pathExists(BROKER_TOPICS_PATH)) {
				Seq<String> subBrokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH);
				topics = JavaConversions.seqAsJavaList(subBrokerTopicsPaths);
				excludeTopic(topics);
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
			}
		} catch (Exception e) {
			LOG.error("Get topic list has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		}
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return targets.toJSONString();
	}

	/** Scan topic meta page display from zookeeper. */
	public List<MetadataInfo> topicMetadataRecords(String clusterAlias, String topic, Map<String, Object> params) {
		List<MetadataInfo> targets = new ArrayList<>();
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		try {
			if (zkc.pathExists(BROKER_TOPICS_PATH)) {
				Seq<String> subBrokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH);
				List<String> topics = JavaConversions.seqAsJavaList(subBrokerTopicsPaths);
				excludeTopic(topics);
				if (topics.contains(topic)) {
					int start = Integer.parseInt(params.get("start").toString());
					int length = Integer.parseInt(params.get("length").toString());
					int offset = 0;
					Tuple2<Option<byte[]>, Stat> tuple = zkc.getDataAndStat(BROKER_TOPICS_PATH + "/" + topic);
					String tupleString = new String(tuple._1.get());
					JSONObject partitionObject = JSON.parseObject(tupleString).getJSONObject("partitions");
					for (String partition : partitionObject.keySet()) {
						if (offset < (start + length) && offset >= start) {
							String path = String.format(TOPIC_ISR, topic, Integer.valueOf(partition));
							Tuple2<Option<byte[]>, Stat> tuple2 = zkc.getDataAndStat(path);
							String tupleString2 = new String(tuple2._1.get());
							JSONObject topicMetadata = JSON.parseObject(tupleString2);
							MetadataInfo metadate = new MetadataInfo();
							metadate.setIsr(topicMetadata.getString("isr"));
							metadate.setLeader(topicMetadata.getInteger("leader"));
							metadate.setPartitionId(Integer.valueOf(partition));
							metadate.setReplicas(kafkaService.getReplicasIsr(clusterAlias, topic, Integer.valueOf(partition)));
							long logSize = 0L;
							if ("kafka".equals(SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage"))) {
								logSize = kafkaService.getKafkaRealLogSize(clusterAlias, topic, Integer.valueOf(partition));
							} else {
								logSize = kafkaService.getRealLogSize(clusterAlias, topic, Integer.valueOf(partition));
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

}
