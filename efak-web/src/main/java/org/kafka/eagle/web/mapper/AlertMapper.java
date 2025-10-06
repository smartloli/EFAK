package org.kafka.eagle.web.mapper;

import org.apache.ibatis.annotations.*;
import org.kafka.eagle.dto.alert.AlertChannel;
import org.kafka.eagle.dto.alert.AlertInfo;
import org.kafka.eagle.dto.alert.AlertTypeConfig;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Alert 数据访问层接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/30 01:40:34
 * @version 5.0.0
 */
@Mapper
public interface AlertMapper {

        /**
         * 查询告警列表
         */
        @Select("<script>" +
                        "SELECT " +
                        "  a.id, " +
                        "  a.alert_task_id as alertTaskId, " +
                        "  a.cluster_id as clusterId, " +
                        "  a.title, " +
                        "  a.description, " +
                        "  a.channel_id as channelId, " +
                        "  a.duration, " +
                        "  a.status, " +
                        "  a.created_at as createdAt, " +
                        "  a.updated_at as updatedAt, " +
                        "  c.type as type, " +
                        "  c.name as channelName, " +
                        "  config.type as alertType, " +
                        "  config.threshold, " +
                        "  config.unit, " +
                        "  config.target as object " +
                        "FROM ke_alerts a " +
                        "LEFT JOIN ke_alert_channels c ON a.channel_id = c.id " +
                        "LEFT JOIN ke_alert_type_configs config ON a.alert_task_id = config.id " +
                        "WHERE 1=1 " +
                        "<if test=\"clusterId != null and clusterId != ''\">" +
                        "  AND a.cluster_id = #{clusterId} " +
                        "</if>" +
                        "<if test=\"search != null and search != ''\">" +
                        "  AND (a.title LIKE CONCAT('%', #{search}, '%') " +
                        "       OR a.description LIKE CONCAT('%', #{search}, '%') " +
                        "       OR config.target LIKE CONCAT('%', #{search}, '%')) " +
                        "</if>" +
                        "<if test=\"status != null\">" +
                        "  AND a.status = #{status} " +
                        "</if>" +
                        "<if test=\"type != null and type != ''\">" +
                        "  AND config.type = #{type} " +
                        "</if>" +
                        "<if test=\"channel != null and channel != ''\">" +
                        "  AND c.type = #{channel} " +
                        "</if>" +
                        "<if test=\"timeRange != null and timeRange != ''\">" +
                        "  AND a.created_at >= DATE_SUB(NOW(), INTERVAL " +
                        "    CASE #{timeRange} " +
                        "      WHEN '1h' THEN 1 " +
                        "      WHEN '3h' THEN 3 " +
                        "      WHEN '6h' THEN 6 " +
                        "      WHEN '12h' THEN 12 " +
                        "      WHEN '24h' THEN 24 " +
                        "      ELSE 24 " +
                        "    END HOUR) " +
                        "</if>" +
                        "ORDER BY " +
                        "<choose>" +
                        "  <when test='sortField == \"createdAt\"'>a.created_at</when>" +
                        "  <when test='sortField == \"updatedAt\"'>a.updated_at</when>" +
                        "  <when test='sortField == \"status\"'>a.status</when>" +
                        "  <when test='sortField == \"type\"'>config.type</when>" +
                        "  <otherwise>a.created_at</otherwise>" +
                        "</choose> " +
                        "${sortOrder} " +
                        "LIMIT #{offset}, #{pageSize}" +
                        "</script>")
        List<AlertInfo> queryAlerts(@Param("clusterId") String clusterId,
                        @Param("search") String search,
                        @Param("status") Integer status,
                        @Param("type") String type,
                        @Param("channel") String channel,
                        @Param("timeRange") String timeRange,
                        @Param("sortField") String sortField,
                        @Param("sortOrder") String sortOrder,
                        @Param("offset") Integer offset,
                        @Param("pageSize") Integer pageSize);

