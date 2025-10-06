package org.kafka.eagle.web.mapper;

import org.apache.ibatis.annotations.*;
import org.kafka.eagle.dto.broker.BrokerMetrics;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Broker性能指标数据访问层
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/14 01:20:56
 * @version 5.0.0
 */
@Mapper
public interface BrokerMetricsMapper {

        /**
         * 插入Broker性能指标数据
         */
        @Insert("INSERT INTO ke_broker_metrics (cluster_id, broker_id, host_ip, port, cpu_usage, memory_usage, collect_time) " +
                        "VALUES (#{clusterId}, #{brokerId}, #{hostIp}, #{port}, #{cpuUsage}, #{memoryUsage}, #{collectTime})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insertBrokerMetrics(BrokerMetrics metrics);

        /**
         * 批量插入Broker性能指标数据
         */
        @Insert("<script>" +
                        "INSERT INTO ke_broker_metrics (cluster_id, broker_id, host_ip, port, cpu_usage, memory_usage, collect_time) VALUES "
                        +
                        "<foreach collection='list' item='item' separator=','>" +
                        "(#{item.clusterId}, #{item.brokerId}, #{item.hostIp}, #{item.port}, #{item.cpuUsage}, #{item.memoryUsage}, #{item.collectTime})"
                        +
                        "</foreach>" +
                        "</script>")
        int batchInsertBrokerMetrics(@Param("list") List<BrokerMetrics> metricsList);

        /**
         * 查询指定时间范围内的Broker性能指标数据
         */
        @Select("<script>" +
                        "SELECT " +
                        "  id, " +
                        "  broker_id as brokerId, " +
                        "  host_ip as hostIp, " +
                        "  port, " +
                        "  cpu_usage as cpuUsage, " +
                        "  memory_usage as memoryUsage, " +
                        "  collect_time as collectTime, " +
                        "  create_time as createTime " +
                        "FROM ke_broker_metrics " +
                        "WHERE collect_time BETWEEN #{startTime} AND #{endTime} " +
                        "<if test='brokerId != null'>" +
                        "  AND broker_id = #{brokerId} " +
                        "</if>" +
                        "ORDER BY collect_time ASC" +
                        "</script>")
        List<BrokerMetrics> queryMetricsByTimeRange(@Param("brokerId") Integer brokerId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);


        /**
         * 删除指定时间之前的历史数据
         */
        @Delete("DELETE FROM ke_broker_metrics WHERE collect_time < #{beforeTime}")
        int deleteMetricsBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);

        /**
         * 删除历史数据
         * 
         * @param cutoffTime 截止时间
         * @return 删除的记录数
         */
        @Delete("DELETE FROM ke_broker_metrics WHERE collect_time < #{cutoffTime}")
        int deleteHistoricalData(@Param("cutoffTime") LocalDateTime cutoffTime);

        /**
         * 查询最新的Broker性能指标数据
         */
        @Select("<script>" +
                        "SELECT " +
                        "  id, " +
                        "  broker_id as brokerId, " +
                        "  host_ip as hostIp, " +
                        "  port, " +
                        "  cpu_usage as cpuUsage, " +
                        "  memory_usage as memoryUsage, " +
                        "  collect_time as collectTime, " +
                        "  create_time as createTime " +
                        "FROM ke_broker_metrics " +
                        "WHERE 1=1 " +
                        "<if test='brokerId != null'>" +
                        "  AND broker_id = #{brokerId} " +
                        "</if>" +
                        "ORDER BY collect_time DESC " +
                        "LIMIT #{limit}" +
                        "</script>")
        List<BrokerMetrics> queryLatestMetrics(@Param("brokerId") Integer brokerId,
                        @Param("limit") Integer limit);




