#!/bin/bash

################################################################################
# EFAK-AI 快速启动脚本
# 用于快速启动 Docker 部署或构建 tar.gz 安装包
################################################################################

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 版本信息
VERSION="5.0.0"
IMAGE_NAME="efak-ai:${VERSION}"

# 显示 Logo
echo -e "${BLUE}"
cat << 'LOGO'
 _____ _____ _    _  __       _    ___
| ____|  ___/ \  | |/ /      / \  |_ _|
|  _| | |_ / _ \ | ' /_____ / _ \  | |
| |___|  _/ ___ \| . \_____/ ___ \ | |
|_____|_|/_/   \_\_|\_\   /_/   \_\___|

Enterprise Kafka AI Assistant & Monitoring Platform
Version 5.0.0
LOGO
echo -e "${NC}"

# 显示菜单
echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}EFAK-AI 快速启动脚本${NC}"
echo -e "${GREEN}================================${NC}"
echo ""
echo "请选择部署方式:"
echo ""
echo "  1) Docker Compose 部署 (推荐)"
echo "  2) Docker 手动构建并部署"
echo "  3) 构建 tar.gz 安装包"
echo "  4) 停止 Docker 服务"
echo "  5) 查看 Docker 日志"
echo "  6) 查看 Docker 状态"
echo "  7) 退出"
echo ""
read -p "请输入选项 [1-7]: " choice

