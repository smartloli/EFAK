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
package org.smartloli.kafka.eagle.consumer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;

/**
 * Use high consumer api to real-time consumer message from kafka8
 *
 * @author smartloli.
 *
 *         Created by Mar 14, 2016
 */
public class TestConsumer8 extends Thread {

	private static final Logger LOG = Logger.getLogger(TestConsumer8.class);
	private ExecutorService executor;
	private ConsumerConnector consumer;
	private static Properties props = new Properties();
	private static String topic = "ke_test1";// ke_test1
	private static final int THREAD_PARALLEL_NUM = 1;

	static {
		props.put("group.id", "group1");
		props.put("zookeeper.connect", "master:2181");
		props.put("zookeeper.session.timeout.ms", "40000");
		props.put("zookeeper.sync.time.ms", "200");
		props.put("auto.commit.interval.ms", "1000");
		props.put("auto.offset.reset", "smallest");
		props.put("fetch.message.max.bytes", "10485760");
	}

	public static void main(String[] args) {
		TestConsumer8 hl = new TestConsumer8();
		try {
			hl.start();
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
	}

	/** Close kafka consumer client. */
	public void shutdown() {
		if (consumer != null) {
			consumer.shutdown();
		}
		if (executor != null) {
			executor.shutdown();
		}
		try {
			if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
				LOG.info("Timed out waiting for consumer threads to shut down, exiting uncleanly");
			}
		} catch (InterruptedException e) {
			LOG.error("Interrupted during shutdown, exiting uncleanly");
		}
	}

	/** Run kafka consumer thread. */
	@Override
	public void run() {
		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(topic, Integer.valueOf(THREAD_PARALLEL_NUM));
		consumer = Consumer.createJavaConsumerConnector(new ConsumerConfig(props));
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
		List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);
		executor = Executors.newFixedThreadPool(THREAD_PARALLEL_NUM);
		int threadNumber = 0;
		for (final KafkaStream<byte[], byte[]> stream : streams) {
			executor.submit(new KafkaConsumerThread(stream, threadNumber));
			threadNumber++;
		}
	}

	/** Child consumer clazz & implements Runnable clazz to start thread. */
	class KafkaConsumerThread implements Runnable {
		private KafkaStream<byte[], byte[]> stream;

		/** Consumer thread construction method. */
		public KafkaConsumerThread(KafkaStream<byte[], byte[]> stream, int a_threadNumber) {
			this.stream = stream;
		}

		/** Foreach message & print result.*/
		@Override
		public void run() {
			ConsumerIterator<byte[], byte[]> it = stream.iterator();
			while (true) {
				MessageAndMetadata<byte[], byte[]> mam = it.next();
				System.out.println(mam.message());
			}
		}
	}

}
