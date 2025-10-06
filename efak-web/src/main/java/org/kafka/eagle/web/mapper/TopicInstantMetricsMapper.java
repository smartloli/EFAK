package org.kafka.eagle.web.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.kafka.eagle.dto.topic.TopicInstantMetrics;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Topic实时指标Mapper
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/29 22:07:19
 * @version 5.0.0
 */
@Mapper
public interface TopicInstantMetricsMapper {

    /**
     * Batch insert or update topic instant metrics
     * 批量插入或更新指标数据（根据cluster_id, topic_name, metric_type作为唯一键）
     *
     * @param metrics list of topic instant metrics
     * @return affected rows
     */
    @Insert("<script>" +
            "INSERT INTO ke_topic_instant_metrics " +
            "(cluster_id, topic_name, metric_type, metric_value, last_updated) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.clusterId}, #{item.topicName}, #{item.metricType}, #{item.metricValue}, NOW())" +
            "</foreach> " +
            "ON DUPLICATE KEY UPDATE " +
            "metric_value = VALUES(metric_value), " +
            "last_updated = VALUES(last_updated)" +
            "</script>")
    int batchUpsertMetrics(@Param("list") List<TopicInstantMetrics> metrics);

    /**
     * Get top N topics by specific metric in cluster
     * 根据集群ID、指标类型、TOPN过滤条件查询数据
     *
     * @param clusterId cluster ID
     * @param metricType metric type (byte_out, capacity, logsize, byte_in)
     * @param limit top N limit
     * @return list of top metrics
     */
    @Select("SELECT " +
            "id, cluster_id as clusterId, topic_name as topicName, " +
            "metric_type as metricType, metric_value as metricValue, " +
            "last_updated as lastUpdated, create_time as createTime " +
            "FROM ke_topic_instant_metrics " +
            "WHERE cluster_id = #{clusterId} AND metric_type = #{metricType} " +
            "ORDER BY CAST(metric_value AS UNSIGNED) DESC " +
            "LIMIT #{limit}")
    List<TopicInstantMetrics> getTopMetricsByType(@Param("clusterId") String clusterId,
                                                     @Param("metricType") String metricType,
                                                     @Param("limit") int limit);

    /**
     * Get aggregated statistics from ke_topic_instant_metrics table
     * 从即时指标表中获取聚合统计数据
     *
     * @param clusterId cluster ID (optional)
     * @return aggregated statistics map
     */
    @Select("<script>" +
            "SELECT " +
            "SUM(CASE WHEN metric_type = 'capacity' THEN CAST(metric_value AS UNSIGNED) ELSE 0 END) as totalCapacity, " +
            "SUM(CASE WHEN metric_type = 'log_size' THEN CAST(metric_value AS UNSIGNED) ELSE 0 END) as totalRecordCount, " +
            "SUM(CASE WHEN metric_type = 'byte_in' THEN CAST(metric_value AS DECIMAL(20,2)) ELSE 0 END) as avgWriteSpeed, " +
            "SUM(CASE WHEN metric_type = 'byte_out' THEN CAST(metric_value AS DECIMAL(20,2)) ELSE 0 END) as avgReadSpeed " +
            "FROM ke_topic_instant_metrics " +
            "<where>" +
            "<if test='clusterId != null and clusterId != \"\"'>" +
            "cluster_id = #{clusterId}" +
            "</if>" +
            "</where>" +
            "</script>")
    java.util.Map<String, Object> getInstantMetricsStatistics(@Param("clusterId") String clusterId);

    /**
     * 统计活跃和空闲主题数量
     * 根据log_size指标判断主题活跃状态: metric_value=0为空闲，>0为活跃
     *
     * @param clusterId 集群ID
     * @return 包含activeCount和idleCount的映射
     */
    @Select("SELECT " +
            "SUM(CASE WHEN CAST(metric_value AS UNSIGNED) > 0 THEN 1 ELSE 0 END) as activeCount, " +
            "SUM(CASE WHEN CAST(metric_value AS UNSIGNED) = 0 THEN 1 ELSE 0 END) as idleCount " +
            "FROM ke_topic_instant_metrics " +
            "WHERE cluster_id = #{clusterId} AND metric_type = 'log_size'")
    java.util.Map<String, Object> getTopicActivityStats(@Param("clusterId") String clusterId);

