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
	private static Map<String, List<KafkaZkClient>> zkCliPools = new HashMap<>();
	/** Set pool max size. */
	private static int zkCliPoolSize = SystemConfigUtils.getIntProperty("kafka.zk.limit.size");

	private static Map<String, String> clusterAliass = new HashMap<>();

	public static final int ZK_CONNECTION_TIMEOUT_MS = 30_000;
	public static final int ZK_SESSION_TIMEOUT_MS = 30_000;
	private static final String METRIC_GROUP_NAME = "topic-management-service";

	private static String errorMessageByZookeeper = "Get pool,and available size [{}]";
	private static String releaseMessageByZookeeper = "Release pool,and available size [{}]";

	/** Init ZkClient pool numbers. */
	static {
		for (String clusterAlias : SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",")) {
			clusterAliass.put(clusterAlias, SystemConfigUtils.getProperty(clusterAlias + ".zk.list"));
		}
		for (Entry<String, String> entry : clusterAliass.entrySet()) {
			List<KafkaZkClient> zkCliPool = new ArrayList<>(zkCliPoolSize);
			KafkaZkClient zkc = null;
			for (int i = 0; i < zkCliPoolSize; i++) {
				try {
					zkc = KafkaZkClient.apply(entry.getValue(), JaasUtils.isZkSecurityEnabled(), ZK_SESSION_TIMEOUT_MS, ZK_CONNECTION_TIMEOUT_MS, Integer.MAX_VALUE, Time.SYSTEM, METRIC_GROUP_NAME, "SessionExpireListener");
					if(zkc!=null) {
						zkCliPool.add(zkc);
					}
				} catch (Exception e) {
					ThrowExceptionUtils.print(KafkaZKPoolUtils.class).error("Error initializing zookeeper, msg is ", e);
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

	/** Reback pool one of ZkClient object. */
	public synchronized KafkaZkClient getZkClient(String clusterAlias) {
		List<KafkaZkClient> zkCliPool = zkCliPools.get(clusterAlias);
		KafkaZkClient zkc = null;
		try {
			if (!zkCliPool.isEmpty()) {
				zkc = zkCliPool.get(0);
				zkCliPool.remove(0);
				String osName = System.getProperties().getProperty(OperateSystem.OS_NAME.getValue());
				if (osName.contains(OperateSystem.LINUX.getValue())) {
					ThrowExceptionUtils.print(KafkaZKPoolUtils.class).debug(errorMessageByZookeeper, zkCliPool.size());
				} else {
					ThrowExceptionUtils.print(KafkaZKPoolUtils.class).info(errorMessageByZookeeper, zkCliPool.size());
				}
			} else {
				for (int i = 0; i < zkCliPoolSize; i++) {
					zkc = KafkaZkClient.apply(clusterAliass.get(clusterAlias), JaasUtils.isZkSecurityEnabled(), ZK_SESSION_TIMEOUT_MS, ZK_CONNECTION_TIMEOUT_MS, Integer.MAX_VALUE, Time.SYSTEM, METRIC_GROUP_NAME, "SessionExpireListener");
					if (zkc != null) {
						zkCliPool.add(zkc);
					}
				}

				zkc = zkCliPool.get(0);
				zkCliPool.remove(0);
				String osName = System.getProperties().getProperty(OperateSystem.OS_NAME.getValue());
				if (osName.contains(OperateSystem.LINUX.getValue())) {
					ThrowExceptionUtils.print(KafkaZKPoolUtils.class).debug(errorMessageByZookeeper, zkCliPool.size());
				} else {
					ThrowExceptionUtils.print(KafkaZKPoolUtils.class).warn(errorMessageByZookeeper, zkCliPool.size());
				}
			}
		} catch (Exception e) {
			ThrowExceptionUtils.print(KafkaZKPoolUtils.class).error("Error initializing zookeeper, msg is ", e);
			ThrowExceptionUtils.print(KafkaZKPoolUtils.class).error("Kafka cluster[" + clusterAlias + ".zk.list] address has null.");
		}
		return zkc;
	}

	/** Release ZkClient object. */
	public synchronized void release(String clusterAlias, KafkaZkClient zkc) {
		List<KafkaZkClient> zkCliPool = zkCliPools.get(clusterAlias);
		if (zkCliPool != null && zkCliPool.size() < zkCliPoolSize) {
			zkCliPool.add(zkc);
		}
		String osName = System.getProperties().getProperty(OperateSystem.OS_NAME.getValue());
		if (osName.contains(OperateSystem.LINUX.getValue())) {
			ThrowExceptionUtils.print(KafkaZKPoolUtils.class).debug(releaseMessageByZookeeper, (zkCliPool == null ? 0 : zkCliPool.size()));
		} else {
			ThrowExceptionUtils.print(KafkaZKPoolUtils.class).info(releaseMessageByZookeeper, (zkCliPool == null ? 0 : zkCliPool.size()));
		}
	}

	/** Construction method. */
	private KafkaZKPoolUtils() {
	}

}
