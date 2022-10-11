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
import org.apache.calcite.sql.util.SqlBasicVisitor;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicPartitionSchema;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.LoggerUtils;
import org.smartloli.kafka.eagle.common.util.StrUtils;

/**
 * The client requests the t operation to parse the SQL and obtain the fields
 * and conditions.
 *
 * @author smartloli.
 * <p>
 * Created by Jul 17, 2022
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
            SqlParser.Config config = SqlParser.config().withLex(Lex.JAVA);
            SqlParser sqlParser = SqlParser.create(sql, config);
            SqlNode sqlNode = sqlParser.parseStmt();
            parseNode(sqlNode, tps);
        } catch (Exception e) {
            LoggerUtils.print(KSqlParser.class).error("Parser kafka sql has error, msg is ", e);
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
                    if (sqlBasicCall.getKind() == SqlKind.AS && sqlBasicCall.operandCount() > 0) {
                        topic = sqlBasicCall.operand(0).toString();
                    }
                }
                // no where
                if (sqlWhere == null) {
                    String[] partitions = {KConstants.Kafka.ALL_PARTITION + ""};
                    tps.getTopicSchema().put(topic, StrUtils.stringsConvertIntegers(partitions));
                    tps.setTopic(topic);
                    tps.setPartitions(StrUtils.stringsConvertIntegers(partitions));
                    return;
                }
                // where info
                String finalTopic = topic;
                sqlWhere.accept(new SqlBasicVisitor<String>() {
                    @Override
                    public String visit(SqlCall call) {
                        if (call.getKind().equals(SqlKind.AND) || call.getKind().equals(SqlKind.OR)) {
                            SqlBasicCall sql = (SqlBasicCall) call;
                            SqlBasicCall left = (SqlBasicCall) sql.getOperandList().get(0);
                            if (!left.getKind().equals(SqlKind.AND) && !left.getKind().equals(SqlKind.OR)) {
                                String[] partitions = left.operand(1).toString().split(",");
                                tps.getTopicSchema().put(finalTopic, StrUtils.stringsConvertIntegers(partitions));
                                tps.setTopic(finalTopic);
                                tps.setPartitions(StrUtils.stringsConvertIntegers(partitions));
                            }
                        }
                        return call.getOperator().acceptCall(this, call);
                    }
                });
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
                SqlNode unionLeft = ((SqlBasicCall) sqlNode).operand(0);
                SqlNode unionRight = ((SqlBasicCall) sqlNode).operand(1);
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
                        LoggerUtils.print(KSqlParser.class).error("Parser limit string to long has error, msg is ", e);
                    }
                    tps.setLimit(limit);
                }
                parseNode(sqlOrderBy.query, tps);
            default:
                break;

        }
    }
}
