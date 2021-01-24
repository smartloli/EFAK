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
import org.smartloli.kafka.eagle.common.util.QuartzManagerUtils;

/**
 * Implements IMService all method.
 *
 * @author smartloli.
 * <p>
 * Created by Jan 1, 2019
 */
public class IMServiceImpl implements IMService {

    private static final String KE_JOB_ID = "ke_job_id_";
    private static final String KE_TRIGGER_ID = "ke_trigger_id_";

    /**
     * Send Json msg by dingding.
     */
    @Override
    public void sendPostMsgByDingDing(AlarmMessageInfo alarmMessageInfo, String url, AlarmCrontabInfo alarmCrontabInfo, String isNormal) {
        BaseJobContext jobContext = new BaseJobContext();
        jobContext.setAlarmMessageInfo(alarmMessageInfo);
        jobContext.setUrl(url);
        String jobName = KE_JOB_ID + alarmCrontabInfo.getType() + "_" + isNormal + "_" + alarmCrontabInfo.getId();
        String triggerName = KE_TRIGGER_ID + alarmCrontabInfo.getType() + "_" + isNormal + "_" + alarmCrontabInfo.getId();
        // QuartzManagerUtils.addJob(jobContext, KE_JOB_ID + new Date().getTime(), DingDingJob.class, QuartzManagerUtils.getCron(new Date(), 5));
        if (alarmCrontabInfo != null) {
            QuartzManagerUtils.replaceJob(jobContext, jobName, triggerName, DingDingJob.class, alarmCrontabInfo.getCrontab());
        }
    }

    @Override
    public void removePostMsgByIM(AlarmCrontabInfo alarmCrontabInfo, String isNormal) {
        String jobName = KE_JOB_ID + alarmCrontabInfo.getType() + "_" + isNormal + "_" + alarmCrontabInfo.getId();
        String triggerName = KE_TRIGGER_ID + alarmCrontabInfo.getType() + "_" + isNormal + "_" + alarmCrontabInfo.getId();
        QuartzManagerUtils.removeJob(jobName, triggerName);
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
