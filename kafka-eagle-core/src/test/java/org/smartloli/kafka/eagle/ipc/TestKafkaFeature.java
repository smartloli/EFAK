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

import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;

import kafka.admin.AdminClient;

/**
 * Test Kafka new feature.
 * 
 * @author smartloli.
 *
 *         Created by Feb 11, 2018
 */
public class TestKafkaFeature {

	public static void main(String[] args) {
		Properties prop = new Properties();
		prop.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
		AdminClient adminClient = AdminClient.create(prop);
		System.out.println(adminClient.listAllConsumerGroups());
	}

}
