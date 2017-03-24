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

import java.util.ArrayList;
import java.util.List;

import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.type.SqlTypeUtil;

/**
 * Define memory table.
 * 
 * @author smartloli.
 * 
 *         Created by Mar 25, 2016
 */
public class JSqlTable extends AbstractTable implements ScannableTable {
	private JSqlMapData.Table sourceTable;
	private RelDataType dataType;

	public JSqlTable(JSqlMapData.Table table) {
		this.sourceTable = table;
		dataType = null;
	}

	private static int[] identityList(int n) {
		int[] integers = new int[n];
		for (int i = 0; i < n; i++) {
			integers[i] = i;
		}
		return integers;
	}

	public RelDataType getRowType(RelDataTypeFactory typeFactory) {
		if (dataType == null) {
			RelDataTypeFactory.FieldInfoBuilder fieldInfo = typeFactory.builder();
			for (JSqlMapData.Column column : this.sourceTable.columns) {
				RelDataType sqlType = typeFactory.createJavaType(JSqlMapData.JAVATYPE_MAPPING.get(column.type));
				sqlType = SqlTypeUtil.addCharsetAndCollation(sqlType, typeFactory);
				fieldInfo.add(column.name, sqlType);
			}
			this.dataType = typeFactory.createStructType(fieldInfo);
		}
		return this.dataType;
	}

	public Enumerable<Object[]> scan(DataContext root) {
		final List<String> types = new ArrayList<String>(sourceTable.columns.size());
		for (JSqlMapData.Column column : sourceTable.columns) {
			types.add(column.type);
		}
		final int[] fields = identityList(this.dataType.getFieldCount());
		return new AbstractEnumerable<Object[]>() {
			public Enumerator<Object[]> enumerator() {
				return new JSqlEnumerator<Object[]>(fields, types, sourceTable.data);
			}
		};
	}

}
