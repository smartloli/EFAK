/**
 * ClusterIdUtils.java
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
package org.kafka.eagle.tool.util;

import java.security.SecureRandom;

/**
 * <p>
 * 使用 Base64 编码生成唯一的 16 位集群 ID。
 * 示例："HGRyVSsJjRNEsPm8"
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/8/31 00:57:30
 * @version 5.0.0
 */
public class ClusterIdGenerator {
    private static final char[] ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final int LENGTH = 16;
    private static final SecureRandom RNG = new SecureRandom();

    private ClusterIdGenerator() {
    }

    /**
     * 生成 16 位 Base64 集群ID（如：A7f2K9...）。
     */
    public static String generate() {
        char[] buf = new char[LENGTH];
        for (int i = 0; i < LENGTH; i++) {
            buf[i] = ALPHABET[RNG.nextInt(ALPHABET.length)];
        }
        return new String(buf);
    }

}
