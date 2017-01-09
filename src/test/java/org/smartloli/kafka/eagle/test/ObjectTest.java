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
package org.smartloli.kafka.eagle.test;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.smartloli.kafka.eagle.ipc.RpcClient;
import org.smartloli.kafka.eagle.util.CalendarUtils;

import com.google.gson.Gson;

/**
 * TODO
 * 
 * @author smartloli.
 *
 *         Created by Jan 4, 2017
 */
public class ObjectTest {

	public static void main(String[] args) throws UnknownHostException {
		// Mill => 1483670359006
		// GetMill => 1483670352215
		// GetMill => 1483670351214
		// Mill => 1483670358005
		// GetMill => 1483670350212
		// Mill => 1483670358004
		System.out.println(CalendarUtils.timeSpan2StrDate(1483927797771L));
		Map<String, List<String>> type = new HashMap<String, List<String>>();
		Gson gson = new Gson();
		Map<String, List<String>> map = gson.fromJson(RpcClient.getConsumer(), type.getClass());
		System.out.println(map.size());
	}

}
