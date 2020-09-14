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
import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.common.util.ErrorUtils;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.core.task.rpc.MasterNodeClient;
import org.smartloli.kafka.eagle.core.task.strategy.KSqlStrategy;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Execute the tasks sent by the master and send the results to the master.
 *
 * @author smartloli.
 * <p>
 * Created by Sep 11, 2020
 */
public class WorkerSchedule implements Runnable {
    private ConcurrentLinkedQueue<KSqlStrategy> taskContainer;
    private CopyOnWriteArrayList<JSONArray> result;
    private CountDownLatch workerComplete;

    private String workNodeHost;
    private int workNodePort;

    public void setTaskContainer(ConcurrentLinkedQueue<KSqlStrategy> taskContainer) {
        this.taskContainer = taskContainer;
    }

    public void setResult(CopyOnWriteArrayList<JSONArray> result) {
        this.result = result;
    }

    public void setWorkNodeHost(String workNodeHost) {
        this.workNodeHost = workNodeHost;
    }

    public void setWorkNodePort(int workNodePort) {
        this.workNodePort = workNodePort;
    }

    @Override
    public void run() {
        while (true) {
            KSqlStrategy input = taskContainer.poll();
            if (input == null) {
                break;
            }
            result.addAll(handler(input));
            workerComplete.countDown();
        }
    }

    public List<JSONArray> handler(KSqlStrategy input) {
        JSONObject object = new JSONObject();
        object.put(KConstants.Protocol.KEY, KConstants.Protocol.KSQL_QUERY);
        object.put(KConstants.Protocol.VALUE, input);
        MasterNodeClient masterCli = new MasterNodeClient(workNodeHost, workNodePort, object);
        try {
            masterCli.start();
        } catch (Exception e) {
            ErrorUtils.print(this.getClass()).error("Submit worknode[" + workNodeHost + ":" + workNodePort + "] has error, msg is ", e);
        }
        return masterCli.getResult();
    }

    public void setCountDownLatch(CountDownLatch workerComplete) {
        this.workerComplete = workerComplete;
    }
}
