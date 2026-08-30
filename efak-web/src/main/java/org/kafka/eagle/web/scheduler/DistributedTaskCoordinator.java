package org.kafka.eagle.web.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.core.util.NetUtils;
import org.kafka.eagle.web.config.DistributedTaskConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Coordinates EFAK nodes for sharded monitor tasks.
 *
 * Node identity is stable {@code ip:port} (or {@code efak.node-id}). Every online
 * node executes the same scheduled task but only processes its own shard.
 */
@Slf4j
@Component
public class DistributedTaskCoordinator {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private DistributedTaskConfig taskConfig;

    @Autowired
    private org.kafka.eagle.web.config.EfakRuntimeProperties runtimeProperties;

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${efak.node-id:}")
    private String configuredNodeId;

    private static final String SERVICE_REGISTRY_KEY = "efak:services:registry";
    private static final String SERVICE_HEARTBEAT_KEY = "efak:services:heartbeat:";
    private static final String TASK_LOCK_PREFIX = "efak:task:lock:";
    private static final String SHARD_RESULT_PREFIX = "efak:task:shard:result:";
    private static final String TASK_DONE_PREFIX = "efak:task:done:";
    private static final String TASK_STATS_PREFIX = "efak:task:stats:";
    private static final String TASK_ROUND_WINDOW_PREFIX = "efak:task:window:";

    private volatile String currentNodeId;

    public void initializeNode() {
        this.currentNodeId = generateNodeId();
        registerService();
        log.info("Distributed node initialized: {}", currentNodeId);
    }

    /**
     * Stable identity: configured id, otherwise {@code ip:port}.
     */
    private String generateNodeId() {
        if (configuredNodeId != null && !configuredNodeId.isBlank()) {
            return configuredNodeId.trim();
        }
        String host = NetUtils.getLocalAddress();
        if (host == null || host.isBlank()) {
            host = "unknown";
        }
        return host + ":" + serverPort;
    }

    public void registerService() {
        try {
            ensureNodeId();
            Map<String, Object> serviceInfo = new HashMap<>();
            serviceInfo.put("nodeId", currentNodeId);
            serviceInfo.put("hostname", NetUtils.getLocalAddress());
            serviceInfo.put("port", serverPort);
            serviceInfo.put("pid", ProcessHandle.current().pid());
            serviceInfo.put("role", runtimeProperties.normalizedRole());
            serviceInfo.put("startTime", LocalDateTime.now().toString());
            serviceInfo.put("lastHeartbeat", LocalDateTime.now().toString());
            redisTemplate.opsForHash().put(SERVICE_REGISTRY_KEY, currentNodeId, serviceInfo);
            updateHeartbeat();
        } catch (Exception e) {
            log.error("Service registration failed", e);
        }
    }

    public void updateHeartbeat() {
        try {
            ensureNodeId();
            String heartbeatKey = SERVICE_HEARTBEAT_KEY + currentNodeId;
            long timeoutSeconds = Math.max(taskConfig.getOfflineTimeout(), 30) + 30;
            redisTemplate.opsForValue().set(heartbeatKey, LocalDateTime.now().toString(),
                    timeoutSeconds, TimeUnit.SECONDS);

            Object raw = redisTemplate.opsForHash().get(SERVICE_REGISTRY_KEY, currentNodeId);
            if (raw instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> serviceInfo = (Map<String, Object>) map;
                serviceInfo.put("lastHeartbeat", LocalDateTime.now().toString());
                redisTemplate.opsForHash().put(SERVICE_REGISTRY_KEY, currentNodeId, serviceInfo);
            } else {
                registerService();
            }
        } catch (Exception e) {
            log.error("Heartbeat update failed", e);
        }
    }

    /**
     * Online nodes, sorted so every process computes the same shard layout.
     */
    public List<String> getOnlineServices() {
        return listOnlineServices(false);
    }

    /**
     * Nodes that actually collect shards. Web-only processes stay in the
     * registry for visibility but must not own topic/broker assignments.
     */
    public List<String> getOnlineWorkerServices() {
        List<String> workers = listOnlineServices(true);
        if (!workers.isEmpty()) {
            return workers;
        }
        ensureNodeId();
        return runtimeProperties.isWorker() ? List.of(currentNodeId) : Collections.emptyList();
    }

