package org.kafka.eagle.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.kafka.eagle.pojo.topic.TopicInfo;

import java.util.List;
import java.util.Map;

/**
 * The interface representing a service for Topic Data Access Object (DAO) operations.
 * It provides methods to interact with the persistent storage for topics.
 *
 * @Author: smartloli
 * @Date: 2023/5/28 00:37
 * @Version: 3.4.0
 */
public interface ITopicDaoService extends IService<TopicInfo> {

    List<TopicInfo> list();

    /**
     * Get topic info by clusterId.
     * @return
     */
    List<TopicInfo> topics(String clusterId);

    /**
     * Get topic info by clusterId and topic name.
     * @param clusterId
     * @param topicName
     * @return
     */
    TopicInfo topics(String clusterId,String topicName);

    /**
     * Get topic info by id(topic id).
     * @param id
     * @return
     */
    TopicInfo topic(Long id);

    /**
     * Insert into topic info data.
     * @param topicInfo
     * @return
     */
    boolean insert(TopicInfo topicInfo);

    /**
     * Page limit.
     * @param params
     * @return
     */
    Page<TopicInfo> pages(Map<String,Object> params);

    /**
     * Update topic info by topic id.
     * @param topicInfo
     * @return
     */
    boolean update(TopicInfo topicInfo);

    /**
     * Delete topic info by topic id.
     * @param topicInfo
     * @return
     */
    boolean delete(TopicInfo topicInfo);

    /**
     * Batch update topic infos.
     * @param topicInfos
     * @return
     */

    boolean update(List<TopicInfo> topicInfos);

    /**
     * Batch insert topic infos.
     * @param topicInfos
     * @return
     */
    boolean batch(List<TopicInfo> topicInfos);

}