    /**
     * 统计集群总容量
     * 查询所有capacity类型指标值的总和
     *
     * @param clusterId 集群ID
     * @return 总容量（字节）
     */
    @Select("SELECT COALESCE(SUM(CAST(metric_value AS UNSIGNED)), 0) as totalCapacity " +
            "FROM ke_topic_instant_metrics " +
            "WHERE cluster_id = #{clusterId} AND metric_type = 'capacity'")
    Long getTotalCapacityByCluster(@Param("clusterId") String clusterId);

    /**
     * 获取容量分布统计
     * 按不同容量区间统计主题数量：0-100MB, 100MB-1GB, 1GB-10GB, 10GB+
     *
     * @param clusterId 集群ID
     * @return 容量分布统计
     */
    @Select("SELECT " +
            "SUM(CASE WHEN CAST(metric_value AS UNSIGNED) >= 0 AND CAST(metric_value AS UNSIGNED) < 104857600 THEN 1 ELSE 0 END) as range0to100MB, " +
            "SUM(CASE WHEN CAST(metric_value AS UNSIGNED) >= 104857600 AND CAST(metric_value AS UNSIGNED) < 1073741824 THEN 1 ELSE 0 END) as range100MBto1GB, " +
            "SUM(CASE WHEN CAST(metric_value AS UNSIGNED) >= 1073741824 AND CAST(metric_value AS UNSIGNED) < 10737418240 THEN 1 ELSE 0 END) as range1GBto10GB, " +
            "SUM(CASE WHEN CAST(metric_value AS UNSIGNED) >= 10737418240 THEN 1 ELSE 0 END) as range10GBPlus " +
            "FROM ke_topic_instant_metrics " +
            "WHERE cluster_id = #{clusterId} AND metric_type = 'capacity'")
    java.util.Map<String, Object> getCapacityDistribution(@Param("clusterId") String clusterId);

    /**
     * 获取性能指标表格数据，结合主题信息、指标数据和数据倾斜信息
     * Get performance metrics table data with broker skew from ke_topic_info
     * Fixed to properly handle decimal metric values for byte rates
     *
     * @param clusterId cluster ID
     * @param metricType metric type (log_size for messages, capacity for size, byte_out for read, byte_in for write)
     * @param limit top N limit
     * @return performance metrics table data with broker skew
     */
    @Select("SELECT " +
            "t.topic_name as topicName, " +
            "t.partitions as partitions, " +
            "t.replicas as replicas, " +
            "t.broker_skewed as brokerSkewed, " +
            "COALESCE(m.metric_value, '0') as metricValue, " +
            "CASE WHEN COALESCE(log_m.metric_value, '0') = '0' THEN 'idle' ELSE 'active' END as status " +
            "FROM ke_topic_info t " +
            "LEFT JOIN ke_topic_instant_metrics m ON t.topic_name = m.topic_name AND t.cluster_id = m.cluster_id AND m.metric_type = #{metricType} " +
            "LEFT JOIN ke_topic_instant_metrics log_m ON t.topic_name = log_m.topic_name AND t.cluster_id = log_m.cluster_id AND log_m.metric_type = 'log_size' " +
            "WHERE t.cluster_id = #{clusterId} " +
            // Use DECIMAL casting instead of UNSIGNED to handle decimal values properly
            "ORDER BY CAST(COALESCE(m.metric_value, '0') AS DECIMAL(20,2)) DESC, t.topic_name ASC " +
            "LIMIT #{limit}")
    List<java.util.Map<String, Object>> getMetricsTableDataWithSkew(@Param("clusterId") String clusterId,
                                                                   @Param("metricType") String metricType,
                                                                   @Param("limit") int limit);

