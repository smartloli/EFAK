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
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.AlarmType;
import org.smartloli.kafka.eagle.common.util.NetUtils;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.AlertServiceImpl;

/**
 * Alert consumer topic & cluster heathy.
 * 
 * @author smartloli.
 *
 *         Created by Oct 28, 2018
 */
public class AlertQuartz {

	private Logger LOG = LoggerFactory.getLogger(AlertQuartz.class);

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
					if (lag > alarmConsumer.getLag() && alarmConsumer.getAlarmTimes() <= alarmConsumer.getAlarmMaxTimes()) {
						// alarm consumer
						alarmConsumer.setAlarmTimes(alarmConsumer.getAlarmTimes() + 1);
						alarmConsumer.setIsNormal("N");
						alertService.modifyConsumerStatusAlertById(alarmConsumer);
						try {
							sendAlarmConsumerError(alarmConfing, alarmConsumer, lag);
						} catch (Exception e) {
							LOG.error("Send alarm consumer exception has error, msg is " + e.getCause().getMessage());
						}
					} else {
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
				if (alarmConfing.getHttpMethod().equals(AlarmType.HTTP_GET)) {

				} else if (alarmConfing.getHttpMethod().equals(AlarmType.HTTP_POST)) {

				}
			} else if (alarmConfing.getAlarmType().equals(AlarmType.DingDing)) {
				AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
				alarmMsg.setAlarmId(alarmConsumer.getId());
				alarmMsg.setTitle("**<font color=\"#FF0000\">Kafka Eagle Alarm Consumer Notice</font>** \n\n");
				alarmMsg.setAlarmContent("<font color=\"#FF0000\">lag.overflow [ current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]</font>");
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
				alarmMsg.setAlarmContent("<font color=\"warning\">lag.overflow [ current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]</font>");
				alarmMsg.setAlarmDate(CalendarUtils.getDate());
				alarmMsg.setAlarmLevel(alarmConsumer.getAlarmLevel());
				alarmMsg.setAlarmProject("Consumer");
				alarmMsg.setAlarmStatus("<font color=\"warning\">PROBLEM</font>");
				alarmMsg.setAlarmTimes("current(" + alarmConsumer.getAlarmTimes() + "), max(" + alarmConsumer.getAlarmMaxTimes() + ")");
				IMServiceImpl im = new IMServiceImpl();
				im.sendPostMsgByWeChat(alarmMsg.toWeChatMarkDown(), alarmConfing.getAlarmUrl());
			} else if (alarmConfing.getAlarmType().equals(AlarmType.WebHook)) {
				if (alarmConfing.getHttpMethod().equals(AlarmType.HTTP_GET)) {

				} else if (alarmConfing.getHttpMethod().equals(AlarmType.HTTP_POST)) {

				}
			}
		}

		private void sendAlarmConsumerNormal(AlarmConfigInfo alarmConfing, AlarmConsumerInfo alarmConsumer, long lag) {
			if (alarmConfing.getAlarmType().equals(AlarmType.EMAIL)) {
				if (alarmConfing.getHttpMethod().equals(AlarmType.HTTP_GET)) {

				} else if (alarmConfing.getHttpMethod().equals(AlarmType.HTTP_POST)) {

				}
			} else if (alarmConfing.getAlarmType().equals(AlarmType.DingDing)) {
				AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
				alarmMsg.setAlarmId(alarmConsumer.getId());
				alarmMsg.setTitle("**<font color=\"#008000\">Kafka Eagle Alarm Consumer Cancel</font>** \n\n");
				alarmMsg.setAlarmContent("<font color=\"#008000\">lag.normal [ current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]</font>");
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
				alarmMsg.setAlarmContent("<font color=\"#008000\">lag.normal [ current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]</font>");
				alarmMsg.setAlarmDate(CalendarUtils.getDate());
				alarmMsg.setAlarmLevel(alarmConsumer.getAlarmLevel());
				alarmMsg.setAlarmProject("Consumer");
				alarmMsg.setAlarmStatus("<font color=\"#008000\">NORMAL</font>");
				alarmMsg.setAlarmTimes("current(" + alarmConsumer.getAlarmTimes() + "), max(" + alarmConsumer.getAlarmMaxTimes() + ")");
				IMServiceImpl im = new IMServiceImpl();
				im.sendPostMsgByWeChat(alarmMsg.toWeChatMarkDown(), alarmConfing.getAlarmUrl());
			} else if (alarmConfing.getAlarmType().equals(AlarmType.WebHook)) {
				if (alarmConfing.getHttpMethod().equals(AlarmType.HTTP_GET)) {

				} else if (alarmConfing.getHttpMethod().equals(AlarmType.HTTP_POST)) {

				}
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
				String[] servers = cluster.getServer().split(",");
				String alarmGroup = cluster.getAlarmGroup();
				Map<String, Object> params = new HashMap<>();
				params.put("cluster", cluster.getCluster());
				params.put("alarmGroup", alarmGroup);
				AlarmConfigInfo alarmConfing = alertService.getAlarmConfigByGroupName(params);
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
				if (errorServers.size() > 0 && cluster.getAlarmTimes() <= cluster.getAlarmMaxTimes()) {
					cluster.setAlarmTimes(cluster.getAlarmTimes() + 1);
					cluster.setIsNormal("N");
					alertService.modifyClusterStatusAlertById(cluster);
					try {
						sendAlarmClusterError(alarmConfing, cluster, errorServers.toString());
					} catch (Exception e) {
						LOG.error("Send alarm cluser exception has error, msg is " + e.getCause().getMessage());
					}
				} else {
					if (cluster.getIsNormal().equals("N")) {
						cluster.setIsNormal("Y");
						// clear error alarm and reset
						cluster.setAlarmTimes(0);
						// notify the cancel of the alarm
						alertService.modifyClusterStatusAlertById(cluster);
						try {
							sendAlarmClusterNormal(alarmConfing, cluster, normalServers.toString());
						} catch (Exception e) {
							LOG.error("Send alarm cluser normal has error, msg is " + e.getCause().getMessage());
						}
					}
				}
			}
		}

		private void sendAlarmClusterError(AlarmConfigInfo alarmConfing, AlarmClusterInfo cluster, String server) {
			if (alarmConfing.getAlarmType().equals(AlarmType.EMAIL)) {
				if (alarmConfing.getHttpMethod().equals(AlarmType.HTTP_GET)) {

				} else if (alarmConfing.getHttpMethod().equals(AlarmType.HTTP_POST)) {

				}
			} else if (alarmConfing.getAlarmType().equals(AlarmType.DingDing)) {
				AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
				alarmMsg.setAlarmId(cluster.getId());
				alarmMsg.setTitle("**<font color=\"#FF0000\">Kafka Eagle Alarm Cluster Notice</font>** \n\n");
				alarmMsg.setAlarmContent("<font color=\"#FF0000\">node.shutdown [ " + server + " ]</font>");
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
				alarmMsg.setAlarmContent("<font color=\"warning\">node.shutdown [ " + server + " ]</font>");
				alarmMsg.setAlarmDate(CalendarUtils.getDate());
				alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
				alarmMsg.setAlarmProject(cluster.getType());
				alarmMsg.setAlarmStatus("<font color=\"warning\">PROBLEM</font>");
				alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + "), max(" + cluster.getAlarmMaxTimes() + ")");
				IMServiceImpl im = new IMServiceImpl();
				im.sendPostMsgByWeChat(alarmMsg.toWeChatMarkDown(), alarmConfing.getAlarmUrl());
			} else if (alarmConfing.getAlarmType().equals(AlarmType.WebHook)) {
				if (alarmConfing.getHttpMethod().equals(AlarmType.HTTP_GET)) {

				} else if (alarmConfing.getHttpMethod().equals(AlarmType.HTTP_POST)) {

				}
			}
		}

		private void sendAlarmClusterNormal(AlarmConfigInfo alarmConfing, AlarmClusterInfo cluster, String server) {
			if (alarmConfing.getAlarmType().equals(AlarmType.EMAIL)) {
				if (alarmConfing.getHttpMethod().equals(AlarmType.HTTP_GET)) {

				} else if (alarmConfing.getHttpMethod().equals(AlarmType.HTTP_POST)) {

				}
			} else if (alarmConfing.getAlarmType().equals(AlarmType.DingDing)) {
				AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
				alarmMsg.setAlarmId(cluster.getId());
				alarmMsg.setTitle("**<font color=\"#008000\">Kafka Eagle Alarm Cluster Cancel</font>** \n\n");
				alarmMsg.setAlarmContent("<font color=\"#008000\">node.alive [ " + server + " ]</font>");
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
				alarmMsg.setAlarmContent("<font color=\"#008000\">node.alive [ " + server + " ]</font>");
				alarmMsg.setAlarmDate(CalendarUtils.getDate());
				alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
				alarmMsg.setAlarmProject(cluster.getType());
				alarmMsg.setAlarmStatus("<font color=\"#008000\">NORMAL</font>");
				alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + "), max(" + cluster.getAlarmMaxTimes() + ")");
				IMServiceImpl im = new IMServiceImpl();
				im.sendPostMsgByWeChat(alarmMsg.toWeChatMarkDown(), alarmConfing.getAlarmUrl());
			} else if (alarmConfing.getAlarmType().equals(AlarmType.WebHook)) {
				if (alarmConfing.getHttpMethod().equals(AlarmType.HTTP_GET)) {

				} else if (alarmConfing.getHttpMethod().equals(AlarmType.HTTP_POST)) {

				}
			}
		}

	}

}
