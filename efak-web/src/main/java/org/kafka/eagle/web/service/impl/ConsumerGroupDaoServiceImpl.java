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
import org.kafka.eagle.common.constants.KConstants;
import org.kafka.eagle.pojo.consumer.ConsumerGroupInfo;
import org.kafka.eagle.web.dao.mapper.ConsumerGroupDaoMapper;
import org.kafka.eagle.web.service.IConsumerGroupDaoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Author: smartloli
 * @Date: 2023/7/16 00:35
 * @Version: 3.4.0
 */
@Service
public class ConsumerGroupDaoServiceImpl extends ServiceImpl<ConsumerGroupDaoMapper, ConsumerGroupInfo> implements IConsumerGroupDaoService {

    @Resource
    ConsumerGroupDaoMapper consumerGroupDaoMapper;

    @Override
    public ConsumerGroupInfo consumerGroups(Long id) {
        return new LambdaQueryChainWrapper<>(this.consumerGroupDaoMapper).eq(ConsumerGroupInfo::getId, id).one();
    }

    @Override
    public List<ConsumerGroupInfo> consumerGroups(String clusterId) {
        return new LambdaQueryChainWrapper<>(this.consumerGroupDaoMapper).eq(ConsumerGroupInfo::getClusterId, clusterId).list();
    }

    @Override
    public Boolean consumerGroups(String clusterId, String groupId, String topicName) {
        return new LambdaQueryChainWrapper<>(this.consumerGroupDaoMapper).eq(ConsumerGroupInfo::getClusterId, clusterId).eq(ConsumerGroupInfo::getGroupId, groupId).eq(ConsumerGroupInfo::getTopicName, topicName).exists();
    }

    @Override
    public ConsumerGroupInfo consumerGroups(ConsumerGroupInfo consumerGroupInfo) {
        return new LambdaQueryChainWrapper<>(this.consumerGroupDaoMapper).eq(ConsumerGroupInfo::getClusterId, consumerGroupInfo.getClusterId()).eq(ConsumerGroupInfo::getGroupId, consumerGroupInfo.getGroupId()).eq(ConsumerGroupInfo::getTopicName, consumerGroupInfo.getTopicName()).one();
    }

    @Override
    public Integer totalOfConsumerGroups(ConsumerGroupInfo consumerGroupInfo) {
        Integer size = 0;
        if (consumerGroupInfo.getStatus() == KConstants.Topic.ALL) {// all consumer group include active and standby
            size = new LambdaQueryChainWrapper<>(this.consumerGroupDaoMapper).eq(ConsumerGroupInfo::getClusterId, consumerGroupInfo.getClusterId()).groupBy(ConsumerGroupInfo::getGroupId).list().size();
        } else if (consumerGroupInfo.getStatus() == KConstants.Topic.RUNNING) { // running
            size = new LambdaQueryChainWrapper<>(this.consumerGroupDaoMapper).eq(ConsumerGroupInfo::getClusterId, consumerGroupInfo.getClusterId()).eq(ConsumerGroupInfo::getStatus, KConstants.Topic.RUNNING).groupBy(ConsumerGroupInfo::getGroupId).list().size();
        }
        return size;
    }

    @Override
    public boolean insert(ConsumerGroupInfo consumerGroupInfo) {
        boolean status = false;
        int code = this.consumerGroupDaoMapper.insert(consumerGroupInfo);
        if (code > 0) {
            status = true;
        }
        return status;
    }

    @Override
    public boolean update(ConsumerGroupInfo consumerGroupInfo) {
        if (!this.consumerGroups(consumerGroupInfo.getClusterId(), consumerGroupInfo.getGroupId(), consumerGroupInfo.getTopicName())) {
            return this.insert(consumerGroupInfo);
        } else {
            LambdaUpdateChainWrapper<ConsumerGroupInfo> lambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<ConsumerGroupInfo>(this.consumerGroupDaoMapper);
            lambdaUpdateChainWrapper.eq(ConsumerGroupInfo::getClusterId, consumerGroupInfo.getClusterId()).eq(ConsumerGroupInfo::getGroupId, consumerGroupInfo.getGroupId()).eq(ConsumerGroupInfo::getTopicName, consumerGroupInfo.getTopicName());
            return lambdaUpdateChainWrapper.update(consumerGroupInfo);
        }
    }

    @Override
    public Page<ConsumerGroupInfo> pages(Map<String, Object> params) {
        int start = Integer.parseInt(params.get("start").toString());
        int size = Integer.parseInt(params.get("size").toString());

        Page<ConsumerGroupInfo> pages = new Page<>(start, size);

        LambdaQueryChainWrapper<ConsumerGroupInfo> queryChainWrapper = new LambdaQueryChainWrapper<ConsumerGroupInfo>(this.consumerGroupDaoMapper);
        // queryChainWrapper.like(AuditLogInfo::getHost, params.get("search").toString());
        return queryChainWrapper.orderByDesc(ConsumerGroupInfo::getModifyTime).page(pages);
    }

    @Override
    public boolean batch(List<ConsumerGroupInfo> consumerGroupInfos) {
        boolean status = false;
        int code = this.consumerGroupDaoMapper.insertBatchSomeColumn(consumerGroupInfos);
        if (code > 0) {
            status = true;
        }
        return status;
    }

    @Override
    public boolean delete(Long id) {
        return new LambdaUpdateChainWrapper<>(this.consumerGroupDaoMapper).eq(ConsumerGroupInfo::getId, id).remove();
    }

    @Override
    public boolean delete(List<Long> groupIds) {
        boolean status = false;
        int code = this.consumerGroupDaoMapper.deleteBatchIds(groupIds);
        if (code > 0) {
            status = true;
        }
        return status;
    }
}
