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
import org.kafka.eagle.pojo.cluster.ClusterCreateInfo;
import org.kafka.eagle.web.dao.mapper.ClusterCreateDaoMapper;
import org.kafka.eagle.web.service.IClusterCreateDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 *
 * @Author: smartloli
 * @Date: 2023/5/28 00:44
 * @Version: 3.4.0
 */
@Service
public class ClusterCreateDaoServiceImpl extends ServiceImpl<ClusterCreateDaoMapper, ClusterCreateInfo> implements IClusterCreateDaoService {

    @Autowired
    ClusterCreateDaoMapper clusterDaoMapper;

    @Override
    public List<ClusterCreateInfo> clusters() {
        return new LambdaQueryChainWrapper<>(this.clusterDaoMapper).list();
    }

    @Override
    public boolean insert(ClusterCreateInfo clusterInfo) {
        boolean status =false;
        int code = this.clusterDaoMapper.insert(clusterInfo);
        if(code>0){
            status = true;
        }
        return status;
    }

    @Override
    public Page<ClusterCreateInfo> pages(Map<String, Object> params) {
        int start = Integer.parseInt(params.get("start").toString());
        int size = Integer.parseInt(params.get("size").toString());

        Page<ClusterCreateInfo> pages = new Page<>(start,size);

        LambdaQueryChainWrapper<ClusterCreateInfo> queryChainWrapper = new LambdaQueryChainWrapper<ClusterCreateInfo>(this.clusterDaoMapper);
        queryChainWrapper.like(ClusterCreateInfo::getBrokerHost,params.get("search").toString());
        return queryChainWrapper.orderByDesc(ClusterCreateInfo::getModifyTime).page(pages);
    }

    @Override
    public boolean update(ClusterCreateInfo clusterInfo) {
        LambdaUpdateChainWrapper<ClusterCreateInfo> lambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<ClusterCreateInfo>(this.clusterDaoMapper);
        lambdaUpdateChainWrapper.eq(ClusterCreateInfo::getClusterId,clusterInfo.getClusterId());
        return lambdaUpdateChainWrapper.update(clusterInfo);
    }

    @Override
    public boolean delete(String clusterId) {
        LambdaUpdateChainWrapper<ClusterCreateInfo> lambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<ClusterCreateInfo>(this.clusterDaoMapper);
        lambdaUpdateChainWrapper.eq(ClusterCreateInfo::getClusterId,clusterId);
        return lambdaUpdateChainWrapper.remove();
    }

    @Transactional
    @Override
    public boolean batch(List<ClusterCreateInfo> createInfos) {
        boolean status =false;
        int code = this.clusterDaoMapper.insertBatchSomeColumn(createInfos);
        if(code>0){
            status = true;
        }
        return status;
    }
}
