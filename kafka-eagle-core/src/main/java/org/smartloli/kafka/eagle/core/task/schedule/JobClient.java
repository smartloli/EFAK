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
package org.smartloli.kafka.eagle.core.task.schedule;

import org.smartloli.kafka.eagle.core.task.strategy.KSqlStrategy;
import org.smartloli.kafka.eagle.core.task.strategy.WorkNodeStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * The client generate the query strategy, initializes the master query task,
 * and send the schedule task to the worker through the master.
 *
 * @author smartloli.
 * <p>
 * Created by Sep 11, 2020
 */
public class JobClient {
    public static void main(String[] args) {

        long startMill = System.currentTimeMillis();
        List<KSqlStrategy> tasks = getTaskStrategy();

        //Stats tasks finish
        CountDownLatch countDownLatch = new CountDownLatch(tasks.size());
        MasterSchedule master = new MasterSchedule(new WorkerSchedule(), getWorkNodes(), countDownLatch);

        for (KSqlStrategy ksql : tasks) {
            master.submit(ksql);
        }

        master.execute();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Result: " + master.getResult());
        System.out.println("Spent: " + (System.currentTimeMillis() - startMill));

    }

    private static List<KSqlStrategy> getTaskStrategy() {
        List<KSqlStrategy> tasks = new ArrayList<>();
        KSqlStrategy ksql = new KSqlStrategy();
        ksql.setCluster("cluster1");
        ksql.setTopic("kjson");
        ksql.setPartition(1);
        ksql.setStart(0);
        ksql.setEnd(2);
        tasks.add(ksql);

        KSqlStrategy ksql2 = new KSqlStrategy();
        ksql2.setCluster("cluster1");
        ksql2.setTopic("kjson");
        ksql2.setPartition(0);
        ksql2.setStart(1);
        ksql2.setEnd(3);
        tasks.add(ksql2);
        return tasks;
    }

    private static List<WorkNodeStrategy> getWorkNodes() {
        List<WorkNodeStrategy> nodes = new ArrayList<>();
        WorkNodeStrategy wns01 = new WorkNodeStrategy();
        wns01.setHost("127.0.0.1");
        wns01.setPort(8786);
        nodes.add(wns01);

//        WorkNodeStrategy wns02 = new WorkNodeStrategy();
//        wns02.setHost("127.0.0.1");
//        wns02.setPort(8787);
//        nodes.add(wns02);
        return nodes;
    }
}
