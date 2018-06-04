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

import java.util.Properties;

import org.apache.kafka.common.security.JaasUtils;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.utils.ZkUtils;

/**
 * Test clazz.
 * 
 * @author smartloli.
 *
 *         Created by Jun 1, 2018
 */
public class TestZkTopic {
	public static void main(String[] args) {
//		create();
		delete();
	}

	public static void delete() {
		ZkUtils zkUtils = ZkUtils.apply("localhost:2181", 30000, 30000, JaasUtils.isZkSecurityEnabled());
		AdminUtils.deleteTopic(zkUtils, "tt1");
		zkUtils.close();
	}

	public static void create() {
		ZkUtils zkUtils = ZkUtils.apply("localhost:2181", 30000, 30000, JaasUtils.isZkSecurityEnabled());
		AdminUtils.createTopic(zkUtils, "tt1", 1, 1, new Properties(), RackAwareMode.Enforced$.MODULE$);
		zkUtils.close();
	}
}
