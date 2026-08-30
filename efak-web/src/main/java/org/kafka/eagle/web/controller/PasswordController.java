/**
 * PasswordController.java
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
package org.kafka.eagle.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: 密码工具控制器
 *
 * @Author: smartloli
 * @Date: 2025/1/1 00:00
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/password")
public class PasswordController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 生成BCrypt加密密码
     * 
     * @param password 明文密码
     * @return 加密结果
     */
    @PostMapping("/encode")
    public Map<String, Object> encodePassword(@RequestParam String password) {
        Map<String, Object> result = new HashMap<>();

        try {
            String encodedPassword = passwordEncoder.encode(password);

            result.put("success", true);
            result.put("message", "密码加密成功");
            result.put("data", new HashMap<String, String>() {
                {
                    put("originalPassword", password);
                    put("encodedPassword", encodedPassword);
                }
            });

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "密码加密失败: " + e.getMessage());
            log.error("密码加密失败", e);
        }

        return result;
    }

    /**
     * 验证密码是否匹配
     * 
     * @param originalPassword 原始密码
     * @param encodedPassword  加密密码
     * @return 验证结果
     */
    @PostMapping("/verify")
    public Map<String, Object> verifyPassword(@RequestParam String originalPassword,
            @RequestParam String encodedPassword) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean matches = passwordEncoder.matches(originalPassword, encodedPassword);

            result.put("success", true);
            result.put("message", matches ? "密码验证成功" : "密码验证失败");
            result.put("data", new HashMap<String, Object>() {
                {
                    put("originalPassword", originalPassword);
                    put("encodedPassword", encodedPassword);
                    put("matches", matches);
                }
            });

            log.info("密码验证: {} 与 {} 匹配结果: {}", originalPassword, encodedPassword, matches);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "密码验证失败: " + e.getMessage());
            log.error("密码验证失败", e);
        }

        return result;
    }

    /**
     * 生成多个加密密码（用于测试）
     * 
     * @param password 明文密码
     * @param count    生成数量
     * @return 多个加密密码
     */
    @PostMapping("/generate-multiple")
    public Map<String, Object> generateMultiplePasswords(@RequestParam String password,
            @RequestParam(defaultValue = "5") int count) {
        Map<String, Object> result = new HashMap<>();

        try {
            final int finalCount = (count <= 0 || count > 20) ? 5 : count; // 限制生成数量

            String[] encodedPasswords = new String[finalCount];
            for (int i = 0; i < finalCount; i++) {
                encodedPasswords[i] = passwordEncoder.encode(password);
            }

            result.put("success", true);
            result.put("message", "成功生成 " + finalCount + " 个加密密码");
            result.put("data", new HashMap<String, Object>() {
                {
                    put("originalPassword", password);
                    put("encodedPasswords", encodedPasswords);
                    put("count", finalCount);
                }
            });

            log.info("生成多个密码: {} -> {} 个", password, finalCount);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "生成密码失败: " + e.getMessage());
            log.error("生成多个密码失败", e);
        }

        return result;
    }

    /**
     * 获取默认admin用户的加密密码
     * 
     * @return 默认密码信息
     */
    @GetMapping("/default-admin")
    public Map<String, Object> getDefaultAdminPassword() {
        Map<String, Object> result = new HashMap<>();

        try {
            String originalPassword = "admin";
            String encodedPassword = passwordEncoder.encode(originalPassword);

            result.put("success", true);
            result.put("message", "获取默认admin密码成功");
            result.put("data", new HashMap<String, String>() {
                {
                    put("username", "admin");
                    put("originalPassword", originalPassword);
                    put("encodedPassword", encodedPassword);
                    put("sqlInsert",
                            "INSERT INTO ke_users_info (username, password, origin_password, roles) VALUES ('admin', '"
                                    + encodedPassword + "', 'admin', 'ROLE_ADMIN');");
                }
            });

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取默认密码失败: " + e.getMessage());
            log.error("获取默认admin密码失败", e);
        }

        return result;
    }
}