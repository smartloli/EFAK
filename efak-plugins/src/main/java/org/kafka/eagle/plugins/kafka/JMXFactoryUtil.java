/**
 * JMXFactoryUtil.java
 * <p>
 * Copyright 2023 smartloli
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
package org.kafka.eagle.plugins.kafka;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.pojo.kafka.JMXInitializeInfo;

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
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/6/7 14:04
 * @Version: 3.4.0
 */
@Slf4j
public class JMXFactoryUtil {
    private static final ThreadFactory daemonThreadFactory = new DaemonThreadFactory();

    private JMXFactoryUtil() {

    }

    public static JMXConnector connectWithTimeout(JMXInitializeInfo initializeInfo) {
        final BlockingQueue<Object> blockQueue = new ArrayBlockingQueue<>(1);
        ExecutorService executor = Executors.newSingleThreadExecutor(daemonThreadFactory);
        executor.submit(new Runnable() {
            public void run() {
                try {
                    JMXConnector connector = null;
                    if (initializeInfo.isAcl()) {
                        Map<String, Object> envs = new HashMap<>();
                        String[] credentials = new String[]{initializeInfo.getJmxUser(), initializeInfo.getJmxPass()};
                        envs.put(JMXConnector.CREDENTIALS, credentials);
                        if (initializeInfo.isSsl()) {
                            envs.put(Context.SECURITY_PROTOCOL, "ssl");
                            envs.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, new SslRMIClientSocketFactory());
                            TrustManager[] tms = getTrustManagers(initializeInfo.getKeyStorePath(), initializeInfo.getKeyStorePassword());
                            SSLContext sslContext = SSLContext.getInstance("TLS");
                            sslContext.init(null, tms, null);
                            SSLContext.setDefault(sslContext);
                            envs.put("com.sun.jndi.rmi.factory.socket", new SslRMIClientSocketFactory());
                        }
                        connector = JMXConnectorFactory.connect(initializeInfo.getUrl(), envs);
                    } else {
                        connector = JMXConnectorFactory.connect(initializeInfo.getUrl());
                    }
                    if (!blockQueue.offer(connector))
                        connector.close();
                } catch (Exception e) {
                    if (!blockQueue.offer(e)) {
                        log.error("JMX block queue is full, error msg is {}", e);
                    }
                }
            }
        });
        Object result = null;
        try {
            result = blockQueue.poll(initializeInfo.getTimeout(), initializeInfo.getTimeUnit());
            if (result == null && !blockQueue.offer("")) {
                result = blockQueue.take();
            }
        } catch (Exception e) {
            log.error("JMX take block queue has error, msg is {}", e);
        } finally {
            executor.shutdown();
        }
        return (JMXConnector) result;
    }

    private static TrustManager[] getTrustManagers(String location, String password)
            throws IOException, GeneralSecurityException {
        String alg = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmFact = TrustManagerFactory.getInstance(alg);

        FileInputStream fis = new FileInputStream(location);
        KeyStore ks = KeyStore.getInstance("jks");
        ks.load(fis, password.toCharArray());
        fis.close();

        tmFact.init(ks);
        TrustManager[] tms = tmFact.getTrustManagers();
        return tms;
    }

    private static class DaemonThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        }
    }
}
