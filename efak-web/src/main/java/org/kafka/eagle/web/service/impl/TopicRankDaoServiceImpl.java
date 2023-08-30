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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.common.utils.StrUtils;
import org.kafka.eagle.pojo.topic.TopicCapacityInfo;
import org.kafka.eagle.pojo.topic.TopicRankInfo;
import org.kafka.eagle.pojo.topic.TopicRankScatterInfo;
import org.kafka.eagle.web.dao.mapper.TopicRankDaoMapper;
import org.kafka.eagle.web.service.ITopicRankDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public String topicCapacity(String clusterId, String topicKey) {
        QueryWrapper<TopicRankInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("SUM(topic_value) AS topic_capacity").lambda().eq(TopicRankInfo::getClusterId, clusterId).eq(TopicRankInfo::getTopicKey, topicKey);
        List<java.util.Map<String, Object>> result = this.topicRankDaoMapper.selectMaps(queryWrapper);
        return result.get(0).get("topic_capacity") == null ? "0.00" : result.get(0).get("topic_capacity").toString();
    }

    @Override
    public List<TopicRankInfo> topics(String clusterId, String topicKey) {
        return new LambdaQueryChainWrapper<>(this.topicRankDaoMapper).eq(TopicRankInfo::getClusterId, clusterId).eq(TopicRankInfo::getTopicKey, topicKey).list();
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

    @Override
    public TopicCapacityInfo getTopicScatter(String clusterId, String topicKey) {
        QueryWrapper<TopicRankInfo> queryWrapper = new QueryWrapper<>();

        queryWrapper.select("COUNT(CASE WHEN topic_value between 0 and 104857600 then topic_value end) as `mb`,COUNT(CASE WHEN topic_value between 104857600 and 10737418240 then topic_value end) as `gb`,COUNT(CASE WHEN topic_value > 10737418240 then topic_value end) as `tb`").lambda().eq(TopicRankInfo::getClusterId, clusterId).eq(TopicRankInfo::getTopicKey, topicKey).ne(TopicRankInfo::getTopicValue, "0");
        List<Map<String, Object>> result = this.topicRankDaoMapper.selectMaps(queryWrapper);

        TopicCapacityInfo topicCapacityInfo = new TopicCapacityInfo();
        topicCapacityInfo.setMb(result.get(0).get("mb") == null ? 0 : Long.parseLong(result.get(0).get("mb").toString()));
        topicCapacityInfo.setGb(result.get(0).get("gb") == null ? 0 : Long.parseLong(result.get(0).get("gb").toString()));
        topicCapacityInfo.setTb(result.get(0).get("tb") == null ? 0 : Long.parseLong(result.get(0).get("tb").toString()));

        return topicCapacityInfo;
    }

    @Override
    public List<TopicRankScatterInfo> pageTopicScatterOfTen(String clusterId, String topicKeyByOrder, List<String> topicKeys) {
        List<TopicRankInfo> topicRankInfos = this.topicRankDaoMapper.selectList(new QueryWrapper<TopicRankInfo>().lambda().eq(TopicRankInfo::getClusterId, clusterId).eq(TopicRankInfo::getTopicKey, topicKeyByOrder).last("order by topic_value+0 desc limit 10"));
        List<TopicRankScatterInfo> topicRankScatterInfos = new ArrayList<>();
        for (TopicRankInfo topicRankInfo : topicRankInfos) {
            TopicRankScatterInfo topicRankScatterInfo = new TopicRankScatterInfo();
            topicRankScatterInfo.setTopicName(topicRankInfo.getTopicName());
            topicRankInfo.setTopicKey(topicRankInfo.getTopicKey());
            if ("capacity".equals(topicKeyByOrder)) {
                String capacity = StrUtils.stringifyByObject(topicRankInfo.getTopicValue()).getString("value");
                topicRankScatterInfo.setTopicCapacity(capacity);
                String byteIn = StrUtils.stringifyByObject(this.topic(clusterId, topicRankInfo.getTopicName(), "byte_in").getTopicValue()).getString("value");
                topicRankScatterInfo.setTopicByteIn(byteIn);
                String byteOut = StrUtils.stringifyByObject(this.topic(clusterId, topicRankInfo.getTopicName(), "byte_out").getTopicValue()).getString("value");
                topicRankScatterInfo.setTopicByteOut(byteOut);
                topicRankScatterInfo.setTopicLogSize(this.topic(clusterId, topicRankInfo.getTopicName(), "logsize").getTopicValue());
            } else if ("logsize".equals(topicKeyByOrder)) {
                topicRankScatterInfo.setTopicLogSize(topicRankInfo.getTopicValue());
                String capacity = StrUtils.stringifyByObject(this.topic(clusterId, topicRankInfo.getTopicName(), "capacity").getTopicValue()).getString("value");
                topicRankScatterInfo.setTopicCapacity(capacity);
                String byteIn = StrUtils.stringifyByObject(this.topic(clusterId, topicRankInfo.getTopicName(), "byte_in").getTopicValue()).getString("value");
                topicRankScatterInfo.setTopicByteIn(byteIn);
                String byteOut = StrUtils.stringifyByObject(this.topic(clusterId, topicRankInfo.getTopicName(), "byte_out").getTopicValue()).getString("value");
                topicRankScatterInfo.setTopicByteOut(byteOut);
            } else if ("byte_in".equals(topicKeyByOrder)) {
                String byteIn = StrUtils.stringifyByObject(topicRankInfo.getTopicValue()).getString("value");
                topicRankScatterInfo.setTopicByteIn(byteIn);
                String capacity = StrUtils.stringifyByObject(this.topic(clusterId, topicRankInfo.getTopicName(), "capacity").getTopicValue()).getString("value");
                topicRankScatterInfo.setTopicCapacity(capacity);
                topicRankScatterInfo.setTopicLogSize(this.topic(clusterId, topicRankInfo.getTopicName(), "logsize").getTopicValue());
                String byteOut = StrUtils.stringifyByObject(this.topic(clusterId, topicRankInfo.getTopicName(), "byte_out").getTopicValue()).getString("value");
                topicRankScatterInfo.setTopicByteOut(byteOut);
            } else if ("byte_out".equals(topicKeyByOrder)) {
                String byteOut = StrUtils.stringifyByObject(topicRankInfo.getTopicValue()).getString("value");
                topicRankScatterInfo.setTopicByteOut(byteOut);
                String capacity = StrUtils.stringifyByObject(this.topic(clusterId, topicRankInfo.getTopicName(), "capacity").getTopicValue()).getString("value");
                topicRankScatterInfo.setTopicCapacity(capacity);
                topicRankScatterInfo.setTopicLogSize(this.topic(clusterId, topicRankInfo.getTopicName(), "logsize").getTopicValue());
                String byteIn = StrUtils.stringifyByObject(this.topic(clusterId, topicRankInfo.getTopicName(), "byte_in").getTopicValue()).getString("value");
                topicRankScatterInfo.setTopicByteIn(byteIn);
            }
            topicRankScatterInfos.add(topicRankScatterInfo);
        }
        return topicRankScatterInfos;
    }

    @Override
    public TopicRankInfo topic(String clusterId, String topicName, String topicKey) {
        return new LambdaQueryChainWrapper<>(this.topicRankDaoMapper).eq(TopicRankInfo::getClusterId, clusterId).eq(TopicRankInfo::getTopicName, topicName).eq(TopicRankInfo::getTopicKey, topicKey).one();
    }
}
