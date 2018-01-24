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
package org.smartloli.kafka.eagle.plugin.sqlite;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.plugin.util.JConstants;

/**
 * Initializing database scripts.
 * 
 * @author smartloli.
 *
 *         Created by Aug 7, 2017
 */
public class SqliteRecordSchema {

	public static final Logger LOG = LoggerFactory.getLogger(SqliteRecordSchema.class);

	public static void main(String[] args) {
		schema();
	}

	/** Load database schema script. */
	public static void schema() {
		String url = SystemConfigUtils.getProperty("kafka.eagle.url");
		String username = SystemConfigUtils.getProperty("kafka.eagle.username");
		String password = SystemConfigUtils.getProperty("kafka.eagle.password");
		tables(url, username, password);
	}

	private static void tables(String url, String username, String password) {
		Connection connection = SqliteStoragePlugin.getInstance(url, username, username);
		ResultSet rs = null;
		Statement stmt = null;
		List<String> tbls = new ArrayList<>();
		try {
			rs = connection.createStatement().executeQuery(JConstants.SQLITE_TABLES);
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			while (rs.next()) {
				for (int i = 1; i <= columnCount; i++) {
					tbls.add(rs.getString(i));
				}
			}

			for (String tbl : JConstants.TBLS) {
				if (tbls.contains(tbl)) {
					LOG.info("The [" + tbl + "] table already exists. Do not need to create it.");
				} else {
					String key = "CREATE_TABLE_SQLITE_" + tbl.toUpperCase();
					stmt = connection.createStatement();
					if (JConstants.KEYS.containsKey(key)) {
						stmt.addBatch(JConstants.KEYS.get(key).toString());
					}
					if (JConstants.KEYS.containsKey(key + "_INSERT")) {
						stmt.addBatch(JConstants.KEYS.get(key + "_INSERT").toString());
					}
					int[] code = stmt.executeBatch();
					if (code.length > 0) {
						LOG.info("Create [" + tbl + "] has successed.");
					} else {
						LOG.error("Create [" + tbl + "] has failed.");
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		SqliteStoragePlugin.close(connection);
	}

}
