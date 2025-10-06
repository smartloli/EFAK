package org.kafka.eagle.web.mapper;

import org.apache.ibatis.annotations.*;
import org.kafka.eagle.dto.topic.TopicInfo;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Description: Topic information mapper for database operations
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/14 20:02:04
 * @version 5.0.0
 */
@Mapper
public interface TopicInfoMapper {
    
    /**
     * Insert or update topic information
     * 
     * @param topicInfo topic information
     * @return affected rows
     */
    @Insert("INSERT INTO ke_topic_info (topic_name, partitions, replicas, broker_spread, broker_skewed, " +
            "leader_skewed, retention_time, icon, create_by, update_by) " +
            "VALUES (#{topicName}, #{partitions}, #{replicas}, #{brokerSpread}, #{brokerSkewed}, " +
            "#{leaderSkewed}, #{retentionTime}, #{icon}, #{createBy}, #{updateBy}) " +
            "ON DUPLICATE KEY UPDATE " +
            "partitions = VALUES(partitions), replicas = VALUES(replicas), " +
            "broker_spread = VALUES(broker_spread), broker_skewed = VALUES(broker_skewed), " +
            "leader_skewed = VALUES(leader_skewed), retention_time = VALUES(retention_time), " +
            "icon = COALESCE(icon, VALUES(icon)), " +
            "update_by = VALUES(update_by), update_time = CURRENT_TIMESTAMP")
    int insertOrUpdate(TopicInfo topicInfo);
    
    /**
     * Insert or update topic information without overriding icon
     *
     * @param topicInfo topic information
     * @return affected rows
     */
    @Insert("INSERT INTO ke_topic_info (topic_name, partitions, replicas, broker_spread, broker_skewed, " +
            "leader_skewed, retention_time, create_by, update_by) " +
            "VALUES (#{topicName}, #{partitions}, #{replicas}, #{brokerSpread}, #{brokerSkewed}, " +
            "#{leaderSkewed}, #{retentionTime}, #{createBy}, #{updateBy}) " +
            "ON DUPLICATE KEY UPDATE " +
            "partitions = VALUES(partitions), replicas = VALUES(replicas), " +
            "broker_spread = VALUES(broker_spread), broker_skewed = VALUES(broker_skewed), " +
            "leader_skewed = VALUES(leader_skewed), retention_time = VALUES(retention_time), " +
            "update_by = VALUES(update_by), update_time = CURRENT_TIMESTAMP")
    int insertOrUpdateWithoutIcon(TopicInfo topicInfo);

    /**
     * Insert or update topic information by topic name and cluster id
     *
     * @param topicInfo topic information
     * @return affected rows
     */
    @Insert("INSERT INTO ke_topic_info (topic_name, cluster_id, partitions, replicas, broker_spread, broker_skewed, " +
            "leader_skewed, retention_time, create_by, update_by) " +
            "VALUES (#{topicName}, #{clusterId}, #{partitions}, #{replicas}, #{brokerSpread}, #{brokerSkewed}, " +
            "#{leaderSkewed}, #{retentionTime}, #{createBy}, #{updateBy}) " +
            "ON DUPLICATE KEY UPDATE " +
            "partitions = VALUES(partitions), replicas = VALUES(replicas), " +
            "broker_spread = VALUES(broker_spread), broker_skewed = VALUES(broker_skewed), " +
            "leader_skewed = VALUES(leader_skewed), retention_time = VALUES(retention_time), " +
            "update_by = VALUES(update_by), update_time = CURRENT_TIMESTAMP")
    int insertOrUpdateByTopicAndCluster(TopicInfo topicInfo);
    
    /**
     * Select topic information by topic name
     *
     * @param topicName topic name
     * @return topic information
     */
    @Select("SELECT * FROM ke_topic_info WHERE topic_name = #{topicName}")
    TopicInfo selectByTopicName(String topicName);

    /**
     * Select topic information by topic name and cluster id
     *
     * @param topicName topic name
     * @param clusterId cluster id
     * @return topic information
     */
    @Select("SELECT * FROM ke_topic_info WHERE topic_name = #{topicName} AND cluster_id = #{clusterId}")
    TopicInfo selectByTopicNameAndClusterId(@Param("topicName") String topicName, @Param("clusterId") String clusterId);

    /**
     * Select all topic information by cluster id
     *
     * @param clusterId cluster id
     * @return list of topic information
     */
    @Select("SELECT * FROM ke_topic_info WHERE cluster_id = #{clusterId} ORDER BY update_time DESC")
    List<TopicInfo> selectByClusterId(String clusterId);
    
    /**
     * Select all topic information
     * 
     * @return list of topic information
     */
    @Select("SELECT * FROM ke_topic_info ORDER BY update_time DESC")
    List<TopicInfo> selectAll();
    
    /**
     * Delete topic information by topic name
     * 
     * @param topicName topic name
     * @return affected rows
     */
    @Delete("DELETE FROM ke_topic_info WHERE topic_name = #{topicName}")
    int deleteByTopicName(String topicName);
    
    /**
     * Update topic information
     * 
     * @param topicInfo topic information
     * @return affected rows
     */
    @Update("UPDATE ke_topic_info SET partitions = #{partitions}, replicas = #{replicas}, " +
            "broker_spread = #{brokerSpread}, broker_skewed = #{brokerSkewed}, " +
            "leader_skewed = #{leaderSkewed}, retention_time = #{retentionTime}, " +
            "icon = #{icon}, " +
            "update_by = #{updateBy}, update_time = CURRENT_TIMESTAMP " +
            "WHERE topic_name = #{topicName}")
    int updateByTopicName(TopicInfo topicInfo);
    
    /**
     * Count total topics
     * 
     * @return total count
     */
    @Select("SELECT COUNT(*) FROM ke_topic_info")
    int countTotal();

    /**
     * 根据集群ID查询Topic信息
     */
    @Select("SELECT " +
            "topic_name as topicName, " +
            "partitions, " +
            "replicas, " +
            "broker_spread as brokerSpread, " +
            "broker_skewed as brokerSkewed, " +
            "leader_skewed as leaderSkewed, " +
            "retention_time as retentionTime, " +
            "create_time as createTime, " +
            "update_time as updateTime " +
            "FROM ke_topic_info " +
            "WHERE cluster_id = #{clusterId} " +
            "ORDER BY update_time DESC LIMIT 100")
    List<Map<String, Object>> findByClusterId(@Param("clusterId") String clusterId);

    /**
     * 根据集群ID和Topic名称查询Topic信息
     */
    @Select("SELECT " +
            "topic_name as topicName, " +
            "partitions, " +
            "replicas, " +
            "broker_spread as brokerSpread, " +
            "broker_skewed as brokerSkewed, " +
            "leader_skewed as leaderSkewed, " +
            "retention_time as retentionTime, " +
            "create_time as createTime, " +
            "update_time as updateTime " +
            "FROM ke_topic_info " +
            "WHERE cluster_id = #{clusterId} AND topic_name = #{topic} " +
            "ORDER BY update_time DESC LIMIT 100")
    List<Map<String, Object>> findByClusterIdAndTopic(@Param("clusterId") String clusterId, @Param("topic") String topic);
}