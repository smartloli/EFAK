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
package org.smartloli.kafka.eagle.core.factory.v2;

import java.util.List;
import java.util.Map;

import org.smartloli.kafka.eagle.common.protocol.MetadataInfo;
import org.smartloli.kafka.eagle.common.protocol.PartitionsInfo;

/**
 * Consumer group, topic and topic page or partition interface.Kafka api 2.x
 * version.
 * 
 * @author smartloli.
 *
 *         Created by Jun 13, 2019
 */
public interface BrokerService {

	/** Check topic from zookeeper metadata. */
	public boolean findKafkaTopic(String clusterAlias, String topic);

	/** Get topic list. */
	public List<String> topicList(String clusterAlias);

	/** Get select topic list. */
	public String topicListParams(String clusterAlias, String search);

	/** Get kafka broker numbers. */
	public long brokerNumbers(String clusterAlias);

	/** Get topic number from zookeeper. */
	public long topicNumbers(String clusterAlias);

	/** Get topic number with match name from zookeeper. */
	public long topicNumbers(String clusterAlias, String topic);

	/** Get partition number from zookeeper. */
	public long partitionNumbers(String clusterAlias, String topic);

	/** Scan topic page display. */
	public List<PartitionsInfo> topicRecords(String clusterAlias, Map<String, Object> params);

	/** Scan topic meta page display. */
	public List<MetadataInfo> topicMetadataRecords(String clusterAlias, String topic, Map<String, Object> params);

	/** Get topic producer logsize total. */
	public long getTopicLogSizeTotal(String clusterAlias, String topic);

	/** Get topic real logsize records. */
	public long getTopicRealLogSize(String clusterAlias, String topic);

	/** Get topic producer send logsize records. */
	public long getTopicProducerLogSize(String clusterAlias, String topic);

	/** Add topic partitions. */
	public Map<String, Object> createTopicPartitions(String clusterAlias, String topic, int totalCount);

	/** Get broker spread by topic. */
	public int getBrokerSpreadByTopic(String clusterAlias, String topic);

	/** Get broker skewed by topic. */
	public int getBrokerSkewedByTopic(String clusterAlias, String topic);

	/** Get broker leader skewed by topic. */
	public int getBrokerLeaderSkewedByTopic(String clusterAlias, String topic);

}
