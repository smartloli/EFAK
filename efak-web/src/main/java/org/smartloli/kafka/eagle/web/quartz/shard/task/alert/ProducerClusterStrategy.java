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

public class ProducerClusterStrategy implements ClusterStrategy {

    @Override
    public void execute(AlarmClusterInfo cluster, AlarmConfigInfo alarmConfig, KafkaMetricsService kafkaMetricsService, AlertServiceImpl alertService) {
        JSONObject producerAlarmJson = JSON.parseObject(cluster.getServer());
        String topic = producerAlarmJson.getString("topic");
        String[] speeds = producerAlarmJson.getString("speed").split(",");
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
