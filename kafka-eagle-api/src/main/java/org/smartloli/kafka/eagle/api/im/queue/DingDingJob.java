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

import java.util.HashMap;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.smartloli.kafka.eagle.common.protocol.alarm.queue.BaseJobContext;
import org.smartloli.kafka.eagle.common.util.HttpClientUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.AlarmQueue;
import org.smartloli.kafka.eagle.common.util.KConstants.IM;

import com.alibaba.fastjson.JSONObject;

/**
 * Add alarm message to dingding job queue.
 * 
 * @author smartloli.
 *
 *         Created by Oct 27, 2019
 */
public class DingDingJob implements Job {

	@Override
	public void execute(JobExecutionContext jobContext) throws JobExecutionException {
		BaseJobContext bjc = (BaseJobContext) jobContext.getJobDetail().getJobDataMap().get(AlarmQueue.JOB_PARAMS);
		sendMsg(bjc.getData(), bjc.getUrl());
	}

	private int sendMsg(String data, String url) {
		try {
			Map<String, Object> dingDingMarkdownMessage = getDingDingMarkdownMessage(IM.TITLE, data, true);
			HttpClientUtils.doPostJson(url, JSONObject.toJSONString(dingDingMarkdownMessage));
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	/**
	 * create markdown format map, do not point @user, option @all.
	 * 
	 * @param title
	 * @param text
	 * @param isAtAll
	 */
	private static Map<String, Object> getDingDingMarkdownMessage(String title, String text, boolean isAtAll) {
		Map<String, Object> map = new HashMap<>();
		map.put("msgtype", "markdown");

		Map<String, Object> markdown = new HashMap<>();
		markdown.put("title", title);
		markdown.put("text", text);
		map.put("markdown", markdown);

		Map<String, Object> at = new HashMap<>();
		at.put("isAtAll", false);
		map.put("at", at);

		return map;
	}

}
