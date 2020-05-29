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
package org.smartloli.kafka.eagle.core.sql.tool;

import com.google.common.collect.Sets;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.smartloli.kafka.eagle.common.util.ThrowExceptionUtils;

import java.util.Collections;
import java.util.Set;

/**
 * The client requests the t operation to parse the SQL and obtain the fields
 * and conditions.
 *
 * @author smartloli.
 * <p>
 * Created by May 19, 2019
 */
public class OdpsSqlParser {

    private OdpsSqlParser() {
    }

    /**
     * Parser sql mapper kafka tree.
     */
    public static Set<String> parserTopic(String sql) {
        try {
            Set<String> tableNames = Sets.newHashSet();
            SqlParser.Config config = SqlParser.configBuilder().setLex(Lex.JAVA).build();
            SqlParser sqlParser = SqlParser.create(sql, config);
            SqlNode sqlNode = sqlParser.parseStmt();
            parseNode(sqlNode, tableNames);
            return tableNames;
        } catch (Exception e) {
            ThrowExceptionUtils.print(OdpsSqlParser.class).error("Parser kafka sql has error, msg is ", e);
            return Collections.emptySet();
        }
    }

    private static void parseNode(SqlNode sqlNode, Set<String> tableNames) {
        SqlKind sqlKind = sqlNode.getKind();
        switch (sqlKind) {
            case SELECT:
                SqlNode sqlFrom = ((SqlSelect) sqlNode).getFrom();
                if (sqlFrom.getKind() == SqlKind.IDENTIFIER) {
                    tableNames.add(sqlFrom.toString());
                } else {
                    parseNode(sqlFrom, tableNames);
                }
                break;
            case JOIN:
                SqlNode leftNode = ((SqlJoin) sqlNode).getLeft();
                SqlNode rightNode = ((SqlJoin) sqlNode).getRight();
                if (leftNode.getKind() == SqlKind.IDENTIFIER) {
                    tableNames.add(leftNode.toString());
                } else {
                    parseNode(leftNode, tableNames);
                }
                if (rightNode.getKind() == SqlKind.IDENTIFIER) {
                    tableNames.add(rightNode.toString());
                } else {
                    parseNode(rightNode, tableNames);
                }
                break;
            case AS:
                SqlNode identifierNode = ((SqlBasicCall) sqlNode).getOperands()[0];
                if (identifierNode.getKind() != SqlKind.IDENTIFIER) {
                    parseNode(identifierNode, tableNames);
                } else {
                    tableNames.add(identifierNode.toString());
                }
                break;
            case UNION:
                SqlNode unionLeft = ((SqlBasicCall) sqlNode).getOperands()[0];
                SqlNode unionRight = ((SqlBasicCall) sqlNode).getOperands()[1];
                if (unionLeft.getKind() == SqlKind.IDENTIFIER) {
                    tableNames.add(unionLeft.toString());
                } else {
                    parseNode(unionLeft, tableNames);
                }
                if (unionRight.getKind() == SqlKind.IDENTIFIER) {
                    tableNames.add(unionRight.toString());
                } else {
                    parseNode(unionRight, tableNames);
                }
                break;
            case ORDER_BY:
                SqlOrderBy sqlOrderBy = (SqlOrderBy) sqlNode;
                parseNode(sqlOrderBy.query, tableNames);
            default:
                break;

        }
    }
}
