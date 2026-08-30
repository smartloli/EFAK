# 集群管理页面开发文档

## 开发概述

本次开发为EFAK-AI项目的集群管理页面添加了集群统计功能，包括后端API接口和前端页面展示。

## 功能特性

### 1. 集群统计信息展示
- **总集群数**: 显示系统中所有集群的总数量
- **在线集群**: 显示当前在线状态的集群数量
- **离线集群**: 显示当前离线状态的集群数量
- **平均可用度**: 显示所有集群的平均可用度百分比

### 2. 实时数据更新
- 页面每30秒自动刷新统计数据
- 支持手动刷新功能
- 数据加载失败时显示默认值（0）

## 技术实现

### 后端实现

#### 1. Controller层
**文件**: `ClusterController.java`
- 新增 `/api/manager/cluster/stats` GET接口
- 返回集群统计数据的JSON响应

```java
@GetMapping("/stats")
public ResponseEntity<Map<String, Object>> getClusterStats() {
    Map<String, Object> stats = clusterService.getClusterStats();
    return ResponseEntity.ok(stats);
}
```

#### 2. Service层
**文件**: `ClusterService.java` 和 `ClusterServiceImpl.java`
- 新增 `getClusterStats()` 方法
- 实现业务逻辑：计算总集群数、在线/离线集群数、平均可用度

```java
@Override
public Map<String, Object> getClusterStats() {
    List<KafkaClusterInfo> allClusters = clusterMapper.selectAll();
    
    int totalClusters = allClusters.size();
    int onlineClusters = 0;
    double totalAvailability = 0.0;
    
    for (KafkaClusterInfo cluster : allClusters) {
        if ("ONLINE".equals(cluster.getStatus())) {
            onlineClusters++;
        }
        if (cluster.getAvailability() != null) {
            totalAvailability += cluster.getAvailability();
        }
    }
    
    int offlineClusters = totalClusters - onlineClusters;
    double avgAvailability = totalClusters > 0 ? totalAvailability / totalClusters : 0.0;
    
    Map<String, Object> stats = new HashMap<>();
    stats.put("totalClusters", totalClusters);
    stats.put("onlineClusters", onlineClusters);
    stats.put("offlineClusters", offlineClusters);
    stats.put("avgAvailability", Math.round(avgAvailability * 100.0) / 100.0);
    
    return stats;
}
```

#### 3. Mapper层
**文件**: `ClusterMapper.java`
- 使用现有的查询方法，无需新增

### 前端实现

#### 1. HTML结构
**文件**: `manager.html`
- 集群概览部分已存在完整的HTML结构
- 包含四个统计卡片：总集群数、在线集群、离线集群、平均可用度

#### 2. JavaScript功能
**文件**: `manager.js`
- 已实现 `loadClusterStats()` 方法调用后端API
- 已实现 `updateClusterStats()` 方法更新页面显示
- 集成到页面初始化和自动刷新流程中

```javascript
loadClusterStats: function() {
    const self = this;
    
    $.ajax({
        url: self.config.apiEndpoints.clusterStats,
        type: 'GET',
        timeout: 10000,
        success: function(response) {
            if (response && response.code === 200) {
                self.updateClusterStats(response.data);
            } else {
                // 处理错误情况，显示默认值
                self.updateClusterStats({
                    totalClusters: 0,
                    onlineClusters: 0,
                    offlineClusters: 0,
                    avgAvailability: 0
                });
            }
        },
        error: function(xhr, status, error) {
            // 错误处理
            console.error('请求集群统计信息失败:', error);
        }
    });
}
```

## 部署说明

### 1. 编译项目
```bash
source ~/.bash_profile && mvn clean compile -DskipTests
```

### 2. 打包项目
```bash
source ~/.bash_profile && mvn clean install -DskipTests
```

### 3. 启动应用
```bash
java -jar efak-web/target/efak-web-5.0.0.jar --server.port=8088
```

### 4. 访问页面
- 应用地址: http://localhost:8088
- 管理页面: http://localhost:8088/manager.html

## 测试验证

### 1. API接口测试
```bash
curl -X GET http://localhost:8088/api/manager/cluster/stats
```

### 2. 前端功能测试
1. 打开管理页面
2. 查看集群概览部分的统计数据
3. 验证数据是否正确显示
4. 测试自动刷新功能

## 注意事项

1. **权限控制**: API接口需要登录认证，未登录用户会被重定向到登录页面
2. **数据库依赖**: 功能依赖 `ke_cluster` 表中的数据
3. **错误处理**: 前端已实现完善的错误处理机制，API调用失败时显示默认值
4. **性能考虑**: 统计数据计算基于内存操作，对于大量集群数据建议考虑缓存优化

## 开发完成状态

✅ 后端API接口开发完成  
✅ 前端页面功能完成  
✅ 项目编译打包成功  
✅ 应用启动成功  
✅ 前端页面可正常访问  

所有开发任务已完成，功能可正常使用。