/**
 * UserDaoServiceImpl.java
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

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.pojo.user.UserInfo;
import org.kafka.eagle.web.dao.mapper.UserDaoMapper;
import org.kafka.eagle.web.service.IUserDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Description: TODO
 * @Author: smartloli
 * @Date: 2023/6/28 23:14
 * @Version: 3.4.0
 */
@Slf4j
@Service
public class UserDaoServiceImpl extends ServiceImpl<UserDaoMapper, UserInfo> implements IUserDaoService {

    @Autowired
    private UserDaoMapper userDaoMapper;

    @Override
    public List<UserInfo> list(){
        return new LambdaQueryChainWrapper<>(this.userDaoMapper).list();
    }

    @Override
    public UserInfo users(String username) {
        return new LambdaQueryChainWrapper<>(this.userDaoMapper).eq(UserInfo::getUsername, username).one();
    }

    @Override
    public UserInfo users(Long id) {
        return null;
    }

    @Override
    public boolean insert(UserInfo userInfo) {
        return false;
    }

    @Override
    public Page<UserInfo> pages(Map<String, Object> params) {
        return null;
    }

    @Override
    public boolean update(UserInfo userInfo) {
        return false;
    }

    @Override
    public boolean delete(UserInfo userInfo) {
        return false;
    }
}
