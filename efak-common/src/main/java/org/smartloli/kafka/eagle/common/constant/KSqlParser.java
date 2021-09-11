/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.common.constant;

import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParser;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicPartitionSchema;
import org.smartloli.kafka.eagle.common.util.ErrorUtils;
import org.smartloli.kafka.eagle.common.util.StrUtils;

/**
 * The client requests the t operation to parse the SQL and obtain the fields
 * and conditions.
 *
 * @author smartloli.
 * <p>
 * Created by May 19, 2019
 */
public class KSqlParser {

    private KSqlParser() {
    }

    /**
     * Parser sql mapper kafka tree.
     */
    public static TopicPartitionSchema parserTopic(String sql) {
        TopicPartitionSchema tps = new TopicPartitionSchema();
        try {
            SqlParser.Config config = SqlParser.configBuilder().setLex(Lex.JAVA).build();
            SqlParser sqlParser = SqlParser.create(sql, config);
            SqlNode sqlNode = sqlParser.parseStmt();
            parseNode(sqlNode, tps);
        } catch (Exception e) {
            ErrorUtils.print(KSqlParser.class).error("Parser kafka sql has error, msg is ", e);
        }
        return tps;
    }

    private static void parseNode(SqlNode sqlNode, TopicPartitionSchema tps) {
        SqlKind sqlKind = sqlNode.getKind();
        switch (sqlKind) {
            case SELECT:
                String topic = "";
                SqlNode sqlFrom = ((SqlSelect) sqlNode).getFrom();
                SqlNode sqlWhere = ((SqlSelect) sqlNode).getWhere();
                if (sqlFrom.getKind() == SqlKind.IDENTIFIER) {
                    topic = sqlFrom.toString();
                } else {
                    SqlBasicCall sqlBasicCall = (SqlBasicCall) sqlFrom;
                    if (sqlBasicCall.getKind() == SqlKind.AS && sqlBasicCall.operands.length > 0) {
                        topic = sqlBasicCall.operands[0].toString();
                    }
                }
                if (sqlWhere.getKind() == SqlKind.IN) {// one and
                    SqlBasicCall sqlBasicCall = (SqlBasicCall) sqlWhere;
                    if (sqlBasicCall.operands.length > 1) {
                        String[] partitions = sqlBasicCall.operands[1].toString().split(",");
                        tps.getTopicSchema().put(topic, StrUtils.stringsConvertIntegers(partitions));
                        tps.setTopic(topic);
                        tps.setPartitions(StrUtils.stringsConvertIntegers(partitions));
                    }
                } else if (sqlWhere.getKind() == SqlKind.AND) {// two and
                    SqlBasicCall sqlBasicCall = (SqlBasicCall) sqlWhere;
                    if (sqlBasicCall.operands.length > 0) {
                        SqlNode sqlNodeChild = sqlBasicCall.operands[0];
                        if (sqlNodeChild.getKind() == SqlKind.IN) {
                            SqlBasicCall sqlBasicCallChild = (SqlBasicCall) sqlNodeChild;
                            if (sqlBasicCallChild.operands.length > 1) {
                                String[] partitions = sqlBasicCallChild.operands[1].toString().split(",");
                                tps.getTopicSchema().put(topic, StrUtils.stringsConvertIntegers(partitions));
                                tps.setTopic(topic);
                                tps.setPartitions(StrUtils.stringsConvertIntegers(partitions));
                            }
                        } else if (sqlNodeChild.getKind() == SqlKind.AND) {
                            SqlNode sqlBasicCallChild = ((SqlBasicCall) sqlNodeChild).operands[0];
                            if (sqlBasicCallChild.getKind() == SqlKind.IN) {
                                SqlBasicCall sqlBasicCallGrandson = (SqlBasicCall) sqlBasicCallChild;
                                if (sqlBasicCallGrandson.operands.length > 1) {
                                    String[] partitions = sqlBasicCallGrandson.operands[1].toString().split(",");
                                    tps.getTopicSchema().put(topic, StrUtils.stringsConvertIntegers(partitions));
                                    tps.setTopic(topic);
                                    tps.setPartitions(StrUtils.stringsConvertIntegers(partitions));
                                }
                            }
                        }
                    }
                }
                break;
            case JOIN:
                SqlNode leftNode = ((SqlJoin) sqlNode).getLeft();
                SqlNode rightNode = ((SqlJoin) sqlNode).getRight();
                if (leftNode.getKind() == SqlKind.IDENTIFIER) {
                    // tps.add(leftNode.toString());
                } else {
                    parseNode(leftNode, tps);
                }
                if (rightNode.getKind() == SqlKind.IDENTIFIER) {
                    // tps.add(rightNode.toString());
                } else {
                    parseNode(rightNode, tps);
                }
                break;
            case UNION:
                SqlNode unionLeft = ((SqlBasicCall) sqlNode).getOperands()[0];
                SqlNode unionRight = ((SqlBasicCall) sqlNode).getOperands()[1];
                if (unionLeft.getKind() == SqlKind.IDENTIFIER) {
                    // tps.add(unionLeft.toString());
                } else {
                    parseNode(unionLeft, tps);
                }
                if (unionRight.getKind() == SqlKind.IDENTIFIER) {
                    // tps.add(unionRight.toString());
                } else {
                    parseNode(unionRight, tps);
                }
                break;
            case ORDER_BY:
                SqlOrderBy sqlOrderBy = (SqlOrderBy) sqlNode;
                if (!StrUtils.isNull(sqlOrderBy.fetch.toString())) {
                    long limit = 0L;
                    try {
                        limit = Long.parseLong(sqlOrderBy.fetch.toString());
                    } catch (Exception e) {
                        ErrorUtils.print(KSqlParser.class).error("Parser limit string to long has error, msg is ", e);
                    }
                    tps.setLimit(limit);
                }
                parseNode(sqlOrderBy.query, tps);
            default:
                break;

        }
    }
}
