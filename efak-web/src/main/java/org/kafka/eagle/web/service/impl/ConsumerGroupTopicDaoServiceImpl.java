/**
 * ConsumerGroupTopicDaoServiceImpl.java
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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.kafka.eagle.pojo.consumer.ConsumerGroupTopicInfo;
import org.kafka.eagle.web.dao.mapper.ConsumerGroupTopicDaoMapper;
import org.kafka.eagle.web.service.IConsumerGroupTopicDaoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/7/26 22:36
 * @Version: 3.4.0
 */
@Service
public class ConsumerGroupTopicDaoServiceImpl extends ServiceImpl<ConsumerGroupTopicDaoMapper, ConsumerGroupTopicInfo> implements IConsumerGroupTopicDaoService {

    @Resource
    private ConsumerGroupTopicDaoMapper consumerGroupTopicDaoMapper;

    @Override
    public List<ConsumerGroupTopicInfo> consumerGroupTopicList(String clusterId) {
        return new LambdaQueryChainWrapper<>(this.consumerGroupTopicDaoMapper).eq(ConsumerGroupTopicInfo::getClusterId, clusterId).list();
    }

    @Override
    public ConsumerGroupTopicInfo consumerGroupTopic(ConsumerGroupTopicInfo consumerGroupTopicInfo) {
        return this.consumerGroupTopicDaoMapper.selectOne(new QueryWrapper<ConsumerGroupTopicInfo>().lambda().eq(ConsumerGroupTopicInfo::getClusterId, consumerGroupTopicInfo.getClusterId()).eq(ConsumerGroupTopicInfo::getGroupId, consumerGroupTopicInfo.getGroupId()).eq(ConsumerGroupTopicInfo::getTopicName, consumerGroupTopicInfo.getTopicName()).orderByDesc(ConsumerGroupTopicInfo::getTimespan).last("limit 1"));
    }

    @Override
    public boolean insert(ConsumerGroupTopicInfo consumerGroupTopicInfo) {
        boolean status = false;
        int code = this.consumerGroupTopicDaoMapper.insert(consumerGroupTopicInfo);
        if (code > 0) {
            status = true;
        }
        return status;
    }

    @Override
    public Boolean consumerGroupTopic(String clusterId, String groupId, String topicName) {
        return new LambdaQueryChainWrapper<>(this.consumerGroupTopicDaoMapper).eq(ConsumerGroupTopicInfo::getClusterId, clusterId).eq(ConsumerGroupTopicInfo::getGroupId, groupId).eq(ConsumerGroupTopicInfo::getTopicName, topicName).exists();
    }

    @Override
    public boolean update(ConsumerGroupTopicInfo consumerGroupTopicInfo) {
        if (!this.consumerGroupTopic(consumerGroupTopicInfo.getClusterId(), consumerGroupTopicInfo.getGroupId(), consumerGroupTopicInfo.getTopicName())) {
            return this.insert(consumerGroupTopicInfo);
        } else {
            LambdaUpdateChainWrapper<ConsumerGroupTopicInfo> lambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<>(this.consumerGroupTopicDaoMapper);
            lambdaUpdateChainWrapper.eq(ConsumerGroupTopicInfo::getClusterId, consumerGroupTopicInfo.getClusterId()).eq(ConsumerGroupTopicInfo::getGroupId, consumerGroupTopicInfo.getGroupId()).eq(ConsumerGroupTopicInfo::getTopicName, consumerGroupTopicInfo.getTopicName());
            return lambdaUpdateChainWrapper.update(consumerGroupTopicInfo);
        }
    }

    @Override
    public boolean batch(List<ConsumerGroupTopicInfo> consumerGroupTopicInfos) {
        boolean status = false;
        int code = this.consumerGroupTopicDaoMapper.insertBatchSomeColumn(consumerGroupTopicInfos);
        if (code > 0) {
            status = true;
        }
        return status;
    }

    @Override
    public boolean delete(Long id) {
        return new LambdaUpdateChainWrapper<>(this.consumerGroupTopicDaoMapper).eq(ConsumerGroupTopicInfo::getId, id).remove();
    }

    @Override
    public boolean delete(List<Long> consumerGroupIds) {
        boolean status = false;
        int code = this.consumerGroupTopicDaoMapper.deleteBatchIds(consumerGroupIds);
        if (code > 0) {
            status = true;
        }
        return status;
    }

    @Override
    public List<ConsumerGroupTopicInfo> pages(Map<String, Object> params) {
        String cid = params.get("cid").toString();
        String group = params.get("group").toString();
        String topic = params.get("topic").toString();
        String stime = params.get("stime").toString();
        String etime = params.get("etime").toString();

        return new LambdaQueryChainWrapper<>(this.consumerGroupTopicDaoMapper).eq(ConsumerGroupTopicInfo::getClusterId, cid).eq(ConsumerGroupTopicInfo::getGroupId,group).eq(ConsumerGroupTopicInfo::getTopicName, topic).between(ConsumerGroupTopicInfo::getDay,stime,etime).list();
    }

    @Override
    public ConsumerGroupTopicInfo consumersOfLatest(String clusterId, String group, String topic) {
        return this.consumerGroupTopicDaoMapper.selectOne(new QueryWrapper<ConsumerGroupTopicInfo>().lambda().eq(ConsumerGroupTopicInfo::getClusterId, clusterId).eq(ConsumerGroupTopicInfo::getGroupId, group).eq(ConsumerGroupTopicInfo::getTopicName, topic).orderByDesc(ConsumerGroupTopicInfo::getTimespan).last("limit 1"));
    }
}
