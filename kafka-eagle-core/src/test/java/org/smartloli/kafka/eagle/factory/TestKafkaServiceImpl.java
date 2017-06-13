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
/**
 * 
 */
package org.smartloli.kafka.eagle.factory;

import java.util.List;

import org.I0Itec.zkclient.ZkClient;
import org.smartloli.kafka.eagle.common.util.ZKPoolUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;

import kafka.utils.ZkUtils;
import scala.collection.JavaConversions;
import scala.collection.Seq;

/**
 * TODO
 * 
 * @author smartloli.
 *
 *         Created by Mar 24, 2017
 */
public class TestKafkaServiceImpl {

	private ZKPoolUtils zkPool = ZKPoolUtils.getInstance();

	private final String BROKER_TOPICS_PATH = "/brokers/topics";
	
	private static KafkaService kafkaService = new KafkaFactory().create();

	public static void main(String[] args) {
		System.out.println(kafkaService.getAllBrokersInfo("cluster1"));
	}

	public List<String> findTopicPartition(String clusterAlias, String topic) {
		ZkClient zkc = zkPool.getZkClient(clusterAlias);
		Seq<String> brokerTopicsPaths = ZkUtils.apply(zkc, false).getChildren(BROKER_TOPICS_PATH + "/" + topic + "/partitions");
		List<String> topicAndPartitions = JavaConversions.seqAsJavaList(brokerTopicsPaths);
		if (zkc != null) {
			zkPool.release(clusterAlias, zkc);
			zkc = null;
		}
		return topicAndPartitions;
	}

}
