# EFAK-AI 部署指南

本文档提供 EFAK-AI 的两种部署方式：Docker 容器化部署和传统的 tar.gz 安装包部署。

## 目录

- [系统要求](#系统要求)
- [Docker 部署](#docker-部署)
- [tar.gz 安装包部署](#targz-安装包部署)
- [配置说明](#配置说明)
- [常见问题](#常见问题)

---

## 系统要求

### 基础要求

- **操作系统**: Linux (Ubuntu 20.04+, CentOS 7+) / macOS / Windows (WSL2)
- **JDK**: 17 或更高版本 (仅 tar.gz 部署需要)
- **内存**: 最低 2GB, 推荐 4GB+
- **磁盘**: 最低 10GB 可用空间

### 依赖服务

- **MySQL**: 8.0 或更高版本
- **Redis**: 6.0 或更高版本
- **Kafka**: 3.0 或更高版本（监控目标）

---

## Docker 部署

Docker 部署是最简单快速的部署方式，适合开发、测试和生产环境。

### 方式一：使用 docker-compose（推荐）

#### 1. 前置准备

确保已安装 Docker 和 Docker Compose:

```bash
# 检查 Docker 版本
docker --version  # 需要 20.10+

# 检查 Docker Compose 版本
docker-compose --version  # 需要 2.0+
```

#### 2. 获取源代码

```bash
git clone https://github.com/smartloli/EFAK-AI.git
cd EFAK-AI
```

#### 3. 启动所有服务

```bash
# 启动 EFAK-AI + MySQL + Redis
docker-compose up -d

# 查看日志
docker-compose logs -f efak-ai
```

这将自动启动以下服务：
- `efak-ai`: 主应用 (端口 8080)
- `mysql`: 数据库 (端口 3306)
- `redis`: 缓存 (端口 6379)

#### 4. 启动 Nginx 反向代理（可选）

```bash
# 使用 nginx profile 启动
docker-compose --profile nginx up -d

# 现在可以通过 http://localhost 访问（80端口）
```

#### 5. 访问应用

```bash
# 直接访问
http://localhost:8080

# 通过 Nginx 访问（如果启动了 nginx）
http://localhost
```

默认登录账号：
- 用户名: `admin`
- 密码: `admin123`

#### 6. 管理命令

```bash
# 查看运行状态
docker-compose ps

# 查看日志
docker-compose logs -f efak-ai

# 停止服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v

# 重启服务
docker-compose restart efak-ai

# 更新镜像
docker-compose pull
docker-compose up -d
```

### 方式二：使用 Dockerfile 构建

#### 1. 构建镜像

```bash
cd EFAK-AI
docker build -t efak-ai:5.0.0 .
```

#### 2. 运行容器

```bash
docker run -d \
  --name efak-ai \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://your-mysql-host:3306/efak_ai" \
  -e SPRING_DATASOURCE_USERNAME="root" \
  -e SPRING_DATASOURCE_PASSWORD="your-password" \
  -e SPRING_DATA_REDIS_HOST="your-redis-host" \
  -e SPRING_DATA_REDIS_PORT="6379" \
  -v /path/to/logs:/opt/efak-ai/logs \
  efak-ai:5.0.0
```

### Docker 部署优势

✅ 环境一致性，无需安装 JDK  
✅ 一键启动所有依赖服务  
✅ 易于扩展和迁移  
✅ 内置健康检查  

---

## tar.gz 安装包部署

传统部署方式，适合不使用 Docker 的生产环境。

### 1. 构建安装包

在项目根目录执行：

```bash
# 确保已安装 JDK 17+ 和 Maven
./build-package.sh
```

构建成功后会在项目根目录生成 `efak-ai-5.0.0.tar.gz`

### 2. 传输安装包

将安装包上传到目标服务器：

```bash
scp efak-ai-5.0.0.tar.gz user@server:/opt/
```

### 3. 解压安装包

```bash
cd /opt
tar -zxvf efak-ai-5.0.0.tar.gz
cd efak-ai-5.0.0
```

解压后的目录结构：

```
efak-ai-5.0.0/
├── bin/              # 启动脚本
│   ├── start.sh      # 启动脚本
│   ├── stop.sh       # 停止脚本
│   ├── restart.sh    # 重启脚本
│   └── status.sh     # 状态检查脚本
├── config/           # 配置文件
│   └── application.yml
├── libs/             # JAR 包
│   └── efak-web-5.0.0.jar
├── logs/             # 日志目录
├── sql/              # SQL 初始化脚本
├── README.txt        # 说明文件
└── VERSION           # 版本信息
```

### 4. 修改配置

编辑 `config/application.yml`:

```bash
vi config/application.yml
```

关键配置项：

```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://your-mysql-host:3306/efak_ai?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: your-password

# Redis 配置
  data:
    redis:
      host: your-redis-host
      port: 6379

# 服务端口
server:
  port: 8080
```

### 5. 初始化数据库

导入 SQL 脚本到 MySQL：

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE efak_ai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入表结构和初始数据
USE efak_ai;
SOURCE /opt/efak-ai-5.0.0/sql/init.sql;
```

### 6. 启动应用

```bash
# 启动
./bin/start.sh

# 查看日志
tail -f logs/efak-ai.log

# 检查状态
./bin/status.sh
```

### 7. 设置开机自启（可选）

创建 systemd 服务：

```bash
sudo vi /etc/systemd/system/efak-ai.service
```

内容如下：

```ini
[Unit]
Description=EFAK-AI Service
After=network.target mysql.service redis.service

[Service]
Type=forking
User=efak
Group=efak
WorkingDirectory=/opt/efak-ai-5.0.0
ExecStart=/opt/efak-ai-5.0.0/bin/start.sh
ExecStop=/opt/efak-ai-5.0.0/bin/stop.sh
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启用服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable efak-ai
sudo systemctl start efak-ai
sudo systemctl status efak-ai
```

### 8. 常用命令

```bash
# 启动
./bin/start.sh

# 停止
./bin/stop.sh

# 重启
./bin/restart.sh

# 查看状态
./bin/status.sh

# 查看实时日志
tail -f logs/efak-ai.log

# 查看错误日志
grep ERROR logs/efak-ai.log
```

---

## 配置说明

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

根据服务器内存调整 `JAVA_OPTS`:

```bash
# 小内存（2GB）
JAVA_OPTS="-Xms256m -Xmx1g -XX:+UseG1GC"

# 中等内存（4GB）
JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC"

# 大内存（8GB+）
JAVA_OPTS="-Xms1g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### 日志配置

在 `application.yml` 中调整日志级别：

```yaml
logging:
  level:
    root: INFO                          # 根日志级别
    org.kafka.eagle: DEBUG              # EFAK 日志级别
    org.apache.kafka: WARN              # Kafka 客户端日志
```

---

## 常见问题

### 1. 数据库连接失败

**问题**: `Communications link failure`

**解决**:
- 检查 MySQL 是否启动
- 检查数据库 URL、用户名、密码是否正确
- 检查防火墙规则（开放 3306 端口）
- 确认 MySQL 允许远程连接

```bash
# MySQL 授权远程访问
mysql -u root -p
GRANT ALL PRIVILEGES ON efak_ai.* TO 'root'@'%' IDENTIFIED BY 'password';
FLUSH PRIVILEGES;
```

### 2. Redis 连接失败

**问题**: `Unable to connect to Redis`

**解决**:
- 检查 Redis 是否启动: `redis-cli ping`
- 检查 Redis 配置文件中的 `bind` 地址
- 检查防火墙规则（开放 6379 端口）

### 3. 端口被占用

**问题**: `Port 8080 is already in use`

**解决**:
```bash
# 查找占用端口的进程
lsof -i:8080

# 修改配置文件中的端口
vi config/application.yml
# 修改 server.port: 8080 为其他端口
```

### 4. 内存不足

**问题**: `OutOfMemoryError`

**解决**:
- 增加 JVM 堆内存: 修改 `JAVA_OPTS` 中的 `-Xmx` 参数
- 检查服务器可用内存: `free -h`
- 优化应用配置，减少缓存大小

### 5. Docker 容器无法启动

**问题**: 容器反复重启

**解决**:
```bash
# 查看容器日志
docker logs efak-ai

# 检查依赖服务是否就绪
docker-compose ps

# 重新构建镜像
docker-compose build --no-cache
docker-compose up -d
```

### 6. 数据库初始化失败

**问题**: 表不存在

**解决**:
- 确认已导入 SQL 脚本
- 检查 SQL 脚本路径是否正确
- 手动执行 SQL 脚本

```bash
# Docker 方式
docker exec -i efak-mysql mysql -uroot -padmin123 efak_ai < sql/init.sql

# 传统方式
mysql -u root -p efak_ai < sql/init.sql
```

### 7. 查看详细日志

```bash
# Docker
docker-compose logs -f --tail=100 efak-ai

# 传统部署
tail -f logs/efak-ai.log

# 查看 JVM 信息
jps -l
jstat -gcutil <pid> 1000
```

---

## 性能优化建议

### 1. 数据库优化

```sql
-- 创建索引
CREATE INDEX idx_cluster_id ON ${ke_table_name}(cluster_id);
CREATE INDEX idx_create_time ON ${ke_table_name}(create_time);

-- 定期清理历史数据
DELETE FROM ${ke_table_name} WHERE create_time < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

### 2. Redis 优化

```bash
# redis.conf
maxmemory 2gb
maxmemory-policy allkeys-lru
```

### 3. 应用优化

```yaml
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50      # 增加连接池
      minimum-idle: 10
  
  data:
    redis:
      lettuce:
        pool:
          max-active: 50         # 增加 Redis 连接池
```

---

## 升级指南

### Docker 方式升级

```bash
# 停止当前服务
docker-compose down

# 拉取最新镜像
git pull origin main
docker-compose build

# 启动新版本
docker-compose up -d

# 检查版本
docker exec efak-ai cat /opt/efak-ai/VERSION
```

### tar.gz 方式升级

```bash
# 停止旧版本
cd /opt/efak-ai-5.0.0
./bin/stop.sh

# 备份配置和数据
cp -r config config.bak
cp -r logs logs.bak

# 解压新版本
cd /opt
tar -zxvf efak-ai-5.1.0.tar.gz

# 迁移配置
cp /opt/efak-ai-5.0.0/config/application.yml /opt/efak-ai-5.1.0/config/

# 启动新版本
cd /opt/efak-ai-5.1.0
./bin/start.sh
```

---

## 监控与运维

### 健康检查

```bash
# HTTP 健康检查
curl http://localhost:8080/health/check

# 响应示例
{
    "application": "EFAK-AI",
    "port": "8080",
    "version": "5.0.0",
    "status": "UP",
    "timestamp": "2025-10-06T23:32:47.392037"
}
```

### 指标监控

```bash
# 查看应用指标
curl http://localhost:8080/actuator/metrics

# JVM 内存使用
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```
---

## 支持与反馈

- **文档**: [https://github.com/smartloli/EFAK-AI](https://github.com/smartloli/EFAK-AI)
- **问题反馈**: [GitHub Issues](https://github.com/smartloli/EFAK-AI/issues)
- **社区讨论**: [GitHub Discussions](https://github.com/smartloli/EFAK-AI/discussions)

---

**祝您部署顺利！如有问题欢迎在 GitHub 上提交 Issue。**
