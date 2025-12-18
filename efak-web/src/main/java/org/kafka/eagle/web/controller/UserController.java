/**
 * UserController.java
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.kafka.eagle.web.service.UserService;
import org.kafka.eagle.dto.user.UserInfo;

import java.security.SecureRandom;
import java.util.*;

/**
 * <p>
 * 用户管理控制器
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/05 19:59:52
 * @version 5.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 关键逻辑：生成密码应使用强随机数，避免 Random 的可预测性
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * 分页获取所有用户列表
     */
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {

        // 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication != null ? authentication.getName() : null;
        UserInfo currentUser = null;
        boolean isSuperAdmin = false;

        if (currentUsername != null) {
            currentUser = userService.getUserByUsername(currentUsername);
            // 检查是否为超级管理员（admin用户）
            isSuperAdmin = "admin".equals(currentUsername);
        }

        try {
            // 使用UserService进行分页查询
            Map<String, Object> result = userService.getUsersWithPagination(page, size, search, status);

            // 转换UserInfo对象为Map格式，并添加前端需要的字段
            List<UserInfo> userInfoList = (List<UserInfo>) result.get("users");
            List<Map<String, Object>> userList = new ArrayList<>();

            for (UserInfo userInfo : userInfoList) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", userInfo.getId());
                userMap.put("username", userInfo.getUsername());
                userMap.put("roles", userInfo.getRoles());
                userMap.put("status", userInfo.getStatus());
                userMap.put("modifyTime", userInfo.getModifyTime());

                // 根据权限决定是否显示原始密码
                if (isSuperAdmin) {
                    // 超级管理员可以看到所有用户的原始密码
                    userMap.put("originPassword", userInfo.getOriginPassword());
                } else if (currentUser != null && currentUser.getUsername().equals(userInfo.getUsername())) {
                    // 普通管理员只能看到自己的原始密码
                    userMap.put("originPassword", userInfo.getOriginPassword());
                } else {
                    // 其他情况不显示原始密码
                    userMap.put("originPassword", null);
                }

                // 设置显示信息
                String displayRole = "用户";
                String avatar = "/images/user_profile_2.jpg"; // 默认普通用户头像
                String roleType = "user";

                if (userInfo.getRoles() != null && userInfo.getRoles().contains("ROLE_ADMIN")) {
                    displayRole = "管理员";
                    roleType = "admin";
                    avatar = "/images/user_profile.jpg"; // 管理员头像
                }

                userMap.put("displayRole", displayRole);
                userMap.put("role", roleType);
                userMap.put("avatar", avatar);
                userMap.put("lastLogin",
                        userInfo.getModifyTime() != null ? userInfo.getModifyTime().toString() : "从未登录");
                userMap.put("createdAt", userInfo.getModifyTime() != null ? userInfo.getModifyTime().toString() : "未知");

                // 设置状态显示信息
                String statusDisplay = "活跃";
                String statusClass = "bg-green-100 text-green-800";
                String statusIcon = "fa-check-circle";
                String statusType = "active";

                if (userInfo.getStatus() != null) {
                    switch (userInfo.getStatus().toUpperCase()) {
                        case "ACTIVE":
                            statusDisplay = "活跃";
                            statusClass = "bg-green-100 text-green-800";
                            statusIcon = "fa-check-circle";
                            statusType = "active";
                            break;
                        case "INACTIVE":
                            statusDisplay = "非活跃";
                            statusClass = "bg-gray-100 text-gray-800";
                            statusIcon = "fa-times-circle";
                            statusType = "inactive";
                            break;
                        case "LOCKED":
                            statusDisplay = "已锁定";
                            statusClass = "bg-red-100 text-red-800";
                            statusIcon = "fa-lock";
                            statusType = "locked";
                            break;
                        default:
                            statusDisplay = "未知";
                            statusClass = "bg-yellow-100 text-yellow-800";
                            statusIcon = "fa-question-circle";
                            statusType = "unknown";
                            break;
                    }
                }

                userMap.put("statusDisplay", statusDisplay);
                userMap.put("status", statusType);
                userMap.put("statusClass", statusClass);
                userMap.put("statusIcon", statusIcon);

                // 权限控制：只有admin用户才能编辑和删除其他用户
                boolean canEdit = false;
                boolean canDelete = false;

                if (isSuperAdmin) {
                    // admin用户可以编辑和删除除自己以外的所有用户
                    canEdit = !"admin".equals(userInfo.getUsername());
                    canDelete = !"admin".equals(userInfo.getUsername());
                } else {
                    // 非admin用户只能查看，不能编辑和删除
                    canEdit = false;
                    canDelete = false;
                }

                userMap.put("canEdit", canEdit);
                userMap.put("canDelete", canDelete);

                userList.add(userMap);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("users", userList);
            response.put("total", result.get("total"));
            response.put("page", result.get("page"));
            response.put("size", result.get("size"));
            response.put("totalPages", result.get("totalPages"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取用户列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Integer id) {
        try {
            UserInfo userInfo = userService.getUserById(id.longValue());
            if (userInfo == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "用户不存在");
                return ResponseEntity.notFound().build();
            }

            // 转换为前端需要的格式
            Map<String, Object> userMap = convertUserInfoToMap(userInfo);
            return ResponseEntity.ok(userMap);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取用户信息失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 创建新用户
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> userData) {
        try {
            // 验证必填字段
            if (!userData.containsKey("username")) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "缺少必填字段");
                return ResponseEntity.badRequest().body(error);
            }

            // 检查用户名是否已存在
            UserInfo existingUser = userService.getUserByUsername((String) userData.get("username"));
            if (existingUser != null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "用户名已存在");
                return ResponseEntity.badRequest().body(error);
            }

            // 生成随机8位密码
            String randomPassword = generateRandomPassword();

            // 对密码进行BCrypt加密
            String encryptedPassword = org.springframework.security.crypto.bcrypt.BCrypt.hashpw(randomPassword,
                    org.springframework.security.crypto.bcrypt.BCrypt.gensalt());

            // 创建新用户
            UserInfo newUser = new UserInfo();
            newUser.setUsername((String) userData.get("username"));
            newUser.setPassword(encryptedPassword); // 使用加密后的密码
            newUser.setOriginPassword(randomPassword); // 保存原始密码
            newUser.setRoles((String) userData.getOrDefault("roles", "ROLE_USER"));
            newUser.setStatus((String) userData.getOrDefault("status", "ACTIVE"));

            boolean success = userService.insertUser(newUser);
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "用户创建成功");
                response.put("user", convertUserInfoToMap(newUser));
                response.put("generatedPassword", randomPassword); // 返回生成的密码
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "用户创建失败");
                return ResponseEntity.status(500).body(error);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "创建用户失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> userData) {

        try {
            UserInfo existingUser = userService.getUserById(id.longValue());
            if (existingUser == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "用户不存在");
                return ResponseEntity.notFound().build();
            }

            // 检查用户名是否已被其他用户使用
            if (userData.containsKey("username")) {
                UserInfo userWithSameName = userService.getUserByUsername((String) userData.get("username"));
                if (userWithSameName != null && !userWithSameName.getId().equals(existingUser.getId())) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "用户名已存在");
                    return ResponseEntity.badRequest().body(error);
                }
            }

            // 更新用户信息
            if (userData.containsKey("username")) {
                existingUser.setUsername((String) userData.get("username"));
            }
            if (userData.containsKey("roles")) {
                existingUser.setRoles((String) userData.get("roles"));
            }
            if (userData.containsKey("status")) {
                existingUser.setStatus((String) userData.get("status"));
            }

            boolean success = userService.updateUser(existingUser);
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "用户更新成功");
                response.put("user", convertUserInfoToMap(existingUser));
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "用户更新失败");
                return ResponseEntity.status(500).body(error);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "更新用户失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Integer id) {
        try {
            UserInfo userInfo = userService.getUserById(id.longValue());
            if (userInfo == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "用户不存在");
                return ResponseEntity.notFound().build();
            }

            // 不允许删除admin用户
            if ("admin".equals(userInfo.getUsername())) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "不能删除管理员用户");
                return ResponseEntity.badRequest().body(error);
            }

            boolean success = userService.deleteUserById(id.longValue());
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "用户删除成功");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "用户删除失败");
                return ResponseEntity.status(500).body(error);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "删除用户失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 重置用户密码
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable Integer id,
            @RequestBody(required = false) Map<String, Object> requestBody) {
        try {
            log.info("reset user_id: {}", id);
            UserInfo userInfo = userService.getUserById(id.longValue());
            if (userInfo == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "用户不存在");
                return ResponseEntity.notFound().build();
            }

            // 获取当前登录用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication != null ? authentication.getName() : null;

            // 验证当前用户是否有权限修改密码
            if (currentUsername == null || !currentUsername.equals(userInfo.getUsername())) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "无权限修改其他用户的密码");
                return ResponseEntity.status(403).body(error);
            }

            String newPassword;
            String message;

            if (requestBody != null && requestBody.containsKey("currentPassword")
                    && requestBody.containsKey("newPassword")) {
                // 修改密码模式
                String currentPassword = (String) requestBody.get("currentPassword");
                newPassword = (String) requestBody.get("newPassword");

                // 验证当前密码
                if (!userService.verifyPassword(userInfo, currentPassword)) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "当前密码不正确");
                    return ResponseEntity.badRequest().body(error);
                }

                // 验证新密码复杂度
                if (newPassword.length() < 8) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "新密码长度至少8位");
                    return ResponseEntity.badRequest().body(error);
                }

                message = "密码修改成功";
            } else {
                // 重置密码模式（管理员操作）
                newPassword = generateRandomPassword();
                message = "密码重置成功";
            }

            // 对密码进行BCrypt加密
            String encryptedPassword = org.springframework.security.crypto.bcrypt.BCrypt.hashpw(newPassword,
                    org.springframework.security.crypto.bcrypt.BCrypt.gensalt());

            userInfo.setPassword(encryptedPassword);
            userInfo.setOriginPassword(newPassword);

            boolean success = userService.resetPassword(userInfo);
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", message);
                if (requestBody == null || !requestBody.containsKey("newPassword")) {
                    response.put("newPassword", newPassword);
                }
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "密码操作失败");
                return ResponseEntity.status(500).body(error);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "密码操作失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        try {
            Map<String, Object> result = userService.getUsersWithPagination(1, Integer.MAX_VALUE, null, null);
            List<UserInfo> allUsers = (List<UserInfo>) result.get("users");

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", allUsers.size());
            stats.put("activeUsers", allUsers.stream()
                    .filter(u -> "ACTIVE".equals(u.getStatus()))
                    .count());
            stats.put("adminUsers", allUsers.stream()
                    .filter(u -> u.getRoles() != null && u.getRoles().contains("ROLE_ADMIN"))
                    .count());
            stats.put("newUsers", allUsers.stream()
                    .filter(u -> {
                        if (u.getModifyTime() == null)
                            return false;
                        // 简单判断：如果修改时间在最近30天内，认为是新用户
                        return u.getModifyTime().isAfter(java.time.LocalDateTime.now().minusDays(30));
                    })
                    .count());

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 生成随机密码
     */
    private String generateRandomPassword() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(PASSWORD_CHARS.charAt(SECURE_RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * 将UserInfo转换为Map格式
     */
    private Map<String, Object> convertUserInfoToMap(UserInfo userInfo) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", userInfo.getId());
        userMap.put("username", userInfo.getUsername());
        userMap.put("roles", userInfo.getRoles());
        userMap.put("status", userInfo.getStatus());
        userMap.put("modifyTime", userInfo.getModifyTime());

        // 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication != null ? authentication.getName() : null;
        boolean isSuperAdmin = "admin".equals(currentUsername);

        // 根据权限决定是否显示原始密码
        if (isSuperAdmin) {
            // 超级管理员可以看到所有用户的原始密码
            userMap.put("originPassword", userInfo.getOriginPassword());
        } else if (currentUsername != null && currentUsername.equals(userInfo.getUsername())) {
            // 普通管理员只能看到自己的原始密码
            userMap.put("originPassword", userInfo.getOriginPassword());
        } else {
            // 其他情况不显示原始密码
            userMap.put("originPassword", null);
        }

        // 设置显示信息
        String displayRole = "用户";
        String avatar = "/images/user_profile_2.jpg"; // 默认普通用户头像
        String roleType = "user";

        if (userInfo.getRoles() != null && userInfo.getRoles().contains("ROLE_ADMIN")) {
            displayRole = "管理员";
            roleType = "admin";
            avatar = "/images/user_profile.jpg"; // 管理员头像
        }

        userMap.put("displayRole", displayRole);
        userMap.put("role", roleType);
        userMap.put("avatar", avatar);
        userMap.put("lastLogin",
                userInfo.getModifyTime() != null ? userInfo.getModifyTime().toString() : "从未登录");
        userMap.put("createdAt", userInfo.getModifyTime() != null ? userInfo.getModifyTime().toString() : "未知");

        // 设置状态显示信息
        String statusDisplay = "活跃";
        String statusClass = "bg-green-100 text-green-800";
        String statusIcon = "fa-check-circle";
        String statusType = "active";

        if (userInfo.getStatus() != null) {
            switch (userInfo.getStatus().toUpperCase()) {
                case "ACTIVE":
                    statusDisplay = "活跃";
                    statusClass = "bg-green-100 text-green-800";
                    statusIcon = "fa-check-circle";
                    statusType = "active";
                    break;
                case "INACTIVE":
                    statusDisplay = "非活跃";
                    statusClass = "bg-gray-100 text-gray-800";
                    statusIcon = "fa-times-circle";
                    statusType = "inactive";
                    break;
                case "LOCKED":
                    statusDisplay = "已锁定";
                    statusClass = "bg-red-100 text-red-800";
                    statusIcon = "fa-lock";
                    statusType = "locked";
                    break;
                default:
                    statusDisplay = "未知";
                    statusClass = "bg-yellow-100 text-yellow-800";
                    statusIcon = "fa-question-circle";
                    statusType = "unknown";
                    break;
            }
        }

        userMap.put("statusDisplay", statusDisplay);
        userMap.put("status", statusType);
        userMap.put("statusClass", statusClass);
        userMap.put("statusIcon", statusIcon);

        // 权限控制：只有admin用户才能编辑和删除其他用户
        boolean canEdit = false;
        boolean canDelete = false;

        if (isSuperAdmin) {
            // admin用户可以编辑和删除除自己以外的所有用户
            canEdit = !"admin".equals(userInfo.getUsername());
            canDelete = !"admin".equals(userInfo.getUsername());
        } else {
            // 非admin用户只能查看，不能编辑和删除
            canEdit = false;
            canDelete = false;
        }

        userMap.put("canEdit", canEdit);
        userMap.put("canDelete", canDelete);

        return userMap;
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 获取当前认证信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("success", false);
                response.put("message", "用户未登录");
                return ResponseEntity.status(401).body(response);
            }

            String username = authentication.getName();
            UserInfo userInfo = userService.getUserByUsername(username);

            if (userInfo == null) {
                response.put("success", false);
                response.put("message", "用户信息不存在");
                return ResponseEntity.status(404).body(response);
            }

            // 构建用户信息响应
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", userInfo.getId());
            userData.put("username", userInfo.getUsername());
            userData.put("roles", userInfo.getRoles());
            userData.put("modifyTime", userInfo.getModifyTime());
            userData.put("status", userInfo.getStatus());

            // 当前用户总是可以看到自己的原始密码
            userData.put("originPassword", userInfo.getOriginPassword());

            // 根据角色设置显示信息
            String displayName = userInfo.getUsername();
            String displayRole = "用户";
            String email = username + "@efak.ai";
            String avatar = "/images/user_profile_2.jpg"; // 默认普通用户头像

            if (userInfo.getRoles() != null && userInfo.getRoles().contains("ROLE_ADMIN")) {
                displayRole = "管理员";
                email = "admin@efak.ai";
                avatar = "/images/user_profile.jpg"; // 管理员头像
            }

            // 设置状态显示信息
            String statusDisplay = "活跃";
            String statusClass = "bg-green-100 text-green-800";
            String statusIcon = "fa-check-circle";

            if (userInfo.getStatus() != null) {
                switch (userInfo.getStatus().toUpperCase()) {
                    case "ACTIVE":
                        statusDisplay = "活跃";
                        statusClass = "bg-green-100 text-green-800";
                        statusIcon = "fa-check-circle";
                        break;
                    case "INACTIVE":
                        statusDisplay = "非活跃";
                        statusClass = "bg-gray-100 text-gray-800";
                        statusIcon = "fa-times-circle";
                        break;
                    case "LOCKED":
                        statusDisplay = "已锁定";
                        statusClass = "bg-red-100 text-red-800";
                        statusIcon = "fa-lock";
                        break;
                    default:
                        statusDisplay = "未知";
                        statusClass = "bg-yellow-100 text-yellow-800";
                        statusIcon = "fa-question-circle";
                        break;
                }
            }

            userData.put("displayName", displayName);
            userData.put("displayRole", displayRole);
            userData.put("email", email);
            userData.put("avatar", avatar);
            userData.put("lastLoginTime",
                    userInfo.getModifyTime() != null ? userInfo.getModifyTime().toString() : "未知");
            userData.put("statusDisplay", statusDisplay);
            userData.put("statusClass", statusClass);
            userData.put("statusIcon", statusIcon);

            response.put("success", true);
            response.put("message", "获取用户信息成功");
            response.put("data", userData);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取用户信息失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}
