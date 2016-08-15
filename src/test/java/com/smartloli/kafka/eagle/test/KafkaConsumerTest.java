package com.smartloli.kafka.eagle.test;

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
 * @Date Mar 14, 2016
 *
 * @Author dengjie
 *
 * @Note Use high consumer api to real-time consumer message from kafka
 */
public class KafkaConsumerTest extends Thread {

	private static final Logger LOG = Logger.getLogger(KafkaConsumerTest.class);
	private ExecutorService executor;
	private ConsumerConnector consumer;
	private static Properties props = new Properties();
	private static String topic = "test_data3";
	private static final int THREAD_PARALLEL_NUM = 6;

	static {
		props.put("group.id", "group2");
		props.put("zookeeper.connect", "master:2181");
		props.put("zookeeper.session.timeout.ms", "40000");
		props.put("zookeeper.sync.time.ms", "200");
		props.put("auto.commit.interval.ms", "1000");
		props.put("auto.offset.reset", "smallest");
		props.put("fetch.message.max.bytes", "10485760");
	}

	public static void main(String[] args) {
		KafkaConsumerTest hl = new KafkaConsumerTest();
		try {
			hl.start();
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
	}

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

	class KafkaConsumerThread implements Runnable {
		private KafkaStream<byte[], byte[]> stream;

		public KafkaConsumerThread(KafkaStream<byte[], byte[]> stream, int a_threadNumber) {
			this.stream = stream;
		}

		@Override
		public void run() {
			ConsumerIterator<byte[], byte[]> it = stream.iterator();
			while (it.hasNext()) {
				MessageAndMetadata<byte[], byte[]> mam = it.next();
				String message = new String(mam.message());
				System.out.println(message);
			}
		}
	}

}
