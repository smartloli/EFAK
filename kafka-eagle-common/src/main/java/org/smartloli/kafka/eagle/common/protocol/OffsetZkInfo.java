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
 * Definition Kafka offset in zookeeper information.
 * 
 * @author smartloli.
 *
 *         Created by Aug 16, 2016
 */
public class OffsetZkInfo extends BaseProtocol {

	private long offset = -1L;
	private String create = "";
	private String modify = "";
	private String owners = "";
	private Set<Integer> partitions = new HashSet<>();

	public Set<Integer> getPartitions() {
		return partitions;
	}

	public void setPartitions(Set<Integer> partitions) {
		this.partitions = partitions;
	}

	public String getOwners() {
		return owners;
	}

	public void setOwners(String owners) {
		this.owners = owners;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public String getCreate() {
		return create;
	}

	public void setCreate(String create) {
		this.create = create;
	}

	public String getModify() {
		return modify;
	}

	public void setModify(String modify) {
		this.modify = modify;
	}

}
