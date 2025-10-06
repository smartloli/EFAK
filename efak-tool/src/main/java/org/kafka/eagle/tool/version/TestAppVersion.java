/**
 * FigletFont2.java
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
package org.kafka.eagle.tool.version;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 测试 EFAK 版本。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/6/22 23:48:59
 * @version 5.0.0
 */
@Slf4j
public class TestAppVersion {
    public static void main(String[] args) {
        KafkaEagleVersion.version();
    }
}
