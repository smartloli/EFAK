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

import java.util.concurrent.CountDownLatch;

/**
 * // NOTE
 *
 * @author smartloli.
 * <p>
 * Created by Sep 11, 2020
 */
public class Client {
    public static void main(String[] args) {
        int allTaskCount = 100;//Total Task is 100
        //Stats tasks finish
        CountDownLatch countDownLatch = new CountDownLatch(allTaskCount);
        MasterSchedule master = new MasterSchedule(new WorkerSchedule(), 10, countDownLatch);
        for (int i = 1; i <= allTaskCount; i++) {
            TaskStrategy task = new TaskStrategy();
            task.setId(i);
            task.setName("任务" + i);
            task.setPrice(i);
            master.submit(task);
        }

        master.execute();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Result: " + master.getResult());
//		while(true){
//			if(master.isComplete()){
//
//				break;
//			}
//		}


    }
}
