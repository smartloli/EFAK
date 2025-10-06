package org.kafka.eagle.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * <p>
 * RestTemplate配置类，负责配置HTTP客户端
 * 设置连接超时和读取超时时间
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/05 22:49:22
 * @version 5.0.0
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000); // 30秒连接超时
        factory.setReadTimeout(60000); // 60秒读取超时

        return new RestTemplate(factory);
    }
}