/**
 * KeConst.java
 * <p>
 * Copyright 2025 smartloli
 * <p>
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
package org.kafka.eagle.tool.constant;

/**
 * <p>
 * 应用程序常量的枚举定义。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/6/22 23:13:40
 * @version 5.0.0
 */
public enum KeConst {

    /** 应用版本常量 */
    APP_VERSION("5.0.0", "当前应用版本"),
    
    /** 最大重试次数的配置键 */
    MAX_RETRY_ATTEMPTS("max.retry.attempts", "3", "操作的最大重试次数"),
    
    /** 默认超时时间（毫秒） */
    DEFAULT_TIMEOUT("default.timeout", "30000", "操作的默认超时时间（毫秒）");

    private final String key;
    private final String value;
    private final String description;

    /**
     * KeConst 枚举构造器。
     * 
     * @param key         配置键
     * @param value       常量值
     * @param description 常量的描述
     */
    KeConst(String key, String value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;
    }

    /**
     * 带默认值的 KeConst 枚举构造器。
     * 
     * @param value       常量值
     * @param description 常量的描述
     */
    KeConst(String value, String description) {
        this(null, value, description);
    }

    /**
     * 获取该常量关联的配置键。
     * 
     * @return 配置键
     */
    public String getKey() {
        return key;
    }

    /**
     * 获取常量值。
     * 
     * @return 常量值
     */
    public String getValue() {
        return value;
    }

    /**
     * 获取常量描述。
     * 
     * @return 常量描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 通过键获取常量。
     * 
     * @param key 要查找的键
     * @return 对应的 KeConst；如果未找到则返回 null
     */
    public static KeConst getByKey(String key) {
        for (KeConst constant : values()) {
            if (key != null && key.equals(constant.getKey())) {
                return constant;
            }
        }
        return null;
    }
}
