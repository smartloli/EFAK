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
package org.smartloli.kafka.eagle.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.smartloli.kafka.eagle.factory.KafkaFactory;
import org.smartloli.kafka.eagle.factory.KafkaService;
import org.smartloli.kafka.eagle.factory.ZkFactory;
import org.smartloli.kafka.eagle.factory.ZkService;
import org.smartloli.kafka.eagle.service.ClusterService;
import org.springframework.stereotype.Service;

/**
 * Kafka & Zookeeper implements service to oprate related cluster.
 * 
 * @author smartloli.
 *
 *         Created by Aug 12, 2016
 */

@Service
public class ClusterServiceImpl implements ClusterService {

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();
	/** Zookeeper service interface. */
	private ZkService zkService = new ZkFactory().create();

	/** Execute zookeeper comand. */
	public String execute(String cmd, String type) {
		String target = "";
		String[] len = cmd.replaceAll(" ", "").split(type);
		if (len.length == 0) {
			return cmd + " has error";
		} else {
			JSONObject object = new JSONObject();
			String command = len[1];
			switch (type) {
			case "delete":
				object.put("result", zkService.delete(command));
				target = object.toJSONString();
				break;
			case "get":
				object.put("result", zkService.get(command));
				target = object.toJSONString();
				break;
			case "ls":
				object.put("result", zkService.ls(command));
				target = object.toJSONString();
				break;
			default:
				target = "Invalid command";
				break;
			}
		}
		return target;
	}

	/** Get kafka & zookeeper cluster information. */
	public String get(String type) {
		JSONObject target = new JSONObject();
		if ("zk".equals(type)) {
			String zkCluster = zkService.zkCluster();
			target.put("zk", JSON.parseArray(zkCluster));
		} else if ("kafka".equals(type)) {
			String kafkaBrokers = kafkaService.getAllBrokersInfo();
			target.put("kafka", JSON.parseArray(kafkaBrokers));
		}
		return target.toJSONString();
	}

	/** Get Zookeeper whether live. */
	public JSONObject status() {
		return zkService.zkCliStatus();
	}

}
