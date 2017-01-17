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
 *         Created by Aug 14, 2016
 */
public final class ZKPoolUtils {
	
	private final static Logger LOG = LoggerFactory.getLogger(ZKPoolUtils.class);
	private static ZKPoolUtils instance = null;
	/** Zookeeper client connection pool. */
	private Vector<ZkClient> pool;
	/** Set pool max size. */
	private int poolSize = SystemConfigUtils.getIntProperty("kafka.zk.limit.size");
	/** Serializer Zookeeper client pool. */
	private Vector<ZkClient> poolZKSerializer;
	/** Get Zookeeper client address. */
	private String zkCliAddress = SystemConfigUtils.getProperty("kafka.zk.list");

	/** Init ZkClient pool numbers. */
	private void addZkClient() {
		ZkClient zkc = null;
		for (int i = 0; i < poolSize; i++) {
			try {
				zkc = new ZkClient(zkCliAddress);
				pool.add(zkc);
			} catch (Exception ex) {
				LOG.error(ex.getMessage());
			}
		}
	}

	/** Add serializer zkclient. */
	private void addZkSerializerClient() {
		ZkClient zkSerializer = null;
		for (int i = 0; i < poolSize; i++) {
			try {
				zkSerializer = new ZkClient(zkCliAddress, Integer.MAX_VALUE, 100000, ZKStringSerializer$.MODULE$);
				poolZKSerializer.add(zkSerializer);
			} catch (Exception ex) {
				LOG.error(ex.getMessage());
			}
		}
	}

	/** Close ZkClient pool. */
	public synchronized void closePool() {
		if (pool != null && pool.size() > 0) {
			for (int i = 0; i < pool.size(); i++) {
				try {
					pool.get(i).close();
				} catch (Exception ex) {
					LOG.error(ex.getMessage());
				} finally {
					pool.remove(i);
				}
			}
		}
	
		if (poolZKSerializer != null && poolZKSerializer.size() > 0) {
			for (int i = 0; i < poolZKSerializer.size(); i++) {
				try {
					poolZKSerializer.get(i).close();
				} catch (Exception ex) {
					LOG.error(ex.getMessage());
				} finally {
					poolZKSerializer.remove(i);
				}
			}
		}
		instance = null;
	}

	/** Single model get ZkClient object. */
	public synchronized static ZKPoolUtils getInstance() {
		if (instance == null) {
			instance = new ZKPoolUtils();
		}
		return instance;
	}

	/** Reback pool one of ZkClient object. */
	public synchronized ZkClient getZkClient() {
		ZkClient zkc = null;
		try {
			if (pool.size() > 0) {
				zkc = pool.get(0);
				pool.remove(0);
				String osName = System.getProperties().getProperty("os.name");
				if (osName.contains("Linux")) {
					LOG.debug("Get pool,and available size [" + pool.size() + "]");
				} else {
					LOG.info("Get pool,and available size [" + pool.size() + "]");
				}
			} else {
				addZkClient();
				zkc = pool.get(0);
				pool.remove(0);
				String osName = System.getProperties().getProperty("os.name");
				if (osName.contains("Linux")) {
					LOG.debug("Get pool,and available size [" + pool.size() + "]");
				} else {
					LOG.warn("Get pool,and available size [" + pool.size() + "]");
				}
			}
		} catch (Exception e) {
			LOG.error("ZK init has error,msg is " + e.getMessage());
		}
		return zkc;
	}

	/** Get zk client by serializer. */
	public synchronized ZkClient getZkClientSerializer() {
		if (poolZKSerializer.size() > 0) {
			ZkClient zkc = poolZKSerializer.get(0);
			poolZKSerializer.remove(0);
			String osName = System.getProperties().getProperty("os.name");
			if (osName.contains("Linux")) {
				LOG.debug("Get poolZKSerializer,and available size [" + poolZKSerializer.size() + "]");
			} else {
				LOG.info("Get poolZKSerializer,and available size [" + poolZKSerializer.size() + "]");
			}
			return zkc;
		} else {
			addZkSerializerClient();
			ZkClient zkc = poolZKSerializer.get(0);
			poolZKSerializer.remove(0);
			String osName = System.getProperties().getProperty("os.name");
			if (osName.contains("Linux")) {
				LOG.debug("get poolZKSerializer,and available size [" + poolZKSerializer.size() + "]");
			} else {
				LOG.warn("get poolZKSerializer,and available size [" + poolZKSerializer.size() + "]");
			}
			return zkc;
		}
	}

	/** Initialization ZkClient pool size. */
	private void initZKPoolUtils() {
		LOG.info("Initialization ZkClient pool size [" + poolSize + "]");
		pool = new Vector<ZkClient>(poolSize);
		poolZKSerializer = new Vector<ZkClient>(poolSize);
		addZkClient();
		addZkSerializerClient();
	}

	/** Release ZkClient object. */
	public synchronized void release(ZkClient zkc) {
		if (pool.size() < 25) {
			pool.add(zkc);
		}
		String osName = System.getProperties().getProperty("os.name");
		if (osName.contains("Linux")) {
			LOG.debug("Release pool,and available size [" + pool.size() + "]");
		} else {
			LOG.info("Release pool,and available size [" + pool.size() + "]");
		}
	}

	/** Release ZkClient Serializer object. */
	public synchronized void releaseZKSerializer(ZkClient zkc) {
		if (poolZKSerializer.size() < 25) {
			poolZKSerializer.add(zkc);
		}
		String osName = System.getProperties().getProperty("os.name");
		if (osName.contains("Linux")) {
			LOG.debug("Release poolZKSerializer,and available size [" + poolZKSerializer.size() + "]");
		} else {
			LOG.info("Release poolZKSerializer,and available size [" + poolZKSerializer.size() + "]");
		}
	}

	/** Construction method. */
	private ZKPoolUtils() {
		initZKPoolUtils();
	}

}
