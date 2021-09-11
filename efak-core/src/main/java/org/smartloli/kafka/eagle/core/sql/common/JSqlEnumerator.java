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

import java.math.BigDecimal;
import java.util.List;

import org.apache.calcite.linq4j.Enumerator;
import org.smartloli.kafka.eagle.core.sql.tool.JSqlDateUtils;

/**
 * Convert query sql data format to standard format.
 *
 * @author smartloli.
 * 
 *         Created by Mar 25, 2016
 */
public class JSqlEnumerator<E> implements Enumerator<E> {
	private int currentIndex = -1;
	private List<String> columnTypes;
	private List<List<String>> data = null;
	private RowConverter<E> rowConvert;
	@SuppressWarnings("unchecked")
	public JSqlEnumerator(int[] fields, List<String> types, List<List<String>> data) {
		this.data = data;
		this.columnTypes = types;
		rowConvert = (RowConverter<E>) new ArrayRowConverter(fields);
	}

	abstract static class RowConverter<E> {
		abstract E convertRow(List<String> rows, List<String> columnTypes);
	}

	static class ArrayRowConverter extends RowConverter<Object[]> {
		private int[] fields;

		public ArrayRowConverter(int[] fields) {
			this.fields = fields;
		}

		@Override
		Object[] convertRow(List<String> rows, List<String> columnTypes) {
			Object[] objects = new Object[fields.length];
			int i = 0;
			for (int field : this.fields) {
				objects[i++] = convertOptiqCellValue(rows.get(field), columnTypes.get(field));
			}
			return objects;
		}
	}

	public void close() {

	}

	public static Object convertOptiqCellValue(String strValue, String dataType) {
		if (strValue == null)
			return null;
	
		if ((strValue.equals("") || strValue.equals("\\N")) && !dataType.equals("string"))
			return null;
	
		// use data type enum instead of string comparison
		if ("date".equals(dataType)) {
			return JSqlDateUtils.stringToDate(strValue);
		} else if ("tinyint".equals(dataType)) {
			return Byte.valueOf(strValue);
		} else if ("short".equals(dataType) || "smallint".equals(dataType)) {
			return Short.valueOf(strValue);
		} else if ("integer".equals(dataType)) {
			return Integer.valueOf(strValue);
		} else if ("long".equals(dataType) || "bigint".equals(dataType)) {
			return Long.valueOf(strValue);
		} else if ("double".equals(dataType)) {
			return Double.valueOf(strValue);
		} else if ("decimal".equals(dataType)) {
			return new BigDecimal(strValue);
		} else if ("timestamp".equals(dataType)) {
			return Long.valueOf(JSqlDateUtils.stringToMillis(strValue));
		} else if ("float".equals(dataType)) {
			return Float.valueOf(strValue);
		} else if ("boolean".equals(dataType)) {
			return Boolean.valueOf(strValue);
		} else {
			return strValue;
		}
	}

	public E current() {
		List<String> line = data.get(currentIndex);
		return rowConvert.convertRow(line, this.columnTypes);
	}

	public boolean moveNext() {
		return ++currentIndex < data.size();
	}

	public void reset() {
	}
}
