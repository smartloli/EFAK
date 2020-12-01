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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.kafka.common.security.JaasUtils;
import org.apache.kafka.common.utils.Time;
import org.smartloli.kafka.eagle.common.util.KConstants.OperateSystem;

import kafka.zk.KafkaZkClient;

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
	private static Map<String, List<KafkaZkClient>> zkCliIdlePools = new HashMap<>();
	/** Zookeeper client current using connection poll. */
	private static Map<String, List<KafkaZkClient>> zkCliUsedPools = new HashMap<>();
	/** Set pool max size. */
	private static int zkCliPoolSize = SystemConfigUtils.getIntProperty("kafka.zk.limit.size");

	private static Map<String, String> clusterAliass = new HashMap<>();

	public static final int ZK_CONNECTION_TIMEOUT_MS = 30_000;
	public static final int ZK_SESSION_TIMEOUT_MS = 30_000;
	private static final String METRIC_GROUP_NAME = "topic-management-service";

	private static String errorMessageByZookeeper = "Get pool,and available size [{}]";
	private static String warningMessageByZookeeper = "Current used pool size [{}], need wait.";
	private static String releaseMessageByZookeeper = "Release pool,and available size [{}]";

	/** Init ZkClient pool numbers. */
	static {
		for (String clusterAlias : SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",")) {
			clusterAliass.put(clusterAlias, SystemConfigUtils.getProperty(clusterAlias + ".zk.list"));
		}
		for (Entry<String, String> entry : clusterAliass.entrySet()) {
			List<KafkaZkClient> zkCliIdlePool = new ArrayList<>(zkCliPoolSize);
			List<KafkaZkClient> zkCliUsedPool = new ArrayList<>(zkCliPoolSize);
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
						zkCliIdlePool.add(zkc);
					}
				} catch (Exception e) {
					ErrorUtils.print(KafkaZKPoolUtils.class).error("Error initializing zookeeper, msg is ", e);
				}
			}
			zkCliIdlePools.put(entry.getKey(), zkCliIdlePool);
			zkCliUsedPools.put(entry.getKey(), zkCliUsedPool);
		}

	}

	/** Single model get ZkClient object. */
	public static synchronized KafkaZKPoolUtils getInstance() {
		if (instance == null) {
			instance = new KafkaZKPoolUtils();
		}
		return instance;
	}

	/** Reback pool one of ZkClient object. */
	public synchronized KafkaZkClient getZkClient(String clusterAlias) {
		long nowTime = System.currentTimeMillis();
		List<KafkaZkClient> zkCliIdlePool = zkCliIdlePools.get(clusterAlias);
		// Connecting pool
		List<KafkaZkClient> zkCliUsedPool = zkCliUsedPools.get(clusterAlias);
		KafkaZkClient zkc = null;
		try {
			// if idle pool is not none, client can get one connection from it.
			if (!zkCliIdlePool.isEmpty()) {
				zkc = zkCliIdlePool.get(0);
				zkCliIdlePool.remove(0);
				zkCliUsedPool.add(zkc);
				String osName = System.getProperties().getProperty(OperateSystem.OS_NAME.getValue());
				if (osName.contains(OperateSystem.LINUX.getValue())) {
					ErrorUtils.print(KafkaZKPoolUtils.class).debug(errorMessageByZookeeper, zkCliIdlePool.size());
				} else {
					ErrorUtils.print(KafkaZKPoolUtils.class).info(errorMessageByZookeeper, zkCliIdlePool.size());
				}
			}
			// else if idle pool is null, and used pool have the same size as zkCliPoolSize, then we should wait.
			else if (zkCliUsedPool.size() == zkCliPoolSize){
				ErrorUtils.print(KafkaZKPoolUtils.class).warn(warningMessageByZookeeper, zkCliIdlePool.size());
				long waitUsed = System.currentTimeMillis() - nowTime;
				boolean getResource =  false;
				while (waitUsed< ZK_CONNECTION_TIMEOUT_MS && !getResource){
					if (!zkCliIdlePool.isEmpty()){
						zkc = zkCliIdlePool.get(0);
						zkCliIdlePool.remove(0);
						zkCliUsedPool.add(zkc);
						getResource = true;
					}else {
						Thread.sleep(1000);
						System.out.println("Sleep 1000 to wait ");
					}
					waitUsed = 1000 + waitUsed;
				}
				if(!getResource) {
					System.out.println("Cannot get zookeeper connection, please increase zookeeper pool size .");
				}
			}
			// else if idle pool is null ,and used pool size less than zkCliPoolSize, then we can create new connection.
			else{
				for (int i = 0; i < zkCliPoolSize-zkCliUsedPool.size(); i++) {
					zkc = KafkaZkClient.apply(clusterAliass.get(clusterAlias), JaasUtils.isZkSecurityEnabled(), ZK_SESSION_TIMEOUT_MS, ZK_CONNECTION_TIMEOUT_MS, Integer.MAX_VALUE, Time.SYSTEM, METRIC_GROUP_NAME, "SessionExpireListener");
					if (zkc != null) {
						zkCliIdlePool.add(zkc);
					}
				}
				String osName = System.getProperties().getProperty(OperateSystem.OS_NAME.getValue());
				if (osName.contains(OperateSystem.LINUX.getValue())) {
					ErrorUtils.print(KafkaZKPoolUtils.class).debug(errorMessageByZookeeper, zkCliIdlePool.size());
				} else {
					ErrorUtils.print(KafkaZKPoolUtils.class).warn(errorMessageByZookeeper, zkCliIdlePool.size());
				}
			}
		} catch (Exception e) {
			ErrorUtils.print(KafkaZKPoolUtils.class).error("Error initializing zookeeper, msg is ", e);
			ErrorUtils.print(KafkaZKPoolUtils.class).error("Kafka cluster[" + clusterAlias + ".zk.list] address has null.");
		}
		return zkc;
	}

	/** Release ZkClient object. */
	public synchronized void release(String clusterAlias, KafkaZkClient zkc) {
		List<KafkaZkClient> zkCliIdlePool = zkCliIdlePools.get(clusterAlias);
		List<KafkaZkClient> zkCliUsedPool = zkCliUsedPools.get(clusterAlias);
		if (zkCliIdlePool != null && zkCliIdlePool.size() < zkCliPoolSize && zkCliUsedPool !=null) {
			zkCliUsedPool.remove(zkc);
			zkCliIdlePool.add(zkc);
		}
		String osName = System.getProperties().getProperty(OperateSystem.OS_NAME.getValue());
		if (osName.contains(OperateSystem.LINUX.getValue())) {
			ErrorUtils.print(KafkaZKPoolUtils.class).debug(releaseMessageByZookeeper, (zkCliIdlePool == null ? 0 : zkCliIdlePool.size()));
		} else {
			ErrorUtils.print(KafkaZKPoolUtils.class).info(releaseMessageByZookeeper, (zkCliIdlePool == null ? 0 : zkCliIdlePool.size()));
		}
	}

	/** Construction method. */
	private KafkaZKPoolUtils() {
	}

}
