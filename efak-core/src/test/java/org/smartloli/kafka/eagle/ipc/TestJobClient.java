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

import org.smartloli.kafka.eagle.core.task.schedule.JobClient;

import java.util.Date;

/**
 * Test JobClient
 *
 * @author smartloli.
 * <p>
 * Created by Sep 20, 2020
 */
public class TestJobClient {
    public static void main(String[] args) {
        // partition-0 -> 90
//        String sql = "select * from test16 where `partition` in (0,1,2) and JSON(msg,'id')=1 limit 10";
        String sql = "select * from ke1115 where `partition` in (0) limit 10";
        String jobId = "job_id_" + new Date().getTime();
        String result = JobClient.physicsSubmit("cluster1", sql, jobId);
        System.out.println(result);
    }
}
