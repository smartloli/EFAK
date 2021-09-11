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

import java.util.Map;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

/**
 * Kafka topic partition rules.
 *
 * @author smartloli.
 *
 *         Created by Mar 14, 2017
 */
public class KafkaPartitioner implements Partitioner {

	public int partition(Object key, int numPartitions) {
		int partition = 0;
		String k = (String) key;
		int hashCode = k.hashCode();
		partition = Math.abs(hashCode) % numPartitions;
		return partition;
	}

	@Override
	public void configure(Map<String, ?> configs) {
		// Do nothing
	}

	@Override
	public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
		int partition = 0;
		String k = (String) key;
		int hashCode = k.hashCode();
		partition = Math.abs(hashCode) % cluster.partitionCountForTopic(topic);
		return partition;
	}

	@Override
	public void close() {
		// Do nothing
	}

}