case $choice in
    1)
        echo -e "${YELLOW}使用 Docker Compose 部署...${NC}"

        # 检查 Docker
        if ! command -v docker &> /dev/null; then
            echo -e "${RED}错误: 未安装 Docker，请先安装 Docker${NC}"
            exit 1
        fi

        # 检查 docker-compose.yml
        if [ ! -f "docker-compose.yml" ]; then
            echo -e "${RED}错误: 未找到 docker-compose.yml 文件${NC}"
            exit 1
        fi

        echo ""
        echo -e "${YELLOW}提示: 请确保已修改 docker-compose.yml 中的数据库和 Redis 连接配置${NC}"
        echo -e "${YELLOW}配置文件路径: docker-compose.yml${NC}"
        echo ""
        read -p "是否继续部署? [Y/n]: " continue_choice

        if [[ $continue_choice =~ ^[Nn]$ ]]; then
            echo -e "${YELLOW}已取消部署${NC}"
            exit 0
        fi

        echo -e "${YELLOW}构建并启动服务...${NC}"
        docker compose up -d --build

        echo ""
        echo -e "${GREEN}================================${NC}"
        echo -e "${GREEN}Docker Compose 部署成功！${NC}"
        echo -e "${GREEN}================================${NC}"
        echo ""
        echo "访问地址: http://localhost:8080"
        echo ""
        echo "常用命令:"
        echo "  查看日志: docker compose logs -f efak-ai"
        echo "  查看状态: docker compose ps"
        echo "  停止服务: docker compose down"
        echo "  重启服务: docker compose restart"
        echo "  进入容器: docker compose exec efak-ai /bin/bash"
        echo "  查看进程: docker compose exec efak-ai jps"
        echo ""
        ;;

    2)
        echo -e "${YELLOW}使用 Docker 手动构建并部署...${NC}"

        # 检查 Docker
        if ! command -v docker &> /dev/null; then
            echo -e "${RED}错误: Docker 未安装，请先安装 Docker${NC}"
            exit 1
        fi

        echo -e "${YELLOW}[1/2] 构建 Docker 镜像...${NC}"
        docker build -t ${IMAGE_NAME} .

        echo -e "${YELLOW}[2/2] 启动容器...${NC}"

        # 停止旧容器
        docker stop efak-ai 2>/dev/null || true
        docker rm efak-ai 2>/dev/null || true

        # 启动新容器
        echo ""
        echo -e "${YELLOW}提示: 使用默认配置（连接宿主机的 MySQL 和 Redis）${NC}"
        echo -e "${YELLOW}如需自定义配置，请使用 Docker Compose 方式部署${NC}"
        echo ""

        docker run -d \
          --name efak-ai \
          -p 8080:8080 \
          -e JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC" \
          -e SPRING_PROFILES_ACTIVE="prod" \
          -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/efak_ai?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai" \
          -e SPRING_DATASOURCE_USERNAME="root" \
          -e SPRING_DATASOURCE_PASSWORD="admin123" \
          -e SPRING_DATA_REDIS_HOST="host.docker.internal" \
          -e SPRING_DATA_REDIS_PORT="6379" \
          -v $(pwd)/logs:/app/logs \
          ${IMAGE_NAME}

        echo ""
        echo -e "${GREEN}================================${NC}"
        echo -e "${GREEN}Docker 容器启动成功！${NC}"
        echo -e "${GREEN}================================${NC}"
        echo ""
        echo "访问地址: http://localhost:8080"
        echo ""
        echo "常用命令:"
        echo "  查看日志: docker logs -f efak-ai"
        echo "  停止容器: docker stop efak-ai"
        echo "  启动容器: docker start efak-ai"
        echo "  重启容器: docker restart efak-ai"
        echo "  进入容器: docker exec -it efak-ai /bin/bash"
        echo "  查看进程: docker exec efak-ai jps"
        echo ""
        ;;

    3)
        echo -e "${YELLOW}构建 tar.gz 安装包...${NC}"

        # 检查 Java
        if ! command -v java &> /dev/null; then
            echo -e "${RED}错误: 未安装 Java，请先安装 JDK 17 或更高版本${NC}"
            exit 1
        fi

        # 检查 Maven
        if ! command -v mvn &> /dev/null; then
            echo -e "${RED}错误: 未安装 Maven，请先安装 Maven 3.6+${NC}"
            exit 1
        fi

        # 执行构建脚本
        if [ -f "./build-package.sh" ]; then
            ./build-package.sh
        else
            echo -e "${RED}错误: 未找到 build-package.sh 脚本${NC}"
            exit 1
        fi
        ;;

    4)
        echo -e "${YELLOW}停止 Docker 服务...${NC}"

        # 检查是否使用 Docker Compose
        if [ -f "docker-compose.yml" ]; then
            echo -e "${YELLOW}检测到 docker-compose.yml，使用 Docker Compose 停止服务${NC}"
            docker compose down
            echo -e "${GREEN}Docker Compose 服务已停止${NC}"
        else
            echo -e "${YELLOW}停止手动启动的容器${NC}"
            docker stop efak-ai 2>/dev/null || true
            docker rm efak-ai 2>/dev/null || true
            echo -e "${GREEN}Docker 容器已停止并删除${NC}"
        fi
        ;;

    5)
        echo -e "${YELLOW}查看 Docker 日志...${NC}"

        # 检查是否使用 Docker Compose
        if [ -f "docker-compose.yml" ] && docker compose ps | grep -q efak-ai; then
            echo -e "${YELLOW}使用 Docker Compose 查看日志${NC}"
            docker compose logs -f efak-ai
        elif docker ps | grep -q efak-ai; then
            echo -e "${YELLOW}查看容器日志${NC}"
            docker logs -f efak-ai
        else
            echo -e "${RED}错误: 未找到运行中的 efak-ai 容器${NC}"
            exit 1
        fi
        ;;

    6)
        echo -e "${YELLOW}查看 Docker 状态...${NC}"
        echo ""

        # 检查容器状态
        if docker ps -a | grep -q efak-ai; then
            echo -e "${GREEN}容器状态:${NC}"
            docker ps -a | grep efak-ai || true
            echo ""

            # 检查容器是否运行
            if docker ps | grep -q efak-ai; then
                echo -e "${GREEN}容器正在运行${NC}"
                echo ""

                # 获取容器 ID
                CONTAINER_ID=$(docker ps | grep efak-ai | awk '{print $1}')

                # 显示健康状态
                HEALTH_STATUS=$(docker inspect --format='{{.State.Health.Status}}' ${CONTAINER_ID} 2>/dev/null || echo "无健康检查")
                echo "健康状态: ${HEALTH_STATUS}"
                echo ""

                # 显示资源使用
                echo -e "${GREEN}资源使用:${NC}"
                docker stats --no-stream efak-ai
                echo ""

                # 验证进程名
                echo -e "${GREEN}Java 进程:${NC}"
                docker exec efak-ai jps 2>/dev/null || echo "无法获取进程信息"
            else
                echo -e "${RED}容器已停止${NC}"
            fi
        else
            echo -e "${RED}未找到 efak-ai 容器${NC}"
        fi
        ;;

    7)
        echo -e "${GREEN}退出${NC}"
        exit 0
        ;;

    *)
        echo -e "${RED}无效选项${NC}"
        exit 1
        ;;
esac
