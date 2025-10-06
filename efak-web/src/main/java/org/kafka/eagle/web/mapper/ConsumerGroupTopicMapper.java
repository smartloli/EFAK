package org.kafka.eagle.web.mapper;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import org.kafka.eagle.dto.consumer.ConsumerGroupTopicInfo;
import org.kafka.eagle.dto.consumer.ConsumerGroupTopicQueryRequest;
import org.kafka.eagle.dto.consumer.ConsumerGroupTopicInsertRequest;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * ConsumerGroupTopic 数据访问层接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/27 23:32:05
 * @version 5.0.0
 */
@Mapper
public interface ConsumerGroupTopicMapper {

    /**
     * 分页查询消费者组主题数据
     *
     * @param request 查询条件
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 消费者组主题数据列表
     */
    @Select("<script>" +
            "SELECT id, cluster_id, group_id, topic_name, state, logsize, offsets, lags, " +
            "collect_time, collect_date " +
            "FROM ke_consumer_group_topic " +
            "<where>" +
            "<if test='request.clusterId != null and request.clusterId != \"\"'>" +
            "AND cluster_id = #{request.clusterId}" +
            "</if>" +
            "<if test='request.groupId != null and request.groupId != \"\"'>" +
            "AND group_id = #{request.groupId}" +
            "</if>" +
            "<if test='request.topicName != null and request.topicName != \"\"'>" +
            "AND topic_name = #{request.topicName}" +
            "</if>" +
            "<if test='request.state != null and request.state != \"\"'>" +
            "AND state = #{request.state}" +
            "</if>" +
            "<if test='request.collectDate != null'>" +
            "AND collect_date = #{request.collectDate}" +
            "</if>" +
            "<if test='request.startTime != null and request.endTime != null'>" +
            "AND collect_time BETWEEN #{request.startTime} AND #{request.endTime}" +
            "</if>" +
            "<if test='request.search != null and request.search != \"\"'>" +
            "AND (group_id LIKE CONCAT('%', #{request.search}, '%') OR topic_name LIKE CONCAT('%', #{request.search}, '%'))" +
            "</if>" +
            "</where>" +
            "<if test='request.sortField != null and request.sortField != \"\"'>" +
            "ORDER BY ${request.sortField}" +
            "</if>" +
            "<if test='request.sortOrder != null and request.sortOrder != \"\"'>" +
            " ${request.sortOrder}" +
            "</if>" +
            " LIMIT #{offset}, #{limit}" +
            "</script>")
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT),
            @Result(column = "cluster_id", property = "clusterId", jdbcType = JdbcType.VARCHAR),
            @Result(column = "group_id", property = "groupId", jdbcType = JdbcType.VARCHAR),
            @Result(column = "topic_name", property = "topicName", jdbcType = JdbcType.VARCHAR),
            @Result(column = "state", property = "state", jdbcType = JdbcType.VARCHAR),
            @Result(column = "logsize", property = "logsize", jdbcType = JdbcType.BIGINT),
            @Result(column = "offsets", property = "offsets", jdbcType = JdbcType.BIGINT),
            @Result(column = "lags", property = "lags", jdbcType = JdbcType.BIGINT),
            @Result(column = "collect_time", property = "collectTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "collect_date", property = "collectDate", jdbcType = JdbcType.DATE)
    })
    List<ConsumerGroupTopicInfo> selectConsumerGroupTopicPage(@Param("request") ConsumerGroupTopicQueryRequest request,
                                                              @Param("offset") int offset,
                                                              @Param("limit") int limit);

    /**
     * 查询消费者组主题数据总数
     *
     * @param request 查询条件
     * @return 总数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM ke_consumer_group_topic " +
            "<where>" +
            "<if test='request.clusterId != null and request.clusterId != \"\"'>" +
            "AND cluster_id = #{request.clusterId}" +
            "</if>" +
            "<if test='request.groupId != null and request.groupId != \"\"'>" +
            "AND group_id = #{request.groupId}" +
            "</if>" +
            "<if test='request.topicName != null and request.topicName != \"\"'>" +
            "AND topic_name = #{request.topicName}" +
            "</if>" +
            "<if test='request.state != null and request.state != \"\"'>" +
            "AND state = #{request.state}" +
            "</if>" +
            "<if test='request.collectDate != null'>" +
            "AND collect_date = #{request.collectDate}" +
            "</if>" +
            "<if test='request.startTime != null and request.endTime != null'>" +
            "AND collect_time BETWEEN #{request.startTime} AND #{request.endTime}" +
            "</if>" +
            "<if test='request.search != null and request.search != \"\"'>" +
            "AND (group_id LIKE CONCAT('%', #{request.search}, '%') OR topic_name LIKE CONCAT('%', #{request.search}, '%'))" +
            "</if>" +
            "</where>" +
            "</script>")
    Long countConsumerGroupTopic(@Param("request") ConsumerGroupTopicQueryRequest request);

    /**
     * 插入消费者组主题数据
     *
     * @param request 插入请求
     * @return 影响行数
     */
    @Insert("INSERT INTO ke_consumer_group_topic (cluster_id, group_id, topic_name, state, logsize, " +
            "offsets, lags, collect_time, collect_date) " +
            "VALUES (#{clusterId}, #{groupId}, #{topicName}, #{state}, #{logsize}, " +
            "#{offsets}, #{lags}, #{collectTime}, #{collectDate})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertConsumerGroupTopic(ConsumerGroupTopicInsertRequest request);

    /**
     * 批量插入消费者组主题数据
     *
     * @param requests 插入请求列表
     * @return 影响行数
     */
    @Insert("<script>" +
            "INSERT INTO ke_consumer_group_topic (cluster_id, group_id, topic_name, state, logsize, " +
            "offsets, lags, collect_time, collect_date) VALUES " +
            "<foreach collection='requests' item='item' separator=','>" +
            "(#{item.clusterId}, #{item.groupId}, #{item.topicName}, #{item.state}, #{item.logsize}, " +
            "#{item.offsets}, #{item.lags}, #{item.collectTime}, #{item.collectDate})" +
            "</foreach>" +
            "</script>")
    int batchInsertConsumerGroupTopic(@Param("requests") List<ConsumerGroupTopicInsertRequest> requests);

    /**
     * 根据ID查询消费者组主题数据
     *
     * @param id 主键ID
     * @return 消费者组主题数据
     */
    @Select("SELECT id, cluster_id, group_id, topic_name, state, logsize, offsets, lags, " +
            "collect_time, collect_date " +
            "FROM ke_consumer_group_topic WHERE id = #{id}")
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT),
            @Result(column = "cluster_id", property = "clusterId", jdbcType = JdbcType.VARCHAR),
            @Result(column = "group_id", property = "groupId", jdbcType = JdbcType.VARCHAR),
            @Result(column = "topic_name", property = "topicName", jdbcType = JdbcType.VARCHAR),
            @Result(column = "state", property = "state", jdbcType = JdbcType.VARCHAR),
            @Result(column = "logsize", property = "logsize", jdbcType = JdbcType.BIGINT),
            @Result(column = "offsets", property = "offsets", jdbcType = JdbcType.BIGINT),
            @Result(column = "lags", property = "lags", jdbcType = JdbcType.BIGINT),
            @Result(column = "collect_time", property = "collectTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "collect_date", property = "collectDate", jdbcType = JdbcType.DATE)
    })
    ConsumerGroupTopicInfo selectConsumerGroupTopicById(@Param("id") Long id);

    /**
     * 删除指定日期之前的数据
     *
     * @param beforeDate 指定日期 (格式: YYYY-MM-DD)
     * @return 影响行数
     */
    @Delete("DELETE FROM ke_consumer_group_topic WHERE collect_date < #{beforeDate}")
    int deleteDataBeforeDate(@Param("beforeDate") String beforeDate);

    /**
     * 根据集群ID删除数据
     *
     * @param clusterId 集群ID
     * @return 影响行数
     */
    @Delete("DELETE FROM ke_consumer_group_topic WHERE cluster_id = #{clusterId}")
    int deleteByClusterId(@Param("clusterId") String clusterId);

    /**
     * 根据消费者组状态删除数据
     *
     * @param state 消费者组状态
     * @param beforeDate 指定日期之前
     * @return 影响行数
     */
    @Delete("DELETE FROM ke_consumer_group_topic WHERE state = #{state} AND collect_date < #{beforeDate}")
    int deleteByStateBeforeDate(@Param("state") String state, @Param("beforeDate") String beforeDate);

    /**
     * 获取延迟统计信息
     *
     * @param clusterId 集群ID
     * @param collectDate 采集日期
     * @return 延迟统计数据
     */
    @Select("SELECT state, COUNT(*) as count, AVG(lags) as avgLags, MAX(lags) as maxLags " +
            "FROM ke_consumer_group_topic " +
            "WHERE cluster_id = #{clusterId} AND collect_date = #{collectDate} " +
            "GROUP BY state")
    List<Map<String, Object>> getLagStatsByClusterAndDate(@Param("clusterId") String clusterId,
                                                           @Param("collectDate") String collectDate);

    // ==================== 消费者组统计查询方法 ====================

    /**
     * 获取消费者组总数
     */
    @Select("SELECT COUNT(DISTINCT group_id) FROM ke_consumer_group_topic WHERE cluster_id = #{clusterId} AND collect_date = #{collectDate}")
    Integer getTotalGroups(@Param("clusterId") String clusterId, @Param("collectDate") String collectDate);

    /**
     * 获取活跃消费者组数（状态为STABLE）
     */
    @Select("SELECT COUNT(DISTINCT group_id) FROM ke_consumer_group_topic WHERE cluster_id = #{clusterId} AND collect_date = #{collectDate} AND state = 'STABLE'")
    Integer getActiveGroups(@Param("clusterId") String clusterId, @Param("collectDate") String collectDate);

    /**
     * 获取空闲消费者组数（状态为EMPTY）
     */
    @Select("SELECT COUNT(DISTINCT group_id) FROM ke_consumer_group_topic WHERE cluster_id = #{clusterId} AND collect_date = #{collectDate} AND state = 'EMPTY'")
    Integer getInactiveGroups(@Param("clusterId") String clusterId, @Param("collectDate") String collectDate);

    /**
     * 获取最新的lags和logsize总和（用于计算平均延迟率）
     */
    @Select("SELECT " +
            "COALESCE(SUM(lags), 0) as totalLags, " +
            "COALESCE(SUM(logsize), 0) as totalLogsize " +
            "FROM ke_consumer_group_topic " +
            "WHERE cluster_id = #{clusterId} AND collect_date = #{collectDate} " +
            "AND collect_time = (SELECT MAX(collect_time) FROM ke_consumer_group_topic WHERE cluster_id = #{clusterId} AND collect_date = #{collectDate})")
    Map<String, Object> getLatestLagsAndLogsize(@Param("clusterId") String clusterId, @Param("collectDate") String collectDate);

    /**
     * 获取按group_id和topic分组的延迟率统计（用于分类慢速和延迟消费者组）
     */
    @Select("SELECT " +
            "group_id, " +
            "topic_name as topic, " +
            "CASE " +
            "  WHEN logsize = 0 THEN 0 " +
            "  ELSE ROUND(lags * 100.0 / logsize, 2) " +
            "END as lag_rate " +
            "FROM ke_consumer_group_topic " +
            "WHERE cluster_id = #{clusterId} AND collect_date = #{collectDate} " +
            "AND collect_time = (SELECT MAX(collect_time) FROM ke_consumer_group_topic WHERE cluster_id = #{clusterId} AND collect_date = #{collectDate})")
    List<Map<String, Object>> getGroupTopicLagRates(@Param("clusterId") String clusterId, @Param("collectDate") String collectDate);

    /**
     * 获取最近两个时间点的所有offset数据（用于计算消费速度）
     * 兼容所有MySQL版本的查询方式
     */
    @Select("SELECT " +
            "group_id, " +
            "topic_name as topic, " +
            "collect_time, " +
            "offsets " +
            "FROM ke_consumer_group_topic " +
            "WHERE cluster_id = #{clusterId} AND collect_date = #{collectDate} " +
            "ORDER BY collect_time DESC, group_id, topic_name " +
            "LIMIT 1000")
    List<Map<String, Object>> getOffsetsForConsumerRate(@Param("clusterId") String clusterId, @Param("collectDate") String collectDate);

    /**
     * 获取最近24小时消费者组趋势数据
     */
    @Select("SELECT " +
            "  DATE_FORMAT(collect_time, '%Y-%m-%d %H:%i') as time_point, " +
            "  COUNT(DISTINCT CASE WHEN state = 'STABLE' THEN group_id END) as active_groups, " +
            "  COUNT(DISTINCT CASE WHEN state = 'EMPTY' THEN group_id END) as idle_groups, " +
            "  collect_time " +
            "FROM ke_consumer_group_topic " +
            "WHERE cluster_id = #{clusterId} " +
            "  AND collect_time >= DATE_SUB(NOW(), INTERVAL #{hours} HOUR) " +
            "  AND (state = 'STABLE' OR state = 'EMPTY') " +
            "GROUP BY DATE_FORMAT(collect_time, '%Y-%m-%d %H:%i'), collect_time " +
            "ORDER BY collect_time")
    List<Map<String, Object>> getIdleGroupsTrend(@Param("clusterId") String clusterId, @Param("hours") int hours);

    // ==================== 消费者组列表查询方法 ====================

    /**
     * 获取消费者组列表（去重，获取最新数据）
     */
    @Select("<script>" +
            "SELECT " +
            "  t1.group_id, " +
            "  t1.topic_name, " +
            "  t1.state, " +
            "  t1.lags as total_lag, " +
            "  t1.logsize, " +
            "  CASE " +
            "    WHEN t1.logsize = 0 THEN 0.00 " +
            "    ELSE ROUND((t1.lags * 100.0 / t1.logsize), 2) " +
            "  END as lag_rate, " +
            "  t1.collect_time " +
            "FROM ke_consumer_group_topic t1 " +
            "INNER JOIN ( " +
            "  SELECT group_id, topic_name, MAX(collect_time) as max_time " +
            "  FROM ke_consumer_group_topic " +
            "  WHERE cluster_id = #{clusterId} AND collect_date = #{collectDate} " +
            "  <if test='search != null and search != \"\"'>" +
            "    AND (group_id LIKE CONCAT('%', #{search}, '%') OR topic_name LIKE CONCAT('%', #{search}, '%'))" +
            "  </if>" +
            "  GROUP BY group_id, topic_name " +
            ") t2 ON t1.group_id = t2.group_id AND t1.topic_name = t2.topic_name AND t1.collect_time = t2.max_time " +
            "WHERE t1.cluster_id = #{clusterId} AND t1.collect_date = #{collectDate} " +
            "ORDER BY t1.group_id, t1.topic_name " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Map<String, Object>> getConsumerGroupsList(@Param("clusterId") String clusterId,
                                                    @Param("collectDate") String collectDate,
                                                    @Param("search") String search,
                                                    @Param("offset") int offset,
                                                    @Param("limit") int limit);

    /**
     * 统计消费者组列表总数（去重）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM ( " +
            "  SELECT DISTINCT group_id, topic_name " +
            "  FROM ke_consumer_group_topic " +
            "  WHERE cluster_id = #{clusterId} AND collect_date = #{collectDate} " +
            "  <if test='search != null and search != \"\"'>" +
            "    AND (group_id LIKE CONCAT('%', #{search}, '%') OR topic_name LIKE CONCAT('%', #{search}, '%'))" +
            "  </if>" +
            ") as temp" +
            "</script>")
    Long countConsumerGroups(@Param("clusterId") String clusterId,
                            @Param("collectDate") String collectDate,
                            @Param("search") String search);

    /**
     * 获取消费者组主题数据列表（用于Service中的查询）
     */
    @Select("<script>" +
            "SELECT id, cluster_id, group_id, topic_name, state, logsize, offsets, lags, " +
            "collect_time, collect_date " +
            "FROM ke_consumer_group_topic " +
            "<where>" +
            "<if test='request.clusterId != null and request.clusterId != \"\"'>" +
            "AND cluster_id = #{request.clusterId}" +
            "</if>" +
            "<if test='request.groupId != null and request.groupId != \"\"'>" +
            "AND group_id = #{request.groupId}" +
            "</if>" +
            "<if test='request.topicName != null and request.topicName != \"\"'>" +
            "AND topic_name = #{request.topicName}" +
            "</if>" +
            "<if test='request.state != null and request.state != \"\"'>" +
            "AND state = #{request.state}" +
            "</if>" +
            "<if test='request.collectDate != null'>" +
            "AND collect_date = #{request.collectDate}" +
            "</if>" +
            "<if test='request.startTime != null and request.endTime != null'>" +
            "AND collect_time BETWEEN #{request.startTime} AND #{request.endTime}" +
            "</if>" +
            "<if test='request.search != null and request.search != \"\"'>" +
            "AND (group_id LIKE CONCAT('%', #{request.search}, '%') OR topic_name LIKE CONCAT('%', #{request.search}, '%'))" +
            "</if>" +
            "</where>" +
            "ORDER BY collect_time DESC" +
            "<if test='request.page != null and request.pageSize != null'>" +
            " LIMIT #{request.pageSize}" +
            "</if>" +
            "</script>")
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT),
            @Result(column = "cluster_id", property = "clusterId", jdbcType = JdbcType.VARCHAR),
            @Result(column = "group_id", property = "groupId", jdbcType = JdbcType.VARCHAR),
            @Result(column = "topic_name", property = "topicName", jdbcType = JdbcType.VARCHAR),
            @Result(column = "state", property = "state", jdbcType = JdbcType.VARCHAR),
            @Result(column = "logsize", property = "logsize", jdbcType = JdbcType.BIGINT),
            @Result(column = "offsets", property = "offsets", jdbcType = JdbcType.BIGINT),
            @Result(column = "lags", property = "lags", jdbcType = JdbcType.BIGINT),
            @Result(column = "collect_time", property = "collectTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "collect_date", property = "collectDate", jdbcType = JdbcType.DATE)
    })
    List<ConsumerGroupTopicInfo> getConsumerGroupTopicList(@Param("request") ConsumerGroupTopicQueryRequest request);

    /**
     * 获取消费者组延迟趋势数据
     * 根据时间戳范围查询指定消费者组和主题的延迟数据
     *
     * @param clusterId 集群ID
     * @param groupId 消费者组ID
     * @param topic 主题名称
     * @param startTimestamp 开始时间戳（毫秒）
     * @param endTimestamp 结束时间戳（毫秒）
     * @return 延迟趋势数据
     */
    @Select("SELECT " +
            "collect_time, " +
            "lags " +
            "FROM ke_consumer_group_topic " +
            "WHERE cluster_id = #{clusterId} " +
            "AND group_id = #{groupId} " +
            "AND topic_name = #{topic} " +
            "AND UNIX_TIMESTAMP(collect_time) * 1000 >= #{startTimestamp} " +
            "AND UNIX_TIMESTAMP(collect_time) * 1000 <= #{endTimestamp} " +
            "ORDER BY collect_time ASC")
    List<Map<String, Object>> getConsumerGroupLagTrend(@Param("clusterId") String clusterId,
                                                       @Param("groupId") String groupId,
                                                       @Param("topic") String topic,
                                                       @Param("startTimestamp") long startTimestamp,
                                                       @Param("endTimestamp") long endTimestamp);

    /**
     * 获取指定消费者组和主题的最新2条记录（用于计算读写速度）
     *
     * @param clusterId 集群ID
     * @param groupId 消费者组ID
     * @param topic 主题名称
     * @return 最新的2条记录数据
     */
    @Select("SELECT " +
            "collect_time, " +
            "logsize, " +
            "offsets " +
            "FROM ke_consumer_group_topic " +
            "WHERE cluster_id = #{clusterId} " +
            "AND group_id = #{groupId} " +
            "AND topic_name = #{topic} " +
            "ORDER BY collect_time DESC " +
            "LIMIT 2")
    List<Map<String, Object>> getLatestTwoRecordsForSpeed(@Param("clusterId") String clusterId,
                                                          @Param("groupId") String groupId,
                                                          @Param("topic") String topic);

    /**
     * 获取指定消费者组和主题当天的所有记录（用于计算详细指标）
     *
     * @param clusterId 集群ID
     * @param groupId 消费者组ID
     * @param topic 主题名称
     * @param collectDate 采集日期（格式：YYYYMMDD）
     * @return 当天的所有记录数据
     */
    @Select("SELECT " +
            "collect_time, " +
            "state, " +
            "logsize, " +
            "offsets, " +
            "lags " +
            "FROM ke_consumer_group_topic " +
            "WHERE cluster_id = #{clusterId} " +
            "AND group_id = #{groupId} " +
            "AND topic_name = #{topic} " +
            "AND collect_date = #{collectDate} " +
            "ORDER BY collect_time ASC")
    List<Map<String, Object>> getTodayRecordsForDetail(@Param("clusterId") String clusterId,
                                                       @Param("groupId") String groupId,
                                                       @Param("topic") String topic,
                                                       @Param("collectDate") String collectDate);

    /**
     * 获取指定消费者组和主题的最新状态
     *
     * @param clusterId 集群ID
     * @param groupId 消费者组ID
     * @param topic 主题名称
     * @return 最新状态记录
     */
    @Select("SELECT " +
            "state, " +
            "collect_time " +
            "FROM ke_consumer_group_topic " +
            "WHERE cluster_id = #{clusterId} " +
            "AND group_id = #{groupId} " +
            "AND topic_name = #{topic} " +
            "ORDER BY collect_time DESC " +
            "LIMIT 1")
    Map<String, Object> getLatestStateForDetail(@Param("clusterId") String clusterId,
                                               @Param("groupId") String groupId,
                                               @Param("topic") String topic);

    /**
     * 根据集群ID和日期统计去重后的消费者组数量
     *
     * @param clusterId 集群ID
     * @param collectDate 采集日期（格式：yyyy-MM-dd）
     * @return 消费者组数量
     */
    @Select("SELECT COUNT(DISTINCT group_id) FROM ke_consumer_group_topic " +
            "WHERE cluster_id = #{clusterId} AND collect_date = #{collectDate}")
    Long countDistinctGroupsByClusterIdAndDate(@Param("clusterId") String clusterId,
                                               @Param("collectDate") String collectDate);

    /**
     * 获取指定主题的消费者组信息（用于主题详情页面）
     * 查询最新时间的消费者组数据，按集群ID和主题名称过滤
     *
     * @param clusterId cluster ID
     * @param topicName topic name
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 消费者组信息列表
     */
    @Select("SELECT " +
            "t1.group_id, " +
            "t1.topic_name, " +
            "t1.state, " +
            "t1.logsize, " +
            "t1.offsets, " +
            "t1.lags, " +
            "t1.collect_time " +
            "FROM ke_consumer_group_topic t1 " +
            "INNER JOIN ( " +
            "  SELECT group_id, MAX(collect_time) as max_time " +
            "  FROM ke_consumer_group_topic " +
            "  WHERE cluster_id = #{clusterId} AND topic_name = #{topicName} " +
            "  AND collect_date = CURDATE() " +
            "  GROUP BY group_id " +
            ") t2 ON t1.group_id = t2.group_id AND t1.collect_time = t2.max_time " +
            "WHERE t1.cluster_id = #{clusterId} AND t1.topic_name = #{topicName} " +
            "ORDER BY t1.group_id " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> getConsumerGroupsByTopicForDetail(@Param("clusterId") String clusterId,
                                                               @Param("topicName") String topicName,
                                                               @Param("offset") int offset,
                                                               @Param("limit") int limit);

    /**
     * 统计指定主题的消费者组总数（用于主题详情页面分页）
     *
     * @param clusterId cluster ID
     * @param topicName topic name
     * @return 消费者组总数
     */
    @Select("SELECT COUNT(DISTINCT group_id) " +
            "FROM ke_consumer_group_topic " +
            "WHERE cluster_id = #{clusterId} AND topic_name = #{topicName} " +
            "AND collect_date = CURDATE()")
    Long countConsumerGroupsByTopicForDetail(@Param("clusterId") String clusterId,
                                           @Param("topicName") String topicName);

    /**
     * 根据集群ID获取消费者组列表（用于告警配置下拉框）
     */
    @Select("SELECT DISTINCT group_id as groupId " +
            "FROM ke_consumer_group_topic " +
            "WHERE cluster_id = #{clusterId} " +
            "AND collect_date = CURDATE() " +
            "ORDER BY group_id ASC")
    List<Map<String, Object>> getConsumerGroupsByClusterId(@Param("clusterId") String clusterId);

    /**
     * 根据消费者组ID获取Topic列表（用于告警配置下拉框）
     */
    @Select("SELECT DISTINCT topic_name as topic " +
            "FROM ke_consumer_group_topic " +
            "WHERE cluster_id = #{clusterId} " +
            "AND group_id = #{groupId} " +
            "AND collect_date = CURDATE() " +
            "ORDER BY topic_name ASC")
    List<Map<String, Object>> getTopicsByGroupId(@Param("clusterId") String clusterId,
                                                 @Param("groupId") String groupId);

    /**
     * 根据集群ID查询消费者组信息
     */
    @Select("SELECT " +
            "group_id as groupId, " +
            "topic_name as topicName, " +
            "state, " +
            "logsize, " +
            "offsets, " +
            "lags, " +
            "collect_time as collectTime " +
            "FROM ke_consumer_group_topic " +
            "WHERE cluster_id = #{clusterId} " +
            "ORDER BY collect_time DESC LIMIT 100")
    List<Map<String, Object>> findByClusterId(@Param("clusterId") String clusterId);

    /**
     * 根据集群ID和group_id查询消费者组信息
     */
    @Select("SELECT " +
            "group_id as groupId, " +
            "topic_name as topicName, " +
            "state, " +
            "logsize, " +
            "offsets, " +
            "lags, " +
            "collect_time as collectTime " +
            "FROM ke_consumer_group_topic " +
            "WHERE cluster_id = #{clusterId} AND group_id = #{groupId} " +
            "ORDER BY collect_time DESC LIMIT 100")
    List<Map<String, Object>> findByClusterIdAndGroupId(@Param("clusterId") String clusterId,
                                                         @Param("groupId") String groupId);

    /**
     * 根据集群ID和topic查询消费者组信息
     */
    @Select("SELECT " +
            "group_id as groupId, " +
            "topic_name as topicName, " +
            "state, " +
            "logsize, " +
            "offsets, " +
            "lags, " +
            "collect_time as collectTime " +
            "FROM ke_consumer_group_topic " +
            "WHERE cluster_id = #{clusterId} AND topic_name = #{topic} " +
            "ORDER BY collect_time DESC LIMIT 100")
    List<Map<String, Object>> findByClusterIdAndTopic(@Param("clusterId") String clusterId,
                                                       @Param("topic") String topic);

    /**
     * 根据集群ID、group_id和topic查询消费者组信息
     */
    @Select("SELECT " +
            "group_id as groupId, " +
            "topic_name as topicName, " +
            "state, " +
            "logsize, " +
            "offsets, " +
            "lags, " +
            "collect_time as collectTime " +
            "FROM ke_consumer_group_topic " +
            "WHERE cluster_id = #{clusterId} AND group_id = #{groupId} AND topic_name = #{topic} " +
            "ORDER BY collect_time DESC LIMIT 100")
    List<Map<String, Object>> findByClusterIdAndGroupIdAndTopic(@Param("clusterId") String clusterId,
                                                                 @Param("groupId") String groupId,
                                                                 @Param("topic") String topic);
}