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
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.pojo.topic.TopicSummaryInfo;
import org.kafka.eagle.web.dao.mapper.TopicSummaryDaoMapper;
import org.kafka.eagle.web.service.ITopicSummaryDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public List<TopicSummaryInfo> list(String day) {
        return new LambdaQueryChainWrapper<>(this.topicSummaryDaoMapper).lt(TopicSummaryInfo::getDay, day).list();
    }


    @Override
    public List<TopicSummaryInfo> topicsOfDay(String clusterId, String day) {
        return new LambdaQueryChainWrapper<>(this.topicSummaryDaoMapper).eq(TopicSummaryInfo::getClusterId, clusterId).eq(TopicSummaryInfo::getDay, day).list();
    }

    @Override
    public TopicSummaryInfo topicOfLatest(String clusterId, String topicName, String day) {
        return this.topicSummaryDaoMapper.selectOne(new QueryWrapper<TopicSummaryInfo>().lambda().eq(TopicSummaryInfo::getClusterId, clusterId).eq(TopicSummaryInfo::getTopicName, topicName).eq(TopicSummaryInfo::getDay, day).orderByDesc(TopicSummaryInfo::getTimespan).last("limit 1"));
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
    public Integer topicOfActiveNums(String clusterId, String stime, String etime) {
        QueryWrapper<TopicSummaryInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("COUNT(DISTINCT topic_name) AS active_numes").lambda().eq(TopicSummaryInfo::getClusterId, clusterId).gt(TopicSummaryInfo::getLogSizeDiffVal,0).between(TopicSummaryInfo::getDay,stime,etime);
        return this.topicSummaryDaoMapper.selectList(queryWrapper).size();
    }

    @Override
    public List<TopicSummaryInfo> pages(Map<String, Object> params) {
        String cid = params.get("cid").toString();
        String topic = params.get("topic").toString();
        String stime = params.get("stime").toString();
        String etime = params.get("etime").toString();

        return new LambdaQueryChainWrapper<>(this.topicSummaryDaoMapper).eq(TopicSummaryInfo::getClusterId, cid).eq(TopicSummaryInfo::getTopicName, topic).between(TopicSummaryInfo::getDay,stime,etime).list();
    }

    @Override
    public List<TopicSummaryInfo> pagesOfDay(Map<String, Object> params) {
        String cid = params.get("cid").toString();
        String topic = params.get("topic").toString();
        String stime = params.get("stime").toString();
        String etime = params.get("etime").toString();
        Set<String> topics = new HashSet<>();
        QueryWrapper<TopicSummaryInfo> queryWrapper = new QueryWrapper<>();
        if(StrUtil.isNotBlank(topic)){
            for (String subTopic : topic.split(",")) {
                topics.add(subTopic);
            }
            queryWrapper.select("day, IFNULL(sum(log_size_diff_val),0) AS log_size_diff_val").lambda().eq(TopicSummaryInfo::getClusterId, cid).in(TopicSummaryInfo::getTopicName,topics).gt(TopicSummaryInfo::getLogSizeDiffVal,0).between(TopicSummaryInfo::getDay,stime,etime).groupBy(TopicSummaryInfo::getDay);
        }else{
            queryWrapper.select("day, IFNULL(sum(log_size_diff_val),0) AS log_size_diff_val").lambda().eq(TopicSummaryInfo::getClusterId, cid).gt(TopicSummaryInfo::getLogSizeDiffVal,0).between(TopicSummaryInfo::getDay,stime,etime).groupBy(TopicSummaryInfo::getDay);
        }

        return this.topicSummaryDaoMapper.selectList(queryWrapper);
    }

    @Override
    public boolean update(TopicSummaryInfo topicInfo) {
        TopicSummaryInfo checkTopicInfo = this.topicOfLatest(topicInfo.getClusterId(), topicInfo.getTopicName(), topicInfo.getDay());
        if (checkTopicInfo == null || StrUtil.isBlank(checkTopicInfo.getTopicName())) {
            return this.insert(topicInfo);
        } else {
            LambdaUpdateChainWrapper<TopicSummaryInfo> lambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<TopicSummaryInfo>(this.topicSummaryDaoMapper);
            lambdaUpdateChainWrapper.eq(TopicSummaryInfo::getClusterId, topicInfo.getClusterId()).eq(TopicSummaryInfo::getTopicName, topicInfo.getTopicName()).eq(TopicSummaryInfo::getDay, topicInfo.getDay());
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
        List<TopicSummaryInfo> topicSummaryInfos = this.topicsOfDay(topicInfo.getClusterId(), topicInfo.getDay());
        if (CollectionUtils.isEmpty(topicSummaryInfos)) {
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
