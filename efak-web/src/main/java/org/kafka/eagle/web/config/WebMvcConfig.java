/**
 * WebMvcConfig.java
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
package org.kafka.eagle.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * <p>
 * Web MVC配置类，负责配置静态资源映射
 * 配置CSS、JS、图片、字体等静态资源的访问路径
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/27 01:28:40
 * @version 5.0.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // 配置静态资源映射
                registry.addResourceHandler("/css/**")
                                .addResourceLocations("classpath:/statics/css/");

                registry.addResourceHandler("/js/**")
                                .addResourceLocations("classpath:/statics/js/");

                registry.addResourceHandler("/images/**")
                                .addResourceLocations("classpath:/statics/images/");

                registry.addResourceHandler("/fonts/**")
                                .addResourceLocations("classpath:/statics/fonts/");

                registry.addResourceHandler("/plugins/**")
                                .addResourceLocations("classpath:/statics/plugins/");

                registry.addResourceHandler("/statics/**")
                                .addResourceLocations("classpath:/statics/");
        }
}