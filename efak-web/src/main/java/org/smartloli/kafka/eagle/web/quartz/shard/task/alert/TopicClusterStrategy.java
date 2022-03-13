package org.smartloli.kafka.eagle.web.quartz.shard.task.alert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmClusterInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmConfigInfo;
import org.smartloli.kafka.eagle.common.util.LoggerUtils;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.AlertServiceImpl;
import org.smartloli.kafka.eagle.web.service.impl.TopicServiceImpl;

import java.util.*;
import java.util.regex.Pattern;

public class TopicClusterStrategy implements ClusterStrategy {

    @Override
    public void execute(AlarmClusterInfo cluster, AlarmConfigInfo alarmConfig, KafkaMetricsService kafkaMetricsService, AlertServiceImpl alertService) {
        JSONObject topicAlarmJson = JSON.parseObject(cluster.getServer());
        List<String> topics = this.getMonitoredTopics(cluster);
        if (topics.isEmpty()) {
            return;
        }
        long alarmCapacity = topicAlarmJson.getLong("capacity");
        long realCapacity = 0L;
        boolean isNormal = true;
        boolean allNormal = true;
        Map<String, Integer> numberOfAlarmMap = this.getNumberOfAlarmMap(topics, cluster.getAlarmTimes());
        for (int i = 0; i < topics.size(); i++) {
            String topic = topics.get(i);
            try {
                realCapacity = kafkaMetricsService.topicCapacity(cluster.getCluster(), topic);
            } catch (Exception e) {
                LoggerUtils.print(this.getClass()).error("Get topic capacity has error, msg is ", e);
            }
            // if each 'realCapacity' is less than 'alarmCapacity', 'allNormal' is true
            allNormal &= realCapacity < alarmCapacity;
            JSONObject alarmTopicMsg = new JSONObject();
            alarmTopicMsg.put("topic", topic);
            alarmTopicMsg.put("alarmCapacity", alarmCapacity);
            alarmTopicMsg.put("realCapacity", realCapacity);
            if (realCapacity > alarmCapacity && (numberOfAlarmMap.get(topic) < cluster.getAlarmMaxTimes() || cluster.getAlarmMaxTimes() == -1)) {
                numberOfAlarmMap.put(topic, numberOfAlarmMap.get(topic) + 1);
                int tmp = cluster.getAlarmTimes();
                // for sendAlarmClusterError
                cluster.setAlarmTimes(numberOfAlarmMap.get(topic));
                // as long as one reaches the alarm standard, isNormal is false
                if (isNormal) {
                    isNormal = false;
                }
                try {
                    ClusterStrategyContext.sendAlarmClusterError(alarmConfig, cluster, alarmTopicMsg.toJSONString());
                } catch (Exception e) {
                    LoggerUtils.print(this.getClass()).error("Send alarm cluster exception has error, msg is ", e);
                }
                // for update the database
                cluster.setAlarmTimes(tmp);
            } else if (allNormal && i == topics.size() - 1) {
                // if each cluster is normal and cluster's 'IsNormal' is 'N' in database, update the 'N' of 'isNormal' to 'Y'  and '0' to 'alarmTimes' in database
                if (cluster.getIsNormal().equals("N")) {
                    cluster.setIsNormal("Y");
                    // clear error alarm and reset
                    cluster.setAlarmTimes(0);
                    // notify the cancel of the alarm
                    alertService.modifyClusterStatusAlertById(cluster);
                    try {
                        alarmTopicMsg.put("topic", topicAlarmJson.getString("topic"));
                        ClusterStrategyContext.sendAlarmClusterNormal(alarmConfig, cluster, alarmTopicMsg.toJSONString());
                    } catch (Exception e) {
                        LoggerUtils.print(this.getClass()).error("Send alarm cluster normal has error, msg is ", e);
                    }
                }
            }
        }
        // as long as one reaches the alarm standard, isNormal is false, update isNormal in the database to 'N', and add 1 to 'alarmTimes'
        if (!isNormal) {
            cluster.setAlarmTimes(cluster.getAlarmTimes() + 1);
            cluster.setIsNormal("N");
            alertService.modifyClusterStatusAlertById(cluster);
        }
    }

    /**
     * obtain topics which matching regular expressions
     */
    private List<String> getMonitoredTopics(AlarmClusterInfo cluster) {
        TopicServiceImpl topicService = StartupListener.getBean("topicServiceImpl", TopicServiceImpl.class);
        List<String> topicList = topicService.listTopic(cluster.getCluster());
        if (null == topicList || topicList.size() == 0) {
            return Collections.emptyList();
        }
        JSONObject topicAlarmJson = JSON.parseObject(cluster.getServer());
        String regex = topicAlarmJson.getString("topic");
        List<String> monitoredTopicList = new ArrayList<>(topicList.size());
        Pattern pattern = Pattern.compile(regex);
        for (String topic : topicList) {
            if (pattern.matcher(topic).find()) {
                monitoredTopicList.add(topic);
            }
        }
        return monitoredTopicList;
    }

    /**
     * initialize the alarmTimes for each topic to 'alarmTimes' to prevent missing or over-calculating alarmTimes
     */
    private Map<String, Integer> getNumberOfAlarmMap(List<String> topics, int alarmTimes) {
        HashMap<String, Integer> map = new HashMap<>();
        for (String topic : topics) {
            map.put(topic, alarmTimes);
        }
        return map;
    }
}
