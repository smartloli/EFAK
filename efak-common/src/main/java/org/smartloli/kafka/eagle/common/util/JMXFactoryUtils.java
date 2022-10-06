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
package org.smartloli.kafka.eagle.common.util;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
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
 * Manager jmx connector object && release.
 *
 * @author smartloli.
 * <p>
 * Created by Feb 25, 2019
 * <p>
 * Update by smartloli Sep 12, 2021
 * Settings prefixed with 'kafka.eagle.' will be deprecated, use 'efak.' instead.
 */
public class JMXFactoryUtils {

    private static final ThreadFactory daemonThreadFactory = new DaemonThreadFactory();

    private JMXFactoryUtils() {

    }

    public static JMXConnector connectWithTimeout(String clusterAlias, final JMXServiceURL url, long timeout, TimeUnit unit) {
        final BlockingQueue<Object> blockQueue = new ArrayBlockingQueue<>(1);
        ExecutorService executor = Executors.newSingleThreadExecutor(daemonThreadFactory);
        executor.submit(new Runnable() {
            public void run() {
                try {
                    JMXConnector connector = null;
                    if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".efak.jmx.acl")) {
                        Map<String, Object> envs = new HashMap<>();
                        String user = SystemConfigUtils.getProperty(clusterAlias + ".efak.jmx.user");
                        String passwd = SystemConfigUtils.getProperty(clusterAlias + ".efak.jmx.password");
                        String[] credentials = new String[]{user, passwd};
                        envs.put(JMXConnector.CREDENTIALS, credentials);
                        if (SystemConfigUtils.getBooleanProperty(clusterAlias + ".efak.jmx.ssl")) {
                            envs.put(Context.SECURITY_PROTOCOL, "ssl");
                            envs.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, new SslRMIClientSocketFactory());
                            String truststoreLocation = SystemConfigUtils.getProperty(clusterAlias + ".efak.jmx.truststore.location");
                            String truststorePassword = SystemConfigUtils.getProperty(clusterAlias + ".efak.jmx.truststore.password");
                            TrustManager[] tms = getTrustManagers(truststoreLocation, truststorePassword);
                            SSLContext sslContext = SSLContext.getInstance("TLS");
                            sslContext.init(null, tms, null);
                            SSLContext.setDefault(sslContext);
                            envs.put("com.sun.jndi.rmi.factory.socket", new SslRMIClientSocketFactory());
                        }
                        connector = JMXConnectorFactory.connect(url, envs);
                    } else {
                        connector = JMXConnectorFactory.connect(url);
                    }
                    if (!blockQueue.offer(connector))
                        connector.close();
                } catch (Exception e) {
                    if (!blockQueue.offer(e)) {
                        LoggerUtils.print(JMXFactoryUtils.class).error("Block queue is full, error msg is ", e);
                    }
                }
            }
        });
        Object result = null;
        try {
            result = blockQueue.poll(timeout, unit);
            if(result instanceof Exception){
                LoggerUtils.print(JMXFactoryUtils.class).error("query jmx error: ", (Exception)result);
                throw (Exception)result;
            }
            if (result == null && !blockQueue.offer("")) {
                result = blockQueue.take();
            }
        } catch (Exception e) {
            LoggerUtils.print(JMXFactoryUtils.class).error("Take block queue has error, msg is ", e);
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
