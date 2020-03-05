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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmClusterInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmConfigInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmConsumerInfo;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicLogSize;
import org.smartloli.kafka.eagle.common.util.KConstants.AlarmType;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.web.dao.AlertDao;
import org.smartloli.kafka.eagle.web.dao.TopicDao;
import org.smartloli.kafka.eagle.web.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Alarm implements service to get configure info.
 *
 * @Author smartloli.
 *
 *         Created by Oct 27, 2018.
 * 
 */
@Service
public class AlertServiceImpl implements AlertService {

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	@Autowired
	private AlertDao alertDao;

	@Autowired
	private TopicDao topicDao;

	@Override
	public int insertAlarmConsumer(AlarmConsumerInfo alarmConsumer) {
		return alertDao.insertAlarmConsumer(alarmConsumer);
	}

	/** Get consumer group to alert. */
	public String getAlarmConsumerGroup(String clusterAlias, String formatter, String search) {
		if ("kafka".equals(formatter)) {
			return getAlarmConsumerGroupKafka(clusterAlias, search);
		} else {
			return getAlarmConsumerGroup(clusterAlias, search);
		}
	}

	/** Get consumer topic to alert. */
	public String getAlarmConsumerTopic(String clusterAlias, String formatter, String group, String search) {
		if ("kafka".equals(formatter)) {
			return getAlarmConsumerTopicKafka(clusterAlias, group, search);
		} else {
			return getAlarmConsumerTopic(clusterAlias, group, search);
		}
	}

	private String getAlarmConsumerGroup(String clusterAlias, String search) {
		Map<String, List<String>> consumers = kafkaService.getConsumers(clusterAlias);
		JSONArray groups = new JSONArray();
		int offset = 0;
		if (search.length() > 0) {
			for (Entry<String, List<String>> entry : consumers.entrySet()) {
				if (entry.getKey().contains(search)) {
					JSONObject object = new JSONObject();
					object.put("text", entry.getKey());
					object.put("id", offset);
					groups.add(object);
					offset++;
				}
			}
		} else {
			for (Entry<String, List<String>> entry : consumers.entrySet()) {
				JSONObject object = new JSONObject();
				object.put("text", entry.getKey());
				object.put("id", offset);
				groups.add(object);
				offset++;
			}
		}
		return groups.toJSONString();
	}

