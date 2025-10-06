package org.kafka.eagle.web.service;

import org.kafka.eagle.dto.topic.TopicInfo;
import java.util.List;

/**
 * <p>
 * Description: Topic information service interface
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/08/24 16:11:04
 * @version 5.0.0
 */
public interface TopicInfoService {
    
    /**
     * Get topic information by topic name
     * 
     * @param topicName topic name
     * @return topic information
     */
    TopicInfo getTopicInfo(String topicName);
    
    /**
     * Get all topic information
     * 
     * @return list of topic information
     */
    List<TopicInfo> getAllTopicInfo();
    
    /**
     * Save or update topic information
     * 
     * @param topicInfo topic information
     * @return success flag
     */
    boolean saveOrUpdateTopicInfo(TopicInfo topicInfo);
    
    /**
     * Delete topic information
     * 
     * @param topicName topic name
     * @return success flag
     */
    boolean deleteTopicInfo(String topicName);
    
    /**
     * Get total topic count
     * 
     * @return total count
     */
    int getTotalTopicCount();
}