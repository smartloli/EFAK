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
package org.smartloli.kafka.eagle.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import kafka.utils.ZkUtils;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.domain.AlarmDomain;
import org.smartloli.kafka.eagle.domain.OffsetsLiteDomain;

import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.Seq;

/**
 * Storing metadata to zookeeper and providing read and write methods.
 * 
 * @author smartloli.
 *
 *         Created by Sep 12, 2016
 */
public class ZKDataUtils {

	private final static Logger LOG = LoggerFactory.getLogger(ZKDataUtils.class);
	private static ZKPoolUtils zkPool = ZKPoolUtils.getInstance();
	private static ZkClient zkc = null;
	private final static String KE_ROOT_PATH = "/kafka_eagle";
	private final static String STORE_OFFSETS = "offsets";
	private final static String STORE_ALARM = "alarm";

	/**
	 * According to the date of each hour to statistics the consume rate data.
	 */
	private static String getZkHour() {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHH");
		return df.format(new Date());
	}

	/** Get alarmer information. */
	public static String getAlarm() {
		JSONArray array = new JSONArray();
		if (zkc == null) {
			zkc = zkPool.getZkClient();
		}
		String path = KE_ROOT_PATH + "/" + STORE_ALARM;
		if (ZkUtils.pathExists(zkc, path)) {
			Seq<String> seq = ZkUtils.getChildren(zkc, path);
			List<String> listSeq = JavaConversions.seqAsJavaList(seq);
			for (String group : listSeq) {
				Seq<String> seq2 = ZkUtils.getChildren(zkc, path + "/" + group);
				List<String> listSeq2 = JavaConversions.seqAsJavaList(seq2);
				for (String topic : listSeq2) {
					try {
						JSONObject object = new JSONObject();
						object.put("group", group);
						object.put("topic", topic);
						Tuple2<Option<String>, Stat> tuple = ZkUtils.readDataMaybeNull(zkc, path + "/" + group + "/" + topic);
						object.put("created", CalendarUtils.convertUnixTime2Date(tuple._2.getCtime()));
						object.put("modify", CalendarUtils.convertUnixTime2Date(tuple._2.getMtime()));
						long lag = JSON.parseObject(tuple._1.get()).getLong("lag");
						String owner = JSON.parseObject(tuple._1.get()).getString("owner");
						object.put("lag", lag);
						object.put("owner", owner);
						array.add(object);
					} catch (Exception ex) {
						LOG.error("[ZK.getAlarm] has error,msg is " + ex.getMessage());
					}
				}
			}
		}
		if (zkc != null) {
			zkPool.release(zkc);
			zkc = null;
		}
		return array.toJSONString();
	}

	/**
	 * Get consumer data that has group and topic as the only sign.
	 * 
	 * @param group
	 *            Consumer group.
	 * @param topic
	 *            Consumer topic.
	 * @return String.
	 */
	public static String getOffsets(String group, String topic) {
		String data = "";
		if (zkc == null) {
			zkc = zkPool.getZkClient();
		}
		String path = KE_ROOT_PATH + "/" + STORE_OFFSETS + "/" + group + "/" + topic;
		if (ZkUtils.pathExists(zkc, path)) {
			try {
				Tuple2<Option<String>, Stat> tuple = ZkUtils.readDataMaybeNull(zkc, path);
				JSONObject obj = JSON.parseObject(tuple._1.get());
				if (getZkHour().equals(obj.getString("hour"))) {
					data = obj.toJSONString();
				}
			} catch (Exception ex) {
				LOG.error("[ZK.getOffsets] has error,msg is " + ex.getMessage());
			}
		}
		if (zkc != null) {
			zkPool.release(zkc);
			zkc = null;
		}
		return data;
	}

	/**
	 * Update metadata information in ke root path in zookeeper.
	 * 
	 * @param data
	 *            Update datasets.
	 * @param path
	 *            Update datasets path.
	 */
	private static void update(String data, String path) {
		if (zkc == null) {
			zkc = zkPool.getZkClient();
		}
		if (!ZkUtils.pathExists(zkc, KE_ROOT_PATH + "/" + path)) {
			ZkUtils.createPersistentPath(zkc, KE_ROOT_PATH + "/" + path, "");
		}
		if (ZkUtils.pathExists(zkc, KE_ROOT_PATH + "/" + path)) {
			ZkUtils.updatePersistentPath(zkc, KE_ROOT_PATH + "/" + path, data);
		}
		if (zkc != null) {
			zkPool.release(zkc);
			zkc = null;
		}
	}

	/**
	 * Insert new datasets.
	 * 
	 * @param list
	 *            New datasets.
	 */
	public static void insert(List<OffsetsLiteDomain> list) {
		String hour = getZkHour();
		for (OffsetsLiteDomain offset : list) {
			JSONObject obj = new JSONObject();
			obj.put("hour", hour);

			JSONObject object = new JSONObject();
			object.put("lag", offset.getLag());
			object.put("lagsize", offset.getLogSize());
			object.put("offsets", offset.getOffsets());
			object.put("created", offset.getCreated());
			String json = getOffsets(offset.getGroup(), offset.getTopic());
			JSONObject tmp = JSON.parseObject(json);
			JSONArray zkArrayData = new JSONArray();
			if (tmp != null && tmp.size() > 0) {
				String zkHour = tmp.getString("hour");
				if (hour.equals(zkHour)) {
					String zkData = tmp.getString("data");
					zkArrayData = JSON.parseArray(zkData);
				}
			}
			if (zkArrayData.size() > 0) {
				zkArrayData.add(object);
				obj.put("data", zkArrayData);
			} else {
				obj.put("data", Arrays.asList(object));
			}
			update(obj.toJSONString(), STORE_OFFSETS + "/" + offset.getGroup() + "/" + offset.getTopic());
		}
	}

	/**
	 * Delete the metadata information in the Ke root directory in zookeeper,
	 * with group and topic as the only sign.
	 * 
	 * @param group
	 *            Consumer group.
	 * @param topic
	 *            Consumer topic.
	 * @param theme
	 *            Consumer theme.
	 */
	public static void delete(String group, String topic, String theme) {
		if (zkc == null) {
			zkc = zkPool.getZkClient();
		}
		String path = theme + "/" + group + "/" + topic;
		if (ZkUtils.pathExists(zkc, KE_ROOT_PATH + "/" + path)) {
			ZkUtils.deletePath(zkc, KE_ROOT_PATH + "/" + path);
		}
		if (zkc != null) {
			zkPool.release(zkc);
			zkc = null;
		}
	}

	/**
	 * Insert new alarmer configure information.
	 * 
	 * @param alarm
	 *            New configure object.
	 * @return Integer.
	 */
	public static int insertAlarmConfigure(AlarmDomain alarm) {
		JSONObject object = new JSONObject();
		object.put("lag", alarm.getLag());
		object.put("owner", alarm.getOwners());
		try {
			update(object.toJSONString(), STORE_ALARM + "/" + alarm.getGroup() + "/" + alarm.getTopics());
		} catch (Exception ex) {
			LOG.error("Insert alarmer configure object has error,msg is " + ex.getMessage());
			return -1;
		}
		return 0;
	}

}