    /**
     * Debug method to check what metric types and values exist for a cluster
     * 调试方法：检查集群中存在哪些指标类型和值
     *
     * @param clusterId cluster ID
     * @return debug information about available metrics
     */
    @Select("SELECT " +
            "metric_type, " +
            "COUNT(*) as count, " +
            "AVG(CAST(metric_value AS DECIMAL(20,2))) as avg_value, " +
            "MAX(CAST(metric_value AS DECIMAL(20,2))) as max_value, " +
            "MIN(CAST(metric_value AS DECIMAL(20,2))) as min_value " +
            "FROM ke_topic_instant_metrics " +
            "WHERE cluster_id = #{clusterId} " +
            "AND metric_type IN ('byte_in', 'byte_out', 'log_size', 'capacity') " +
            "GROUP BY metric_type " +
            "ORDER BY metric_type")
    List<java.util.Map<String, Object>> getMetricTypesDebugInfo(@Param("clusterId") String clusterId);

    /**
     * Delete topic instant metrics by topic name and cluster ID
     * 根据主题名称和集群ID删除即时指标数据
     *
     * @param clusterId cluster ID
     * @param topicName topic name
     * @return affected rows
     */
    @Delete("DELETE FROM ke_topic_instant_metrics WHERE cluster_id = #{clusterId} AND topic_name = #{topicName}")
    int deleteByTopicNameAndClusterId(@Param("clusterId") String clusterId, @Param("topicName") String topicName);

    /**
     * 根据指标类型获取Topic列表（用于告警配置下拉框）
     */
    @Select("SELECT DISTINCT topic_name as topic " +
            "FROM ke_topic_instant_metrics " +
            "WHERE cluster_id = #{clusterId} " +
            "AND metric_type = #{metricType} " +
            "ORDER BY topic_name ASC")
    List<java.util.Map<String, Object>> getTopicsByMetricType(@Param("clusterId") String clusterId,
                                                             @Param("metricType") String metricType);

    /**
     * 根据集群ID、主题名称和指标类型查询最近一小时的最新记录
     * Query the most recent record within the last hour by cluster ID, topic name, and metric type
     *
     * @param clusterId cluster ID
     * @param topicName topic name
     * @param metricType metric type
     * @return list of topic instant metrics (usually one record - the most recent)
     */
    @Select("SELECT " +
            "id, cluster_id as clusterId, topic_name as topicName, " +
            "metric_type as metricType, metric_value as metricValue, " +
            "last_updated as lastUpdated, create_time as createTime " +
            "FROM ke_topic_instant_metrics " +
            "WHERE cluster_id = #{clusterId} " +
            "AND topic_name = #{topicName} " +
            "AND metric_type = #{metricType} " +
            "AND last_updated >= DATE_SUB(NOW(), INTERVAL 1 HOUR) " +
            "ORDER BY last_updated DESC " +
            "LIMIT 1")
    List<TopicInstantMetrics> selectByTopicAndClusterAndMetricType(@Param("clusterId") String clusterId,
                                                                   @Param("topicName") String topicName,
                                                                   @Param("metricType") String metricType);

    /**
     * 根据集群ID查询Topic即时指标
     */
    @Select("SELECT " +
            "topic_name as topicName, " +
            "metric_type as metricType, " +
            "metric_value as metricValue, " +
            "last_updated as lastUpdated " +
            "FROM ke_topic_instant_metrics " +
            "WHERE cluster_id = #{clusterId} " +
            "ORDER BY last_updated DESC LIMIT 100")
    List<Map<String, Object>> findByClusterId(@Param("clusterId") String clusterId);

    /**
     * 根据集群ID和Topic名称查询Topic即时指标
     */
    @Select("SELECT " +
            "topic_name as topicName, " +
            "metric_type as metricType, " +
            "metric_value as metricValue, " +
            "last_updated as lastUpdated " +
            "FROM ke_topic_instant_metrics " +
            "WHERE cluster_id = #{clusterId} AND topic_name = #{topic} " +
            "ORDER BY last_updated DESC LIMIT 100")
    List<Map<String, Object>> findByClusterIdAndTopic(@Param("clusterId") String clusterId, @Param("topic") String topic);
}