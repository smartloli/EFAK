package org.kafka.eagle.web.service;

import org.kafka.eagle.dto.topic.TopicCreateRequest;
import org.kafka.eagle.dto.topic.TopicInfo;
import org.kafka.eagle.dto.topic.TopicPageResponse;
import org.kafka.eagle.dto.topic.TopicQueryRequest;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Topic 服务接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/27 03:31:49
 * @version 5.0.0
 */
public interface TopicService {
    
    /**
     * 分页查询Topic列表
     * 
     * @param request 查询条件
     * @return 分页结果
     */
    TopicPageResponse getTopicPage(TopicQueryRequest request);
    
    /**
     * 根据ID查询Topic详情
     * 
     * @param id 主键ID
     * @return Topic信息
     */
    TopicInfo getTopicById(Long id);
    
    /**
     * 根据Topic名称和集群ID查询
     *
     * @param topicName Topic名称
     * @param clusterId 集群ID
     * @return Topic信息
     */
    TopicInfo getTopicByNameAndCluster(String topicName, String clusterId);
    
    /**
     * 创建Topic
     * 
     * @param request 创建请求
     * @return 创建结果
     */
    boolean createTopic(TopicCreateRequest request);
    
    /**
     * 根据Topic名称和集群ID删除Topic
     *
     * @param topicName Topic名称
     * @param clusterId 集群ID
     * @return 删除结果
     */
    boolean deleteTopic(String topicName, String clusterId);
    
    /**
     * 扩容Topic分区
     *
     * @param topicName Topic名称
     * @param newPartitions 新分区数
     * @param clusterId 集群ID（可选）
     * @return 扩容结果
     */
    boolean scaleTopic(String topicName, Integer newPartitions, String clusterId,String username);
    
    /**
     * 设置Topic保留时间
     *
     * @param topicName Topic名称
     * @param retentionMs 保留时间（毫秒）
     * @param clusterId 集群ID
     * @return 设置结果
     */
    boolean setTopicRetention(String topicName, Long retentionMs, String clusterId,String username);
    
    /**
     * 发送测试数据到Topic
     *
     * @param topicName Topic名称
     * @param dataType 数据类型
     * @param messageCount 消息数量
     * @return 发送结果
     */
    boolean sendTestData(String topicName, String dataType, Integer messageCount);

    /**
     * 获取所有主题名称列表
     *
     * @return 主题名称列表
     */
    List<String> getAllTopicNames();

    /**
     * 获取主题详细统计信息（包含消息总数、大小、读写速度等）
     *
     * @param topicName 主题名称
     * @param clusterId 集群ID
     * @return 主题详细统计信息
     */
    Map<String, Object> getTopicDetailedStats(String topicName, String clusterId);

    /**
     * 获取主题分区分页数据
     *
     * @param topicName 主题名称
     * @param clusterId 集群ID
     * @param params 分页参数（start, length）
     * @return 分区分页数据
     */
    Map<String, Object> getTopicPartitionPage(String topicName, String clusterId, Map<String, Object> params);

    /**
     * 获取主题分区消息
     *
     * @param topicName 主题名称
     * @param clusterId 集群ID
     * @param partition 分区ID
     * @param limit 消息数量限制
     * @return 分区消息列表
     */
    List<Map<String, Object>> getTopicPartitionMessages(String topicName, String clusterId, Integer partition, Integer limit);

    /**
     * 获取主题配置信息
     *
     * @param topicName 主题名称
     * @param clusterId 集群ID
     * @return 主题配置Map
     */
    Map<String, String> getTopicConfig(String topicName, String clusterId);

    /**
     * 获取指定主题的消费者组信息（用于主题详情页面）
     *
     * @param topicName 主题名称
     * @param clusterId 集群ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 消费者组分页数据
     */
    Map<String, Object> getTopicConsumerGroups(String topicName, String clusterId, Integer page, Integer pageSize);
}