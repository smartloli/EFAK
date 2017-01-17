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
package org.smartloli.kafka.eagle.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

import org.smartloli.kafka.eagle.domain.OffsetDomain;
import org.smartloli.kafka.eagle.domain.OffsetZkDomain;
import org.smartloli.kafka.eagle.ipc.RpcClient;
import org.smartloli.kafka.eagle.service.OffsetService;
import org.smartloli.kafka.eagle.util.CalendarUtils;
import org.smartloli.kafka.eagle.util.ZKDataUtils;
import org.springframework.stereotype.Service;
import org.smartloli.kafka.eagle.util.KafkaClusterUtils;

/**
 * Offsets consumer data.
 *
 * @author smartloli.
 *
 *         Created by Aug 16, 2016
 */
@Service
public class OffsetServiceImpl implements OffsetService {

	/** Get Kafka brokers. */
	private List<String> getBrokers(String topic, String group) {
		JSONArray arr = JSON.parseArray(KafkaClusterUtils.getAllBrokersInfo());
		List<String> list = new ArrayList<String>();
		for (Object object : arr) {
			JSONObject obj = (JSONObject) object;
			String host = obj.getString("host");
			int port = obj.getInteger("port");
			list.add(host + ":" + port);
		}
		return list;
	}

	/** Get Kafka logsize from Kafka topic. */
	private String getKafkaLogSize(String topic, String group) {
		List<String> hosts = getBrokers(topic, group);
		List<String> partitions = KafkaClusterUtils.findTopicPartition(topic);
		List<OffsetDomain> list = new ArrayList<OffsetDomain>();
		for (String partition : partitions) {
			int partitionInt = Integer.parseInt(partition);
			OffsetZkDomain offsetZk = getKafkaOffset(topic, group, partitionInt);
			OffsetDomain offset = new OffsetDomain();
			long logSize = KafkaClusterUtils.getLogSize(hosts, topic, partitionInt);
			offset.setPartition(partitionInt);
			offset.setLogSize(logSize);
			offset.setCreate(offsetZk.getCreate());
			offset.setModify(offsetZk.getModify());
			offset.setOffset(offsetZk.getOffset());
			offset.setLag(offsetZk.getOffset() == -1 ? 0 : logSize - offsetZk.getOffset());
			offset.setOwner(offsetZk.getOwners());
			list.add(offset);
		}
		return list.toString();
	}

	/** Get Kafka offset from Kafka topic. */
	private OffsetZkDomain getKafkaOffset(String topic, String group, int partition) {
		JSONArray array = JSON.parseArray(RpcClient.getOffset());
		OffsetZkDomain offsetZk = new OffsetZkDomain();
		for (Object obj : array) {
			JSONObject object = (JSONObject) obj;
			String _topic = object.getString("topic");
			String _group = object.getString("group");
			int _partition = object.getInteger("partition");
			long timestamp = object.getLong("timestamp");
			long offset = object.getLong("offset");
			String owner = object.getString("owner");
			if (topic.equals(_topic) && group.equals(_group) && partition == _partition) {
				offsetZk.setOffset(offset);
				offsetZk.setOwners(owner);
				offsetZk.setCreate(CalendarUtils.convertUnixTime2Date(timestamp));
				offsetZk.setModify(CalendarUtils.convertUnixTime2Date(timestamp));
			}
		}
		return offsetZk;
	}

	/** Get logsize from zookeeper. */
	private String getLogSize(String topic, String group) {
		List<String> hosts = getBrokers(topic, group);
		List<String> partitions = KafkaClusterUtils.findTopicPartition(topic);
		List<OffsetDomain> list = new ArrayList<OffsetDomain>();
		for (String partition : partitions) {
			int partitionInt = Integer.parseInt(partition);
			OffsetZkDomain offsetZk = KafkaClusterUtils.getOffset(topic, group, partitionInt);
			OffsetDomain offset = new OffsetDomain();
			long logSize = KafkaClusterUtils.getLogSize(hosts, topic, partitionInt);
			offset.setPartition(partitionInt);
			offset.setLogSize(logSize);
			offset.setCreate(offsetZk.getCreate());
			offset.setModify(offsetZk.getModify());
			offset.setOffset(offsetZk.getOffset());
			offset.setLag(offsetZk.getOffset() == -1 ? 0 : logSize - offsetZk.getOffset());
			offset.setOwner(offsetZk.getOwners());
			list.add(offset);
		}
		return list.toString();
	}

	/** Get logsize from Kafka topic or Zookeeper. */
	public String getLogSize(String formatter, String topic, String group) {
		if ("kafka".equals(formatter)) {
			return getKafkaLogSize(topic, group);
		} else {
			return getLogSize(topic, group);
		}
	}

	/** Get Kafka offset graph data from Zookeeper. */
	public String getOffsetsGraph(String group, String topic) {
		String ret = ZKDataUtils.getOffsets(group, topic);
		if (ret.length() > 0) {
			ret = JSON.parseObject(ret).getString("data");
		}
		return ret;
	}

	/** Judge group & topic from Zookeeper has exist. */
	private boolean hasGroupTopic(String group, String topic) {
		return KafkaClusterUtils.findTopicIsConsumer(topic, group);
	}

	/** Judge group & topic exist Kafka topic or Zookeeper. */
	public boolean hasGroupTopic(String formatter, String group, String topic) {
		if ("kafka".equals(formatter)) {
			return hasKafkaGroupTopic(group, topic);
		} else {
			return hasGroupTopic(group, topic);
		}
	}

	/** Judge group & topic from Kafka topic has exist. */
	private boolean hasKafkaGroupTopic(String group, String topic) {
		boolean status = false;
		Map<String, List<String>> type = new HashMap<String, List<String>>();
		Gson gson = new Gson();
		Map<String, List<String>> map = gson.fromJson(RpcClient.getConsumer(), type.getClass());
		if (map.containsKey(group)) {
			for (String _topic : map.get(group)) {
				if (_topic.equals(topic)) {
					status = true;
					break;
				}
			}
		}
		return status;
	}

}
