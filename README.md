# EFAK-AI (Eagle For Apache Kafka - AI Enhanced)

[![Version](https://img.shields.io/badge/version-5.0.0-blue.svg)](https://github.com/smartloli/EFAK)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-green.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

[![Stargazers over time](https://starchart.cc/smartloli/EFAK.svg?variant=adaptive)](https://starchart.cc/smartloli/EFAK)

## 项目简介

**EFAK-AI (Eagle For Apache Kafka - AI)** 是一款开源的 Kafka 智能监控与管理平台，融合了人工智能技术，为 Kafka 运维提供智能化、可视化、自动化的全方位解决方案。

### 🎯 核心优势

- **🧠 AI 驱动**: 集成主流大语言模型（OpenAI、Claude、DeepSeek 等），提供智能对话式运维
- **📊 实时监控**: 全方位监控 Kafka 集群健康状态、性能指标、消费延迟等关键数据
- **🚀 高性能**: 基于 Spring Boot 3.x 和 JDK 17，采用响应式编程和异步处理
- **🔧 易部署**: 支持 Docker 一键部署和传统 tar.gz 安装包两种方式

## 核心特性

### 🤖 AI 智能助手
- **多模型支持**: 集成 OpenAI、Claude、DeepSeek 等多种大语言模型
- **Function Calling**: AI 可自动调用后端函数查询实时数据
- **图表自动生成**: 根据时序数据自动生成可视化图表
- **Kafka 专家**: 专业的 Kafka 集群分析、性能优化和故障诊断建议
- **流式对话**: 基于 SSE 的实时流式响应，体验更流畅
- **Markdown 渲染**: 支持代码高亮、表格、Mermaid 图表等丰富格式
- **对话历史**: 完整的会话管理和历史记录功能

### 📊 集群监控
- **实时监控**: Broker 节点状态、主题分区、消费者组监控
- **性能指标**: 吞吐量、延迟、存储容量等关键指标
- **历史数据**: 长期趋势分析和性能对比
- **多集群支持**: 同时管理多个 Kafka 集群

### ⚡ 分布式任务调度
- **智能分片**: 基于 Redis 的分布式任务分片执行
- **故障转移**: 自动检测节点故障并重新分配任务
- **负载均衡**: 动态调整任务分配，优化资源利用
- **单节点优化**: 自动检测单节点环境，跳过分片逻辑

### 🚨 告警管理
- **多渠道告警**: 支持钉钉、微信、飞书等多种告警渠道
- **智能阈值**: 基于历史数据的动态阈值调整
- **告警聚合**: 避免告警风暴，提供告警聚合和降噪
- **可视化配置**: 直观的告警规则配置界面

## 技术架构

### 模块结构
```
EFAK-AI/
├── efak-ai/          # 告警功能模块
├── efak-core/        # 核心功能模块 (Kafka 连接、监控逻辑)
├── efak-dto/         # 数据传输对象
├── efak-tool/        # 工具类模块
└── efak-web/         # Web 应用模块 (控制器、服务、前端)
```

### 技术栈
- **后端框架**: Spring Boot 3.4.5
- **数据库**: MySQL 8.0+ (主数据库)
- **缓存**: Redis 6.0+ (分布式锁、任务调度)
- **消息队列**: Apache Kafka 4.0.0
- **ORM**: MyBatis 3.0.4
- **前端**: Thymeleaf
- **构建工具**: Maven 3.6+
- **Java 版本**: JDK 17

## 快速开始

EFAK-AI 提供两种部署方式：**Docker 容器化部署**（推荐）和 **tar.gz 安装包部署**。

### 🚀 一键启动（超简单！）

```bash
# 克隆项目
git clone https://github.com/smartloli/EFAK-AI.git
cd EFAK-AI

# 运行快速启动脚本
./quick-start.sh
```

快速启动脚本提供：
1. Docker 一键部署
2. tar.gz 安装包构建
3. 日志查看和服务管理

### 方式一：Docker 部署（推荐）⚡

#### 环境要求
- Docker Desktop 4.43.2+
- Docker Compose 2.0+

#### 一键启动
```bash
# 1. 克隆项目
git clone https://github.com/smartloli/EFAK-AI.git
cd EFAK-AI

# 2. 启动所有服务（包括 MySQL、Redis）
docker-compose up -d

# 3. 查看日志
docker-compose logs -f efak-ai

# 4. 访问应用
# http://localhost:8080
# 默认账号: admin / admin
```

#### 启动 Nginx 反向代理（可选）
```bash
# 使用 nginx profile 启动
docker-compose --profile nginx up -d

# 通过 http://localhost (80端口) 访问
```

#### 常用 Docker 命令
```bash
# 查看运行状态
docker-compose ps

# 停止服务
docker-compose down

# 重启服务
docker-compose restart efak-ai

# 查看日志
docker-compose logs -f
```

### 方式二：tar.gz 安装包部署

#### 环境要求
- JDK 17+
- MySQL 8.0+
- Redis 6.0+

#### 1. 构建安装包
```bash
# 克隆项目
git clone https://github.com/smartloli/EFAK-AI.git
cd EFAK-AI

# 执行构建脚本
./build-package.sh

# 生成安装包: efak-ai-5.0.0.tar.gz
```

#### 2. 部署安装包
```bash
# 传输到服务器（如果需要）
scp efak-ai-5.0.0.tar.gz user@server:/opt/

# 解压
cd /opt
tar -zxvf efak-ai-5.0.0.tar.gz
cd efak-ai-5.0.0

# 目录结构
# bin/      - 启动脚本
# config/   - 配置文件
# libs/     - JAR 包
# logs/     - 日志目录
# sql/      - SQL 脚本
```

#### 3. 初始化数据库
```bash
mysql -u root -p
CREATE DATABASE efak_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE efak_ai;
SOURCE /opt/efak-ai-5.0.0/sql/ke.sql;
```

#### 4. 修改配置
编辑 `config/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/efak_ai?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

#### 5. 启动应用
```bash
# 启动
./bin/start.sh

# 查看日志
tail -f logs/efak-ai.log

# 查看状态
./bin/status.sh

# 停止
./bin/stop.sh

# 重启
./bin/restart.sh
```

#### 6. 访问应用
- 应用地址: http://localhost:8080
- 默认账号: admin / admin123

#### 7. 验证进程
```bash
# 查看进程（进程名显示为 KafkaEagle）
ps aux | grep KafkaEagle
```

### 详细部署文档

完整的部署指南、配置说明和故障排查，请参阅：
- 📖 [详细部署文档](efak-web/src/main/resources/docs/DEPLOY.md)
- 🚀 [功能预览文档](efak-web/src/main/resources/docs/FEATURE_PREVIEW.md)


## 开发指南

### 本地开发环境

#### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

#### 开发步骤
```bash
# 1. 克隆项目
git clone https://github.com/smartloli/EFAK-AI.git
cd EFAK-AI

# 2. 创建数据库并导入 SQL 脚本
mysql -u root -p
CREATE DATABASE efak_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE efak_ai;
SOURCE efak-web/src/main/resources/sql/ke.sql;
# ... 导入其他 SQL 脚本

# 3. 修改配置文件
vi efak-web/src/main/resources/application.yml

# 4. 编译项目
mvn clean compile -DskipTests

# 5. 运行应用
cd efak-web
mvn spring-boot:run
```

## 配置说明

### 核心配置文件

#### application.yml
```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/efak_ai?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: admin123

# Redis 配置
  data:
    redis:
      host: localhost
      port: 6379
      database: 0

# 服务端口
server:
  port: 8080
```

### 环境变量配置（Docker）

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `SPRING_DATASOURCE_URL` | 数据库连接 URL | jdbc:mysql://mysql:3306/efak_ai |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 | root |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 | admin123 |
| `SPRING_DATA_REDIS_HOST` | Redis 主机 | redis |
| `SPRING_DATA_REDIS_PORT` | Redis 端口 | 6379 |
| `SERVER_PORT` | 应用端口 | 8080 |
| `JAVA_OPTS` | JVM 参数 | -Xms512m -Xmx2g |

### JVM 参数调优

根据服务器内存调整：
```bash
# 小内存（2GB）
JAVA_OPTS="-Xms256m -Xmx1g -XX:+UseG1GC"

# 中等内存（4GB）
JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC"

# 大内存（8GB+）
JAVA_OPTS="-Xms1g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

## API 文档

### 健康检查 API
```bash
# 基础健康检查
GET /health/check

# 响应示例
{
    "application": "EFAK-AI",
    "port": "8080",
    "version": "5.0.0",
    "status": "UP",
    "timestamp": "2025-10-06T23:32:47.392037"
}
```

### AI 助手 API
```bash
# SSE 流式聊天（推荐）
GET /api/chat/stream?modelId=1&message=分析集群性能&clusterId=xxx&enableCharts=true

# 返回: Server-Sent Events 流式响应
data: {"type":"thinking","content":"正在分析..."}
data: {"type":"content","content":"根据查询结果..."}
data: {"type":"chart","chartData":"{\"type\":\"line\",...}"}
data: {"type":"end"}
```

### 集群监控 API
```bash
# 获取集群列表
GET /api/cluster/list

# 获取集群详情
GET /api/cluster/info?clusterId=xxx

# 获取 Broker 信息
GET /api/cluster/brokers?clusterId=xxx

# 获取 Topic 列表
GET /api/topic/list?clusterId=xxx

# 获取 Topic 指标
GET /api/topic/metrics?clusterId=xxx&topic=xxx

# 获取消费者组信息
GET /api/consumer/groups?clusterId=xxx
```

## 开发指南

### 项目结构
```
efak-web/src/main/java/org/kafka/eagle/
├── web/
│   ├── controller/     # 控制器层
│   ├── service/        # 服务层
│   ├── mapper/         # 数据访问层
│   ├── config/         # 配置类
│   ├── security/       # 安全配置
│   └── scheduler/      # 任务调度
├── core/               # 核心业务逻辑
├── dto/                # 数据传输对象
└── tool/               # 工具类
```

### 开发规范
- 使用 Java 17 特性
- 遵循 Spring Boot 最佳实践
- 使用 Lombok 简化代码
- 统一异常处理和日志记录
- 编写单元测试和集成测试

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目基于 Apache License 2.0 许可证开源。详情请参阅 [LICENSE](LICENSE) 文件。

## 联系方式

- 官网主页: https://www.kafka-eagle.org/
- 项目主页: https://github.com/smartloli/EFAK-AI
- 问题反馈: https://github.com/smartloli/EFAK-AI/issues
- 作者: Mr.SmartLoli

## 更新日志

### v5.0.0 (2025-10-06)
- ✨ 集成 AI 智能助手功能
- ✨ 实现分布式任务调度系统
- ✨ 支持多种大语言模型
- ✨ 完善告警管理系统
- ✨ 优化用户界面和用户体验

---

**EFAK-AI** - 让 Kafka 监控更智能，让运维更高效！
