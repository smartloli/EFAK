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

	private int brokers = 0;
	private int topics = 0;
	private int zks = 0;
	private int consumers = 0;

	public int getBrokers() {
		return brokers;
	}

	public void setBrokers(int brokers) {
		this.brokers = brokers;
	}

	public int getTopics() {
		return topics;
	}

	public void setTopics(int topics) {
		this.topics = topics;
	}

	public int getZks() {
		return zks;
	}

	public void setZks(int zks) {
		this.zks = zks;
	}

	public int getConsumers() {
		return consumers;
	}

	public void setConsumers(int consumers) {
		this.consumers = consumers;
	}

}
