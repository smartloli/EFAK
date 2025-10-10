package org.kafka.eagle.web.mapper;

import org.apache.ibatis.annotations.*;
import org.kafka.eagle.dto.broker.BrokerInfo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Broker数据访问层接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/29 22:03:43
 * @version 5.0.0
 */
@Mapper
public interface BrokerMapper {

        /**
         * 分页查询Broker列表
         */
        @Select("<script>" +
                        "SELECT " +
                        "  id, " +
                        "  cluster_id as clusterId, " +
                        "  broker_id as brokerId, " +
                        "  host_ip as hostIp, " +
                        "  port, " +
                        "  port as kafkaPort, " +
                        "  jmx_port as jmxPort, " +
                        "  status, " +
                        "  cpu_usage as cpuUsage, " +
                        "  memory_usage as memoryUsage, " +
                        "  startup_time as startupTime, " +
                        "  version, " +
                        "  created_by as createdBy, " +
                        "  created_at as createdAt, " +
                        "  updated_at as updatedAt " +
                        "FROM ke_broker_info " +
                        "WHERE 1=1 " +
                        "<if test=\"search != null and search != ''\">" +
                        "  AND (host_ip LIKE CONCAT('%', #{search}, '%') " +
                        "       OR CAST(broker_id AS CHAR) LIKE CONCAT('%', #{search}, '%')) " +
                        "</if>" +
                        "<if test=\"status != null and status != ''\">" +
                        "  AND status = #{status} " +
                        "</if>" +
                        "<if test=\"clusterId != null and clusterId != ''\">" +
                        "  AND cluster_id = #{clusterId} " +
                        "</if>" +
                        "ORDER BY " +
                        "<choose>" +
                        "  <when test='sortField == \"brokerId\"'>broker_id</when>" +
                        "  <when test='sortField == \"hostIp\"'>host_ip</when>" +
                        "  <when test='sortField == \"status\"'>status</when>" +
                        "  <when test='sortField == \"cpuUsage\"'>cpu_usage</when>" +
                        "  <when test='sortField == \"memoryUsage\"'>memory_usage</when>" +
                        "  <when test='sortField == \"startupTime\"'>startup_time</when>" +
                        "  <when test='sortField == \"createdAt\"'>created_at</when>" +
                        "  <otherwise>created_at</otherwise>" +
                        "</choose> " +
                        "<choose>" +
                        "  <when test='sortOrder == \"ASC\" or sortOrder == \"asc\"'>ASC</when>" +
                        "  <otherwise>DESC</otherwise>" +
                        "</choose> " +
                        "LIMIT #{offset}, #{pageSize}" +
                        "</script>")
        List<BrokerInfo> queryBrokers(@Param("search") String search,
                        @Param("status") String status,
                        @Param("clusterId") String clusterId,
                        @Param("sortField") String sortField,
                        @Param("sortOrder") String sortOrder,
                        @Param("offset") Integer offset,
                        @Param("pageSize") Integer pageSize);

        /**
         * 查询Broker总数
         */
        @Select("<script>" +
                        "SELECT COUNT(*) FROM ke_broker_info " +
                        "WHERE 1=1 " +
                        "<if test=\"search != null and search != ''\">" +
                        "  AND (host_ip LIKE CONCAT('%', #{search}, '%') " +
                        "       OR CAST(broker_id AS CHAR) LIKE CONCAT('%', #{search}, '%')) " +
                        "</if>" +
                        "<if test=\"status != null and status != ''\">" +
                        "  AND status = #{status} " +
                        "</if>" +
                        "<if test=\"clusterId != null and clusterId != ''\">" +
                        "  AND cluster_id = #{clusterId} " +
                        "</if>" +
                        "</script>")
        Long countBrokers(@Param("search") String search,
                        @Param("status") String status,
                        @Param("clusterId") String clusterId);

        // 统计方法已在文件后部与按集群统计方法合并定义