        /**
         * 查询告警通知列表（用于通知中心）
         */
        @Select("<script>" +
                        "SELECT " +
                        "  a.id, " +
                        "  a.alert_task_id as alertTaskId, " +
                        "  a.cluster_id as clusterId, " +
                        "  a.title, " +
                        "  a.description, " +
                        "  a.channel_id as channelId, " +
                        "  a.duration, " +
                        "  a.status, " +
                        "  a.created_at as createdAt, " +
                        "  a.updated_at as updatedAt, " +
                        "  c.type as type, " +
                        "  c.name as channelName, " +
                        "  config.type as alertType, " +
                        "  config.threshold, " +
                        "  config.unit, " +
                        "  config.target as object " +
                        "FROM ke_alerts a " +
                        "LEFT JOIN ke_alert_channels c ON a.channel_id = c.id " +
                        "LEFT JOIN ke_alert_type_configs config ON a.alert_task_id = config.id " +
                        "WHERE 1=1 " +
                        "<if test=\"clusterId != null and clusterId != ''\">" +
                        "  AND a.cluster_id = #{clusterId} " +
                        "</if>" +
                        "ORDER BY " +
                        "<choose>" +
                        "  <when test='sortField == \"createdAt\"'>a.created_at</when>" +
                        "  <when test='sortField == \"updatedAt\"'>a.updated_at</when>" +
                        "  <when test='sortField == \"status\"'>a.status</when>" +
                        "  <when test='sortField == \"type\"'>config.type</when>" +
                        "  <otherwise>a.updated_at</otherwise>" +
                        "</choose> " +
                        "${sortOrder} " +
                        "LIMIT #{offset}, #{pageSize}" +
                        "</script>")
        List<AlertInfo> queryAlertsForNotifications(@Param("clusterId") String clusterId,
                        @Param("sortField") String sortField,
                        @Param("sortOrder") String sortOrder,
                        @Param("offset") Integer offset,
                        @Param("pageSize") Integer pageSize);

        /**
         * 查询告警通知总数（用于通知中心）
         */
        @Select("<script>" +
                        "SELECT COUNT(*) FROM ke_alerts a " +
                        "WHERE 1=1 " +
                        "<if test=\"clusterId != null and clusterId != ''\">" +
                        "  AND a.cluster_id = #{clusterId} " +
                        "</if>" +
                        "</script>")
        Long countAlertsForNotifications(@Param("clusterId") String clusterId);

        /**
         * 查询告警总数
         */
        @Select("<script>" +
                        "SELECT COUNT(*) FROM ke_alerts a " +
                        "LEFT JOIN ke_alert_channels c ON a.channel_id = c.id " +
                        "LEFT JOIN ke_alert_type_configs config ON a.alert_task_id = config.id " +
                        "WHERE 1=1 " +
                        "<if test=\"clusterId != null and clusterId != ''\">" +
                        "  AND a.cluster_id = #{clusterId} " +
                        "</if>" +
                        "<if test=\"search != null and search != ''\">" +
                        "  AND (a.title LIKE CONCAT('%', #{search}, '%') " +
                        "       OR a.description LIKE CONCAT('%', #{search}, '%') " +
                        "       OR config.target LIKE CONCAT('%', #{search}, '%')) " +
                        "</if>" +
                        "<if test=\"status != null\">" +
                        "  AND a.status = #{status} " +
                        "</if>" +
                        "<if test=\"type != null and type != ''\">" +
                        "  AND config.type = #{type} " +
                        "</if>" +
                        "<if test=\"channel != null and channel != ''\">" +
                        "  AND c.type = #{channel} " +
                        "</if>" +
                        "<if test=\"timeRange != null and timeRange != ''\">" +
                        "  AND a.created_at >= DATE_SUB(NOW(), INTERVAL " +
                        "    CASE #{timeRange} " +
                        "      WHEN '1h' THEN 1 " +
                        "      WHEN '3h' THEN 3 " +
                        "      WHEN '6h' THEN 6 " +
                        "      WHEN '12h' THEN 12 " +
                        "      WHEN '24h' THEN 24 " +
                        "      ELSE 24 " +
                        "    END HOUR) " +
                        "</if>" +
                        "</script>")
        Long countAlerts(@Param("clusterId") String clusterId,
                        @Param("search") String search,
                        @Param("status") Integer status,
                        @Param("type") String type,
                        @Param("channel") String channel,
                        @Param("timeRange") String timeRange);

        /**
         * 查询告警统计信息
         */
        @Select("<script>" +
                        "SELECT " +
                        "  SUM(CASE WHEN a.status = 0 THEN 1 ELSE 0 END) as unprocessed, " +
                        "  SUM(CASE WHEN a.status = 1 THEN 1 ELSE 0 END) as processed, " +
                        "  SUM(CASE WHEN a.status = 2 THEN 1 ELSE 0 END) as ignored, " +
                        "  SUM(CASE WHEN a.status = 3 THEN 1 ELSE 0 END) as resolved, " +
                        "  COUNT(*) as total " +
                        "FROM ke_alerts a " +
                        "WHERE a.created_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR) " +
                        "<if test=\"clusterId != null and clusterId != ''\">" +
                        "  AND a.cluster_id = #{clusterId} " +
                        "</if>" +
                        "</script>")
        Map<String, Object> getAlertStats(@Param("clusterId") String clusterId);

