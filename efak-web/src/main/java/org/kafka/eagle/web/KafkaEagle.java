/**
 * KafkaEagle.java
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
package org.kafka.eagle.web;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.plugins.mysql.MySqlRecordSchema;
import org.kafka.eagle.pojo.mysql.MySQLDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

/**
 *  KafkaEagle is a web-based monitoring and management platform for Apache Kafka clusters.
 *  This Spring Boot application initializes the KafkaEagle web application and starts an embedded Tomcat server
 *  that serves the web pages and REST APIs.
 *
 * @Author: smartloli
 * @Date: 2023/5/13 21:11
 * @Version: 3.4.0
 */
@SpringBootApplication
@EnableGlobalMethodSecurity(securedEnabled = true)
@Slf4j
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
        SpringApplication.run(KafkaEagle.class, args);
    }

    @Bean
    public void initKafkaEagleDatabase() {
        MySQLDataSource mySQLDataSource = new MySQLDataSource();
        mySQLDataSource.setDbUrl(this.dbUrl);
        mySQLDataSource.setDbUserName(this.dbUserName);
        mySQLDataSource.setDbPassword(this.dbPassword);
        mySQLDataSource.setDbDriverName(this.dbDriverName);
        MySqlRecordSchema.schema(mySQLDataSource);
        log.info("MySQLDataSource initialization: {}",mySQLDataSource);
    }
}