	private String getAlarmConsumerGroupKafka(String clusterAlias, String search) {
		int offset = 0;
		JSONArray groups = new JSONArray();
		JSONArray consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumer(clusterAlias));
		if (search.length() > 0) {
			for (Object object : consumerGroups) {
				JSONObject consumerGroup = (JSONObject) object;
				if (consumerGroup.getString("group").contains(search)) {
					JSONObject group = new JSONObject();
					group.put("text", consumerGroup.getString("group"));
					group.put("id", offset);
					groups.add(group);
					offset++;
				}
			}
		} else {
			for (Object object : consumerGroups) {
				JSONObject consumerGroup = (JSONObject) object;
				JSONObject group = new JSONObject();
				group.put("text", consumerGroup.getString("group"));
				group.put("id", offset);
				groups.add(group);
				offset++;
			}
		}
		return groups.toJSONString();
	}

	private String getAlarmConsumerTopic(String clusterAlias, String group, String search) {
		Map<String, List<String>> consumers = kafkaService.getConsumers(clusterAlias);
		JSONArray topics = new JSONArray();
		int offset = 0;
		if (search.length() > 0) {
			for (Entry<String, List<String>> entry : consumers.entrySet()) {
				if (entry.getKey().contains(search) && entry.getKey().equals(group)) {
					JSONObject object = new JSONObject();
					object.put("text", entry.getValue());
					object.put("id", offset);
					topics.add(object);
					offset++;
				}
			}
		} else {
			for (Entry<String, List<String>> entry : consumers.entrySet()) {
				JSONObject object = new JSONObject();
				object.put("text", entry.getValue());
				object.put("id", offset);
				topics.add(object);
				offset++;
			}
		}
		return topics.toJSONString();
	}

	private String getAlarmConsumerTopicKafka(String clusterAlias, String group, String search) {
		int offset = 0;
		JSONArray topics = new JSONArray();
		Set<String> topicSets = kafkaService.getKafkaConsumerTopic(clusterAlias, group);
		if (search.length() > 0) {
			for (String topic : topicSets) {
				if (topic.contains(search)) {
					JSONObject object = new JSONObject();
					object.put("text", topic);
					object.put("id", offset);
					topics.add(object);
					offset++;
				}
			}
		} else {
			for (String topic : topicSets) {
				JSONObject object = new JSONObject();
				object.put("text", topic);
				object.put("id", offset);
				topics.add(object);
				offset++;
			}
		}
		return topics.toJSONString();
	}

	@Override
	public List<AlarmConsumerInfo> getAlarmConsumerAppList(Map<String, Object> params) {
		return alertDao.getAlarmConsumerAppList(params);
	}

	@Override
	public int alertConsumerAppCount(Map<String, Object> params) {
		return alertDao.alertConsumerAppCount(params);
	}

	@Override
	public int deleteAlarmConsumerById(int id) {
		return alertDao.deleteAlarmConsumerById(id);
	}

	@Override
	public int modifyAlarmConsumerById(AlarmConsumerInfo alarmConsumer) {
		return alertDao.modifyAlarmConsumerById(alarmConsumer);
	}

	@Override
	public int create(AlarmClusterInfo clusterInfo) {
		return alertDao.insertAlarmCluster(clusterInfo);
	}

	@Override
	public List<AlarmClusterInfo> getAlarmClusterList(Map<String, Object> params) {
		return alertDao.getAlarmClusterList(params);
	}

	@Override
	public int getAlarmClusterCount(Map<String, Object> params) {
		return alertDao.getAlarmClusterCount(params);
	}

	@Override
	public int deleteAlarmClusterAlertById(int id) {
		return alertDao.deleteAlarmClusterAlertById(id);
	}

	@Override
	public AlarmClusterInfo findAlarmClusterAlertById(int id) {
		return alertDao.findAlarmClusterAlertById(id);
	}

	@Override
	public int modifyClusterAlertById(AlarmClusterInfo cluster) {
		return alertDao.modifyClusterAlertById(cluster);
	}

	@Override
	public List<AlarmClusterInfo> getAllAlarmClusterTasks() {
		return alertDao.getAllAlarmClusterTasks();
	}

	/** Get alert type list. */
	public String getAlertTypeList() {
		int offset = 0;
		JSONArray typeList = new JSONArray();
		for (String type : AlarmType.TYPE) {
			JSONObject object = new JSONObject();
			object.put("text", type);
			object.put("id", offset);
			typeList.add(object);
			offset++;
		}
		return typeList.toJSONString();
	}

	@Override
	public int insertOrUpdateAlarmConfig(AlarmConfigInfo alarmConfig) {
		return alertDao.insertOrUpdateAlarmConfig(alarmConfig);
	}

	@Override
	public boolean findAlarmConfigByGroupName(Map<String, Object> params) {
		return alertDao.findAlarmConfigByGroupName(params) > 0 ? true : false;
	}

	@Override
	public List<AlarmConfigInfo> getAlarmConfigList(Map<String, Object> params) {
		return alertDao.getAlarmConfigList(params);
	}

	@Override
	public int alarmConfigCount(Map<String, Object> params) {
		return alertDao.alarmConfigCount(params);
	}

	@Override
	public int deleteAlertByGroupName(Map<String, Object> params) {
		return alertDao.deleteAlertByGroupName(params);
	}

	@Override
	public AlarmConfigInfo getAlarmConfigByGroupName(Map<String, Object> params) {
		return alertDao.getAlarmConfigByGroupName(params);
	}

	@Override
	public String getAlertClusterTypeList(String type, Map<String, Object> params) {
		int offset = 0;
		JSONArray typeList = new JSONArray();
		if ("type".equals(type)) {
			if (params.get("search").toString().length() > 0) {
				for (String cluster : AlarmType.CLUSTER) {
					if (cluster.contains(params.get("search").toString())) {
						JSONObject object = new JSONObject();
						object.put("text", cluster);
						object.put("id", offset);
						typeList.add(object);
						offset++;
					}
				}
			} else {
				for (String cluster : AlarmType.CLUSTER) {
					JSONObject object = new JSONObject();
					object.put("text", cluster);
					object.put("id", offset);
					typeList.add(object);
					offset++;
				}
			}
		} else if ("level".equals(type)) {
			if (params.get("search").toString().length() > 0) {
				for (String level : AlarmType.LEVEL) {
					if (level.contains(params.get("search").toString())) {
						JSONObject object = new JSONObject();
						object.put("text", level);
						object.put("id", offset);
						typeList.add(object);
						offset++;
					}
				}
			} else {
				for (String level : AlarmType.LEVEL) {
					JSONObject object = new JSONObject();
					object.put("text", level);
					object.put("id", offset);
					typeList.add(object);
					offset++;
				}
			}
		} else if ("group".equals(type)) {
			List<AlarmConfigInfo> alarmGroups = alertDao.getAlarmConfigList(params);
			if (alarmGroups != null) {
				for (AlarmConfigInfo alarmGroup : alarmGroups) {
					JSONObject object = new JSONObject();
					object.put("text", alarmGroup.getAlarmGroup());
					object.put("id", offset);
					typeList.add(object);
					offset++;
				}
			}
		} else if ("maxtimes".equals(type)) {
			if (params.get("search").toString().length() > 0) {
				for (int maxtimes : AlarmType.MAXTIMES) {
					Pattern pattern = Pattern.compile("^[+-]?\\d");
					if (pattern.matcher(params.get("search").toString()).matches() && maxtimes == Integer.parseInt(params.get("search").toString())) {
						JSONObject object = new JSONObject();
						object.put("text", maxtimes);
						object.put("id", offset);
						typeList.add(object);
						offset++;
					}
				}
			} else {
				for (int maxtimes : AlarmType.MAXTIMES) {
					JSONObject object = new JSONObject();
					object.put("text", maxtimes);
					object.put("id", offset);
					typeList.add(object);
					offset++;
				}
			}
		}

		return typeList.toJSONString();
	}

	@Override
	public int modifyClusterAlertSwitchById(AlarmClusterInfo clusterInfo) {
		return alertDao.modifyClusterAlertSwitchById(clusterInfo);
	}

	@Override
	public int modifyConsumerAlertSwitchById(AlarmConsumerInfo alarmConsumer) {
		return alertDao.modifyConsumerAlertSwitchById(alarmConsumer);
	}

	@Override
	public AlarmConsumerInfo findAlarmConsumerAlertById(int id) {
		return alertDao.findAlarmConsumerAlertById(id);
	}

	@Override
	public int modifyClusterStatusAlertById(AlarmClusterInfo cluster) {
		return alertDao.modifyClusterStatusAlertById(cluster);
	}

	@Override
	public List<AlarmConsumerInfo> getAllAlarmConsumerTasks() {
		return alertDao.getAllAlarmConsumerTasks();
	}

	@Override
	public long queryLastestLag(Map<String, Object> params) {
		return topicDao.queryLastestLag(params);
	}

	@Override
	public int modifyConsumerStatusAlertById(AlarmConsumerInfo alarmConsumer) {
		return alertDao.modifyConsumerStatusAlertById(alarmConsumer);
	}

	@Override
	public List<TopicLogSize> queryTopicProducerByAlarm(Map<String, Object> params) {
		return topicDao.queryTopicProducerByAlarm(params);
	}

}
