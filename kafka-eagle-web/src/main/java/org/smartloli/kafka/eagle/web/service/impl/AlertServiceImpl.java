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

import org.smartloli.kafka.eagle.common.protocol.AlertInfo;
import org.smartloli.kafka.eagle.common.protocol.ClustersInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmClusterInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmConfigInfo;
import org.smartloli.kafka.eagle.common.util.KConstants.AlarmType;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.web.dao.AlertDao;
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

	@Override
	public int add(AlertInfo alert) {
		return alertDao.insertAlert(alert);
	}

	public String get(String clusterAlias, String formatter) {
		if ("kafka".equals(formatter)) {
			return getKafka(clusterAlias);
		} else {
			return get(clusterAlias);
		}
	}

	/** Get consumer topics to alert. */
	private String get(String clusterAlias) {
		Map<String, List<String>> consumers = kafkaService.getConsumers(clusterAlias);
		JSONArray topics = new JSONArray();
		for (Entry<String, List<String>> entry : consumers.entrySet()) {
			JSONObject groupAndTopics = new JSONObject();
			groupAndTopics.put("group", entry.getKey());
			groupAndTopics.put("topics", entry.getValue());
			topics.add(groupAndTopics);
		}
		return topics.toJSONString();
	}

	private String getKafka(String clusterAlias) {
		JSONArray topics = new JSONArray();
		JSONArray consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumer(clusterAlias));
		for (Object object : consumerGroups) {
			JSONObject consumerGroup = (JSONObject) object;
			JSONObject groupAndTopics = new JSONObject();
			groupAndTopics.put("group", consumerGroup.getString("group"));
			groupAndTopics.put("topics", kafkaService.getKafkaConsumerTopic(clusterAlias, consumerGroup.getString("group")));
			topics.add(groupAndTopics);
		}
		return topics.toJSONString();
	}

	@Override
	public List<AlertInfo> list(Map<String, Object> params) {
		return alertDao.query(params);
	}

	@Override
	public int alertCount(Map<String, Object> params) {
		return alertDao.alertCount(params);
	}

	@Override
	public int isExistAlertByCGT(Map<String, Object> params) {
		return alertDao.isExistAlertByCGT(params);
	}

	@Override
	public int deleteAlertById(int id) {
		return alertDao.deleteAlertById(id);
	}

	@Override
	public String findAlertById(int id) {
		AlertInfo alert = alertDao.findAlertById(id);
		JSONObject object = new JSONObject();
		object.put("lag", alert.getLag());
		object.put("owners", alert.getOwner());
		return object.toJSONString();
	}

	@Override
	public int modifyAlertById(AlertInfo alert) {
		return alertDao.modifyAlertById(alert);
	}

	@Override
	public AlertInfo findAlertByCGT(Map<String, Object> params) {
		return alertDao.findAlertByCGT(params);
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
	public int modifyClusterAlertById(ClustersInfo cluster) {
		return alertDao.modifyClusterAlertById(cluster);
	}

	@Override
	public List<AlarmClusterInfo> getAllAlarmTasks() {
		return alertDao.getAllAlarmTasks();
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
			for (String cluster : AlarmType.CLUSTER) {
				JSONObject object = new JSONObject();
				object.put("text", cluster);
				object.put("id", offset);
				typeList.add(object);
				offset++;
			}
		} else if ("level".equals(type)) {
			for (String level : AlarmType.LEVEL) {
				JSONObject object = new JSONObject();
				object.put("text", level);
				object.put("id", offset);
				typeList.add(object);
				offset++;
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
			for (int maxtimes : AlarmType.MAXTIMES) {
				JSONObject object = new JSONObject();
				object.put("text", maxtimes);
				object.put("id", offset);
				typeList.add(object);
				offset++;
			}
		}

		return typeList.toJSONString();
	}

}
