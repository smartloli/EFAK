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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.util.HttpClientUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.IM;
import org.smartloli.kafka.eagle.common.util.KConstants.WeChat;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;

import com.alibaba.fastjson.JSONObject;

/**
 * Implements IMService all method.
 * 
 * @author smartloli.
 *
 *         Created by Jan 1, 2019
 */
public class IMServiceImpl implements IMService {

	private final Logger LOG = LoggerFactory.getLogger(IMServiceImpl.class);
	private static ExecutorService service = Executors.newFixedThreadPool(10);

	/** Send Json msg by dingding. */
	@Override
	public void sendPostMsgByDingDing(final String data, final String url) {
		service.submit(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				Thread.sleep(200);
				return sendMsg(data, url);
			}
		});
		// release
		service.shutdown();
	}

	private int sendMsg(String data, String url) {
		try {
			Map<String, Object> dingDingMarkdownMessage = getDingDingMarkdownMessage(IM.TITLE, data, true);
			LOG.info("IM[DingDing] response: " + HttpClientUtils.doPostJson(url, JSONObject.toJSONString(dingDingMarkdownMessage)));
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	@Override
	public void sendGetMsgByDingDing(String data, String url) {

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

	/** Send Json msg by wechat. */
	@Override
	public void sendJsonMsgByWeChat(String data, String url) {
		Map<String, Object> wechatMarkdownMessage = getWeChatMarkdownMessage(data);
		LOG.info("IM[WeChat] response: " + HttpClientUtils.doPostJson(url, JSONObject.toJSONString(wechatMarkdownMessage)));
	}

	private static Map<String, Object> getWeChatMarkdownMessage(String text) {
		Map<String, Object> map = new HashMap<>();
		map.put("msgtype", "markdown");

		Map<String, Object> markdown = new HashMap<>();
		markdown.put("content", text);
		map.put("markdown", markdown);

		map.put("touser", SystemConfigUtils.getProperty("kafka.eagle.im.wechat.touser", WeChat.TOUSER));
		map.put("toparty", SystemConfigUtils.getProperty("kafka.eagle.im.wechat.toparty", WeChat.TOPARTY));
		map.put("totag", SystemConfigUtils.getProperty("kafka.eagle.im.wechat.totag", WeChat.TOTAG));
		map.put("agentid", SystemConfigUtils.getLongProperty("kafka.eagle.im.wechat.agentid", WeChat.AGENTID));

		return map;
	}

	@Override
	public void sendPostMsgByWebhook(String data, String url) {

	}

	@Override
	public void sendGetMsgByWebhook(String data, String url) {

	}

}
