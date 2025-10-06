package org.kafka.eagle.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.topic.TopicInfo;
import org.kafka.eagle.web.service.TopicInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Topic监控控制器（手动操作）
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/07/28 23:44:35
 * @version 5.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/topic/monitor")
public class TopicMonitorController {
    
    @Autowired
    private TopicInfoService topicInfoService;
    
    /**
     * 根据名称获取Topic信息
     *
     * @param topicName Topic名称
     * @return Topic信息
     */
    @GetMapping("/topics/{topicName}")
    public ResponseEntity<Map<String, Object>> getTopicByName(@PathVariable String topicName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            TopicInfo topic = topicInfoService.getTopicInfo(topicName);
            
            if (topic != null) {
                result.put("success", true);
                result.put("topic", topic);
            } else {
                result.put("success", false);
                result.put("message", "Topic not found: " + topicName);
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Failed to get topic: {}", topicName, e);
            
            result.put("success", false);
            result.put("message", "Failed to get topic: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * Get topic monitoring statistics
     * 
     * @return monitoring statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getMonitoringStats() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int totalCount = topicInfoService.getTotalTopicCount();
            
            result.put("success", true);
            result.put("totalTopics", totalCount);
            result.put("lastUpdateTime", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Failed to get monitoring stats", e);
            
            result.put("success", false);
            result.put("message", "Failed to get monitoring stats: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }
}