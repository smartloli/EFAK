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
import org.kafka.eagle.dto.user.UserInfo;
import org.kafka.eagle.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin-only BCrypt helper for user password hashes.
 */
@Slf4j
@RestController
@RequestMapping("/api/password")
@PreAuthorize("hasRole('ADMIN')")
public class PasswordController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @PostMapping("/encode")
    public Map<String, Object> encodePassword(@RequestParam String password) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (password == null || password.isBlank()) {
                result.put("success", false);
                result.put("message", "密码不能为空");
                return result;
            }
            result.put("success", true);
            result.put("message", "加密成功");
            result.put("encodedPassword", passwordEncoder.encode(password));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "加密失败");
            log.error("密码加密失败", e);
        }
        return result;
    }

    @PostMapping("/verify")
    public Map<String, Object> verifyPassword(@RequestParam String originalPassword,
            @RequestParam String encodedPassword) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean matches = passwordEncoder.matches(originalPassword, encodedPassword);
            result.put("success", true);
            result.put("matches", matches);
            result.put("message", matches ? "密码匹配" : "密码不匹配");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "校验失败");
            log.error("密码校验失败", e);
        }
        return result;
    }

    @PostMapping("/generate-multiple")
    public Map<String, Object> generateMultiplePasswords(@RequestParam String password,
            @RequestParam(defaultValue = "3") int count) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (password == null || password.isBlank()) {
                result.put("success", false);
                result.put("message", "密码不能为空");
                return result;
            }
            int finalCount = Math.min(Math.max(count, 1), 10);
            String[] encodedPasswords = new String[finalCount];
            for (int i = 0; i < finalCount; i++) {
                encodedPasswords[i] = passwordEncoder.encode(password);
            }
            result.put("success", true);
            result.put("message", "已生成 " + finalCount + " 个哈希");
            result.put("encodedPasswords", encodedPasswords);
            result.put("count", finalCount);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "生成失败");
            log.error("批量加密失败", e);
        }
        return result;
    }

    @GetMapping("/accounts")
    public Map<String, Object> listAccounts() {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> page = userService.getUsersWithPagination(1, 200, null, null);
            @SuppressWarnings("unchecked")
            List<UserInfo> users = (List<UserInfo>) page.get("users");
            List<Map<String, Object>> items = new ArrayList<>();
            if (users != null) {
                for (UserInfo user : users) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", user.getId());
                    item.put("username", user.getUsername());
                    boolean admin = user.getRoles() != null && user.getRoles().contains("ROLE_ADMIN");
                    item.put("role", admin ? "管理员" : "用户");
                    items.add(item);
                }
            }
            result.put("success", true);
            result.put("users", items);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取用户列表失败");
            log.error("获取可重置账号失败", e);
        }
        return result;
    }

    @PostMapping("/reset-user")
    public Map<String, Object> resetUserPassword(@RequestParam Long userId, @RequestParam String password) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (password == null || password.isBlank()) {
                result.put("success", false);
                result.put("message", "密码不能为空");
                return result;
            }
            if (password.length() < 8) {
                result.put("success", false);
                result.put("message", "密码长度至少 8 位");
                return result;
            }
            UserInfo user = userService.getUserById(userId);
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }
            user.setPassword(passwordEncoder.encode(password));
            user.setOriginPassword(password);
            boolean ok = userService.resetPassword(user);
            result.put("success", ok);
            result.put("username", user.getUsername());
            result.put("message", ok ? "已重置用户 " + user.getUsername() + " 的密码" : "重置失败");
            if (ok) {
                log.info("管理员重置用户密码: {}", user.getUsername());
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "重置失败");
            log.error("管理员重置用户密码失败, userId={}", userId, e);
        }
        return result;
    }
}