    private List<String> listOnlineServices(boolean workersOnly) {
        try {
            Set<Object> allServices = redisTemplate.opsForHash().keys(SERVICE_REGISTRY_KEY);
            List<String> onlineServices = new ArrayList<>();
            if (allServices == null || allServices.isEmpty()) {
                ensureNodeId();
                if (currentNodeId == null) {
                    return Collections.emptyList();
                }
                return !workersOnly || runtimeProperties.isWorker() ? List.of(currentNodeId) : Collections.emptyList();
            }
            for (Object serviceKey : allServices) {
                String nodeId = serviceKey.toString();
                String heartbeatKey = SERVICE_HEARTBEAT_KEY + nodeId;
                if (!Boolean.TRUE.equals(redisTemplate.hasKey(heartbeatKey))) {
                    continue;
                }
                if (workersOnly && !isWorkerRole(nodeId)) {
                    continue;
                }
                onlineServices.add(nodeId);
            }
            Collections.sort(onlineServices);
            return onlineServices;
        } catch (Exception e) {
            log.error("Failed to list online services", e);
            ensureNodeId();
            if (currentNodeId == null) {
                return Collections.emptyList();
            }
            return !workersOnly || runtimeProperties.isWorker() ? List.of(currentNodeId) : Collections.emptyList();
        }
    }

    private boolean isWorkerRole(String nodeId) {
        Object raw = redisTemplate.opsForHash().get(SERVICE_REGISTRY_KEY, nodeId);
        if (!(raw instanceof Map<?, ?> stored)) {
            return true;
        }
        Object role = stored.get("role");
        if (role == null) {
            return true;
        }
        String value = role.toString().trim();
        return value.isEmpty() || "worker".equalsIgnoreCase(value) || "all".equalsIgnoreCase(value);
    }

    public int getUniqueOnlineServiceCount() {
        return getOnlineServices().size();
    }

    public List<Map<String, Object>> getServiceDetails() {
        List<Map<String, Object>> serviceDetails = new ArrayList<>();
        try {
            Set<Object> allServices = redisTemplate.opsForHash().keys(SERVICE_REGISTRY_KEY);
            if (allServices == null) {
                return serviceDetails;
            }
            for (Object serviceKey : allServices) {
                String nodeId = serviceKey.toString();
                String heartbeatKey = SERVICE_HEARTBEAT_KEY + nodeId;
                if (!Boolean.TRUE.equals(redisTemplate.hasKey(heartbeatKey))) {
                    continue;
                }
                Map<String, Object> serviceInfo = new HashMap<>();
                Object raw = redisTemplate.opsForHash().get(SERVICE_REGISTRY_KEY, nodeId);
                if (raw instanceof Map<?, ?> stored) {
                    Object hostname = stored.get("hostname");
                    Object port = stored.get("port");
                    Object pid = stored.get("pid");
                    serviceInfo.put("hostname", hostname);
                    serviceInfo.put("port", port);
                    serviceInfo.put("pid", pid != null ? String.valueOf(pid) : "-");
                    serviceInfo.put("role", stored.get("role") != null ? stored.get("role").toString() : "worker");
                    serviceInfo.put("ipAddress", hostname != null ? hostname.toString() : parseHost(nodeId));
                } else {
                    serviceInfo.put("ipAddress", parseHost(nodeId));
                    serviceInfo.put("port", parsePort(nodeId));
                    serviceInfo.put("pid", "-");
                    serviceInfo.put("role", "worker");
                }
                serviceInfo.put("nodeId", nodeId);
                serviceInfo.put("status", "ONLINE");
                Object heartbeatTime = redisTemplate.opsForValue().get(heartbeatKey);
                serviceInfo.put("lastHeartbeat", heartbeatTime != null ? heartbeatTime.toString() : "unknown");
                serviceDetails.add(serviceInfo);
            }
        } catch (Exception e) {
            log.error("Failed to load service details", e);
        }
        return serviceDetails;
    }

    public void cleanupOfflineServices() {
        try {
            Set<Object> allServices = redisTemplate.opsForHash().keys(SERVICE_REGISTRY_KEY);
            if (allServices == null) {
                return;
            }
            for (Object serviceKey : allServices) {
                String nodeId = serviceKey.toString();
                String heartbeatKey = SERVICE_HEARTBEAT_KEY + nodeId;
                if (!Boolean.TRUE.equals(redisTemplate.hasKey(heartbeatKey))) {
                    redisTemplate.opsForHash().delete(SERVICE_REGISTRY_KEY, nodeId);
                    log.info("Removed offline node from registry: {}", nodeId);
                }
            }
        } catch (Exception e) {
            log.error("Offline service cleanup failed", e);
        }
    }

