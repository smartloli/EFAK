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
import java.util.Map;
import java.util.Set;

import org.smartloli.kafka.eagle.common.protocol.OffsetInfo;
import org.smartloli.kafka.eagle.common.protocol.OffsetZkInfo;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicOffsetsInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.web.dao.MBeanDao;
import org.smartloli.kafka.eagle.web.service.OffsetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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

	@Autowired
	private MBeanDao mbeanDao;

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** Get Kafka logsize from Kafka topic. */
	private String getKafkaLogSize(String clusterAlias, String topic, String group) {
		List<String> partitions = kafkaService.findTopicPartition(clusterAlias, topic);
		List<OffsetInfo> targets = new ArrayList<OffsetInfo>();
		for (String partition : partitions) {
			int partitionInt = Integer.parseInt(partition);
			OffsetZkInfo offsetZk = getKafkaOffset(clusterAlias, topic, group, partitionInt);
			OffsetInfo offset = new OffsetInfo();
			long logSize = 0L;
			logSize = kafkaService.getKafkaLogSize(clusterAlias, topic, partitionInt);
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
	private OffsetZkInfo getKafkaOffset(String clusterAlias, String topic, String group, int partition) {
		JSONArray kafkaOffsets = JSON.parseArray(kafkaService.getKafkaOffset(clusterAlias));
		OffsetZkInfo targetOffset = new OffsetZkInfo();
		if (kafkaOffsets == null) {
			return targetOffset;
		}
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
		List<String> partitions = kafkaService.findTopicPartition(clusterAlias, topic);
		List<OffsetInfo> targets = new ArrayList<OffsetInfo>();
		for (String partition : partitions) {
			int partitionInt = Integer.parseInt(partition);
			OffsetZkInfo offsetZk = kafkaService.getOffset(clusterAlias, topic, group, partitionInt);
			OffsetInfo offset = new OffsetInfo();
			long logSize = 0L;
			if ("kafka".equals(SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage"))) {
				logSize = kafkaService.getKafkaLogSize(clusterAlias, topic, partitionInt);
			} else {
				logSize = kafkaService.getLogSize(clusterAlias, topic, partitionInt);
			}
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

	/** Get kafka offset graph data. */
	public String getOffsetsGraph(Map<String, Object> params) {
		List<String> x = new ArrayList<>();
		List<String> y = new ArrayList<>();
		List<TopicOffsetsInfo> topicOffsets = mbeanDao.getConsumerLagTopic(params);
		if (topicOffsets.size() > 0) {
			for (TopicOffsetsInfo topicOffset : topicOffsets) {
				x.add(CalendarUtils.convertUnixTime(topicOffset.getTimespan(), "yyyy-MM-dd HH:mm"));
				y.add(topicOffset.getLag());
			}
		}
		JSONObject value = new JSONObject();
		value.put("x", x);
		value.put("y", y);
		return value.toJSONString();
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

	/** Get topic consumer & producer rate by bytes per sec. */
	public String getOffsetRate(Map<String, Object> params) {
		// sort by desc.
		List<TopicOffsetsInfo> topicOffsets = mbeanDao.getConsumerRateTopic(params);
		String ins = "0";
		String outs = "0";
		if (topicOffsets.size() == 1) {
			ins = topicOffsets.get(0).getLogsize();
			outs = topicOffsets.get(0).getOffsets();
		} else if (topicOffsets.size() == 2) {
			try {
				ins = String.valueOf(Math.abs(Long.parseLong(topicOffsets.get(0).getLogsize()) - Long.parseLong(topicOffsets.get(1).getLogsize())));
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				outs = String.valueOf(Math.abs(Long.parseLong(topicOffsets.get(0).getOffsets()) - Long.parseLong(topicOffsets.get(1).getOffsets())));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		JSONObject target = new JSONObject();
		target.put("ins", ins);
		target.put("outs", outs);

		return target.toJSONString();
	}

}
