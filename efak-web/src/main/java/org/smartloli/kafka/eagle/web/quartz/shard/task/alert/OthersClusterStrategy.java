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
package org.smartloli.kafka.eagle.web.quartz.shard.task.alert;

import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmClusterInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmConfigInfo;
import org.smartloli.kafka.eagle.common.util.LoggerUtils;
import org.smartloli.kafka.eagle.common.util.NetUtils;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsService;
import org.smartloli.kafka.eagle.web.service.impl.AlertServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * General alarm information of cluster nodes.
 *
 * @author LambertCOL.
 * <p>
 * Created by Dec 26, 2021
 */
public class OthersClusterStrategy implements ClusterStrategy {

    @Override
    public void execute(AlarmClusterInfo cluster, AlarmConfigInfo alarmConfig, KafkaMetricsService kafkaMetricsService, AlertServiceImpl alertService) {
        String[] servers = cluster.getServer().split(",");
        List<String> errorServers = new ArrayList<String>();
        List<String> normalServers = new ArrayList<String>();
        for (String server : servers) {
            String host = server.split(":")[0];
            int port = 0;
            try {
                port = Integer.parseInt(server.split(":")[1]);
                boolean status = NetUtils.telnet(host, port);
                if (!status) {
                    errorServers.add(server);
                } else {
                    normalServers.add(server);
                }
            } catch (Exception e) {
                LoggerUtils.print(this.getClass()).error("Alarm cluster has error, msg is ", e);
            }
        }
        if (errorServers.size() > 0 && (cluster.getAlarmTimes() < cluster.getAlarmMaxTimes() || cluster.getAlarmMaxTimes() == -1)) {
            cluster.setAlarmTimes(cluster.getAlarmTimes() + 1);
            cluster.setIsNormal("N");
            alertService.modifyClusterStatusAlertById(cluster);
            try {
                ClusterStrategyContext.sendAlarmClusterError(alarmConfig, cluster, errorServers.toString());
            } catch (Exception e) {
                LoggerUtils.print(this.getClass()).error("Send alarm cluster exception has error, msg is ", e);
            }
        } else if (errorServers.size() == 0) {
            if (cluster.getIsNormal().equals("N")) {
                cluster.setIsNormal("Y");
                // clear error alarm and reset
                cluster.setAlarmTimes(0);
                // notify the cancel of the alarm
                alertService.modifyClusterStatusAlertById(cluster);
                try {
                    ClusterStrategyContext.sendAlarmClusterNormal(alarmConfig, cluster, normalServers.toString());
                } catch (Exception e) {
                    LoggerUtils.print(this.getClass()).error("Send alarm cluster normal has error, msg is ", e);
                }
            }
        }
    }

}
