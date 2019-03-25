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

	/** Send Json msg by dingding. */
	@Override
	public void sendJsonMsgByDingDing(String data) {
		if (SystemConfigUtils.getBooleanProperty("kafka.eagle.im.dingding.enable")) {
			String uri = SystemConfigUtils.getProperty("kafka.eagle.im.dingding.url");
			Map<String, Object> dingDingMarkdownMessage = getDingDingMarkdownMessage(IM.TITLE, data, true);
			LOG.info("IM[DingDing] response: " + HttpClientUtils.doPostJson(uri, JSONObject.toJSONString(dingDingMarkdownMessage)));
		}
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
	public void sendJsonMsgByWeChat(String data) {
		if (SystemConfigUtils.getBooleanProperty("kafka.eagle.im.wechat.enable")) {
			String uri = SystemConfigUtils.getProperty("kafka.eagle.im.wechat.url");
			Map<String, Object> wechatMarkdownMessage = getWeChatMarkdownMessage(data);
			LOG.info("IM[WeChat] response: " + HttpClientUtils.doPostJson(uri, JSONObject.toJSONString(wechatMarkdownMessage)));
		}
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

}
