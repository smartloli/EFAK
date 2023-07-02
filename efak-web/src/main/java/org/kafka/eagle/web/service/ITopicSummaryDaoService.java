package org.kafka.eagle.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.kafka.eagle.pojo.topic.TopicSummaryInfo;

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
public interface ITopicSummaryDaoService extends IService<TopicSummaryInfo> {

    List<TopicSummaryInfo> list();

    /**
     * Get topic info by day.
     * @param day
     * @return
     */
    List<TopicSummaryInfo> list(String day);

    /**
     * Get topic info by clusterId.
     * @return
     */
    List<TopicSummaryInfo> topicsOfDay(String clusterId,String day);

    /**
     * Get topic info by clusterId and topic name.
     * @param clusterId
     * @param topicName
     * @return
     */
    TopicSummaryInfo topicOfLatest(String clusterId,String topicName,String day);

    /**
     * Get topic info by id(topic id).
     * @param id
     * @return
     */
    TopicSummaryInfo topic(Long id);

    /**
     * Insert into topic info data.
     * @param topicInfo
     * @return
     */
    boolean insert(TopicSummaryInfo topicInfo);

    /**
     * Page limit.
     * @param params
     * @return
     */
    List<TopicSummaryInfo> pages(Map<String,Object> params);

    /**
     * Update topic info by topic id.
     * @param topicInfo
     * @return
     */
    boolean update(TopicSummaryInfo topicInfo);

    /**
     * Delete topic info by topic id.
     * @param topicIds
     * @return
     */
    boolean delete(List<Long> topicIds);

    /**
     * Batch update topic infos.
     * @param topicInfos
     * @return
     */

    boolean update(List<TopicSummaryInfo> topicInfos);

    /**
     * Batch insert topic infos.
     * @param topicInfos
     * @return
     */
    boolean batch(List<TopicSummaryInfo> topicInfos);

}
