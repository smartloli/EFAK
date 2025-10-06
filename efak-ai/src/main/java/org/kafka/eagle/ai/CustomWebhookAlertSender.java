/**
 * CustomWebhookAlertSender.java
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

/**
 * <p>
 * 自定义Webhook告警发送器，支持发送JSON和自定义格式的告警消息
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/13 21:55:10
 * @version 5.0.0
 */
public class CustomWebhookAlertSender {
    // Webhook的URL
    private final String webhookUrl;
    // 请求头
    private final Map<String, String> headers;
    // 连接超时时间（毫秒）
    private int connectTimeout = 5000;
    // 读取超时时间（毫秒）
    private int readTimeout = 10000;

    /**
     * 构造函数，使用默认的Content-Type为application/json
     *
     * @param webhookUrl Webhook的URL
     */
    public CustomWebhookAlertSender(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.headers = new HashMap<>();
        this.headers.put("Content-Type", "application/json");
    }

    /**
     * 构造函数，允许自定义请求头
     *
     * @param webhookUrl Webhook的URL
     * @param headers    请求头
     */
    public CustomWebhookAlertSender(String webhookUrl, Map<String, String> headers) {
        this.webhookUrl = webhookUrl;
        this.headers = headers != null ? new HashMap<>(headers) : new HashMap<>();
        // 如果用户没有设置Content-Type，设置默认值
        if (!this.headers.containsKey("Content-Type")) {
            this.headers.put("Content-Type", "application/json");
        }
    }

    /**
     * 设置连接超时时间
     *
     * @param connectTimeout 连接超时时间（毫秒）
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * 设置读取超时时间
     *
     * @param readTimeout 读取超时时间（毫秒）
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * 发送JSON格式的告警消息
     *
     * @param message 消息内容（Map将被转换为JSON）
     * @return 响应结果
     * @throws IOException 网络异常
     */
    public String sendAlert(Map<String, Object> message) throws IOException {
        return sendRequest(JSON.toJSONString(message));
    }

    /**
     * 发送自定义格式的告警消息
     *
     * @param payload 请求体内容
     * @return 响应结果
     * @throws IOException 网络异常
     */
    public String sendAlert(String payload) throws IOException {
        return sendRequest(payload);
    }

    /**
     * 发送HTTP请求
     *
     * @param payload 请求体内容
     * @return 响应结果
     * @throws IOException 网络异常
     */
    private String sendRequest(String payload) throws IOException {
        URL obj = new URL(webhookUrl);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // 设置请求方法
        con.setRequestMethod("POST");

        // 设置超时时间
        con.setConnectTimeout(connectTimeout);
        con.setReadTimeout(readTimeout);

        // 设置请求头
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            con.setRequestProperty(entry.getKey(), entry.getValue());
        }

        // 允许输出
        con.setDoOutput(true);

        // 发送请求体
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
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
