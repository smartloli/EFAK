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
package org.smartloli.kafka.eagle.web.quartz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.api.im.IMFactory;
import org.smartloli.kafka.eagle.api.im.IMService;
import org.smartloli.kafka.eagle.api.im.IMServiceImpl;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmClusterInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmConfigInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmConsumerInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmMessageInfo;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicLogSize;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.AlarmType;
import org.smartloli.kafka.eagle.common.util.NetUtils;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsFactory;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.AlertServiceImpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * Alert consumer topic & cluster heathy.
 * 
 * @author smartloli.
 *
 *         Created by Oct 28, 2018
 */
public class AlertQuartz {

	private Logger LOG = LoggerFactory.getLogger(AlertQuartz.class);

	/** Kafka topic config service interface. */
	private KafkaMetricsService kafkaMetricsService = new KafkaMetricsFactory().create();

	public void alertJobQuartz() {
		// Run consumer job
		Consumer consumer = new Consumer();
		consumer.consumer();

		// Run cluster job
		Cluster cluster = new Cluster();
		cluster.cluster();
	}

	class Consumer {
		public void consumer() {
			try {
				AlertServiceImpl alertService = StartupListener.getBean("alertServiceImpl", AlertServiceImpl.class);
				List<AlarmConsumerInfo> alarmConsumers = alertService.getAllAlarmConsumerTasks();
				for (AlarmConsumerInfo alarmConsumer : alarmConsumers) {
					if (AlarmType.DISABLE.equals(alarmConsumer.getIsEnable())) {
						break;
					}

					Map<String, Object> map = new HashMap<>();
					map.put("cluster", alarmConsumer.getCluster());
					map.put("alarmGroup", alarmConsumer.getAlarmGroup());
					AlarmConfigInfo alarmConfing = alertService.getAlarmConfigByGroupName(map);

					Map<String, Object> params = new HashMap<>();
					params.put("cluster", alarmConsumer.getCluster());
					params.put("tday", CalendarUtils.getCustomDate("yyyyMMdd"));
					params.put("group", alarmConsumer.getGroup());
					params.put("topic", alarmConsumer.getTopic());
					// real consumer lag
					long lag = alertService.queryLastestLag(params);
					if (lag > alarmConsumer.getLag() && (alarmConsumer.getAlarmTimes() < alarmConsumer.getAlarmMaxTimes() || alarmConsumer.getAlarmMaxTimes() == -1)) {
						// alarm consumer
						alarmConsumer.setAlarmTimes(alarmConsumer.getAlarmTimes() + 1);
						alarmConsumer.setIsNormal("N");
						alertService.modifyConsumerStatusAlertById(alarmConsumer);
						try {
							sendAlarmConsumerError(alarmConfing, alarmConsumer, lag);
						} catch (Exception e) {
							LOG.error("Send alarm consumer exception has error, msg is " + e.getCause().getMessage());
						}
					} else if (lag <= alarmConsumer.getLag()) {
						if (alarmConsumer.getIsNormal().equals("N")) {
							alarmConsumer.setIsNormal("Y");
							// clear error alarm and reset
							alarmConsumer.setAlarmTimes(0);
							// notify the cancel of the alarm
							alertService.modifyConsumerStatusAlertById(alarmConsumer);
							try {
								sendAlarmConsumerNormal(alarmConfing, alarmConsumer, lag);
							} catch (Exception e) {
								LOG.error("Send alarm consumer normal has error, msg is " + e.getCause().getMessage());
							}
						}
					}
				}
			} catch (Exception ex) {
				LOG.error("Alarm consumer lag has error, msg is " + ex.getCause().getMessage());
				ex.printStackTrace();
			}
		}

