/**
 * AppConfig.java
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

import org.springframework.context.annotation.Configuration;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/8/26 21:12
 * @Version: 3.4.0
 */
@Configuration
public class AppConfig {
    static {
        String osName = System.getProperty("os.name");
        if (osName.contains("Windows") || osName.contains("Mac")) {
            System.setProperty("spring.config.location", "classpath:config/application.properties");
        } else {
            System.setProperty("spring.config.location", System.getProperty("user.dir") + "/config/application.properties");
        }
    }
}
