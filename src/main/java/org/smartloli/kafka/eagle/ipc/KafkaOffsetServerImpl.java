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
package org.smartloli.kafka.eagle.ipc;

import java.util.Map.Entry;

import org.apache.thrift.TException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import kafka.common.OffsetAndMetadata;
import kafka.server.GroupTopicPartition;

/**
 * TODO
 * 
 * @author smartloli.
 *
 *         Created by Jan 5, 2017
 */
public class KafkaOffsetServerImpl implements KafkaOffsetServer.Iface {

	@Override
	public String query(String group, String topic, int partition) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOffset() throws TException {
		JSONArray array = new JSONArray();
		for (Entry<GroupTopicPartition, OffsetAndMetadata> entry : KafkaOffsetGetter.offsetMap.entrySet()) {
			JSONObject object = new JSONObject();
			object.put("group", entry.getKey().group());
			object.put("topic", entry.getKey().topicPartition().topic());
			object.put("partition", entry.getKey().topicPartition().partition());
			object.put("offset", entry.getValue().offset());
			object.put("timestamp", entry.getValue().timestamp());
			array.add(object);
		}
		return array.toJSONString();
	}

	@Override
	public String sql(String sql) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getConsumer() throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getActiverConsumer() throws TException {
		return KafkaOffsetGetter.activeMap.toString();
	}

}
