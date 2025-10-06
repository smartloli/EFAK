package org.kafka.eagle.web.mapper;

import org.apache.ibatis.annotations.*;
import org.kafka.eagle.dto.topic.TopicMetrics;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Topic指标Mapper
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/27 03:22:25
 * @version 5.0.0
 */
@Mapper
public interface TopicMetricsMapper {
    
    /**
     * Insert topic metrics data
     *
     * @param topicMetrics topic metrics information
     * @return affected rows
     */
    @Insert("INSERT INTO ke_topics_metrics (cluster_id, topic_name, record_count, capacity, write_speed, read_speed, " +
            "record_count_diff, capacity_diff, collect_time, create_time) " +
            "VALUES (#{clusterId}, #{topicName}, #{recordCount}, #{capacity}, #{writeSpeed}, #{readSpeed}, " +
            "#{recordCountDiff}, #{capacityDiff}, #{collectTime}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TopicMetrics topicMetrics);
    
    /**
     * Batch insert topic metrics data
     * 
     * @param topicMetricsList list of topic metrics
     * @return affected rows
     */
    @Insert("<script>" +
            "INSERT INTO ke_topics_metrics (cluster_id, topic_name, record_count, capacity, write_speed, read_speed, " +
            "record_count_diff, capacity_diff, collect_time, create_time) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.clusterId}, #{item.topicName}, #{item.recordCount}, #{item.capacity}, #{item.writeSpeed}, #{item.readSpeed}, " +
            "#{item.recordCountDiff}, #{item.capacityDiff}, #{item.collectTime}, #{item.createTime})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("list") List<TopicMetrics> topicMetricsList);
    
    /**
     * Query topic metrics by topic name with pagination
     * 
     * @param params query parameters
     * @return topic metrics list
     */
    @Select("<script>" +
            "SELECT id, cluster_id as clusterId, topic_name as topicName, record_count as recordCount, capacity, " +
            "write_speed as writeSpeed, read_speed as readSpeed, record_count_diff as recordCountDiff, " +
            "capacity_diff as capacityDiff, collect_time as collectTime, create_time as createTime " +
            "FROM ke_topics_metrics " +
            "<where>" +
            "<if test=\"clusterId != null and clusterId != ''\">" +
            "AND cluster_id = #{clusterId} " +
            "</if>" +
            "<if test=\"topicName != null and topicName != ''\">" +
            "AND topic_name = #{topicName} " +
            "</if>" +
            "<if test='startTime != null'>" +
            "AND collect_time >= #{startTime} " +
            "</if>" +
            "<if test='endTime != null'>" +
            "AND collect_time &lt;= #{endTime} " +
            "</if>" +
            "</where>" +
            "ORDER BY collect_time DESC " +
            "<if test='offset != null and limit != null'>" +
            "LIMIT #{offset}, #{limit}" +
            "</if>" +
            "</script>")
    List<TopicMetrics> selectTopicMetrics(Map<String, Object> params);
    
    /**
     * Count topic metrics records
     * 
     * @param params query parameters
     * @return total count
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM ke_topics_metrics " +
            "<where>" +
            "<if test=\"clusterId != null and clusterId != ''\">" +
            "AND cluster_id = #{clusterId} " +
            "</if>" +
            "<if test=\"topicName != null and topicName != ''\">" +
            "AND topic_name = #{topicName} " +
            "</if>" +
            "<if test='startTime != null'>" +
            "AND collect_time >= #{startTime} " +
            "</if>" +
            "<if test='endTime != null'>" +
            "AND collect_time &lt;= #{endTime} " +
            "</if>" +
            "</where>" +
            "</script>")
    int countTopicMetrics(Map<String, Object> params);
    
    /**
     * Get latest topic metrics for all topics
     * 
     * @return latest topic metrics list
     */
    @Select("SELECT tm1.id, tm1.cluster_id as clusterId, tm1.topic_name as topicName, tm1.record_count as recordCount, " +
            "tm1.capacity, tm1.write_speed as writeSpeed, tm1.read_speed as readSpeed, " +
            "tm1.record_count_diff as recordCountDiff, tm1.capacity_diff as capacityDiff, " +
            "tm1.collect_time as collectTime, tm1.create_time as createTime " +
            "FROM ke_topics_metrics tm1 " +
            "INNER JOIN (" +
            "  SELECT cluster_id, topic_name, MAX(collect_time) as max_collect_time " +
            "  FROM ke_topics_metrics " +
            "  GROUP BY cluster_id, topic_name" +
            ") tm2 ON tm1.cluster_id = tm2.cluster_id AND tm1.topic_name = tm2.topic_name AND tm1.collect_time = tm2.max_collect_time " +
            "ORDER BY tm1.cluster_id, tm1.topic_name")
    List<TopicMetrics> selectLatestTopicMetrics();
    
    /**
     * Delete old topic metrics data
     * 
     * @param beforeTime delete records before this time
     * @return affected rows
     */
    @Delete("DELETE FROM ke_topics_metrics WHERE collect_time < #{beforeTime}")
    int deleteOldMetrics(@Param("beforeTime") java.time.LocalDateTime beforeTime);
    
    /**
     * Get topic metrics statistics
     * 
     * @param topicName topic name
     * @param startTime start time
     * @param endTime end time
     * @return statistics map
     */
    @Select("SELECT " +
            "AVG(record_count) as avgRecordCount, " +
            "MAX(record_count) as maxRecordCount, " +
            "MIN(record_count) as minRecordCount, " +
            "AVG(capacity) as avgCapacity, " +
            "MAX(capacity) as maxCapacity, " +
            "MIN(capacity) as minCapacity, " +
            "AVG(write_speed) as avgWriteSpeed, " +
            "MAX(write_speed) as maxWriteSpeed, " +
            "MIN(write_speed) as minWriteSpeed, " +
            "AVG(read_speed) as avgReadSpeed, " +
            "MAX(read_speed) as maxReadSpeed, " +
            "MIN(read_speed) as minReadSpeed " +
            "FROM ke_topics_metrics " +
            "WHERE cluster_id = #{clusterId} " +
            "AND topic_name = #{topicName} " +
            "AND collect_time >= #{startTime} " +
            "AND collect_time <= #{endTime}")
    Map<String, Object> getTopicMetricsStatistics(@Param("clusterId") String clusterId,
                                                   @Param("topicName") String topicName,
                                                   @Param("startTime") java.time.LocalDateTime startTime,
                                                   @Param("endTime") java.time.LocalDateTime endTime);

    /**
     * Get current topic metrics statistics (latest data)
     *
     * @return current topic metrics statistics
     */
    @Select("SELECT " +
            "SUM(capacity) as totalCapacity, " +
            "SUM(record_count) as totalRecordCount, " +
            "AVG(read_speed) as avgReadSpeed, " +
            "AVG(write_speed) as avgWriteSpeed " +
            "FROM ke_topics_metrics tm1 " +
            "INNER JOIN (" +
            "  SELECT cluster_id, topic_name, MAX(collect_time) as max_collect_time " +
            "  FROM ke_topics_metrics " +
            "  WHERE collect_time >= DATE_SUB(NOW(), INTERVAL 1 DAY) " +
            "  GROUP BY cluster_id, topic_name" +
            ") tm2 ON tm1.cluster_id = tm2.cluster_id AND tm1.topic_name = tm2.topic_name AND tm1.collect_time = tm2.max_collect_time")
    Map<String, Object> getCurrentTopicStatistics();

    /**
     * Get yesterday's topic metrics statistics for comparison
     *
     * @return yesterday's topic metrics statistics
     */
    @Select("SELECT " +
            "SUM(capacity) as totalCapacity, " +
            "SUM(record_count) as totalRecordCount, " +
            "AVG(read_speed) as avgReadSpeed, " +
            "AVG(write_speed) as avgWriteSpeed " +
            "FROM ke_topics_metrics tm1 " +
            "INNER JOIN (" +
            "  SELECT cluster_id, topic_name, MAX(collect_time) as max_collect_time " +
            "  FROM ke_topics_metrics " +
            "  WHERE collect_time >= DATE_SUB(NOW(), INTERVAL 2 DAY) " +
            "  AND collect_time < DATE_SUB(NOW(), INTERVAL 1 DAY) " +
            "  GROUP BY cluster_id, topic_name" +
            ") tm2 ON tm1.cluster_id = tm2.cluster_id AND tm1.topic_name = tm2.topic_name AND tm1.collect_time = tm2.max_collect_time")
    Map<String, Object> getYesterdayTopicStatistics();

    /**
     * Get topic trend data for chart display
     * 根据时间范围查询总容量或总记录数的趋势数据
     *
     * @param dimension 数据维度 (capacity/messages)
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param topics 主题列表
     * @param clusterId 集群ID
     * @return 趋势数据
     */
    @Select("<script>" +
            "SELECT " +
            "DATE(collect_time) as date" +
            "<if test='topics != null and topics.size() > 0'>" +
            ", topic_name as topicName" +
            "</if>" +
            "<choose>" +
            "<when test=\'dimension == \"capacity\"'>" +
            ", SUM(capacity_diff) as totalCapacity" +
            "</when>" +
            "<otherwise>" +
            ", SUM(record_count_diff) as totalRecordCount" +
            "</otherwise>" +
            "</choose>" +
            " FROM ke_topics_metrics " +
            "WHERE DATE(collect_time) BETWEEN #{startDate} AND #{endDate} " +
            "<if test=\"clusterId != null and clusterId != ''\">" +
            "AND cluster_id = #{clusterId} " +
            "</if>" +
            "<if test='topics != null and topics.size() > 0'>" +
            "AND topic_name IN " +
            "<foreach collection='topics' item='topic' open='(' separator=',' close=')'>" +
            "#{topic}" +
            "</foreach> " +
            "</if>" +
            "GROUP BY DATE(collect_time)" +
            "<if test='topics != null and topics.size() > 0'>" +
            ", topic_name" +
            "</if> " +
            "ORDER BY date ASC" +
            "<if test='topics != null and topics.size() > 0'>" +
            ", topic_name ASC" +
            "</if>" +
            "</script>")
    List<Map<String, Object>> getTrendData(@Param("dimension") String dimension,
                                          @Param("startDate") String startDate,
                                          @Param("endDate") String endDate,
                                          @Param("topics") List<String> topics,
                                          @Param("clusterId") String clusterId);

    /**
     * Get latest topic metrics for specific cluster and topic
     *
     * @param clusterId cluster ID
     * @param topicName topic name
     * @return latest topic metrics
     */
    @Select("SELECT id, cluster_id as clusterId, topic_name as topicName, record_count as recordCount, " +
            "capacity, write_speed as writeSpeed, read_speed as readSpeed, " +
            "record_count_diff as recordCountDiff, capacity_diff as capacityDiff, " +
            "collect_time as collectTime, create_time as createTime " +
            "FROM ke_topics_metrics " +
            "WHERE cluster_id = #{clusterId} AND topic_name = #{topicName} " +
            "ORDER BY collect_time DESC " +
            "LIMIT 1")
    TopicMetrics selectLatestTopicMetricsByClusterAndTopic(@Param("clusterId") String clusterId,
                                                          @Param("topicName") String topicName);

    /**
     * Get topic message flow trend data for chart display
     * 根据时间范围查询消息流量趋势数据（用于生产消息流量趋势图表）
     *
     * @param clusterId 集群ID
     * @param topicName 主题名称
     * @param startTime 开始时间戳（毫秒）
     * @param endTime 结束时间戳（毫秒）
     * @return 消息流量趋势数据
     */
    @Select("SELECT " +
            "UNIX_TIMESTAMP(collect_time) * 1000 as timestamp, " +
            "record_count_diff as produced " +
            "FROM ke_topics_metrics " +
            "WHERE cluster_id = #{clusterId} " +
            "AND topic_name = #{topicName} " +
            "AND UNIX_TIMESTAMP(collect_time) * 1000 >= #{startTime} " +
            "AND UNIX_TIMESTAMP(collect_time) * 1000 <= #{endTime} " +
            "ORDER BY collect_time ASC")
    List<Map<String, Object>> getTopicMessageFlowTrend(@Param("clusterId") String clusterId,
                                                       @Param("topicName") String topicName,
                                                       @Param("startTime") Long startTime,
                                                       @Param("endTime") Long endTime);

    /**
     * Delete topic metrics by topic name and cluster ID
     * 根据主题名称和集群ID删除主题指标数据
     *
     * @param clusterId cluster ID
     * @param topicName topic name
     * @return affected rows
     */
    @Delete("DELETE FROM ke_topics_metrics WHERE cluster_id = #{clusterId} AND topic_name = #{topicName}")
    int deleteByTopicNameAndClusterId(@Param("clusterId") String clusterId, @Param("topicName") String topicName);

    /**
     * 根据集群ID查询Topic历史指标
     */
    @Select("SELECT " +
            "topic_name as topicName, " +
            "record_count as recordCount, " +
            "capacity, " +
            "write_speed as writeSpeed, " +
            "read_speed as readSpeed, " +
            "record_count_diff as recordCountDiff, " +
            "capacity_diff as capacityDiff, " +
            "collect_time as collectTime " +
            "FROM ke_topics_metrics " +
            "WHERE cluster_id = #{clusterId} " +
            "ORDER BY collect_time DESC LIMIT 100")
    List<Map<String, Object>> findByClusterId(@Param("clusterId") String clusterId);

    /**
     * 根据集群ID和Topic名称查询Topic历史指标
     */
    @Select("SELECT " +
            "topic_name as topicName, " +
            "record_count as recordCount, " +
            "capacity, " +
            "write_speed as writeSpeed, " +
            "read_speed as readSpeed, " +
            "record_count_diff as recordCountDiff, " +
            "capacity_diff as capacityDiff, " +
            "collect_time as collectTime " +
            "FROM ke_topics_metrics " +
            "WHERE cluster_id = #{clusterId} AND topic_name = #{topic} " +
            "ORDER BY collect_time DESC LIMIT 100")
    List<Map<String, Object>> findByClusterIdAndTopic(@Param("clusterId") String clusterId, @Param("topic") String topic);

    /**
     * 根据集群ID和时间范围查询Topic历史指标
     */
    @Select("SELECT " +
            "topic_name as topicName, " +
            "record_count as recordCount, " +
            "capacity, " +
            "write_speed as writeSpeed, " +
            "read_speed as readSpeed, " +
            "record_count_diff as recordCountDiff, " +
            "capacity_diff as capacityDiff, " +
            "collect_time as collectTime " +
            "FROM ke_topics_metrics " +
            "WHERE cluster_id = #{clusterId} " +
            "AND collect_time >= #{startTime} " +
            "AND collect_time <= #{endTime} " +
            "ORDER BY collect_time DESC LIMIT 100")
    List<Map<String, Object>> findByClusterIdAndTimeRange(@Param("clusterId") String clusterId,
                                                           @Param("startTime") String startTime,
                                                           @Param("endTime") String endTime);

    /**
     * 根据集群ID、Topic名称和时间范围查询Topic历史指标
     */
    @Select("SELECT " +
            "topic_name as topicName, " +
            "record_count as recordCount, " +
            "capacity, " +
            "write_speed as writeSpeed, " +
            "read_speed as readSpeed, " +
            "record_count_diff as recordCountDiff, " +
            "capacity_diff as capacityDiff, " +
            "collect_time as collectTime " +
            "FROM ke_topics_metrics " +
            "WHERE cluster_id = #{clusterId} " +
            "AND topic_name = #{topic} " +
            "AND collect_time >= #{startTime} " +
            "AND collect_time <= #{endTime} " +
            "ORDER BY collect_time DESC LIMIT 100")
    List<Map<String, Object>> findByClusterIdAndTopicAndTimeRange(@Param("clusterId") String clusterId,
                                                                   @Param("topic") String topic,
                                                                   @Param("startTime") String startTime,
                                                                   @Param("endTime") String endTime);
}