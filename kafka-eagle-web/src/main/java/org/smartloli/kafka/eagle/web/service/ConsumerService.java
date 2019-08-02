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
package org.smartloli.kafka.eagle.web.service;

import org.smartloli.kafka.eagle.common.protocol.DisplayInfo;

/**
 * Kafka consumer data interface.
 * 
 * @author smartloli.
 *
 *         Created by Jan 17, 2017.
 * 
 *         Update by hexiang 20170216
 */
public interface ConsumerService {

	/** Get active topic graph data interface. */
	public String getActiveGraph(String clusterAlias);

	/** Storage offset in kafka or zookeeper interface. */
	public String getActiveTopic(String clusterAlias, String formatter);

	/**
	 * Judge consumer detail information storage offset in kafka or zookeeper
	 * interface.
	 */
	public String getConsumerDetail(String clusterAlias, String formatter, String group);

	/** Judge consumers storage offset in kafka or zookeeper interface. */
	public String getConsumer(String clusterAlias, String formatter, DisplayInfo page);

	/** Get consumer size from kafka topic interface. */
	public int getConsumerCount(String clusterAlias, String formatter);

	/** Check if the application is consuming. */
	public boolean isConsumering(String clusterAlias, String group, String topic);

}
