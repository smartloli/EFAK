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
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.pojo.topic.TopicRankInfo;
import org.kafka.eagle.web.dao.mapper.TopicRankDaoMapper;
import org.kafka.eagle.web.service.ITopicRankDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Implementation of the Topic Data Access Object (DAO) service interface.
 * This class provides the actual implementation for CRUD operations on topics.
 *
 * @Author: smartloli
 * @Date: 2023/8/14 16:15
 * @Version: 3.4.0
 */
@Slf4j
@Service
public class TopicRankDaoServiceImpl extends ServiceImpl<TopicRankDaoMapper, TopicRankInfo> implements ITopicRankDaoService {

    @Autowired
    TopicRankDaoMapper topicRankDaoMapper;

    @Override
    public List<TopicRankInfo> list() {
        return new LambdaQueryChainWrapper<>(this.topicRankDaoMapper).list();
    }

    @Override
    public List<TopicRankInfo> topics(String clusterId) {
        return new LambdaQueryChainWrapper<>(this.topicRankDaoMapper).eq(TopicRankInfo::getClusterId, clusterId).list();
    }

    @Override
    public TopicRankInfo topics(String clusterId, String topicName, String topicKey) {
        return new LambdaQueryChainWrapper<>(this.topicRankDaoMapper).eq(TopicRankInfo::getClusterId, clusterId).eq(TopicRankInfo::getTopicName, topicName).eq(TopicRankInfo::getTopicKey, topicKey).one();
    }

    @Override
    public TopicRankInfo topic(Long id) {
        return new LambdaQueryChainWrapper<>(this.topicRankDaoMapper).eq(TopicRankInfo::getId, id).one();
    }

    @Override
    public boolean insert(TopicRankInfo topicRankInfo) {
        boolean status = false;
        int code = this.topicRankDaoMapper.insert(topicRankInfo);
        if (code > 0) {
            status = true;
        }
        return status;
    }

    @Override
    public Page<TopicRankInfo> pages(Map<String, Object> params) {
        int start = Integer.parseInt(params.get("start").toString());
        int size = Integer.parseInt(params.get("size").toString());
        String cid = params.get("cid").toString();

        Page<TopicRankInfo> pages = new Page<>(start, size);

        LambdaQueryChainWrapper<TopicRankInfo> queryChainWrapper = new LambdaQueryChainWrapper<TopicRankInfo>(this.topicRankDaoMapper);
        queryChainWrapper.eq(TopicRankInfo::getClusterId, cid).like(TopicRankInfo::getTopicName, params.get("search").toString());
        return queryChainWrapper.page(pages);
    }

    @Override
    public boolean update(TopicRankInfo topicRankInfo) {
        TopicRankInfo checkTopicInfo = this.topics(topicRankInfo.getClusterId(), topicRankInfo.getTopicName(), topicRankInfo.getTopicKey());
        if (checkTopicInfo == null || StrUtil.isBlank(checkTopicInfo.getTopicName())) {
            return this.insert(topicRankInfo);
        } else {
            LambdaUpdateChainWrapper<TopicRankInfo> lambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<TopicRankInfo>(this.topicRankDaoMapper);
            lambdaUpdateChainWrapper.eq(TopicRankInfo::getClusterId, topicRankInfo.getClusterId()).eq(TopicRankInfo::getTopicName, topicRankInfo.getTopicName()).eq(TopicRankInfo::getTopicKey, topicRankInfo.getTopicKey());
            return lambdaUpdateChainWrapper.update(topicRankInfo);
        }
    }

    @Override
    public boolean delete(List<Long> topicIds) {
        boolean status = false;
        int code = this.topicRankDaoMapper.deleteBatchIds(topicIds);
        if (code > 0) {
            status = true;
        }
        return status;
    }

    @Override
    public boolean batch(List<TopicRankInfo> topicRankInfos) {

        boolean status = false;

        int code = this.topicRankDaoMapper.insertBatchSomeColumn(topicRankInfos);
        if (code > 0) {
            status = true;
        }
        return status;
    }

    @Override
    public boolean replace(List<TopicRankInfo> topicRankInfos) {

        boolean status = false;

        int code = this.topicRankDaoMapper.replaceBatchSomeColumn(topicRankInfos);
        if (code > 0) {
            status = true;
        }
        return status;
    }
}
