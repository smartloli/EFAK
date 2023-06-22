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
        return null;
    }

    @Override
    public TopicInfo topic(Long id) {
        return null;
    }

    @Override
    public boolean insert(TopicInfo topicInfo) {
        return false;
    }

    @Override
    public Page<TopicInfo> pages(Map<String, Object> params) {
        return null;
    }

    @Override
    public boolean update(TopicInfo topicInfo) {
        return false;
    }

    @Override
    public boolean delete(TopicInfo topicInfo) {
        return false;
    }

    @Override
    public boolean update(List<TopicInfo> topicInfos) {
        return false;
    }

    @Override
    public boolean batch(List<TopicInfo> topicInfos) {
        return false;
    }
}
