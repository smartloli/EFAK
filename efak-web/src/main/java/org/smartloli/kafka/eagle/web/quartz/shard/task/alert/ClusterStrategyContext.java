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
import org.smartloli.kafka.eagle.api.im.IMFactory;
import org.smartloli.kafka.eagle.api.im.IMService;
import org.smartloli.kafka.eagle.api.im.IMServiceImpl;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmClusterInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmConfigInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmMessageInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.JSONUtils;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.StrUtils;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsFactory;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.AlertServiceImpl;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Distribute and dispatch different alarm types.
 *
 * @author LambertCOL.
 * <p>
 * Created by Dec 26, 2021
 */
@Component
public class ClusterStrategyContext {

    private String clusterType;

    private ClusterStrategy clusterStrategy;

    private AlarmClusterInfo cluster;

    /**
     * Kafka topic config service interface.
     */
    private KafkaMetricsService kafkaMetricsService = new KafkaMetricsFactory().create();

    private ClusterStrategyContext() {
    }

    public ClusterStrategyContext(AlarmClusterInfo cluster) {
        this.cluster = cluster;
        this.clusterType = cluster.getType();
        switch (this.clusterType) {
            case KConstants.AlarmType.TOPIC:
                this.clusterStrategy = new TopicClusterStrategy();
                break;
            case KConstants.AlarmType.PRODUCER:
                this.clusterStrategy = new ProducerClusterStrategy();
                break;
            default:
                this.clusterStrategy = new OthersClusterStrategy();
                break;
        }
    }

    public void execute() {
        String alarmGroup = cluster.getAlarmGroup();
        Map<String, Object> params = new HashMap<>();
        params.put("cluster", cluster.getCluster());
        params.put("alarmGroup", alarmGroup);
        AlertServiceImpl alertService = StartupListener.getBean("alertServiceImpl", AlertServiceImpl.class);
        AlarmConfigInfo alarmConfig = alertService.getAlarmConfigByGroupName(params);
        clusterStrategy.execute(cluster, alarmConfig, kafkaMetricsService, alertService);
    }

