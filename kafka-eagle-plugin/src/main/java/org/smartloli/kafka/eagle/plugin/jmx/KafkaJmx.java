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
package org.smartloli.kafka.eagle.plugin.jmx;

import java.io.IOException;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * This class consists of static methods each of which returns one or more
 * platform MXBeans.
 * 
 * @author smartloli.
 *
 *         Created by Sep 4, 2018
 */
public class KafkaJmx {

	/** A class with only static fields and methods. */
	private KafkaJmx() {
	}

	private static String JMX = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";

	public static void main(String[] args) {
		try {
			JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX, "127.0.0.1:9999"));
			JMXConnector connector = JMXConnectorFactory.connect(jmxSeriverUrl);
			MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();

//			printBeanInfo(mbeanConnection.getMBeanInfo(getObjectName("kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec")), mbeanConnection);
			printBeanInfo(mbeanConnection.getMBeanInfo(getObjectName("kafka.log:type=Log,name=Size,topic=kv_test12,partition=0")), mbeanConnection);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void printBeanInfo(MBeanInfo mBeanInfo, MBeanServerConnection mbeanConnection) throws AttributeNotFoundException, InstanceNotFoundException, MalformedObjectNameException, MBeanException, ReflectionException, IOException {
		System.out.println(mBeanInfo.getClassName());
		for (MBeanAttributeInfo attribute : mBeanInfo.getAttributes()) {
			Object attr = mbeanConnection.getAttribute(getObjectName("kafka.log:type=Log,name=Size,topic=kv_test12,partition=0"), attribute.getName());
			System.out.println(attribute.getName() + ":" + attr.toString());
		}
		System.out.println("==============");
	}

	public static ObjectName getObjectName(String type) throws MalformedObjectNameException {
		return new ObjectName(type);
	}

}
