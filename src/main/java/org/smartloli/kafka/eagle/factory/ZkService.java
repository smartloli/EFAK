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

import java.util.List;

import org.smartloli.kafka.eagle.domain.AlarmDomain;
import org.smartloli.kafka.eagle.domain.OffsetsLiteDomain;

import com.alibaba.fastjson.JSONObject;

/**
 * TODO
 * 
 * @author smartloli.
 *
 *         Created by Jan 18, 2017
 */
public interface ZkService {

	/** Zookeeper delete command. */
	public String delete(String cmd);

	/** Zookeeper get command. */
	public String get(String cmd);

	/** Get alarmer information. */
	public String getAlarm();

	/** Get consumer data that has group and topic as the only sign. */
	public String getOffsets(String group, String topic);

	/** Insert new datasets. */
	public void insert(List<OffsetsLiteDomain> list);

	/** Insert new alarmer configure information. */
	public int insertAlarmConfigure(AlarmDomain alarm);

	/** Zookeeper ls command. */
	public String ls(String cmd);

	/**
	 * Remove the metadata information in the Ke root directory in
	 * zookeeper,with group and topic as the only sign.
	 */
	public void remove(String group, String topic, String theme);

	/** Get zookeeper health status. */
	public String status(String host, String port);

	/** Get zookeeper cluster information. */
	public String zkCluster();

	/** Judge whether the zkcli is active. */
	public JSONObject zkCliStatus();

}
