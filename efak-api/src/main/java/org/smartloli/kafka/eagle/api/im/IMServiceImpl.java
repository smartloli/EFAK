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
package org.smartloli.kafka.eagle.api.im;

import java.util.Date;

import org.smartloli.kafka.eagle.api.im.queue.DingDingJob;
import org.smartloli.kafka.eagle.api.im.queue.KafkaJob;
import org.smartloli.kafka.eagle.api.im.queue.MailJob;
import org.smartloli.kafka.eagle.api.im.queue.WeChatJob;
import org.smartloli.kafka.eagle.common.protocol.alarm.queue.BaseJobContext;
import org.smartloli.kafka.eagle.common.util.QuartzManagerUtils;

/**
 * Implements IMService all method.
 * 
 * @author smartloli.
 *
 *         Created by Jan 1, 2019
 */
public class IMServiceImpl implements IMService {

	private static final String KE_JOB_ID = "ke_job_id_";

	/** Send Json msg by dingding. */
	@Override
	public void sendPostMsgByDingDing(String data, String url) {
		BaseJobContext jobContext = new BaseJobContext();
		jobContext.setData(data);
		jobContext.setUrl(url);
		QuartzManagerUtils.addJob(jobContext, KE_JOB_ID + new Date().getTime(), DingDingJob.class, QuartzManagerUtils.getCron(new Date(), 5));
	}

	@Override
	public void sendPostMsgByWeChat(String data, String url) {
		BaseJobContext jobContext = new BaseJobContext();
		jobContext.setData(data);
		jobContext.setUrl(url);
		QuartzManagerUtils.addJob(jobContext, KE_JOB_ID + new Date().getTime(), WeChatJob.class, QuartzManagerUtils.getCron(new Date(), 5));
	}

	@Override
	public void sendPostMsgByMail(String data, String url) {
		BaseJobContext jobContext = new BaseJobContext();
		jobContext.setData(data);
		jobContext.setUrl(url);
		QuartzManagerUtils.addJob(jobContext, KE_JOB_ID + new Date().getTime(), MailJob.class, QuartzManagerUtils.getCron(new Date(), 5));
	}

	@Override
	public void sendPostMsgByKafka(String data, String topic) {
		BaseJobContext jobContext = new BaseJobContext();
		jobContext.setData(data);
		jobContext.setUrl(topic);
		QuartzManagerUtils.addJob(jobContext, KE_JOB_ID + new Date().getTime(), KafkaJob.class, QuartzManagerUtils.getCron(new Date(), 5));
	}
}
