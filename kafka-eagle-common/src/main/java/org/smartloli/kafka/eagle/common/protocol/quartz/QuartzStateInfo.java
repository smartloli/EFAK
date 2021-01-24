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
package org.smartloli.kafka.eagle.common.protocol.quartz;

import org.quartz.Job;
import org.smartloli.kafka.eagle.common.protocol.BaseProtocol;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmMessageInfo;

/**
 * Define quartz state.
 *
 * @author smartloli.
 * <p>
 * Created by Jan 24, 2021
 */
public class QuartzStateInfo extends BaseProtocol {
    private String keJobGroup;
    private String keTriggerGroup;
    private String keJobName;
    private String keTriggerName;

    public AlarmMessageInfo alarmMessageInfo;
    private Class<? extends Job> jobClass;
    private String url;
    private String cron;

    public AlarmMessageInfo getAlarmMessageInfo() {
        return alarmMessageInfo;
    }

    public void setAlarmMessageInfo(AlarmMessageInfo alarmMessageInfo) {
        this.alarmMessageInfo = alarmMessageInfo;
    }

    public Class<? extends Job> getJobClass() {
        return jobClass;
    }

    public void setJobClass(Class<? extends Job> jobClass) {
        this.jobClass = jobClass;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getKeJobGroup() {
        return keJobGroup;
    }

    public void setKeJobGroup(String keJobGroup) {
        this.keJobGroup = keJobGroup;
    }

    public String getKeTriggerGroup() {
        return keTriggerGroup;
    }

    public void setKeTriggerGroup(String keTriggerGroup) {
        this.keTriggerGroup = keTriggerGroup;
    }

    public String getKeJobName() {
        return keJobName;
    }

    public void setKeJobName(String keJobName) {
        this.keJobName = keJobName;
    }

    public String getKeTriggerName() {
        return keTriggerName;
    }

    public void setKeTriggerName(String keTriggerName) {
        this.keTriggerName = keTriggerName;
    }
}
