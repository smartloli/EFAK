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
	private Vector<ZkClient> zkCliPool;
	/** Set pool max size. */
	private int zkCliPoolSize = SystemConfigUtils.getIntProperty("kafka.zk.limit.size");
	/** Serializer Zookeeper client pool. */
	private Vector<ZkClient> zkCliPoolSerializer;
	/** Get Zookeeper client address. */
	private String zkCliAddress = SystemConfigUtils.getProperty("kafka.zk.list");

	/** Init ZkClient pool numbers. */
	private void addZkClient() {
		ZkClient zkc = null;
		for (int i = 0; i < zkCliPoolSize; i++) {
			try {
				zkc = new ZkClient(zkCliAddress);
				zkCliPool.add(zkc);
			} catch (Exception ex) {
				LOG.error(ex.getMessage());
			}
		}
	}

	/** Add serializer zkclient. */
	private void addZkSerializerClient() {
		ZkClient zkSerializer = null;
		for (int i = 0; i < zkCliPoolSize; i++) {
			try {
				zkSerializer = new ZkClient(zkCliAddress, Integer.MAX_VALUE, 100000, ZKStringSerializer$.MODULE$);
				zkCliPoolSerializer.add(zkSerializer);
			} catch (Exception ex) {
				LOG.error(ex.getMessage());
			}
		}
	}

	/** Close ZkClient pool. */
	public synchronized void closePool() {
		if (zkCliPool != null && zkCliPool.size() > 0) {
			for (int i = 0; i < zkCliPool.size(); i++) {
				try {
					zkCliPool.get(i).close();
				} catch (Exception ex) {
					LOG.error(ex.getMessage());
				} finally {
					zkCliPool.remove(i);
				}
			}
		}

		if (zkCliPoolSerializer != null && zkCliPoolSerializer.size() > 0) {
			for (int i = 0; i < zkCliPoolSerializer.size(); i++) {
				try {
					zkCliPoolSerializer.get(i).close();
				} catch (Exception ex) {
					LOG.error(ex.getMessage());
				} finally {
					zkCliPoolSerializer.remove(i);
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
				addZkClient();
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
	public synchronized ZkClient getZkClientSerializer() {
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
			addZkSerializerClient();
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

	/** Initialization ZkClient pool size. */
	private void initZKPoolUtils() {
		LOG.info("Initialization ZkClient pool size [" + zkCliPoolSize + "]");
		zkCliPool = new Vector<ZkClient>(zkCliPoolSize);
		zkCliPoolSerializer = new Vector<ZkClient>(zkCliPoolSize);
		addZkClient();
		addZkSerializerClient();
	}

	/** Release ZkClient object. */
	public synchronized void release(ZkClient zkc) {
		if (zkCliPool.size() < 25) {
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
	public synchronized void releaseZKSerializer(ZkClient zkc) {
		if (zkCliPoolSerializer.size() < 25) {
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
		initZKPoolUtils();
	}

}
