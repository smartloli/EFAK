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
package org.smartloli.kafka.eagle.ipc;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.util.Constants;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;

import kafka.common.OffsetAndMetadata;
import kafka.common.Topic;
import kafka.coordinator.GroupMetadataManager;
import kafka.coordinator.GroupTopicPartition;
import kafka.coordinator.OffsetKey;

/**
 * New offset storage formats: kafka
 * 
 * @author smartloli.
 *
 *         Created by Jan 3, 2017
 */
public class TestSaslKafkaOffsetGetter extends Thread {

	private final static Logger LOG = LoggerFactory.getLogger(TestSaslKafkaOffsetGetter.class);

	/** Multi cluster information. */
	public static Map<String, Map<GroupTopicPartition, OffsetAndMetadata>> multiKafkaConsumerOffsets = new ConcurrentHashMap<>();

	/** Kafka brokers */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** Listening offset thread method. */
	private static synchronized void startOffsetListener(String clusterAlias, KafkaConsumer<String, String> consumer) {
		while (true) {
			ConsumerRecords<String, String> records = consumer.poll(Constants.Kafka.TIME_OUT);
			for (ConsumerRecord<String, String> record : records) {
				try {
					if (record.value() == null) {
						continue;
					}
					GroupTopicPartition commitKey = ((OffsetKey) GroupMetadataManager.readMessageKey(ByteBuffer.wrap(record.key().getBytes()))).key();
					OffsetAndMetadata commitValue = GroupMetadataManager.readOffsetMessageValue(ByteBuffer.wrap(record.value().getBytes()));
					if (multiKafkaConsumerOffsets.containsKey(clusterAlias)) {
					} else {
						Map<GroupTopicPartition, OffsetAndMetadata> kafkaConsumerOffsets = new ConcurrentHashMap<>();
						kafkaConsumerOffsets.put(commitKey, commitValue);
						multiKafkaConsumerOffsets.put(clusterAlias, kafkaConsumerOffsets);
					}
					System.err.println(multiKafkaConsumerOffsets);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	static {
		LOG.info("Initialize KafkaOffsetGetter clazz.");
		TestSaslKafkaOffsetGetter kafka = new TestSaslKafkaOffsetGetter();
		kafka.start();
	}

	/** Instance KafkaOffsetGetter clazz. */
	public static void getInstance() {
		LOG.info(TestSaslKafkaOffsetGetter.class.getName());
	}

	public static void main(String[] args) {
		getInstance();
	}

	/** Run method for running thread. */
	public void run() {
		String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
		for (String clusterAlias : clusterAliass) {
			Properties props = new Properties();
			props.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.Kafka.KAFKA_EAGLE_SYSTEM_GROUP);
			props.put(ConsumerConfig.EXCLUDE_INTERNAL_TOPICS_CONFIG, false);
			props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaService.getKafkaBrokerServer(clusterAlias));
			props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
			props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
			props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

			if (SystemConfigUtils.getBooleanProperty("kafka.eagle.sasl.enable")) {
				props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SystemConfigUtils.getProperty("kafka.eagle.sasl.protocol"));
				props.put(SaslConfigs.SASL_MECHANISM, SystemConfigUtils.getProperty("kafka.eagle.sasl.mechanism"));
			}

			KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
			consumer.subscribe(Arrays.asList(Topic.GroupMetadataTopicName()));

			startOffsetListener(clusterAlias, consumer);
		}
	}

}
