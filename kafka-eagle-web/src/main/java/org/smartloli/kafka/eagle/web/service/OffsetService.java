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

import java.util.Map;

/**
 * Offsets consumer data interface.
 * 
 * @author smartloli.
 *
 *         Created by Jan 17, 2017.
 * 
 *         Update by hexiang 20170216
 */
public interface OffsetService {

	/** Get logsize from Kafka topic or Zookeeper interface. */
	public String getLogSize(String clusterAlias, String formatter, String topic, String group);

	/** Get Kafka offset graph data from Zookeeper interface. */
	public String getOffsetsGraph(Map<String, Object> param);

	/** Judge group & topic exist Kafka topic or Zookeeper interface. */
	public boolean hasGroupTopic(String clusterAlias, String formatter, String group, String topic);

	/** Get topic consumer & producer rate. */
	public String getOffsetRate(String clusterAlias, String topic);

}
