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

import org.smartloli.kafka.eagle.common.util.KConstants.WeChat;

import java.util.HashMap;
import java.util.Map;

/**
 * Add alarm message to wechat job queue.
 *
 * @author smartloli.
 * <p>
 * Created by Oct 27, 2019
 */
public class WeChatJob extends AbstractJob {


    private static Map<String, Object> getWeChatMarkdownMessage(String text) {
        Map<String, Object> map = new HashMap<>();
        map.put("msgtype", "markdown");

        Map<String, Object> markdown = new HashMap<>();
        markdown.put("content", text);
        map.put("markdown", markdown);

        map.put("touser", WeChat.TOUSER);
        map.put("toparty", WeChat.TOPARTY);
        map.put("totag", WeChat.TOTAG);
        map.put("agentid", WeChat.AGENTID);

        return map;
    }

    @Override
    protected Map<String, Object> parseSendMessage(String data, String url) {
        return getWeChatMarkdownMessage(data);
    }
}
