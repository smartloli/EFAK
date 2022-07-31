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

import org.smartloli.kafka.eagle.common.constant.ThreadConstants;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.LoggerUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.task.schedule.JobClient;
import org.smartloli.kafka.eagle.web.quartz.shard.task.sub.CleanChartSubTask;

import java.util.List;
import java.util.Map;
import java.util.Random;

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

    public void cleanCharts() {
        try {
            if (SystemConfigUtils.getBooleanProperty("efak.distributed.enable")) {
                if (KConstants.EFAK.MODE_MASTER.equals(SystemConfigUtils.getProperty("efak.cluster.mode.status"))) {
                    new CleanChartSubTask().start();
                }
            } else {
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
        LoggerUtils.print(this.getClass()).info("Distributed mode start thread on cluster.");
        if (KConstants.EFAK.MODE_SLAVE.equals(SystemConfigUtils.getProperty("efak.cluster.mode.status"))) {
            // sleep job client thread
            try {
                Thread.sleep(new Random().nextInt(KConstants.EFAK.THREAD_SLEEP_TIME_SEED));
            } catch (Exception e) {
                LoggerUtils.print(this.getClass()).error("Sleep job client thread has error, msg is ", e);
            }
            List<String> shardTasks = JobClient.getWorkNodeShardTask();
            if (shardTasks != null) {
                for (String shardTask : shardTasks) {
                    LoggerUtils.print(this.getClass()).info("Task thread [" + shardTask + "]");
                    try {
                        Class subThreadClass = Class.forName(shardTask);
                        Thread thread = (Thread) subThreadClass.newInstance();
                        thread.start();
                    } catch (Exception e) {
                        LoggerUtils.print(this.getClass()).info("Distributed node start thread sub task has error, msg is ", e);
                    }
                }
            }

            try {
                Class subThreadClass = Class.forName(ThreadConstants.SUPER_VIP_SUB_TASK);
                Thread thread = (Thread) subThreadClass.newInstance();
                thread.start();
            } catch (Exception e) {
                LoggerUtils.print(this.getClass()).info("Distributed node start super vip thread sub task has error, msg is ", e);
            }
        }

    }


    // if efak is standalone mode
    private void jobForStandaloneAllTasks() {
        LoggerUtils.print(this.getClass()).info("Standalone mode start thread on one node.");
        for (Map.Entry<String, Integer> entry : ThreadConstants.SUB_TASK_MAP.entrySet()) {
            try {
                Class subThreadClass = Class.forName(entry.getKey());
                Thread thread = (Thread) subThreadClass.newInstance();
                thread.start();
            } catch (Exception e) {
                LoggerUtils.print(this.getClass()).info("Standalone mode start thread sub task has error, msg is ", e);
            }
        }
    }

}
