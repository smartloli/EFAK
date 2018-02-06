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

import com.google.gson.Gson;

/**
 * Zookeeper cluster information.
 * 
 * @author smartloli.
 *
 *         Created by Feb 5, 2018
 */
public class ZkClusterInfo {

	private String zkPacketsReceived;// received client packet numbers
	private String zkPacketsSent;// send client packet numbers
	private String zkAvgLatency;// response client request avg time
	private String zkNumAliveConnections;// has connected client numbers
	/** waiting deal with client request numbers in queue. */
	private String zkOutstandingRequests;
	/** server mode,like standalone|cluster[leader,follower]. */
	private String zkOpenFileDescriptorCount;
	private String zkMaxFileDescriptorCount;

	public String getZkPacketsReceived() {
		return zkPacketsReceived;
	}

	public void setZkPacketsReceived(String zkPacketsReceived) {
		this.zkPacketsReceived = zkPacketsReceived;
	}

	public String getZkPacketsSent() {
		return zkPacketsSent;
	}

	public void setZkPacketsSent(String zkPacketsSent) {
		this.zkPacketsSent = zkPacketsSent;
	}

	public String getZkAvgLatency() {
		return zkAvgLatency;
	}

	public void setZkAvgLatency(String zkAvgLatency) {
		this.zkAvgLatency = zkAvgLatency;
	}

	public String getZkNumAliveConnections() {
		return zkNumAliveConnections;
	}

	public void setZkNumAliveConnections(String zkNumAliveConnections) {
		this.zkNumAliveConnections = zkNumAliveConnections;
	}

	public String getZkOutstandingRequests() {
		return zkOutstandingRequests;
	}

	public void setZkOutstandingRequests(String zkOutstandingRequests) {
		this.zkOutstandingRequests = zkOutstandingRequests;
	}

	public String getZkOpenFileDescriptorCount() {
		return zkOpenFileDescriptorCount;
	}

	public void setZkOpenFileDescriptorCount(String zkOpenFileDescriptorCount) {
		this.zkOpenFileDescriptorCount = zkOpenFileDescriptorCount;
	}

	public String getZkMaxFileDescriptorCount() {
		return zkMaxFileDescriptorCount;
	}

	public void setZkMaxFileDescriptorCount(String zkMaxFileDescriptorCount) {
		this.zkMaxFileDescriptorCount = zkMaxFileDescriptorCount;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
