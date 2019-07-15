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
package org.smartloli.kafka.eagle.ipc;

import java.util.HashMap;
import java.util.Map;

import org.smartloli.kafka.eagle.core.factory.v2.BrokerFactory;
import org.smartloli.kafka.eagle.core.factory.v2.BrokerService;

/**
 * TODO
 * 
 * @author smartloli.
 *
 *         Created by Jul 15, 2019
 */
public class TestTopicList {
	/** Broker service interface. */
	private static BrokerService brokerService = new BrokerFactory().create();

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		Map<String, Object> params = new HashMap<>();
		params.put("search", "");
		params.put("start", "0");
		params.put("length", "10");
		brokerService.topicRecords("cluster1", params);
		System.out.println(System.currentTimeMillis() - start);
	}
}