    public List<Integer> shardBrokers(List<Integer> brokerIds) {
        return shardItems(brokerIds);
    }

    public List<String> shardTopics(List<String> topicNames) {
        return shardItems(topicNames);
    }

    public List<String> shardConsumerGroups(List<String> consumerGroups) {
        return shardItems(consumerGroups);
    }

    public List<Long> shardAlertConfigs(List<Long> alertConfigIds) {
        return shardItems(alertConfigIds);
    }

    public List<String> shardTables(List<String> tableNames) {
        return shardItems(tableNames);
    }

    /**
     * Assign items with a consistent hash ring so membership changes only
     * move about 1/N of the keys.
     */
    <T> List<T> shardItems(List<T> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        ensureNodeId();
        List<String> onlineServices = getOnlineWorkerServices();
        if (onlineServices.isEmpty()) {
            log.warn("No online nodes; skipping shard assignment on {}", currentNodeId);
            return Collections.emptyList();
        }
        if (onlineServices.size() == 1) {
            return items;
        }
        if (!onlineServices.contains(currentNodeId)) {
            log.warn("Current node {} is not in online list {}; skipping this round",
                    currentNodeId, onlineServices);
            return Collections.emptyList();
        }
        List<T> assigned = new ArrayList<>();
        for (T item : items) {
            String owner = ConsistentHashRing.owner(item, onlineServices);
            if (currentNodeId.equals(owner)) {
                assigned.add(item);
            }
        }
        return assigned;
    }

    public String getCurrentNodeId() {
        ensureNodeId();
        return currentNodeId;
    }

    /**
     * Node-scoped lock so the same process cannot overlap a task, while other
     * nodes can run their shards in parallel.
     */
    public boolean acquireTaskLock(String taskType, long lockTimeout) {
        ensureNodeId();
        return tryLock(TASK_LOCK_PREFIX + taskType + ":" + currentNodeId, currentNodeId, lockTimeout);
    }

    public void releaseTaskLock(String taskType) {
        ensureNodeId();
        unlock(TASK_LOCK_PREFIX + taskType + ":" + currentNodeId, currentNodeId);
    }

    public void renewTaskLock(String taskType, long lockTimeout) {
        ensureNodeId();
        renewLock(TASK_LOCK_PREFIX + taskType + ":" + currentNodeId, currentNodeId, lockTimeout);
    }

    public boolean tryLock(String lockKey, String owner, long timeoutSeconds) {
        try {
            byte[] key = lockKey.getBytes(StandardCharsets.UTF_8);
            byte[] val = owner.getBytes(StandardCharsets.UTF_8);
            Boolean acquired = redisTemplate.execute((RedisCallback<Boolean>) connection ->
                    connection.stringCommands().set(key, val,
                            Expiration.seconds(Math.max(timeoutSeconds, 1)),
                            RedisStringCommands.SetOption.SET_IF_ABSENT));
            return Boolean.TRUE.equals(acquired);
        } catch (Exception e) {
            log.error("Failed to acquire lock {}", lockKey, e);
            return false;
        }
    }

