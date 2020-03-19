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
/**
 * 
 */
package org.smartloli.kafka.eagle.core.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.constant.JmxConstants.BrokerServer;
import org.smartloli.kafka.eagle.common.protocol.MBeanInfo;
import org.smartloli.kafka.eagle.common.util.JMXFactoryUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.MBean;

/**
 * Implements Mx4jService all method.
 * 
 * @author smartloli.
 *
 *         Created by Jul 14, 2017
 */
public class Mx4jServiceImpl implements Mx4jService {

	private Logger LOG = LoggerFactory.getLogger(Mx4jServiceImpl.class);
	private static final String JMX = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";
	private static final String TOPIC_CONCAT_CHARACTER = ",topic=";

	/** Get brokers all topics bytes in per sec. */
	@Override
	public MBeanInfo bytesInPerSec(String uri) {
		return common(uri, BrokerServer.BYTES_IN_PER_SEC.getValue());
	}

	/** Get brokers bytes in per sec by topic. */
	@Override
	public MBeanInfo bytesInPerSec(String uri, String topic) {
		String mbean = BrokerServer.BYTES_IN_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic;
		return common(uri, mbean);
	}

	/** Get brokers all topics bytes out per sec. */
	@Override
	public MBeanInfo bytesOutPerSec(String uri) {
		return common(uri, BrokerServer.BYTES_OUT_PER_SEC.getValue());
	}

	/** Get brokers bytes out per sec by topic. */
	@Override
	public MBeanInfo bytesOutPerSec(String uri, String topic) {
		String mbean = BrokerServer.BYTES_OUT_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic;
		return common(uri, mbean);
	}

	/** Get brokers all topics byte rejected per sec. */
	@Override
	public MBeanInfo bytesRejectedPerSec(String uri) {
		return common(uri, BrokerServer.BYTES_REJECTED_PER_SEC.getValue());
	}

	/** Get brokers byte rejected per sec by topic. */
	@Override
	public MBeanInfo bytesRejectedPerSec(String uri, String topic) {
		String mbean = BrokerServer.BYTES_REJECTED_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic;
		return common(uri, mbean);
	}

	/** Get brokers all topic failed fetch request per sec. */
	@Override
	public MBeanInfo failedFetchRequestsPerSec(String uri) {
		return common(uri, BrokerServer.FAILED_FETCH_REQUESTS_PER_SEC.getValue());
	}

	/** Get brokers failed fetch request per sec by topic. */
	@Override
	public MBeanInfo failedFetchRequestsPerSec(String uri, String topic) {
		String mbean = BrokerServer.FAILED_FETCH_REQUESTS_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic;
		return common(uri, mbean);
	}

	/** Get brokers all topics failed fetch produce request per sec. */
	@Override
	public MBeanInfo failedProduceRequestsPerSec(String uri) {
		return common(uri, BrokerServer.FAILED_PRODUCE_REQUESTS_PER_SEC.getValue());
	}

	/** Get brokers failed fetch produce request per sec by topic. */
	@Override
	public MBeanInfo failedProduceRequestsPerSec(String uri, String topic) {
		String mbean = BrokerServer.FAILED_PRODUCE_REQUESTS_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic;
		return common(uri, mbean);
	}

