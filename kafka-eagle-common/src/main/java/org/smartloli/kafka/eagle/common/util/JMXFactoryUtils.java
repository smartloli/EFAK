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
package org.smartloli.kafka.eagle.common.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Manager jmx connector object && release.
 * 
 * @author smartloli.
 *
 *         Created by Feb 25, 2019
 */
public class JMXFactoryUtils {

	private static final ThreadFactory daemonThreadFactory = new DaemonThreadFactory();

	private JMXFactoryUtils() {

	}

	public static JMXConnector connectWithTimeout(final JMXServiceURL url, long timeout, TimeUnit unit) {
		final BlockingQueue<Object> blockQueue = new ArrayBlockingQueue<>(1);
		ExecutorService executor = Executors.newSingleThreadExecutor(daemonThreadFactory);
		executor.submit(new Runnable() {
			public void run() {
				try {
					JMXConnector connector = JMXConnectorFactory.connect(url);
					if (!blockQueue.offer(connector))
						connector.close();
				} catch (Exception e) {
					if (!blockQueue.offer(e)) {
						ThrowExceptionUtils.print(JMXFactoryUtils.class).error("Block queue is full, error msg is ", e);
					}
				}
			}
		});
		Object result = null;
		try {
			result = blockQueue.poll(timeout, unit);
			if (result == null && !blockQueue.offer("")) {
				result = blockQueue.take();
			}
		} catch (Exception e) {
			ThrowExceptionUtils.print(JMXFactoryUtils.class).error("Take block queue has error, msg is ", e);
		} finally {
			executor.shutdown();
		}
		return (JMXConnector) result;
	}

	private static class DaemonThreadFactory implements ThreadFactory {
		public Thread newThread(Runnable r) {
			Thread t = Executors.defaultThreadFactory().newThread(r);
			t.setDaemon(true);
			return t;
		}
	}

}
