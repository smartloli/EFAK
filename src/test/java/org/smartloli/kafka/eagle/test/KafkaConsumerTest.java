package org.smartloli.kafka.eagle.test;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.protocol.types.Schema;
import org.apache.kafka.common.protocol.types.Struct;
import org.apache.kafka.common.protocol.types.Type;
import org.apache.log4j.Logger;
import org.smartloli.kafka.eagle.domain.offsets.KeyAndValueSchemasDomain;
import org.smartloli.kafka.eagle.domain.offsets.MessageValueStructAndVersionDomain;

import kafka.common.OffsetAndMetadata;
import kafka.common.TopicAndPartition;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import kafka.server.GroupTopicPartition;

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
	private static String topic = "ke_test1";// ke_test1
	private static final int THREAD_PARALLEL_NUM = 1;

	// massive code stealing from kafka.server.OffsetManager
	private static Schema OFFSET_COMMIT_KEY_SCHEMA_V0 = new Schema(new Field("group", Type.STRING), new Field("topic", Type.STRING), new Field("partition", Type.INT32));
	private static Field KEY_GROUP_FIELD = OFFSET_COMMIT_KEY_SCHEMA_V0.get("group");
	private static Field KEY_TOPIC_FIELD = OFFSET_COMMIT_KEY_SCHEMA_V0.get("topic");
	private static Field KEY_PARTITION_FIELD = OFFSET_COMMIT_KEY_SCHEMA_V0.get("partition");

	private static Schema OFFSET_COMMIT_VALUE_SCHEMA_V0 = new Schema(new Field("offset", Type.INT64), new Field("metadata", Type.STRING, "Associated metadata.", ""), new Field("timestamp", Type.INT64));

	private static Schema OFFSET_COMMIT_VALUE_SCHEMA_V1 = new Schema(new Field("offset", Type.INT64), new Field("metadata", Type.STRING, "Associated metadata.", ""), new Field("commit_timestamp", Type.INT64),
			new Field("expire_timestamp", Type.INT64));

	private static Field VALUE_OFFSET_FIELD_V0 = OFFSET_COMMIT_VALUE_SCHEMA_V0.get("offset");
	private static Field VALUE_METADATA_FIELD_V0 = OFFSET_COMMIT_VALUE_SCHEMA_V0.get("metadata");
	private static Field VALUE_TIMESTAMP_FIELD_V0 = OFFSET_COMMIT_VALUE_SCHEMA_V0.get("timestamp");

	private static Field VALUE_OFFSET_FIELD_V1 = OFFSET_COMMIT_VALUE_SCHEMA_V1.get("offset");
	private static Field VALUE_METADATA_FIELD_V1 = OFFSET_COMMIT_VALUE_SCHEMA_V1.get("metadata");
	private static Field VALUE_COMMIT_TIMESTAMP_FIELD_V1 = OFFSET_COMMIT_VALUE_SCHEMA_V1.get("commit_timestamp");

	// map of version to schemas
	@SuppressWarnings("serial")
	private static Map<Integer, KeyAndValueSchemasDomain> OFFSET_SCHEMAS = new HashMap<Integer, KeyAndValueSchemasDomain>() {
		{
			KeyAndValueSchemasDomain ks0 = new KeyAndValueSchemasDomain();
			ks0.setKeySchema(OFFSET_COMMIT_KEY_SCHEMA_V0);
			ks0.setValueSchema(OFFSET_COMMIT_VALUE_SCHEMA_V0);
			put(0, ks0);

			KeyAndValueSchemasDomain ks1 = new KeyAndValueSchemasDomain();
			ks1.setKeySchema(OFFSET_COMMIT_KEY_SCHEMA_V0);
			ks1.setValueSchema(OFFSET_COMMIT_VALUE_SCHEMA_V1);
			put(1, ks1);
		}
	};

	static {
		props.put("group.id", "group1");
		props.put("zookeeper.connect", "slave01:2181");
//		props.put("zookeeper.session.timeout.ms", "40000");
//		props.put("zookeeper.sync.time.ms", "200");
//		props.put("auto.commit.interval.ms", "1000");
//		props.put("auto.offset.reset", "smallest");
//		props.put("fetch.message.max.bytes", "10485760");
		props.put("exclude.internal.topics", "false");
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
			while (true) {
				MessageAndMetadata<byte[], byte[]> mam = it.next();
				GroupTopicPartition commitKey = readMessageKey(ByteBuffer.wrap(mam.key()));
				OffsetAndMetadata commitValue = readMessageValue(ByteBuffer.wrap(mam.message()));
				System.out.println(commitKey + "=>" + commitValue);
			}
		}
	}

	private static KeyAndValueSchemasDomain schemaFor(int version) {
		return OFFSET_SCHEMAS.get(version);
	}

	private static GroupTopicPartition readMessageKey(ByteBuffer buffer) {
		short version = buffer.getShort();
		Schema keySchema = schemaFor(version).getKeySchema();
		Struct key = (Struct) keySchema.read(buffer);
		String group = key.getString(KEY_GROUP_FIELD);
		String topic = key.getString(KEY_TOPIC_FIELD);
		int partition = key.getInt(KEY_PARTITION_FIELD);
		return new GroupTopicPartition(group, new TopicAndPartition(topic, partition));
	}

	private static OffsetAndMetadata readMessageValue(ByteBuffer buffer) {
		MessageValueStructAndVersionDomain structAndVersion = readMessageValueStruct(buffer);
		if (structAndVersion.getValue() == null) {
			return null;
		} else {
			if (structAndVersion.getVersion() == 0) {
				long offset = structAndVersion.getValue().getLong(VALUE_OFFSET_FIELD_V0);
				String metadata = structAndVersion.getValue().getString(VALUE_METADATA_FIELD_V0);
				long timestamp = structAndVersion.getValue().getLong(VALUE_TIMESTAMP_FIELD_V0);
				return new OffsetAndMetadata(offset, metadata, timestamp);
			} else if (structAndVersion.getVersion() == 1) {
				long offset = structAndVersion.getValue().getLong(VALUE_OFFSET_FIELD_V1);
				String metadata = structAndVersion.getValue().getString(VALUE_METADATA_FIELD_V1);
				long commitTimestamp = structAndVersion.getValue().getLong(VALUE_COMMIT_TIMESTAMP_FIELD_V1);
				return new OffsetAndMetadata(offset, metadata, commitTimestamp);
			}
		}
		return null;
	}

	private static MessageValueStructAndVersionDomain readMessageValueStruct(ByteBuffer buffer) {
		MessageValueStructAndVersionDomain mvs = new MessageValueStructAndVersionDomain();
		if (buffer == null) {
			mvs.setValue(null);
			mvs.setVersion(Short.valueOf("-1"));
		} else {
			short version = buffer.getShort();
			Schema valueSchema = schemaFor(version).getValueSchema();
			Struct value = (Struct) valueSchema.read(buffer);
			mvs.setValue(value);
			mvs.setVersion(version);
		}
		return mvs;
	}

}
