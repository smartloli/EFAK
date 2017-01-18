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
package org.smartloli.kafka.eagle.factory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.domain.AlarmDomain;
import org.smartloli.kafka.eagle.domain.OffsetsLiteDomain;
import org.smartloli.kafka.eagle.util.CalendarUtils;
import org.smartloli.kafka.eagle.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.util.ZKPoolUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import kafka.utils.ZkUtils;
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.Seq;

/**
 * Implements ZkService all method.
 * 
 * @author smartloli.
 *
 *         Created by Jan 18, 2017
 * 
 * @see org.smartloli.kafka.eagle.factory.ZkService
 */
public class ZkServiceImpl implements ZkService {

	private final static Logger LOG = LoggerFactory.getLogger(ZkServiceImpl.class);

	private final String KE_ROOT_PATH = "/kafka_eagle";
	private final String STORE_OFFSETS = "offsets";
	private final String STORE_ALARM = "alarm";
	/** Request memory space. */
	private ZkClient zkc = null;
	/** Instance zookeeper client pool. */
	private ZKPoolUtils zkPool = ZKPoolUtils.getInstance();

	/** Zookeeper delete command. */
	public String delete(String cmd) {
		String ret = "";
		ZkClient zkc = zkPool.getZkClient();
		boolean status = ZkUtils.pathExists(zkc, cmd);
		if (status) {
			if (zkc.delete(cmd)) {
				ret = "[" + cmd + "] has delete success";
			} else {
				ret = "[" + cmd + "] has delete failed";
			}
		}
		if (zkc != null) {
			zkPool.release(zkc);
			zkc = null;
		}
		return ret;
	}

	/** Zookeeper get command. */
	public String get(String cmd) {
		String ret = "";
		ZkClient zkc = zkPool.getZkClientSerializer();
		boolean status = ZkUtils.pathExists(zkc, cmd);
		if (status) {
			Tuple2<Option<String>, Stat> tuple2 = ZkUtils.readDataMaybeNull(zkc, cmd);
			ret += tuple2._1.get() + "\n";
			ret += "cZxid = " + tuple2._2.getCzxid() + "\n";
			ret += "ctime = " + tuple2._2.getCtime() + "\n";
			ret += "mZxid = " + tuple2._2.getMzxid() + "\n";
			ret += "mtime = " + tuple2._2.getMtime() + "\n";
			ret += "pZxid = " + tuple2._2.getPzxid() + "\n";
			ret += "cversion = " + tuple2._2.getCversion() + "\n";
			ret += "dataVersion = " + tuple2._2.getVersion() + "\n";
			ret += "aclVersion = " + tuple2._2.getAversion() + "\n";
			ret += "ephemeralOwner = " + tuple2._2.getEphemeralOwner() + "\n";
			ret += "dataLength = " + tuple2._2.getDataLength() + "\n";
			ret += "numChildren = " + tuple2._2.getNumChildren() + "\n";
		}
		if (zkc != null) {
			zkPool.releaseZKSerializer(zkc);
			zkc = null;
		}
		return ret;
	}

	/** Get alarmer information. */
	public String getAlarm() {
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
	public String getOffsets(String group, String topic) {
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
	 * According to the date of each hour to statistics the consume rate data.
	 */
	private String getZkHour() {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHH");
		return df.format(new Date());
	}

	/**
	 * Insert new datasets.
	 * 
	 * @param list
	 *            New datasets.
	 */
	public void insert(List<OffsetsLiteDomain> list) {
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
	 * Insert new alarmer configure information.
	 * 
	 * @param alarm
	 *            New configure object.
	 * @return Integer.
	 */
	public int insertAlarmConfigure(AlarmDomain alarm) {
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

	/** Zookeeper ls command. */
	public String ls(String cmd) {
		String ret = "";
		ZkClient zkc = zkPool.getZkClient();
		boolean status = ZkUtils.pathExists(zkc, cmd);
		if (status) {
			ret = zkc.getChildren(cmd).toString();
		}
		if (zkc != null) {
			zkPool.release(zkc);
			zkc = null;
		}
		return ret;
	}

	/**
	 * Remove the metadata information in the Ke root directory in zookeeper,
	 * with group and topic as the only sign.
	 * 
	 * @param group
	 *            Consumer group.
	 * @param topic
	 *            Consumer topic.
	 * @param theme
	 *            Consumer theme.
	 */
	public void remove(String group, String topic, String theme) {
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
	 * Get zookeeper health status.
	 * 
	 * @param host
	 *            Zookeeper host
	 * @param port
	 *            Zookeeper port
	 * @return String.
	 */
	public String status(String host, String port) {
		String ret = "";
		Socket sock = null;
		try {
			String tmp = "";
			if (port.contains("/")) {
				tmp = port.split("/")[0];
			} else {
				tmp = port;
			}
			sock = new Socket(host, Integer.parseInt(tmp));
		} catch (Exception e) {
			LOG.error("Socket[" + host + ":" + port + "] connect refused");
			return "death";
		}
		BufferedReader reader = null;
		try {
			OutputStream outstream = sock.getOutputStream();
			outstream.write("stat".getBytes());
			outstream.flush();
			sock.shutdownOutput();

			reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.indexOf("Mode: ") != -1) {
					ret = line.replaceAll("Mode: ", "").trim();
				}
			}
		} catch (Exception ex) {
			LOG.error("Read ZK buffer has error,msg is " + ex.getMessage());
			return "death";
		} finally {
			try {
				sock.close();
				if (reader != null) {
					reader.close();
				}
			} catch (Exception ex) {
				LOG.error("Close read has error,msg is " + ex.getMessage());
			}
		}
		return ret;
	}

	/**
	 * Update metadata information in ke root path in zookeeper.
	 * 
	 * @param data
	 *            Update datasets.
	 * @param path
	 *            Update datasets path.
	 */
	private void update(String data, String path) {
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

	/** Get zookeeper cluster information. */
	public String zkCluster() {
		String[] zks = SystemConfigUtils.getPropertyArray("kafka.zk.list", ",");
		JSONArray arr = new JSONArray();
		int id = 1;
		for (String zk : zks) {
			JSONObject obj = new JSONObject();
			obj.put("id", id++);
			obj.put("ip", zk.split(":")[0]);
			obj.put("port", zk.split(":")[1]);
			obj.put("mode", status(zk.split(":")[0], zk.split(":")[1]));
			arr.add(obj);
		}
		return arr.toJSONString();
	}

	/** Judge whether the zkcli is active. */
	public JSONObject zkCliStatus() {
		JSONObject object = new JSONObject();
		ZkClient zkc = zkPool.getZkClient();
		if (zkc != null) {
			object.put("live", true);
			object.put("list", SystemConfigUtils.getProperty("kafka.zk.list"));
		} else {
			object.put("live", false);
			object.put("list", SystemConfigUtils.getProperty("kafka.zk.list"));
		}
		if (zkc != null) {
			zkPool.release(zkc);
			zkc = null;
		}
		return object;
	}
}
