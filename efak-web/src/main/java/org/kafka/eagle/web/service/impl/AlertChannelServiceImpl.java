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
import org.kafka.eagle.pojo.alert.AlertChannelInfo;
import org.kafka.eagle.web.dao.mapper.AlertChannelDaoMapper;
import org.kafka.eagle.web.service.IAlertChannelDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/8/20 11:53
 * @Version: 3.4.0
 */
@Slf4j
@Service
public class AlertChannelServiceImpl extends ServiceImpl<AlertChannelDaoMapper, AlertChannelInfo> implements IAlertChannelDaoService {

    @Autowired
    private AlertChannelDaoMapper alertChannelDaoMapper;

    @Override
    public AlertChannelInfo channel(Long id) {
        return new LambdaQueryChainWrapper<>(this.alertChannelDaoMapper).eq(AlertChannelInfo::getId, id).one();
    }

    @Override
    public boolean insert(AlertChannelInfo alertChannelInfo) {
        boolean status = false;
        int code = this.alertChannelDaoMapper.insert(alertChannelInfo);
        if (code > 0) {
            status = true;
        }
        return status;
    }

    @Override
    public Page<AlertChannelInfo> pages(Map<String, Object> params) {
        int start = Integer.parseInt(params.get("start").toString());
        int size = Integer.parseInt(params.get("size").toString());
        String cid = params.get("cid").toString();
        String roles = params.get("roles").toString();

        Page<AlertChannelInfo> pages = new Page<>(start, size);
        LambdaQueryChainWrapper<AlertChannelInfo> queryChainWrapper = new LambdaQueryChainWrapper<AlertChannelInfo>(this.alertChannelDaoMapper);
        if(!roles.equals("ROLE_ADMIN")){
            queryChainWrapper.eq(AlertChannelInfo::getClusterId, cid).eq(AlertChannelInfo::getChannelUserRoles, roles).like(AlertChannelInfo::getChannelName, params.get("search").toString());
        }else{
            queryChainWrapper.eq(AlertChannelInfo::getClusterId, cid).like(AlertChannelInfo::getChannelName, params.get("search").toString());
        }

        return queryChainWrapper.page(pages);
    }

    @Override
    public boolean update(AlertChannelInfo alertChannelInfo) {
        LambdaUpdateChainWrapper<AlertChannelInfo> lambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<AlertChannelInfo>(this.alertChannelDaoMapper);
        lambdaUpdateChainWrapper.eq(AlertChannelInfo::getId, alertChannelInfo.getId());
        return lambdaUpdateChainWrapper.update(alertChannelInfo);
    }

    @Override
    public boolean delete(Long id) {
        return new LambdaUpdateChainWrapper<>(this.alertChannelDaoMapper).eq(AlertChannelInfo::getId, id).remove();
    }
}
