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

import com.google.gson.Gson;

/**
 * Topic lag information.
 * 
 * @author smartloli.
 *
 *         Created by Jan 16, 2019
 */
public class TopicLagInfo {

	private String cluster;
	private String group;
	private String topic;
	private String lag;
	private long timespan;
	private String tm;

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

	public String getLag() {
		return lag;
	}

	public void setLag(String lag) {
		this.lag = lag;
	}

	public long getTimespan() {
		return timespan;
	}

	public void setTimespan(long timespan) {
		this.timespan = timespan;
	}

	public String getTm() {
		return tm;
	}

	public void setTm(String tm) {
		this.tm = tm;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