        /**
         * 统一的趋势数据查询方法（支持CPU和内存，支持集群过滤）
         */
        @Select("<script>" +
                        "<choose>" +
                        "  <when test='aggregationType == \"hourly\"'>" +
                        "    SELECT " +
                        "      broker_id as brokerId, " +
                        "      host_ip as hostIp, " +
                        "      DATE_FORMAT(collect_time, '%Y-%m-%d %H:00:00') as collectTime, " +
                        "      <choose>" +
                        "        <when test='metricType == \"cpu\"'>" +
                        "          AVG(cpu_usage) as value, " +
                        "        </when>" +
                        "        <otherwise>" +
                        "          AVG(memory_usage) as value, " +
                        "        </otherwise>" +
                        "      </choose>" +
                        "      COUNT(*) as sampleCount " +
                        "    FROM ke_broker_metrics " +
                        "    WHERE collect_time BETWEEN #{startTime} AND #{endTime} " +
                        "    <if test='clusterId != null and clusterId != \"\"'>" +
                        "      AND cluster_id = #{clusterId} " +
                        "    </if>" +
                        "    GROUP BY broker_id, host_ip, DATE_FORMAT(collect_time, '%Y-%m-%d %H') " +
                        "    ORDER BY DATE_FORMAT(collect_time, '%Y-%m-%d %H') ASC" +
                        "  </when>" +
                        "  <when test='aggregationType == \"daily\"'>" +
                        "    SELECT " +
                        "      broker_id as brokerId, " +
                        "      host_ip as hostIp, " +
                        "      DATE_FORMAT(collect_time, '%Y-%m-%d 00:00:00') as collectTime, " +
                        "      <choose>" +
                        "        <when test='metricType == \"cpu\"'>" +
                        "          AVG(cpu_usage) as value, " +
                        "        </when>" +
                        "        <otherwise>" +
                        "          AVG(memory_usage) as value, " +
                        "        </otherwise>" +
                        "      </choose>" +
                        "      COUNT(*) as sampleCount " +
                        "    FROM ke_broker_metrics " +
                        "    WHERE collect_time BETWEEN #{startTime} AND #{endTime} " +
                        "    <if test='clusterId != null and clusterId != \"\"'>" +
                        "      AND cluster_id = #{clusterId} " +
                        "    </if>" +
                        "    GROUP BY broker_id, host_ip, DATE_FORMAT(collect_time, '%Y-%m-%d') " +
                        "    ORDER BY DATE_FORMAT(collect_time, '%Y-%m-%d') ASC" +
                        "  </when>" +
                        "  <otherwise>" +
                        "    SELECT " +
                        "      broker_id as brokerId, " +
                        "      host_ip as hostIp, " +
                        "      collect_time as collectTime, " +
                        "      <choose>" +
                        "        <when test='metricType == \"cpu\"'>" +
                        "          cpu_usage as value, " +
                        "        </when>" +
                        "        <otherwise>" +
                        "          memory_usage as value, " +
                        "        </otherwise>" +
                        "      </choose>" +
                        "      1 as sampleCount " +
                        "    FROM ke_broker_metrics " +
                        "    WHERE collect_time BETWEEN #{startTime} AND #{endTime} " +
                        "    <if test='clusterId != null and clusterId != \"\"'>" +
                        "      AND cluster_id = #{clusterId} " +
                        "    </if>" +
                        "    ORDER BY collect_time ASC" +
                        "  </otherwise>" +
                        "</choose>" +
                        "</script>")
        List<Map<String, Object>> queryTrendDataUnified(@Param("metricType") String metricType,
                        @Param("clusterId") String clusterId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime,
                        @Param("aggregationType") String aggregationType);

        /**
         * 根据集群ID查询所有Broker监控指标
         */
        @Select("SELECT " +
                        "broker_id as brokerId, " +
                        "host_ip as hostIp, " +
                        "port, " +
                        "cpu_usage as cpuUsage, " +
                        "memory_usage as memoryUsage, " +
                        "collect_time as collectTime " +
                        "FROM ke_broker_metrics " +
                        "WHERE cluster_id = #{clusterId} " +
                        "ORDER BY collect_time DESC LIMIT 100")
        List<Map<String, Object>> findByClusterId(@Param("clusterId") String clusterId);

        /**
         * 根据集群ID和IP查询Broker监控指标
         */
        @Select("SELECT " +
                        "broker_id as brokerId, " +
                        "host_ip as hostIp, " +
                        "port, " +
                        "cpu_usage as cpuUsage, " +
                        "memory_usage as memoryUsage, " +
                        "collect_time as collectTime " +
                        "FROM ke_broker_metrics " +
                        "WHERE cluster_id = #{clusterId} AND host_ip = #{ip} " +
                        "ORDER BY collect_time DESC LIMIT 100")
        List<Map<String, Object>> findByClusterIdAndIp(@Param("clusterId") String clusterId, @Param("ip") String ip);
}