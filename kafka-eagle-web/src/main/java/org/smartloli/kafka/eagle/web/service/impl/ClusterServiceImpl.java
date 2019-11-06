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
package org.smartloli.kafka.eagle.web.service.impl;

import java.util.List;

import org.smartloli.kafka.eagle.common.protocol.BrokersInfo;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.core.factory.ZkFactory;
import org.smartloli.kafka.eagle.core.factory.ZkService;
import org.smartloli.kafka.eagle.web.service.ClusterService;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Kafka & Zookeeper implements service to oprate related cluster.
 * 
 * @author smartloli.
 *
 *         Created by Aug 12, 2016.
 * 
 *         Update by hexiang 20170216
 */

@Service
public class ClusterServiceImpl implements ClusterService {

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();
	/** Zookeeper service interface. */
	private ZkService zkService = new ZkFactory().create();

	@Override
	public JSONArray clusterAliass() {
		String[] multiClusters = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
		JSONArray aliass = new JSONArray();
		int i = 1;
		for (String cluster : multiClusters) {
			JSONObject object = new JSONObject();
			object.put("id", i++);
			object.put("clusterAlias", cluster);
			object.put("zkhost", SystemConfigUtils.getProperty(cluster + ".zk.list"));
			aliass.add(object);
		}
		return aliass;
	}

	/** Execute zookeeper comand. */
	public String execute(String clusterAlias, String cmd, String type) {
		String target = "";
		String[] len = cmd.replaceAll(" ", "").split(type);
		if (len.length == 0) {
			return cmd + " has error";
		} else {
			JSONObject object = new JSONObject();
			String command = len[1];
			switch (type) {
			case "delete":
				object.put("result", zkService.delete(clusterAlias, command));
				target = object.toJSONString();
				break;
			case "get":
				object.put("result", zkService.get(clusterAlias, command));
				target = object.toJSONString();
				break;
			case "ls":
				object.put("result", zkService.ls(clusterAlias, command));
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
	public String get(String clusterAlias, String type) {
		JSONObject target = new JSONObject();
		if ("zk".equals(type)) {
			String zkCluster = zkService.zkCluster(clusterAlias);
			target.put("zk", JSON.parseArray(zkCluster));
		} else if ("kafka".equals(type)) {
			List<BrokersInfo> kafkaBrokers = kafkaService.getAllBrokersInfo(clusterAlias);
			for (BrokersInfo broker : kafkaBrokers) {
				String version = kafkaService.getKafkaVersion(broker.getHost(), broker.getJmxPort(), broker.getIds(), clusterAlias);
				broker.setVersion(version);
			}
			target.put("kafka", JSON.parseArray(kafkaBrokers.toString()));
		}
		return target.toJSONString();
	}

	@Override
	public boolean hasClusterAlias(String clusterAlias) {
		String[] multiClusters = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
		for (String cluster : multiClusters) {
			if (cluster.equals(clusterAlias)) {
				return true;
			}
		}
		return false;
	}

	/** Get Zookeeper whether live. */
	public JSONObject status(String clusterAlias) {
		return zkService.zkCliStatus(clusterAlias);
	}

}