        /**
         * 根据ID查询Broker详情
         */
        @Select("SELECT " +
                        "  id, " +
                        "  cluster_id as clusterId, " +
                        "  broker_id as brokerId, " +
                        "  host_ip as hostIp, " +
                        "  port, " +
                        "  port as kafkaPort, " +
                        "  jmx_port as jmxPort, " +
                        "  status, " +
                        "  cpu_usage as cpuUsage, " +
                        "  memory_usage as memoryUsage, " +
                        "  startup_time as startupTime, " +
                        "  version, " +
                        "  created_by as createdBy, " +
                        "  created_at as createdAt, " +
                        "  updated_at as updatedAt " +
                        "FROM ke_broker_info WHERE id = #{id}")
        BrokerInfo getBrokerById(@Param("id") Long id);

        /**
         * 根据Broker ID查询Broker信息
         */
        @Select("SELECT " +
                        "  id, " +
                        "  cluster_id as clusterId, " +
                        "  broker_id as brokerId, " +
                        "  host_ip as hostIp, " +
                        "  port, " +
                        "  port as kafkaPort, " +
                        "  jmx_port as jmxPort, " +
                        "  status, " +
                        "  cpu_usage as cpuUsage, " +
                        "  memory_usage as memoryUsage, " +
                        "  startup_time as startupTime, " +
                        "  version, " +
                        "  created_by as createdBy, " +
                        "  created_at as createdAt, " +
                        "  updated_at as updatedAt " +
                        "FROM ke_broker_info WHERE broker_id = #{brokerId}")
        BrokerInfo getBrokerByBrokerId(@Param("brokerId") Integer brokerId);

        /**
         * 根据集群ID和Broker ID查询Broker信息（确保唯一性）
         */
        @Select("SELECT " +
                        "  id, " +
                        "  cluster_id as clusterId, " +
                        "  broker_id as brokerId, " +
                        "  host_ip as hostIp, " +
                        "  port, " +
                        "  port as kafkaPort, " +
                        "  jmx_port as jmxPort, " +
                        "  status, " +
                        "  cpu_usage as cpuUsage, " +
                        "  memory_usage as memoryUsage, " +
                        "  startup_time as startupTime, " +
                        "  version, " +
                        "  created_by as createdBy, " +
                        "  created_at as createdAt, " +
                        "  updated_at as updatedAt " +
                        "FROM ke_broker_info WHERE cluster_id = #{clusterId} AND broker_id = #{brokerId}")
        BrokerInfo getBrokerByClusterIdAndBrokerId(@Param("clusterId") String clusterId, @Param("brokerId") Integer brokerId);

        /**
         * 根据IP查询Broker信息
         */
        @Select("SELECT " +
                "  id, " +
                "  cluster_id as clusterId, " +
                "  broker_id as brokerId, " +
                "  host_ip as hostIp, " +
                "  port, " +
                "  port as kafkaPort, " +
                "  jmx_port as jmxPort, " +
                "  status, " +
                "  cpu_usage as cpuUsage, " +
                "  memory_usage as memoryUsage, " +
                "  startup_time as startupTime, " +
                "  version, " +
                "  created_by as createdBy, " +
                "  created_at as createdAt, " +
                "  updated_at as updatedAt " +
                "FROM ke_broker_info WHERE host_ip = #{hostIp}")
        BrokerInfo getBrokerByHostIp(@Param("hostIp") String hostIp);

        /**
         * 创建Broker
         */
        @Insert("INSERT INTO ke_broker_info (" +
                        "  cluster_id, " +
                        "  broker_id, host_ip, port, jmx_port, status, " +
                        "  cpu_usage, memory_usage, startup_time, version, " +
                        "  created_by" +
                        ") VALUES (" +
                        "  #{clusterId}, " +
                        "  #{brokerId}, #{hostIp}, #{port}, #{jmxPort}, #{status}, " +
                        "  #{cpuUsage}, #{memoryUsage}, #{startupTime}, #{version}, " +
                        "  #{createdBy}" +
                        ")")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int createBroker(BrokerInfo broker);

