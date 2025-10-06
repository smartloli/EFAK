/**
 * UserInfo.java
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
package org.kafka.eagle.dto.user;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户信息 DTO 类，用于存储用户的基本信息和状态。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/6/28 00:02:03
 * @version 5.0.0
 */
@Data
public class UserInfo implements Serializable {

    /**
     * 用户 ID，自增主键
     */
    private Long id;

    /**
     * 用户名，默认长度 20
     */
    private String username;

    /**
     * 密码，默认长度 100
     */
    private String password;

    /**
     * 原始密码，默认长度 100
     */
    private String originPassword;

    /**
     * 角色，如 ADMIN、DEV、TEST
     */
    private String roles;

    /**
     * 用户状态：ACTIVE、INACTIVE、LOCKED
     */
    private String status = "ACTIVE";

    /**
     * 用户修改时间
     */
    private LocalDateTime modifyTime = LocalDateTime.now();

}