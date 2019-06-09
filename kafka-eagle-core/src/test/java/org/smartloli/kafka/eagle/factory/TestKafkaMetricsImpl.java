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
package org.smartloli.kafka.eagle.factory;

import org.apache.kafka.clients.admin.ConfigEntry;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsFactory;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsService;

/**
 * TODO
 * 
 * @author smartloli.
 *
 *         Created by Jun 9, 2019
 */
public class TestKafkaMetricsImpl {

	private static KafkaMetricsService kafkaMetric = new KafkaMetricsFactory().create();

	public static void main(String[] args) {
		ConfigEntry configEntry = new ConfigEntry("cleanup.policy", "122ss");
		String target = kafkaMetric.changeTopicConfig("cluster1", "kv-test2019", "ADD", configEntry);
		System.out.println("target: " + target);
	}
}
