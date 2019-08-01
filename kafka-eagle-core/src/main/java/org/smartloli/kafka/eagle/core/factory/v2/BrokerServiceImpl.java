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
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.protocol.MetadataInfo;
import org.smartloli.kafka.eagle.common.protocol.PartitionsInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KafkaZKPoolUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

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
		if (zkc.pathExists(BROKER_TOPICS_PATH)) {
			Seq<String> subBrokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH);
			count = JavaConversions.seqAsJavaList(subBrokerTopicsPaths).size();
		}
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return count;
	}

	@Override
	public long topicNumbers(String clusterAlias, String topic) {
		long count = 0L;
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		if (zkc.pathExists(BROKER_TOPICS_PATH)) {
			Seq<String> subBrokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH);
			List<String> topics = JavaConversions.seqAsJavaList(subBrokerTopicsPaths);
			for (String name : topics) {
				if (topic != null && name.contains(topic)) {
					count++;
				}
			}
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
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		if (zkc.pathExists(BROKER_TOPICS_PATH + "/" + topic + "/partitions")) {
			Seq<String> subBrokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH + "/" + topic + "/partitions");
			count = JavaConversions.seqAsJavaList(subBrokerTopicsPaths).size();
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
		int start = Integer.parseInt(params.get("start").toString());
		int length = Integer.parseInt(params.get("length").toString());
		if (params.containsKey("search") && params.get("search").toString().length() > 0) {
			if (zkc.pathExists(BROKER_TOPICS_PATH)) {
				Seq<String> subBrokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH);
				List<String> topics = JavaConversions.seqAsJavaList(subBrokerTopicsPaths);
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
		status = zkc.pathExists(BROKER_TOPICS_PATH + "/" + topic);
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
		if (zkc.pathExists(BROKER_IDS_PATH)) {
			Seq<String> subBrokerIdsPaths = zkc.getChildren(BROKER_IDS_PATH);
			count = JavaConversions.seqAsJavaList(subBrokerIdsPaths).size();
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
		if (zkc.pathExists(BROKER_TOPICS_PATH)) {
			Seq<String> subBrokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH);
			topics = JavaConversions.seqAsJavaList(subBrokerTopicsPaths);
		}
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return topics;
	}

	/** Scan topic meta page display from zookeeper. */
	public List<MetadataInfo> topicMetadataRecords(String clusterAlias, String topic, Map<String, Object> params) {
		List<MetadataInfo> targets = new ArrayList<>();
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		if (zkc.pathExists(BROKER_TOPICS_PATH)) {
			Seq<String> subBrokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH);
			List<String> topics = JavaConversions.seqAsJavaList(subBrokerTopicsPaths);
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
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return targets;
	}

	/** Get topic producer logsize total. */
	public long getTopicLogSizeTotal(String clusterAlias, String topic) {
		long logSize = 0L;
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		if (zkc.pathExists(BROKER_TOPICS_PATH)) {
			Seq<String> subBrokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH);
			List<String> topics = JavaConversions.seqAsJavaList(subBrokerTopicsPaths);
			if (topics.contains(topic)) {
				Tuple2<Option<byte[]>, Stat> tuple = zkc.getDataAndStat(BROKER_TOPICS_PATH + "/" + topic);
				String tupleString = new String(tuple._1.get());
				JSONObject partitionObject = JSON.parseObject(tupleString).getJSONObject("partitions");
				for (String partition : partitionObject.keySet()) {
					if ("kafka".equals(SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage"))) {
						logSize += kafkaService.getKafkaLogSize(clusterAlias, topic, Integer.valueOf(partition));
					} else {
						logSize += kafkaService.getLogSize(clusterAlias, topic, Integer.valueOf(partition));
					}
				}
			}
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
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		if (zkc.pathExists(BROKER_TOPICS_PATH)) {
			Seq<String> subBrokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH);
			List<String> topics = JavaConversions.seqAsJavaList(subBrokerTopicsPaths);
			if (topics.contains(topic)) {
				Tuple2<Option<byte[]>, Stat> tuple = zkc.getDataAndStat(BROKER_TOPICS_PATH + "/" + topic);
				String tupleString = new String(tuple._1.get());
				JSONObject partitionObject = JSON.parseObject(tupleString).getJSONObject("partitions");
				for (String partition : partitionObject.keySet()) {
					if ("kafka".equals(SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage"))) {
						logSize += kafkaService.getKafkaRealLogSize(clusterAlias, topic, Integer.valueOf(partition));
					} else {
						logSize += kafkaService.getLogSize(clusterAlias, topic, Integer.valueOf(partition));
					}
				}
			}
		}
		if (zkc != null) {
			kafkaZKPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return logSize;
	}

}