        /**
         * 更新Broker
         */
        @Update("UPDATE ke_broker_info SET " +
                        "  cluster_id = #{clusterId}, " +
                        "  broker_id = #{brokerId}, " +
                        "  host_ip = #{hostIp}, " +
                        "  port = #{port}, " +
                        "  jmx_port = #{jmxPort}, " +
                        "  status = #{status}, " +
                        "  cpu_usage = #{cpuUsage}, " +
                        "  memory_usage = #{memoryUsage}, " +
                        "  startup_time = #{startupTime}, " +
                        "  version = #{version}, " +
                        "  updated_at = NOW() " +
                        "WHERE id = #{id}")
        int updateBroker(BrokerInfo broker);

        /**
         * 删除Broker
         */
        @Delete("DELETE FROM ke_broker_info WHERE id = #{id}")
        int deleteBroker(@Param("id") Long id);

        /**
         * 根据集群ID删除所有Broker节点
         */
        @Delete("DELETE FROM ke_broker_info WHERE cluster_id = #{clusterId}")
        int deleteBrokersByClusterId(@Param("clusterId") String clusterId);

        /**
         * 更新Broker状态
         */
        @Update("UPDATE ke_broker_info SET " +
                        "  status = #{status}, " +
                        "  cpu_usage = #{cpuUsage}, " +
                        "  memory_usage = #{memoryUsage}, " +
                        "  startup_time = #{startupTime}, " +
                        "  updated_at = NOW() " +
                        "WHERE id = #{id}")
        int updateBrokerStatus(@Param("id") Long id,
                        @Param("status") String status,
                        @Param("cpuUsage") java.math.BigDecimal cpuUsage,
                        @Param("memoryUsage") java.math.BigDecimal memoryUsage,
                        @Param("startupTime") java.time.LocalDateTime startupTime);

        /**
         * 更新Broker动态信息（不更新host、port、jmx_port字段）
         */
        @Update("UPDATE ke_broker_info SET " +
                        "  status = #{status}, " +
                        "  cpu_usage = #{cpuUsage}, " +
                        "  memory_usage = #{memoryUsage}, " +
                        "  startup_time = #{startupTime}, " +
                        "  version = #{version}, " +
                        "  updated_at = NOW() " +
                        "WHERE cluster_id = #{clusterId} AND broker_id = #{brokerId}")
        int updateBrokerDynamicInfo(@Param("clusterId") String clusterId,
                        @Param("brokerId") Integer brokerId,
                        @Param("status") String status,
                        @Param("cpuUsage") java.math.BigDecimal cpuUsage,
                        @Param("memoryUsage") java.math.BigDecimal memoryUsage,
                        @Param("startupTime") java.time.LocalDateTime startupTime,
                        @Param("version") String version);

        /**
         * 检查Broker是否存在
         */
        @Select("SELECT COUNT(*) FROM ke_broker_info WHERE cluster_id = #{clusterId} AND broker_id = #{brokerId} AND host_ip = #{hostIp} AND port = #{port}")
        int checkBrokerExists(@Param("clusterId") String clusterId,
                        @Param("brokerId") Integer brokerId,
                        @Param("hostIp") String hostIp,
                        @Param("port") Integer port);

        /**
         * 查询所有Broker信息（不分页）
         */
        @Select("SELECT " +
                "  id, " +
                "  cluster_id as clusterId, " +
                "  broker_id as brokerId, " +
                "  host_ip as hostIp, " +
                "  port, " +
                "  port as kafkaPort, " +
                "  jmx_port as jmxPort, " +
                "  status, " +
                "  cpu_usage as cpuUsage, " +
                "  memory_usage as memoryUsage, " +
                "  startup_time as startupTime, " +
                "  version, " +
                "  created_by as createdBy, " +
                "  created_at as createdAt, " +
                "  updated_at as updatedAt " +
                "FROM ke_broker_info " +
                "ORDER BY cluster_id ASC, broker_id ASC")
        List<BrokerInfo> queryAllBrokers();

