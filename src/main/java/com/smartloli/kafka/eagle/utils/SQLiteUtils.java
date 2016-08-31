package com.smartloli.kafka.eagle.utils;

import java.sql.Connection;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Date Aug 18, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note SQLite connection clazz to operate db
 */
public class SQLiteUtils {

	private static Logger LOG = LoggerFactory.getLogger(SQLiteUtils.class);
	private static PoolProperties p = new PoolProperties();
	
	static {
		String osName = System.getProperties().getProperty("os.name");
		String jdbc = "";
		if (osName.contains("Mac") || osName.contains("Win")) {
			jdbc = "jdbc:sqlite:/Users/dengjie/hadoop/workspace/kafka-eagle/src/main/resources/ke.db";
		} else {
			jdbc = "jdbc:sqlite:" + System.getProperty("user.dir") + "/db/ke.db";
		}
		p.setUrl(jdbc);
		p.setDriverClassName("org.sqlite.JDBC");
		p.setJmxEnabled(true);
		p.setTestWhileIdle(false);
		p.setTestOnBorrow(true);
		p.setValidationQuery("SELECT 1");
		p.setInitSQL("CREATE TABLE IF NOT EXISTS offsets (groups string,topic string,created string,logsize long,offsets long,lag long)");
		p.setTestOnReturn(false);
		p.setValidationInterval(30000);
		p.setTimeBetweenEvictionRunsMillis(30000);
		p.setMaxActive(100);
		p.setInitialSize(10);
		p.setMaxWait(10000);
		p.setRemoveAbandonedTimeout(60);
		p.setMinEvictableIdleTimeMillis(30000);
		p.setMinIdle(10);
		p.setLogAbandoned(true);
		p.setRemoveAbandoned(true);
		p.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;" + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
	}

	public static Connection getInstance() {
		Connection conn = null;
		try {
			conn = initialize().getConnection();
		} catch (Exception e) {
			LOG.error("SQLite connect has error,msg is  " + e.getMessage());
		}
		return conn;
	}

	private static DataSource initialize() {
		DataSource datasource = new DataSource();
		datasource.setPoolProperties(p);
		return datasource;
	}

	public static void close(Connection conn) {
		try {
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		} catch (Exception ex) {
			LOG.error("Close SQLite has error,msg is " + ex.getMessage());
		}
	}
}
