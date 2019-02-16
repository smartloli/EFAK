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
package org.smartloli.kafka.eagle.core.sql.schema;

import java.util.HashMap;
import java.util.Map;

import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.schema.Function;
import org.apache.calcite.schema.ScalarFunction;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.smartloli.kafka.eagle.core.sql.common.JSqlMapData;
import org.smartloli.kafka.eagle.core.sql.common.JSqlTable;
import org.smartloli.kafka.eagle.core.sql.function.JSONFunction;
import org.smartloli.kafka.eagle.core.sql.common.JSqlMapData.Database;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * The query sql is defined as a structured schema.
 * 
 * @author smartloli.
 * 
 *         Created by Mar 25, 2016
 */
public class JSqlSchema extends AbstractSchema {

	private String dbName;

	public JSqlSchema(String name) {
		this.dbName = name;
	}

	@Override
	public boolean isMutable() {
		return super.isMutable();
	}

	@Override
	public boolean contentsHaveChangedSince(long lastCheck, long now) {
		return super.contentsHaveChangedSince(lastCheck, now);
	}

	@Override
	public Expression getExpression(SchemaPlus parentSchema, String name) {
		return super.getExpression(parentSchema, name);
	}

	@Override
	protected Multimap<String, Function> getFunctionMultimap() {
		ImmutableMultimap<String, ScalarFunction> funcs = ScalarFunctionImpl.createAll(JSONFunction.class);
		Multimap<String, Function> functions = HashMultimap.create();
		for (String key : funcs.keySet()) {
			for (ScalarFunction func : funcs.get(key)) {
				functions.put(key, func);
			}
		}
		return functions;
	}

	@Override
	protected Map<String, Schema> getSubSchemaMap() {
		return super.getSubSchemaMap();
	}

	@Override
	protected Map<String, Table> getTableMap() {
		Map<String, Table> tables = new HashMap<String, Table>();
		Database database = JSqlMapData.MAP.get(this.dbName);
		if (database == null)
			return tables;
		for (JSqlMapData.Table table : database.tables) {
			tables.put(table.tableName, new JSqlTable(table));
		}

		return tables;
	}

}
