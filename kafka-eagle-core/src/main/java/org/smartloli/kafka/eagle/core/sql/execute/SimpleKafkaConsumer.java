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
package org.smartloli.kafka.eagle.core.sql.execute;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.domain.HostsDomain;
import org.smartloli.kafka.eagle.common.domain.KafkaSqlDomain;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.ErrorMapping;
import kafka.common.TopicAndPartition;
import kafka.javaapi.FetchResponse;
import kafka.javaapi.OffsetResponse;
import kafka.javaapi.PartitionMetadata;
import kafka.javaapi.TopicMetadata;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.message.MessageAndOffset;

/**
 * Kafka underlying consumer API interface.
 *
 * @author smartloli.
 *
 *         Created by Mar 14, 2016
 */
public class SimpleKafkaConsumer {
	private static Logger LOG = LoggerFactory.getLogger(SimpleKafkaConsumer.class);
	private List<HostsDomain> m_replicaBrokers = new ArrayList<HostsDomain>();
	private static int buff_size = 64 * 1024;
	private static int fetch_size = 1000 * 1000 * 1000;
	private static int timeout = 100000;
	private static int maxSize = 5000;

	public SimpleKafkaConsumer() {
		m_replicaBrokers = new ArrayList<HostsDomain>();
	}

	public static List<JSONArray> start(KafkaSqlDomain kafkaSql) {
		List<JSONArray> messages = new ArrayList<>();
		List<HostsDomain> seeds = kafkaSql.getSeeds();
		for (int partition : kafkaSql.getPartition()) {
			messages.add(consumer(partition, kafkaSql.getTopic(), seeds));
		}
		return messages;
	}

	public static JSONArray consumer(int _partition, String _topic, List<HostsDomain> seeds) {
		JSONArray msg = null;
		SimpleKafkaConsumer example = new SimpleKafkaConsumer();
		// Max read number
		long maxReads = 3L;
		// To subscribe to the topic
		String topic = _topic;
		// Find partition
		int partition = _partition;
		try {
			msg = example.run(maxReads, topic, partition, seeds);
		} catch (Exception e) {
			LOG.error("[SimapleConsumer.consumer] Oops:" + e);
			e.printStackTrace();
		}
		return msg;
	}

	private JSONArray run(long a_maxReads, String a_topic, int a_partition, List<HostsDomain> a_seedBrokers) throws Exception {
		JSONArray topics = new JSONArray();
		// Get point topic partition's meta
		PartitionMetadata metadata = findLeader(a_seedBrokers, a_topic, a_partition);
		if (metadata == null) {
			LOG.error("[SimpleKafkaConsumer.run()] - Can't find metadata for Topic and Partition. Exiting");
			return null;
		}
		if (metadata.leader() == null) {
			LOG.error("[SimpleKafkaConsumer.run()] - Can't find Leader for Topic and Partition. Exiting");
			return null;
		}
		String leadBroker = metadata.leader().host();
		int a_port = metadata.leader().port();
		String clientName = "Client_" + a_topic + "_" + a_partition;

		SimpleConsumer consumer = new SimpleConsumer(leadBroker, a_port, timeout, buff_size, clientName);
		long latestOffset = getLastOffset(consumer, a_topic, a_partition, kafka.api.OffsetRequest.LatestTime(), clientName);
		long earliestOffset = getLastOffset(consumer, a_topic, a_partition, kafka.api.OffsetRequest.EarliestTime(), clientName);
		long readOffset = 0L;
		if ((latestOffset - maxSize) > 0) {
			readOffset = (latestOffset - maxSize) > earliestOffset ? (latestOffset - maxSize) : earliestOffset;
		} else {
			readOffset = getLastOffset(consumer, a_topic, a_partition, kafka.api.OffsetRequest.EarliestTime(), clientName);
		}
		int numErrors = 0;
		while (a_maxReads > 0) {
			if (consumer == null) {
				consumer = new SimpleConsumer(leadBroker, a_port, timeout, buff_size, clientName);
			}
			FetchRequest req = new FetchRequestBuilder().clientId(clientName).addFetch(a_topic, a_partition, readOffset, fetch_size).build();
			FetchResponse fetchResponse = consumer.fetch(req);

			if (fetchResponse.hasError()) {
				numErrors++;
				// Something went wrong!
				short code = fetchResponse.errorCode(a_topic, a_partition);
				LOG.error("[SimpleKafkaConsumer.run()] - Error fetching data from the Broker:" + leadBroker + ":" + a_port + " Reason: " + code);
				if (numErrors > 5)
					break;
				if (code == ErrorMapping.OffsetOutOfRangeCode()) {
					// We asked for an invalid offset. For simple case ask for
					// the last element to reset
					readOffset = getLastOffset(consumer, a_topic, a_partition, kafka.api.OffsetRequest.LatestTime(), clientName);
					continue;
				}
				consumer.close();
				consumer = null;
				leadBroker = findNewLeader(leadBroker, a_topic, a_partition, a_port);
				continue;
			}
			numErrors = 0;

			long numRead = 0;
			for (MessageAndOffset messageAndOffset : fetchResponse.messageSet(a_topic, a_partition)) {
				long currentOffset = messageAndOffset.offset();
				if (currentOffset < readOffset) {
					LOG.info("[SimpleKafkaConsumer.run()] - Found an old offset: " + currentOffset + " Expecting: " + readOffset);
					continue;
				}

				readOffset = messageAndOffset.nextOffset();
				ByteBuffer payload = messageAndOffset.message().payload();

				byte[] bytes = new byte[payload.limit()];
				payload.get(bytes);
				JSONObject topic = new JSONObject();
				topic.put("partition", a_partition);
				topic.put("offset", messageAndOffset.offset());
				topic.put("msg", new String(bytes, "UTF-8"));
				topics.add(topic);

				numRead++;
				a_maxReads--;
			}

			if (numRead == 0) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}
		if (consumer != null)
			consumer.close();
		return topics;
	}

