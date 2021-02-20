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

import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.web.service.AclService;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;

/**
 * Kafka acl data interface, and set up the return data set.
 *
 * @author jeff.
 *
 */
@Service
public class AclServiceImpl implements AclService {

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	/** Get acl data from kafka cluster. */
	public JSONArray getAcls(String clusterAlias) {
		JSONArray activeTopics = kafkaService.getKafkaAcl(clusterAlias);

		return activeTopics;
	}

	@Override
	public JSONArray getTopicAcls(String clusterAlias, String topicname) {
		JSONArray acls = kafkaService.getKafkaAclBYTopicName(clusterAlias, topicname);

		return acls;
	}
	
}






