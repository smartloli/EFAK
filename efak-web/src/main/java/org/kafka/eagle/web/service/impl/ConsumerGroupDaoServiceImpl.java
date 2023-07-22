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

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
        QueryWrapper<ConsumerGroupInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT group_id").lambda().eq(ConsumerGroupInfo::getClusterId, clusterId);
        return this.consumerGroupDaoMapper.selectList(queryWrapper);
    }

    @Override
    public List<ConsumerGroupInfo> consumerGroupList(String clusterId) {
        return new LambdaQueryChainWrapper<>(this.consumerGroupDaoMapper).eq(ConsumerGroupInfo::getClusterId, clusterId).list();
    }

    @Override
    public List<ConsumerGroupInfo> consumerGroups(String clusterId, String groupId) {
        return new LambdaQueryChainWrapper<>(this.consumerGroupDaoMapper).eq(ConsumerGroupInfo::getClusterId, clusterId).eq(ConsumerGroupInfo::getGroupId, groupId).list();
    }

    @Override
    public Boolean consumerGroups(String clusterId, String groupId, String topicName) {
        if (StrUtil.isBlank(topicName)) {
            return new LambdaQueryChainWrapper<>(this.consumerGroupDaoMapper).eq(ConsumerGroupInfo::getClusterId, clusterId).eq(ConsumerGroupInfo::getGroupId, groupId).exists();
        } else {
            return new LambdaQueryChainWrapper<>(this.consumerGroupDaoMapper).eq(ConsumerGroupInfo::getClusterId, clusterId).eq(ConsumerGroupInfo::getGroupId, groupId).eq(ConsumerGroupInfo::getTopicName, topicName).exists();
        }
    }

    @Override
    public ConsumerGroupInfo consumerGroups(ConsumerGroupInfo consumerGroupInfo) {
        return new LambdaQueryChainWrapper<>(this.consumerGroupDaoMapper).eq(ConsumerGroupInfo::getClusterId, consumerGroupInfo.getClusterId()).eq(ConsumerGroupInfo::getGroupId, consumerGroupInfo.getGroupId()).eq(ConsumerGroupInfo::getTopicName, consumerGroupInfo.getTopicName()).one();
    }

    @Override
    public Long totalOfConsumerGroups(ConsumerGroupInfo consumerGroupInfo) {
        QueryWrapper<ConsumerGroupInfo> queryWrapper = new QueryWrapper<>();
        if (consumerGroupInfo.getStatus() == KConstants.Topic.ALL) {// all consumer group include active and standby
            queryWrapper.select("DISTINCT group_id").lambda().eq(ConsumerGroupInfo::getClusterId, consumerGroupInfo.getClusterId());
        } else if (consumerGroupInfo.getStatus() == KConstants.Topic.RUNNING) { // running
            queryWrapper.select("DISTINCT group_id").lambda().eq(ConsumerGroupInfo::getClusterId, consumerGroupInfo.getClusterId()).eq(ConsumerGroupInfo::getStatus, KConstants.Topic.RUNNING);
        }
        return this.consumerGroupDaoMapper.selectCount(queryWrapper);
    }

    @Override
    public Long totalOfConsumerGroupTopics(String clusterId, String groupId) {
        QueryWrapper<ConsumerGroupInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ConsumerGroupInfo::getClusterId,clusterId).eq(ConsumerGroupInfo::getGroupId,groupId).ne(ConsumerGroupInfo::getTopicName,"");
        return this.consumerGroupDaoMapper.selectCount(queryWrapper);
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
        System.out.println("consumerGroupInfo:"+consumerGroupInfo.toString());
        if (!this.consumerGroups(consumerGroupInfo.getClusterId(), consumerGroupInfo.getGroupId(), consumerGroupInfo.getTopicName())) {
            return this.insert(consumerGroupInfo);
        } else {
            LambdaUpdateChainWrapper<ConsumerGroupInfo> lambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<ConsumerGroupInfo>(this.consumerGroupDaoMapper);
            if (StrUtil.isBlank(consumerGroupInfo.getTopicName())) {
                lambdaUpdateChainWrapper.eq(ConsumerGroupInfo::getClusterId, consumerGroupInfo.getClusterId()).eq(ConsumerGroupInfo::getGroupId, consumerGroupInfo.getGroupId());
            } else {
                lambdaUpdateChainWrapper.eq(ConsumerGroupInfo::getClusterId, consumerGroupInfo.getClusterId()).eq(ConsumerGroupInfo::getGroupId, consumerGroupInfo.getGroupId()).eq(ConsumerGroupInfo::getTopicName, consumerGroupInfo.getTopicName());
            }
            return lambdaUpdateChainWrapper.update(consumerGroupInfo);
        }
    }

    @Override
    public Page<ConsumerGroupInfo> pages(Map<String, Object> params) {
        int start = Integer.parseInt(params.get("start").toString());
        int size = Integer.parseInt(params.get("size").toString());

        Page<ConsumerGroupInfo> pages = new Page<>(start, size);

        LambdaQueryChainWrapper<ConsumerGroupInfo> queryChainWrapper = new LambdaQueryChainWrapper<ConsumerGroupInfo>(this.consumerGroupDaoMapper);
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
