package org.kafka.eagle.core.api;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.kafka.eagle.dto.cluster.KafkaClientInfo;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process-wide AdminClient cache keyed by cluster. Clients stay open across
 * monitor rounds so bootstrap/handshake is not paid per topic.
 */
@Slf4j
public final class KafkaAdminClientPool {

    private static final KafkaAdminClientPool INSTANCE = new KafkaAdminClientPool();

    private final ConcurrentHashMap<String, AdminClient> clients = new ConcurrentHashMap<>();

    private KafkaAdminClientPool() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::closeAll, "efak-admin-pool-shutdown"));
    }

    public static KafkaAdminClientPool getInstance() {
        return INSTANCE;
    }

    public AdminClient get(KafkaClientInfo clientInfo, KafkaStoragePlugin plugin) {
        String key = cacheKey(clientInfo);
        AdminClient existing = clients.get(key);
        if (existing != null) {
            return existing;
        }
        Properties props = plugin.buildAdminClientProps(clientInfo);
        log.info("Creating pooled AdminClient for {}", key);
        AdminClient created = AdminClient.create(props);
        AdminClient raced = clients.putIfAbsent(key, created);
        if (raced != null) {
            try {
                created.close();
            } catch (Exception ignored) {
            }
            return raced;
        }
        return created;
    }

    public void invalidate(KafkaClientInfo clientInfo) {
        String key = cacheKey(clientInfo);
        AdminClient client = clients.remove(key);
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                log.warn("Failed to close AdminClient {}", key, e);
            }
        }
    }

    public void closeAll() {
        for (Map.Entry<String, AdminClient> entry : clients.entrySet()) {
            try {
                entry.getValue().close();
            } catch (Exception e) {
                log.warn("Failed to close AdminClient {}", entry.getKey(), e);
            }
        }
        clients.clear();
    }

    static String cacheKey(KafkaClientInfo clientInfo) {
        String cluster = clientInfo.getClusterId() != null ? clientInfo.getClusterId() : "default";
        String brokers = clientInfo.getBrokerServer() != null ? clientInfo.getBrokerServer() : "";
        return cluster + "|" + brokers;
    }
}
