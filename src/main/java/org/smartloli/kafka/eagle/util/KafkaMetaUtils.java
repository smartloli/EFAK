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
package org.smartloli.kafka.eagle.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.domain.KafkaBrokerDomain;
import org.smartloli.kafka.eagle.domain.KafkaMetaDomain;

import kafka.cluster.Broker;
import kafka.javaapi.PartitionMetadata;
import kafka.javaapi.TopicMetadata;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.TopicMetadataResponse;
import kafka.javaapi.consumer.SimpleConsumer;

/**
 * As kafka low api to get metadata.
 *
 * @author smartloli.
 *
 *         Created by Aug 15, 2016
 */
public class KafkaMetaUtils {

	private static Logger LOG = LoggerFactory.getLogger(KafkaMetaUtils.class);

	/**
	 * Find leader through topic.
	 * 
	 * @param topic
	 * @return List
	 * @see org.smartloli.kafka.eagle.domain.KafkaMetaDomain
	 */
	public static List<KafkaMetaDomain> findLeader(String topic) {
		List<KafkaMetaDomain> list = new ArrayList<>();

		SimpleConsumer consumer = null;
		for (KafkaBrokerDomain broker : getBrokers()) {
			try {
				consumer = new SimpleConsumer(broker.getHost(), broker.getPort(), 100000, 64 * 1024, "leaderLookup");
				if (consumer != null) {
					break;
				}
			} catch (Exception ex) {
				LOG.error(ex.getMessage());
			}
		}

		if (consumer == null) {
			LOG.error("Connection [SimpleConsumer] has failed,please check brokers.");
			return list;
		}

		List<String> topics = Collections.singletonList(topic);
		TopicMetadataRequest req = new TopicMetadataRequest(topics);
		TopicMetadataResponse resp = consumer.send(req);
		if (resp == null) {
			LOG.error("Get [TopicMetadataResponse] has null.");
			return list;
		}
		List<TopicMetadata> metaData = resp.topicsMetadata();
		for (TopicMetadata item : metaData) {
			for (PartitionMetadata part : item.partitionsMetadata()) {
				KafkaMetaDomain kMeta = new KafkaMetaDomain();
				kMeta.setIsr(KafkaClusterUtils.geyReplicasIsr(topic, part.partitionId()));
				kMeta.setLeader(part.leader() == null ? -1 : part.leader().id());
				kMeta.setPartitionId(part.partitionId());
				List<Integer> repliList = new ArrayList<>();
				for (Broker repli : part.replicas()) {
					repliList.add(repli.id());
				}
				kMeta.setReplicas(repliList.toString());
				list.add(kMeta);
			}
		}
		if (consumer != null) {
			consumer.close();
		}
		return list;
	}

	/** Get kafka brokers from zookeeper. */
	private static List<KafkaBrokerDomain> getBrokers() {
		String brokersStr = KafkaClusterUtils.getAllBrokersInfo();
		List<KafkaBrokerDomain> brokers = new ArrayList<KafkaBrokerDomain>();
		JSONArray arr = JSON.parseArray(brokersStr);
		for (Object object : arr) {
			JSONObject obj = (JSONObject) object;
			KafkaBrokerDomain broker = new KafkaBrokerDomain();
			broker.setHost(obj.getString("host"));
			broker.setPort(obj.getInteger("port"));
			brokers.add(broker);
		}
		return brokers;
	}

}