		private void sendAlarmConsumerError(AlarmConfigInfo alarmConfing, AlarmConsumerInfo alarmConsumer, long lag) {
			if (alarmConfing.getAlarmType().equals(AlarmType.EMAIL)) {
				AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
				alarmMsg.setAlarmId(alarmConsumer.getId());
				alarmMsg.setTitle("Kafka Eagle Alarm Consumer Notice");
				alarmMsg.setAlarmContent("lag.overflow [ cluster(" + alarmConsumer.getCluster() + "), group(" + alarmConsumer.getGroup() + "), topic(" + alarmConsumer.getTopic() + "), current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]");
				alarmMsg.setAlarmDate(CalendarUtils.getDate());
				alarmMsg.setAlarmLevel(alarmConsumer.getAlarmLevel());
				alarmMsg.setAlarmProject("Consumer");
				alarmMsg.setAlarmStatus("PROBLEM");
				alarmMsg.setAlarmTimes("current(" + alarmConsumer.getAlarmTimes() + "), max(" + alarmConsumer.getAlarmMaxTimes() + ")");
				IMService im = new IMFactory().create();
				JSONObject object = new JSONObject();
				object.put("address", alarmConfing.getAlarmAddress());
				object.put("msg", alarmMsg.toMail());
				im.sendPostMsgByMail(object.toJSONString(), alarmConfing.getAlarmUrl());
			} else if (alarmConfing.getAlarmType().equals(AlarmType.DingDing)) {
				AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
				alarmMsg.setAlarmId(alarmConsumer.getId());
				alarmMsg.setTitle("**<font color=\"#FF0000\">Kafka Eagle Alarm Consumer Notice</font>** \n\n");
				alarmMsg.setAlarmContent("<font color=\"#FF0000\">lag.overflow [ cluster(" + alarmConsumer.getCluster() + "), group(" + alarmConsumer.getGroup() + "), topic(" + alarmConsumer.getTopic() + "), current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]</font>");
				alarmMsg.setAlarmDate(CalendarUtils.getDate());
				alarmMsg.setAlarmLevel(alarmConsumer.getAlarmLevel());
				alarmMsg.setAlarmProject("Consumer");
				alarmMsg.setAlarmStatus("<font color=\"#FF0000\">PROBLEM</font>");
				alarmMsg.setAlarmTimes("current(" + alarmConsumer.getAlarmTimes() + "), max(" + alarmConsumer.getAlarmMaxTimes() + ")");
				IMService im = new IMFactory().create();
				im.sendPostMsgByDingDing(alarmMsg.toDingDingMarkDown(), alarmConfing.getAlarmUrl());
			} else if (alarmConfing.getAlarmType().equals(AlarmType.WeChat)) {
				AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
				alarmMsg.setAlarmId(alarmConsumer.getId());
				alarmMsg.setTitle("`Kafka Eagle Alarm Consumer Notice`\n");
				alarmMsg.setAlarmContent("<font color=\"warning\">lag.overflow [ cluster(" + alarmConsumer.getCluster() + "), group(" + alarmConsumer.getGroup() + "), topic(" + alarmConsumer.getTopic() + "), current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]</font>");
				alarmMsg.setAlarmDate(CalendarUtils.getDate());
				alarmMsg.setAlarmLevel(alarmConsumer.getAlarmLevel());
				alarmMsg.setAlarmProject("Consumer");
				alarmMsg.setAlarmStatus("<font color=\"warning\">PROBLEM</font>");
				alarmMsg.setAlarmTimes("current(" + alarmConsumer.getAlarmTimes() + "), max(" + alarmConsumer.getAlarmMaxTimes() + ")");
				IMServiceImpl im = new IMServiceImpl();
				im.sendPostMsgByWeChat(alarmMsg.toWeChatMarkDown(), alarmConfing.getAlarmUrl());
			}
		}

