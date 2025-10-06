package org.kafka.eagle.web.service;

import org.kafka.eagle.dto.consumer.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 消费者组主题数据采集服务接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/27 13:25:17
 * @version 5.0.0
 */
public interface ConsumerGroupTopicService {

    /**
     * 分页查询消费者组主题数据
     *
     * @param request 查询条件
     * @return 分页结果
     */
    ConsumerGroupTopicPageResponse getConsumerGroupTopicPage(ConsumerGroupTopicQueryRequest request);

    /**
     * 插入消费者组主题数据
     *
     * @param request 插入请求
     * @return 插入结果
     */
    boolean insertConsumerGroupTopic(ConsumerGroupTopicInsertRequest request);

    /**
     * 批量插入消费者组主题数据
     *
     * @param requests 插入请求列表
     * @return 插入结果
     */
    boolean batchInsertConsumerGroupTopic(List<ConsumerGroupTopicInsertRequest> requests);

    /**
     * 根据ID查询消费者组主题数据
     *
     * @param id 主键ID
     * @return 消费者组主题数据
     */
    ConsumerGroupTopicInfo getConsumerGroupTopicById(Long id);

    /**
     * 清理指定时间之前的数据
     *
     * @param beforeDay 指定日期（YYYYMMDD格式）
     * @return 清理结果
     */
    boolean cleanDataBeforeDay(String beforeDay);

    /**
     * 根据集群ID删除数据
     *
     * @param clusterId 集群ID
     * @return 删除结果
     */
    boolean deleteByClusterId(String clusterId);

    /**
     * 获取指定集群的消费者组统计信息
     *
     * @param clusterId 集群ID
     * @return 消费者组统计信息
     */
    Map<String, Object> getConsumerStats(String clusterId);

    /**
     * 获取空闲消费者组趋势数据
     *
     * @param clusterId 集群ID
     * @param timeRange 时间范围（如"1d"表示最近24小时）
     * @return 趋势数据列表
     */
    List<Map<String, Object>> getIdleGroupsTrend(String clusterId, String timeRange);

    /**
     * 获取消费者组列表（分页）
     *
     * @param clusterId 集群ID
     * @param search 搜索关键字（消费者组ID或主题名称）
     * @param page 页码
     * @param pageSize 每页大小
     * @return 消费者组分页数据
     */
    ConsumerGroupPageResponse getConsumerGroupsList(String clusterId, String search, int page, int pageSize);

    /**
     * 获取消费者组详细信息
     *
     * @param clusterId 集群ID
     * @param groupId 消费者组ID
     * @param topic 主题名称
     * @return 消费者组详细信息
     */
    Map<String, Object> getConsumerGroupDetail(String clusterId, String groupId, String topic);

    /**
     * 获取消费者组速度数据
     *
     * @param clusterId 集群ID
     * @param groupId 消费者组ID
     * @param topic 主题名称
     * @return 速度数据
     */
    Map<String, Object> getConsumerGroupSpeed(String clusterId, String groupId, String topic);

    /**
     * 获取消费者组延迟趋势数据
     *
     * @param clusterId 集群ID
     * @param groupId 消费者组ID
     * @param topic 主题名称
     * @param timeRange 时间范围
     * @return 延迟趋势数据
     */
    List<Map<String, Object>> getConsumerGroupLagTrend(String clusterId, String groupId, String topic, String timeRange);

    /**
     * 重置消费者组偏移量（使用KafkaClientInfo）
     *
     * @param kafkaClientInfo Kafka客户端信息
     * @param groupId 消费者组ID
     * @param topic 主题名称
     * @param resetType 重置类型（earliest, latest, specific, timestamp）
     * @param resetValue 重置值（当resetType为specific或timestamp时需要）
     * @return 重置结果
     */
    boolean resetConsumerGroupOffsetWithClientInfo(org.kafka.eagle.dto.cluster.KafkaClientInfo kafkaClientInfo, String groupId, String topic, String resetType, Long resetValue);
}