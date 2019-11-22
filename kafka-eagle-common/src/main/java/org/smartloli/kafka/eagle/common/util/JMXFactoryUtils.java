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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
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

	public static JMXConnector connectWithTimeout(final JMXServiceURL url, long timeout, TimeUnit unit) throws IOException {
		final BlockingQueue<Object> blockQueue = new ArrayBlockingQueue<Object>(1);
		ExecutorService executor = Executors.newSingleThreadExecutor(daemonThreadFactory);
		executor.submit(new Runnable() {
			public void run() {
				try {
					JMXConnector connector = JMXConnectorFactory.connect(url);
					if (!blockQueue.offer(connector))
						connector.close();
				} catch (Throwable t) {
					blockQueue.offer(t);
				}
			}
		});
		Object result;
		try {
			result = blockQueue.poll(timeout, unit);
			if (result == null) {
				if (!blockQueue.offer(""))
					result = blockQueue.take();
			}
		} catch (InterruptedException e) {
			throw initCause(new InterruptedIOException(e.getMessage()), e);
		} finally {
			executor.shutdown();
		}
		if (result == null)
			throw new SocketTimeoutException("Kafka JMX Connect timed out: " + url);
		if (result instanceof JMXConnector)
			return (JMXConnector) result;
		try {
			throw (Throwable) result;
		} catch (IOException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Error e) {
			throw e;
		} catch (Throwable e) {
			throw new IOException(e.toString(), e);
		}
	}

	private static <T extends Throwable> T initCause(T wrapper, Throwable wrapped) {
		wrapper.initCause(wrapped);
		return wrapper;
	}

	private static class DaemonThreadFactory implements ThreadFactory {
		public Thread newThread(Runnable r) {
			Thread t = Executors.defaultThreadFactory().newThread(r);
			t.setDaemon(true);
			return t;
		}
	}

}