        /**
         * 根据集群ID查询所有Broker信息（不分页）
         */
        @Select("SELECT " +
                        "  id, " +
                        "  cluster_id as clusterId, " +
                        "  broker_id as brokerId, " +
                        "  host_ip as hostIp, " +
                        "  port, " +
                        "  port as kafkaPort, " +
                        "  jmx_port as jmxPort, " +
                        "  status, " +
                        "  cpu_usage as cpuUsage, " +
                        "  memory_usage as memoryUsage, " +
                        "  startup_time as startupTime, " +
                        "  version, " +
                        "  created_by as createdBy, " +
                        "  created_at as createdAt, " +
                        "  updated_at as updatedAt " +
                        "FROM ke_broker_info " +
                        "WHERE cluster_id = #{clusterId} " +
                        "ORDER BY broker_id ASC")
        List<BrokerInfo> getBrokersByClusterId(@Param("clusterId") String clusterId);

        // 新增：按集群统计Broker数量与在线数量
        @Select("SELECT COUNT(*) FROM ke_broker_info WHERE cluster_id = #{clusterId}")
        Long countByClusterId(@Param("clusterId") String clusterId);

        @Select("SELECT COUNT(*) FROM ke_broker_info WHERE cluster_id = #{clusterId} AND status = 'online'")
        Long countOnlineByClusterId(@Param("clusterId") String clusterId);

        /**
         * 查询Broker统计信息（全量）
         */
        @Select("SELECT " +
                "  COUNT(*) as total_count, " +
                "  SUM(CASE WHEN status = 'online' THEN 1 ELSE 0 END) as online_count, " +
                "  SUM(CASE WHEN status = 'offline' THEN 1 ELSE 0 END) as offline_count, " +
                "  AVG(cpu_usage) as avg_cpu_usage, " +
                "  AVG(memory_usage) as avg_memory_usage " +
                "FROM ke_broker_info")
        Map<String, Object> getBrokerStats();

        /**
         * 查询Broker统计信息（按集群）
         */
        @Select("SELECT " +
                "  COUNT(*) as total_count, " +
                "  SUM(CASE WHEN status = 'online' THEN 1 ELSE 0 END) as online_count, " +
                "  SUM(CASE WHEN status = 'offline' THEN 1 ELSE 0 END) as offline_count, " +
                "  AVG(cpu_usage) as avg_cpu_usage, " +
                "  AVG(memory_usage) as avg_memory_usage " +
                "FROM ke_broker_info WHERE cluster_id = #{clusterId}")
        Map<String, Object> getBrokerStatsByClusterId(@Param("clusterId") String clusterId);

        /**
         * 获取最早的启动时间（全量）
         */
        @Select("SELECT MIN(startup_time) FROM ke_broker_info WHERE status = 'online' AND startup_time IS NOT NULL")
        java.time.LocalDateTime getEarliestStartupTime();

        /**
         * 获取指定集群最早启动时间
         */
        @Select("SELECT MIN(startup_time) FROM ke_broker_info WHERE cluster_id = #{clusterId} AND status = 'online' AND startup_time IS NOT NULL")
        java.time.LocalDateTime getEarliestStartupTimeByClusterId(@Param("clusterId") String clusterId);

        /**
         * 根据集群ID获取Broker列表（用于告警配置下拉框）
         */
        @Select("SELECT " +
                "  broker_id as brokerId, " +
                "  CONCAT(host_ip, ':', port) as host " +
                "FROM ke_broker_info " +
                "WHERE cluster_id = #{clusterId} AND status = 'online' " +
                "ORDER BY broker_id ASC")
        List<Map<String, Object>> getBrokersForAlert(@Param("clusterId") String clusterId);
}