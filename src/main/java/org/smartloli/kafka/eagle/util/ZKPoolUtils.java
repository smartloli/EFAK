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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import kafka.utils.ZKStringSerializer$;

import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZkClient pool utils.
 *
 * @author smartloli.
 *
 *         Created by Aug 14, 2016.
 * 
 *         Update by hexiang 20170216
 */
public final class ZKPoolUtils {

	private final static Logger LOG = LoggerFactory.getLogger(ZKPoolUtils.class);
	private static ZKPoolUtils instance = null;
	/** Zookeeper client connection pool. */
	private static Map<String, Vector<ZkClient>> zkCliPools = new HashMap<>();
	/** Set pool max size. */
	private final static int zkCliPoolSize = SystemConfigUtils.getIntProperty("kafka.zk.limit.size");
	/** Serializer Zookeeper client pool. */
	private static Map<String, Vector<ZkClient>> zkCliPoolsSerializer = new HashMap<>();

	private static Map<String, String> clusterAliass = new HashMap<>();

	/** Init ZkClient pool numbers. */
	static {
		for (String clusterAlias : SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",")) {
			clusterAliass.put(clusterAlias, SystemConfigUtils.getProperty(clusterAlias + ".zk.list"));
		}
		for (Entry<String, String> entry : clusterAliass.entrySet()) {
			Vector<ZkClient> zkCliPool = new Vector<ZkClient>(zkCliPoolSize);
			Vector<ZkClient> zkCliPoolSerializer = new Vector<ZkClient>(zkCliPoolSize);
			ZkClient zkc = null;
			ZkClient zkSerializer = null;
			for (int i = 0; i < zkCliPoolSize; i++) {
				try {
					zkc = new ZkClient(entry.getValue());
					zkCliPool.add(zkc);

					zkSerializer = new ZkClient(entry.getValue(), Integer.MAX_VALUE, 100000, ZKStringSerializer$.MODULE$);
					zkCliPoolSerializer.add(zkSerializer);
				} catch (Exception ex) {
					LOG.error(ex.getMessage());
				}
			}
			zkCliPools.put(entry.getKey(), zkCliPool);
			zkCliPoolsSerializer.put(entry.getKey(), zkCliPoolSerializer);
		}

	}

	/** Single model get ZkClient object. */
	public synchronized static ZKPoolUtils getInstance() {
		if (instance == null) {
			instance = new ZKPoolUtils();
		}
		return instance;
	}

	/** Reback pool one of ZkClient object. */
	public synchronized ZkClient getZkClient(String clusterAlias) {
		Vector<ZkClient> zkCliPool = zkCliPools.get(clusterAlias);
		ZkClient zkc = null;
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
						zkc = new ZkClient(clusterAliass.get(clusterAlias));
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
		}
		return zkc;
	}

	/** Get zk client by serializer. */
	public synchronized ZkClient getZkClientSerializer(String clusterAlias) {
		Vector<ZkClient> zkCliPoolSerializer = zkCliPoolsSerializer.get(clusterAlias);
		ZkClient zkSerializer = null;
		if (zkCliPoolSerializer.size() > 0) {
			ZkClient zkc = zkCliPoolSerializer.get(0);
			zkCliPoolSerializer.remove(0);
			String osName = System.getProperties().getProperty("os.name");
			if (osName.contains("Linux")) {
				LOG.debug("Get poolZKSerializer,and available size [" + zkCliPoolSerializer.size() + "]");
			} else {
				LOG.info("Get poolZKSerializer,and available size [" + zkCliPoolSerializer.size() + "]");
			}
			return zkc;
		} else {
			for (int i = 0; i < zkCliPoolSize; i++) {
				try {
					zkSerializer = new ZkClient(clusterAliass.get(clusterAlias), Integer.MAX_VALUE, 100000, ZKStringSerializer$.MODULE$);
					zkCliPoolSerializer.add(zkSerializer);
				} catch (Exception e) {
					LOG.error(e.getMessage());
				}
			}

			ZkClient zkc = zkCliPoolSerializer.get(0);
			zkCliPoolSerializer.remove(0);
			String osName = System.getProperties().getProperty("os.name");
			if (osName.contains("Linux")) {
				LOG.debug("get poolZKSerializer,and available size [" + zkCliPoolSerializer.size() + "]");
			} else {
				LOG.warn("get poolZKSerializer,and available size [" + zkCliPoolSerializer.size() + "]");
			}
			return zkc;
		}
	}

	/** Release ZkClient object. */
	public synchronized void release(String clusterAlias, ZkClient zkc) {
		Vector<ZkClient> zkCliPool = zkCliPools.get(clusterAlias);
		if (zkCliPool.size() < zkCliPoolSize) {
			zkCliPool.add(zkc);
		}
		String osName = System.getProperties().getProperty("os.name");
		if (osName.contains("Linux")) {
			LOG.debug("Release pool,and available size [" + zkCliPool.size() + "]");
		} else {
			LOG.info("Release pool,and available size [" + zkCliPool.size() + "]");
		}
	}

	/** Release ZkClient Serializer object. */
	public synchronized void releaseZKSerializer(String clusterAlias, ZkClient zkc) {
		Vector<ZkClient> zkCliPoolSerializer = zkCliPoolsSerializer.get(clusterAlias);
		if (zkCliPoolSerializer.size() < zkCliPoolSize) {
			zkCliPoolSerializer.add(zkc);
		}
		String osName = System.getProperties().getProperty("os.name");
		if (osName.contains("Linux")) {
			LOG.debug("Release poolZKSerializer,and available size [" + zkCliPoolSerializer.size() + "]");
		} else {
			LOG.info("Release poolZKSerializer,and available size [" + zkCliPoolSerializer.size() + "]");
		}
	}

	/** Construction method. */
	private ZKPoolUtils() {
	}

}
