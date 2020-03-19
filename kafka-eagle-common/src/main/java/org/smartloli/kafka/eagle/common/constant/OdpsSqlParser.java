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
package org.smartloli.kafka.eagle.common.constant;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.smartloli.kafka.eagle.common.util.ThrowExceptionUtils;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.odps.visitor.OdpsSchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.stat.TableStat.Name;
import com.alibaba.druid.util.JdbcConstants;

/**
 * The client requests the t operation to parse the SQL and obtain the fields
 * and conditions.
 * 
 * @author smartloli.
 *
 *         Created by May 19, 2019
 */
public class OdpsSqlParser {

	private OdpsSqlParser() {
	}

	/** Parser sql mapper kafka tree. */
	public static String parserTopic(String sql) {
		try {
			String dbType = JdbcConstants.MYSQL;
			List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);
			SQLStatement stmt = stmtList.get(0);
			OdpsSchemaStatVisitor visitor = new OdpsSchemaStatVisitor();
			stmt.accept(visitor);
			Map<Name, TableStat> tabmap = visitor.getTables();
			String tableName = "";
			Iterator<Name> iterator = tabmap.keySet().iterator();
			while (iterator.hasNext()) {
				Name name = iterator.next();
				tableName = name.toString();
			}
			return tableName.trim();
		} catch (Exception e) {
			ThrowExceptionUtils.print(OdpsSqlParser.class).error("Parser kafka sql has error, msg is ", e);
			return "";
		}
	}
}