		private void sendAlarmConsumerNormal(AlarmConfigInfo alarmConfing, AlarmConsumerInfo alarmConsumer, long lag) {
			if (alarmConfing.getAlarmType().equals(AlarmType.EMAIL)) {
				AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
				alarmMsg.setAlarmId(alarmConsumer.getId());
				alarmMsg.setTitle("Kafka Eagle Alarm Consumer Cancel");
				alarmMsg.setAlarmContent("lag.normal [ cluster(" + alarmConsumer.getCluster() + "), group(" + alarmConsumer.getGroup() + "), topic(" + alarmConsumer.getTopic() + "), current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]");
				alarmMsg.setAlarmDate(CalendarUtils.getDate());
				alarmMsg.setAlarmLevel(alarmConsumer.getAlarmLevel());
				alarmMsg.setAlarmProject("Consumer");
				alarmMsg.setAlarmStatus("NORMAL");
				alarmMsg.setAlarmTimes("current(" + alarmConsumer.getAlarmTimes() + "), max(" + alarmConsumer.getAlarmMaxTimes() + ")");
				IMService im = new IMFactory().create();
				JSONObject object = new JSONObject();
				object.put("address", alarmConfing.getAlarmAddress());
				object.put("msg", alarmMsg.toMail());
				im.sendPostMsgByMail(object.toJSONString(), alarmConfing.getAlarmUrl());
			} else if (alarmConfing.getAlarmType().equals(AlarmType.DingDing)) {
				AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
				alarmMsg.setAlarmId(alarmConsumer.getId());
				alarmMsg.setTitle("**<font color=\"#008000\">Kafka Eagle Alarm Consumer Cancel</font>** \n\n");
				alarmMsg.setAlarmContent("<font color=\"#008000\">lag.normal [ cluster(" + alarmConsumer.getCluster() + "), group(" + alarmConsumer.getGroup() + "), topic(" + alarmConsumer.getTopic() + "), current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]</font>");
				alarmMsg.setAlarmDate(CalendarUtils.getDate());
				alarmMsg.setAlarmLevel(alarmConsumer.getAlarmLevel());
				alarmMsg.setAlarmProject("Consumer");
				alarmMsg.setAlarmStatus("<font color=\"#008000\">NORMAL</font>");
				alarmMsg.setAlarmTimes("current(" + alarmConsumer.getAlarmTimes() + "), max(" + alarmConsumer.getAlarmMaxTimes() + ")");
				IMService im = new IMFactory().create();
				im.sendPostMsgByDingDing(alarmMsg.toDingDingMarkDown(), alarmConfing.getAlarmUrl());
			} else if (alarmConfing.getAlarmType().equals(AlarmType.WeChat)) {
				AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
				alarmMsg.setAlarmId(alarmConsumer.getId());
				alarmMsg.setTitle("`Kafka Eagle Alarm Consumer Cancel`\n");
				alarmMsg.setAlarmContent("<font color=\"#008000\">lag.normal [ cluster(" + alarmConsumer.getCluster() + "), group(" + alarmConsumer.getGroup() + "), topic(" + alarmConsumer.getTopic() + "), current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]</font>");
				alarmMsg.setAlarmDate(CalendarUtils.getDate());
				alarmMsg.setAlarmLevel(alarmConsumer.getAlarmLevel());
				alarmMsg.setAlarmProject("Consumer");
				alarmMsg.setAlarmStatus("<font color=\"#008000\">NORMAL</font>");
				alarmMsg.setAlarmTimes("current(" + alarmConsumer.getAlarmTimes() + "), max(" + alarmConsumer.getAlarmMaxTimes() + ")");
				IMServiceImpl im = new IMServiceImpl();
				im.sendPostMsgByWeChat(alarmMsg.toWeChatMarkDown(), alarmConfing.getAlarmUrl());
			}
		}
	}

	class Cluster {

