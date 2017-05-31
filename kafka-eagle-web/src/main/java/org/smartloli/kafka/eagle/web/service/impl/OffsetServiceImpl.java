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
package org.smartloli.kafka.eagle.web.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.smartloli.kafka.eagle.common.domain.OffsetDomain;
import org.smartloli.kafka.eagle.common.domain.OffsetZkDomain;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.ZkFactory;
import org.smartloli.kafka.eagle.core.factory.ZkService;
import org.smartloli.kafka.eagle.web.service.OffsetService;
import org.springframework.stereotype.Service;

/**
 * Offsets consumer data.
 *
 * @author smartloli.
 *
 *         Created by Aug 16, 2016.
 * 
 *         Update by hexiang 20170216
 */
@Service
public class OffsetServiceImpl implements OffsetService {

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** Zookeeper service interface. */
	private ZkService zkService = new ZkFactory().create();

	/** Get Kafka brokers. */
	private List<String> getBrokers(String clusterAlias, String topic, String group) {
		JSONArray brokers = JSON.parseArray(kafkaService.getAllBrokersInfo(clusterAlias));
		List<String> targets = new ArrayList<String>();
		for (Object object : brokers) {
			JSONObject target = (JSONObject) object;
			String host = target.getString("host");
			int port = target.getInteger("port");
			targets.add(host + ":" + port);
		}
		return targets;
	}

	/** Get Kafka logsize from Kafka topic. */
	private String getKafkaLogSize(String clusterAlias, String topic, String group) {
		List<String> hosts = getBrokers(clusterAlias, topic, group);
		List<String> partitions = kafkaService.findTopicPartition(clusterAlias, topic);
		List<OffsetDomain> targets = new ArrayList<OffsetDomain>();
		for (String partition : partitions) {
			int partitionInt = Integer.parseInt(partition);
			OffsetZkDomain offsetZk = getKafkaOffset(clusterAlias, topic, group, partitionInt);
			OffsetDomain offset = new OffsetDomain();
			long logSize = kafkaService.getLogSize(hosts, topic, partitionInt);
			offset.setPartition(partitionInt);
			offset.setLogSize(logSize);
			offset.setCreate(offsetZk.getCreate());
			offset.setModify(offsetZk.getModify());
			offset.setOffset(offsetZk.getOffset());
			offset.setLag(offsetZk.getOffset() == -1 ? 0 : logSize - offsetZk.getOffset());
			offset.setOwner(offsetZk.getOwners());
			targets.add(offset);
		}
		return targets.toString();
	}

	/** Get Kafka offset from Kafka topic. */
	private OffsetZkDomain getKafkaOffset(String clusterAlias, String topic, String group, int partition) {
		JSONArray kafkaOffsets = JSON.parseArray(kafkaService.getKafkaOffset(clusterAlias));
		OffsetZkDomain targetOffset = new OffsetZkDomain();
		for (Object kafkaOffset : kafkaOffsets) {
			JSONObject object = (JSONObject) kafkaOffset;
			String _topic = object.getString("topic");
			String _group = object.getString("group");
			int _partition = object.getInteger("partition");
			long timestamp = object.getLong("timestamp");
			long offset = object.getLong("offset");
			if (topic.equals(_topic) && group.equals(_group) && partition == _partition) {
				targetOffset.setOffset(offset);
				targetOffset.setCreate(CalendarUtils.convertUnixTime2Date(timestamp));
				targetOffset.setModify(CalendarUtils.convertUnixTime2Date(timestamp));
				JSONArray consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumerGroupTopic(clusterAlias, group));
				for (Object consumerObject : consumerGroups) {
					JSONObject consumerGroup = (JSONObject) consumerObject;
					for (Object topicSubObject : consumerGroup.getJSONArray("topicSub")) {
						JSONObject topicSub = (JSONObject) topicSubObject;
						if (topic.equals(topicSub.getString("topic")) && partition == topicSub.getInteger("partition")) {
							targetOffset.setOwners(consumerGroup.getString("node") + "-" + consumerGroup.getString("owner"));
						}
					}
				}
			}
		}
		return targetOffset;
	}

	/** Get logsize from zookeeper. */
	private String getLogSize(String clusterAlias, String topic, String group) {
		List<String> hosts = getBrokers(clusterAlias, topic, group);
		List<String> partitions = kafkaService.findTopicPartition(clusterAlias, topic);
		List<OffsetDomain> targets = new ArrayList<OffsetDomain>();
		for (String partition : partitions) {
			int partitionInt = Integer.parseInt(partition);
			OffsetZkDomain offsetZk = kafkaService.getOffset(clusterAlias, topic, group, partitionInt);
			OffsetDomain offset = new OffsetDomain();
			long logSize = kafkaService.getLogSize(hosts, topic, partitionInt);
			offset.setPartition(partitionInt);
			offset.setLogSize(logSize);
			offset.setCreate(offsetZk.getCreate());
			offset.setModify(offsetZk.getModify());
			offset.setOffset(offsetZk.getOffset());
			offset.setLag(offsetZk.getOffset() == -1 ? 0 : logSize - offsetZk.getOffset());
			offset.setOwner(offsetZk.getOwners());
			targets.add(offset);
		}
		return targets.toString();
	}

	/** Get logsize from Kafka topic or Zookeeper. */
	public String getLogSize(String clusterAlias, String formatter, String topic, String group) {
		if ("kafka".equals(formatter)) {
			return getKafkaLogSize(clusterAlias, topic, group);
		} else {
			return getLogSize(clusterAlias, topic, group);
		}
	}

	/** Get Kafka offset graph data from Zookeeper. */
	public String getOffsetsGraph(String clusterAlias, String group, String topic) {
		String target = zkService.getOffsets(clusterAlias, group, topic);
		if (target.length() > 0) {
			target = JSON.parseObject(target).getString("data");
		}
		return target;
	}

	/** Judge group & topic from Zookeeper has exist. */
	private boolean hasGroupTopic(String clusterAlias, String group, String topic) {
		return kafkaService.findTopicAndGroupExist(clusterAlias, topic, group);
	}

	/** Judge group & topic exist Kafka topic or Zookeeper. */
	public boolean hasGroupTopic(String clusterAlias, String formatter, String group, String topic) {
		if ("kafka".equals(formatter)) {
			return hasKafkaGroupTopic(clusterAlias, group, topic);
		} else {
			return hasGroupTopic(clusterAlias, group, topic);
		}
	}

	/** Judge group & topic from Kafka topic has exist. */
	private boolean hasKafkaGroupTopic(String clusterAlias, String group, String topic) {
		boolean status = false;
		Set<String> topics = kafkaService.getKafkaConsumerTopic(clusterAlias, group);
		if (topics.contains(topic)) {
			status = true;
		}
		return status;
	}

}
