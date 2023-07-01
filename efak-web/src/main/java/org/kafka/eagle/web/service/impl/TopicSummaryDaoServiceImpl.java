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
import org.kafka.eagle.pojo.topic.TopicSummaryInfo;
import org.kafka.eagle.web.dao.mapper.TopicSummaryDaoMapper;
import org.kafka.eagle.web.service.ITopicSummaryDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Implementation of the Topic Data Access Object (DAO) service interface.
 * This class provides the actual implementation for CRUD operations on topics.
 *
 * @Author: smartloli
 * @Date: 2023/7/1 23:26
 * @Version: 3.4.0
 */
@Slf4j
@Service
public class TopicSummaryDaoServiceImpl extends ServiceImpl<TopicSummaryDaoMapper, TopicSummaryInfo> implements ITopicSummaryDaoService {

    @Autowired
    TopicSummaryDaoMapper topicSummaryDaoMapper;

    @Override
    public List<TopicSummaryInfo> list() {
        return new LambdaQueryChainWrapper<>(this.topicSummaryDaoMapper).list();
    }

    @Override
    public List<TopicSummaryInfo> topics(String clusterId) {
        return new LambdaQueryChainWrapper<>(this.topicSummaryDaoMapper).eq(TopicSummaryInfo::getClusterId, clusterId).list();
    }

    @Override
    public TopicSummaryInfo topics(String clusterId, String topicName) {
        return new LambdaQueryChainWrapper<>(this.topicSummaryDaoMapper).eq(TopicSummaryInfo::getClusterId, clusterId).eq(TopicSummaryInfo::getTopicName, topicName).one();
    }

    @Override
    public TopicSummaryInfo topic(Long id) {
        return new LambdaQueryChainWrapper<>(this.topicSummaryDaoMapper).eq(TopicSummaryInfo::getId, id).one();
    }

    @Override
    public boolean insert(TopicSummaryInfo topicInfo) {
        boolean status = false;
        int code = this.topicSummaryDaoMapper.insert(topicInfo);
        if (code > 0) {
            status = true;
        }
        return status;
    }

    @Override
    public Page<TopicSummaryInfo> pages(Map<String, Object> params) {
        int start = Integer.parseInt(params.get("start").toString());
        int size = Integer.parseInt(params.get("size").toString());
        String cid = params.get("cid").toString();

        Page<TopicSummaryInfo> pages = new Page<>(start, size);

        LambdaQueryChainWrapper<TopicSummaryInfo> queryChainWrapper = new LambdaQueryChainWrapper<TopicSummaryInfo>(this.topicSummaryDaoMapper);
        queryChainWrapper.eq(TopicSummaryInfo::getClusterId, cid).like(TopicSummaryInfo::getTopicName, params.get("search").toString());
        return queryChainWrapper.orderByDesc(TopicSummaryInfo::getModifyTime).page(pages);
    }

    @Override
    public boolean update(TopicSummaryInfo topicInfo) {
        TopicSummaryInfo checkTopicInfo = this.topics(topicInfo.getClusterId(), topicInfo.getTopicName());
        if (checkTopicInfo == null || StrUtil.isBlank(checkTopicInfo.getTopicName())) {
            return this.insert(topicInfo);
        } else {
            LambdaUpdateChainWrapper<TopicSummaryInfo> lambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<TopicSummaryInfo>(this.topicSummaryDaoMapper);
            lambdaUpdateChainWrapper.eq(TopicSummaryInfo::getClusterId, topicInfo.getClusterId()).eq(TopicSummaryInfo::getTopicName, topicInfo.getTopicName());
            return lambdaUpdateChainWrapper.update(topicInfo);
        }
    }

    @Override
    public boolean delete(List<Long> topicIds) {
        boolean status = false;
        int code = this.topicSummaryDaoMapper.deleteBatchIds(topicIds);
        if (code > 0) {
            status = true;
        }
        return status;
    }

    @Override
    public boolean update(List<TopicSummaryInfo> topicInfos) {
        log.info("Topic batch update.");
        if (topicInfos == null || CollectionUtils.isEmpty(topicInfos) || topicInfos.size() == 0) {
            return false;
        }

        TopicSummaryInfo topicInfo = topicInfos.get(0);
        List<TopicSummaryInfo> brokerInfosInDb = this.topics(topicInfo.getClusterId());
        if (CollectionUtils.isEmpty(brokerInfosInDb)) {
            return this.batch(topicInfos);
        } else {
            return this.updateBatchById(topicInfos);
        }

    }

    @Override
    public boolean batch(List<TopicSummaryInfo> topicInfos) {

        boolean status = false;

        int code = this.topicSummaryDaoMapper.insertBatchSomeColumn(topicInfos);
        if (code > 0) {
            status = true;
        }
        return status;
    }
}
