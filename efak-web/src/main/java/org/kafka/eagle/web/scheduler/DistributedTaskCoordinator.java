package org.kafka.eagle.web.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.core.util.NetUtils;
import org.kafka.eagle.web.config.DistributedTaskConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * DistributedTaskCoordinator类
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/30 01:07:14
 * @version 5.0.0
 */
@Slf4j
@Component
public class DistributedTaskCoordinator {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DistributedTaskConfig taskConfig;

    @Value("${server.port:8080}")
    private int serverPort;

    // Redis键前缀
    private static final String SERVICE_REGISTRY_KEY = "efak:services:registry";
    private static final String SERVICE_HEARTBEAT_KEY = "efak:services:heartbeat:";
    private static final String TASK_SHARD_LOCK_KEY = "efak:task:shard:lock:";
    private static final String TASK_SHARD_RESULT_KEY = "efak:task:shard:result:";
    
    // 服务心跳超时时间（秒）
    private static final long HEARTBEAT_TIMEOUT = 180;
    
    // 当前节点ID
    private String currentNodeId;
    
    /**
     * 初始化当前节点
     */
    public void initializeNode() {
        this.currentNodeId = generateNodeId();
        registerService();
    }
    
    /**
     * 生成节点ID
     * 使用 IP+端口+进程信息，确保同一台机器上不同实例也能正确参与分片与统计
     */
    private String generateNodeId() {
        String hostName = NetUtils.getLocalAddress();
        String pid = String.valueOf(ProcessHandle.current().pid());
        // 关键逻辑：加入端口避免“同IP不同实例”被误判为同一个服务
        return hostName + "-" + serverPort + "-" + pid + "-" + System.currentTimeMillis();
    }
    
    /**
     * 获取服务的唯一标识（基于IP地址+端口）
     */
    private String getServiceUniqueId(String nodeId) {
        if (nodeId == null) {
            return null;
        }
        // 关键逻辑：兼容老格式 nodeId=ip-pid-ts，以及新格式 nodeId=ip-port-pid-ts
        String[] parts = nodeId.split("-");
        if (parts.length >= 4) {
            String ipAddress = parts[0];
            String port = parts[1];
            return ipAddress + ":" + port;
        }
        if (parts.length >= 2) {
            String ipAddress = parts[0];
            // 老节点ID不含端口时，只能退化为“当前端口”
            return ipAddress + ":" + serverPort;
        }
        return nodeId;
    }
    
    /**
     * 注册服务到Redis
     */
    public void registerService() {
        try {
            Map<String, Object> serviceInfo = new HashMap<>();
            serviceInfo.put("nodeId", currentNodeId);
            serviceInfo.put("hostname", NetUtils.getLocalAddress());
            serviceInfo.put("port", serverPort);
            serviceInfo.put("pid", ProcessHandle.current().pid());
            serviceInfo.put("startTime", LocalDateTime.now().toString());
            serviceInfo.put("lastHeartbeat", LocalDateTime.now().toString());
            
            // 注册到服务列表
            redisTemplate.opsForHash().put(SERVICE_REGISTRY_KEY, currentNodeId, serviceInfo);
            
            // 设置心跳
            updateHeartbeat();
            
        } catch (Exception e) {
            log.error("服务注册失败", e);
        }
    }
    
    /**
     * 更新心跳
     */
    public void updateHeartbeat() {
        try {
            String heartbeatKey = SERVICE_HEARTBEAT_KEY + currentNodeId;
            long timeoutSeconds = taskConfig.getOfflineTimeout() + 30; // 增加缓冲时间
            redisTemplate.opsForValue().set(heartbeatKey, LocalDateTime.now().toString(), 
                    timeoutSeconds, TimeUnit.SECONDS);
            
            // 同时更新注册表中的心跳时间
            redisTemplate.opsForHash().put(SERVICE_REGISTRY_KEY, currentNodeId + ":lastHeartbeat", 
                    LocalDateTime.now().toString());
        } catch (Exception e) {
            log.error("更新心跳失败", e);
        }
    }
    
