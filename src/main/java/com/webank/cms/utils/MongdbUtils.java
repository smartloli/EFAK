package com.webank.cms.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoOptions;

/**
 * @Date Mar 3, 2015
 * 
 * @Author smartloli
 * 
 * @Email smartloli.org@gmail.com
 * 
 * @Note mongodb manager
 */
public class MongdbUtils {

	private static final Logger logger = LoggerFactory.getLogger(MongdbUtils.class);
	private static Mongo mongo = null;

	private MongdbUtils() {
	}

	static {
		initClient();
	}

	// get DB object
	public static DB getDB(String dbName) {
		return mongo.getDB(dbName);
	}

	// get DB object without param
	public static DB getDB() {
		String dbName = SystemConfigUtils.getProperty("cms.webank.mongodb.dbname");
		return mongo.getDB(dbName);
	}

	// init mongodb pool
	private static void initClient() {
		try {
			String[] hosts = SystemConfigUtils.getProperty("cms.webank.mongodb.host").split(",");
			for (int i = 0; i < hosts.length; i++) {
				try {
					String host = hosts[i].split(":")[0];
					int port = Integer.parseInt(hosts[i].split(":")[1]);
					mongo = new Mongo(host, port);
					if (mongo.getDatabaseNames().size() > 0) {
						logger.info(String.format("connection success,host=[%s],port=[%d]", host, port));
						break;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					logger.error(String.format("create connection has error,msg is %s", ex.getMessage()));
				}
			}

			// 设置连接池的信息
			MongoOptions opt = mongo.getMongoOptions();
			opt.connectionsPerHost = SystemConfigUtils.getIntProperty("cms.webank.mongodb.poolsize");// poolsize
			opt.threadsAllowedToBlockForConnectionMultiplier = SystemConfigUtils.getIntProperty("cms.webank.mongodb.blocksize");// blocksize
			opt.socketKeepAlive = true;
			opt.autoConnectRetry = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
