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

/**
 * Schema topic metadata information.
 * 
 * @author smartloli.
 *
 *         Created by Aug 15, 2016
 */
public class MetadataInfo extends BaseProtocol {

	private int partitionId;
	private long logSize;
	private int leader;
	private String isr;
	private String replicas;
	private boolean preferredLeader;
	private boolean underReplicated;

	public boolean isPreferredLeader() {
		return preferredLeader;
	}

	public void setPreferredLeader(boolean preferredLeader) {
		this.preferredLeader = preferredLeader;
	}

	public boolean isUnderReplicated() {
		return underReplicated;
	}

	public void setUnderReplicated(boolean underReplicated) {
		this.underReplicated = underReplicated;
	}

	public long getLogSize() {
		return logSize;
	}

	public void setLogSize(long logSize) {
		this.logSize = logSize;
	}

	public int getPartitionId() {
		return partitionId;
	}

	public void setPartitionId(int partitionId) {
		this.partitionId = partitionId;
	}

	public int getLeader() {
		return leader;
	}

	public void setLeader(int leader) {
		this.leader = leader;
	}

	public String getIsr() {
		return isr;
	}

	public void setIsr(String isr) {
		this.isr = isr;
	}

	public String getReplicas() {
		return replicas;
	}

	public void setReplicas(String replicas) {
		this.replicas = replicas;
	}

}
