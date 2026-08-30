#!/bin/bash

################################################################################
# EFAK-AI 安装包构建脚本
# 用于构建 tar.gz 格式的发行版安装包
# Author: Mr.SmartLoli
# Version: 5.0.0
################################################################################

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR"

# 版本信息
VERSION="5.0.0"
PACKAGE_NAME="efak-ai-${VERSION}"

# 目录定义
DIST_DIR="${PROJECT_ROOT}/dist"
BUILD_DIR="${PROJECT_ROOT}/target/package"
PACKAGE_DIR="${BUILD_DIR}/${PACKAGE_NAME}"

echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}EFAK-AI 安装包构建脚本${NC}"
echo -e "${GREEN}版本: ${VERSION}${NC}"
echo -e "${GREEN}================================${NC}"

# 清理旧的构建目录
echo -e "${YELLOW}[1/6] 清理旧的构建目录...${NC}"
rm -rf "${BUILD_DIR}"
mkdir -p "${BUILD_DIR}"
mkdir -p "${PACKAGE_DIR}"

# 构建项目
echo -e "${YELLOW}[2/6] 编译项目...${NC}"
cd "${PROJECT_ROOT}"
mvn clean package -DskipTests -q
if [ $? -ne 0 ]; then
    echo -e "${RED}编译失败！${NC}"
    exit 1
fi
echo -e "${GREEN}编译完成！${NC}"

# 创建包目录结构
echo -e "${YELLOW}[3/6] 创建包目录结构...${NC}"
mkdir -p "${PACKAGE_DIR}/bin"
mkdir -p "${PACKAGE_DIR}/config"
mkdir -p "${PACKAGE_DIR}/libs"
mkdir -p "${PACKAGE_DIR}/logs"
mkdir -p "${PACKAGE_DIR}/sql"
mkdir -p "${PACKAGE_DIR}/font"

# 复制 JAR 文件
echo -e "${YELLOW}[4/6] 复制应用文件...${NC}"

# 复制主 JAR 包（已重命名为 KafkaEagle.jar）
cp "${PROJECT_ROOT}/efak-web/target/KafkaEagle.jar" "${PACKAGE_DIR}/libs/"

# 复制依赖 JAR（如果使用 dependency:copy-dependencies）
# mvn dependency:copy-dependencies -DoutputDirectory="${PACKAGE_DIR}/libs" -DincludeScope=runtime

# 复制配置文件
echo -e "${YELLOW}[5/6] 复制配置文件...${NC}"
cp "${PROJECT_ROOT}/efak-web/src/main/resources/application.yml" "${PACKAGE_DIR}/config/"

# 复制 SQL 脚本
if [ -d "${PROJECT_ROOT}/efak-web/src/main/resources/sql" ]; then
    cp -r "${PROJECT_ROOT}/efak-web/src/main/resources/sql/"* "${PACKAGE_DIR}/sql/"
fi

# 复制字体文件
echo -e "${YELLOW}复制字体文件...${NC}"
if [ -d "${PROJECT_ROOT}/efak-tool/src/main/resources/font" ]; then
    cp -r "${PROJECT_ROOT}/efak-tool/src/main/resources/font/"* "${PACKAGE_DIR}/font/"
    echo -e "${GREEN}字体文件复制完成${NC}"
else
    echo -e "${YELLOW}警告: 未找到字体文件目录${NC}"
fi

# 创建启动脚本
cat > "${PACKAGE_DIR}/bin/start.sh" << 'STARTSCRIPT'
#!/bin/bash

################################################################################
# EFAK-AI 启动脚本
################################################################################

# 获取脚本所在目录
BIN_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(dirname "$BIN_DIR")"

# 配置目录
CONFIG_DIR="${BASE_DIR}/config"
LIBS_DIR="${BASE_DIR}/libs"
LOGS_DIR="${BASE_DIR}/logs"

# 日志文件
LOG_FILE="${LOGS_DIR}/efak-ai.log"
PID_FILE="${BASE_DIR}/efak-ai.pid"

# JVM 参数
JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx2g -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOGS_DIR}}"