		public void cluster() {
			AlertServiceImpl alertService = StartupListener.getBean("alertServiceImpl", AlertServiceImpl.class);
			for (AlarmClusterInfo cluster : alertService.getAllAlarmClusterTasks()) {
				if (AlarmType.DISABLE.equals(cluster.getIsEnable())) {
					break;
				}
				String alarmGroup = cluster.getAlarmGroup();
				Map<String, Object> params = new HashMap<>();
				params.put("cluster", cluster.getCluster());
				params.put("alarmGroup", alarmGroup);
				AlarmConfigInfo alarmConfig = alertService.getAlarmConfigByGroupName(params);
				if (AlarmType.TOPIC.equals(cluster.getType())) {
					JSONObject topicAlarmJson = JSON.parseObject(cluster.getServer());
					String topic = topicAlarmJson.getString("topic");
					long alarmCapacity = topicAlarmJson.getLong("capacity");
					long realCapacity = kafkaMetricsService.topicCapacity(cluster.getCluster(), topic);
					JSONObject alarmTopicMsg = new JSONObject();
					alarmTopicMsg.put("topic", topic);
					alarmTopicMsg.put("alarmCapacity", alarmCapacity);
					alarmTopicMsg.put("realCapacity", realCapacity);
					if (realCapacity > alarmCapacity && (cluster.getAlarmTimes() < cluster.getAlarmMaxTimes() || cluster.getAlarmMaxTimes() == -1)) {
						cluster.setAlarmTimes(cluster.getAlarmTimes() + 1);
						cluster.setIsNormal("N");
						alertService.modifyClusterStatusAlertById(cluster);
						try {
							sendAlarmClusterError(alarmConfig, cluster, alarmTopicMsg.toJSONString());
						} catch (Exception e) {
							LOG.error("Send alarm cluser exception has error, msg is " + e.getCause().getMessage());
						}
					} else if (realCapacity < alarmCapacity) {
						if (cluster.getIsNormal().equals("N")) {
							cluster.setIsNormal("Y");
							// clear error alarm and reset
							cluster.setAlarmTimes(0);
							// notify the cancel of the alarm
							alertService.modifyClusterStatusAlertById(cluster);
							try {
								sendAlarmClusterNormal(alarmConfig, cluster, alarmTopicMsg.toJSONString());
							} catch (Exception e) {
								LOG.error("Send alarm cluser normal has error, msg is " + e.getCause().getMessage());
							}
						}
					}
				} else if (AlarmType.PRODUCER.equals(cluster.getType())) {
					JSONObject producerAlarmJson = JSON.parseObject(cluster.getServer());
					String topic = producerAlarmJson.getString("topic");
					String[] speeds = producerAlarmJson.getString("speed").split(",");
					long startSpeed = 0L;
					long endSpeed = 0L;
					if (speeds.length == 2) {
						startSpeed = Long.parseLong(speeds[0]);
						endSpeed = Long.parseLong(speeds[1]);
					}
					Map<String, Object> producerSpeedParams = new HashMap<>();
					producerSpeedParams.put("cluster", cluster.getCluster());
					producerSpeedParams.put("topic", topic);
					producerSpeedParams.put("stime", CalendarUtils.getCustomDate("yyyyMMdd"));
					List<TopicLogSize> topicLogSizes = alertService.queryTopicProducerByAlarm(producerSpeedParams);
					long realSpeed = 0;
					if (topicLogSizes != null && topicLogSizes.size() > 0) {
						realSpeed = topicLogSizes.get(0).getDiffval();
					}

					JSONObject alarmTopicMsg = new JSONObject();
					alarmTopicMsg.put("topic", topic);
					alarmTopicMsg.put("alarmSpeeds", startSpeed + "," + endSpeed);
					alarmTopicMsg.put("realSpeeds", realSpeed);
					if ((realSpeed < startSpeed || realSpeed > endSpeed) && (cluster.getAlarmTimes() < cluster.getAlarmMaxTimes() || cluster.getAlarmMaxTimes() == -1)) {
						cluster.setAlarmTimes(cluster.getAlarmTimes() + 1);
						cluster.setIsNormal("N");
						alertService.modifyClusterStatusAlertById(cluster);
						try {
							sendAlarmClusterError(alarmConfig, cluster, alarmTopicMsg.toJSONString());
						} catch (Exception e) {
							LOG.error("Send alarm cluser exception has error, msg is " + e.getCause().getMessage());
						}
					} else if (realSpeed >= startSpeed && realSpeed <= endSpeed) {
						if (cluster.getIsNormal().equals("N")) {
							cluster.setIsNormal("Y");
							// clear error alarm and reset
							cluster.setAlarmTimes(0);
							// notify the cancel of the alarm
							alertService.modifyClusterStatusAlertById(cluster);
							try {
								sendAlarmClusterNormal(alarmConfig, cluster, alarmTopicMsg.toJSONString());
							} catch (Exception e) {
								LOG.error("Send alarm cluser normal has error, msg is " + e.getCause().getMessage());
							}
						}
					}
				} else {
					String[] servers = cluster.getServer().split(",");
					List<String> errorServers = new ArrayList<String>();
					List<String> normalServers = new ArrayList<String>();
					for (String server : servers) {
						String host = server.split(":")[0];
						int port = 0;
						try {
							port = Integer.parseInt(server.split(":")[1]);
							boolean status = NetUtils.telnet(host, port);
							if (!status) {
								errorServers.add(server);
							} else {
								normalServers.add(server);
							}
						} catch (Exception e) {
							LOG.error("Alarm cluster has error, msg is " + e.getCause().getMessage());
							e.printStackTrace();
						}
					}
					if (errorServers.size() > 0 && (cluster.getAlarmTimes() < cluster.getAlarmMaxTimes() || cluster.getAlarmMaxTimes() == -1)) {
						cluster.setAlarmTimes(cluster.getAlarmTimes() + 1);
						cluster.setIsNormal("N");
						alertService.modifyClusterStatusAlertById(cluster);
						try {
							sendAlarmClusterError(alarmConfig, cluster, errorServers.toString());
						} catch (Exception e) {
							LOG.error("Send alarm cluser exception has error, msg is " + e.getCause().getMessage());
						}
					} else if (errorServers.size() == 0) {
						if (cluster.getIsNormal().equals("N")) {
							cluster.setIsNormal("Y");
							// clear error alarm and reset
							cluster.setAlarmTimes(0);
							// notify the cancel of the alarm
							alertService.modifyClusterStatusAlertById(cluster);
							try {
								sendAlarmClusterNormal(alarmConfig, cluster, normalServers.toString());
							} catch (Exception e) {
								LOG.error("Send alarm cluser normal has error, msg is " + e.getCause().getMessage());
							}
						}
					}
				}
			}
		}

