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
 * Definition dashboard information.
 * 
 * @author smartloli.
 *
 *         Created by Aug 13, 2016
 */
public class DashboardInfo extends BaseProtocol{

	private long brokers = 0;
	private long topics = 0;
	private long zks = 0;
	private long consumers = 0;

	public long getBrokers() {
		return brokers;
	}

	public void setBrokers(long brokers) {
		this.brokers = brokers;
	}

	public long getTopics() {
		return topics;
	}

	public void setTopics(long topics) {
		this.topics = topics;
	}

	public long getZks() {
		return zks;
	}

	public void setZks(long zks) {
		this.zks = zks;
	}

	public long getConsumers() {
		return consumers;
	}

	public void setConsumers(long consumers) {
		this.consumers = consumers;
	}

}
