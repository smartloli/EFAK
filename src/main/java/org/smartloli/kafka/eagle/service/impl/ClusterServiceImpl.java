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

import com.alibaba.fastjson.JSONObject;

import org.smartloli.kafka.eagle.service.ClusterService;
import org.smartloli.kafka.eagle.util.KafkaClusterUtils;
import org.smartloli.kafka.eagle.util.ZKCliUtils;
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

	/** Execute zookeeper comand. */
	public String execute(String cmd, String type) {
		String ret = "";
		if ("ls".equals(type)) {
			JSONObject object = new JSONObject();
			object.put("result", ls(cmd));
			ret = object.toJSONString();
		} else if ("delete".equals(type)) {
			JSONObject object = new JSONObject();
			object.put("result", delete(cmd));
			ret = object.toJSONString();
		} else if ("get".equals(type)) {
			JSONObject object = new JSONObject();
			object.put("result", get(cmd));
			ret = object.toJSONString();
		} else {
			ret = "Invalid command";
		}
		return ret;
	}

	/** Get kafka & zookeeper cluster information. */
	public String get() {
		String zk = KafkaClusterUtils.getZkCluster();
		String kafka = KafkaClusterUtils.getAllBrokersInfo();
		JSONObject obj = new JSONObject();
		obj.put("zk", zk);
		obj.put("kafka", kafka);
		return obj.toJSONString();
	}

	/** Get Zookeeper whether live. */
	public JSONObject status() {
		return KafkaClusterUtils.zkCliStatus();
	}

	/** Delete zookeeper metadata & use command. */
	private Object delete(String cmd) {
		String ret = "";
		String[] len = cmd.replaceAll(" ", "").split("delete");
		if (len.length == 0) {
			return cmd + " has error";
		} else {
			String command = len[1];
			ret = ZKCliUtils.delete(command);
		}
		return ret;
	}

	/** Get command & obtain information from zookeeper. */
	private Object get(String cmd) {
		String ret = "";
		String[] len = cmd.replaceAll(" ", "").split("get");
		if (len.length == 0) {
			return cmd + " has error";
		} else {
			String command = len[1];
			ret = ZKCliUtils.get(command);
		}
		return ret;
	}

	/** Zookeeper ls command to list information. */
	private String ls(String cmd) {
		String ret = "";
		String[] len = cmd.replaceAll(" ", "").split("ls");
		if (len.length == 0) {
			return cmd + " has error";
		} else {
			String command = len[1];
			ret = ZKCliUtils.ls(command);
		}
		return ret;
	}

}
