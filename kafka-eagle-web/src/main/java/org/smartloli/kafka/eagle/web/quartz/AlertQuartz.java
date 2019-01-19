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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.api.email.MailFactory;
import org.smartloli.kafka.eagle.api.email.MailProvider;
import org.smartloli.kafka.eagle.api.email.module.ClusterContentModule;
import org.smartloli.kafka.eagle.api.email.module.LagContentModule;
import org.smartloli.kafka.eagle.api.im.IMFactory;
import org.smartloli.kafka.eagle.api.im.IMProvider;
import org.smartloli.kafka.eagle.common.protocol.AlertInfo;
import org.smartloli.kafka.eagle.common.protocol.ClustersInfo;
import org.smartloli.kafka.eagle.common.protocol.OffsetZkInfo;
import org.smartloli.kafka.eagle.common.protocol.OffsetsLiteInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.NetUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.AlertServiceImpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	public void alertJobQuartz() {
		String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
		for (String clusterAlias : clusterAliass) {
			// Run consumer job
			Consumer consumer = new Consumer();
			consumer.consumer(clusterAlias);

			// Run cluster job
			Cluster cluster = new Cluster();
			cluster.cluster();

		}
	}

	class Consumer {
		public void consumer(String clusterAlias) {
			try {
				List<String> hosts = getBrokers(clusterAlias);
				List<OffsetsLiteInfo> offsetLites = new ArrayList<OffsetsLiteInfo>();
				String formatter = SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage");
				Map<String, List<String>> consumers = null;
				if ("kafka".equals(formatter)) {
					Map<String, List<String>> consumerGroupMap = new HashMap<String, List<String>>();
					try {
						JSONArray consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumer(clusterAlias));
						for (Object object : consumerGroups) {
							JSONObject consumerGroup = (JSONObject) object;
							String group = consumerGroup.getString("group");
							List<String> topics = new ArrayList<>();
							for (String topic : kafkaService.getKafkaConsumerTopic(clusterAlias, group)) {
								topics.add(topic);
							}
							consumerGroupMap.put(group, topics);
						}
						consumers = consumerGroupMap;
					} catch (Exception e) {
						LOG.error("Get consumer info from [kafkaService.getKafkaConsumer] has error,msg is " + e.getMessage());
						e.printStackTrace();
					}
				} else {
					consumers = kafkaService.getConsumers(clusterAlias);
				}
				String statsPerDate = getStatsPerDate();
				for (Entry<String, List<String>> entry : consumers.entrySet()) {
					String group = entry.getKey();
					for (String topic : entry.getValue()) {
						OffsetsLiteInfo offsetSQLite = new OffsetsLiteInfo();
						for (String partitionStr : kafkaService.findTopicPartition(clusterAlias, topic)) {
							int partition = Integer.parseInt(partitionStr);
							long logSize = 0L;
							if ("kafka".equals(SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.offset.storage"))) {
								logSize = kafkaService.getKafkaLogSize(clusterAlias, topic, partition);
							} else {
								logSize = kafkaService.getLogSize(clusterAlias, topic, partition);
							}
//							if (SystemConfigUtils.getBooleanProperty("kafka.eagle.sasl.enable")) {
//								logSize = kafkaService.getKafkaLogSize(clusterAlias, topic, partition);
//							} else {
//								logSize = kafkaService.getLogSize(hosts, topic, partition);
//							}
							OffsetZkInfo offsetZk = null;
							if ("kafka".equals(formatter)) {
								String bootstrapServers = "";
								for (String host : hosts) {
									bootstrapServers += host + ",";
								}
								bootstrapServers = bootstrapServers.substring(0, bootstrapServers.length() - 1);
								offsetZk = getKafkaOffset(clusterAlias, bootstrapServers, topic, group, partition);
							} else {
								offsetZk = kafkaService.getOffset(clusterAlias, topic, group, partition);
							}
							offsetSQLite.setGroup(group);
							offsetSQLite.setCreated(statsPerDate);
							offsetSQLite.setTopic(topic);
							if (logSize == 0) {
								offsetSQLite.setLag(0L + offsetSQLite.getLag());
							} else {
								long lag = offsetSQLite.getLag() + (offsetZk.getOffset() == -1 ? 0 : logSize - offsetZk.getOffset());
								offsetSQLite.setLag(lag);
							}
							offsetSQLite.setLogSize(logSize + offsetSQLite.getLogSize());
							offsetSQLite.setOffsets(offsetZk.getOffset() + offsetSQLite.getOffsets());
						}
						offsetLites.add(offsetSQLite);
					}
				}

				// Monitor consumer topic rate min/per
				// zkService.insert(clusterAlias, offsetLites);

				alert(clusterAlias, offsetLites);
			} catch (Exception ex) {
				LOG.error("Quartz statistics offset has error,msg is " + ex.getMessage());
				ex.printStackTrace();
			}
		}

		private void alert(String clusterAlias, List<OffsetsLiteInfo> offsetLites) {
			for (OffsetsLiteInfo offset : offsetLites) {
				Map<String, Object> params = new HashMap<>();
				params.put("cluster", clusterAlias);
				params.put("group", offset.getGroup());
				params.put("topic", offset.getTopic());

				AlertServiceImpl alertService = StartupListener.getBean("alertServiceImpl", AlertServiceImpl.class);
				AlertInfo alertInfo = alertService.findAlertByCGT(params);

				if (alertInfo != null && offset.getLag() > alertInfo.getLag()) {
					// Mail
					try {
						MailProvider provider = new MailFactory();
						String subject = "Kafka Eagle Consumer Alert";
						String address = alertInfo.getOwner();
						LagContentModule lcm = new LagContentModule();
						lcm.setCluster(clusterAlias);
						lcm.setConsumerLag(offset.getLag() + "");
						lcm.setGroup(alertInfo.getGroup());
						lcm.setLagThreshold(alertInfo.getLag() + "");
						lcm.setTime(CalendarUtils.getDate());
						lcm.setTopic(alertInfo.getTopic());
						lcm.setType("Consumer");
						lcm.setUser(alertInfo.getOwner());
						provider.create().send(subject, address, lcm.toString(), "");
					} catch (Exception ex) {
						ex.printStackTrace();
						LOG.error("Topic[" + alertInfo.getTopic() + "] Send alarm mail has error,msg is " + ex.getMessage());
					}

					// IM (WeChat & DingDing)
					try {
						IMProvider provider = new IMFactory();
						LagContentModule lcm = new LagContentModule();
						lcm.setCluster(clusterAlias);
						lcm.setConsumerLag(offset.getLag() + "");
						lcm.setGroup(alertInfo.getGroup());
						lcm.setLagThreshold(alertInfo.getLag() + "");
						lcm.setTime(CalendarUtils.getDate());
						lcm.setTopic(alertInfo.getTopic());
						lcm.setType("Consumer");
						lcm.setUser(alertInfo.getOwner());
						provider.create().sendJsonMsgByWeChat(lcm.toWeChatMarkDown());
						provider.create().sendJsonMsgByDingDing(lcm.toDingDingMarkDown());
					} catch (Exception ex) {
						ex.printStackTrace();
						LOG.error("Topic[" + alertInfo.getTopic() + "] Send alarm wechat or dingding has error,msg is " + ex.getMessage());
					}
				}
			}
		}

		/** Get kafka brokers. */
		private List<String> getBrokers(String clusterAlias) {
			String brokers = kafkaService.getAllBrokersInfo(clusterAlias);
			JSONArray kafkaBrokers = JSON.parseArray(brokers);
			List<String> targets = new ArrayList<String>();
			for (Object object : kafkaBrokers) {
				JSONObject kafkaBroker = (JSONObject) object;
				String host = kafkaBroker.getString("host");
				int port = kafkaBroker.getInteger("port");
				targets.add(host + ":" + port);
			}
			return targets;
		}

		private OffsetZkInfo getKafkaOffset(String clusterAlias, String bootstrapServers, String topic, String group, int partition) {
			JSONArray kafkaOffsets = JSON.parseArray(kafkaService.getKafkaOffset(clusterAlias));
			OffsetZkInfo targets = new OffsetZkInfo();
			for (Object object : kafkaOffsets) {
				JSONObject kafkaOffset = (JSONObject) object;
				String _topic = kafkaOffset.getString("topic");
				String _group = kafkaOffset.getString("group");
				int _partition = kafkaOffset.getInteger("partition");
				long timestamp = kafkaOffset.getLong("timestamp");
				long offset = kafkaOffset.getLong("offset");
				if (topic.equals(_topic) && group.equals(_group) && partition == _partition) {
					targets.setOffset(offset);
					targets.setCreate(CalendarUtils.convertUnixTime2Date(timestamp));
					targets.setModify(CalendarUtils.convertUnixTime2Date(timestamp));
				}
			}
			return targets;
		}

		/** Get the corresponding string per minute. */
		private String getStatsPerDate() {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			return df.format(new Date());
		}
	}

	class Cluster {

		public void cluster() {
			AlertServiceImpl alertService = StartupListener.getBean("alertServiceImpl", AlertServiceImpl.class);
			for (ClustersInfo cluster : alertService.historys()) {
				String[] servers = cluster.getServer().split(",");
				for (String server : servers) {
					String host = server.split(":")[0];
					int port = 0;
					try {
						port = Integer.parseInt(server.split(":")[1]);
						boolean status = NetUtils.telnet(host, port);
						if (!status) {
							// Mail
							try {
								MailProvider provider = new MailFactory();
								String subject = "Kafka Eagle Alert";
								ClusterContentModule ccm = new ClusterContentModule();
								ccm.setCluster(cluster.getCluster());
								ccm.setServer(host + ":" + port);
								ccm.setTime(CalendarUtils.getDate());
								ccm.setType(cluster.getType());
								ccm.setUser(cluster.getOwner());
								provider.create().send(subject, cluster.getOwner(), ccm.toString(), "");
							} catch (Exception ex) {
								ex.printStackTrace();
								LOG.error("Alertor[" + cluster.getOwner() + "] Send alarm mail has error,msg is " + ex.getMessage());
							}

							// IM (WeChat & DingDing)
							try {
								IMProvider provider = new IMFactory();
								ClusterContentModule ccm = new ClusterContentModule();
								ccm.setCluster(cluster.getCluster());
								ccm.setServer(host + ":" + port);
								ccm.setTime(CalendarUtils.getDate());
								ccm.setType(cluster.getType());
								ccm.setUser(cluster.getOwner());
								provider.create().sendJsonMsgByDingDing(ccm.toDingDingMarkDown());
								provider.create().sendJsonMsgByWeChat(ccm.toWeChatMarkDown());
							} catch (Exception ex) {
								ex.printStackTrace();
								LOG.error("Send alarm wechat or dingding has error,msg is " + ex.getMessage());
							}
						}
					} catch (Exception e) {
						LOG.error("Parse port[" + server.split(":")[1] + "] has error, msg is " + e.getMessage());
					}
				}
			}
		}
	}

}
