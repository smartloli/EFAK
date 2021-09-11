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
package org.smartloli.kafka.eagle.core.factory;

import com.alibaba.fastjson.JSONObject;

/**
 * ZkService operate comand and get metadata from zookeeper interface.
 * 
 * @author smartloli.
 *
 *         Created by Jan 18, 2017.
 * 
 *         Update by hexiang 20170216
 */
public interface ZkService {

	/** Zookeeper delete command. */
	public String delete(String clusterAlias, String cmd);

	/** Find zookeeper leader node. */
	public String findZkLeader(String clusterAlias);

	/** Zookeeper get command. */
	public String get(String clusterAlias, String cmd);

	/** Zookeeper ls command. */
	public String ls(String clusterAlias, String cmd);

	/** Get zookeeper health status. */
	public String status(String host, String port);

	/** Get zookeeper version. */
	public String version(String host, String port);

	/** Get zookeeper cluster information. */
	public String zkCluster(String clusterAlias);

	/** Judge whether the zkcli is active. */
	public JSONObject zkCliStatus(String clusterAlias);

}
