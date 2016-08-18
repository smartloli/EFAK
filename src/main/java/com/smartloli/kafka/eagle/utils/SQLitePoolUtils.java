package com.smartloli.kafka.eagle.utils;

import java.beans.PropertyVetoException;
import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @Date Aug 18, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
 */
public class SQLitePoolUtils {

	private static Logger LOG = LoggerFactory.getLogger(SQLitePoolUtils.class);

	private static ComboPooledDataSource cpds = null;

	public static Connection getSQLiteConn() {
		Connection conn = null;
		try {
			conn = getInstance().getConnection();
			conn.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS offsets (topic string,created string,logsize long,offsets long,lag long,producer long,consumer long,PRIMARY KEY(topic))");
		} catch (Exception e) {
			LOG.error("SQLite connect has error,msg is  " + e.getMessage());
		}
		return conn;
	}

	private static ComboPooledDataSource getInstance() {
		if (cpds == null) {
			cpds = new ComboPooledDataSource();
			try {
				cpds.setDriverClass("org.sqlite.JDBC");
			} catch (PropertyVetoException e) {
				LOG.error(e.getMessage());
			}
			String osName = System.getProperties().getProperty("os.name");
			if (osName.contains("Mac") || osName.contains("Win")) {
				cpds.setJdbcUrl("jdbc:sqlite:" + System.getProperty("user.dir") + "/src/main/resources/ke.db");
			} else {
				cpds.setJdbcUrl("jdbc:sqlite:" + System.getProperty("user.dir") + "/conf/ke.db");
			}
			cpds.setMinPoolSize(1);
			cpds.setMaxPoolSize(10);
			cpds.setInitialPoolSize(5);
		}
		return cpds;
	}

	public static void release() {
		try {
			if (cpds != null) {
				cpds.close();
			}
		} catch (Exception e) {
			LOG.error("Release c3p0 has error = " + e.getMessage());
		}
	}

	public static void closeSQLite(Connection sqlite) {
		try {
			if (sqlite != null) {
				sqlite.close();
			}
			if (cpds != null) {
				cpds.close();
			}
		} catch (Exception e) {
			LOG.error("SQLite close has error = " + e.getMessage());
		}
	}

}
