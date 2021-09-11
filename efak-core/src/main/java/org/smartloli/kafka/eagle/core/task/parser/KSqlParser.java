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
package org.smartloli.kafka.eagle.core.task.parser;

import com.alibaba.fastjson.JSONObject;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParser;
import org.smartloli.kafka.eagle.common.util.ErrorUtils;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.core.sql.schema.TopicSchema;
import org.smartloli.kafka.eagle.core.task.strategy.FieldSchemaStrategy;
import org.smartloli.kafka.eagle.core.task.strategy.KSqlStrategy;

import java.util.Arrays;

/**
 * The filter conditions of kafka sql are analyzed and distributed execution
 * strategy is generated.
 *
 * @author smartloli.
 * <p>
 * Created by Sep 13, 2020
 */
public class KSqlParser {

    private KSqlParser() {
    }

    /**
     * Parse SQL filter conditions, partition, topic name and other fields.
     */
    public static KSqlStrategy parseQueryKSql(String sql, String cluster) {
        KSqlStrategy ksql = new KSqlStrategy();
        ksql.setCluster(cluster);
        try {
            SqlParser.Config config = SqlParser.configBuilder().setLex(Lex.JAVA).build();
            SqlParser sqlParser = SqlParser.create(sql, config);
            SqlNode sqlNode = sqlParser.parseStmt();
            parseStmt(sqlNode, ksql);
        } catch (Exception e) {
            ErrorUtils.print(KSqlParser.class).error("Parser kafka sql has error, msg is ", e);
        }
        return ksql;
    }

    private static JSONObject isIncludeType(String condition) {
        JSONObject result = new JSONObject();
        for (String compare : KConstants.KSQL.COMPARE_CONDITIONS) {
            if (condition.contains(compare)) {
                result.put("status", true);
                result.put("type", compare);
                return result;
            }
        }
        result.put("status", false);
        return result;
    }

