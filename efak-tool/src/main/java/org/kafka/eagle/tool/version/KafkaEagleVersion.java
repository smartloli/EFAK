/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kafka.eagle.tool.version;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.tool.constant.KeConst;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * <p>
 * 显示 EFAK 版本信息的工具类，并配合 ASCII 艺术字输出。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/6/19 23:59:45
 * @version 5.0.0
 */
@Slf4j
public class KafkaEagleVersion {

    /**
     * 以 ASCII 艺术字横幅形式展示应用版本信息。
     */
    public static void version() {
        try {
            String appVersion = KeConst.APP_VERSION.getValue();
            InputStream fontStream = getFontInputStream();

            if (fontStream != null) {
                String asciiArt = FigletFont.convertOneLine(fontStream, "EFAK · AI");

                System.out.println("Welcome to");
                System.out.println(asciiArt + "( Eagle For Apache Kafka® )\n");
                System.out.println("Version " + appVersion + " -- Copyright 2016-2025");

                fontStream.close();
            } else {
                // 如果无法加载字体，使用简单文本输出
                System.out.println("Welcome to");
                System.out.println("EFAK · AI ( Eagle For Apache Kafka® )\n");
                System.out.println("Version " + appVersion + " -- Copyright 2016-2025");
            }
        } catch (Exception e) {
            log.error("获取 Kafka Eagle 版本信息失败", e);
            // 失败时也显示基本信息
            try {
                System.out.println("Welcome to EFAK · AI ( Eagle For Apache Kafka® )");
                System.out.println("Version " + KeConst.APP_VERSION.getValue() + " -- Copyright 2016-2025");
            } catch (Exception ex) {
                log.error("无法显示版本信息", ex);
            }
        }
    }

    /**
     * 获取字体文件的输入流。
     * 优先从 classpath 加载（JAR 包内），如果失败则尝试从外部文件系统加载。
     *
     * @return 字体文件的输入流，如果无法获取则返回 null
     */
    private static InputStream getFontInputStream() {
        try {
            // 方式1：尝试从 classpath 加载（适用于 JAR 包部署）
            ClassPathResource resource = new ClassPathResource("font/slant.flf");
            if (resource.exists()) {
                log.debug("从 classpath 加载字体文件");
                return resource.getInputStream();
            }
        } catch (Exception e) {
            log.debug("从 classpath 加载字体文件失败: {}", e.getMessage());
        }

        try {
            // 方式2：尝试从外部文件系统加载（适用于解压包部署）
            String userDir = System.getProperty("user.dir");
            File externalFont = new File(userDir + File.separator + "font" + File.separator + "slant.flf");
            if (externalFont.exists() && externalFont.isFile()) {
                log.debug("从外部文件系统加载字体文件: {}", externalFont.getAbsolutePath());
                return new FileInputStream(externalFont);
            }
        } catch (Exception e) {
            log.debug("从外部文件系统加载字体文件失败: {}", e.getMessage());
        }

        log.warn("无法加载字体文件，将使用简单文本格式显示版本信息");
        return null;
    }
}
