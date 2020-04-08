/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.api.im.queue;

import java.util.Arrays;

import org.apache.http.message.BasicNameValuePair;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.protocol.alarm.queue.BaseJobContext;
import org.smartloli.kafka.eagle.common.util.HttpClientUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.AlarmQueue;
import org.smartloli.kafka.eagle.common.util.ThrowExceptionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * Add alarm message to wechat job queue.
 * 
 * @author smartloli.
 *
 *         Created by Oct 27, 2019
 */
public class MailJob implements Job {

	private Logger LOG = LoggerFactory.getLogger(MailJob.class);

	/** Send alarm information by mail. */
	public void execute(JobExecutionContext jobContext) throws JobExecutionException {
		BaseJobContext bjc = (BaseJobContext) jobContext.getJobDetail().getJobDataMap().get(AlarmQueue.JOB_PARAMS);
		sendMsg(bjc.getData(), bjc.getUrl());
	}

	private int sendMsg(String data, String url) {
		try {
			JSONObject object = JSON.parseObject(data);
			BasicNameValuePair address = new BasicNameValuePair("address", object.getString("address"));
			BasicNameValuePair msg = new BasicNameValuePair("msg", object.getString("msg"));
			HttpClientUtils.doPostForm(url, Arrays.asList(address, msg));
		} catch (Exception e) {
			ThrowExceptionUtils.print(this.getClass()).error("Send alarm message has error by mail, msg is ", e);
			LOG.error("Send alarm message has error by mail, msg is ", e);
			return 0;
		}
		return 1;
	}

}
