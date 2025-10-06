/**
 * JmxConnectionManager.java
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
package org.kafka.eagle.core.api;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.jmx.JMXInitializeInfo;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.naming.Context;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * <p>
 * 用于建立支持可选ACL和SSL的JMX连接的工具类，提供超时机制以防止连接尝试期间的无限期阻塞。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/8/23 21:33:02
 * @version 5.0.0
 */
@Slf4j
public class JmxConnectionManager {

    private static final ThreadFactory DAEMON_THREAD_FACTORY = new DaemonThreadFactory();

    private JmxConnectionManager() {
        // 工具类，防止实例化
    }

    /**
     * 建立带有超时机制的JMX连接。
     *
     * @param initializeInfo JMX初始化配置
     * @return JMXConnector 如果成功则返回，否则返回null
     */
    public static JMXConnector connectWithTimeout(JMXInitializeInfo initializeInfo) {
        final BlockingQueue<Object> blockQueue = new ArrayBlockingQueue<>(1);
        ExecutorService executor = Executors.newSingleThreadExecutor(DAEMON_THREAD_FACTORY);

        executor.submit(() -> {
            try {
                JMXConnector connector;
                if (initializeInfo.isAcl()) {
                    Map<String, Object> envs = new HashMap<>();
                    String[] credentials = {initializeInfo.getJmxUser(), initializeInfo.getJmxPass()};
                    envs.put(JMXConnector.CREDENTIALS, credentials);

                    if (initializeInfo.isSsl()) {
                        envs.put(Context.SECURITY_PROTOCOL, "ssl");
                        envs.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, new SslRMIClientSocketFactory());

                        TrustManager[] tms = getTrustManagers(
                                initializeInfo.getKeyStorePath(),
                                initializeInfo.getKeyStorePassword()
                        );
                        SSLContext sslContext = SSLContext.getInstance("TLS");
                        sslContext.init(null, tms, null);
                        SSLContext.setDefault(sslContext);
                        envs.put("com.sun.jndi.rmi.factory.socket", new SslRMIClientSocketFactory());
                    }
                    connector = JMXConnectorFactory.connect(initializeInfo.getUrl(), envs);
                } else {
                    connector = JMXConnectorFactory.connect(initializeInfo.getUrl());
                }

                if (!blockQueue.offer(connector)) {
                    connector.close();
                }
            } catch (Exception e) {
                if (!blockQueue.offer(e)) {
                    log.error("JMX阻塞队列已满, 错误: {}", e.getMessage(), e);
                }
            }
        });

        Object result = null;
        try {
            result = blockQueue.poll(initializeInfo.getTimeout(), initializeInfo.getTimeUnit());
            if (result == null) {
                result = blockQueue.take();
            }
        } catch (Exception e) {
            log.error("从队列中检索JMX连接器时出错: {}", e.getMessage(), e);
        } finally {
            executor.shutdown();
        }

        return (result instanceof JMXConnector) ? (JMXConnector) result : null;
    }

    /**
     * 从提供的密钥库加载信任管理器。
     *
     * @param location 密钥库路径
     * @param password 密钥库密码
     * @return TrustManagers数组
     */
    private static TrustManager[] getTrustManagers(String location, String password)
            throws IOException, GeneralSecurityException {
        String algorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(algorithm);

        try (FileInputStream fis = new FileInputStream(location)) {
            KeyStore keyStore = KeyStore.getInstance("jks");
            keyStore.load(fis, password.toCharArray());
            tmFactory.init(keyStore);
        }

        return tmFactory.getTrustManagers();
    }

    /**
     * 为执行器服务创建守护线程。
     */
    private static class DaemonThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        }
    }
}