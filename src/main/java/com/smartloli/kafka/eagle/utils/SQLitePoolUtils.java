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
			conn.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS offsets (groups string,topic string,created string,logsize long,offsets long,lag long)");
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
				cpds.setJdbcUrl("jdbc:sqlite:/Users/dengjie/hadoop/workspace/kafka-eagle/src/main/resources/ke.db");
			} else {
				cpds.setJdbcUrl("jdbc:sqlite:" + System.getProperty("user.dir") + "/db/ke.db");
			}
			cpds.setMinPoolSize(1);
			cpds.setMaxPoolSize(10);
			cpds.setInitialPoolSize(5);
		}
		return cpds;
	}

	public static void release(Connection connect) {
		try {
			if (connect != null) {
				connect.close();
			}
		} catch (Exception ex) {
			LOG.error("Release connect has error,msg is " + ex.getMessage());
		}
	}

}
