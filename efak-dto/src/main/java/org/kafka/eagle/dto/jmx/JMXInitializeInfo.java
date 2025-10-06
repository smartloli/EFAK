/**
 * JMXInitializeInfo.java
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
package org.kafka.eagle.dto.jmx;

import lombok.Data;

import javax.management.remote.JMXServiceURL;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Kafka Broker JMX 连接配置信息类，用于封装建立 JMX 连接到 Kafka 集群所需的所有属性。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/8/23 09:05:23
 * @version 5.0.0
 */
@Data
public class JMXInitializeInfo {

    /**
     * Kafka 集群标识符
     */
    private String clusterId;

    /**
     * Kafka Broker 主机地址
     */
    private String host;

    /**
     * Kafka Broker JMX 端口
     */
    private int port;

    /**
     * Kafka Broker 标识符
     */
    private String brokerId;

    /**
     * JMX 连接服务 URL
     */
    private JMXServiceURL url;

    /**
     * JMX 连接统一资源标识符格式
     */
    private String uri = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";

    /**
     * 连接超时时间（秒）
     */
    private Long timeout = 30L;

    /**
     * 超时配置的时间单位
     */
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    /**
     * 启用访问控制认证
     */
    private boolean acl = false;

    /**
     * JMX 认证用户名
     */
    private String jmxUser;

    /**
     * JMX 认证密码
     */
    private String jmxPass;

    /**
     * 启用 JMX 连接的 SSL 加密
     */
    private boolean ssl = false;

    /**
     * SSL 密钥存储文件路径
     */
    private String keyStorePath;

    /**
     * SSL 密钥存储密码
     */
    private String keyStorePassword;

    /**
     * 用于指标检索的 JMX 对象名称
     */
    private String objectName;
}
