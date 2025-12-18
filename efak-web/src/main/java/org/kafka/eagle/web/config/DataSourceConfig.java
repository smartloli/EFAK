/**
 * DataSourceConfig.java
 * <p>
 * Copyright 2023 smartloli
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
package org.kafka.eagle.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * <p>
 * 数据源配置类，负责配置应用程序的数据库连接
 * 支持MySQL和H2数据库，连接失败时自动降级到H2内存数据库
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/27 01:15:04
 * @version 5.0.0
 */
@Slf4j
@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url:jdbc:h2:mem:efak;DB_CLOSE_DELAY=-1}")
    private String url;

    @Value("${spring.datasource.username:sa}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @Value("${spring.datasource.driver-class-name:org.h2.Driver}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        try {
            // 尝试使用配置的数据源
            DataSource dataSource = DataSourceBuilder.create()
                    .url(url)
                    .username(username)
                    .password(password)
                    .driverClassName(driverClassName)
                    .build();

            // 测试连接
            // 关键逻辑：使用 try-with-resources，确保连接在异常情况下也能正确释放
            try (Connection connection = dataSource.getConnection()) {
                // no-op
            }
            return dataSource;

        } catch (Exception e) {
            log.error("数据库连接失败，使用H2内存数据库: {}", e.getMessage(), e);

            // 降级到H2内存数据库
            return DataSourceBuilder.create()
                    .url("jdbc:h2:mem:efak;DB_CLOSE_DELAY=-1")
                    .username("sa")
                    .password("")
                    .driverClassName("org.h2.Driver")
                    .build();
        }
    }
}
