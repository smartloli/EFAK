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
package org.smartloli.kafka.eagle.common.util;

import kafka.zk.KafkaZkClient;
import org.apache.kafka.common.security.JaasUtils;
import org.apache.kafka.common.utils.Time;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * KafkaZkClient pool utils.
 *
 * @author smartloli.
 *
 *         Created by Jan 11, 2019.
 */
public final class KafkaZKPoolUtils {

	private static KafkaZKPoolUtils instance = null;
	/** Zookeeper client connection pool. */
	private static Map<String, List<KafkaZkClient>> zkCliPools = new HashMap<>();
	/** Set pool max size. */
	private static int zkCliPoolSize = SystemConfigUtils.getIntProperty("kafka.zk.limit.size");

	private static Map<String, String> clusterAliass = new HashMap<>();

	public static final int ZK_CONNECTION_TIMEOUT_MS = 30_000;
	public static final int ZK_SESSION_TIMEOUT_MS = 30_000;
	private static final String METRIC_GROUP_NAME = "topic-management-service";

	/** Init ZkClient pool numbers. */
	static {
		for (String clusterAlias : SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",")) {
			clusterAliass.put(clusterAlias, SystemConfigUtils.getProperty(clusterAlias + ".zk.list"));
		}
		for (Map.Entry<String, String> entry : clusterAliass.entrySet()) {
			List<KafkaZkClient> zkCliPool = new ArrayList<>(zkCliPoolSize);
			KafkaZkClient zkc = null;
			for (int i = 0; i < zkCliPoolSize; i++) {
				try {
					zkc = KafkaZkClient.apply(entry.getValue(), JaasUtils.isZkSecurityEnabled(), ZK_SESSION_TIMEOUT_MS, ZK_CONNECTION_TIMEOUT_MS, Integer.MAX_VALUE, Time.SYSTEM, METRIC_GROUP_NAME, "SessionExpireListener");
					if (zkc != null) {
						if (SystemConfigUtils.getBooleanProperty(entry.getKey() + ".zk.acl.enable")) {
							String schema = SystemConfigUtils.getProperty(entry.getKey() + ".zk.acl.schema");
							String username = SystemConfigUtils.getProperty(entry.getKey() + ".zk.acl.username");
							String password = SystemConfigUtils.getProperty(entry.getKey() + ".zk.acl.password");
							try {
								zkc.currentZooKeeper().addAuthInfo(schema, (username + ":" + password).getBytes());
							} catch (Exception e) {
								ErrorUtils.print(KafkaZKPoolUtils.class).error("ClusterAlias[" + entry.getKey() + "] add acl has error, msg is ", e);
							}
						}
						zkCliPool.add(zkc);
					}
				} catch (Exception e) {
					ErrorUtils.print(KafkaZKPoolUtils.class).error("Error initializing zookeeper, msg is ", e);
				}
			}
			zkCliPools.put(entry.getKey(), zkCliPool);
		}

	}

	/** Single model get ZkClient object. */
	public static synchronized KafkaZKPoolUtils getInstance() {
		if (instance == null) {
			instance = new KafkaZKPoolUtils();
		}
		return instance;
	}

	private boolean zkClientIsActive(KafkaZkClient zkc){
		if(zkc==null){
			return false;
		}
		try{
			zkc.getAllBrokersInCluster();
			return true;
		}catch (Exception e){
			ErrorUtils.print(KafkaZKPoolUtils.class).error("zookeeper client not alive!", e);
		}
		return false;
	}

	/** Reback pool one of ZkClient object. */
	public  KafkaZkClient getZkClient(String clusterAlias) {
		//fix issues-470,ZooKeeper client is thread safe.
		//So, in general, we can keep the persistent connection.
		List<KafkaZkClient> zkCliPool = zkCliPools.get(clusterAlias);
		int n = ThreadLocalRandom.current().nextInt(zkCliPool.size());
		return zkCliPool.get(n);
	}

	/** Release ZkClient object. */
	public void release(String clusterAlias, KafkaZkClient zkc) {
		//do nothing.just keep code style
	}

	/** Construction method. */
	private KafkaZKPoolUtils() {
	}


}
