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
import org.kafka.eagle.pojo.cluster.ClusterInfo;
import org.kafka.eagle.web.dao.mapper.ClusterDaoMapper;
import org.kafka.eagle.web.service.IClusterDaoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 *
 * @Author: smartloli
 * @Date: 2023/5/28 00:44
 * @Version: 3.4.0
 */
@Service
public class ClusterDaoServiceImpl extends ServiceImpl<ClusterDaoMapper, ClusterInfo> implements IClusterDaoService {

    @Resource
    ClusterDaoMapper clusterDaoMapper;

    @Override
    public List<ClusterInfo> clusters() {
        return new LambdaQueryChainWrapper<>(this.clusterDaoMapper).list();
    }

    @Override
    public boolean insert(ClusterInfo clusterInfo) {
        boolean status =false;
        int code = this.clusterDaoMapper.insert(clusterInfo);
        if(code>0){
            status = true;
        }
        return status;
    }

    @Override
    public Page<ClusterInfo> pages(Map<String, Object> params) {
        int start = Integer.parseInt(params.get("start").toString());
        int size = Integer.parseInt(params.get("size").toString());

        Page<ClusterInfo> pages = new Page<>(start,size);

        LambdaQueryChainWrapper<ClusterInfo> queryChainWrapper = new LambdaQueryChainWrapper<ClusterInfo>(this.clusterDaoMapper);
        queryChainWrapper.like(ClusterInfo::getName,params.get("search").toString());
        return queryChainWrapper.orderByDesc(ClusterInfo::getModifyTime).page(pages);
    }

    @Override
    public boolean update(ClusterInfo clusterInfo) {
        LambdaUpdateChainWrapper<ClusterInfo> lambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<ClusterInfo>(this.clusterDaoMapper);
        lambdaUpdateChainWrapper.eq(ClusterInfo::getId,clusterInfo.getId());
        return lambdaUpdateChainWrapper.update(clusterInfo);
    }
}
