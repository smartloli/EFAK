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

/**
 * Kafka topic service interface.
 * 
 * @author smartloli.
 *
 *         Created by Jan 17, 2017.
 * 
 *         Update by hexiang 20170216
 */
public interface TopicService {

	/** Find topic name in all topics. */
	public boolean hasTopic(String clusterAlias, String topicName);

	/** Get metadata in topic. */
	public String metadata(String clusterAlias, String topicName);

	/** List all the topic under Kafka in partition. */
	public String list(String clusterAlias);

	/** Execute kafka query sql. */
	public String execute(String clusterAlias, String sql);

	/** Get mock topics. */
	public String mockTopics(String clusterAlias, String name);

	/** Send mock message to topic. */
	public boolean mockSendMsg(String clusterAlias, String topic, String message);

}