	public static long getLastOffset(SimpleConsumer consumer, String topic, int partition, long whichTime, String clientName) {
		TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
		Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
		requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 1));
		kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(requestInfo, kafka.api.OffsetRequest.CurrentVersion(), clientName);
		OffsetResponse response = consumer.getOffsetsBefore(request);

		if (response.hasError()) {
			LOG.info("[SimpleKafkaConsumer.getLastOffset()] - Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topic, partition));
			return 0;
		}
		long[] offsets = response.offsets(topic, partition);
		return offsets[0];
	}

	/**
	 * @param a_oldLeader
	 * @param a_topic
	 * @param a_partition
	 * @param a_port
	 * @return String
	 * @throws Exception
	 *             find next leader broker
	 */
	private String findNewLeader(String a_oldLeader, String a_topic, int a_partition, int a_port) throws Exception {
		for (int i = 0; i < 3; i++) {
			boolean goToSleep = false;
			PartitionMetadata metadata = findLeader(m_replicaBrokers, a_topic, a_partition);
			if (metadata == null) {
				goToSleep = true;
			} else if (metadata.leader() == null) {
				goToSleep = true;
			} else if (a_oldLeader.equalsIgnoreCase(metadata.leader().host()) && i == 0) {
				// first time through if the leader hasn't changed give
				// ZooKeeper a second to recover
				// second time, assume the broker did recover before failover,
				// or it was a non-Broker issue
				//
				goToSleep = true;
			} else {
				return metadata.leader().host();
			}
			if (goToSleep) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
				}
			}
		}
		throw new Exception("Unable to find new leader after Broker failure. Exiting");
	}

	private PartitionMetadata findLeader(List<HostsDomain> a_seedBrokers, String a_topic, int a_partition) {
		PartitionMetadata returnMetaData = null;
		loop: for (HostsDomain seed : a_seedBrokers) {
			SimpleConsumer consumer = null;
			try {
				consumer = new SimpleConsumer(seed.getHost(), seed.getPort(), timeout, buff_size, "leaderLookup");
				List<String> topics = Collections.singletonList(a_topic);
				TopicMetadataRequest req = new TopicMetadataRequest(topics);
				kafka.javaapi.TopicMetadataResponse resp = consumer.send(req);

				List<TopicMetadata> metaData = resp.topicsMetadata();
				for (TopicMetadata item : metaData) {
					for (PartitionMetadata part : item.partitionsMetadata()) {
						if (part.partitionId() == a_partition) {
							returnMetaData = part;
							break loop;
						}
					}
				}
			} catch (Exception e) {
				LOG.error("Error communicating with Broker [" + seed + "] to find Leader for [" + a_topic + ", " + a_partition + "] Reason: " + e);
			} finally {
				if (consumer != null)
					consumer.close();
			}
		}
		if (returnMetaData != null) {
			m_replicaBrokers.clear();
			for (kafka.cluster.BrokerEndPoint replica : returnMetaData.replicas()) {
				HostsDomain host = new HostsDomain();
				host.setHost(replica.host());
				host.setPort(replica.port());
				m_replicaBrokers.add(host);
			}
		}
		return returnMetaData;
	}
}
