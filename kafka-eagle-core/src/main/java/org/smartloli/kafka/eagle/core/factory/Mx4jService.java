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
/**
 * 
 */
package org.smartloli.kafka.eagle.core.factory;

import org.smartloli.kafka.eagle.common.protocol.MBeanInfo;

import java.util.Map;

/**
 * Mx4jService operate comand and get metadata from kafka jmx interface.
 * 
 * @author smartloli.
 *
 *         Created by Jul 14, 2017
 */
public interface Mx4jService {

	/** Get brokers all topics bytes in per sec. */
	public MBeanInfo bytesInPerSec(String uri);

	/** Get brokers bytes in per sec by topic. */
	public MBeanInfo bytesInPerSec(String uri, String topic);

	/** Get brokers all topics bytes out per sec. */
	public MBeanInfo bytesOutPerSec(String uri);

	/** Get brokers bytes out per sec by topic. */
	public MBeanInfo bytesOutPerSec(String uri, String topic);

	/** Get brokers all topics byte rejected per sec. */
	public MBeanInfo bytesRejectedPerSec(String uri);

	/** Get brokers byte rejected per sec by topic. */
	public MBeanInfo bytesRejectedPerSec(String uri, String topic);

	/** Get brokers all topic failed fetch request per sec. */
	public MBeanInfo failedFetchRequestsPerSec(String uri);

	/** Get brokers failed fetch request per sec by topic. */
	public MBeanInfo failedFetchRequestsPerSec(String uri, String topic);

	/** Get brokers all topics failed fetch produce request per sec. */
	public MBeanInfo failedProduceRequestsPerSec(String uri);

	/** Get brokers failed fetch produce request per sec by topic. */
	public MBeanInfo failedProduceRequestsPerSec(String uri, String topic);

	/** Get brokers topic all partitions log end offset. */
	public Map<Integer, Long> logEndOffset(String uri, String topic);

	/** Get brokers all topics message in per sec. */
	public MBeanInfo messagesInPerSec(String uri);

	/** Get brokers message in per sec by topic. */
	public MBeanInfo messagesInPerSec(String uri, String topic);

	public MBeanInfo produceMessageConversionsPerSec(String uri);

	public MBeanInfo produceMessageConversionsPerSec(String uri, String topic);

	public MBeanInfo totalFetchRequestsPerSec(String uri);

	public MBeanInfo totalFetchRequestsPerSec(String uri, String topic);

	public MBeanInfo totalProduceRequestsPerSec(String uri);

	public MBeanInfo totalProduceRequestsPerSec(String uri, String topic);

	public MBeanInfo replicationBytesInPerSec(String uri);

	public MBeanInfo replicationBytesInPerSec(String uri, String topic);

	public MBeanInfo replicationBytesOutPerSec(String uri);

	MBeanInfo replicationBytesOutPerSec(String uri, String topic);
}
