package com.smartloli.kafka.eagle.utils;

import java.util.Vector;

import kafka.utils.ZKStringSerializer$;

import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Date Aug 14, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note ZkClient pool utils
 */
public class ZKPoolUtils {
	private Logger LOG = LoggerFactory.getLogger(ZKPoolUtils.class);
	private String zkInfo = SystemConfigUtils.getProperty("kafka.zk.list");;

	private Vector<ZkClient> pool;
	private Vector<ZkClient> poolZKSerializer;
	private int poolSize = SystemConfigUtils.getIntProperty("kafka.zk.limit.size");
	private static ZKPoolUtils instance = null;

	private ZKPoolUtils() {
		initZKPoolUtils();
	}

	private void initZKPoolUtils() {
		LOG.debug("Initialization ZkClient pool size [" + poolSize + "]");
		pool = new Vector<ZkClient>(poolSize);
		poolZKSerializer = new Vector<ZkClient>(poolSize);
		addZkClient();
		addZkSerializerClient();
	}

	/**
	 * Init ZkClient pool numbers
	 */
	private void addZkClient() {
		ZkClient zkc = null;
		for (int i = 0; i < poolSize; i++) {
			try {
				zkc = new ZkClient(zkInfo);
				pool.add(zkc);
			} catch (Exception ex) {
				LOG.error(ex.getMessage());
			}
		}
	}

	private void addZkSerializerClient() {
		ZkClient zkSerializer = null;
		for (int i = 0; i < poolSize; i++) {
			try {
				zkSerializer = new ZkClient(zkInfo, Integer.MAX_VALUE, 100000, ZKStringSerializer$.MODULE$);
				poolZKSerializer.add(zkSerializer);
			} catch (Exception ex) {
				LOG.error(ex.getMessage());
			}
		}
	}

	/**
	 * Release ZkClient Serializer object
	 * 
	 * @param zkc
	 */
	public synchronized void releaseZKSerializer(ZkClient zkc) {
		if (poolZKSerializer.size() < 25) {
			poolZKSerializer.add(zkc);
		}
		LOG.debug("release poolZKSerializer size [" + poolZKSerializer.size() + "]");
	}

	/**
	 * Release ZkClient object
	 * 
	 * @param zkc
	 */
	public synchronized void release(ZkClient zkc) {
		if (pool.size() < 25) {
			pool.add(zkc);
		}
		LOG.debug("release pool size [" + pool.size() + "]");
	}

	/**
	 * Close ZkClient pool
	 */
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

	/**
	 * Reback pool one of ZkClient object
	 * 
	 * @return
	 */
	public synchronized ZkClient getZkClient() {
		if (pool.size() > 0) {
			ZkClient zkc = pool.get(0);
			pool.remove(0);
			LOG.debug("get pool size [" + pool.size() + "]");
			return zkc;
		} else {
			addZkClient();
			ZkClient zkc = pool.get(0);
			pool.remove(0);
			LOG.debug("get pool size [" + pool.size() + "]");
			return zkc;
		}
	}

	public synchronized ZkClient getZkClientSerializer() {
		if (poolZKSerializer.size() > 0) {
			ZkClient zkc = poolZKSerializer.get(0);
			poolZKSerializer.remove(0);
			LOG.debug("get poolZKSerializer size [" + poolZKSerializer.size() + "]");
			return zkc;
		} else {
			addZkSerializerClient();
			ZkClient zkc = poolZKSerializer.get(0);
			poolZKSerializer.remove(0);
			LOG.debug("get poolZKSerializer size [" + poolZKSerializer.size() + "]");
			return zkc;
		}
	}

	/**
	 * Single model get ZkClient object
	 * 
	 * @return
	 */
	public static ZKPoolUtils getInstance() {
		if (instance == null) {
			instance = new ZKPoolUtils();
		}
		return instance;
	}

}