        /**
         * 根据ID查询告警详情
         */
        @Select("SELECT " +
                        "  a.id, " +
                        "  a.alert_task_id as alertTaskId, " +
                        "  a.cluster_id as clusterId, " +
                        "  a.title, " +
                        "  a.description, " +
                        "  a.channel_id as channelId, " +
                        "  a.duration, " +
                        "  a.status, " +
                        "  a.created_at as createdAt, " +
                        "  a.updated_at as updatedAt, " +
                        "  c.type as type, " +
                        "  c.name as channelName, " +
                        "  config.type as alertType, " +
                        "  config.threshold, " +
                        "  config.unit, " +
                        "  config.target as object " +
                        "FROM ke_alerts a " +
                        "LEFT JOIN ke_alert_channels c ON a.channel_id = c.id " +
                        "LEFT JOIN ke_alert_type_configs config ON a.alert_task_id = config.id " +
                        "WHERE a.id = #{id}")
        AlertInfo getAlertById(@Param("id") Long id);

        /**
         * 更新告警状态
         */
        @Update("UPDATE ke_alerts SET " +
                        "status = #{status}, " +
                        "updated_at = NOW() " +
                        "WHERE alert_task_id = #{alertTaskId} AND cluster_id = #{clusterId}")
        int updateAlertStatus(@Param("alertTaskId") Long alertTaskId,
                        @Param("clusterId") String clusterId,
                        @Param("status") Integer status);

        /**
         * 根据告警ID更新告警状态
         */
        @Update("UPDATE ke_alerts SET " +
                        "status = #{status}, " +
                        "updated_at = NOW() " +
                        "WHERE id = #{id} AND cluster_id = #{clusterId}")
        int updateAlertStatusById(@Param("id") Long id,
                        @Param("clusterId") String clusterId,
                        @Param("status") Integer status);

        /**
         * 查询告警渠道列表
         */
        @Select("<script>" +
                        "SELECT " +
                        "  id, " +
                        "  cluster_id as clusterId, " +
                        "  name, " +
                        "  type, " +
                        "  api_url as apiUrl, " +
                        "  enabled, " +
                        "  created_at as createdAt, " +
                        "  updated_at as updatedAt, " +
                        "  created_by as createdBy " +
                        "FROM ke_alert_channels " +
                        "<where>" +
                        "<if test=\"clusterId != null and clusterId != ''\">" +
                        "  AND cluster_id = #{clusterId} " +
                        "</if>" +
                        "</where>" +
                        "ORDER BY created_at DESC" +
                        "</script>")
        List<AlertChannel> getAlertChannels(@Param("clusterId") String clusterId);

        /**
         * 根据ID查询告警渠道
         */
        @Select("SELECT " +
                        "  id, " +
                        "  cluster_id as clusterId, " +
                        "  name, " +
                        "  type, " +
                        "  api_url as apiUrl, " +
                        "  enabled, " +
                        "  created_at as createdAt, " +
                        "  updated_at as updatedAt, " +
                        "  created_by as createdBy " +
                        "FROM ke_alert_channels WHERE id = #{id}")
        AlertChannel getAlertChannelById(@Param("id") Long id);

