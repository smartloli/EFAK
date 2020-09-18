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
package org.smartloli.kafka.eagle.ipc;

import org.smartloli.kafka.eagle.core.task.parser.KSqlParser;

/**
 * // NOTE
 *
 * @author smartloli.
 * <p>
 * Created by Sep 13, 2020
 */
public class TestKSqlParser {
    public static void main(String[] args) {
//        String sql = "select * from kk where `partition` in (0,1,2) and JSON(msg,'id') = '2' order by timespan limit 10";
//        String sql2 = "select * from kk where `partition` in (0,1,2) and (msg like 'kafka%' or msg like 'ss%') limit 10";
//        String sql3 = "select * from kk where `partition` in (0,1,2) and JSONS(msg,'id') like '2%' limit 10";
        String sql4 = "select msg as records,`offset`,`partition` as 分区 from kk where `partition` in (0,1,2) and JSON(msg,'id') like '2%' group by JSON(msg,'id') limit 10";
//        System.out.println(KSqlParser.parseQueryKSql(sql, "cluster1"));
//        System.out.println(KSqlParser.parseQueryKSql(sql2, "cluster1"));
//        System.out.println(KSqlParser.parseQueryKSql(sql3, "cluster1"));
        System.out.println(KSqlParser.parseQueryKSql(sql4, "cluster1"));
    }
}
