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
package org.smartloli.kafka.eagle.core.sql.function;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Objects;

/**
 * Parse a JSONObject or a JSONArray in a kafka topic message using a custom
 * function.
 * 
 * @author smartloli.
 *
 *         Created by Feb 16, 2019
 */
public class JSONFunction {

	/** Parse a JSONObject. */
	public String JSON(String jsonObject, String key) {
		JSONObject object = null;
		try {
			Objects.requireNonNull(jsonObject);
			object = com.alibaba.fastjson.JSON.parseObject(jsonObject);
		} catch (Exception e) {
			return null;
		}
		return object.getString(key);
	}

	/** Parse a JSONArray. */
	public String JSONS(String jsonArray, String key) {
		JSONArray target = null;
		try {
			Objects.requireNonNull(jsonArray);
			JSONArray object = com.alibaba.fastjson.JSON.parseArray(jsonArray);
			target = new JSONArray();
			for (Object tmp : object) {
				JSONObject result = (JSONObject) tmp;
				JSONObject value = new JSONObject();
				value.put(key, result.getString(key));
				target.add(value);
			}
		} catch (Exception e) {
			return null;
		}
		return target.toJSONString();
	}
}
