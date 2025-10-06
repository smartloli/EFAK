/**
 * DingTalkAlertSender.java
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
 * 钉钉机器人告警发送器，支持文本与Markdown，内置加签生成与HTTP请求发送，统一返回响应内容。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/10 23:44:11
 * @version 5.0.0
 */
public class DingTalkAlertSender {
    // 钉钉机器人的Webhook地址
    private final String webhookUrl;
    // 可选的加签密钥
    private final String secret;

    public DingTalkAlertSender(String webhookUrl) {
        this(webhookUrl, null);
    }

    public DingTalkAlertSender(String webhookUrl, String secret) {
        this.webhookUrl = webhookUrl;
        this.secret = secret;
    }

    /**
     * 发送文本类型的告警消息
     * @param content 消息内容
     * @param isAtAll 是否@所有人
     * @param mobileList 需要@的手机号列表
     * @return 发送结果
     * @throws IOException 网络异常
     */
    public String sendTextAlert(String content, boolean isAtAll, String... mobileList) throws IOException {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "text");

        Map<String, Object> text = new HashMap<>();
        text.put("content", content);
        message.put("text", text);

        Map<String, Object> at = new HashMap<>();
        at.put("isAtAll", isAtAll);
        if (mobileList != null && mobileList.length > 0) {
            at.put("atMobiles", mobileList);
        }
        message.put("at", at);

        return sendRequest(message);
    }

    /**
     * 发送Markdown类型的告警消息
     * @param title 消息标题
     * @param text 消息内容(Markdown格式)
     * @param isAtAll 是否@所有人
     * @param mobileList 需要@的手机号列表
     * @return 发送结果
     * @throws IOException 网络异常
     */
    public String sendMarkdownAlert(String title, String text, boolean isAtAll, String... mobileList) throws IOException {
        Map<String, Object> message = new HashMap<>();
        message.put("msgtype", "markdown");

        Map<String, Object> markdown = new HashMap<>();
        markdown.put("title", title);
        markdown.put("text", text);
        message.put("markdown", markdown);

        Map<String, Object> at = new HashMap<>();
        at.put("isAtAll", isAtAll);
        if (mobileList != null && mobileList.length > 0) {
            at.put("atMobiles", mobileList);
        }
        message.put("at", at);

        return sendRequest(message);
    }

    /**
     * 发送请求到钉钉API
     * @param message 消息内容
     * @return 响应结果
     * @throws IOException 网络异常
     */
    private String sendRequest(Map<String, Object> message) throws IOException {
        String url = webhookUrl;
        // 如果有签名，处理签名逻辑
        if (secret != null && !secret.isEmpty()) {
            long timestamp = System.currentTimeMillis();
            String sign = generateSign(timestamp, secret);
            url = webhookUrl + "&timestamp=" + timestamp + "&sign=" + sign;
        }

        URL obj = new URL(url);
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
        }

        return "HTTP响应码: " + responseCode + "\n响应内容: " + response.toString();
    }

    /**
     * 生成签名(安全加固)
     * @param timestamp 时间戳
     * @param secret 密钥
     * @return 签名结果
     */
    private String generateSign(long timestamp, String secret) {
        try {
            String stringToSign = timestamp + "\n" + secret;
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            return java.net.URLEncoder.encode(java.util.Base64.getEncoder().encodeToString(signData), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("生成签名失败", e);
        }
    }

}
