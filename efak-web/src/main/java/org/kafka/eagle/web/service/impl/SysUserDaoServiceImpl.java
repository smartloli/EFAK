/**
 * SysUserDaoServiceImpl.java
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

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.pojo.user.UserInfo;
import org.kafka.eagle.web.service.ISysUserDaoService;
import org.kafka.eagle.web.service.IUserDaoService;
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
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/6/28 23:34
 * @Version: 3.4.0
 */
@Slf4j
@Service
public class SysUserDaoServiceImpl implements ISysUserDaoService {

    @Autowired
    private IUserDaoService userDaoService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // get user info by username
        UserInfo userInfo = userDaoService.users(username);
        if (userInfo == null) {
            return null;
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        // get user roles
        String roles = userInfo.getRoles();
        if (StrUtil.isNotBlank(roles)) {
            String[] roleArr = roles.split(",");
            for (String role : roleArr) {
                GrantedAuthority auth = new SimpleGrantedAuthority(role);
                authorities.add(auth);
            }
        }

//        List<GrantedAuthority> authorities = new ArrayList<>();
//        GrantedAuthority auth = new SimpleGrantedAuthority("ROLE_ROOT");
//        authorities.add(auth);
//        GrantedAuthority auth2 = new SimpleGrantedAuthority("ROLE_TEST");
//        authorities.add(auth2);
        //UserDetails user = new User(s,"{noop}aaa",authorities);
        UserDetails user = new User(
                userInfo.getUsername(),
                userInfo.getPasswd(),
                true,
                true,
                true, // true: credentials not expired
                true, // user not locked
                authorities);

        return user;
    }
}
