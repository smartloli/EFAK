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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmClusterInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmConfigInfo;
import org.smartloli.kafka.eagle.common.protocol.topic.TopicLogSize;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.LoggerUtils;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsService;
import org.smartloli.kafka.eagle.web.service.impl.AlertServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Producer alarm information.
 *
 * @author LambertCOL.
 * <p>
 * Created by Dec 26, 2021
 */
public class ProducerClusterStrategy implements ClusterStrategy {

    @Override
    public void execute(AlarmClusterInfo cluster, AlarmConfigInfo alarmConfig, KafkaMetricsService kafkaMetricsService, AlertServiceImpl alertService) {
        JSONObject producerAlarmJson = JSON.parseObject(cluster.getServer());
        String topic = producerAlarmJson.getString("topic");
        String[] speeds = producerAlarmJson.getString("speed").split(",");
        String duration_tmp = producerAlarmJson.getString("duration"); // unit is minute
        int duration = 1;
        if(duration_tmp == null){
            duration = 1;
        }
        try{
            duration = Integer.valueOf(duration_tmp).intValue();
        }catch (NumberFormatException e){
            throw new RuntimeException(e);
        }

        long startSpeed = 0L;
        long endSpeed = 0L;
        if (speeds.length == 2) {
            startSpeed = Long.parseLong(speeds[0]);
            endSpeed = Long.parseLong(speeds[1]);
        }
        Map<String, Object> producerSpeedParams = new HashMap<>();
        producerSpeedParams.put("cluster", cluster.getCluster());
        producerSpeedParams.put("topic", topic);
        producerSpeedParams.put("stime", CalendarUtils.getCustomDate("yyyyMMdd"));
        producerSpeedParams.put("duration", duration);
        List<TopicLogSize> topicLogSizes = alertService.queryTopicProducerByAlarm(producerSpeedParams);
        long realSpeed = 0;
        if (topicLogSizes != null && topicLogSizes.size() > 0) {
            realSpeed = topicLogSizes.get(0).getDiffval();
        }

        JSONObject alarmTopicMsg = new JSONObject();
        alarmTopicMsg.put("topic", topic);
        alarmTopicMsg.put("alarmSpeeds", startSpeed + "," + endSpeed);
        alarmTopicMsg.put("realSpeeds", realSpeed);
        if ((realSpeed < startSpeed || realSpeed > endSpeed) && (cluster.getAlarmTimes() < cluster.getAlarmMaxTimes() || cluster.getAlarmMaxTimes() == -1)) {
            cluster.setAlarmTimes(cluster.getAlarmTimes() + 1);
            cluster.setIsNormal("N");
            alertService.modifyClusterStatusAlertById(cluster);
            try {
                ClusterStrategyContext.sendAlarmClusterError(alarmConfig, cluster, alarmTopicMsg.toJSONString());
            } catch (Exception e) {
                LoggerUtils.print(this.getClass()).error("Send alarm cluser exception has error, msg is ", e);
            }
        } else if (realSpeed >= startSpeed && realSpeed <= endSpeed) {
            if (cluster.getIsNormal().equals("N")) {
                cluster.setIsNormal("Y");
                // clear error alarm and reset
                cluster.setAlarmTimes(0);
                // notify the cancel of the alarm
                alertService.modifyClusterStatusAlertById(cluster);
                try {
                    ClusterStrategyContext.sendAlarmClusterNormal(alarmConfig, cluster, alarmTopicMsg.toJSONString());
                } catch (Exception e) {
                    LoggerUtils.print(this.getClass()).error("Send alarm cluser normal has error, msg is ", e);
                }
            }
        }
    }
}
