/**
 * SysUserServiceImpl.java
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

import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.user.UserInfo;
import org.kafka.eagle.web.service.SysUserService;
import org.kafka.eagle.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: 系统用户服务实现类
 *
 * @Author: smartloli
 * @Date: 2023/6/28 23:34
 * @Version: 3.4.0
 */
@Slf4j
@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名获取用户信息
        UserInfo userInfo = userService.getUserByUsername(username);
        if (userInfo == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        // 获取用户角色
        String roles = userInfo.getRoles();
        if (StringUtils.hasText(roles)) {
            String[] roleArr = roles.split(",");
            for (String role : roleArr) {
                GrantedAuthority auth = new SimpleGrantedAuthority(role);
                authorities.add(auth);
            }
        }

        UserDetails user = new User(
                userInfo.getUsername(),
                userInfo.getPassword(),
                true,
                true,
                true, // credentials not expired
                true, // user not locked
                authorities);

        return user;
    }
}