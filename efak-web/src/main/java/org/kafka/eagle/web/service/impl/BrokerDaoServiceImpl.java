/**
 * ClusterDaoServiceImpl.java
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
import org.kafka.eagle.pojo.cluster.BrokerInfo;
import org.kafka.eagle.web.dao.mapper.BrokerDaoMapper;
import org.kafka.eagle.web.service.IBrokerDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @Author: smartloli
 * @Date: 2023/5/28 00:44
 * @Version: 3.4.0
 */
@Service
public class BrokerDaoServiceImpl extends ServiceImpl<BrokerDaoMapper, BrokerInfo> implements IBrokerDaoService {

    @Autowired
    BrokerDaoMapper brokerDaoMapper;

    @Override
    public List<BrokerInfo> list() {
        return new LambdaQueryChainWrapper<>(this.brokerDaoMapper).list();
    }

    @Override
    public List<BrokerInfo> clusters(String clusterId) {
        return new LambdaQueryChainWrapper<>(this.brokerDaoMapper).eq(BrokerInfo::getClusterId, clusterId).list();
    }

    @Override
    public boolean insert(BrokerInfo brokerInfo) {
        boolean status = false;
        int code = this.brokerDaoMapper.insert(brokerInfo);
        if (code > 0) {
            status = true;
        }
        return status;
    }

    @Override
    public Page<BrokerInfo> pages(Map<String, Object> params) {
        int start = Integer.parseInt(params.get("start").toString());
        int size = Integer.parseInt(params.get("size").toString());
        String search = params.get("search").toString();
        String cid = params.get("cid").toString();

        Page<BrokerInfo> pages = new Page<>(start, size);

        LambdaQueryChainWrapper<BrokerInfo> queryChainWrapper = new LambdaQueryChainWrapper<BrokerInfo>(this.brokerDaoMapper);
        queryChainWrapper.like(BrokerInfo::getBrokerHost, search).eq(BrokerInfo::getClusterId, cid);
        return queryChainWrapper.orderByDesc(BrokerInfo::getModifyTime).page(pages);
    }

    @Override
    public boolean update(BrokerInfo brokerInfo) {
        LambdaUpdateChainWrapper<BrokerInfo> lambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<BrokerInfo>(this.brokerDaoMapper);
        lambdaUpdateChainWrapper.eq(BrokerInfo::getClusterId,brokerInfo.getClusterId()).eq(BrokerInfo::getId, brokerInfo.getId());
        return lambdaUpdateChainWrapper.update(brokerInfo);
    }

    @Override
    public boolean delete(BrokerInfo brokerInfo) {
        LambdaUpdateChainWrapper<BrokerInfo> lambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<BrokerInfo>(this.brokerDaoMapper);
        lambdaUpdateChainWrapper.eq(BrokerInfo::getId, brokerInfo.getId());
        return lambdaUpdateChainWrapper.remove();
    }

    @Transactional
    @Override
    public boolean batch(List<BrokerInfo> brokerInfos) {
        boolean status = false;
        int code = this.brokerDaoMapper.insertBatchSomeColumn(brokerInfos);
        if (code > 0) {
            status = true;
        }
        return status;
    }

}
