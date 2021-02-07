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
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.DestroyMode;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.kafka.common.security.JaasUtils;
import org.apache.kafka.common.utils.Time;
import org.smartloli.kafka.eagle.common.util.KConstants.OperateSystem;

import java.util.HashMap;
import java.util.Map;

/**
 * KafkaZkClient pool utils.
 *
 * @author smartloli.
 *
 *         Created by Jan 11, 2019.
 */
public final class KafkaZKPoolUtils {

	private static KafkaZKPoolUtils instance = null;
	/** Set pool max size. */
	private static int zkCliPoolSize = SystemConfigUtils.getIntProperty("kafka.zk.limit.size");

	/** Zookeeper client connection pool. */
	private static GenericKeyedObjectPool<String,KafkaZkClient> connectionPool;
	private static Map<String, String> clusterAliass = new HashMap<>();

	public static final int DEFAULT_RETRY_TIMES = 25;
	public static final int DEFAULT_POOL_SIZE = 15;
	public static final int ZK_CONNECTION_TIMEOUT_MS = 30_000;
	public static final int ZK_SESSION_TIMEOUT_MS = 30_000;
	private static final String METRIC_GROUP_NAME = "topic-management-service";

	/** Init ZkClient pool numbers. */
	static {
		try {
			init();
		} catch (Exception e) {
			ErrorUtils.print(KafkaZKPoolUtils.class).error("Init zookeeper-client pool failed,error msg is ", e);
		}
	}

	private static void init() throws Exception {
		for (String clusterAlias : SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",")) {
			clusterAliass.put(clusterAlias, SystemConfigUtils.getProperty(clusterAlias + ".zk.list"));
		}
		if(zkCliPoolSize<1){
			zkCliPoolSize = DEFAULT_POOL_SIZE;
		}
		if(connectionPool!=null){
			return;
		}
		KeyedPooledObjectFactory<String,KafkaZkClient> keyedPooledObjectFactory = new BaseKeyedPooledObjectFactory<String,KafkaZkClient>() {
			@Override
			public void destroyObject(String key, PooledObject<KafkaZkClient> p, DestroyMode mode) throws Exception {
				if(p!=null){
					KafkaZkClient zkClient = p.getObject();
					if(zkClient!=null){
						zkClient.close();
					}
				}
			}
			@Override
			public KafkaZkClient create(String clusterAlias) throws Exception {
				KafkaZkClient zkc = KafkaZkClient.apply(clusterAliass.get(clusterAlias), JaasUtils.isZkSecurityEnabled(), ZK_SESSION_TIMEOUT_MS, ZK_CONNECTION_TIMEOUT_MS, Integer.MAX_VALUE, Time.SYSTEM, METRIC_GROUP_NAME, "SessionExpireListener");
				if (zkc != null) {
					if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".zk.acl.enable")) {
						String schema = SystemConfigUtils.getProperty(clusterAlias + ".zk.acl.schema");
						String username = SystemConfigUtils.getProperty(clusterAlias + ".zk.acl.username");
						String password = SystemConfigUtils.getProperty(clusterAlias + ".zk.acl.password");
						try {
							zkc.currentZooKeeper().addAuthInfo(schema, (username + ":" + password).getBytes());
						} catch (Exception e) {
							ErrorUtils.print(KafkaZKPoolUtils.class).error("ClusterAlias[" + clusterAlias + "] add acl has error, msg is ", e);
						}
					}
				}
				return zkc;
			}

			@Override
			public PooledObject<KafkaZkClient> wrap(KafkaZkClient kafkaZkClient) {
				return new DefaultPooledObject<>(kafkaZkClient);
			}

			@Override
			public boolean validateObject(String key, PooledObject<KafkaZkClient> p) {
				return zkClientIsActive(p.getObject());
			}
		};
		GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();
		config.setMaxTotalPerKey(zkCliPoolSize);
		//hard-code,We first solve the problem
		config.setMaxWaitMillis(ZK_CONNECTION_TIMEOUT_MS);
		config.setTestOnBorrow(true);
		config.setMaxIdlePerKey(zkCliPoolSize>1?zkCliPoolSize-1:1);
		GenericKeyedObjectPool<String,KafkaZkClient> genericKeyedObjectPool = new GenericKeyedObjectPool(keyedPooledObjectFactory,config);
		for (String clusterAlias : clusterAliass.keySet()) {
			genericKeyedObjectPool.addObject(clusterAlias);
		}
		connectionPool = genericKeyedObjectPool;
	}

	/** Single model get ZkClient object. */
	public static synchronized KafkaZKPoolUtils getInstance() {
		if (instance == null) {
			instance = new KafkaZKPoolUtils();
		}
		return instance;
	}

	private static boolean zkClientIsActive(KafkaZkClient zkc){
		if(zkc==null){
			return false;
		}
		for(int i=0;i<DEFAULT_RETRY_TIMES;i++){
			try{
				zkc.getAllBrokersInCluster();
				return true;
			}catch (Exception e){
				ErrorUtils.print(KafkaZKPoolUtils.class).error("zookeeper client not alive!Retry times:"+(i+1), e);
			}
		}
		return false;
	}

	/** Reback pool one of ZkClient object. */
	public synchronized KafkaZkClient getZkClient(String clusterAlias) {
		KafkaZkClient zkc = null;
		try {
			zkc = connectionPool.borrowObject(clusterAlias);
			ErrorUtils.print(KafkaZKPoolUtils.class).debug("Get Connection,Active connection["+connectionPool.getNumActive(clusterAlias)+"],"+"Idle connection["+connectionPool.getNumIdle(clusterAlias)+"]");
		} catch (Exception e) {
			ErrorUtils.print(KafkaZKPoolUtils.class).error("Error initializing zookeeper, msg is ", e);
			ErrorUtils.print(KafkaZKPoolUtils.class).error("Kafka cluster[" + clusterAlias + ".zk.list] address has null.");
		}
		if(zkc==null){
			ErrorUtils.print(KafkaZKPoolUtils.class).error("Can not get zookeeper connection!");
		}
		return zkc;
	}

	/** Release ZkClient object. */
	public synchronized void release(String clusterAlias, KafkaZkClient zkc) {
		if(zkc==null){
			return;
		}
		connectionPool.returnObject(clusterAlias,zkc);
		String osName = System.getProperties().getProperty(OperateSystem.OS_NAME.getValue());
		if (osName.contains(OperateSystem.LINUX.getValue())) {
			ErrorUtils.print(KafkaZKPoolUtils.class).debug("Release Connection,Active connection["+connectionPool.getNumActive(clusterAlias)+"],"+"Idle connection["+connectionPool.getNumIdle(clusterAlias)+"]");
		} else {
			ErrorUtils.print(KafkaZKPoolUtils.class).debug("Release Connection,Active connection["+connectionPool.getNumActive(clusterAlias)+"],"+"Idle connection["+connectionPool.getNumIdle(clusterAlias)+"]");
		}
	}

	/** Construction method. */
	private KafkaZKPoolUtils() {
	}

}
