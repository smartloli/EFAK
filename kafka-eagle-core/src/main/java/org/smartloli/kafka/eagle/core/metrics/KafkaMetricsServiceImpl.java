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
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.constant.JmxConstants.KafkaLog;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.Mx4jServiceImpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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
	
	@Override
	public String topicSize(String clusterAlias, String topic) {
		JSONArray brokers = JSON.parseArray(kafkaService.getAllBrokersInfo(clusterAlias));
		List<String> jnis = new ArrayList<>();
		for (Object object : brokers) {
			JSONObject broker = (JSONObject) object;
			String jni = broker.getString("host") + ":" + broker.getInteger("jmxPort");
			jnis.add(jni);
		}
		String jmx = "";
		if (jnis.size() > 0) {
			jmx = String.format(JMX, jnis.get(0));
		}

		List<String> partitions = kafkaService.findTopicPartition(clusterAlias, topic);

		JMXConnector connector = null;
		long tpSize = 0L;
		try {
			JMXServiceURL jmxSeriverUrl = new JMXServiceURL(jmx);
			connector = JMXConnectorFactory.connect(jmxSeriverUrl);
			MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
			for (String partition : partitions) {
				String objectName = String.format(KafkaLog.size, topic, partition);
				Object size = mbeanConnection.getAttribute(new ObjectName(objectName), KafkaLog.value);
				tpSize += Long.parseLong(size.toString());
			}
		} catch (Exception ex) {
			LOG.error("Get topic size from jmx has error, msg is " + ex.getMessage());
		} finally {
			if (connector != null) {
				try {
					connector.close();
				} catch (IOException e) {
					LOG.error("Close jmx connector has error, msg is " + e.getMessage());
				}
			}
		}

		return StrUtils.stringify(tpSize * jnis.size());
	}

}