        /**
         * 创建告警渠道
         */
        @Insert("INSERT INTO ke_alert_channels (cluster_id, name, type, api_url, enabled, created_by) " +
                        "VALUES (#{clusterId}, #{name}, #{type}, #{apiUrl}, #{enabled}, #{createdBy})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int createAlertChannel(AlertChannel channel);

        /**
         * 更新告警渠道
         */
        @Update("UPDATE ke_alert_channels SET " +
                        "cluster_id = #{clusterId}, name = #{name}, type = #{type}, api_url = #{apiUrl}, " +
                        "enabled = #{enabled}, updated_at = NOW() " +
                        "WHERE id = #{id}")
        int updateAlertChannel(AlertChannel channel);

        /**
         * 删除告警渠道
         */
        @Delete("DELETE FROM ke_alert_channels WHERE id = #{id}")
        int deleteAlertChannel(@Param("id") Long id);

        /**
         * 查询告警类型配置列表
         */
        @Select("<script>" +
                        "SELECT " +
                        "  id, " +
                        "  cluster_id as clusterId, " +
                        "  type, " +
                        "  name, " +
                        "  description, " +
                        "  enabled, " +
                        "  threshold, " +
                        "  unit, " +
                        "  target, " +
                        "  channel_id as channelIdsJson, " +
                        "  created_by as createdBy, " +
                        "  created_at as createdAt, " +
                        "  updated_at as updatedAt " +
                        "FROM ke_alert_type_configs " +
                        "<where>" +
                        "<if test=\"clusterId != null and clusterId != ''\">" +
                        "  AND cluster_id = #{clusterId} " +
                        "</if>" +
                        "</where>" +
                        "ORDER BY created_at DESC" +
                        "</script>")
        List<AlertTypeConfig> getAlertTypeConfigs(@Param("clusterId") String clusterId);

        /**
         * 分页查询告警类型配置列表
         */
        @Select("<script>" +
                        "SELECT " +
                        "  id, " +
                        "  cluster_id as clusterId, " +
                        "  type, " +
                        "  name, " +
                        "  description, " +
                        "  enabled, " +
                        "  threshold, " +
                        "  unit, " +
                        "  target, " +
                        "  channel_id as channelIdsJson, " +
                        "  created_by as createdBy, " +
                        "  created_at as createdAt, " +
                        "  updated_at as updatedAt " +
                        "FROM ke_alert_type_configs " +
                        "<where>" +
                        "<if test=\"clusterId != null and clusterId != ''\">" +
                        "  AND cluster_id = #{clusterId} " +
                        "</if>" +
                        "</where>" +
                        "ORDER BY created_at DESC " +
                        "LIMIT #{offset}, #{pageSize}" +
                        "</script>")
        List<AlertTypeConfig> getAlertTypeConfigsPage(@Param("clusterId") String clusterId,
                        @Param("offset") Integer offset,
                        @Param("pageSize") Integer pageSize);

        /**
         * 查询告警类型配置总数
         */
        @Select("<script>" +
                        "SELECT COUNT(*) FROM ke_alert_type_configs " +
                        "<where>" +
                        "<if test=\"clusterId != null and clusterId != ''\">" +
                        "  AND cluster_id = #{clusterId} " +
                        "</if>" +
                        "</where>" +
                        "</script>")
        Long countAlertTypeConfigs(@Param("clusterId") String clusterId);

        /**
         * 根据ID查询告警类型配置
         */
        @Select("SELECT " +
                        "  id, " +
                        "  cluster_id as clusterId, " +
                        "  type, " +
                        "  name, " +
                        "  description, " +
                        "  enabled, " +
                        "  threshold, " +
                        "  unit, " +
                        "  target, " +
                        "  channel_id as channelIdsJson, " +
                        "  created_by as createdBy, " +
                        "  created_at as createdAt, " +
                        "  updated_at as updatedAt " +
                        "FROM ke_alert_type_configs WHERE id = #{id}")
        AlertTypeConfig getAlertTypeConfigById(@Param("id") Long id);

        /**
         * 创建告警类型配置
         */
        @Insert("INSERT INTO ke_alert_type_configs (cluster_id, type, name, description, enabled, threshold, unit, target, channel_id, created_by) "
                        +
                        "VALUES (#{clusterId}, #{type}, #{name}, #{description}, #{enabled}, #{threshold}, #{unit}, #{target}, #{channelIdsJson}, #{createdBy})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int createAlertTypeConfig(AlertTypeConfig config);

        /**
         * 更新告警类型配置
         */
        @Update("UPDATE ke_alert_type_configs SET " +
                        "cluster_id = #{clusterId}, enabled = #{enabled}, " +
                        "threshold = #{threshold}, unit = #{unit}, " +
                        "channel_id = #{channelIdsJson}, updated_at = NOW() " +
                        "WHERE id = #{id}")
        int updateAlertTypeConfig(AlertTypeConfig config);

        /**
         * 删除告警类型配置
         */
        @Delete("DELETE FROM ke_alert_type_configs WHERE id = #{id}")
        int deleteAlertTypeConfig(@Param("id") Long id);

        /**
         * 检查监控目标是否存在
         */
        @Select("<script>" +
                        "SELECT COUNT(*) FROM (" +
                        "  <choose>" +
                        "    <when test=\"target == 'all'\">SELECT 1</when>" +
                        "    <when test=\"topicName != null and topicName != '' and groupId != null and groupId != ''\">" +
                        "      SELECT 1 FROM ke_consumer_group_topic WHERE group_id = #{groupId} AND topic_name = #{topicName}" +
                        "      <if test=\"clusterId != null and clusterId != ''\">" +
                        "        AND cluster_id = #{clusterId}" +
                        "      </if>" +
                        "    </when>" +
                        "    <when test=\"topicName != null and topicName != '' and (groupId == null or groupId == '')\">" +
                        "      SELECT 1 FROM ke_topic_info WHERE topic_name = #{topicName}" +
                        "      <if test=\"clusterId != null and clusterId != ''\">" +
                        "        AND cluster_id = #{clusterId}" +
                        "      </if>" +
                        "    </when>" +
                        "    <when test=\"brokerId != null and brokerId != ''\">" +
                        "      SELECT 1 FROM ke_broker_info WHERE host_ip = #{brokerId}" +
                        "      <if test=\"clusterId != null and clusterId != ''\">" +
                        "        AND cluster_id = #{clusterId}" +
                        "      </if>" +
                        "    </when>" +
                        "    <otherwise>SELECT 0</otherwise>" +
                        "  </choose>" +
                        ") t" +
                        "</script>")
        int checkTargetExists(@Param("clusterId") String clusterId,
                        @Param("target") String target,
                        @Param("topicName") String topicName,
                        @Param("groupId") String groupId,
                        @Param("brokerId") String brokerId); // brokerId现在存储的是IP地址

        /**
         * 根据类型和目标查询告警类型配置
         */
        @Select("<script>" +
                        "SELECT " +
                        "  id, " +
                        "  cluster_id as clusterId, " +
                        "  type, " +
                        "  name, " +
                        "  description, " +
                        "  enabled, " +
                        "  threshold, " +
                        "  unit, " +
                        "  target, " +
                        "  channel_id as channelIdsJson, " +
                        "  created_by as createdBy, " +
                        "  created_at as createdAt, " +
                        "  updated_at as updatedAt " +
                        "FROM ke_alert_type_configs " +
                        "WHERE type = #{type} AND target = #{target} " +
                        "<if test=\"clusterId != null and clusterId != ''\">" +
                        "  AND cluster_id = #{clusterId} " +
                        "</if>" +
                        "</script>")
        List<AlertTypeConfig> getAlertTypeConfigsByTypeAndTarget(@Param("clusterId") String clusterId,
                        @Param("type") String type,
                        @Param("target") String target);

        /**
         * 创建告警记录
         */
        @Insert("INSERT INTO ke_alerts (alert_task_id, cluster_id, title, description, channel_id, duration, status) " +
                        "VALUES (#{alertTaskId}, #{clusterId}, #{title}, #{description}, #{channelId}, #{duration}, #{status})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int createAlert(AlertInfo alert);

        /**
         * 根据告警任务ID查询最新告警记录
         */
        @Select("SELECT " +
                        "  a.id, " +
                        "  a.alert_task_id as alertTaskId, " +
                        "  a.cluster_id as clusterId, " +
                        "  a.title, " +
                        "  a.description, " +
                        "  a.channel_id as channelId, " +
                        "  a.duration, " +
                        "  a.status, " +
                        "  a.created_at as createdAt, " +
                        "  a.updated_at as updatedAt " +
                        "FROM ke_alerts a " +
                        "WHERE a.alert_task_id = #{alertTaskId} AND a.cluster_id = #{clusterId} " +
                        "ORDER BY a.created_at DESC LIMIT 1")
        AlertInfo getLatestAlertByTaskId(@Param("alertTaskId") Long alertTaskId,
                        @Param("clusterId") String clusterId);

        /**
         * 根据集群ID查询告警信息
         */
        @Select("SELECT " +
                        "a.id, " +
                        "a.alert_task_id as alertTaskId, " +
                        "a.cluster_id as clusterId, " +
                        "a.title, " +
                        "a.description, " +
                        "a.channel_id as channelId, " +
                        "a.duration, " +
                        "a.status, " +
                        "a.created_at as createdAt, " +
                        "a.updated_at as updatedAt " +
                        "FROM ke_alerts a " +
                        "WHERE a.cluster_id = #{clusterId} " +
                        "ORDER BY a.created_at DESC LIMIT 100")
        List<Map<String, Object>> findByClusterId(@Param("clusterId") String clusterId);
}