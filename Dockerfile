# ========================================
# 第一阶段：构建阶段
# ========================================
FROM maven:3.9-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /build

# 复制 pom.xml 文件（利用 Docker 缓存层）
COPY pom.xml .
COPY efak-ai/pom.xml efak-ai/
COPY efak-core/pom.xml efak-core/
COPY efak-dto/pom.xml efak-dto/
COPY efak-tool/pom.xml efak-tool/
COPY efak-web/pom.xml efak-web/

# 下载依赖项
RUN mvn dependency:go-offline -B

# 复制源代码
COPY efak-ai/src efak-ai/src
COPY efak-core/src efak-core/src
COPY efak-dto/src efak-dto/src
COPY efak-tool/src efak-tool/src
COPY efak-web/src efak-web/src

# 构建应用
RUN mvn clean package -DskipTests -B

# ========================================
# 第二阶段：运行阶段
# ========================================
FROM eclipse-temurin:17-jdk

# 设置维护者信息
LABEL maintainer="Mr.SmartLoli <smartloli.org@gmail.com>"
LABEL description="EFAK-AI - (Eagle For Apache Kafka - AI Enhanced)"
LABEL version="5.0.0"

# 设置工作目录
WORKDIR /app

# 安装必要的系统依赖
RUN apt-get update && \
    apt-get install -y \
    curl \
    wget \
    && rm -rf /var/lib/apt/lists/*

# 创建应用用户
RUN groupadd -r efak && useradd -r -g efak efak

# 创建运行时目录
RUN mkdir -p /app/logs /app/config

# 从构建阶段复制编译好的 JAR 文件
COPY --from=builder /build/efak-web/target/KafkaEagle.jar /app/KafkaEagle.jar

# 复制配置文件
COPY --from=builder /build/efak-web/src/main/resources/application.yml /app/config/
COPY --from=builder /build/efak-web/src/main/resources/log4j.properties /app/config/

# 复制 SQL 脚本到配置目录
COPY --from=builder /build/efak-web/src/main/resources/sql /app/config/sql

# 复制静态资源
COPY --from=builder /build/efak-web/src/main/resources/statics /app/statics
COPY --from=builder /build/efak-web/src/main/resources/templates /app/templates

# 设置文件权限
RUN chown -R efak:efak /app

# 切换到应用用户
USER efak

# 暴露端口
EXPOSE 8080

# 设置 JVM 参数
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom"

# 设置 Spring Boot 配置
ENV SPRING_PROFILES_ACTIVE=docker
ENV SPRING_CONFIG_LOCATION=classpath:/application.yml,file:/app/config/application.yml

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/health/check || exit 1

# 启动应用
# JAR 文件已重命名为 KafkaEagle.jar，这样 jps 会显示 KafkaEagle
# 通过 JVM 参数 -Dapp.name=KafkaEagle 来进一步标识进程
CMD ["sh", "-c", "exec java $JAVA_OPTS -Dapp.name=KafkaEagle -Dspring.application.name=KafkaEagle -Dspring.config.location=$SPRING_CONFIG_LOCATION -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -jar /app/KafkaEagle.jar"]
