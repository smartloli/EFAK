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
package org.smartloli.kafka.eagle.sql;

import org.smartloli.kafka.eagle.core.sql.execute.KafkaSqlParser;

/**
 * Test kafka sql query.
 * 
 * @author smartloli.
 *
 *         Created by Feb 28, 2017
 */
public class TestKafkaParser {

	public static void main(String[] args) {
		// String sql = "SELECT \"partition\", \"offset\",\"msg\" from
		// \"kv-test2019\" where \"partition\" in (0) and \"offset\"=37445 group
		// by \"partition\" limit 10";
		String sql = "select * from \"kv-test2019\" where \"partition\" in (0) limit 10";
		String result = KafkaSqlParser.execute("cluster1", sql);
		System.out.println("result: " + result);
	}

}
