/**
 * UserServiceImpl.java
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
package org.kafka.eagle.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.user.UserInfo;
import org.kafka.eagle.web.mapper.UserMapper;
import org.kafka.eagle.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: 用户服务实现类
 *
 * @Author: smartloli
 * @Date: 2023/6/28 23:14
 * @Version: 3.4.0
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<UserInfo> list() {
        // 由于简化了Mapper，这里返回空列表或默认用户
        List<UserInfo> users = new ArrayList<>();
        UserInfo defaultUser = getDefaultAdminUser();
        if (defaultUser != null) {
            users.add(defaultUser);
        }
        return users;
    }

    @Override
    public UserInfo getUserByUsername(String username) {
        try {
            // 优先从数据库查询
            UserInfo user = userMapper.findByUsername(username);
            if (user != null) {
                return user;
            }
        } catch (Exception e) {
            log.warn("数据库查询失败: {}", e.getMessage());
        }

        // 如果数据库中没有找到，返回默认用户
        if ("admin".equals(username)) {
            log.info("使用默认用户: {}", username);
            return getDefaultAdminUser();
        }

        return null;
    }

    @Override
    public UserInfo getUserByUsernameAndPassword(String username, String password) {
        try {
            return userMapper.findByUsernameAndPassword(username, password);
        } catch (Exception e) {
            log.warn("数据库查询失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public UserInfo getUserById(Long id) {
        try {
            return userMapper.findById(id);
        } catch (Exception e) {
            log.warn("数据库查询失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean insertUser(UserInfo userInfo) {
        try {
            log.info("插入用户: {}", userInfo.getUsername());

            // 设置默认值
            if (userInfo.getStatus() == null) {
                userInfo.setStatus("ACTIVE");
            }
            if (userInfo.getRoles() == null) {
                userInfo.setRoles("ROLE_USER");
            }
            if (userInfo.getModifyTime() == null) {
                userInfo.setModifyTime(java.time.LocalDateTime.now());
            }

            // 调用Mapper插入数据库
            int result = userMapper.insertUser(userInfo);

            if (result > 0) {
                return true;
            } else {
                log.warn("插入用户失败，影响行数为0: {}", userInfo.getUsername());
                return false;
            }
        } catch (Exception e) {
            log.error("插入用户失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean updateUser(UserInfo userInfo) {
        try {

            // 设置修改时间
            userInfo.setModifyTime(java.time.LocalDateTime.now());

            // 调用Mapper更新数据库
            int result = userMapper.updateUser(userInfo);

            if (result > 0) {
                return true;
            } else {
                log.warn("更新用户信息失败，影响行数为0: {}", userInfo.getUsername());
                return false;
            }
        } catch (Exception e) {
            log.error("更新用户信息失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean resetPassword(UserInfo userInfo) {
        try {
            log.info("重置用户密码: {}", userInfo.getUsername());

            // 设置修改时间
            userInfo.setModifyTime(java.time.LocalDateTime.now());

            // 调用Mapper更新密码
            int result = userMapper.updatePassword(userInfo);

            if (result > 0) {
                return true;
            } else {
                log.warn("重置密码失败，影响行数为0: {}", userInfo.getUsername());
                return false;
            }
        } catch (Exception e) {
            log.error("重置密码失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean deleteUser(UserInfo userInfo) {
        try {

            // 调用Mapper删除用户
            int result = userMapper.deleteUserById(userInfo.getId());

            if (result > 0) {
                return true;
            } else {
                log.warn("删除用户失败，影响行数为0: {}", userInfo.getUsername());
                return false;
            }
        } catch (Exception e) {
            log.error("删除用户失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean deleteUserById(Long id) {
        try {

            // 调用Mapper删除用户
            int result = userMapper.deleteUserById(id);

            if (result > 0) {
                return true;
            } else {
                log.warn("删除用户失败，影响行数为0: ID={}", id);
                return false;
            }
        } catch (Exception e) {
            log.error("删除用户失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getUsersWithPagination(int page, int size, String search, String status) {
        Map<String, Object> result = new HashMap<>();

        try {
            int offset = (page - 1) * size;
            List<UserInfo> users;
            int total;

            // 根据搜索条件和状态过滤
            if (search != null && !search.trim().isEmpty()) {
                users = userMapper.findUsersWithSearch(search.trim(), offset, size);
                total = userMapper.countUsersWithSearch(search.trim());
            } else if (status != null && !status.trim().isEmpty()) {
                users = userMapper.findUsersByStatus(status.trim(), offset, size);
                total = userMapper.countUsersByStatus(status.trim());
            } else {
                users = userMapper.findUsersWithPagination(offset, size);
                total = userMapper.countTotalUsers();
            }

            result.put("users", users);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", (int) Math.ceil((double) total / size));

        } catch (Exception e) {
            log.error("分页查询用户失败: {}", e.getMessage(), e);
            // 返回空结果
            result.put("users", new ArrayList<>());
            result.put("total", 0);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", 0);
        }

        return result;
    }

    @Override
    public boolean verifyPassword(UserInfo userInfo, String password) {
        try {
            // 使用BCrypt验证密码
            return org.springframework.security.crypto.bcrypt.BCrypt.checkpw(password, userInfo.getPassword());
        } catch (Exception e) {
            log.error("密码验证失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取默认管理员用户
     */
    private UserInfo getDefaultAdminUser() {
        UserInfo defaultUser = new UserInfo();
        defaultUser.setId(1L);
        defaultUser.setUsername("admin");
        // 使用BCrypt加密的密码 "admin"
        defaultUser.setPassword("$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi");
        defaultUser.setOriginPassword("admin");
        defaultUser.setRoles("ROLE_ADMIN");
        defaultUser.setStatus("ACTIVE");
        return defaultUser;
    }
}