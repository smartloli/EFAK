/**
 * HealthController.java
 * <p>
 * Copyright 2025 smartloli
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kafka.eagle.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.tool.constant.KeConst;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 健康检查和系统检测Controller
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/06/22 23:11:42
 * @version 5.0.0
 */
@Slf4j
@RestController
@RequestMapping("/health")
public class HealthController {

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * 基础健康检查接口
     *
     * @return 健康状态响应
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("status", "UP");
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            response.put("application", "EFAK-AI");
            response.put("version", KeConst.APP_VERSION.getValue());
            response.put("port", serverPort);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("健康检查失败", e);
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }

    /**
     * 系统信息接口
     *
     * @return 系统信息响应
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> systemInfo() {
        try {
            Map<String, Object> systemInfo = getSystemInfo();
            log.info("系统信息已检索");
            return ResponseEntity.ok(systemInfo);
        } catch (Exception e) {
            log.error("获取系统信息失败", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 版本信息接口
     *
     * @return 版本信息响应
     */
    @GetMapping("/version")
    public ResponseEntity<Map<String, Object>> versionInfo() {
        try {
            Map<String, Object> versionInfo = new HashMap<>();
            versionInfo.put("application", "EFAK-AI");
            versionInfo.put("version", KeConst.APP_VERSION.getValue());
            versionInfo.put("description", "Eagle For Apache Kafka® - AI Enhanced");
            versionInfo.put("buildTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            versionInfo.put("javaVersion", System.getProperty("java.version"));
            versionInfo.put("springBootVersion", getSpringBootVersion());

            log.info("版本信息已检索");
            return ResponseEntity.ok(versionInfo);
        } catch (Exception e) {
            log.error("获取版本信息失败", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取系统信息
     *
     * @return 系统信息Map
     */
    private Map<String, Object> getSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> systemInfo = new HashMap<>();

        systemInfo.put("os", Map.of(
                "name", System.getProperty("os.name"),
                "version", System.getProperty("os.version"),
                "arch", System.getProperty("os.arch")
        ));

        systemInfo.put("java", Map.of(
                "version", System.getProperty("java.version"),
                "vendor", System.getProperty("java.vendor"),
                "home", System.getProperty("java.home")
        ));

        systemInfo.put("memory", Map.of(
                "total", runtime.totalMemory(),
                "free", runtime.freeMemory(),
                "used", runtime.totalMemory() - runtime.freeMemory(),
                "max", runtime.maxMemory()
        ));

        systemInfo.put("processors", runtime.availableProcessors());
        systemInfo.put("uptime", System.currentTimeMillis());

        return systemInfo;
    }

    /**
     * 获取Spring Boot版本
     *
     * @return Spring Boot版本字符串
     */
    private String getSpringBootVersion() {
        try {
            Package springBootPackage = org.springframework.boot.SpringApplication.class.getPackage();
            return springBootPackage != null ? springBootPackage.getImplementationVersion() : "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }
} 