    /**
     * 获取在线服务列表
     * 按IP地址去重，确保同一台服务器只被识别为一个在线服务
     */
    public List<String> getOnlineServices() {
        try {
            Set<Object> allServices = redisTemplate.opsForHash().keys(SERVICE_REGISTRY_KEY);
            Set<String> uniqueServices = new HashSet<>(); // 用于IP地址去重
            List<String> onlineServices = new ArrayList<>();
            
            for (Object serviceKey : allServices) {
                String nodeId = serviceKey.toString();
                if (nodeId.contains(":")) {
                    continue; // 跳过心跳时间等附加信息
                }
                
                String heartbeatKey = SERVICE_HEARTBEAT_KEY + nodeId;
                if (redisTemplate.hasKey(heartbeatKey)) {
                    // 获取服务的唯一标识（基于IP地址）
                    String serviceUniqueId = getServiceUniqueId(nodeId);
                    if (serviceUniqueId != null && !uniqueServices.contains(serviceUniqueId)) {
                        uniqueServices.add(serviceUniqueId);
                        onlineServices.add(nodeId); // 保留完整的nodeId用于后续处理
                    }
                }
            }

            return onlineServices;
        } catch (Exception e) {
            log.error("获取在线服务列表失败", e);
            return Collections.singletonList(currentNodeId); // 降级处理，返回当前节点
        }
    }
    
    /**
     * 获取唯一在线服务数量（按IP+端口去重）
     * 用于分片逻辑判断
     */
    public int getUniqueOnlineServiceCount() {
        try {
            Set<Object> allServices = redisTemplate.opsForHash().keys(SERVICE_REGISTRY_KEY);
            Set<String> uniqueServiceInstances = new HashSet<>();
            
            for (Object serviceKey : allServices) {
                String nodeId = serviceKey.toString();
                if (nodeId.contains(":")) {
                    continue; // 跳过心跳时间等附加信息
                }
                
                String heartbeatKey = SERVICE_HEARTBEAT_KEY + nodeId;
                if (redisTemplate.hasKey(heartbeatKey)) {
                    String serviceUniqueId = getServiceUniqueId(nodeId);
                    if (serviceUniqueId != null) {
                        uniqueServiceInstances.add(serviceUniqueId);
                    }
                }
            }
            
            int uniqueCount = uniqueServiceInstances.size();
            return uniqueCount;
        } catch (Exception e) {
            log.error("获取唯一在线服务数量失败", e);
            return 1; // 降级处理，返回1
        }
    }
    