	/** Get brokers topic all partitions log end offset. */
	@Override
	public Map<Integer, Long> logEndOffset(String uri, String topic) {
		String mbean = "kafka.log:type=Log,name=LogEndOffset,topic=" + topic + ",partition=*";
		JMXConnector connector = null;
		Map<Integer, Long> endOffsets = new HashMap<>();
		try {
			JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX, uri));
			connector = JMXFactoryUtils.connectWithTimeout(jmxSeriverUrl, 30, TimeUnit.SECONDS);
			MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
			Set<ObjectName> objectNames = mbeanConnection.queryNames(new ObjectName(mbean), null);
			for (ObjectName objectName : objectNames) {
				int partition = Integer.valueOf(objectName.getKeyProperty("partition"));
				Object value = mbeanConnection.getAttribute(new ObjectName(mbean), MBean.VALUE);
				if (value != null) {
					endOffsets.put(partition, Long.valueOf(value.toString()));
				}
			}
		} catch (Exception e) {
			LOG.error("JMX service url[" + uri + "] create has error,msg is " + e.getMessage());
		} finally {
			if (connector != null) {
				try {
					connector.close();
				} catch (Exception e) {
					LOG.error("Close JMXConnector[" + uri + "] has error,msg is " + e.getMessage());
				}
			}
		}
		return endOffsets;
	}

	/** Get brokers all topics message in per sec. */
	@Override
	public MBeanInfo messagesInPerSec(String uri) {
		return common(uri, BrokerServer.MESSAGES_IN_PER_SEC.getValue());
	}

	/** Get brokers message in per sec by topic. */
	@Override
	public MBeanInfo messagesInPerSec(String uri, String topic) {
		String mbean = BrokerServer.MESSAGES_IN_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic;
		return common(uri, mbean);
	}

	@Override
	public MBeanInfo produceMessageConversionsPerSec(String uri) {
		return common(uri, BrokerServer.PRODUCE_MESSAGE_CONVERSIONS_PER_SEC.getValue());
	}

	@Override
	public MBeanInfo produceMessageConversionsPerSec(String uri, String topic) {
		String mbean = BrokerServer.PRODUCE_MESSAGE_CONVERSIONS_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic;
		return common(uri, mbean);
	}

	@Override
	public MBeanInfo totalFetchRequestsPerSec(String uri) {
		return common(uri, BrokerServer.TOTAL_FETCH_REQUESTS_PER_SEC.getValue());
	}

	@Override
	public MBeanInfo totalFetchRequestsPerSec(String uri, String topic) {
		String mbean = BrokerServer.TOTAL_FETCH_REQUESTS_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic;
		return common(uri, mbean);
	}

	@Override
	public MBeanInfo totalProduceRequestsPerSec(String uri) {
		return common(uri, BrokerServer.TOTAL_PRODUCE_REQUESTS_PER_SEC.getValue());
	}

	@Override
	public MBeanInfo totalProduceRequestsPerSec(String uri, String topic) {
		String mbean = BrokerServer.TOTAL_PRODUCE_REQUESTS_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic;
		return common(uri, mbean);
	}

	@Override
	public MBeanInfo replicationBytesInPerSec(String uri) {
		return common(uri, BrokerServer.REPLICATION_BYTES_IN_PER_SEC.getValue());
	}

	@Override
	public MBeanInfo replicationBytesInPerSec(String uri, String topic) {
		String mbean =  BrokerServer.REPLICATION_BYTES_IN_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic;
		return common(uri, mbean);
	}

	@Override
	public MBeanInfo replicationBytesOutPerSec(String uri) {
		return common(uri, BrokerServer.REPLICATION_BYTES_OUT_PER_SEC.getValue());
	}

	@Override
	public MBeanInfo replicationBytesOutPerSec(String uri, String topic) {
		String mbean = BrokerServer.REPLICATION_BYTES_OUT_PER_SEC.getValue() + TOPIC_CONCAT_CHARACTER + topic;
		return common(uri, mbean);
	}

	/**
	 * Before Kafka 0.11.x, some exceptions are thrown, such as
	 * {@link ReplicationBytesOutPerSec} Exception.
	 */
	private MBeanInfo common(String uri, String mbean) {
		JMXConnector connector = null;
		MBeanInfo mbeanInfo = new MBeanInfo();
		try {
			JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX, uri));
			connector = JMXFactoryUtils.connectWithTimeout(jmxSeriverUrl, 30, TimeUnit.SECONDS);
			MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
			if (mbeanConnection.isRegistered(new ObjectName(mbean))) {
				Object fifteenMinuteRate = mbeanConnection.getAttribute(new ObjectName(mbean), MBean.FIFTEEN_MINUTE_RATE);
				Object fiveMinuteRate = mbeanConnection.getAttribute(new ObjectName(mbean), MBean.FIVE_MINUTE_RATE);
				Object meanRate = mbeanConnection.getAttribute(new ObjectName(mbean), MBean.MEAN_RATE);
				Object oneMinuteRate = mbeanConnection.getAttribute(new ObjectName(mbean), MBean.ONE_MINUTE_RATE);
				mbeanInfo.setFifteenMinute(fifteenMinuteRate.toString());
				mbeanInfo.setFiveMinute(fiveMinuteRate.toString());
				mbeanInfo.setMeanRate(meanRate.toString());
				mbeanInfo.setOneMinute(oneMinuteRate.toString());
			} else {
				mbeanInfo.setFifteenMinute("0.0");
				mbeanInfo.setFiveMinute("0.0");
				mbeanInfo.setMeanRate("0.0");
				mbeanInfo.setOneMinute("0.0");
			}
		} catch (Exception e) {
			LOG.error("JMX service url[" + uri + "] create has error,msg is " + e.getMessage());
			e.printStackTrace();
			mbeanInfo.setFifteenMinute("0.0");
			mbeanInfo.setFiveMinute("0.0");
			mbeanInfo.setMeanRate("0.0");
			mbeanInfo.setOneMinute("0.0");
		} finally {
			if (connector != null) {
				try {
					connector.close();
				} catch (Exception e) {
					LOG.error("Close JMXConnector[" + uri + "] has error,msg is " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		return mbeanInfo;
	}

}
