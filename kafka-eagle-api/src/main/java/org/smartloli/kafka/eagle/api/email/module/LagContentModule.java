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
package org.smartloli.kafka.eagle.api.email.module;

/**
 * Builder mail content module
 * 
 * @author smartloli.
 *
 *         Created by Nov 1, 2018
 */
public class LagContentModule {

	private String user;
	private String type;
	private String cluster;
	private String group;
	private String topic;
	private String lagThreshold;
	private String consumerLag;
	private String time;

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getLagThreshold() {
		return lagThreshold;
	}

	public void setLagThreshold(String lagThreshold) {
		this.lagThreshold = lagThreshold;
	}

	public String getConsumerLag() {
		return consumerLag;
	}

	public void setConsumerLag(String consumerLag) {
		this.consumerLag = consumerLag;
	}

	@Override
	public String toString() {
		String head = "Hi " + user + ",<br/>";
		String title = "If Kafka broker or Zookeeper is down, or [Consumer Lag] is greater than [Lag Threshold], the following alarm will occur.<br/>";
		String content = "</table>" + "	<table align='center' border='1'>  <tr><th  colspan='3' align='center'><label>Kafka Eagle Alert Data</label></th></tr>" + "        <tr><td>Type</td><td>" + type + "</td></tr>"
				+ "        <tr><td>Cluster</td><td>" + cluster + "</td></tr>" + "        <tr><td>Group</td><td>" + group + "</td></tr>" + "        <tr><td>Topic</td><td>" + topic + "</td></tr>"
				+ "        <tr><td>Lag Threshold</td><td bgcolor='green'>" + lagThreshold + "</td></tr>" + "        <tr><td>Consumer Lag</td><td bgcolor='red'>" + consumerLag + "</td></tr>" + "<tr><td>Time</td><td>" + time + "</td></tr>"
				+ "        <tr><td>Note</td><td>Please check in kafka cluster and applications.</td></tr>" + "	</table>";
		return head + title + content;
	}

}
