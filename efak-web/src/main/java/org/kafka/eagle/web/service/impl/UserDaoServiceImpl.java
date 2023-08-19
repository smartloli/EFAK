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
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
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
 *
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
    public List<UserInfo> list() {
        return new LambdaQueryChainWrapper<>(this.userDaoMapper).list();
    }

    @Override
    public UserInfo users(String username) {
        return new LambdaQueryChainWrapper<>(this.userDaoMapper).eq(UserInfo::getUsername, username).one();
    }

    @Override
    public UserInfo users(String username, String password) {
        return new LambdaQueryChainWrapper<>(this.userDaoMapper).eq(UserInfo::getUsername, username).eq(UserInfo::getOriginPassword, password).one();
    }

    @Override
    public UserInfo users(Long id) {
        return null;
    }

    @Override
    public boolean insert(UserInfo userInfo) {
        boolean status = false;
        int code = this.userDaoMapper.insert(userInfo);
        if (code > 0) {
            status = true;
        }
        return status;
    }

    @Override
    public Page<UserInfo> pages(Map<String, Object> params) {

        int start = Integer.parseInt(params.get("start").toString());
        int size = Integer.parseInt(params.get("size").toString());

        Page<UserInfo> pages = new Page<>(start, size);
        LambdaQueryChainWrapper<UserInfo> queryChainWrapper = new LambdaQueryChainWrapper<UserInfo>(this.userDaoMapper);
        queryChainWrapper.like(UserInfo::getUsername, params.get("search").toString());
        return queryChainWrapper.page(pages);
    }

    @Override
    public boolean update(UserInfo userInfo) {
        LambdaUpdateChainWrapper<UserInfo> lambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<UserInfo>(this.userDaoMapper);
        lambdaUpdateChainWrapper.eq(UserInfo::getId, userInfo.getId());
        return lambdaUpdateChainWrapper.update(userInfo);
    }

    @Override
    public boolean reset(UserInfo userInfo) {
        LambdaUpdateChainWrapper<UserInfo> lambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<UserInfo>(this.userDaoMapper);
        lambdaUpdateChainWrapper.eq(UserInfo::getUsername, userInfo.getUsername());
        return lambdaUpdateChainWrapper.update(userInfo);
    }

    @Override
    public boolean delete(UserInfo userInfo) {
        return false;
    }

    @Override
    public boolean delete(Long id) {
        return new LambdaUpdateChainWrapper<>(this.userDaoMapper).eq(UserInfo::getId, id).remove();
    }
}
