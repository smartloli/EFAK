/**
 * FeishuAlertSender.java
 * <p>
 * Copyright 2025 smartloli
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kafka.eagle.ai;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * 飞书机器人告警发送器，支持发送文本和富文本格式的告警消息
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/13 21:52:12
 * @version 5.0.0
 */
@Slf4j
public class FeishuAlertSender {
    // 飞书机器人的Webhook地址
    private final String webhookUrl;

    public FeishuAlertSender(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    /**
     * 发送文本类型的告警消息
     * @param content 消息内容
     * @param atMobiles 需要@的手机号列表
     * @return 发送结果
     * @throws IOException 网络异常
     */
    public String sendTextAlert(String content, String... atMobiles) throws IOException {
        Map<String, Object> message = new HashMap<>();
        message.put("msg_type", "text");

        Map<String, Object> contentMap = new HashMap<>();
        StringBuilder textBuilder = new StringBuilder(content);

        if (atMobiles != null && atMobiles.length > 0) {
            textBuilder.append("\n");
            for (String mobile : atMobiles) {
                textBuilder.append("<at mobile=\"").append(mobile).append("\"></at>");
            }
        }
        contentMap.put("text", textBuilder.toString());
        message.put("content", contentMap);

        return sendRequest(message);
    }

    /**
     * 发送富文本类型的告警消息
     * @param title 消息标题
     * @param content 消息内容
     * @param atMobiles 需要@的手机号列表
     * @return 发送结果
     * @throws IOException 网络异常
     */
    public String sendPostAlert(String title, String content, String... atMobiles) throws IOException {
        Map<String, Object> message = new HashMap<>();
        message.put("msg_type", "post");

        Map<String, Object> contentMap = new HashMap<>();
        Map<String, Object> post = new HashMap<>();

        // 中文内容
        Map<String, Object> zhCn = new HashMap<>();

        // 标题
        zhCn.put("title", title);

        // 内容
        Map<String, Object> text = new HashMap<>();
        text.put("tag", "text");
        text.put("text", content);

        // @人
        if (atMobiles != null && atMobiles.length > 0) {
            StringBuilder atText = new StringBuilder();
            for (String mobile : atMobiles) {
                atText.append("<at mobile=\"").append(mobile).append("\"></at> ");
            }
            Map<String, Object> at = new HashMap<>();
            at.put("tag", "text");
            at.put("text", atText.toString());
        }

        zhCn.put("content", new Object[][]{{text}});
        post.put("zh_cn", zhCn);
        contentMap.put("post", post);
        message.put("content", contentMap);

        return sendRequest(message);
    }

    /**
     * 发送请求到飞书API
     * @param message 消息内容
     * @return 响应结果
     * @throws IOException 网络异常
     */
    private String sendRequest(Map<String, Object> message) throws IOException {
        URL obj = new URL(webhookUrl);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // 设置请求头
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        // 发送请求体
        String jsonPayload = JSON.toJSONString(message);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // 读取响应
        int responseCode = con.getResponseCode();
        StringBuilder response = new StringBuilder();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        } catch (IOException e) {
            // 处理错误响应
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
        }

        return "HTTP响应码: " + responseCode + "\n响应内容: " + response.toString();
    }

}
