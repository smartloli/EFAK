/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.ipc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.core.sql.execute.KafkaSqlParser;
import org.smartloli.kafka.eagle.core.sql.tool.KSqlUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Test Kafka Sql.
 *
 * @author smartloli.
 * <p>
 * Created by Feb 27, 2018
 */
public class TestKSql {

    public static void main(String[] args) throws Exception {
        // calcite();
        // String sql = "select * from \"k20200326_1\" where \"partition\" in
        // (0) and \"msg\" like 's1%' limit 10";
        String sql = "select * from k20210607 where `partition` in (0) limit 10";

        String result = KafkaSqlParser.execute("cluster1", sql);
        System.out.println("result: " + result);
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

        String sql = "select * from stu where id=0 and age=10 limit 10";

        List<JSONArray> dts = new ArrayList<>();
        dts.add(dataSets);
        long start = System.currentTimeMillis();
        JSONObject object = KSqlUtils.query(tabSchema, tableName, dts, sql);
        String rs = object.getString("result");
        System.out.println("[Spent] :: " + (System.currentTimeMillis() - start) + "ms");
        System.out.println(rs);
    }

}
