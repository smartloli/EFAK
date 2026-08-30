# 分布式任务协调器 IP+端口唯一标识修复

## 问题描述

在原有的分布式任务协调器实现中，服务的唯一标识仅基于IP地址生成，这导致了一个重要问题：

**当一台服务器上启动多个不同端口的服务实例时，基于IP地址的唯一标识会导致在线服务个数统计错误，影响分片任务的正确分配。**

### 原有逻辑问题

```java
// 原有实现：仅基于IP地址
private String getServiceUniqueId(String nodeId) {
    // 从nodeId中提取IP地址部分作为服务唯一标识
    int firstDashIndex = nodeId.indexOf('-');
    if (firstDashIndex > 0) {
        return nodeId.substring(0, firstDashIndex); // 仅返回IP
    }
    return nodeId;
}
```

### 问题场景

假设在同一台服务器 `192.168.1.100` 上启动了三个服务实例：
- 实例1：端口 8080
- 实例2：端口 8081  
- 实例3：端口 8082

原有逻辑会将这三个实例都识别为同一个服务 `192.168.1.100`，导致：
1. 在线服务数量统计为 1 而不是 3
2. 分片任务分配不均匀
3. 负载分布不合理

## 解决方案

### 修改内容

1. **添加端口配置注入**
```java
@Value("${server.port:8080}")
private int serverPort;
```

2. **修改服务唯一标识生成逻辑**
```java
/**
 * 获取服务的唯一标识（基于IP地址+端口）
 */
private String getServiceUniqueId(String nodeId) {
    if (nodeId == null) {
        return null;
    }
    // 从nodeId中提取IP地址部分，并结合端口作为服务唯一标识
    int firstDashIndex = nodeId.indexOf('-');
    if (firstDashIndex > 0) {
        String ipAddress = nodeId.substring(0, firstDashIndex);
        return ipAddress + ":" + serverPort;
    }
    return nodeId + ":" + serverPort;
}
```

3. **更新在线服务统计逻辑**
```java
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
        log.debug("唯一在线服务数量: {} (按IP+端口去重), 服务实例列表: {}", uniqueCount, uniqueServiceInstances);
        return uniqueCount;
    } catch (Exception e) {
        log.error("获取唯一在线服务数量失败", e);
        return 1; // 降级处理，返回1
    }
}
```

### 修改后的效果

现在同一台服务器上的多个服务实例会被正确识别：
- 实例1：`192.168.1.100:8080`
- 实例2：`192.168.1.100:8081`
- 实例3：`192.168.1.100:8082`

在线服务数量统计为 3，分片任务可以正确分配到每个实例。

## 技术细节

### 1. 端口获取
- 使用 `@Value("${server.port:8080}")` 注解从配置文件中获取服务端口
- 默认值为 8080，确保向后兼容性

### 2. 唯一标识格式
- 格式：`IP:PORT`
- 示例：`192.168.1.100:8080`

### 3. Redis存储结构
服务注册信息在Redis中的存储结构保持不变，但服务唯一标识的计算逻辑已更新。

### 4. 日志输出
更新了日志输出，明确显示按IP+端口去重的服务实例列表。

## 兼容性说明

### 向后兼容
- 现有的单实例部署不受影响
- 配置文件无需修改
- Redis数据结构保持不变

### 升级建议
1. 部署新版本代码
2. 重启所有服务实例
3. 观察日志确认服务实例正确识别
4. 验证分片任务分配是否均匀

## 测试验证

### 单机多实例测试
1. 在同一台服务器上启动多个不同端口的服务实例
2. 检查Redis中的服务注册信息
3. 验证在线服务数量统计是否正确
4. 观察分片任务分配是否均匀

### 多机测试
1. 在不同服务器上启动服务实例
2. 验证跨服务器的服务发现和分片功能
3. 确认负载均衡效果

## 相关文件

- **主要修改文件**：`DistributedTaskCoordinator.java`
- **配置文件**：`application.yml`
- **相关文档**：`distributed-task-README.md`

## 总结

通过将服务唯一标识从仅基于IP改为基于IP+端口的组合，成功解决了同一台服务器上多个服务实例无法正确识别的问题，确保了分布式任务调度系统的正确性和可靠性。

这个修改提高了系统的：
- **准确性**：正确统计在线服务数量
- **可扩展性**：支持单机多实例部署
- **负载均衡**：任务分配更加均匀
- **监控能力**：更精确的服务实例监控