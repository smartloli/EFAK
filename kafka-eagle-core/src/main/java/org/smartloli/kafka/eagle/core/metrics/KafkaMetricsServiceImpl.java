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
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.config.SaslConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.constant.JmxConstants.KafkaLog;
import org.smartloli.kafka.eagle.common.protocol.MetadataInfo;
import org.smartloli.kafka.eagle.common.protocol.PropertyInfo;
import org.smartloli.kafka.eagle.common.util.JMXFactoryUtils;
import org.smartloli.kafka.eagle.common.util.KafkaZKPoolUtils;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.Mx4jServiceImpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import kafka.admin.AdminUtils;
import kafka.server.ConfigType;
import kafka.zk.KafkaZkClient;

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

	@Override
	public String topicSize(String clusterAlias, String topic) {
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
				String objectName = String.format(KafkaLog.size, topic, leader.getPartitionId());
				Object size = mbeanConnection.getAttribute(new ObjectName(objectName), KafkaLog.value);
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

		return StrUtils.stringify(tpSize);
	}

	@Override
	public String changeTopicConfig(String clusterAlias, String topic, PropertyInfo property) {
		Properties prop = new Properties();
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, parseBrokerServer(clusterAlias));
		if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".kafka.eagle.sasl.enable")) {
			sasl(prop, getKafkaBrokerServer(clusterAlias), clusterAlias);
		}
		try {
			AdminClient adminClient = AdminClient.create(prop);
			//adminClient.alterConfigs(configs)
//			AdminUtils.fetchEntityConfig(adminClient., ConfigType.Topic(), topic);
			KafkaZkClient kafkaZkCli = kafkaZKPool.getZkClient(clusterAlias);
		}catch (Exception e) {
		}
		return null;
	}

	@Override
	public String getTopicConfig(String clusterAlias, String topic) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTopicConfig(String clusterAlias, String topic, PropertyInfo property) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private String parseBrokerServer(String clusterAlias) {
		String brokerServer = "";
		JSONArray brokers = JSON.parseArray(kafkaService.getAllBrokersInfo(clusterAlias));
		for (Object object : brokers) {
			JSONObject broker = (JSONObject) object;
			brokerServer += broker.getString("host") + ":" + broker.getInteger("port") + ",";
		}
		if ("".equals(brokerServer)) {
			return "";
		}
		return brokerServer.substring(0, brokerServer.length() - 1);
	}
	
	private void sasl(Properties props, String bootstrapServers, String clusterAlias) {
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.sasl.protocol"));
		props.put(SaslConfigs.SASL_MECHANISM, SystemConfigUtils.getProperty(clusterAlias + ".kafka.eagle.sasl.mechanism"));
	}

	public String getKafkaBrokerServer(String clusterAlias) {
		return parseBrokerServer(clusterAlias);
	}
}
