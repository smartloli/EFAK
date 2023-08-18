package org.kafka.eagle.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.kafka.eagle.pojo.topic.TopicCapacityInfo;
import org.kafka.eagle.pojo.topic.TopicRankInfo;
import org.kafka.eagle.pojo.topic.TopicRankScatterInfo;

import java.util.List;
import java.util.Map;

/**
 * The interface representing a service for Topic Data Access Object (DAO) operations.
 * It provides methods to interact with the persistent storage for topics.
 *
 * @Author: smartloli
 * @Date: 2023/8/14 16:13
 * @Version: 3.4.0
 */
public interface ITopicRankDaoService extends IService<TopicRankInfo> {

    List<TopicRankInfo> list();

    /**
     * Get topic info by clusterId.
     *
     * @return
     */
    List<TopicRankInfo> topics(String clusterId);

    /**
     * Get topic info by clusterId and topic name and key.
     *
     * @param clusterId
     * @param topicKey
     * @return
     */
    List<TopicRankInfo> topics(String clusterId, String topicKey);

    String topicCapacity(String clusterId, String topicKey);

    /**
     * Get topic info by id(topic id).
     *
     * @param id
     * @return
     */
    TopicRankInfo topic(Long id);

    TopicRankInfo topic(String clusterId, String topicName, String topicKey);

    /**
     * Insert into topic rank info data.
     *
     * @param topicRankInfo
     * @return
     */
    boolean insert(TopicRankInfo topicRankInfo);

    /**
     * Page limit.
     *
     * @param params
     * @return
     */
    Page<TopicRankInfo> pages(Map<String, Object> params);

    /**
     * Delete topic info by topic id.
     *
     * @param topicIds
     * @return
     */
    boolean delete(List<Long> topicIds);

    /**
     * Batch insert topic rank infos.
     *
     * @param topicRankInfos
     * @return
     */
    boolean batch(List<TopicRankInfo> topicRankInfos);

    /**
     * replace topic rank infos.
     *
     * @param topicRankInfos
     * @return
     */
    boolean replace(List<TopicRankInfo> topicRankInfos);

    /**
     * get topic scatter of capacity numbers.
     *
     * @param clusterId
     * @param topicKey
     * @return
     */
    TopicCapacityInfo getTopicScatter(String clusterId, String topicKey);

    /**
     * get topic scatter page of 10 records.
     *
     * @param clusterId
     * @return
     */
    List<TopicRankScatterInfo> pageTopicScatterOfTen(String clusterId, String topicKeyByOrder, List<String> topicKeys);


}
