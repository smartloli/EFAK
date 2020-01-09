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
package org.smartloli.kafka.eagle.core.metrics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigsResult;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.DescribeLogDirsResult;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.ConfigResource.Type;
import org.apache.kafka.common.requests.DescribeLogDirsResponse;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.constant.JmxConstants.KafkaLog;
import org.smartloli.kafka.eagle.common.protocol.BrokersInfo;
import org.smartloli.kafka.eagle.common.protocol.MetadataInfo;
import org.smartloli.kafka.eagle.common.util.JMXFactoryUtils;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.KConstants.Kafka;
import org.smartloli.kafka.eagle.common.util.KConstants.Topic;
import org.smartloli.kafka.eagle.common.util.KafkaZKPoolUtils;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.Mx4jServiceImpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import kafka.zk.KafkaZkClient;
import scala.Option;
import scala.Tuple2;

/**
 * Implements KafkaMetricsService all methods.
 * 
 * @author smartloli.
 *
 *         Created by Oct 26, 2018
 */
public class KafkaMetricsServiceImpl implements KafkaMetricsService {

	private Logger LOG = LoggerFactory.getLogger(Mx4jServiceImpl.class);
	private String JMX = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** Instance Kafka Zookeeper client pool. */
	private KafkaZKPoolUtils kafkaZKPool = KafkaZKPoolUtils.getInstance();

	/** Get topic config path in zookeeper. */
	private final String CONFIG_TOPIC_PATH = "/config/topics/";

	public JSONObject topicKafkaCapacity(String clusterAlias, String topic) {
		if (Kafka.CONSUMER_OFFSET_TOPIC.equals(topic)) {
			return new JSONObject();
		}
		Properties prop = new Properties();
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, parseBrokerServer(clusterAlias));

