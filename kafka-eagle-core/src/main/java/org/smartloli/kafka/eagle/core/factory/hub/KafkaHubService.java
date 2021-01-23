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
package org.smartloli.kafka.eagle.core.factory.hub;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

/**
 * Used to realize data migration, capacity expansion, selection and other
 * central component operations.
 * 
 * @author smartloli.
 *
 *         Created by May 21, 2020
 */
public interface KafkaHubService {

	/** Generate topics. */
	public JSONObject generate(String clusterAlias, String reassignTopicsJson, List<Object> brokerIdList);

	/** Execute topics. */
	public String execute(String clusterAlias, String reassignTopicsJson);

	/** Verify topics. */
	public String verify(String clusterAlias, String reassignTopicsJson);

	/** Preferred replica leader election */
	public String prefReplicaElection(String clusterAlias, String topic);
}
