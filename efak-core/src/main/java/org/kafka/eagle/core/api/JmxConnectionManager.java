package org.kafka.eagle.core.api;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.jmx.JMXInitializeInfo;

import javax.management.MBeanServerConnection;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Pooled JMX connectors with a shared timeout executor and per-broker circuit
 * breaking. Callers must not close the connector returned by
 * {@link #connectWithTimeout(JMXInitializeInfo)}.
 */
@Slf4j
public class JmxConnectionManager {

    private static final ExecutorService CONNECT_EXECUTOR = new ThreadPoolExecutor(
            2, 8, 60, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            daemonFactory("efak-jmx-connect"),
            new ThreadPoolExecutor.CallerRunsPolicy());

    private static final ConcurrentHashMap<String, JMXConnector> POOL = new ConcurrentHashMap<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(JmxConnectionManager::closeAll, "efak-jmx-pool-shutdown"));
    }

    private JmxConnectionManager() {
    }

    @FunctionalInterface
    public interface JmxCallback {
        void execute(MBeanServerConnection connection) throws Exception;
    }

    public static JMXConnector connectWithTimeout(JMXInitializeInfo initializeInfo) {
        if (initializeInfo == null) {
            return null;
        }
        if (JmxBrokerGuard.isOpen(initializeInfo)) {
            return null;
        }
        String key = JmxBrokerGuard.key(initializeInfo);
        JMXConnector cached = POOL.get(key);
        if (cached != null && isLive(cached)) {
            return cached;
        }
        if (cached != null) {
            invalidate(initializeInfo);
        }
        JMXConnector created = openWithTimeout(initializeInfo);
        if (created == null) {
            JmxBrokerGuard.fail(initializeInfo);
            return null;
        }
        JMXConnector previous = POOL.put(key, created);
        if (previous != null && previous != created) {
            silentClose(previous);
        }
        JmxBrokerGuard.success(initializeInfo);
        return created;
    }

    public static void execute(JMXInitializeInfo initializeInfo, JmxCallback callback) {
        JMXConnector connector = connectWithTimeout(initializeInfo);
        if (connector == null) {
            return;
        }
        try {
            callback.execute(connector.getMBeanServerConnection());
            JmxBrokerGuard.success(initializeInfo);
        } catch (Exception e) {
            log.debug("JMX operation failed on {}:{} - {}", initializeInfo.getHost(), initializeInfo.getPort(), e.getMessage());
            JmxBrokerGuard.fail(initializeInfo);
            invalidate(initializeInfo);
        }
    }

    public static void invalidate(JMXInitializeInfo initializeInfo) {
        JMXConnector removed = POOL.remove(JmxBrokerGuard.key(initializeInfo));
        silentClose(removed);
    }

    private static JMXConnector openWithTimeout(JMXInitializeInfo initializeInfo) {
        long timeout = initializeInfo.getTimeout() != null ? initializeInfo.getTimeout() : 5L;
        TimeUnit unit = initializeInfo.getTimeUnit() != null ? initializeInfo.getTimeUnit() : TimeUnit.SECONDS;
        if (timeout > 5 && unit == TimeUnit.SECONDS) {
            timeout = 5;
        }
        Future<JMXConnector> future = CONNECT_EXECUTOR.submit(() -> open(initializeInfo));
        try {
            return future.get(timeout, unit);
        } catch (Exception e) {
            future.cancel(true);
            log.debug("JMX connect timed out for {}:{}", initializeInfo.getHost(), initializeInfo.getPort());
            return null;
        }
    }

    private static JMXConnector open(JMXInitializeInfo initializeInfo) throws Exception {
        if (initializeInfo.getUrl() == null) {
            String endpoint = initializeInfo.getHost() + ":" + initializeInfo.getPort();
            String uri = initializeInfo.getUri() != null
                    ? initializeInfo.getUri()
                    : "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";
            initializeInfo.setUrl(new JMXServiceURL(String.format(uri, endpoint)));
        }
        if (initializeInfo.isAcl()) {
            Map<String, Object> envs = new HashMap<>();
            String[] credentials = {initializeInfo.getJmxUser(), initializeInfo.getJmxPass()};
            envs.put(JMXConnector.CREDENTIALS, credentials);
            if (initializeInfo.isSsl()) {
                envs.put(Context.SECURITY_PROTOCOL, "ssl");
                envs.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, new SslRMIClientSocketFactory());
                TrustManager[] tms = getTrustManagers(initializeInfo.getKeyStorePath(), initializeInfo.getKeyStorePassword());
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tms, null);
                envs.put("com.sun.jndi.rmi.factory.socket", new SslRMIClientSocketFactory());
            }
            return JMXConnectorFactory.connect(initializeInfo.getUrl(), envs);
        }
        return JMXConnectorFactory.connect(initializeInfo.getUrl());
    }

    private static boolean isLive(JMXConnector connector) {
        try {
            connector.getConnectionId();
            connector.getMBeanServerConnection();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void silentClose(JMXConnector connector) {
        if (connector == null) {
            return;
        }
        try {
            connector.close();
        } catch (Exception ignored) {
        }
    }

    private static void closeAll() {
        for (JMXConnector connector : POOL.values()) {
            silentClose(connector);
        }
        POOL.clear();
        CONNECT_EXECUTOR.shutdownNow();
    }

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

    private static ThreadFactory daemonFactory(String prefix) {
        return runnable -> {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setName(prefix + "-" + thread.getId());
            thread.setDaemon(true);
            return thread;
        };
    }
}
