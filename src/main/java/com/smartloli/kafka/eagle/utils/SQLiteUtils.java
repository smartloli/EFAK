package com.smartloli.kafka.eagle.utils;

import java.sql.Connection;
import java.sql.DriverManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Date Aug 18, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
 */
public class SQLiteUtils {

	private static Logger LOG = LoggerFactory.getLogger(SQLiteUtils.class);

	public Connection getSQLiteConn() {
		Connection conn = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String osName = System.getProperties().getProperty("os.name");
			if (osName.contains("Mac") || osName.contains("Win")) {
				conn = DriverManager.getConnection("jdbc:sqlite:/Users/dengjie/hadoop/workspace/kafka-eagle/src/main/resources/ke.db");
			} else {
				conn = DriverManager.getConnection("jdbc:sqlite:" + System.getProperty("user.dir") + "/db/ke.db");
			}
			conn.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS offsets (groups string,topic string,created string,logsize long,offsets long,lag long)");
		} catch (Exception e) {
			LOG.error("SQLite connect has error,msg is  " + e.getMessage());
		}
		return conn;
	}

	public void close(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (Exception ex) {
			LOG.error("Close SQLite has error,msg is " + ex.getMessage());
		}
	}
}
