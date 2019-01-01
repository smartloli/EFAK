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
		String title = "Kafka Broker [LagBlocked] is greater than [LagExpect], the following alarm will occur.<br/>";
		String content = "</table>" + "	<table align='center' border='1'>  <tr><th  colspan='3' align='center'><label>Kafka Eagle Alert Data</label></th></tr>" + "        <tr><td>Type</td><td>" + type + "</td></tr>"
				+ "        <tr><td>Cluster</td><td>" + cluster + "</td></tr>" + "        <tr><td>Group</td><td>" + group + "</td></tr>" + "        <tr><td>Topic</td><td>" + topic + "</td></tr>"
				+ "        <tr><td>Lag Expect</td><td bgcolor='green'>" + lagThreshold + "</td></tr>" + "        <tr><td>Lag Blocked</td><td bgcolor='red'>" + consumerLag + "</td></tr>" + "<tr><td>Time</td><td>" + time + "</td></tr>"
				+ "        <tr><td>Note</td><td>Please check in kafka cluster and applications.</td></tr>" + "	</table>";
		return head + title + content;
	}

	public String toWeChatMarkDown() {
		String content = "`### [MINOR] Kafka Eagle Alert`\n" + ">**Information** \n" + ">Type: <font color=\"info\">" + type + "</font> \n" + ">ClusterID: cluster1 \n" + ">Owners: " + user + "\n" + ">Group: " + group + "\n" + ">Topic: " + topic + "\n"
				+ ">LagExpect: <font color=\"info\">" + lagThreshold + "</font> \n" + ">LagBlocked: <font color=\"warning\">" + consumerLag + "</font> \n" + "> \n" + ">Time: <font color=\"info\">" + time + "</font> \n" + ">Describer:\n"
				+ "><font color=\"warning\">Please check in kafka cluster and applications.</font>";
		return content;
	}

	public String toDingDingMarkDown() {
		String content = "<font color=\"#FF0000\"> ### [MINOR] Kafka Eagle Alert</font> \n\n" + "> #### Information \n" + "> #### Type: <font color=\"#008000\">" + type + "</font> \n" + "> #### ClusterID: cluster1 \n" + "> #### Owners: "
				+ user + "\n" + "> #### Group: " + group + "\n" + "> #### Topic: " + topic + "\n" + "> #### LagExpect: <font color=\"#008000\">" + lagThreshold + "</font> \n" + "> #### LagBlocked: <font color=\"#FFA500\">" + consumerLag
				+ "</font> \n" + "> \n" + "> #### Time: <font color=\"#008000\">" + time + "</font> \n" + "> #### Describer:\n" + "><font color=\"#FFA500\">Please check in kafka cluster and applications.</font>";

		return content;
	}
}
