/**
 * Licensed to the Apache Software Foundation (ASF) under one
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
package org.smartloli.kafka.eagle.ipc;

import java.util.ArrayList;
import java.util.List;

import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.core.sql.ignite.domain.TopicX;
import org.smartloli.kafka.eagle.core.sql.ignite.factory.KafkaSqlFactory;
import org.smartloli.kafka.eagle.core.sql.tool.JSqlUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Test Kafka Sql.
 * 
 * @author smartloli.
 *
 *         Created by Feb 27, 2018
 */
public class TestKSql {

	public static void main(String[] args) throws Exception {
		// ignite();
		// calcite();
		System.out.println(KConstants.Kafka.POSITION);
		JSONObject object = new JSONObject();
		object.put("owner", "");
		System.out.println();
		int i = 0;
		if (!"".equals(object.getString("owner"))&&object.getString("owner")!=null) {
			i++;
		}
		System.out.println(i);
	}

	public static void ignite() {
		List<TopicX> collectors = new ArrayList<>();
		int count = 0;
		for (int i = 0; i < 10; i++) {
			TopicX td = new TopicX();
			if (count > 3) {
				count = 0;
			}
			td.setPartitionId(count);
			td.setOffsets(i);
			td.setMessage("hello_" + i);
			td.setTopicName("test");
			collectors.add(td);
			count++;
		}

		String sql = "select offsets,message from TopicX where offsets>6 and partitionId in (0,1) limit 1";
		long stime = System.currentTimeMillis();
		KafkaSqlFactory.sql(sql, collectors);
		System.out.println("Cost time [" + (System.currentTimeMillis() - stime) / 1000.0 + "]ms");
	}

	public static void calcite() throws Exception {
		JSONObject tabSchema = new JSONObject();
		tabSchema.put("id", "integer");
		tabSchema.put("name", "varchar");
		tabSchema.put("age", "integer");

		String tableName = "stu";

		JSONArray dataSets = new JSONArray();

		for (int i = 0; i < 5000; i++) {
			JSONObject object = new JSONObject();
			object.put("id", i);
			object.put("name", "aa" + i);
			object.put("age", 10 + i);
			dataSets.add(object);
		}

		String sql = "select * from \"stu\" where \"id\"=0 and \"age\"=10 limit 10";

		List<JSONArray> dts = new ArrayList<>();
		dts.add(dataSets);
		long start = System.currentTimeMillis();
		String rs = JSqlUtils.query(tabSchema, tableName, dts, sql);
		System.out.println("[Spent] :: " + (System.currentTimeMillis() - start) + "ms");
		System.out.println(rs);
	}

}
