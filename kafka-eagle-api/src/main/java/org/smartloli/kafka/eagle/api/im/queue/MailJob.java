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
package org.smartloli.kafka.eagle.api.im.queue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.message.BasicNameValuePair;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.smartloli.kafka.eagle.api.util.MailFactoryUtils;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmEmailJsonInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.queue.BaseJobContext;
import org.smartloli.kafka.eagle.common.util.ErrorUtils;
import org.smartloli.kafka.eagle.common.util.HttpClientUtils;
import org.smartloli.kafka.eagle.common.util.JSONUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.AlarmQueue;

import java.util.Arrays;

/**
 * Add alarm message to wechat job queue.
 *
 * @author smartloli.
 * <p>
 * Created by Oct 27, 2019
 */
public class MailJob implements Job {

    /**
     * Send alarm information by mail or webhook.
     */
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        BaseJobContext bjc = (BaseJobContext) jobContext.getJobDetail().getJobDataMap().get(AlarmQueue.JOB_PARAMS);
        sendMsg(bjc.getData(), bjc.getUrl());
    }

    private int sendMsg(String data, String url) {
        if (JSONUtils.isJsonObject(url)) {
            try {
                AlarmEmailJsonInfo email = JSON.parseObject(url, AlarmEmailJsonInfo.class);
                MailFactoryUtils.send(email, data);
            } catch (Exception e) {
                ErrorUtils.print(this.getClass()).error("Send alarm message has error by mail address, msg is ", e);
                return 0;
            }
        } else {
            try {
                JSONObject object = JSON.parseObject(data);
                BasicNameValuePair address = new BasicNameValuePair("address", object.getString("address"));
                BasicNameValuePair msg = new BasicNameValuePair("msg", object.getString("msg"));
                HttpClientUtils.doPostForm(url, Arrays.asList(address, msg));
            } catch (Exception e) {
                ErrorUtils.print(this.getClass()).error("Send alarm message has error by mail, msg is ", e);
                return 0;
            }
        }
        return 1;
    }

}
