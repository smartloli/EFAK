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
package org.smartloli.kafka.eagle.api.im;

import org.smartloli.kafka.eagle.api.im.queue.DingDingJob;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmCrontabInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmMessageInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.queue.BaseJobContext;
import org.smartloli.kafka.eagle.common.protocol.quartz.QuartzStateInfo;
import org.smartloli.kafka.eagle.common.util.QuartzManagerUtils;

/**
 * Implements IMService all method.
 *
 * @author smartloli.
 * <p>
 * Created by Jan 1, 2019
 */
public class IMServiceImpl implements IMService {

    private static final String KE_JOB_GROUP_ID = "KE_JOB_GROUP_ID_";
    private static final String KE_JOB_NAME = "KE_JOB_NAME_";
    private static final String KE_TRIGGER_GROUP_ID = "KE_TRIGGER_GROUP_ID";
    private static final String KE_TRIGGER_NAME = "KE_TRIGGER_NAME_";

    /**
     * Send Json msg by dingding.
     */
    @Override
    public void sendPostMsgByDingDing(AlarmMessageInfo alarmMessageInfo, String url, AlarmCrontabInfo alarmCrontabInfo, String isNormal) {
//        BaseJobContext jobContext = new BaseJobContext();
//        jobContext.setAlarmMessageInfo(alarmMessageInfo);
//        jobContext.setUrl(url);

        // QuartzManagerUtils.addJob(jobContext, KE_JOB_ID + new Date().getTime(), DingDingJob.class, QuartzManagerUtils.getCron(new Date(), 5));
        if (alarmCrontabInfo != null) {
            String jobGroup = KE_JOB_GROUP_ID + alarmCrontabInfo.getType() + "_" + isNormal + "_" + alarmCrontabInfo.getId();
            String jobName = KE_JOB_NAME + alarmCrontabInfo.getType() + "_" + isNormal + "_" + alarmCrontabInfo.getId();
            String triggerGroup = KE_TRIGGER_GROUP_ID + alarmCrontabInfo.getType() + "_" + isNormal + "_" + alarmCrontabInfo.getId();
            String triggerName = KE_TRIGGER_NAME + alarmCrontabInfo.getType() + "_" + isNormal + "_" + alarmCrontabInfo.getId();
            QuartzStateInfo qsi = new QuartzStateInfo();
            qsi.setCron(alarmCrontabInfo.getCrontab());
            qsi.setAlarmMessageInfo(alarmMessageInfo);
            qsi.setJobClass(DingDingJob.class);
            qsi.setKeJobGroup(jobGroup);
            qsi.setKeJobName(jobName);
            qsi.setKeTriggerGroup(triggerGroup);
            qsi.setKeTriggerName(triggerName);
            qsi.setUrl(url);
            QuartzManagerUtils.replaceJob(qsi);
        }
    }

    @Override
    public void removePostMsgByIM(String id, String type, String isNormal) {
        String jobGroup = KE_JOB_GROUP_ID + type + "_" + isNormal + "_" + id;
        String jobName = KE_JOB_NAME + type + "_" + isNormal + "_" + id;
        String triggerGroup = KE_TRIGGER_GROUP_ID + type + "_" + isNormal + "_" + id;
        String triggerName = KE_TRIGGER_NAME + type + "_" + isNormal + "_" + id;
        QuartzStateInfo qsi = new QuartzStateInfo();
        qsi.setKeJobGroup(jobGroup);
        qsi.setKeJobName(jobName);
        qsi.setKeTriggerGroup(triggerGroup);
        qsi.setKeTriggerName(triggerName);
        QuartzManagerUtils.removeJob(qsi);
    }

    @Override
    public void sendPostMsgByWeChat(String data, String url) {
        BaseJobContext jobContext = new BaseJobContext();
        jobContext.setData(data);
        jobContext.setUrl(url);
        // QuartzManagerUtils.addJob(jobContext, KE_JOB_ID + new Date().getTime(), WeChatJob.class, QuartzManagerUtils.getCron(new Date(), 5));
    }

    @Override
    public void sendPostMsgByMail(String data, String url) {
        BaseJobContext jobContext = new BaseJobContext();
        jobContext.setData(data);
        jobContext.setUrl(url);
        // QuartzManagerUtils.addJob(jobContext, KE_JOB_ID + new Date().getTime(), MailJob.class, QuartzManagerUtils.getCron(new Date(), 5));
    }

}
