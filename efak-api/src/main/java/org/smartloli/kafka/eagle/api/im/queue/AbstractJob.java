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

import com.alibaba.fastjson.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.smartloli.kafka.eagle.common.protocol.alarm.queue.BaseJobContext;
import org.smartloli.kafka.eagle.common.util.HttpClientUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.AlarmQueue;
import org.smartloli.kafka.eagle.common.util.LoggerUtils;

import java.text.MessageFormat;
import java.util.Map;

/**
 * alarm AbstractJob
 *
 * AbstractJob
 * author：yinzhidong
 * email：yinzhidong@shizhuang-inc.com
 * time：2022/6/30 5:51 下午
 */
public abstract class AbstractJob implements Job {

    public final static String SEND_MSG = "{0} SendMsg Result: {1}";

    public final static String SEND_ERROR_MSG = "Send alarm message has error by {0}, msg is ";

    /**
     * Send alarm information
     */
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        BaseJobContext bjc = (BaseJobContext) jobContext.getJobDetail().getJobDataMap().get(AlarmQueue.JOB_PARAMS);
        sendMsg(bjc.getData(), bjc.getUrl());
    }


    private int sendMsg(String data, String url) {
        Class<? extends AbstractJob> jobClass = this.getClass();
        String simpleClassName = jobClass.getSimpleName();

        try {
            // abstrct method parseSendMessage, that different alarm platforms, different implementations
            Map<String, Object> parseSendMessage = this.parseSendMessage(data, url);
            String result = HttpClientUtils.doPostJson(url, JSONObject.toJSONString(parseSendMessage));

            String info = MessageFormat.format(SEND_MSG, simpleClassName, result);
            LoggerUtils.print(jobClass).info(info);
        } catch (Exception e) {
            String errorInfo = MessageFormat.format(SEND_ERROR_MSG, simpleClassName);
            LoggerUtils.print(jobClass).error(errorInfo, e);
            return 0;
        }
        return 1;
    }


    /**
     * parse alarm information of Map
     *
     * @param data
     * @param url
     * @return
     */
    protected abstract Map<String, Object> parseSendMessage(String data, String url);

}
