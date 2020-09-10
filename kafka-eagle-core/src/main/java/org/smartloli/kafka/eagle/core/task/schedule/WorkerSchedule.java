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

import org.smartloli.kafka.eagle.core.task.strategy.TaskStrategy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

/**
 * Execute the tasks sent by the master and send the results to the master.
 *
 * @author smartloli.
 * <p>
 * Created by Sep 11, 2020
 */
public class WorkerSchedule implements Runnable {
    private ConcurrentLinkedQueue<TaskStrategy> taskContainer;
    private ConcurrentHashMap<String, Object> result;
    private CountDownLatch workerComplete;

    public void setTaskContainer(ConcurrentLinkedQueue<TaskStrategy> taskContainer) {
        this.taskContainer = taskContainer;
    }

    public void setResult(ConcurrentHashMap<String, Object> result) {
        this.result = result;
    }

    @Override
    public void run() {
        Integer output = null;
        while (true) {
            TaskStrategy input = taskContainer.poll();
            if (input == null) break;
            output = (Integer) handler(input);
            result.put(Integer.toString(input.getId()), output);
            workerComplete.countDown();
        }
    }

    public Object handler(TaskStrategy input) {
        Object output = input.getPrice();
        return output;
    }

    public void setCountDownLatch(CountDownLatch workerComplete) {
        this.workerComplete = workerComplete;

    }
}
