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
package org.smartloli.kafka.eagle.core.sql.ignite.domain;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import com.google.gson.Gson;

/**
 * Kafka topic pojo mapper.
 * 
 * @author smartloli.
 *
 *         Created by Mar 9, 2018
 */
public class TopicX {

	@QuerySqlField
	private long partitionId;

	@QuerySqlField
	private long offsets;

	@QuerySqlField
	private String message;

	@QuerySqlField
	private String topicName;

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public long getPartitionId() {
		return partitionId;
	}

	public void setPartitionId(long partitionId) {
		this.partitionId = partitionId;
	}

	public long getOffsets() {
		return offsets;
	}

	public void setOffsets(long offsets) {
		this.offsets = offsets;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
