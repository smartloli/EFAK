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
package org.smartloli.kafka.eagle.common.protocol;

import java.util.HashSet;
import java.util.Set;

/**
 * Definition Kafka partition information.
 * 
 * @author smartloli.
 *
 *         Created by Mar 30, 2016
 */
public class PartitionsInfo extends BaseProtocol {

	private int id = 0;
	private String topic = "";
	private Set<String> partitions = new HashSet<String>();
	private int partitionNumbers = 0;
	private long brokersSkewed;
	private long brokersSpread;
	private long brokersLeaderSkewed;
	private String created = "";
	private String modify = "";

	public long getBrokersSkewed() {
		return brokersSkewed;
	}

	public void setBrokersSkewed(long brokersSkewed) {
		this.brokersSkewed = brokersSkewed;
	}

	public long getBrokersSpread() {
		return brokersSpread;
	}

	public void setBrokersSpread(long brokersSpread) {
		this.brokersSpread = brokersSpread;
	}

	public long getBrokersLeaderSkewed() {
		return brokersLeaderSkewed;
	}

	public void setBrokersLeaderSkewed(long brokersLeaderSkewed) {
		this.brokersLeaderSkewed = brokersLeaderSkewed;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getModify() {
		return modify;
	}

	public void setModify(String modify) {
		this.modify = modify;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPartitionNumbers() {
		return partitionNumbers;
	}

	public void setPartitionNumbers(int partitionNumbers) {
		this.partitionNumbers = partitionNumbers;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Set<String> getPartitions() {
		return partitions;
	}

	public void setPartitions(Set<String> partitions) {
		this.partitions = partitions;
	}

}
