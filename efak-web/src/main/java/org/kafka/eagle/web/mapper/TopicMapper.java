package org.kafka.eagle.web.mapper;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import org.kafka.eagle.dto.topic.TopicInfo;
import org.kafka.eagle.dto.topic.TopicQueryRequest;

import java.util.List;

/**
 * <p>
 * Topic 数据访问层接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/25 22:41:40
 * @version 5.0.0
 */
@Mapper
public interface TopicMapper {
    
    /**
     * 分页查询Topic列表
     * 
     * @param request 查询条件
     * @param offset 偏移量
     * @param limit 限制数量
     * @return Topic列表
     */
    @Select("<script>" +
            "SELECT id, topic_name, partitions, replicas, broker_spread, broker_skewed, " +
            "leader_skewed, retention_time, icon, " +
            "create_time, update_time, create_by, update_by " +
            "FROM ke_topic_info " +
            "<where>" +
            "<if test='request.search != null and request.search != &quot;&quot;'>" +
            "AND topic_name LIKE CONCAT('%', #{request.search}, '%')" +
            "</if>" +
            "<if test='request.clusterId != null and request.clusterId != &quot;&quot;'>" +
            "AND cluster_id = #{request.clusterId}" +
            "</if>" +
            "</where>" +
            "<if test='request.sortField != null and request.sortField != &quot;&quot;'>" +
            "ORDER BY ${request.sortField}" +
            "</if>" +
            "<if test='request.sortOrder != null and request.sortOrder != &quot;&quot;'>" +
            " ${request.sortOrder}" +
            "</if>" +
            " LIMIT #{offset}, #{limit}" +
            "</script>")
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT),
            @Result(column = "topic_name", property = "topicName", jdbcType = JdbcType.VARCHAR),
            @Result(column = "partitions", property = "partitions", jdbcType = JdbcType.INTEGER),
            @Result(column = "replicas", property = "replicas", jdbcType = JdbcType.INTEGER),
            @Result(column = "broker_spread", property = "brokerSpread", jdbcType = JdbcType.VARCHAR),
            @Result(column = "broker_skewed", property = "brokerSkewed", jdbcType = JdbcType.VARCHAR),
            @Result(column = "leader_skewed", property = "leaderSkewed", jdbcType = JdbcType.VARCHAR),
            @Result(column = "retention_time", property = "retentionTime", jdbcType = JdbcType.BIGINT),
            @Result(column = "icon", property = "icon", jdbcType = JdbcType.VARCHAR),
            @Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "update_time", property = "updateTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "create_by", property = "createBy", jdbcType = JdbcType.VARCHAR),
            @Result(column = "update_by", property = "updateBy", jdbcType = JdbcType.VARCHAR)
    })
    List<TopicInfo> selectTopicPage(@Param("request") TopicQueryRequest request, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 查询Topic总数
     * 
     * @param request 查询条件
     * @return 总数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM ke_topic_info " +
            "<where>" +
            "<if test='request.search != null and request.search != &quot;&quot;'>" +
            "AND topic_name LIKE CONCAT('%', #{request.search}, '%')" +
            "</if>" +
            "<if test='request.brokerSpread != null and request.brokerSpread != &quot;&quot;'>" +
            "AND broker_spread = #{request.brokerSpread}" +
            "</if>" +
            "<if test='request.brokerSkewed != null and request.brokerSkewed != &quot;&quot;'>" +
            "AND broker_skewed = #{request.brokerSkewed}" +
            "</if>" +
            "<if test='request.leaderSkewed != null and request.leaderSkewed != &quot;&quot;'>" +
            "AND leader_skewed = #{request.leaderSkewed}" +
            "</if>" +
            "</where>" +
            "</script>")
    Long countTopic(@Param("request") TopicQueryRequest request);
    
    /**
     * 根据ID查询Topic
     *
     * @param id 主键ID
     * @return Topic信息
     */
    @Select("SELECT id, topic_name, cluster_id, partitions, replicas, broker_spread, broker_skewed, leader_skewed, retention_time, icon, create_time, update_time, create_by, update_by FROM ke_topic_info WHERE id = #{id}")
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT),
            @Result(column = "topic_name", property = "topicName", jdbcType = JdbcType.VARCHAR),
            @Result(column = "cluster_id", property = "clusterId", jdbcType = JdbcType.VARCHAR),
            @Result(column = "partitions", property = "partitions", jdbcType = JdbcType.INTEGER),
            @Result(column = "replicas", property = "replicas", jdbcType = JdbcType.INTEGER),
            @Result(column = "broker_spread", property = "brokerSpread", jdbcType = JdbcType.VARCHAR),
            @Result(column = "broker_skewed", property = "brokerSkewed", jdbcType = JdbcType.VARCHAR),
            @Result(column = "leader_skewed", property = "leaderSkewed", jdbcType = JdbcType.VARCHAR),
            @Result(column = "retention_time", property = "retentionTime", jdbcType = JdbcType.BIGINT),
            @Result(column = "icon", property = "icon", jdbcType = JdbcType.VARCHAR),
            @Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "update_time", property = "updateTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "create_by", property = "createBy", jdbcType = JdbcType.VARCHAR),
            @Result(column = "update_by", property = "updateBy", jdbcType = JdbcType.VARCHAR)
    })
    TopicInfo selectTopicById(Long id);
    
    /**
     * 根据Topic名称查询
     *
     * @param topicName Topic名称
     * @return Topic信息
     */
    @Select("SELECT id, topic_name, cluster_id, partitions, replicas, broker_spread, broker_skewed, leader_skewed, retention_time, icon, create_time, update_time, create_by, update_by FROM ke_topic_info WHERE topic_name = #{topicName}")
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT),
            @Result(column = "topic_name", property = "topicName", jdbcType = JdbcType.VARCHAR),
            @Result(column = "cluster_id", property = "clusterId", jdbcType = JdbcType.VARCHAR),
            @Result(column = "partitions", property = "partitions", jdbcType = JdbcType.INTEGER),
            @Result(column = "replicas", property = "replicas", jdbcType = JdbcType.INTEGER),
            @Result(column = "broker_spread", property = "brokerSpread", jdbcType = JdbcType.VARCHAR),
            @Result(column = "broker_skewed", property = "brokerSkewed", jdbcType = JdbcType.VARCHAR),
            @Result(column = "leader_skewed", property = "leaderSkewed", jdbcType = JdbcType.VARCHAR),
            @Result(column = "retention_time", property = "retentionTime", jdbcType = JdbcType.BIGINT),
            @Result(column = "icon", property = "icon", jdbcType = JdbcType.VARCHAR),
            @Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "update_time", property = "updateTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "create_by", property = "createBy", jdbcType = JdbcType.VARCHAR),
            @Result(column = "update_by", property = "updateBy", jdbcType = JdbcType.VARCHAR)
    })
    TopicInfo selectTopicByName(String topicName);

    /**
     * 根据Topic名称和集群ID查询
     *
     * @param topicName Topic名称
     * @param clusterId 集群ID
     * @return Topic信息
     */
    @Select("<script>" +
            "SELECT id, topic_name, cluster_id, partitions, replicas, broker_spread, broker_skewed, leader_skewed, retention_time, icon, create_time, update_time, create_by, update_by " +
            "FROM ke_topic_info " +
            "WHERE topic_name = #{topicName} " +
            "<if test='clusterId != null and clusterId != \"\"'>" +
            "AND cluster_id = #{clusterId}" +
            "</if>" +
            "</script>")
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT),
            @Result(column = "topic_name", property = "topicName", jdbcType = JdbcType.VARCHAR),
            @Result(column = "cluster_id", property = "clusterId", jdbcType = JdbcType.VARCHAR),
            @Result(column = "partitions", property = "partitions", jdbcType = JdbcType.INTEGER),
            @Result(column = "replicas", property = "replicas", jdbcType = JdbcType.INTEGER),
            @Result(column = "broker_spread", property = "brokerSpread", jdbcType = JdbcType.VARCHAR),
            @Result(column = "broker_skewed", property = "brokerSkewed", jdbcType = JdbcType.VARCHAR),
            @Result(column = "leader_skewed", property = "leaderSkewed", jdbcType = JdbcType.VARCHAR),
            @Result(column = "retention_time", property = "retentionTime", jdbcType = JdbcType.BIGINT),
            @Result(column = "icon", property = "icon", jdbcType = JdbcType.VARCHAR),
            @Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "update_time", property = "updateTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "create_by", property = "createBy", jdbcType = JdbcType.VARCHAR),
            @Result(column = "update_by", property = "updateBy", jdbcType = JdbcType.VARCHAR)
    })
    TopicInfo selectTopicByNameAndCluster(@Param("topicName") String topicName, @Param("clusterId") String clusterId);
    
    /**
     * 插入Topic
     * 
     * @param topicInfo Topic信息
     * @return 影响行数
     */
    @Insert("INSERT INTO ke_topic_info (topic_name, cluster_id, partitions, replicas, broker_spread, broker_skewed, leader_skewed, retention_time, icon, create_by, update_by) VALUES (#{topicName}, #{clusterId}, #{partitions}, #{replicas}, #{brokerSpread}, #{brokerSkewed}, #{leaderSkewed}, #{retentionTime}, #{icon}, #{createBy}, #{updateBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertTopic(TopicInfo topicInfo);
    
    /**
     * 更新Topic
     * 
     * @param topicInfo Topic信息
     * @return 影响行数
     */
    @Update("<script>" +
            "UPDATE ke_topic_info " +
            "<set>" +
            "<if test='topicName != null'>topic_name = #{topicName},</if>" +
            "<if test='partitions != null'>partitions = #{partitions},</if>" +
            "<if test='replicas != null'>replicas = #{replicas},</if>" +
            "<if test='brokerSpread != null'>broker_spread = #{brokerSpread},</if>" +
            "<if test='brokerSkewed != null'>broker_skewed = #{brokerSkewed},</if>" +
            "<if test='leaderSkewed != null'>leader_skewed = #{leaderSkewed},</if>" +
            "<if test='retentionTime != null'>retention_time = #{retentionTime},</if>" +
            "<if test='icon != null'>icon = #{icon},</if>" +
            "<if test='updateBy != null'>update_by = #{updateBy},</if>" +
            "update_time = CURRENT_TIMESTAMP" +
            "</set>" +
            "WHERE id = #{id}" +
            "</script>")
    int updateTopic(TopicInfo topicInfo);
    
    /**
     * 删除Topic
     * 
     * @param id 主键ID
     * @return 影响行数
     */
    @Delete("DELETE FROM ke_topic_info WHERE id = #{id}")
    int deleteTopic(@Param("id") Long id);
    
    /**
     * 批量删除Topic
     *
     * @param ids 主键ID列表
     * @return 影响行数
     */
    @Delete("<script>" +
            "DELETE FROM ke_topic_info WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchDeleteTopic(@Param("ids") List<Long> ids);

    /**
     * 获取所有主题名称列表
     *
     * @return 主题名称列表
     */
    @Select("SELECT topic_name FROM ke_topic_info ORDER BY topic_name")
    List<String> selectAllTopicNames();

    /**
     * 根据集群ID统计主题数量
     *
     * @param clusterId 集群ID
     * @return 主题数量
     */
    @Select("SELECT COUNT(*) FROM ke_topic_info WHERE cluster_id = #{clusterId}")
    Long countTopicsByClusterId(@Param("clusterId") String clusterId);

    /**
     * 根据集群ID统计分区总数
     *
     * @param clusterId 集群ID
     * @return 分区总数
     */
    @Select("SELECT COALESCE(SUM(partitions), 0) FROM ke_topic_info WHERE cluster_id = #{clusterId}")
    Integer sumPartitionsByClusterId(@Param("clusterId") String clusterId);
}