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
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.pojo.topic.TopicInfo;
import org.kafka.eagle.web.dao.mapper.TopicDaoMapper;
import org.kafka.eagle.web.service.ITopicDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Implementation of the Topic Data Access Object (DAO) service interface.
 * This class provides the actual implementation for CRUD operations on topics.
 *
 * @Author: smartloli
 * @Date: 2023/5/28 00:44
 * @Version: 3.4.0
 */
@Slf4j
@Service
public class TopicDaoServiceImpl extends ServiceImpl<TopicDaoMapper, TopicInfo> implements ITopicDaoService {

    @Autowired
    TopicDaoMapper topicDaoMapper;

    @Override
    public List<TopicInfo> list() {
        return new LambdaQueryChainWrapper<>(this.topicDaoMapper).list();
    }

    @Override
    public List<TopicInfo> topics(String clusterId) {
        return new LambdaQueryChainWrapper<>(this.topicDaoMapper).eq(TopicInfo::getClusterId, clusterId).list();
    }

    @Override
    public TopicInfo topics(String clusterId, String topicName) {
        return new LambdaQueryChainWrapper<>(this.topicDaoMapper).eq(TopicInfo::getClusterId, clusterId).eq(TopicInfo::getTopicName, topicName).one();
    }

    @Override
    public TopicInfo topic(Long id) {
        return new LambdaQueryChainWrapper<>(this.topicDaoMapper).eq(TopicInfo::getId, id).one();
    }

    @Override
    public boolean insert(TopicInfo topicInfo) {
        boolean status = false;
        int code = this.topicDaoMapper.insert(topicInfo);
        if (code > 0) {
            status = true;
        }
        return status;
    }

    @Override
    public Page<TopicInfo> pages(Map<String, Object> params) {
        int start = Integer.parseInt(params.get("start").toString());
        int size = Integer.parseInt(params.get("size").toString());
        String cid = params.get("cid").toString();

        Page<TopicInfo> pages = new Page<>(start, size);

        LambdaQueryChainWrapper<TopicInfo> queryChainWrapper = new LambdaQueryChainWrapper<TopicInfo>(this.topicDaoMapper);
        queryChainWrapper.eq(TopicInfo::getClusterId, cid).like(TopicInfo::getTopicName, params.get("search").toString());
        return queryChainWrapper.orderByDesc(TopicInfo::getRetainMs).page(pages);
    }

    @Override
    public boolean update(TopicInfo topicInfo) {
        TopicInfo checkTopicInfo = this.topics(topicInfo.getClusterId(), topicInfo.getTopicName());
        if (checkTopicInfo == null || StrUtil.isBlank(checkTopicInfo.getTopicName())) {
            return this.insert(topicInfo);
        } else {
            LambdaUpdateChainWrapper<TopicInfo> lambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<TopicInfo>(this.topicDaoMapper);
            lambdaUpdateChainWrapper.eq(TopicInfo::getClusterId, topicInfo.getClusterId()).eq(TopicInfo::getTopicName, topicInfo.getTopicName());
            return lambdaUpdateChainWrapper.update(topicInfo);
        }
    }

    @Override
    public boolean delete(List<Long> topicIds) {
        boolean status = false;
        int code = this.topicDaoMapper.deleteBatchIds(topicIds);
        if (code > 0) {
            status = true;
        }
        return status;
    }

    @Override
    public boolean update(List<TopicInfo> topicInfos) {
        log.info("Topic batch update.");
        if (topicInfos == null || CollectionUtils.isEmpty(topicInfos) || topicInfos.size() == 0) {
            return false;
        }

        TopicInfo topicInfo = topicInfos.get(0);
        List<TopicInfo> brokerInfosInDb = this.topics(topicInfo.getClusterId());
        if (CollectionUtils.isEmpty(brokerInfosInDb)) {
            return this.batch(topicInfos);
        } else {
            return this.updateBatchById(topicInfos);
        }

    }

    @Override
    public boolean batch(List<TopicInfo> topicInfos) {

        boolean status = false;

        int code = this.topicDaoMapper.insertBatchSomeColumn(topicInfos);
        if (code > 0) {
            status = true;
        }
        return status;
    }
}
