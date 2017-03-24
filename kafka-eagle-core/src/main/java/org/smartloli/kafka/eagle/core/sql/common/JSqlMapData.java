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
package org.smartloli.kafka.eagle.core.sql.common;

import java.sql.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.calcite.sql.type.SqlTypeName;

import com.alibaba.fastjson.JSONObject;

/**
 * Load data set to memory.
 * 
 * @author smartloli.
 * 
 *         Created by Mar 25, 2016
 */
public class JSqlMapData {

	public static final Map<String, Database> MAP = new HashMap<String, Database>();
	public static Map<String, SqlTypeName> SQLTYPE_MAPPING = new HashMap<String, SqlTypeName>();
	@SuppressWarnings("rawtypes")
	public static Map<String, Class> JAVATYPE_MAPPING = new HashMap<String, Class>();

	static {
		loadDatabaseType();
	}

	private static void loadDatabaseType() {
		SQLTYPE_MAPPING.put("char", SqlTypeName.CHAR);
		JAVATYPE_MAPPING.put("char", Character.class);
		SQLTYPE_MAPPING.put("varchar", SqlTypeName.VARCHAR);
		JAVATYPE_MAPPING.put("varchar", String.class);
		SQLTYPE_MAPPING.put("boolean", SqlTypeName.BOOLEAN);
		SQLTYPE_MAPPING.put("integer", SqlTypeName.INTEGER);
		JAVATYPE_MAPPING.put("integer", Integer.class);
		SQLTYPE_MAPPING.put("tinyint", SqlTypeName.TINYINT);
		SQLTYPE_MAPPING.put("smallint", SqlTypeName.SMALLINT);
		SQLTYPE_MAPPING.put("bigint", SqlTypeName.BIGINT);
		JAVATYPE_MAPPING.put("bigint", Long.class);
		SQLTYPE_MAPPING.put("decimal", SqlTypeName.DECIMAL);
		SQLTYPE_MAPPING.put("numeric", SqlTypeName.DECIMAL);
		SQLTYPE_MAPPING.put("float", SqlTypeName.FLOAT);
		SQLTYPE_MAPPING.put("real", SqlTypeName.REAL);
		SQLTYPE_MAPPING.put("double", SqlTypeName.DOUBLE);
		SQLTYPE_MAPPING.put("date", SqlTypeName.DATE);
		JAVATYPE_MAPPING.put("date", Date.class);
		SQLTYPE_MAPPING.put("time", SqlTypeName.TIME);
		SQLTYPE_MAPPING.put("timestamp", SqlTypeName.TIMESTAMP);
		SQLTYPE_MAPPING.put("any", SqlTypeName.ANY);
	}

	public static void loadSchema(JSONObject cols, String tableName, List<List<String>> datas) {
		Database db = new Database();
		Table table = new Table();
		table.tableName = tableName;
		for (String key : cols.keySet()) {
			Column _col = new Column();
			_col.name = key;
			_col.type = cols.getString(key);
			table.columns.add(_col);
		}
		table.data = datas;
		db.tables.add(table);
		MAP.put("db", db);
	}

	public static class Database {
		public List<Table> tables = new LinkedList<Table>();
	}

	public static class Table {
		public String tableName;
		public List<Column> columns = new LinkedList<Column>();
		public List<List<String>> data = new LinkedList<List<String>>();
	}

	public static class Column {
		public String name;
		public String type;
	}
}
