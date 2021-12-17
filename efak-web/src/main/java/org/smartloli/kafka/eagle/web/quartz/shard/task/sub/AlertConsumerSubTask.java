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

import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.api.im.IMFactory;
import org.smartloli.kafka.eagle.api.im.IMService;
import org.smartloli.kafka.eagle.api.im.IMServiceImpl;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmConfigInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmConsumerInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmMessageInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.JSONUtils;
import org.smartloli.kafka.eagle.common.util.KConstants;
import org.smartloli.kafka.eagle.common.util.LoggerUtils;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsFactory;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.AlertServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Alert consumer metrics sub task.
 *
 * @author smartloli.
 * <p>
 * Created by Dec 09, 2021
 */
public class AlertConsumerSubTask extends Thread {

    private KafkaMetricsService kafkaMetricsService = new KafkaMetricsFactory().create();

    @Override
    public synchronized void run() {
        // run consumer metrics job
        Consumer consumer = new Consumer();
        consumer.consumer();
    }

    class Consumer {
        public void consumer() {
            try {
                AlertServiceImpl alertService = StartupListener.getBean("alertServiceImpl", AlertServiceImpl.class);
                List<AlarmConsumerInfo> alarmConsumers = alertService.getAllAlarmConsumerTasks();
                for (AlarmConsumerInfo alarmConsumer : alarmConsumers) {
                    if (KConstants.AlarmType.DISABLE.equals(alarmConsumer.getIsEnable())) {
                        break;
                    }

                    Map<String, Object> map = new HashMap<>();
                    map.put("cluster", alarmConsumer.getCluster());
                    map.put("alarmGroup", alarmConsumer.getAlarmGroup());
                    AlarmConfigInfo alarmConfing = alertService.getAlarmConfigByGroupName(map);

                    Map<String, Object> params = new HashMap<>();
                    params.put("cluster", alarmConsumer.getCluster());
                    params.put("tday", CalendarUtils.getCustomDate("yyyyMMdd"));
                    params.put("group", alarmConsumer.getGroup());
                    params.put("topic", alarmConsumer.getTopic());
                    // real consumer lag
                    long lag = alertService.queryLastestLag(params);
                    // alert common info
                    AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
                    try {
                        alarmMsg.setAlarmId(alarmConsumer.getId());
                        alarmMsg.setAlarmCluster(alarmConfing.getCluster());
                        alarmMsg.setAlarmDate(CalendarUtils.getDate());
                        alarmMsg.setAlarmLevel(alarmConsumer.getAlarmLevel());
                        alarmMsg.setAlarmProject("Consumer");
                        alarmMsg.setAlarmTimes("current(" + alarmConsumer.getAlarmTimes() + "), max(" + alarmConsumer.getAlarmMaxTimes() + ")");
                    } catch (Exception e) {
                        LoggerUtils.print(this.getClass()).error("Alert message load common information has error, msg is ", e);
                    }
                    if (lag > alarmConsumer.getLag() && (alarmConsumer.getAlarmTimes() < alarmConsumer.getAlarmMaxTimes() || alarmConsumer.getAlarmMaxTimes() == -1)) {
                        // alarm consumer
                        alarmConsumer.setAlarmTimes(alarmConsumer.getAlarmTimes() + 1);
                        alarmConsumer.setIsNormal("N");
                        alertService.modifyConsumerStatusAlertById(alarmConsumer);
                        try {
                            sendAlarmConsumerError(alarmConfing, alarmConsumer, lag, alarmMsg);
                        } catch (Exception e) {
                            LoggerUtils.print(this.getClass()).error("Send alarm consumer exception has error, msg is ", e);
                        }
                    } else if (lag <= alarmConsumer.getLag()) {
                        if (alarmConsumer.getIsNormal().equals("N")) {
                            alarmConsumer.setIsNormal("Y");
                            // clear error alarm and reset
                            alarmConsumer.setAlarmTimes(0);
                            // notify the cancel of the alarm
                            alertService.modifyConsumerStatusAlertById(alarmConsumer);
                            try {
                                sendAlarmConsumerNormal(alarmConfing, alarmConsumer, lag, alarmMsg);
                            } catch (Exception e) {
                                LoggerUtils.print(this.getClass()).error("Send alarm consumer normal has error, msg is ", e);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LoggerUtils.print(this.getClass()).error("Alarm consumer lag has error, msg is ", e);
            }
        }

        private void sendAlarmConsumerError(AlarmConfigInfo alarmConfing, AlarmConsumerInfo alarmConsumer, long lag, AlarmMessageInfo alarmMsg) {
            if (alarmConfing.getAlarmType().equals(KConstants.AlarmType.EMAIL)) {
                alarmMsg.setTitle("EFAK - Alert Consumer Notice");
                alarmMsg.setAlarmStatus("PROBLEM");
                alarmMsg.setAlarmContent("lag.overflow [ cluster(" + alarmConsumer.getCluster() + "), group(" + alarmConsumer.getGroup() + "), topic(" + alarmConsumer.getTopic() + "), current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]");
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
                alarmMsg.setTitle("EFAK - Alert Consumer Notice");
                alarmMsg.setAlarmContent("lag.overflow [ cluster(" + alarmConsumer.getCluster() + "), group(" + alarmConsumer.getGroup() + "), topic(" + alarmConsumer.getTopic() + "), current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]");
                alarmMsg.setAlarmStatus("PROBLEM");
                IMService im = new IMFactory().create();
                im.sendPostMsgByDingDing(alarmMsg.toDingDingMarkDown(), alarmConfing.getAlarmUrl());
            } else if (alarmConfing.getAlarmType().equals(KConstants.AlarmType.WeChat)) {
                alarmMsg.setTitle("`EFAK - Alert Consumer Notice`\n");
                alarmMsg.setAlarmContent("<font color=\"warning\">lag.overflow [ cluster(" + alarmConsumer.getCluster() + "), group(" + alarmConsumer.getGroup() + "), topic(" + alarmConsumer.getTopic() + "), current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]</font>");
                alarmMsg.setAlarmStatus("<font color=\"warning\">PROBLEM</font>");
                IMServiceImpl im = new IMServiceImpl();
                im.sendPostMsgByWeChat(alarmMsg.toWeChatMarkDown(), alarmConfing.getAlarmUrl());
            }
        }

        private void sendAlarmConsumerNormal(AlarmConfigInfo alarmConfing, AlarmConsumerInfo alarmConsumer, long lag, AlarmMessageInfo alarmMsg) {
            if (alarmConfing.getAlarmType().equals(KConstants.AlarmType.EMAIL)) {
                alarmMsg.setTitle("EFAK - Alert Consumer Cancel");
                alarmMsg.setAlarmContent("lag.normal [ cluster(" + alarmConsumer.getCluster() + "), group(" + alarmConsumer.getGroup() + "), topic(" + alarmConsumer.getTopic() + "), current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]");
                alarmMsg.setAlarmStatus("NORMAL");
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
                alarmMsg.setTitle("EFAK - Alert Consumer Notice");
                alarmMsg.setAlarmContent("lag.normal [ cluster(" + alarmConsumer.getCluster() + "), group(" + alarmConsumer.getGroup() + "), topic(" + alarmConsumer.getTopic() + "), current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]");
                alarmMsg.setAlarmStatus("NORMAL");
                IMService im = new IMFactory().create();
                im.sendPostMsgByDingDing(alarmMsg.toDingDingMarkDown(), alarmConfing.getAlarmUrl());
            } else if (alarmConfing.getAlarmType().equals(KConstants.AlarmType.WeChat)) {
                alarmMsg.setTitle("`EFAK - Alert Consumer Notice`\n");
                alarmMsg.setAlarmContent("<font color=\"#008000\">lag.normal [ cluster(" + alarmConsumer.getCluster() + "), group(" + alarmConsumer.getGroup() + "), topic(" + alarmConsumer.getTopic() + "), current(" + lag + "), max(" + alarmConsumer.getLag() + ") ]</font>");
                alarmMsg.setAlarmStatus("<font color=\"#008000\">NORMAL</font>");
                IMServiceImpl im = new IMServiceImpl();
                im.sendPostMsgByWeChat(alarmMsg.toWeChatMarkDown(), alarmConfing.getAlarmUrl());
            }
        }
    }
}
