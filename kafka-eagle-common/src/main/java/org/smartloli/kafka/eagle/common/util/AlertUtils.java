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
package org.smartloli.kafka.eagle.common.util;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.smartloli.kafka.eagle.common.util.KConstants.IM;
import org.smartloli.kafka.eagle.common.util.KConstants.WeChat;

import com.alibaba.fastjson.JSONObject;

/**
 * Send alert util.
 * 
 * @author smartloli.
 *
 *         Created by Oct 6, 2019
 */
public class AlertUtils {

	private static final String MARKDOWN = "markdown";

	private AlertUtils() {

	}

	/** Send Json msg by wechat. */
	public static String sendTestMsgByWeChat(String url, String data) {
		Map<String, Object> wechatMarkdownMessage = getWeChatMarkdownMessage(data);
		return HttpClientUtils.doPostJson(url, JSONObject.toJSONString(wechatMarkdownMessage));
	}

	private static Map<String, Object> getWeChatMarkdownMessage(String text) {
		Map<String, Object> map = new HashMap<>();
		map.put("msgtype", MARKDOWN);

		Map<String, Object> markdown = new HashMap<>();
		markdown.put("content", text);
		map.put(MARKDOWN, markdown);

		map.put("touser", WeChat.TOUSER);
		map.put("toparty", WeChat.TOPARTY);
		map.put("totag", WeChat.TOTAG);
		map.put("agentid", WeChat.AGENTID);

		return map;
	}

	/** Send Json msg by dingding. */
	public static String sendTestMsgByDingDing(String uri, String data) {
		Map<String, Object> dingDingMarkdownMessage = getDingDingMarkdownMessage(IM.TITLE, data, true);
		return HttpClientUtils.doPostJson(uri, JSONObject.toJSONString(dingDingMarkdownMessage));
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
		map.put("msgtype", MARKDOWN);

		Map<String, Object> markdown = new HashMap<>();
		markdown.put("title", title);
		markdown.put("text", text);
		map.put(MARKDOWN, markdown);

		Map<String, Object> at = new HashMap<>();
		at.put("isAtAll", isAtAll);
		map.put("at", at);

		return map;
	}

	public static String sendTestMsgByEmail(String url) {
		URI uri = URI.create(url);
		boolean status = NetUtils.telnet(uri.getHost(), uri.getPort());
		JSONObject object = new JSONObject();
		if (status) {
			object.put("errcode", 0);
		} else {
			object.put("errcode", 1);
		}
		return object.toJSONString();
	}

}
