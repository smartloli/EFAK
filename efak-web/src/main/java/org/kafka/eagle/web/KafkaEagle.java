/**
 * KafkaEagle.java
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
package org.kafka.eagle.web;

import org.kafka.eagle.tool.version.KafkaEagleVersion;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * <p>
 * EFAK-AI主应用启动类
 * Eagle For Apache Kafka - AI Enhanced，基于AI增强的Kafka监控管理平台
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/13 21:13:55
 * @version 5.0.0
 */
@SpringBootApplication(scanBasePackages = "org.kafka.eagle")
@MapperScan("org.kafka.eagle.web.mapper")
@EnableMethodSecurity(securedEnabled = true)
@EnableScheduling
public class KafkaEagle {

    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String dbUserName;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String dbDriverName;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(KafkaEagle.class);
        application.setBannerMode(Banner.Mode.OFF);
        application.run(args);
        KafkaEagleVersion.version();
    }
}
