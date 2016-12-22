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
package org.smartloli.kafka.eagle.util;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import kafka.admin.TopicCommand;

/**
 * @author smartloli.
 *
 *         Created by Aug 15, 2016
 */
public class KafkaCommandUtils {

	public static void list() {
		String[] options = new String[] { "--list", "--zookeeper", "master:2181" };
		TopicCommand.main(options);
	}

	public static void describe() {
		String[] options = new String[] { "--describe", "--zookeeper", "dn1:2181,dn2:2181,dn3:2181", "--topic=boyaa_mf_test12345" };
		TopicCommand.main(options);
	}

	public static Map<String, Object> create(String topicName, String partitions, String replic) {
		Map<String, Object> map = new HashMap<String, Object>();
		int brokers = JSON.parseArray(KafkaClusterUtils.getAllBrokersInfo()).size();
		if (Integer.parseInt(replic) > brokers) {
			map.put("status", "error");
			map.put("info", "replication factor: " + replic + " larger than available brokers: " + brokers);
			return map;
		}
		String zks = SystemConfigUtils.getProperty("kafka.zk.list");
		String[] options = new String[] { "--create", "--zookeeper", zks, "--partitions", partitions, "--topic", topicName, "--replication-factor", replic };
		TopicCommand.main(options);
		map.put("status", "success");
		map.put("info", "Create topic[" + topicName + "] has successed,partitions numbers is [" + partitions + "],replication-factor numbers is [" + replic + "]");
		return map;
	}

	public static void main(String[] args) {
		describe();
	}

}
