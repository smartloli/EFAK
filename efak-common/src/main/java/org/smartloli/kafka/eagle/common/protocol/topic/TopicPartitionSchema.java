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
package org.smartloli.kafka.eagle.common.protocol.topic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.smartloli.kafka.eagle.common.protocol.BaseProtocol;

/**
 * KSQL topic and partitions schema.
 * 
 * @author smartloli.
 *
 *         Created by Jun 8, 2020
 */
public class TopicPartitionSchema extends BaseProtocol {

	/**
	 * key: topic
	 * 
	 * value: partitions
	 * 
	 */
	private Map<String, List<Integer>> topicSchema = new HashMap<>();

	private String topic;
	private List<Integer> partitions = new ArrayList<Integer>();

	private long limit = 0L;

	public long getLimit() {
		return limit;
	}

	public void setLimit(long limit) {
		this.limit = limit;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public List<Integer> getPartitions() {
		return partitions;
	}

	public void setPartitions(List<Integer> partitions) {
		this.partitions = partitions;
	}

	public Map<String, List<Integer>> getTopicSchema() {
		return topicSchema;
	}

	public void setTopicSchema(Map<String, List<Integer>> topicSchema) {
		this.topicSchema = topicSchema;
	}

}