		if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
			kafkaService.sasl(prop, clusterAlias);
		}
		long sum = 0L;
		AdminClient adminClient = null;
		try {
			adminClient = AdminClient.create(prop);
			List<MetadataInfo> leaders = kafkaService.findKafkaLeader(clusterAlias, topic);
			Set<Integer> ids = new HashSet<>();
			for (MetadataInfo metadata : leaders) {
				ids.add(metadata.getLeader());
			}
			DescribeLogDirsResult logSizeBytes = adminClient.describeLogDirs(ids);
			Map<Integer, Map<String, DescribeLogDirsResponse.LogDirInfo>> tmp = logSizeBytes.all().get();
			if (tmp == null) {
				return new JSONObject();
			}

			for (Map.Entry<Integer, Map<String, DescribeLogDirsResponse.LogDirInfo>> entry : tmp.entrySet()) {
				Map<String, DescribeLogDirsResponse.LogDirInfo> logDirInfos = entry.getValue();
				for (Map.Entry<String, DescribeLogDirsResponse.LogDirInfo> logDirInfo : logDirInfos.entrySet()) {
					DescribeLogDirsResponse.LogDirInfo info = logDirInfo.getValue();
					Map<TopicPartition, DescribeLogDirsResponse.ReplicaInfo> replicaInfoMap = info.replicaInfos;
					for (Map.Entry<TopicPartition, DescribeLogDirsResponse.ReplicaInfo> replicas : replicaInfoMap.entrySet()) {
						if (topic.equals(replicas.getKey().topic())) {
							sum += replicas.getValue().size;
						}
					}
				}
			}

		} catch (Exception e) {
			LOG.error("Get topic capacity has error, msg is " + e.getCause().getMessage());
			e.printStackTrace();
		} finally {
			adminClient.close();
		}
		return StrUtils.stringifyByObject(sum);
	}

	/** Get topic size from kafka jmx. */
	public JSONObject topicSize(String clusterAlias, String topic) {
		String jmx = "";
		JMXConnector connector = null;
		List<MetadataInfo> leaders = kafkaService.findKafkaLeader(clusterAlias, topic);
		long tpSize = 0L;
		for (MetadataInfo leader : leaders) {
			String jni = kafkaService.getBrokerJMXFromIds(clusterAlias, leader.getLeader());
			jmx = String.format(JMX, jni);
			try {
				JMXServiceURL jmxSeriverUrl = new JMXServiceURL(jmx);
				connector = JMXFactoryUtils.connectWithTimeout(jmxSeriverUrl, 30, TimeUnit.SECONDS);
				MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
				String objectName = String.format(KafkaLog.SIZE.getValue(), topic, leader.getPartitionId());
				Object size = mbeanConnection.getAttribute(new ObjectName(objectName), KafkaLog.VALUE.getValue());
				tpSize += Long.parseLong(size.toString());
			} catch (Exception ex) {
				LOG.error("Get topic size from jmx has error, msg is " + ex.getMessage());
				ex.printStackTrace();
			} finally {
				if (connector != null) {
					try {
						connector.close();
					} catch (IOException e) {
						LOG.error("Close jmx connector has error, msg is " + e.getMessage());
					}
				}
			}
		}

		return StrUtils.stringifyByObject(tpSize);
	}

	/** Alter topic config. */
	public String changeTopicConfig(String clusterAlias, String topic, String type, ConfigEntry configEntry) {
		JSONObject object = new JSONObject();
		Properties prop = new Properties();
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, parseBrokerServer(clusterAlias));
		if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
			kafkaService.sasl(prop, clusterAlias);
		}
		try {
			switch (type) {
			case Topic.ADD:
				AdminClient adminClientAdd = AdminClient.create(prop);
				object.put("type", type);
				object.put("value", addTopicConfig(clusterAlias, adminClientAdd, topic, configEntry));
				adminClientAdd.close();
				break;
			case Topic.DELETE:
				AdminClient adminClientDelete = AdminClient.create(prop);
				object.put("type", type);
				object.put("value", deleteTopicConfig(clusterAlias, adminClientDelete, topic, configEntry));
				adminClientDelete.close();
				break;
			case Topic.DESCRIBE:
				object.put("type", type);
				object.put("value", describeTopicConfig(clusterAlias, topic));
				break;
			default:
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Type[" + type + "] topic config has error, msg is " + e.getMessage());
		}
		return object.toJSONString();
	}

	private String addTopicConfig(String clusterAlias, AdminClient adminClient, String topic, ConfigEntry configEntry) {
		try {
			String describeTopicConfigs = describeTopicConfig(clusterAlias, topic);
			JSONObject object = JSON.parseObject(describeTopicConfigs).getJSONObject("config");
			if (object.containsKey(configEntry.name())) {
				object.remove(configEntry.name());
			}
			List<ConfigEntry> configEntrys = new ArrayList<>();
			for (String key : KConstants.Topic.getTopicConfigKeys()) {
				if (object.containsKey(key)) {
					configEntrys.add(new ConfigEntry(key, object.getString(key)));
				}
			}
			configEntrys.add(configEntry);
			Map<ConfigResource, Config> configs = new HashMap<>();
			ConfigResource configRes = new ConfigResource(Type.TOPIC, topic);
			Config config = new Config(configEntrys);
			configs.put(configRes, config);
			AlterConfigsResult alterConfig = adminClient.alterConfigs(configs);
			alterConfig.all().get();
			return KConstants.Topic.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Add topic[" + topic + "] config has error, msg is " + e.getMessage());
			return e.getMessage();
		}
	}

	private String deleteTopicConfig(String clusterAlias, AdminClient adminClient, String topic, ConfigEntry configEntry) {
		try {
			String describeTopicConfigs = describeTopicConfig(clusterAlias, topic);
			JSONObject object = JSON.parseObject(describeTopicConfigs).getJSONObject("config");
			object.remove(configEntry.name());
			List<ConfigEntry> configEntrys = new ArrayList<>();
			for (String key : KConstants.Topic.getTopicConfigKeys()) {
				if (object.containsKey(key)) {
					configEntrys.add(new ConfigEntry(key, object.getString(key)));
				}
			}
			Map<ConfigResource, Config> configs = new HashMap<>();
			ConfigResource configRes = new ConfigResource(Type.TOPIC, topic);
			Config config = new Config(configEntrys);
			configs.put(configRes, config);
			adminClient.alterConfigs(configs);
			return KConstants.Topic.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Delete topic[" + topic + "] config has error, msg is " + e.getMessage());
			return e.getMessage();
		}
	}

	private String describeTopicConfig(String clusterAlias, String topic) {
		String target = "";
		try {
			KafkaZkClient kafkaZkCli = kafkaZKPool.getZkClient(clusterAlias);
			if (kafkaZkCli.pathExists(CONFIG_TOPIC_PATH + topic)) {
				Tuple2<Option<byte[]>, Stat> tuple = kafkaZkCli.getDataAndStat(CONFIG_TOPIC_PATH + topic);
				target = new String(tuple._1.get());
			}
			if (kafkaZkCli != null) {
				kafkaZKPool.release(clusterAlias, kafkaZkCli);
				kafkaZkCli = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Describe topic[" + topic + "] config has error, msg is " + e.getMessage());
		}
		return target;
	}

	private String parseBrokerServer(String clusterAlias) {
		String brokerServer = "";
		List<BrokersInfo> brokers = kafkaService.getAllBrokersInfo(clusterAlias);
		for (BrokersInfo broker : brokers) {
			brokerServer += broker.getHost() + ":" + broker.getPort() + ",";
		}
		if ("".equals(brokerServer)) {
			return "";
		}
		return brokerServer.substring(0, brokerServer.length() - 1);
	}

	public String getKafkaBrokerServer(String clusterAlias) {
		return parseBrokerServer(clusterAlias);
	}

	/** Get kafka topic capacity size . */
	public long topicCapacity(String clusterAlias, String topic) {
		String jmx = "";
		JMXConnector connector = null;
		List<MetadataInfo> leaders = kafkaService.findKafkaLeader(clusterAlias, topic);
		long tpSize = 0L;
		for (MetadataInfo leader : leaders) {
			String jni = kafkaService.getBrokerJMXFromIds(clusterAlias, leader.getLeader());
			jmx = String.format(JMX, jni);
			try {
				JMXServiceURL jmxSeriverUrl = new JMXServiceURL(jmx);
				connector = JMXFactoryUtils.connectWithTimeout(jmxSeriverUrl, 30, TimeUnit.SECONDS);
				MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
				String objectName = String.format(KafkaLog.SIZE.getValue(), topic, leader.getPartitionId());
				Object size = mbeanConnection.getAttribute(new ObjectName(objectName), KafkaLog.VALUE.getValue());
				tpSize += Long.parseLong(size.toString());
			} catch (Exception ex) {
				LOG.error("Get topic size from jmx has error, msg is " + ex.getMessage());
				ex.printStackTrace();
			} finally {
				if (connector != null) {
					try {
						connector.close();
					} catch (IOException e) {
						LOG.error("Close jmx connector has error, msg is " + e.getMessage());
					}
				}
			}
		}

		return tpSize;
	}
}
