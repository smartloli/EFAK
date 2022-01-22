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

import org.smartloli.kafka.eagle.common.protocol.BaseProtocol;

/**
 * Stats topic and consumer groups.
 * 
 * @author jacob.
 *
 *         Created by Dec 25, 2021
 */
public class TopicSummaryInfo extends BaseProtocol {
	private String cluster;
	private String topic;
	private int groupNumbers;
	private int activeGroup;

	public String getCluster() {
		return cluster;
	}

	public void setCluster(String cluster) {
		this.cluster = cluster;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public int getGroupNumbers() {
		return groupNumbers;
	}

	public void setGroupNumbers(int groupNumbers) {
		this.groupNumbers = groupNumbers;
	}

	public int getActiveGroup() {
		return activeGroup;
	}

	public void setActiveGroup(int activeGroup) {
		this.activeGroup = activeGroup;
	}

}
