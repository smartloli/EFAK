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
package org.smartloli.kafka.eagle.web.quartz.shard.task.schedule;

import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.LoggerUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.web.quartz.shard.task.strategy.ThreadConstantsStrategy;
import org.smartloli.kafka.eagle.web.quartz.shard.task.sub.CleanChartSubTask;

import java.util.Map;

/**
 * Unified scheduling and allocation of thread tasks.
 * <p>
 * Application indicators on a cluster.
 *
 * @author smartloli.
 * <p>
 * Created by Dec 09, 2021
 */
public class ScheduleShardSubTask {

//    private List<String> SUB_TASK = Arrays.asList("");


    public void cleanCharts() {
        try {
            if (KConstants.EFAK.MODE_STATUS.equals(SystemConfigUtils.getBooleanProperty("efak.cluster.mode.status"))) {
                new CleanChartSubTask().start();
            }
        } catch (Exception e) {
            LoggerUtils.print(this.getClass()).info("Master node starts cleaning up expired data.");
        }
    }

    public void allocateTask() {
        // whether is enable distributed
        if (SystemConfigUtils.getBooleanProperty("efak.distributed.enable")) {
            jobForDistributedAllTasks();
        } else {
            jobForStandaloneAllTasks();
        }
    }

    // if efak is distributed mode
    private void jobForDistributedAllTasks() {


    }

    // if efak is standalone mode
    private void jobForStandaloneAllTasks() {
        for (Map.Entry<Class<?>, Integer> entry : ThreadConstantsStrategy.SUB_TASK_MAP.entrySet()) {
            try {
                Class subThreadClass = Class.forName(entry.getKey().getName().toString());
                Thread thread = (Thread) subThreadClass.newInstance();
                thread.start();
            } catch (Exception e) {
                LoggerUtils.print(this.getClass()).info("Standalone node start thread sub task has error, msg is ", e);
            }
        }
    }

    public static void main(String[] args) {
        ScheduleShardSubTask ss = new ScheduleShardSubTask();
        ss.allocateTask();
    }

}
