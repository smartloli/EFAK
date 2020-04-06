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
package org.smartloli.kafka.eagle.common.protocol.consumer;

import org.smartloli.kafka.eagle.common.protocol.BaseProtocol;

/**
 * Stats consumer groups and topic.
 * 
 * @author smartloli.
 *
 *         Created by Mar 17, 2020
 */
public class ConsumerSummaryInfo extends BaseProtocol {
	private String cluster;
	private String group;
	private int topicNumbers;
	private String coordinator;
	private int activeTopic;
	private int activeThread;

	public String getCoordinator() {
		return coordinator;
	}

	public void setCoordinator(String coordinator) {
		this.coordinator = coordinator;
	}

	public int getActiveTopic() {
		return activeTopic;
	}

	public void setActiveTopic(int activeTopic) {
		this.activeTopic = activeTopic;
	}

	public int getActiveThread() {
		return activeThread;
	}

	public void setActiveThread(int activeThread) {
		this.activeThread = activeThread;
	}

	public String getCluster() {
		return cluster;
	}

	public void setCluster(String cluster) {
		this.cluster = cluster;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public int getTopicNumbers() {
		return topicNumbers;
	}

	public void setTopicNumbers(int topicNumbers) {
		this.topicNumbers = topicNumbers;
	}

}
