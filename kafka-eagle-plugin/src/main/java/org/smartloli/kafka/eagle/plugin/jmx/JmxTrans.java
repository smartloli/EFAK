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
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * 
 * @author smartloli.
 *
 *         Created by Jun 12, 2018
 */
public class JmxTrans {

	private static String JMX = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";
	private static Logger LOG = LoggerFactory.getLogger(JmxTrans.class);

	public static void main(String[] args) {
		try {
			JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX, "127.0.0.1:9999"));
			JMXConnector connector = JMXConnectorFactory.connect(jmxSeriverUrl);
			MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();

			MemoryMXBean memBean = ManagementFactory.newPlatformMXBeanProxy(mbeanConnection, ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);

			System.out.println(memBean.getHeapMemoryUsage().getUsed());

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/** Get memory by address(ip:port), such as 127.0.0.1:9999 . */
	public static long jvmMem(String address) {
		long memSize = 0L;
		JMXConnector connector = null;
		try {
			JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX, address));
			connector = JMXConnectorFactory.connect(jmxSeriverUrl);
			MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
			MemoryMXBean memBean = ManagementFactory.newPlatformMXBeanProxy(mbeanConnection, ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);
			memSize = memBean.getHeapMemoryUsage().getUsed();
		} catch (Exception ex) {
			LOG.error("Get memory from jmx has error, msg is " + ex.getMessage());
		} finally {
			if (connector != null) {
				try {
					connector.close();
				} catch (IOException e) {
					LOG.error("Close memory connect has error, msg is " + e.getMessage());
				}
			}
		}

		return memSize;
	}
}
