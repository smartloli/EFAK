# Topic字段映射修复文档

## 问题描述

在Topic管理功能中，发现前端显示的分区数和副本数字段与数据库字段映射不一致的问题：

- 数据库字段：`partitions`、`replicas`
- 前端期望字段：`partitionCount`、`replicationFactor`
- TopicMapper映射：原本映射到`partitions`、`replicas`属性

这导致前端无法正确显示从数据库获取的真实数据。

## 解决方案

### 1. 修改TopicMapper字段映射

**文件：** `efak-web/src/main/java/org/kafka/eagle/web/mapper/TopicMapper.java`

**修改内容：**
- 将数据库字段`partitions`映射到`partitionCount`属性
- 将数据库字段`replicas`映射到`replicationFactor`属性

**涉及方法：**
- `selectTopicPage()` - 分页查询Topic列表
- `selectTopicById()` - 根据ID查询Topic
- `selectTopicByName()` - 根据名称查询Topic

### 2. 更新TopicInfo实体类

**文件：** `efak-dto/src/main/java/org/kafka/eagle/dto/topic/TopicInfo.java`

**修改内容：**
- 添加`partitionCount`属性（前端使用）
- 修改`replicationFactor`属性为Integer类型（前端使用）
- 保留原有`partitions`和`replicas`属性（数据库兼容）
- 添加兼容性getter/setter方法，确保数据同步

### 3. 前端字段映射修复

**文件：** `efak-web/src/main/resources/statics/js/system/topics.js`

**修改内容：**
- 在`createTableRow`方法中，将`topic.partitions`改为`topic.partitionCount`
- 将`topic.replicas`改为`topic.replicationFactor`

### 4. 后端API数据修复

**文件：** `efak-web/src/main/java/org/kafka/eagle/web/controller/TopicController.java`

**修改内容：**
- 在`getTopicDetail`方法中，将mock数据替换为真实数据库查询
- 使用`topicService.getTopicByName(topicName)`获取真实Topic信息
- 正确映射数据库字段到响应对象

## 技术细节

### 字段映射关系

| 数据库字段 | TopicInfo属性 | 前端使用字段 | 说明 |
|-----------|--------------|-------------|------|
| partitions | partitions | partitionCount | 分区数 |
| replicas | replicas | replicationFactor | 副本数 |

### 兼容性处理

为了确保向后兼容，TopicInfo类中实现了兼容性方法：

```java
public Integer getPartitionCount() {
    return partitionCount != null ? partitionCount : partitions;
}

public void setPartitionCount(Integer partitionCount) {
    this.partitionCount = partitionCount;
    this.partitions = partitionCount;
}

public Integer getReplicationFactor() {
    return replicationFactor != null ? replicationFactor : replicas;
}

public void setReplicationFactor(Integer replicationFactor) {
    this.replicationFactor = replicationFactor;
    this.replicas = replicationFactor;
}
```

## 验证结果

### 编译验证
- ✅ 所有模块编译成功
- ✅ 无编译错误
- ✅ 字段映射正确

### 功能验证
- ✅ 数据库字段正确映射到前端显示字段
- ✅ Topic列表页面能正确显示分区数和副本数
- ✅ Topic详情页面能显示真实数据库数据
- ✅ 向后兼容性保持良好

## 影响范围

### 修改文件
1. `TopicMapper.java` - 数据库字段映射
2. `TopicInfo.java` - 实体类属性和兼容性方法
3. `topics.js` - 前端字段引用
4. `TopicController.java` - API数据源修改

### 功能影响
- ✅ Topic列表显示
- ✅ Topic详情显示
- ✅ Topic创建和编辑
- ✅ Topic数据统计

## 总结

通过本次修复，解决了Topic管理功能中前端显示字段与数据库字段映射不一致的问题，确保了：

1. **数据一致性**：前端能正确显示数据库中的真实数据
2. **字段映射正确性**：数据库字段正确映射到前端期望的字段名
3. **向后兼容性**：保持了原有代码的兼容性
4. **代码健壮性**：通过兼容性方法确保数据同步

修复后，Topic管理功能能够正确显示从数据库获取的分区数和副本数信息，提升了用户体验和数据准确性。