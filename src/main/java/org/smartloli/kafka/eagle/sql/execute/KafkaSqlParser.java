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
package org.smartloli.kafka.eagle.sql.execute;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.domain.KafkaSqlDomain;
import org.smartloli.kafka.eagle.factory.KafkaFactory;
import org.smartloli.kafka.eagle.factory.KafkaService;
import org.smartloli.kafka.eagle.sql.tool.JSqlUtils;

import com.alibaba.fastjson.JSONArray;

/**
 * Pre processing the SQL submitted by the client.
 * 
 * @author smartloli.
 *
 *         Created by Feb 28, 2017
 */
public class KafkaSqlParser {

	private final static Logger LOG = LoggerFactory.getLogger(KafkaSqlParser.class);

	public static String execute(String clusterAlias, String sql) {
		String results = "";
		try {
			KafkaService kafkaService = new KafkaFactory().create();
			KafkaSqlDomain kafkaSql = kafkaService.parseSql(clusterAlias, sql);
			LOG.info("KafkaSqlParser - SQL[" + kafkaSql.getSql() + "]");
			List<JSONArray> dataSets = SimpleKafkaConsumer.start(kafkaSql);
			results = JSqlUtils.query(kafkaSql.getSchema(), kafkaSql.getTableName(), dataSets, kafkaSql.getSql());
		} catch (Exception e) {
			LOG.error("Execute sql to query kafka topic has error,msg is " + e.getMessage());
		}
		return results;
	}
}