    public void renewLock(String lockKey, String owner, long timeoutSeconds) {
        try {
            byte[] key = lockKey.getBytes(StandardCharsets.UTF_8);
            byte[] val = owner.getBytes(StandardCharsets.UTF_8);
            redisTemplate.execute((RedisCallback<Boolean>) connection -> {
                byte[] current = connection.stringCommands().get(key);
                if (current != null && java.util.Arrays.equals(current, val)) {
                    connection.keyCommands().expire(key, Math.max(timeoutSeconds, 1));
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            });
        } catch (Exception e) {
            log.error("Failed to renew lock {}", lockKey, e);
        }
    }

    public void unlock(String lockKey, String owner) {
        try {
            byte[] key = lockKey.getBytes(StandardCharsets.UTF_8);
            byte[] val = owner.getBytes(StandardCharsets.UTF_8);
            redisTemplate.execute((RedisCallback<Long>) connection -> {
                byte[] current = connection.stringCommands().get(key);
                if (current != null && java.util.Arrays.equals(current, val)) {
                    connection.keyCommands().del(key);
                    return 1L;
                }
                return 0L;
            });
        } catch (Exception e) {
            log.error("Failed to release lock {}", lockKey, e);
        }
    }

    public void saveShardResult(String taskType, Map<String, Object> shardResult) {
        try {
            ensureNodeId();
            String resultKey = SHARD_RESULT_PREFIX + taskType;
            redisTemplate.opsForHash().put(resultKey, currentNodeId, shardResult);
            long expireSeconds = Math.max(taskConfig.getShardResultExpireMinutes(), 1) * 60L;
            redisTemplate.expire(resultKey, expireSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Failed to save shard result: taskType={}", taskType, e);
        }
    }

    public Map<String, Object> getAllShardResults(String taskType) {
        try {
            String resultKey = SHARD_RESULT_PREFIX + taskType;
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(resultKey);
            Map<String, Object> allResults = new HashMap<>();
            if (entries != null) {
                for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        allResults.put(entry.getKey().toString(), entry.getValue());
                    }
                }
            }
            return allResults;
        } catch (Exception e) {
            log.error("Failed to read shard results: taskType={}", taskType, e);
            return Collections.emptyMap();
        }
    }

    public void clearShardResults(String taskType) {
        try {
            redisTemplate.delete(SHARD_RESULT_PREFIX + taskType);
        } catch (Exception e) {
            log.error("Failed to clear shard results: taskType={}", taskType, e);
        }
    }

    /**
     * Marks that this node already processed the given schedule round.
     */
    public boolean markRoundDone(Long taskId, String roundId) {
        try {
            ensureNodeId();
            String key = TASK_DONE_PREFIX + taskId + ":" + currentNodeId + ":" + roundId;
            return tryLock(key, currentNodeId, TimeUnit.HOURS.toSeconds(25));
        } catch (Exception e) {
            log.error("Failed to mark round done: taskId={}", taskId, e);
            return false;
        }
    }

    public boolean hasCompletedRound(Long taskId, String roundId) {
        try {
            ensureNodeId();
            String key = TASK_DONE_PREFIX + taskId + ":" + currentNodeId + ":" + roundId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Failed to check round completion: taskId={}", taskId, e);
            return false;
        }
    }

    /**
     * Only one node per round updates shared scheduler statistics.
     */
    /**
     * Opens a short join window so later workers can still process their
     * shards after the first node updates {@code last_execute_time}.
     */
    public void openRoundWindow(Long taskId, String roundId, long ttlSeconds) {
        try {
            String key = TASK_ROUND_WINDOW_PREFIX + taskId;
            redisTemplate.opsForValue().set(key, roundId, Math.max(ttlSeconds, 30), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Failed to open round window: taskId={}", taskId, e);
        }
    }

    public String getOpenRoundId(Long taskId) {
        try {
            Object value = redisTemplate.opsForValue().get(TASK_ROUND_WINDOW_PREFIX + taskId);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.error("Failed to read round window: taskId={}", taskId, e);
            return null;
        }
    }

    public boolean tryClaimStatsUpdate(Long taskId, String roundId) {
        try {
            ensureNodeId();
            String key = TASK_STATS_PREFIX + taskId + ":" + roundId;
            return tryLock(key, currentNodeId, TimeUnit.HOURS.toSeconds(25));
        } catch (Exception e) {
            log.error("Failed to claim stats update: taskId={}", taskId, e);
            return false;
        }
    }

    private void ensureNodeId() {
        if (currentNodeId == null || currentNodeId.isBlank()) {
            currentNodeId = generateNodeId();
        }
    }

    private String parseHost(String nodeId) {
        if (nodeId == null) {
            return "-";
        }
        int idx = nodeId.lastIndexOf(':');
        if (idx > 0) {
            return nodeId.substring(0, idx);
        }
        return nodeId;
    }

    private String parsePort(String nodeId) {
        if (nodeId == null) {
            return "-";
        }
        int idx = nodeId.lastIndexOf(':');
        if (idx > 0 && idx < nodeId.length() - 1) {
            return nodeId.substring(idx + 1);
        }
        return String.valueOf(serverPort);
    }
}
