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

import com.alibaba.fastjson.JSONArray;
import org.smartloli.kafka.eagle.common.util.ErrorUtils;
import org.smartloli.kafka.eagle.core.task.strategy.KSqlStrategy;
import org.smartloli.kafka.eagle.core.task.strategy.WorkNodeStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Master is the controller, which is used to receive the task sent by the client
 * and assign the task to each worker. After calculation, each worker returns
 * the result to the master.
 *
 * @author smartloli.
 * <p>
 * Created by Sep 11, 2020
 */
public class MasterSchedule {
    // 1.Defined the add task container
    private ConcurrentLinkedQueue<KSqlStrategy> taskContainer = new ConcurrentLinkedQueue<KSqlStrategy>();
    // 2.That's the WorkerSchedule container, that's a thread
    private HashMap<String, Thread> workers = new HashMap<String, Thread>();
    // 3.That's the add result container
    // private ConcurrentHashMap<String, Object> result = new ConcurrentHashMap<String, Object>();
    private CopyOnWriteArrayList<JSONArray> result = new CopyOnWriteArrayList<>();
    // 4.Statistics of task completion
    private final CountDownLatch workerComplete;

    // 5.Initialize WorkerSchedule
    public MasterSchedule(WorkerSchedule worker, List<WorkNodeStrategy> workNodes, CountDownLatch countDownLatch) {
        // Each worker must have a reference to the master to assign tasks
        worker.setTaskContainer(taskContainer);
        // Send the result to work, which is used to return to master after calculation
        worker.setResult(result);
        this.workerComplete = countDownLatch;
        worker.setCountDownLatch(workerComplete);

        for (WorkNodeStrategy workNode : workNodes) {
            worker.setWorkNodeHost(workNode.getHost());
            worker.setWorkNodePort(workNode.getPort());
            workers.put("WorkNodeServer[" + workNode.toString() + "]", new Thread(worker));
        }
    }

    // 6.Submit task
    public void submit(KSqlStrategy task) {
        taskContainer.add(task);
    }

    // 7.Execute task
    public void execute() {
        for (Map.Entry<String, Thread> work : workers.entrySet()) {
            ErrorUtils.print(this.getClass()).info(work.getKey() + " task has submit.");
            work.getValue().start();
        }
    }

    public CopyOnWriteArrayList<JSONArray> getResult() {
        return result;
    }
}
