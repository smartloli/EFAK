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
package org.smartloli.kafka.eagle.web.quartz.shard.task.sub;

import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmClusterInfo;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.quartz.shard.task.alert.ClusterStrategyContext;
import org.smartloli.kafka.eagle.web.service.impl.AlertServiceImpl;

/**
 * Alert cluster metrics sub task.
 *
 * @author smartloli.
 * <p>
 * Created by Dec 09, 2021
 */
public class AlertClusterSubTask extends Thread {

    @Override
    public synchronized void run() {
        // run cluster metrics job
        Cluster cluster = new Cluster();
        cluster.cluster();
    }

    class Cluster {

        public void cluster() {
            AlertServiceImpl alertService = StartupListener.getBean("alertServiceImpl", AlertServiceImpl.class);
            for (AlarmClusterInfo cluster : alertService.getAllAlarmClusterTasks()) {
                if (KConstants.AlarmType.DISABLE.equals(cluster.getIsEnable())) {
                    break;
                }
                ClusterStrategyContext strategyContext = new ClusterStrategyContext(cluster);
                strategyContext.execute();
            }
        }

    }
}