    public static void sendAlarmClusterError(AlarmConfigInfo alarmConfing, AlarmClusterInfo cluster, String server) {
        if (alarmConfing.getAlarmType().equals(KConstants.AlarmType.EMAIL)) {
            AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
            alarmMsg.setAlarmId(cluster.getId());
            alarmMsg.setAlarmCluster(alarmConfing.getCluster());
            alarmMsg.setTitle("EFAK - Alert Cluster Error");
            if (KConstants.AlarmType.TOPIC.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                long alarmCapacity = alarmTopicMsg.getLong("alarmCapacity");
                long realCapacity = alarmTopicMsg.getLong("realCapacity");
                alarmMsg.setAlarmContent("topic.capacity.overflow [topic(" + topic + "), real.capacity(" + StrUtils.stringify(realCapacity) + "), alarm.capacity(" + StrUtils.stringify(alarmCapacity) + ")]");
            } else if (KConstants.AlarmType.PRODUCER.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                String alarmSpeeds = alarmTopicMsg.getString("alarmSpeeds");
                long realSpeeds = alarmTopicMsg.getLong("realSpeeds");
                alarmMsg.setAlarmContent("producer.speed.overflow [topic(" + topic + "), real.speeds(" + realSpeeds + "), alarm.speeds.range(" + alarmSpeeds + ")]");
            } else {
                alarmMsg.setAlarmContent("node.shutdown [ " + server + " ]");
            }
            alarmMsg.setAlarmDate(CalendarUtils.getDate());
            alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
            alarmMsg.setAlarmProject(cluster.getType());
            alarmMsg.setAlarmStatus("PROBLEM");
            alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + "), max(" + cluster.getAlarmMaxTimes() + ")");
            IMService im = new IMFactory().create();
            JSONObject object = new JSONObject();
            object.put("address", alarmConfing.getAlarmAddress());
            if (JSONUtils.isJsonObject(alarmConfing.getAlarmUrl())) {
                object.put("msg", alarmMsg.toMailJSON());
            } else {
                object.put("msg", alarmMsg.toMail());
            }
            object.put("title", alarmMsg.getTitle());
            im.sendPostMsgByMail(object.toJSONString(), alarmConfing.getAlarmUrl());
        } else if (alarmConfing.getAlarmType().equals(KConstants.AlarmType.DingDing)) {
            AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
            alarmMsg.setAlarmId(cluster.getId());
            alarmMsg.setAlarmCluster(alarmConfing.getCluster());
            alarmMsg.setTitle("EFAK - Alert Cluster Error");
            if (KConstants.AlarmType.TOPIC.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                long alarmCapacity = alarmTopicMsg.getLong("alarmCapacity");
                long realCapacity = alarmTopicMsg.getLong("realCapacity");
                alarmMsg.setAlarmContent("topic.capacity.overflow [topic(" + topic + "), real.capacity(" + StrUtils.stringify(realCapacity) + "), alarm.capacity(" + StrUtils.stringify(alarmCapacity) + ")]");
            } else if (KConstants.AlarmType.PRODUCER.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                String alarmSpeeds = alarmTopicMsg.getString("alarmSpeeds");
                long realSpeeds = alarmTopicMsg.getLong("realSpeeds");
                alarmMsg.setAlarmContent("producer.speed.overflow [topic(" + topic + "), real.speeds(" + realSpeeds + "), alarm.speeds.range(" + alarmSpeeds + ")]");
            } else {
                alarmMsg.setAlarmContent("node.shutdown [ " + server + " ]");
            }
            alarmMsg.setAlarmDate(CalendarUtils.getDate());
            alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
            alarmMsg.setAlarmProject(cluster.getType());
            alarmMsg.setAlarmStatus("PROBLEM");
            alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + "), max(" + cluster.getAlarmMaxTimes() + ")");
            IMService im = new IMFactory().create();
            im.sendPostMsgByDingDing(alarmMsg.toDingDingMarkDown(), alarmConfing.getAlarmUrl());
        } else if (alarmConfing.getAlarmType().equals(KConstants.AlarmType.WeChat)) {
            AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
            alarmMsg.setAlarmId(cluster.getId());
            alarmMsg.setAlarmCluster(alarmConfing.getCluster());
            alarmMsg.setTitle("`EFAK - Alert Cluster Error`\n");
            if (KConstants.AlarmType.TOPIC.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                long alarmCapacity = alarmTopicMsg.getLong("alarmCapacity");
                long realCapacity = alarmTopicMsg.getLong("realCapacity");
                alarmMsg.setAlarmContent("<font color=\"warning\">topic.capacity.overflow [topic(" + topic + "), real.capacity(" + StrUtils.stringify(realCapacity) + "), alarm.capacity(" + StrUtils.stringify(alarmCapacity) + ")]</font>");
            } else if (KConstants.AlarmType.PRODUCER.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                String alarmSpeeds = alarmTopicMsg.getString("alarmSpeeds");
                long realSpeeds = alarmTopicMsg.getLong("realSpeeds");
                alarmMsg.setAlarmContent("<font color=\"warning\">producer.speed.overflow [topic(" + topic + "), real.speeds(" + realSpeeds + "), alarm.speeds.range(" + alarmSpeeds + ")]</font>");
            } else {
                alarmMsg.setAlarmContent("<font color=\"warning\">node.shutdown [ " + server + " ]</font>");
            }
            alarmMsg.setAlarmDate(CalendarUtils.getDate());
            alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
            alarmMsg.setAlarmProject(cluster.getType());
            alarmMsg.setAlarmStatus("<font color=\"warning\">PROBLEM</font>");
            alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + "), max(" + cluster.getAlarmMaxTimes() + ")");
            IMServiceImpl im = new IMServiceImpl();
            im.sendPostMsgByWeChat(alarmMsg.toWeChatMarkDown(), alarmConfing.getAlarmUrl());
        } else if (alarmConfing.getAlarmType().equals(KConstants.AlarmType.LARK)) {
            AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
            alarmMsg.setAlarmId(cluster.getId());
            alarmMsg.setAlarmCluster(alarmConfing.getCluster());
            alarmMsg.setTitle("`EFAK - Alert Cluster Error`\n");
            if (KConstants.AlarmType.TOPIC.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                long alarmCapacity = alarmTopicMsg.getLong("alarmCapacity");
                long realCapacity = alarmTopicMsg.getLong("realCapacity");
                alarmMsg.setAlarmContent("topic.capacity.overflow [topic(" + topic + "), real.capacity(" + StrUtils.stringify(realCapacity) + "), alarm.capacity(" + StrUtils.stringify(alarmCapacity) + ")]");
            } else if (KConstants.AlarmType.PRODUCER.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                String alarmSpeeds = alarmTopicMsg.getString("alarmSpeeds");
                long realSpeeds = alarmTopicMsg.getLong("realSpeeds");
                alarmMsg.setAlarmContent("producer.speed.overflow [topic(" + topic + "), real.speeds(" + realSpeeds + "), alarm.speeds.range(" + alarmSpeeds + ")]");
            } else {
                alarmMsg.setAlarmContent("node.shutdown [ " + server + " ]");
            }
            alarmMsg.setAlarmDate(CalendarUtils.getDate());
            alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
            alarmMsg.setAlarmProject(cluster.getType());
            alarmMsg.setAlarmStatus("PROBLEM");
            alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + 1 + "), max(" + cluster.getAlarmMaxTimes() + ")");
            IMServiceImpl im = new IMServiceImpl();
            im.sendPostMsgByLark(alarmMsg.toMail(), alarmConfing.getAlarmUrl());
        }
    }

    public static void sendAlarmClusterNormal(AlarmConfigInfo alarmConfing, AlarmClusterInfo cluster, String server) {
        if (alarmConfing.getAlarmType().equals(KConstants.AlarmType.EMAIL)) {
            AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
            alarmMsg.setAlarmId(cluster.getId());
            alarmMsg.setAlarmCluster(alarmConfing.getCluster());
            alarmMsg.setTitle("EFAK - Alert Cluster Notice");
            if (KConstants.AlarmType.TOPIC.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                long alarmCapacity = alarmTopicMsg.getLong("alarmCapacity");
                long realCapacity = alarmTopicMsg.getLong("realCapacity");
                alarmMsg.setAlarmContent("topic.capacity.normal [topic(" + topic + "), real.capacity(" + StrUtils.stringify(realCapacity) + "), alarm.capacity(" + StrUtils.stringify(alarmCapacity) + ")]");
            } else if (KConstants.AlarmType.PRODUCER.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                String alarmSpeeds = alarmTopicMsg.getString("alarmSpeeds");
                long realSpeeds = alarmTopicMsg.getLong("realSpeeds");
                alarmMsg.setAlarmContent("producer.speed.normal [topic(" + topic + "), real.speeds(" + realSpeeds + "), alarm.speeds.range(" + alarmSpeeds + ")]");
            } else {
                alarmMsg.setAlarmContent("node.alive [ " + server + " ]");
            }
            alarmMsg.setAlarmDate(CalendarUtils.getDate());
            alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
            alarmMsg.setAlarmProject(cluster.getType());
            alarmMsg.setAlarmStatus("NORMAL");
            alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + "), max(" + cluster.getAlarmMaxTimes() + ")");
            IMService im = new IMFactory().create();
            JSONObject object = new JSONObject();
            object.put("address", alarmConfing.getAlarmAddress());
            object.put("title", alarmMsg.getTitle());
            if (JSONUtils.isJsonObject(alarmConfing.getAlarmUrl())) {
                object.put("msg", alarmMsg.toMailJSON());
            } else {
                object.put("msg", alarmMsg.toMail());
            }
            im.sendPostMsgByMail(object.toJSONString(), alarmConfing.getAlarmUrl());
        } else if (alarmConfing.getAlarmType().equals(KConstants.AlarmType.DingDing)) {
            AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
            alarmMsg.setAlarmId(cluster.getId());
            alarmMsg.setAlarmCluster(alarmConfing.getCluster());
            alarmMsg.setTitle("EFAK - Alert Cluster Notice");
            if (KConstants.AlarmType.TOPIC.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                long alarmCapacity = alarmTopicMsg.getLong("alarmCapacity");
                long realCapacity = alarmTopicMsg.getLong("realCapacity");
                alarmMsg.setAlarmContent("topic.capacity.normal [topic(" + topic + "), real.capacity(" + StrUtils.stringify(realCapacity) + "), alarm.capacity(" + StrUtils.stringify(alarmCapacity) + ")]");
            } else if (KConstants.AlarmType.PRODUCER.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                String alarmSpeeds = alarmTopicMsg.getString("alarmSpeeds");
                long realSpeeds = alarmTopicMsg.getLong("realSpeeds");
                alarmMsg.setAlarmContent("producer.speed.normal [topic(" + topic + "), real.speeds(" + realSpeeds + "), alarm.speeds.range(" + alarmSpeeds + ")]");
            } else {
                alarmMsg.setAlarmContent("node.alive [ " + server + " ]");
            }
            alarmMsg.setAlarmDate(CalendarUtils.getDate());
            alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
            alarmMsg.setAlarmProject(cluster.getType());
            alarmMsg.setAlarmStatus("NORMAL");
            alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + "), max(" + cluster.getAlarmMaxTimes() + ")");
            IMService im = new IMFactory().create();
            im.sendPostMsgByDingDing(alarmMsg.toDingDingMarkDown(), alarmConfing.getAlarmUrl());
        } else if (alarmConfing.getAlarmType().equals(KConstants.AlarmType.WeChat)) {
            AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
            alarmMsg.setAlarmId(cluster.getId());
            alarmMsg.setAlarmCluster(alarmConfing.getCluster());
            alarmMsg.setTitle("`EFAK - Alert Cluster Notice`\n");
            if (KConstants.AlarmType.TOPIC.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                long alarmCapacity = alarmTopicMsg.getLong("alarmCapacity");
                long realCapacity = alarmTopicMsg.getLong("realCapacity");
                alarmMsg.setAlarmContent("<font color=\"#008000\">topic.capacity.normal [topic(" + topic + "), real.capacity(" + StrUtils.stringify(realCapacity) + "), alarm.capacity(" + StrUtils.stringify(alarmCapacity) + ")]</font>");
            } else if (KConstants.AlarmType.PRODUCER.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                String alarmSpeeds = alarmTopicMsg.getString("alarmSpeeds");
                long realSpeeds = alarmTopicMsg.getLong("realSpeeds");
                alarmMsg.setAlarmContent("<font color=\"#008000\">producer.speed.normal [topic(" + topic + "), real.speeds(" + realSpeeds + "), alarm.speeds.range(" + alarmSpeeds + ")]</font>");
            } else {
                alarmMsg.setAlarmContent("<font color=\"#008000\">node.alive [ " + server + " ]</font>");
            }
            alarmMsg.setAlarmDate(CalendarUtils.getDate());
            alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
            alarmMsg.setAlarmProject(cluster.getType());
            alarmMsg.setAlarmStatus("<font color=\"#008000\">NORMAL</font>");
            alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + "), max(" + cluster.getAlarmMaxTimes() + ")");
            IMServiceImpl im = new IMServiceImpl();
            im.sendPostMsgByWeChat(alarmMsg.toWeChatMarkDown(), alarmConfing.getAlarmUrl());
        } else if (alarmConfing.getAlarmType().equals(KConstants.AlarmType.LARK)) {
            AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
            alarmMsg.setAlarmId(cluster.getId());
            alarmMsg.setAlarmCluster(alarmConfing.getCluster());
            alarmMsg.setTitle("`EFAK - Alert Cluster Error`\n");
            if (KConstants.AlarmType.TOPIC.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                long alarmCapacity = alarmTopicMsg.getLong("alarmCapacity");
                long realCapacity = alarmTopicMsg.getLong("realCapacity");
                alarmMsg.setAlarmContent("topic.capacity.overflow [topic(" + topic + "), real.capacity(" + StrUtils.stringify(realCapacity) + "), alarm.capacity(" + StrUtils.stringify(alarmCapacity) + ")]");
            } else if (KConstants.AlarmType.PRODUCER.equals(cluster.getType())) {
                JSONObject alarmTopicMsg = JSON.parseObject(server);
                String topic = alarmTopicMsg.getString("topic");
                String alarmSpeeds = alarmTopicMsg.getString("alarmSpeeds");
                long realSpeeds = alarmTopicMsg.getLong("realSpeeds");
                alarmMsg.setAlarmContent("producer.speed.overflow [topic(" + topic + "), real.speeds(" + realSpeeds + "), alarm.speeds.range(" + alarmSpeeds + ")]");
            } else {
                alarmMsg.setAlarmContent("node.shutdown [ " + server + " ]");
            }
            alarmMsg.setAlarmDate(CalendarUtils.getDate());
            alarmMsg.setAlarmLevel(cluster.getAlarmLevel());
            alarmMsg.setAlarmProject(cluster.getType());
            alarmMsg.setAlarmStatus("PROBLEM");
            alarmMsg.setAlarmTimes("current(" + cluster.getAlarmTimes() + 1 + "), max(" + cluster.getAlarmMaxTimes() + ")");
            IMServiceImpl im = new IMServiceImpl();
            im.sendPostMsgByLark(alarmMsg.toMail(), alarmConfing.getAlarmUrl());
        }
    }
}
