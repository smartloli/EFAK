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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.protocol.PartitionsInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KafkaZKPoolUtils;

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
	private final String CONSUMERS_PATH = "/consumers";
	private final String OWNERS = "/owners";
	private final String TOPIC_ISR = "/brokers/topics/%s/partitions/%s/state";
	private final Logger LOG = LoggerFactory.getLogger(BrokerServiceImpl.class);

	/** Instance Kafka Zookeeper client pool. */
	private KafkaZKPoolUtils kafkaZKPool = KafkaZKPoolUtils.getInstance();

	@Override
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

	@Override
	public List<PartitionsInfo> topicRecords(String clusterAlias, Map<String, Object> params) {
		KafkaZkClient zkc = kafkaZKPool.getZkClient(clusterAlias);
		List<PartitionsInfo> targets = new ArrayList<PartitionsInfo>();
		if (params.containsKey("search") && params.get("search").toString().length() > 0) {
			if (zkc.pathExists(BROKER_TOPICS_PATH)) {
				Seq<String> subBrokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH);
				List<String> topics = JavaConversions.seqAsJavaList(subBrokerTopicsPaths);
				String search = params.get("search").toString();
				for (String topic : topics) {
					if (search.equals(topic)) {
						try {
							Tuple2<Option<byte[]>, Stat> tuple = zkc.getDataAndStat(BROKER_TOPICS_PATH + "/" + topic);
							PartitionsInfo partition = new PartitionsInfo();
							partition.setId(1);
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
				}
			}
		} else {
			if (zkc.pathExists(BROKER_TOPICS_PATH)) {
				Seq<String> subBrokerTopicsPaths = zkc.getChildren(BROKER_TOPICS_PATH);
				List<String> topics = JavaConversions.seqAsJavaList(subBrokerTopicsPaths);
				int start = Integer.parseInt(params.get("start").toString());
				int length = Integer.parseInt(params.get("length").toString());
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

}
