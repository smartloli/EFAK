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
package org.smartloli.kafka.eagle.service;

import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.util.KafkaClusterUtils;
import org.smartloli.kafka.eagle.util.ZKCliUtils;

/**
 * @author smartloli.
 *
 *         Created by Aug 12, 2016
 */
public class ClusterService {

	public static String getCluster() {
		String zk = KafkaClusterUtils.getZkInfo();
		String kafka = KafkaClusterUtils.getAllBrokersInfo();
		JSONObject obj = new JSONObject();
		obj.put("zk", zk);
		obj.put("kafka", kafka);
		return obj.toJSONString();
	}

	public static JSONObject zkCliIsLive() {
		return KafkaClusterUtils.zkCliIsLive();
	}

	public static String getZKMenu(String cmd, String type) {
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

	private static Object delete(String cmd) {
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

	private static Object get(String cmd) {
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

	private static String ls(String cmd) {
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
