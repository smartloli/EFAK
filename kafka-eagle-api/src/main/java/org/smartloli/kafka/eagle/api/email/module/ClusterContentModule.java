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
 * TODO
 * 
 * @author smartloli.
 *
 *         Created by Nov 1, 2018
 */
public class ClusterContentModule {

	private String user;
	private String cluster;
	private String type;
	private String server;
	private String time;

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getCluster() {
		return cluster;
	}

	public void setCluster(String cluster) {
		this.cluster = cluster;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	@Override
	public String toString() {
		String head = "Hi " + user + ",<br/>";
		String title = "If Kafka broker or Zookeeper is down, the following alarm will occur.<br/>";
		String content = "<table align='center' border='1'>  <tr><th  colspan='3' align='center'><label>Kafka Eagle Alert Data</label></th></tr>" + "        <tr><td>Type</td><td>" + type + "</td></tr>" + "        <tr><td>Cluster</td><td>"
				+ cluster + "</td></tr>" + "        <tr><td>Servers</td><td>" + server + "</td></tr>      " + "		<tr><td>Time</td><td>" + time + "</td></tr>"
				+ "        <tr><td>Note</td><td>Please check in kafka cluster or Zookeeper cluster.</td></tr>" + "	</table>";
		return head + title + content;
	}

	public String toWeChatMarkDown() {
		String[] servers = server.split(",");
		String describer = "";
		for (String serve : servers) {
			describer += "><font color=\"warning\">Telnet server[" + serve + "] is not available.</font>\n";
		}
		String content = "`### [CRITICAL] Kafka Eagle Alert` \n" + ">**Information** \n" + ">Type: <font color=\"info\">" + type + "</font> \n" + ">ClusterID: " + cluster + " \n" + ">Owners: " + user + " \n" + ">Time: <font color=\"info\">" + time
				+ "</font> \n" + ">Describer:\n" + describer;
		return content;
	}

	public String toDingDingMarkDown() {
		String[] servers = server.split(",");
		String describer = "";
		for (String serve : servers) {
			describer += "> <font color=\"#FFA500\">Telnet server[" + serve + "] is not available.</font>\n";
		}
		String content = "<font color=\"#FF0000\">### [CRITICAL] Kafka Eagle Alert</font> \n\n" + "> #### Information \n" + "> #### Type: <font color=\"#008000\">" + type + "</font> \n" + "> #### ClusterID: " + cluster + " \n"
				+ "> #### Owners: " + user + " \n" + "> #### Time: <font color=\"#008000\">" + time + "</font> \n" + "> #### Describer:\n" + describer;
		return content;
	}

}
