/**
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
package org.smartloli.kafka.eagle.core.sql.tool;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.constant.JConstants;
import org.smartloli.kafka.eagle.core.sql.common.JSqlMapData;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

/**
 * Define the data structure, query by condition.
 *
 * @author smartloli.
 * <p>
 * Created by Mar 29, 2016
 */
public class JSqlUtils {

    private final static Logger LOG = LoggerFactory.getLogger(JSqlUtils.class);

    /**
     * @param tabSchema
     * @param dataSets
     * @param sql
     * @return
     * @throws Exception
     */
    public static String query(JSONObject tabSchema, Map<String, List<List<String>>> dataSets, String sql) throws Exception {
        Connection connection = null;
        ResultSet result = null;
        List<Map<String, Object>> ret = null;
        Statement st = null;
        try {
            String model = createTempJson();

            JSqlMapData.loadSchema(tabSchema, dataSets);

            Class.forName(JConstants.KAFKA_DRIVER);
            Properties info = new Properties();
            info.setProperty("lex", "JAVA");

            connection = DriverManager.getConnection("jdbc:calcite:model=inline:" + model, info);
            st = connection.createStatement();
            result = st.executeQuery(sql);
            ResultSetMetaData rsmd = result.getMetaData();
            ret = new ArrayList<Map<String, Object>>();
            while (result.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    map.put(rsmd.getColumnName(i), result.getString(rsmd.getColumnName(i)));
                }
                ret.add(map);
            }
        } catch (ClassNotFoundException e) {
            LOG.error(ExceptionUtils.getStackTrace(e));
        } catch (SQLException e) {
            LOG.error(ExceptionUtils.getStackTrace(e));
        } finally {
            if (st != null) {
                st.close();
            }
            if (result != null) {
                result.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
        return new Gson().toJson(ret);
    }

    /**
     * Parse datasets to datatable.
     */
    public static String toJSONObject(List<JSONArray> dataSets) {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        for (JSONArray dataSet : dataSets) {
            for (Object obj : dataSet) {
                JSONObject object = (JSONObject) obj;
                Map<String, Object> map = new HashMap<String, Object>();
                for (String key : object.keySet()) {
                    map.put(key, object.getString(key));
                }
                results.add(map);
            }
        }
        return new Gson().toJson(results);
    }

    private static String createTempJson() {
        JSONObject object = new JSONObject();
        object.put("version", "1.21.0");
        object.put("defaultSchema", "db");
        JSONArray array = new JSONArray();
        JSONObject tmp = new JSONObject();
        tmp.put("name", "db");
        tmp.put("type", "custom");
        tmp.put("factory", "org.smartloli.kafka.eagle.core.sql.schema.JSqlSchemaFactory");
        JSONObject tmp2 = new JSONObject();
        tmp.put("operand", tmp2.put("database", "calcite_memory_db"));
        array.add(tmp);
        object.put("schemas", array);
        return object.toJSONString();
    }

}
