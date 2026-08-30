# Kafka安全认证配置指南

本文档介绍如何在EFAK中配置Kafka安全认证，包括SASL和SSL配置。

## 概述

EFAK现在支持从数据库读取Kafka安全配置，在应用启动时自动初始化带有安全认证的Kafka客户端。支持的安全协议包括：

- **PLAINTEXT**: 无安全认证（默认）
- **SASL_PLAINTEXT**: 仅SASL认证，无SSL加密
- **SSL**: 仅SSL加密，无SASL认证
- **SASL_SSL**: SASL认证 + SSL加密

## 数据库表结构

### ke_broker_info表
存储Kafka broker节点信息：
```sql
CREATE TABLE ke_broker_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cluster_name VARCHAR(255),
    host_ip VARCHAR(255),
    port INT,
    created_time TIMESTAMP,
    updated_time TIMESTAMP
);
```

### ke_kafka_security_config表
存储Kafka安全配置信息：
```sql
CREATE TABLE ke_kafka_security_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(255) UNIQUE,
    config_value TEXT,
    description VARCHAR(500),
    created_time TIMESTAMP,
    updated_time TIMESTAMP
);
```

## 配置步骤

### 1. 配置Broker信息

首先在`ke_broker_info`表中添加Kafka broker节点信息：

```sql
INSERT INTO ke_broker_info (cluster_name, host_ip, port, created_time, updated_time) VALUES
('kafka-cluster', '192.168.1.100', 9092, NOW(), NOW()),
('kafka-cluster', '192.168.1.101', 9092, NOW(), NOW()),
('kafka-cluster', '192.168.1.102', 9092, NOW(), NOW());
```

### 2. 配置安全认证

根据你的Kafka集群安全配置，选择相应的配置方案：

#### 方案1: SASL_PLAINTEXT (PLAIN机制)

```sql
INSERT INTO ke_kafka_security_config (config_key, config_value, description, created_time, updated_time) VALUES
('security.protocol', 'SASL_PLAINTEXT', 'Kafka安全协议', NOW(), NOW()),
('sasl.mechanism', 'PLAIN', 'SASL认证机制', NOW(), NOW()),
('sasl.jaas.config', 'org.apache.kafka.common.security.plain.PlainLoginModule required username="admin" password="admin-secret";', 'SASL JAAS配置', NOW(), NOW());
```

#### 方案2: SASL_SSL (SCRAM-SHA-256机制)

```sql
INSERT INTO ke_kafka_security_config (config_key, config_value, description, created_time, updated_time) VALUES
('security.protocol', 'SASL_SSL', 'Kafka安全协议', NOW(), NOW()),
('sasl.mechanism', 'SCRAM-SHA-256', 'SASL认证机制', NOW(), NOW()),
('sasl.jaas.config', 'org.apache.kafka.common.security.scram.ScramLoginModule required username="admin" password="admin-secret";', 'SASL JAAS配置', NOW(), NOW()),
('ssl.truststore.location', '/path/to/kafka.client.truststore.jks', 'SSL信任库位置', NOW(), NOW()),
('ssl.truststore.password', 'truststore-password', 'SSL信任库密码', NOW(), NOW());
```

#### 方案3: 仅SSL

```sql
INSERT INTO ke_kafka_security_config (config_key, config_value, description, created_time, updated_time) VALUES
('security.protocol', 'SSL', 'Kafka安全协议', NOW(), NOW()),
('ssl.truststore.location', '/path/to/kafka.client.truststore.jks', 'SSL信任库位置', NOW(), NOW()),
('ssl.truststore.password', 'truststore-password', 'SSL信任库密码', NOW(), NOW()),
('ssl.keystore.location', '/path/to/kafka.client.keystore.jks', 'SSL密钥库位置', NOW(), NOW()),
('ssl.keystore.password', 'keystore-password', 'SSL密钥库密码', NOW(), NOW()),
('ssl.key.password', 'key-password', 'SSL密钥密码', NOW(), NOW());
```

### 3. 重启应用

配置完成后，重启EFAK应用。应用启动时会自动读取数据库配置并初始化Kafka客户端。

## 配置验证

启动应用后，查看日志输出：

```
2025-01-XX XX:XX:XX INFO  [main] o.k.e.w.c.KafkaInitializer - 开始初始化Kafka配置...
2025-01-XX XX:XX:XX INFO  [main] o.k.e.w.c.KafkaInitializer - 发现 3 个broker配置，开始构建Kafka配置
2025-01-XX XX:XX:XX INFO  [main] o.k.e.w.c.KafkaInitializer - 检测到安全认证已启用，构建包含安全配置的KafkaConfig
2025-01-XX XX:XX:XX INFO  [main] o.k.e.c.k.p.KafkaServiceProxy - Initializing KafkaServiceProxy with KafkaConfig: 192.168.1.100:9092,192.168.1.101:9092,192.168.1.102:9092
2025-01-XX XX:XX:XX INFO  [main] o.k.e.c.k.p.KafkaServiceProxy - KafkaServiceProxy initialized successfully with security configurations
2025-01-XX XX:XX:XX INFO  [main] o.k.e.w.c.KafkaInitializer - KafkaServiceProxy已成功初始化，bootstrap servers: 192.168.1.100:9092,192.168.1.101:9092,192.168.1.102:9092
2025-01-XX XX:XX:XX INFO  [main] o.k.e.w.c.KafkaInitializer - Kafka安全认证已启用: 安全协议=SASL_PLAINTEXT, SASL机制=PLAIN
```

## 支持的配置项

### SASL配置
- `security.protocol`: 安全协议 (SASL_PLAINTEXT, SASL_SSL)
- `sasl.mechanism`: SASL机制 (PLAIN, SCRAM-SHA-256, SCRAM-SHA-512, GSSAPI)
- `sasl.jaas.config`: JAAS配置字符串

### SSL配置
- `security.protocol`: 安全协议 (SSL, SASL_SSL)
- `ssl.truststore.location`: 信任库文件路径
- `ssl.truststore.password`: 信任库密码
- `ssl.keystore.location`: 密钥库文件路径
- `ssl.keystore.password`: 密钥库密码
- `ssl.key.password`: 密钥密码
- `ssl.endpoint.identification.algorithm`: 端点识别算法（默认：https）

## 故障排除

### 1. 应用启动失败
- 检查数据库连接是否正常
- 确认`ke_broker_info`和`ke_kafka_security_config`表是否存在
- 查看应用日志中的错误信息

### 2. Kafka连接失败
- 验证broker地址和端口是否正确
- 检查安全配置是否与Kafka集群配置匹配
- 确认SSL证书路径是否正确且可访问
- 验证SASL用户名和密码是否正确

### 3. 配置不生效
- 确认配置已正确插入数据库
- 重启应用以加载新配置
- 检查是否有配置冲突（同一config_key有多个值）

## 安全注意事项

1. **密码安全**: 数据库中的密码应该加密存储，避免明文保存
2. **证书管理**: SSL证书应该妥善保管，定期更新
3. **权限控制**: 限制对配置表的访问权限
4. **日志安全**: 避免在日志中输出敏感信息

## 动态配置更新

目前配置更新需要重启应用。未来版本将支持动态配置更新功能。