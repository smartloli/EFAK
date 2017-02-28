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
package org.smartloli.kafka.eagle.factory;

import java.util.List;
import java.util.Map;

import org.smartloli.kafka.eagle.domain.PageParamDomain;
import org.smartloli.kafka.eagle.domain.KafkaSqlDomain;
import org.smartloli.kafka.eagle.domain.MetadataDomain;
import org.smartloli.kafka.eagle.domain.OffsetZkDomain;

/**
 * Kafka group,topic and partition interface.
 * 
 * @author smartloli.
 *
 *         Created by Jan 18, 2017
 * 
 *         Update by hexiang 20170216
 */
public interface KafkaService {

	/** Find topic and group exist in zookeeper. */
	public boolean findTopicAndGroupExist(String clusterAlias, String topic, String group);

	/** Obtaining metadata in zookeeper by topic. */
	public List<String> findTopicPartition(String clusterAlias, String topic);

	/** Get kafka active consumer topic. */
	public Map<String, List<String>> getActiveTopic(String clusterAlias);

	/** Get all broker list from zookeeper. */
	public String getAllBrokersInfo(String clusterAlias);

	/** Get all topic info from zookeeper. */
	public String getAllPartitions(String clusterAlias);

	/** Obtaining kafka consumer information from zookeeper. */
	public Map<String, List<String>> getConsumers(String clusterAlias);

	/** Obtaining kafka consumer page information from zookeeper. */
	public Map<String, List<String>> getConsumers(String clusterAlias, PageParamDomain page);

	/** Use Kafka low consumer API & get logsize size from zookeeper. */
	public long getLogSize(List<String> hosts, String topic, int partition);

	/** According to group, topic and partition to get offset from zookeeper. */
	public OffsetZkDomain getOffset(String clusterAlias, String topic, String group, int partition);

	/** According to topic and partition to obtain Replicas & Isr. */
	public String geyReplicasIsr(String clusterAlias, String topic, int partitionid);

	/** Use kafka console comand to create topic. */
	public Map<String, Object> create(String clusterAlias, String topicName, String partitions, String replic);

	/** Find leader through topic. */
	public List<MetadataDomain> findLeader(String clusterAlias, String topic);

	/** Convert query kafka to topic in the sql message for standard sql. */
	public KafkaSqlDomain parseSql(String clusterAlias,String sql);
}
