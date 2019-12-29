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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import kafka.zk.KafkaZkClient;

import org.apache.kafka.common.security.JaasUtils;
import org.apache.kafka.common.utils.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KafkaZkClient pool utils.
 *
 * @author smartloli.
 *
 *         Created by Jan 11, 2019.
 */
public final class KafkaZKPoolUtils {

	private final static Logger LOG = LoggerFactory.getLogger(KafkaZKPoolUtils.class);
	private static KafkaZKPoolUtils instance = null;
	/** Zookeeper client connection pool. */
	private static Map<String, Vector<KafkaZkClient>> zkCliPools = new HashMap<>();
	/** Set pool max size. */
	private final static int zkCliPoolSize = SystemConfigUtils.getIntProperty("kafka.zk.limit.size");

	private static Map<String, String> clusterAliass = new HashMap<>();

	public static final int ZK_CONNECTION_TIMEOUT_MS = 30_000;
	public static final int ZK_SESSION_TIMEOUT_MS = 30_000;
	private static final String METRIC_GROUP_NAME = "topic-management-service";

	/** Init ZkClient pool numbers. */
	static {
		for (String clusterAlias : SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",")) {
			clusterAliass.put(clusterAlias, SystemConfigUtils.getProperty(clusterAlias + ".zk.list"));
		}
		for (Entry<String, String> entry : clusterAliass.entrySet()) {
			Vector<KafkaZkClient> zkCliPool = new Vector<KafkaZkClient>(zkCliPoolSize);
			KafkaZkClient zkc = null;
			for (int i = 0; i < zkCliPoolSize; i++) {
				try {
					zkc = KafkaZkClient.apply(entry.getValue(), JaasUtils.isZkSecurityEnabled(), ZK_SESSION_TIMEOUT_MS, ZK_CONNECTION_TIMEOUT_MS, Integer.MAX_VALUE, Time.SYSTEM, METRIC_GROUP_NAME, "SessionExpireListener");
					zkCliPool.add(zkc);
				} catch (Exception ex) {
					LOG.error(ex.getMessage());
				}
			}
			zkCliPools.put(entry.getKey(), zkCliPool);
		}

	}

	/** Single model get ZkClient object. */
	public synchronized static KafkaZKPoolUtils getInstance() {
		if (instance == null) {
			instance = new KafkaZKPoolUtils();
		}
		return instance;
	}

	/** Reback pool one of ZkClient object. */
	public synchronized KafkaZkClient getZkClient(String clusterAlias) {
		Vector<KafkaZkClient> zkCliPool = zkCliPools.get(clusterAlias);
		KafkaZkClient zkc = null;
		try {
			if (zkCliPool.size() > 0) {
				zkc = zkCliPool.get(0);
				zkCliPool.remove(0);
				String osName = System.getProperties().getProperty("os.name");
				if (osName.contains("Linux")) {
					LOG.debug("Get pool,and available size [" + zkCliPool.size() + "]");
				} else {
					LOG.info("Get pool,and available size [" + zkCliPool.size() + "]");
				}
			} else {
				for (int i = 0; i < zkCliPoolSize; i++) {
					try {
						zkc = KafkaZkClient.apply(clusterAliass.get(clusterAlias), JaasUtils.isZkSecurityEnabled(), ZK_SESSION_TIMEOUT_MS, ZK_CONNECTION_TIMEOUT_MS, Integer.MAX_VALUE, Time.SYSTEM, METRIC_GROUP_NAME,
								"SessionExpireListener");
						zkCliPool.add(zkc);
					} catch (Exception ex) {
						LOG.error(ex.getMessage());
					}
				}

				zkc = zkCliPool.get(0);
				zkCliPool.remove(0);
				String osName = System.getProperties().getProperty("os.name");
				if (osName.contains("Linux")) {
					LOG.debug("Get pool,and available size [" + zkCliPool.size() + "]");
				} else {
					LOG.warn("Get pool,and available size [" + zkCliPool.size() + "]");
				}
			}
		} catch (Exception e) {
			LOG.error("ZK init has error,msg is " + e.getMessage());
			LOG.error("Kafka cluster[" + clusterAlias + ".zk.list] address has null.");
		}
		return zkc;
	}

	/** Release ZkClient object. */
	public synchronized void release(String clusterAlias, KafkaZkClient zkc) {
		Vector<KafkaZkClient> zkCliPool = zkCliPools.get(clusterAlias);
		if (zkCliPool != null && zkCliPool.size() < zkCliPoolSize) {
			zkCliPool.add(zkc);
		}
		String osName = System.getProperties().getProperty("os.name");
		if (osName.contains("Linux")) {
			LOG.debug("Release pool,and available size [" + (zkCliPool == null ? 0 : zkCliPool.size()) + "]");
		} else {
			LOG.info("Release pool,and available size [" + (zkCliPool == null ? 0 : zkCliPool.size()) + "]");
		}
	}

	/** Construction method. */
	private KafkaZKPoolUtils() {
	}

}