# 应用配置
SPRING_CONFIG_LOCATION="file:${CONFIG_DIR}/application.yml"
SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-prod}"

# 检查 Java 环境
if [ -z "$JAVA_HOME" ]; then
    JAVA_CMD="java"
else
    JAVA_CMD="$JAVA_HOME/bin/java"
fi

# 检查 Java 版本
$JAVA_CMD -version > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "错误: 未找到 Java 环境，请安装 JDK 17 或更高版本"
    exit 1
fi

JAVA_VERSION=$($JAVA_CMD -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "错误: Java 版本过低，需要 JDK 17 或更高版本，当前版本: $JAVA_VERSION"
    exit 1
fi

# 创建日志目录
mkdir -p "${LOGS_DIR}"

# 检查是否已经运行
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null 2>&1; then
        echo "EFAK-AI 已经在运行中 (PID: $PID)"
        exit 1
    else
        rm -f "$PID_FILE"
    fi
fi

# 查找 JAR 文件（文件名已改为 KafkaEagle.jar）
JAR_FILE="${LIBS_DIR}/KafkaEagle.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "错误: 未找到应用 JAR 文件 (KafkaEagle.jar)"
    exit 1
fi

echo "================================"
echo "启动 EFAK-AI..."
echo "================================"
echo "Java 命令: $JAVA_CMD"
echo "Java 版本: $JAVA_VERSION"
echo "配置目录: $CONFIG_DIR"
echo "日志目录: $LOGS_DIR"
echo "JAR 文件: $JAR_FILE"
echo "================================"

# 启动应用
# 注意：Spring Boot fat jar 需要使用 -jar 方式启动
# 通过 JVM 参数 -Dapp.name=KafkaEagle 来标识进程
nohup "$JAVA_CMD" $JAVA_OPTS \
  -Dapp.name=KafkaEagle \
  -Dspring.application.name=KafkaEagle \
  -Dspring.config.location="$SPRING_CONFIG_LOCATION" \
  -Dspring.profiles.active="$SPRING_PROFILES_ACTIVE" \
  -Dlogging.file.path="$LOGS_DIR" \
  -jar "$JAR_FILE" > "$LOG_FILE" 2>&1 &

PID=$!
echo $PID > "$PID_FILE"

echo "EFAK-AI 启动成功！"
echo "进程 PID: $PID"
echo "进程名称: KafkaEagle"
echo "日志文件: $LOG_FILE"
echo ""
echo "使用以下命令查看日志:"
echo "  tail -f $LOG_FILE"
echo ""
echo "使用以下命令停止应用:"
echo "  $BIN_DIR/stop.sh"
STARTSCRIPT

# 创建停止脚本
cat > "${PACKAGE_DIR}/bin/stop.sh" << 'STOPSCRIPT'
#!/bin/bash

################################################################################
# EFAK-AI 停止脚本
################################################################################

# 获取脚本所在目录
BIN_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(dirname "$BIN_DIR")"

PID_FILE="${BASE_DIR}/efak-ai.pid"

if [ ! -f "$PID_FILE" ]; then
    echo "EFAK-AI 未运行"
    exit 1
fi

PID=$(cat "$PID_FILE")

if ! ps -p $PID > /dev/null 2>&1; then
    echo "进程 $PID 不存在，清理 PID 文件"
    rm -f "$PID_FILE"
    exit 1
fi

echo "停止 EFAK-AI (PID: $PID)..."
kill $PID

# 等待进程结束
for i in {1..30}; do
    if ! ps -p $PID > /dev/null 2>&1; then
        echo "EFAK-AI 已停止"
        rm -f "$PID_FILE"
        exit 0
    fi
    sleep 1
done

# 强制停止
echo "强制停止 EFAK-AI..."
kill -9 $PID
rm -f "$PID_FILE"
echo "EFAK-AI 已强制停止"
STOPSCRIPT

# 创建重启脚本
cat > "${PACKAGE_DIR}/bin/restart.sh" << 'RESTARTSCRIPT'
#!/bin/bash

################################################################################
# EFAK-AI 重启脚本
################################################################################

BIN_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "重启 EFAK-AI..."

# 停止
"$BIN_DIR/stop.sh"

# 等待 2 秒
sleep 2

# 启动
"$BIN_DIR/start.sh"
RESTARTSCRIPT

# 创建状态检查脚本
cat > "${PACKAGE_DIR}/bin/status.sh" << 'STATUSSCRIPT'
#!/bin/bash

################################################################################
# EFAK-AI 状态检查脚本
################################################################################

BIN_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE_DIR="$(dirname "$BIN_DIR")"

PID_FILE="${BASE_DIR}/efak-ai.pid"

if [ ! -f "$PID_FILE" ]; then
    echo "EFAK-AI 未运行"
    exit 1
fi

PID=$(cat "$PID_FILE")

if ps -p $PID > /dev/null 2>&1; then
    echo "EFAK-AI 正在运行 (PID: $PID)"
    echo ""
    ps -p $PID -o pid,ppid,user,%cpu,%mem,vsz,rss,tty,stat,start,time,cmd
    exit 0
else
    echo "EFAK-AI 未运行（但 PID 文件存在）"
    exit 1
fi
STATUSSCRIPT

# 设置脚本执行权限
chmod +x "${PACKAGE_DIR}/bin/"*.sh

# 创建 README
cat > "${PACKAGE_DIR}/README.txt" << 'README'
================================
EFAK-AI v5.0.0
================================

企业级 Kafka AI 助手与监控平台

## 目录结构

- bin/        : 启动脚本目录
- config/     : 配置文件目录
- libs/       : JAR 包目录
- logs/       : 日志文件目录
- sql/        : SQL 初始化脚本
- font/       : 字体文件目录（ASCII 艺术字体）

## 安装步骤

1. 解压安装包
   tar -zxvf efak-ai-5.0.0.tar.gz
   cd efak-ai-5.0.0

2. 修改配置文件
   vi config/application.yml
   
   配置数据库连接:
   - spring.datasource.url
   - spring.datasource.username
   - spring.datasource.password
   
   配置 Redis 连接:
   - spring.data.redis.host
   - spring.data.redis.port

3. 初始化数据库
   导入 sql/ 目录下的 SQL 脚本到数据库

4. 启动应用
   bin/start.sh

5. 访问应用
   http://localhost:8080

## 常用命令

启动: bin/start.sh
停止: bin/stop.sh
重启: bin/restart.sh
状态: bin/status.sh

## 要求

- JDK 17 或更高版本
- MySQL 8.0 或更高版本
- Redis 6.0 或更高版本

## 支持

文档: https://github.com/smartloli/EFAK-AI
问题: https://github.com/smartloli/EFAK-AI/issues

================================
README

# 创建版本信息文件
cat > "${PACKAGE_DIR}/VERSION" << VERSIONFILE
EFAK-AI Version ${VERSION}
Build Date: $(date '+%Y-%m-%d %H:%M:%S')
Git Commit: $(git rev-parse --short HEAD 2>/dev/null || echo "N/A")
VERSIONFILE

# 打包
echo -e "${YELLOW}[6/6] 打包文件...${NC}"
cd "${BUILD_DIR}"
tar -czf "${PACKAGE_NAME}.tar.gz" "${PACKAGE_NAME}"

# 移动到项目根目录
mv "${PACKAGE_NAME}.tar.gz" "${PROJECT_ROOT}/"

echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}构建完成！${NC}"
echo -e "${GREEN}================================${NC}"
echo -e "安装包位置: ${PROJECT_ROOT}/${PACKAGE_NAME}.tar.gz"
echo -e "包大小: $(du -h "${PROJECT_ROOT}/${PACKAGE_NAME}.tar.gz" | cut -f1)"
echo ""
echo -e "解压命令:"
echo -e "  tar -zxvf ${PACKAGE_NAME}.tar.gz"
echo ""
echo -e "启动命令:"
echo -e "  cd ${PACKAGE_NAME}"
echo -e "  bin/start.sh"
echo -e "${GREEN}================================${NC}"