		private void sendAlarmClusterError(AlarmConfigInfo alarmConfing, AlarmClusterInfo cluster, String server) {
			if (alarmConfing.getAlarmType().equals(AlarmType.EMAIL)) {
				AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
				alarmMsg.setAlarmId(cluster.getId());
				alarmMsg.setTitle("Kafka Eagle Alarm Cluster Notice");
				if (AlarmType.TOPIC.equals(cluster.getType())) {
					JSONObject alarmTopicMsg = JSON.parseObject(server);
					String topic = alarmTopicMsg.getString("topic");
					long alarmCapacity = alarmTopicMsg.getLong("alarmCapacity");
					long realCapacity = alarmTopicMsg.getLong("realCapacity");
					alarmMsg.setAlarmContent("topic.capacity.overflow [topic(" + topic + "), real.capacity(" + StrUtils.stringify(realCapacity) + "), alarm.capacity(" + StrUtils.stringify(alarmCapacity) + ")]");
				} else if (AlarmType.PRODUCER.equals(cluster.getType())) {
					JSONObject alarmTopicMsg = JSON.parseObject(server);
					String topic = alarmTopicMsg.getString("topic");
					String alarmSpeeds = alarmTopicMsg.getString("alarmSpeeds");
					long realSpeeds = alarmTopicMsg.getLong("realSpeeds");
					alarmMsg.setAlarmContent("producer.speed.overflow [topic(" + topic + "), real.speeds(" + realSpeeds + "), alarm.speeds.range(" + alarmSpeeds + ")]");
				} else {
					alarmMsg.setAlarmContent("node.shutdown [ " + server + " ]");
				}
				alarmMsg.setAlarmDate(CalendarUtils.getDate());
				alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
				alarmMsg.setAlarmProject(cluster.getType());
				alarmMsg.setAlarmStatus("PROBLEM");
				alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + "), max(" + cluster.getAlarmMaxTimes() + ")");
				IMService im = new IMFactory().create();
				JSONObject object = new JSONObject();
				object.put("address", alarmConfing.getAlarmAddress());
				object.put("msg", alarmMsg.toMail());
				im.sendPostMsgByMail(object.toJSONString(), alarmConfing.getAlarmUrl());
			} else if (alarmConfing.getAlarmType().equals(AlarmType.DingDing)) {
				AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
				alarmMsg.setAlarmId(cluster.getId());
				alarmMsg.setTitle("**<font color=\"#FF0000\">Kafka Eagle Alarm Cluster Notice</font>** \n\n");
				if (AlarmType.TOPIC.equals(cluster.getType())) {
					JSONObject alarmTopicMsg = JSON.parseObject(server);
					String topic = alarmTopicMsg.getString("topic");
					long alarmCapacity = alarmTopicMsg.getLong("alarmCapacity");
					long realCapacity = alarmTopicMsg.getLong("realCapacity");
					alarmMsg.setAlarmContent("<font color=\"#FF0000\">topic.capacity.overflow [topic(" + topic + "), real.capacity(" + StrUtils.stringify(realCapacity) + "), alarm.capacity(" + StrUtils.stringify(alarmCapacity) + ")]</font>");
				} else if (AlarmType.PRODUCER.equals(cluster.getType())) {
					JSONObject alarmTopicMsg = JSON.parseObject(server);
					String topic = alarmTopicMsg.getString("topic");
					String alarmSpeeds = alarmTopicMsg.getString("alarmSpeeds");
					long realSpeeds = alarmTopicMsg.getLong("realSpeeds");
					alarmMsg.setAlarmContent("<font color=\"#FF0000\">producer.speed.overflow [topic(" + topic + "), real.speeds(" + realSpeeds + "), alarm.speeds.range(" + alarmSpeeds + ")]</font>");
				} else {
					alarmMsg.setAlarmContent("<font color=\"#FF0000\">node.shutdown [ " + server + " ]</font>");
				}
				alarmMsg.setAlarmDate(CalendarUtils.getDate());
				alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
				alarmMsg.setAlarmProject(cluster.getType());
				alarmMsg.setAlarmStatus("<font color=\"#FF0000\">PROBLEM</font>");
				alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + "), max(" + cluster.getAlarmMaxTimes() + ")");
				IMService im = new IMFactory().create();
				im.sendPostMsgByDingDing(alarmMsg.toDingDingMarkDown(), alarmConfing.getAlarmUrl());
			} else if (alarmConfing.getAlarmType().equals(AlarmType.WeChat)) {
				AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
				alarmMsg.setAlarmId(cluster.getId());
				alarmMsg.setTitle("`Kafka Eagle Alarm Cluster Notice`\n");
				if (AlarmType.TOPIC.equals(cluster.getType())) {
					JSONObject alarmTopicMsg = JSON.parseObject(server);
					String topic = alarmTopicMsg.getString("topic");
					long alarmCapacity = alarmTopicMsg.getLong("alarmCapacity");
					long realCapacity = alarmTopicMsg.getLong("realCapacity");
					alarmMsg.setAlarmContent("<font color=\"warning\">topic.capacity.overflow [topic(" + topic + "), real.capacity(" + StrUtils.stringify(realCapacity) + "), alarm.capacity(" + StrUtils.stringify(alarmCapacity) + ")]</font>");
				} else if (AlarmType.PRODUCER.equals(cluster.getType())) {
					JSONObject alarmTopicMsg = JSON.parseObject(server);
					String topic = alarmTopicMsg.getString("topic");
					String alarmSpeeds = alarmTopicMsg.getString("alarmSpeeds");
					long realSpeeds = alarmTopicMsg.getLong("realSpeeds");
					alarmMsg.setAlarmContent("<font color=\"warning\">producer.speed.overflow [topic(" + topic + "), real.speeds(" + realSpeeds + "), alarm.speeds.range(" + alarmSpeeds + ")]</font>");
				} else {
					alarmMsg.setAlarmContent("<font color=\"warning\">node.shutdown [ " + server + " ]</font>");
				}
				alarmMsg.setAlarmDate(CalendarUtils.getDate());
				alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
				alarmMsg.setAlarmProject(cluster.getType());
				alarmMsg.setAlarmStatus("<font color=\"warning\">PROBLEM</font>");
				alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + "), max(" + cluster.getAlarmMaxTimes() + ")");
				IMServiceImpl im = new IMServiceImpl();
				im.sendPostMsgByWeChat(alarmMsg.toWeChatMarkDown(), alarmConfing.getAlarmUrl());
			}
		}

		private void sendAlarmClusterNormal(AlarmConfigInfo alarmConfing, AlarmClusterInfo cluster, String server) {
			if (alarmConfing.getAlarmType().equals(AlarmType.EMAIL)) {
				AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
				alarmMsg.setAlarmId(cluster.getId());
				alarmMsg.setTitle("Kafka Eagle Alarm Cluster Cancel");
				if (AlarmType.TOPIC.equals(cluster.getType())) {
					JSONObject alarmTopicMsg = JSON.parseObject(server);
					String topic = alarmTopicMsg.getString("topic");
					long alarmCapacity = alarmTopicMsg.getLong("alarmCapacity");
					long realCapacity = alarmTopicMsg.getLong("realCapacity");
					alarmMsg.setAlarmContent("topic.capacity.normal [topic(" + topic + "), real.capacity(" + StrUtils.stringify(realCapacity) + "), alarm.capacity(" + StrUtils.stringify(alarmCapacity) + ")]");
				} else if (AlarmType.PRODUCER.equals(cluster.getType())) {
					JSONObject alarmTopicMsg = JSON.parseObject(server);
					String topic = alarmTopicMsg.getString("topic");
					String alarmSpeeds = alarmTopicMsg.getString("alarmSpeeds");
					long realSpeeds = alarmTopicMsg.getLong("realSpeeds");
					alarmMsg.setAlarmContent("producer.speed.normal [topic(" + topic + "), real.speeds(" + realSpeeds + "), alarm.speeds.range(" + alarmSpeeds + ")]");
				} else {
					alarmMsg.setAlarmContent("node.alive [ " + server + " ]");
				}
				alarmMsg.setAlarmDate(CalendarUtils.getDate());
				alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
				alarmMsg.setAlarmProject(cluster.getType());
				alarmMsg.setAlarmStatus("NORMAL");
				alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + "), max(" + cluster.getAlarmMaxTimes() + ")");
				IMService im = new IMFactory().create();
				JSONObject object = new JSONObject();
				object.put("address", alarmConfing.getAlarmAddress());
				object.put("msg", alarmMsg.toMail());
				im.sendPostMsgByMail(object.toJSONString(), alarmConfing.getAlarmUrl());
			} else if (alarmConfing.getAlarmType().equals(AlarmType.DingDing)) {
				AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
				alarmMsg.setAlarmId(cluster.getId());
				alarmMsg.setTitle("**<font color=\"#008000\">Kafka Eagle Alarm Cluster Cancel</font>** \n\n");
				if (AlarmType.TOPIC.equals(cluster.getType())) {
					JSONObject alarmTopicMsg = JSON.parseObject(server);
					String topic = alarmTopicMsg.getString("topic");
					long alarmCapacity = alarmTopicMsg.getLong("alarmCapacity");
					long realCapacity = alarmTopicMsg.getLong("realCapacity");
					alarmMsg.setAlarmContent("<font color=\"#008000\">topic.capacity.normal [topic(" + topic + "), real.capacity(" + StrUtils.stringify(realCapacity) + "), alarm.capacity(" + StrUtils.stringify(alarmCapacity) + ")]</font>");
				} else if (AlarmType.PRODUCER.equals(cluster.getType())) {
					JSONObject alarmTopicMsg = JSON.parseObject(server);
					String topic = alarmTopicMsg.getString("topic");
					String alarmSpeeds = alarmTopicMsg.getString("alarmSpeeds");
					long realSpeeds = alarmTopicMsg.getLong("realSpeeds");
					alarmMsg.setAlarmContent("<font color=\"#008000\">producer.speed.normal [topic(" + topic + "), real.speeds(" + realSpeeds + "), alarm.speeds.range(" + alarmSpeeds + ")]</font>");
				} else {
					alarmMsg.setAlarmContent("<font color=\"#008000\">node.alive [ " + server + " ]</font>");
				}
				alarmMsg.setAlarmDate(CalendarUtils.getDate());
				alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
				alarmMsg.setAlarmProject(cluster.getType());
				alarmMsg.setAlarmStatus("<font color=\"#008000\">NORMAL</font>");
				alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + "), max(" + cluster.getAlarmMaxTimes() + ")");
				IMService im = new IMFactory().create();
				im.sendPostMsgByDingDing(alarmMsg.toDingDingMarkDown(), alarmConfing.getAlarmUrl());
			} else if (alarmConfing.getAlarmType().equals(AlarmType.WeChat)) {
				AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
				alarmMsg.setAlarmId(cluster.getId());
				alarmMsg.setTitle("`Kafka Eagle Alarm Cluster Cancel`\n");
				if (AlarmType.TOPIC.equals(cluster.getType())) {
					JSONObject alarmTopicMsg = JSON.parseObject(server);
					String topic = alarmTopicMsg.getString("topic");
					long alarmCapacity = alarmTopicMsg.getLong("alarmCapacity");
					long realCapacity = alarmTopicMsg.getLong("realCapacity");
					alarmMsg.setAlarmContent("<font color=\"#008000\">topic.capacity.normal [topic(" + topic + "), real.capacity(" + StrUtils.stringify(realCapacity) + "), alarm.capacity(" + StrUtils.stringify(alarmCapacity) + ")]</font>");
				} else if (AlarmType.PRODUCER.equals(cluster.getType())) {
					JSONObject alarmTopicMsg = JSON.parseObject(server);
					String topic = alarmTopicMsg.getString("topic");
					String alarmSpeeds = alarmTopicMsg.getString("alarmSpeeds");
					long realSpeeds = alarmTopicMsg.getLong("realSpeeds");
					alarmMsg.setAlarmContent("<font color=\"#008000\">producer.speed.normal [topic(" + topic + "), real.speeds(" + realSpeeds + "), alarm.speeds.range(" + alarmSpeeds + ")]</font>");
				} else {
					alarmMsg.setAlarmContent("<font color=\"#008000\">node.alive [ " + server + " ]</font>");
				}
				alarmMsg.setAlarmDate(CalendarUtils.getDate());
				alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
				alarmMsg.setAlarmProject(cluster.getType());
				alarmMsg.setAlarmStatus("<font color=\"#008000\">NORMAL</font>");
				alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + "), max(" + cluster.getAlarmMaxTimes() + ")");
				IMServiceImpl im = new IMServiceImpl();
				im.sendPostMsgByWeChat(alarmMsg.toWeChatMarkDown(), alarmConfing.getAlarmUrl());
			}
		}

	}

}