    private static void parseStmt(SqlNode sqlNode, KSqlStrategy ksql) {
        SqlKind sqlKind = sqlNode.getKind();
        switch (sqlKind) {
            case SELECT:
                String topic = "";
                SqlNode sqlColumns = ((SqlSelect) sqlNode).getSelectList();
                SqlNode sqlFrom = ((SqlSelect) sqlNode).getFrom();
                SqlNode sqlWhere = ((SqlSelect) sqlNode).getWhere();
                // parser columns
                if (sqlColumns.toString().contains("*")) {
                    ksql.setColumns(Arrays.asList(TopicSchema.PARTITION, TopicSchema.OFFSET, TopicSchema.MSG, TopicSchema.TIMESPAN, TopicSchema.DATE));
                } else {
                    String[] columns = sqlColumns.toString().split(",");
                    for (String column : columns) {
                        if (column.contains("AS")) {
                            ksql.getColumns().add(column.split("AS")[1].trim().replaceAll("`", ""));
                        } else {
                            ksql.getColumns().add(column.trim().replaceAll("`", ""));
                        }
                    }
                }

                if (sqlFrom.getKind() == SqlKind.IDENTIFIER) {
                    topic = sqlFrom.toString();
                } else {
                    SqlBasicCall sqlBasicCall = (SqlBasicCall) sqlFrom;
                    if (sqlBasicCall.getKind() == SqlKind.AS && sqlBasicCall.operands.length > 0) {
                        topic = sqlBasicCall.operands[0].toString();
                    }
                }
                ksql.setTopic(topic);
                SqlBasicCall sqlBasicCallWhere = (SqlBasicCall) sqlWhere;
                String whereSql = sqlBasicCallWhere.toString();
                String[] conditions = whereSql.split(KConstants.KSQL.AND);
                for (String condition : conditions) {
                    JSONObject compareObject = isIncludeType(condition);
                    if (condition.contains(KConstants.KSQL.IN)) {// partition
                        String[] partitionStrs = condition.split(KConstants.KSQL.IN)[1].split(KConstants.KSQL.REG_LB)[1].split(KConstants.KSQL.REG_RB)[0].split(",");
                        ksql.getPartitions().addAll(StrUtils.stringsConvertIntegers(partitionStrs));
                    } else if (compareObject.getBoolean("status") && !condition.contains(KConstants.KSQL.LB) && !condition.contains(KConstants.KSQL.JSON)) {
                        String key = condition.split(compareObject.getString("type"))[0];
                        String value = condition.split(compareObject.getString("type"))[1];
                        FieldSchemaStrategy field = new FieldSchemaStrategy();
                        field.setKey(key.trim());
                        field.setType(compareObject.getString("type"));
                        field.setValue(value.trim().replaceAll("'", ""));
                        ksql.getFieldSchema().add(field);
                    } else if (condition.contains(KConstants.KSQL.LIKE) && !condition.contains(KConstants.KSQL.JSON)) {
                        if (condition.contains(KConstants.KSQL.OR)) {
                            String[] conditionChilds = condition.split(KConstants.KSQL.OR);
                            for (int i = 0; i < conditionChilds.length; i++) {
                                String key = "";
                                String value = "";
                                if (i == 0) {// start
                                    key = conditionChilds[i].split(KConstants.KSQL.REG_LB)[1].split(KConstants.KSQL.LIKE)[0];
                                    value = conditionChilds[i].split(KConstants.KSQL.REG_LB)[1].split(KConstants.KSQL.LIKE)[1];
                                } else if (i == (conditionChilds.length - 1)) {// end
                                    key = conditionChilds[i].split(KConstants.KSQL.REG_RB)[0].split(KConstants.KSQL.LIKE)[0];
                                    value = conditionChilds[i].split(KConstants.KSQL.REG_RB)[0].split(KConstants.KSQL.LIKE)[1];
                                } else {// middle
                                    key = conditionChilds[i].split(KConstants.KSQL.LIKE)[0];
                                    value = conditionChilds[i].split(KConstants.KSQL.LIKE)[1];
                                }
                                FieldSchemaStrategy field = new FieldSchemaStrategy();
                                field.setKey(key.trim());
                                field.setType(KConstants.KSQL.LIKE);
                                field.setValue(value.trim().replaceAll("%", "").replaceAll("'", ""));
                                ksql.getFieldSchema().add(field);
                            }
                        } else {
                            String key = condition.split(KConstants.KSQL.LIKE)[0];
                            String value = condition.split(KConstants.KSQL.LIKE)[1];
                            FieldSchemaStrategy field = new FieldSchemaStrategy();
                            field.setKey(key.trim());
                            field.setType(KConstants.KSQL.LIKE);
                            field.setValue(value.trim().replaceAll("'", "").replaceAll("%", ""));
                            ksql.getFieldSchema().add(field);
                        }
                    } else if (condition.contains(KConstants.KSQL.JSON)) {
                        if (condition.contains(KConstants.KSQL.LIKE)) {
                            String key = condition.split(KConstants.KSQL.LIKE)[0];
                            String value = condition.split(KConstants.KSQL.LIKE)[1];
                            FieldSchemaStrategy field = new FieldSchemaStrategy();
                            field.setKey(key.split("'")[1].split("'")[0].trim());
                            field.setType(KConstants.KSQL.LIKE);
                            field.setValue(value.trim().replaceAll("'", "").replaceAll("%", ""));
                            if (condition.contains(KConstants.KSQL.JSONS)) {
                                field.setJsonsUdf(true);
                            } else {
                                field.setJsonUdf(true);
                            }
                            ksql.getFieldSchema().add(field);
                        } else if (compareObject.getBoolean("status")) {
                            String key = condition.split(compareObject.getString("type"))[0];
                            String value = condition.split(compareObject.getString("type"))[1];
                            FieldSchemaStrategy field = new FieldSchemaStrategy();
                            field.setKey(key.split("'")[1].split("'")[0].trim());
                            field.setType(compareObject.getString("type"));
                            field.setValue(value.trim().replaceAll("'", ""));
                            if (condition.contains(KConstants.KSQL.JSONS)) {
                                field.setJsonsUdf(true);
                            } else {
                                field.setJsonUdf(true);
                            }
                            ksql.getFieldSchema().add(field);
                        }
                    }
                }
                break;
            case JOIN:
                SqlNode leftNode = ((SqlJoin) sqlNode).getLeft();
                SqlNode rightNode = ((SqlJoin) sqlNode).getRight();
                if (leftNode.getKind() == SqlKind.IDENTIFIER) {
                    // reserved interface
                } else {
                    // reserved interface
                }
                if (rightNode.getKind() == SqlKind.IDENTIFIER) {
                    // reserved interface
                } else {
                    // reserved interface
                }
                break;
            case UNION:
                SqlNode unionLeft = ((SqlBasicCall) sqlNode).getOperands()[0];
                SqlNode unionRight = ((SqlBasicCall) sqlNode).getOperands()[1];
                if (unionLeft.getKind() == SqlKind.IDENTIFIER) {
                    // reserved interface
                } else {
                    // reserved interface
                }
                if (unionRight.getKind() == SqlKind.IDENTIFIER) {
                    // reserved interface
                } else {
                    // reserved interface
                }
                break;
            case ORDER_BY:
                SqlOrderBy sqlOrderBy = (SqlOrderBy) sqlNode;
                boolean isJSON = false;
                boolean isJSONs = false;
                if (((SqlOrderBy) sqlNode).query.toString().contains(KConstants.KSQL.JSON)) {
                    if (((SqlOrderBy) sqlNode).query.toString().contains(KConstants.KSQL.JSONS)) {
                        isJSONs = true;
                    } else {
                        isJSON = true;
                    }
                }
                if (!StrUtils.isNull(sqlOrderBy.fetch.toString())) {
                    long limit = 0L;
                    try {
                        limit = Long.parseLong(sqlOrderBy.fetch.toString());
                    } catch (Exception e) {
                        ErrorUtils.print(KSqlParser.class).error("Parser limit string to long has error, msg is ", e);
                    }
                    ksql.setLimit(limit);
                }
                if (sqlOrderBy.orderList.size() > 0) {
                    SqlNode sqlZNode = sqlOrderBy.orderList.get(0);
                    if (sqlZNode instanceof SqlBasicCall) {
                        SqlBasicCall sqlBZNode = (SqlBasicCall) sqlZNode;
                        FieldSchemaStrategy field = new FieldSchemaStrategy();
                        field.setKey(sqlBZNode.operands[0].toString());
                        field.setType(KConstants.KSQL.ORDER_BY);
                        field.setValue(sqlBZNode.getOperator().toString());
                        field.setJsonUdf(isJSON);
                        field.setJsonsUdf(isJSONs);
                        ksql.getFieldSchema().add(field);
                    } else {
                        FieldSchemaStrategy field = new FieldSchemaStrategy();
                        field.setKey(sqlZNode.toString());
                        field.setType(KConstants.KSQL.ORDER_BY);
                        field.setValue(KConstants.KSQL.ORDER_BY_DEFAULT);
                        field.setJsonUdf(isJSON);
                        field.setJsonsUdf(isJSONs);
                        ksql.getFieldSchema().add(field);
                    }
                }
                parseStmt(sqlOrderBy.query, ksql);
            default:
                break;
        }
    }

}
