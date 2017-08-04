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

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.protocol.MBeanInfo;
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
	private String JMX = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";

	/** Get brokers all topics bytes in per sec. */
	public MBeanInfo bytesInPerSec(String uri) {
		String mbean = "kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec";
		return common(uri, mbean);
	}

	/** Get brokers bytes in per sec by topic. */
	public MBeanInfo bytesInPerSec(String uri, String topic) {
		String mbean = "kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec,topic=" + topic;
		return common(uri, mbean);
	}

	/** Get brokers all topics bytes out per sec. */
	public MBeanInfo bytesOutPerSec(String uri) {
		String mbean = "kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec";
		return common(uri, mbean);
	}

	/** Get brokers bytes out per sec by topic. */
	public MBeanInfo bytesOutPerSec(String uri, String topic) {
		String mbean = "kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec,topic=" + topic;
		return common(uri, mbean);
	}

	/** Get brokers all topics byte rejected per sec. */
	public MBeanInfo bytesRejectedPerSec(String uri) {
		String mbean = "kafka.server:type=BrokerTopicMetrics,name=BytesRejectedPerSec";
		return common(uri, mbean);
	}

	/** Get brokers byte rejected per sec by topic. */
	public MBeanInfo bytesRejectedPerSec(String uri, String topic) {
		String mbean = "kafka.server:type=BrokerTopicMetrics,name=BytesRejectedPerSec,topic=" + topic;
		return common(uri, mbean);
	}

	/** Get brokers all topic failed fetch request per sec. */
	public MBeanInfo failedFetchRequestsPerSec(String uri) {
		String mbean = "kafka.server:type=BrokerTopicMetrics,name=FailedFetchRequestsPerSec";
		return common(uri, mbean);
	}

	/** Get brokers failed fetch request per sec by topic. */
	public MBeanInfo failedFetchRequestsPerSec(String uri, String topic) {
		String mbean = "kafka.server:type=BrokerTopicMetrics,name=FailedFetchRequestsPerSec,topic=" + topic;
		return common(uri, mbean);
	}

	/** Get brokers all topics failed fetch produce request per sec. */
	public MBeanInfo failedProduceRequestsPerSec(String uri) {
		String mbean = "kafka.server:type=BrokerTopicMetrics,name=FailedProduceRequestsPerSec";
		return common(uri, mbean);
	}

	/** Get brokers failed fetch produce request per sec by topic. */
	public MBeanInfo failedProduceRequestsPerSec(String uri, String topic) {
		String mbean = "kafka.server:type=BrokerTopicMetrics,name=FailedProduceRequestsPerSec,topic=" + topic;
		return common(uri, mbean);
	}

	/** Get brokers topic all partitions log end offset. */
	public Map<Integer, Long> logEndOffset(String uri, String topic) {
		String mbean = "kafka.log:type=Log,name=LogEndOffset,topic=" + topic + ",partition=*";
		JMXConnector connector = null;
		Map<Integer, Long> endOffsets = new HashMap<>();
		try {
			JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX, uri));
			connector = JMXConnectorFactory.connect(jmxSeriverUrl);
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
			try {
				if (connector != null) {
					connector.close();
				}
			} catch (Exception e) {
				LOG.error("Close JMXConnector[" + uri + "] has error,msg is " + e.getMessage());
			}
		}
		return endOffsets;
	}

	/** Get brokers all topics message in per sec. */
	public MBeanInfo messagesInPerSec(String uri) {
		String mbean = "kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec";
		return common(uri, mbean);
	}

	/** Get brokers message in per sec by topic. */
	public MBeanInfo messagesInPerSec(String uri, String topic) {
		String mbean = "kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec,topic=" + topic;
		return common(uri, mbean);
	}

	private MBeanInfo common(String uri, String mbean) {
		JMXConnector connector = null;
		MBeanInfo MBeanInfo = new MBeanInfo();
		try {
			JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX, uri));
			connector = JMXConnectorFactory.connect(jmxSeriverUrl);
			MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
			Object fifteenMinuteRate = mbeanConnection.getAttribute(new ObjectName(mbean), MBean.FIFTEEN_MINUTE_RATE);
			Object fiveMinuteRate = mbeanConnection.getAttribute(new ObjectName(mbean), MBean.FIVE_MINUTE_RATE);
			Object meanRate = mbeanConnection.getAttribute(new ObjectName(mbean), MBean.MEAN_RATE);
			Object oneMinuteRate = mbeanConnection.getAttribute(new ObjectName(mbean), MBean.ONE_MINUTE_RATE);
			MBeanInfo.setFifteenMinute(fifteenMinuteRate.toString());
			MBeanInfo.setFiveMinute(fiveMinuteRate.toString());
			MBeanInfo.setMeanRate(meanRate.toString());
			MBeanInfo.setOneMinute(oneMinuteRate.toString());
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
		return MBeanInfo;
	}

}
