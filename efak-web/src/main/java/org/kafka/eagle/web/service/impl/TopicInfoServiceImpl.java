package org.kafka.eagle.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.topic.TopicInfo;
import org.kafka.eagle.web.mapper.TopicInfoMapper;
import org.kafka.eagle.web.service.DatabaseConfigService;
import org.kafka.eagle.web.service.TopicInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * Description: Topic information service implementation
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/08/24 16:13:26
 * @version 5.0.0
 */
@Slf4j
@Service
public class TopicInfoServiceImpl implements TopicInfoService {
    
    @Autowired
    private TopicInfoMapper topicInfoMapper;
    
    @Autowired
    private DatabaseConfigService databaseConfigService;
    
    @Override
    public TopicInfo getTopicInfo(String topicName) {
        try {
            return topicInfoMapper.selectByTopicName(topicName);
        } catch (Exception e) {
            log.error("Failed to get topic info for: {}", topicName, e);
            return null;
        }
    }
    
    @Override
    public List<TopicInfo> getAllTopicInfo() {
        try {
            return topicInfoMapper.selectAll();
        } catch (Exception e) {
            log.error("Failed to get all topic info", e);
            return null;
        }
    }
    
    @Override
    @Transactional
    public boolean saveOrUpdateTopicInfo(TopicInfo topicInfo) {
        try {
            topicInfo.setUpdateBy("system");
            int result = topicInfoMapper.insertOrUpdate(topicInfo);
            return result > 0;
        } catch (Exception e) {
            log.error("Failed to save or update topic info: {}", topicInfo.getTopicName(), e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean deleteTopicInfo(String topicName) {
        try {
            int result = topicInfoMapper.deleteByTopicName(topicName);
            return result > 0;
        } catch (Exception e) {
            log.error("Failed to delete topic info: {}", topicName, e);
            return false;
        }
    }
    
    @Override
    public int getTotalTopicCount() {
        try {
            return topicInfoMapper.countTotal();
        } catch (Exception e) {
            log.error("Failed to get total topic count", e);
            return 0;
        }
    }
}