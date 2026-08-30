# 主题图标覆盖和API重复调用问题修复

## 问题描述

### 1. 主题图标被覆盖问题
- **问题现象**: `TaskExecutorManager.java` 中的 `executeTopicMonitorTask` 方法在更新 `ke_topic_info` 表时会覆盖 `icon` 字段
- **根本原因**: `TopicInfoMapper.insertOrUpdate` 方法使用 `ON DUPLICATE KEY UPDATE` 语句更新所有字段，包括 `icon` 字段
- **影响**: 用户手动设置的主题图标会被系统监控任务重置

### 2. API接口重复调用问题
- **问题现象**: 主题管理页面存在重复调用 `/api/users/current` 和 `/topic/api/list` 接口的问题
- **根本原因**: 
  - `topics.html` 中存在多个 `DOMContentLoaded` 事件监听器
  - `CommonModule.init()` 被重复调用
  - 导致页面初始化逻辑执行多次

## 解决方案

### 1. 修复主题图标覆盖问题

#### 1.1 修改 TopicInfoMapper.java
- 在 `insertOrUpdate` 方法中，将 `icon` 字段的更新逻辑改为 `icon = COALESCE(icon, VALUES(icon))`
- 新增 `insertOrUpdateWithoutIcon` 方法，专门用于系统监控任务的更新操作

```java
@Insert("INSERT INTO ke_topic_info (topic_name, partitions, replicas, broker_spread, broker_skewed, leader_skewed, retention_time, icon) " +
        "VALUES (#{topicName}, #{partitions}, #{replicas}, #{brokerSpread}, #{brokerSkewed}, #{leaderSkewed}, #{retentionTime}, #{icon}) " +
        "ON DUPLICATE KEY UPDATE " +
        "partitions = VALUES(partitions), " +
        "replicas = VALUES(replicas), " +
        "broker_spread = VALUES(broker_spread), " +
        "broker_skewed = VALUES(broker_skewed), " +
        "leader_skewed = VALUES(leader_skewed), " +
        "retention_time = VALUES(retention_time)")
void insertOrUpdateWithoutIcon(TopicInfo topicInfo);
```

#### 1.2 修改 TaskExecutorManager.java
- 将 `saveTopicStatsToDatabase` 方法中的 `topicInfoMapper.insertOrUpdate` 调用替换为 `topicInfoMapper.insertOrUpdateWithoutIcon`

```java
// 修改前
topicInfoMapper.insertOrUpdate(topicInfo);

// 修改后
topicInfoMapper.insertOrUpdateWithoutIcon(topicInfo);
```

### 2. 修复API重复调用问题

#### 2.1 修复 topics.html 重复初始化
- 合并多个 `DOMContentLoaded` 事件监听器为一个
- 将所有初始化逻辑集中到一个事件监听器中
- 删除重复的 `CommonModule.init()` 调用

#### 2.2 优化初始化顺序
```javascript
document.addEventListener('DOMContentLoaded', function () {
    // 设置当前页面活跃状态
    if (typeof setActiveNavItem === 'function') {
        setActiveNavItem('topics');
    }

    // 初始化页面功能
    if (typeof TopicsModule !== 'undefined') {
        TopicsModule.init();
    }

    // 初始化模态框事件和创建主题模态框
    setTimeout(() => {
        initModalEvents();
        initCreateTopicModal();
    }, 100);

    // 初始化Select2
    initializeSelect2();

    // 初始化主题趋势图表
    initializeTopicTrendChart();

    // 初始化主题列表
    initializeTopicList();

    // 主题详情页面跳转
    document.querySelectorAll('.topic-link').forEach(link => {
        link.addEventListener('click', function (e) {
            if (!e.ctrlKey && !e.metaKey) {
                e.preventDefault();
                const topicName = this.getAttribute('data-topic');
                window.location.href = `/topic-detail?name=${encodeURIComponent(topicName)}`;
            }
        });
    });
});
```

## 修复效果

### 1. 主题图标保护
- ✅ 系统监控任务不再覆盖用户设置的主题图标
- ✅ 保持原有的插入和更新功能正常工作
- ✅ 新创建的主题仍可正常设置图标

### 2. API调用优化
- ✅ 消除了重复的 `/api/users/current` 接口调用
- ✅ 避免了重复的 `/topic/api/list` 接口调用
- ✅ 提升了页面加载性能
- ✅ 减少了服务器负载

## 测试建议

1. **功能测试**
   - 创建新主题并设置图标，验证图标正常显示
   - 等待系统监控任务执行后，验证图标未被覆盖
   - 测试主题列表页面的加载和刷新功能

2. **性能测试**
   - 使用浏览器开发者工具监控网络请求
   - 验证页面加载时不存在重复的API调用
   - 检查页面初始化时间是否有改善

3. **兼容性测试**
   - 测试现有主题的图标显示是否正常
   - 验证主题管理的各项功能是否正常工作

## 相关文件

- `efak-web/src/main/java/org/kafka/eagle/web/mapper/TopicInfoMapper.java`
- `efak-web/src/main/java/org/kafka/eagle/web/service/TaskExecutorManager.java`
- `efak-web/src/main/resources/templates/view/topics.html`
- `efak-web/src/main/resources/statics/js/system/topics.js`