    /**
     * 获取分布式服务节点详细信息
     */
    public List<Map<String, Object>> getServiceDetails() {
        List<Map<String, Object>> serviceDetails = new ArrayList<>();
        try {
            Set<Object> allServices = redisTemplate.opsForHash().keys(SERVICE_REGISTRY_KEY);
            
            for (Object serviceKey : allServices) {
                String nodeId = serviceKey.toString();
                if (nodeId.contains(":")) {
                    continue; // 跳过心跳时间等附加信息
                }
                
                String heartbeatKey = SERVICE_HEARTBEAT_KEY + nodeId;
                if (redisTemplate.hasKey(heartbeatKey)) {
                    Map<String, Object> serviceInfo = new HashMap<>();
                    
                    // 解析节点ID获取IP和端口
                    String[] parts = nodeId.split("-");
                    if (parts.length >= 4) {
                        String ipAddress = parts[0];
                        String port = parts[1];
                        String pid = parts[2];
                        
                        serviceInfo.put("nodeId", nodeId);
                        serviceInfo.put("ipAddress", ipAddress);
                        serviceInfo.put("port", port);
                        serviceInfo.put("pid", pid);
                        serviceInfo.put("status", "ONLINE");
                        
                        // 获取最后心跳时间
                        Object heartbeatTime = redisTemplate.opsForValue().get(heartbeatKey);
                        if (heartbeatTime != null) {
                            serviceInfo.put("lastHeartbeat", heartbeatTime.toString());
                        } else {
                            serviceInfo.put("lastHeartbeat", "未知");
                        }
                        
                        serviceDetails.add(serviceInfo);
                    } else if (parts.length >= 2) {
                        // 兼容老格式 nodeId=ip-pid-ts
                        String ipAddress = parts[0];
                        String pid = parts[1];
                        serviceInfo.put("nodeId", nodeId);
                        serviceInfo.put("ipAddress", ipAddress);
                        serviceInfo.put("port", String.valueOf(serverPort));
                        serviceInfo.put("pid", pid);
                        serviceInfo.put("status", "ONLINE");
                        Object heartbeatTime = redisTemplate.opsForValue().get(heartbeatKey);
                        serviceInfo.put("lastHeartbeat", heartbeatTime != null ? heartbeatTime.toString() : "未知");
                        serviceDetails.add(serviceInfo);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("获取分布式服务节点详细信息失败", e);
        }
        
        return serviceDetails;
    }
    
    /**
     * 清理离线服务
     * 清理没有心跳的服务节点
     */
    public void cleanupOfflineServices() {
        try {
            Set<Object> allServices = redisTemplate.opsForHash().keys(SERVICE_REGISTRY_KEY);
            
            for (Object serviceKey : allServices) {
                String nodeId = serviceKey.toString();
                if (nodeId.contains(":")) {
                    continue; // 跳过心跳时间等附加信息
                }
                
                String heartbeatKey = SERVICE_HEARTBEAT_KEY + nodeId;
                if (!redisTemplate.hasKey(heartbeatKey)) {
                    // 服务已离线，从注册表中移除
                    redisTemplate.opsForHash().delete(SERVICE_REGISTRY_KEY, nodeId);
                    String serviceUniqueId = getServiceUniqueId(nodeId);
                    log.info("清理离线服务: {} (IP: {})", nodeId, serviceUniqueId);
                }
            }
        } catch (Exception e) {
            log.error("清理离线服务失败", e);
        }
    }
    
    /**
     * 对broker列表进行分片
     * @param brokerIds 所有broker ID列表
     * @return 分配给当前节点的broker ID列表
     */
    public List<Integer> shardBrokers(List<Integer> brokerIds) {
        if (brokerIds == null || brokerIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<String> onlineServices = getOnlineServices();
        if (onlineServices.isEmpty()) {
            log.warn("没有在线服务，当前节点处理所有broker");
            return brokerIds;
        }
        
        // 使用唯一在线服务数量判断是否需要分片
        int uniqueServiceCount = getUniqueOnlineServiceCount();
        if (uniqueServiceCount == 1) {
            return brokerIds;
        }
        
        // 对服务列表排序，确保所有节点的分片结果一致
        Collections.sort(onlineServices);
        
        int currentNodeIndex = onlineServices.indexOf(currentNodeId);
        if (currentNodeIndex == -1) {
            log.warn("当前节点不在在线服务列表中，处理所有broker");
            return brokerIds;
        }
        
        // 计算分片
        List<Integer> assignedBrokers = new ArrayList<>();
        int serviceCount = onlineServices.size();
        
        for (int i = 0; i < brokerIds.size(); i++) {
            if (i % serviceCount == currentNodeIndex) {
                assignedBrokers.add(brokerIds.get(i));
            }
        }
        
        return assignedBrokers;
    }
    
    /**
     * 对主题列表进行分片
     * @param topicNames 所有主题名称列表
     * @return 分配给当前节点的主题名称列表
     */
    public List<String> shardTopics(List<String> topicNames) {
        if (topicNames == null || topicNames.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<String> onlineServices = getOnlineServices();
        if (onlineServices.isEmpty()) {
            log.warn("没有在线服务，当前节点处理所有主题");
            return topicNames;
        }
        
        // 使用唯一在线服务数量判断是否需要分片
        int uniqueServiceCount = getUniqueOnlineServiceCount();
        if (uniqueServiceCount == 1) {
            return topicNames;
        }
        
        // 对服务列表排序，确保所有节点的分片结果一致
        Collections.sort(onlineServices);
        
        int currentNodeIndex = onlineServices.indexOf(currentNodeId);
        if (currentNodeIndex == -1) {
            log.warn("当前节点不在在线服务列表中，处理所有主题");
            return topicNames;
        }
        
        // 计算分片
        List<String> assignedTopics = new ArrayList<>();
        int serviceCount = onlineServices.size();
        
        for (int i = 0; i < topicNames.size(); i++) {
            if (i % serviceCount == currentNodeIndex) {
                assignedTopics.add(topicNames.get(i));
            }
        }
        
        return assignedTopics;
    }
    
    /**
     * 对消费者组列表进行分片
     * @param consumerGroups 所有消费者组列表
     * @return 分配给当前节点的消费者组列表
     */
    public List<String> shardConsumerGroups(List<String> consumerGroups) {
        if (consumerGroups == null || consumerGroups.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<String> onlineServices = getOnlineServices();
        if (onlineServices.isEmpty()) {
            log.warn("没有在线服务，当前节点处理所有消费者组");
            return consumerGroups;
        }
        
        // 使用唯一在线服务数量判断是否需要分片
        int uniqueServiceCount = getUniqueOnlineServiceCount();
        if (uniqueServiceCount == 1) {
            return consumerGroups;
        }
        
        // 对服务列表排序，确保所有节点的分片结果一致
        Collections.sort(onlineServices);
        
        int currentNodeIndex = onlineServices.indexOf(currentNodeId);
        if (currentNodeIndex == -1) {
            log.warn("当前节点不在在线服务列表中，处理所有消费者组");
            return consumerGroups;
        }
        
        // 计算分片
        List<String> assignedGroups = new ArrayList<>();
        int serviceCount = onlineServices.size();
        
        for (int i = 0; i < consumerGroups.size(); i++) {
            if (i % serviceCount == currentNodeIndex) {
                assignedGroups.add(consumerGroups.get(i));
            }
        }
        
        return assignedGroups;
    }

    /**
     * 对告警配置列表进行分片
     * @param alertConfigIds 所有告警配置ID列表
     * @return 分配给当前节点的告警配置ID列表
     */
    public List<Long> shardAlertConfigs(List<Long> alertConfigIds) {
        if (alertConfigIds == null || alertConfigIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> onlineServices = getOnlineServices();
        if (onlineServices.isEmpty()) {
            log.warn("没有在线服务，当前节点处理所有告警配置");
            return alertConfigIds;
        }

        // 使用唯一在线服务数量判断是否需要分片
        int uniqueServiceCount = getUniqueOnlineServiceCount();
        if (uniqueServiceCount == 1) {
            return alertConfigIds;
        }

        // 对服务列表排序，确保所有节点的分片结果一致
        Collections.sort(onlineServices);

        int currentNodeIndex = onlineServices.indexOf(currentNodeId);
        if (currentNodeIndex == -1) {
            log.warn("当前节点不在在线服务列表中，处理所有告警配置");
            return alertConfigIds;
        }

        // 计算分片
        List<Long> assignedConfigs = new ArrayList<>();
        int serviceCount = onlineServices.size();

        for (int i = 0; i < alertConfigIds.size(); i++) {
            if (i % serviceCount == currentNodeIndex) {
                assignedConfigs.add(alertConfigIds.get(i));
            }
        }

        return assignedConfigs;
    }

    /**
     * 对数据表列表进行分片
     * @param tableNames 所有需要清理的表名列表
     * @return 分配给当前节点的表名列表
     */
    public List<String> shardTables(List<String> tableNames) {
        if (tableNames == null || tableNames.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> onlineServices = getOnlineServices();
        if (onlineServices.isEmpty()) {
            log.warn("没有在线服务，当前节点处理所有数据表");
            return tableNames;
        }

        // 使用唯一在线服务数量判断是否需要分片
        int uniqueServiceCount = getUniqueOnlineServiceCount();
        if (uniqueServiceCount == 1) {
            return tableNames;
        }

        // 对服务列表排序，确保所有节点的分片结果一致
        Collections.sort(onlineServices);

        int currentNodeIndex = onlineServices.indexOf(currentNodeId);
        if (currentNodeIndex == -1) {
            log.warn("当前节点不在在线服务列表中，处理所有数据表");
            return tableNames;
        }

        // 计算分片
        List<String> assignedTables = new ArrayList<>();
        int serviceCount = onlineServices.size();

        for (int i = 0; i < tableNames.size(); i++) {
            if (i % serviceCount == currentNodeIndex) {
                assignedTables.add(tableNames.get(i));
            }
        }

        return assignedTables;
    }

    /**
     * 获取当前节点ID
     */
    public String getCurrentNodeId() {
        return currentNodeId;
    }
    
    /**
     * 获取分布式任务执行锁
     * @param taskType 任务类型
     * @param lockTimeout 锁超时时间（秒）
     * @return 是否获取到锁
     */
    public boolean acquireTaskLock(String taskType, long lockTimeout) {
        try {
            String lockKey = TASK_SHARD_LOCK_KEY + taskType;
            Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(
                    lockKey, currentNodeId, lockTimeout, TimeUnit.SECONDS);
            
            if (lockAcquired != null && lockAcquired) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("获取任务锁异常: taskType={}", taskType, e);
            return false;
        }
    }
    
    /**
     * 释放分布式任务执行锁
     * @param taskType 任务类型
     */
    public void releaseTaskLock(String taskType) {
        try {
            String lockKey = TASK_SHARD_LOCK_KEY + taskType;
            redisTemplate.delete(lockKey);
        } catch (Exception e) {
            log.error("释放任务锁异常: taskType={}", taskType, e);
        }
    }
    
    /**
     * 保存分片任务结果
     * @param taskType 任务类型
     * @param shardResult 分片结果
     */
    public void saveShardResult(String taskType, Map<String, Object> shardResult) {
        try {
            String resultKey = TASK_SHARD_RESULT_KEY + taskType + ":" + currentNodeId;
            long expireSeconds = taskConfig.getShardResultExpireMinutes() * 60;
            redisTemplate.opsForValue().set(resultKey, shardResult, expireSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("保存分片任务结果异常: taskType={}", taskType, e);
        }
    }
    
    /**
     * 获取所有分片任务结果
     * @param taskType 任务类型
     * @return 所有节点的分片结果
     */
    public Map<String, Object> getAllShardResults(String taskType) {
        try {
            String pattern = TASK_SHARD_RESULT_KEY + taskType + ":*";
            Set<String> keys = scanKeys(pattern);
            String keyPrefix = TASK_SHARD_RESULT_KEY + taskType + ":";

            Map<String, Object> allResults = new HashMap<>();
            if (keys != null && !keys.isEmpty()) {
                // 关键逻辑：批量拉取结果，减少 Redis 往返次数
                List<String> keyList = new ArrayList<>(keys);
                final int batchSize = 200;
                for (int i = 0; i < keyList.size(); i += batchSize) {
                    List<String> batchKeys = keyList.subList(i, Math.min(i + batchSize, keyList.size()));
                    List<Object> batchValues = redisTemplate.opsForValue().multiGet(batchKeys);
                    if (batchValues == null) {
                        continue;
                    }
                    for (int j = 0; j < batchKeys.size(); j++) {
                        Object value = batchValues.get(j);
                        if (value == null) {
                            continue;
                        }
                        String key = batchKeys.get(j);
                        String nodeId = key.startsWith(keyPrefix) ? key.substring(keyPrefix.length())
                                : key.substring(key.lastIndexOf(":") + 1);
                        allResults.put(nodeId, value);
                    }
                }
            }
            
            return allResults;
        } catch (Exception e) {
            log.error("获取所有分片任务结果异常: taskType={}", taskType, e);
            return Collections.emptyMap();
        }
    }
    
    /**
     * 清理指定任务类型的所有分片结果
     * @param taskType 任务类型
     */
    public void clearShardResults(String taskType) {
        try {
            String pattern = TASK_SHARD_RESULT_KEY + taskType + ":*";
            Set<String> keys = scanKeys(pattern);
            if (keys == null || keys.isEmpty()) {
                return;
            }

            // 关键逻辑：分批删除，避免一次性删除过多 key
            List<String> keyList = new ArrayList<>(keys);
            final int batchSize = 500;
            for (int i = 0; i < keyList.size(); i += batchSize) {
                List<String> batchKeys = keyList.subList(i, Math.min(i + batchSize, keyList.size()));
                redisTemplate.delete(batchKeys);
            }
        } catch (Exception e) {
            log.error("清理分片任务结果失败", e);
        }
    }

    private Set<String> scanKeys(String pattern) {
        // 关键逻辑：用 SCAN 替代 KEYS，避免 Redis 阻塞
        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = new LinkedHashSet<>();
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();

            try (Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                    String key = serializer.deserialize(cursor.next());
                    if (key != null) {
                        keys.add(key);
                    }
                }
            }
            return keys;
        });
    }
}
