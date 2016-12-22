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

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.data.Stat;

import kafka.utils.ZkUtils;
import scala.Option;
import scala.Tuple2;

/**
 * @author smartloli.
 *
 *         Created by Nov 3, 2016
 */
public class ZKCliUtils {

	private static ZKPoolUtils zkPool = ZKPoolUtils.getInstance();

	public static String ls(String cmd) {
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

	public static String delete(String cmd) {
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

	public static String get(String cmd) {
		String ret = "";
		ZkClient zkc = zkPool.getZkClientSerializer();
		boolean status = ZkUtils.pathExists(zkc, cmd);
		if (status) {
			Tuple2<Option<String>, Stat> tuple2 = ZkUtils.readDataMaybeNull(zkc,
					cmd);
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

	public static void main(String[] args) {
		System.out.println(get("/kafka_eagle/offsets/group2/ke_test1"));
	}

}
