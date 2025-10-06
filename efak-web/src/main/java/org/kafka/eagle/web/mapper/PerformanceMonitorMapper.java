package org.kafka.eagle.web.mapper;

import org.apache.ibatis.annotations.*;
import org.kafka.eagle.dto.performance.PerformanceMonitor;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 性能监控Mapper
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/24 23:43:00
 * @version 5.0.0
 */
@Mapper
public interface PerformanceMonitorMapper {

    /**
     * Insert performance monitor data
     *
     * @param performanceMonitor performance monitor information
     * @return affected rows
     */
    @Insert("INSERT INTO ke_performance_monitor (cluster_id, kafka_host, message_in, byte_in, byte_out, time_ms_produce, " +
            "time_ms_consumer, memory_usage, cpu_usage, collect_time, collect_date) " +
            "VALUES (#{clusterId}, #{kafkaHost}, #{messageIn}, #{byteIn}, #{byteOut}, #{timeMsProduce}, " +
            "#{timeMsConsumer}, #{memoryUsage}, #{cpuUsage}, #{collectTime}, #{collectDate})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PerformanceMonitor performanceMonitor);

    /**
     * Batch insert performance monitor data
     *
     * @param performanceMonitorList list of performance monitors
     * @return affected rows
     */
    @Insert("<script>" +
            "INSERT INTO ke_performance_monitor (cluster_id, kafka_host, message_in, byte_in, byte_out, time_ms_produce, " +
            "time_ms_consumer, memory_usage, cpu_usage, collect_time, collect_date) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.clusterId}, #{item.kafkaHost}, #{item.messageIn}, #{item.byteIn}, #{item.byteOut}, #{item.timeMsProduce}, " +
            "#{item.timeMsConsumer}, #{item.memoryUsage}, #{item.cpuUsage}, #{item.collectTime}, #{item.collectDate})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("list") List<PerformanceMonitor> performanceMonitorList);

    /**
     * Query performance monitor data with pagination
     *
     * @param params query parameters
     * @return performance monitor list
     */
    @Select("<script>" +
            "SELECT id, cluster_id as clusterId, kafka_host as kafkaHost, message_in as messageIn, byte_in as byteIn, " +
            "byte_out as byteOut, time_ms_produce as timeMsProduce, time_ms_consumer as timeMsConsumer, memory_usage as memoryUsage, " +
            "cpu_usage as cpuUsage, collect_time as collectTime, collect_date as collectDate " +
            "FROM ke_performance_monitor " +
            "<where>" +
            "<if test=\"clusterId != null and clusterId != ''\">" +
            "AND cluster_id = #{clusterId} " +
            "</if>" +
            "<if test=\"kafkaHost != null and kafkaHost != ''\">" +
            "AND kafka_host = #{kafkaHost} " +
            "</if>" +
            "<if test='memoryUsageThreshold != null'>" +
            "AND memory_usage >= #{memoryUsageThreshold} " +
            "</if>" +
            "<if test='cpuUsageThreshold != null'>" +
            "AND cpu_usage >= #{cpuUsageThreshold} " +
            "</if>" +
            "<if test='messageInThreshold != null'>" +
            "AND message_in >= #{messageInThreshold} " +
            "</if>" +
            "<if test='byteInThreshold != null'>" +
            "AND byte_in >= #{byteInThreshold} " +
            "</if>" +
            "<if test='byteOutThreshold != null'>" +
            "AND byte_out >= #{byteOutThreshold} " +
            "</if>" +
            "<if test='timeMsProduceThreshold != null'>" +
            "AND time_ms_produce >= #{timeMsProduceThreshold} " +
            "</if>" +
            "<if test='startDate != null'>" +
            "AND collect_date >= #{startDate} " +
            "</if>" +
            "<if test='endDate != null'>" +
            "AND collect_date &lt;= #{endDate} " +
            "</if>" +
            "</where>" +
            "ORDER BY collect_time DESC " +
            "<if test='offset != null and limit != null'>" +
            "LIMIT #{offset}, #{limit}" +
            "</if>" +
            "</script>")
    List<PerformanceMonitor> selectPerformanceMonitors(Map<String, Object> params);

    /**
     * Count performance monitor records
     *
     * @param params query parameters
     * @return total count
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM ke_performance_monitor " +
            "<where>" +
            "<if test=\"clusterId != null and clusterId != ''\">" +
            "AND cluster_id = #{clusterId} " +
            "</if>" +
            "<if test=\"kafkaHost != null and kafkaHost != ''\">" +
            "AND kafka_host = #{kafkaHost} " +
            "</if>" +
            "<if test='memoryUsageThreshold != null'>" +
            "AND memory_usage >= #{memoryUsageThreshold} " +
            "</if>" +
            "<if test='cpuUsageThreshold != null'>" +
            "AND cpu_usage >= #{cpuUsageThreshold} " +
            "</if>" +
            "<if test='messageInThreshold != null'>" +
            "AND message_in >= #{messageInThreshold} " +
            "</if>" +
            "<if test='byteInThreshold != null'>" +
            "AND byte_in >= #{byteInThreshold} " +
            "</if>" +
            "<if test='byteOutThreshold != null'>" +
            "AND byte_out >= #{byteOutThreshold} " +
            "</if>" +
            "<if test='timeMsProduceThreshold != null'>" +
            "AND time_ms_produce >= #{timeMsProduceThreshold} " +
            "</if>" +
            "<if test='startDate != null'>" +
            "AND collect_date >= #{startDate} " +
            "</if>" +
            "<if test='endDate != null'>" +
            "AND collect_date &lt;= #{endDate} " +
            "</if>" +
            "</where>" +
            "</script>")
    int countPerformanceMonitors(Map<String, Object> params);

    /**
     * Get latest performance monitor for each cluster
     *
     * @return latest performance monitor list
     */
    @Select("SELECT pm1.id, pm1.cluster_id as clusterId, pm1.kafka_host as kafkaHost, pm1.message_in as messageIn, pm1.byte_in as byteIn, " +
            "pm1.byte_out as byteOut, pm1.time_ms_produce as timeMsProduce, pm1.time_ms_consumer as timeMsConsumer, pm1.memory_usage as memoryUsage, " +
            "pm1.cpu_usage as cpuUsage, pm1.collect_time as collectTime, pm1.collect_date as collectDate " +
            "FROM ke_performance_monitor pm1 " +
            "INNER JOIN (" +
            "  SELECT cluster_id, MAX(collect_time) as max_collect_time " +
            "  FROM ke_performance_monitor " +
            "  GROUP BY cluster_id" +
            ") pm2 ON pm1.cluster_id = pm2.cluster_id AND pm1.collect_time = pm2.max_collect_time " +
            "ORDER BY pm1.cluster_id")
    List<PerformanceMonitor> selectLatestPerformanceMonitors();

    /**
     * Delete old performance monitor data
     *
     * @param beforeDate delete records before this date
     * @return affected rows
     */
    @Delete("DELETE FROM ke_performance_monitor WHERE collect_date < #{beforeDate}")
    int deleteOldMonitors(@Param("beforeDate") java.time.LocalDate beforeDate);

    /**
     * Get performance statistics
     *
     * @param clusterId cluster id
     * @param startDate start date
     * @param endDate end date
     * @return statistics map
     */
    @Select("SELECT " +
            "AVG(message_in) as avgMessageIn, " +
            "MAX(message_in) as maxMessageIn, " +
            "MIN(message_in) as minMessageIn, " +
            "AVG(byte_in) as avgByteIn, " +
            "MAX(byte_in) as maxByteIn, " +
            "MIN(byte_in) as minByteIn, " +
            "AVG(byte_out) as avgByteOut, " +
            "MAX(byte_out) as maxByteOut, " +
            "MIN(byte_out) as minByteOut, " +
            "AVG(time_ms_produce) as avgTimeMsProduce, " +
            "MAX(time_ms_produce) as maxTimeMsProduce, " +
            "MIN(time_ms_produce) as minTimeMsProduce, " +
            "AVG(time_ms_consumer) as avgTimeMsConsumer, " +
            "MAX(time_ms_consumer) as maxTimeMsConsumer, " +
            "MIN(time_ms_consumer) as minTimeMsConsumer, " +
            "AVG(memory_usage) as avgMemoryUsage, " +
            "MAX(memory_usage) as maxMemoryUsage, " +
            "MIN(memory_usage) as minMemoryUsage, " +
            "AVG(cpu_usage) as avgCpuUsage, " +
            "MAX(cpu_usage) as maxCpuUsage, " +
            "MIN(cpu_usage) as minCpuUsage, " +
            "SUM(message_in) as totalMessageIn, " +
            "SUM(byte_in) as totalByteIn, " +
            "SUM(byte_out) as totalByteOut " +
            "FROM ke_performance_monitor " +
            "WHERE cluster_id = #{clusterId} " +
            "AND collect_date >= #{startDate} " +
            "AND collect_date <= #{endDate}")
    Map<String, Object> getPerformanceStatistics(@Param("clusterId") String clusterId,
                                                 @Param("startDate") java.time.LocalDate startDate,
                                                 @Param("endDate") java.time.LocalDate endDate);

    /**
     * Get performance trend data for chart display with time-based aggregation
     *
     * @param clusterId cluster ID
     * @param startTime start datetime
     * @param endTime end datetime
     * @return trend data aggregated by time
     */
    @Select("SELECT " +
            "DATE_FORMAT(collect_time, '%Y-%m-%d %H:%i:00') as collectTime, " +
            "AVG(message_in) as avgMessageIn, " +
            "AVG(byte_in) as avgByteIn, " +
            "AVG(byte_out) as avgByteOut, " +
            "AVG(time_ms_produce) as avgTimeMsProduce, " +
            "AVG(time_ms_consumer) as avgTimeMsConsumer, " +
            "AVG(memory_usage) as avgMemoryUsage, " +
            "AVG(cpu_usage) as avgCpuUsage " +
            "FROM ke_performance_monitor " +
            "WHERE cluster_id = #{clusterId} " +
            "AND collect_time >= #{startTime} " +
            "AND collect_time <= #{endTime} " +
            "GROUP BY DATE_FORMAT(collect_time, '%Y-%m-%d %H:%i:00') " +
            "ORDER BY collectTime ASC")
    List<Map<String, Object>> getPerformanceTrendDataByTime(@Param("clusterId") String clusterId,
                                                           @Param("startTime") java.time.LocalDateTime startTime,
                                                           @Param("endTime") java.time.LocalDateTime endTime);

    /**
     * Get performance trend data for chart display (original method)
     *
     * @param clusterId cluster ID
     * @param startDate start date
     * @param endDate end date
     * @return trend data
     */
    @Select("SELECT " +
            "collect_date as date, " +
            "AVG(message_in) as avgMessageIn, " +
            "AVG(byte_in) as avgByteIn, " +
            "AVG(byte_out) as avgByteOut, " +
            "AVG(time_ms_produce) as avgTimeMsProduce, " +
            "AVG(time_ms_consumer) as avgTimeMsConsumer, " +
            "AVG(memory_usage) as avgMemoryUsage, " +
            "AVG(cpu_usage) as avgCpuUsage " +
            "FROM ke_performance_monitor " +
            "WHERE cluster_id = #{clusterId} " +
            "AND collect_date >= #{startDate} " +
            "AND collect_date <= #{endDate} " +
            "GROUP BY collect_date " +
            "ORDER BY collect_date ASC")
    List<Map<String, Object>> getPerformanceTrendData(@Param("clusterId") String clusterId,
                                                      @Param("startDate") java.time.LocalDate startDate,
                                                      @Param("endDate") java.time.LocalDate endDate);

    /**
     * Get real-time performance data for monitoring dashboard
     *
     * @param clusterId cluster ID
     * @return real-time performance data
     */
    @Select("SELECT " +
            "cluster_id as clusterId, " +
            "kafka_host as kafkaHost, " +
            "message_in as messageIn, " +
            "byte_in as byteIn, " +
            "byte_out as byteOut, " +
            "time_ms_produce as timeMsProduce, " +
            "time_ms_consumer as timeMsConsumer, " +
            "memory_usage as memoryUsage, " +
            "cpu_usage as cpuUsage, " +
            "collect_time as collectTime " +
            "FROM ke_performance_monitor " +
            "WHERE cluster_id = #{clusterId} " +
            "ORDER BY collect_time DESC " +
            "LIMIT 1")
    PerformanceMonitor selectLatestByClusterId(@Param("clusterId") String clusterId);

    /**
     * Get performance data by Kafka host
     *
     * @param kafkaHost Kafka host
     * @param startDate start date
     * @param endDate end date
     * @return performance monitor list
     */
    @Select("SELECT id, cluster_id as clusterId, kafka_host as kafkaHost, message_in as messageIn, byte_in as byteIn, " +
            "byte_out as byteOut, time_ms_produce as timeMsProduce, time_ms_consumer as timeMsConsumer, memory_usage as memoryUsage, " +
            "cpu_usage as cpuUsage, collect_time as collectTime, collect_date as collectDate " +
            "FROM ke_performance_monitor " +
            "WHERE kafka_host = #{kafkaHost} " +
            "AND collect_date >= #{startDate} " +
            "AND collect_date <= #{endDate} " +
            "ORDER BY collect_time DESC")
    List<PerformanceMonitor> selectByKafkaHost(@Param("kafkaHost") String kafkaHost,
                                               @Param("startDate") java.time.LocalDate startDate,
                                               @Param("endDate") java.time.LocalDate endDate);

    /**
     * Get high resource usage alerts
     *
     * @param memoryThreshold memory usage threshold
     * @param cpuThreshold CPU usage threshold
     * @return performance monitors with high resource usage
     */
    @Select("SELECT id, cluster_id as clusterId, kafka_host as kafkaHost, message_in as messageIn, byte_in as byteIn, " +
            "byte_out as byteOut, time_ms_produce as timeMsProduce, time_ms_consumer as timeMsConsumer, memory_usage as memoryUsage, " +
            "cpu_usage as cpuUsage, collect_time as collectTime, collect_date as collectDate " +
            "FROM ke_performance_monitor " +
            "WHERE memory_usage >= #{memoryThreshold} OR cpu_usage >= #{cpuThreshold} " +
            "ORDER BY collect_time DESC " +
            "LIMIT 100")
    List<PerformanceMonitor> selectHighResourceUsage(@Param("memoryThreshold") java.math.BigDecimal memoryThreshold,
                                                     @Param("cpuThreshold") java.math.BigDecimal cpuThreshold);

    /**
     * Get resource usage trend by host
     *
     * @param kafkaHost kafka host
     * @param startDate start date
     * @param endDate end date
     * @return resource usage trend data
     */
    @Select("SELECT " +
            "collect_date as date, " +
            "AVG(memory_usage) as avgMemoryUsage, " +
            "MAX(memory_usage) as maxMemoryUsage, " +
            "AVG(cpu_usage) as avgCpuUsage, " +
            "MAX(cpu_usage) as maxCpuUsage " +
            "FROM ke_performance_monitor " +
            "WHERE kafka_host = #{kafkaHost} " +
            "AND collect_date >= #{startDate} " +
            "AND collect_date <= #{endDate} " +
            "GROUP BY collect_date " +
            "ORDER BY collect_date ASC")
    List<Map<String, Object>> getResourceUsageTrendByHost(@Param("kafkaHost") String kafkaHost,
                                                          @Param("startDate") java.time.LocalDate startDate,
                                                          @Param("endDate") java.time.LocalDate endDate);

    /**
     * Get latest performance monitor data for specific cluster within last 10 minutes
     *
     * @param clusterId cluster ID
     * @return latest performance monitor within 10 minutes
     */
    @Select("SELECT id, cluster_id as clusterId, kafka_host as kafkaHost, message_in as messageIn, byte_in as byteIn, " +
            "byte_out as byteOut, time_ms_produce as timeMsProduce, time_ms_consumer as timeMsConsumer, memory_usage as memoryUsage, " +
            "cpu_usage as cpuUsage, collect_time as collectTime, collect_date as collectDate " +
            "FROM ke_performance_monitor " +
            "WHERE cluster_id = #{clusterId} " +
            "AND collect_time >= DATE_SUB(NOW(), INTERVAL 10 MINUTE) " +
            "ORDER BY collect_time DESC " +
            "LIMIT 1")
    PerformanceMonitor selectLatestPerformanceByClusterWithin10Minutes(@Param("clusterId") String clusterId);

    /**
     * 根据集群ID查询性能监控数据
     */
    @Select("SELECT " +
            "cluster_id as clusterId, " +
            "kafka_host as kafkaHost, " +
            "message_in as messageIn, " +
            "byte_in as byteIn, " +
            "byte_out as byteOut, " +
            "time_ms_produce as timeMsProduce, " +
            "time_ms_consumer as timeMsConsumer, " +
            "memory_usage as memoryUsage, " +
            "cpu_usage as cpuUsage, " +
            "collect_time as collectTime " +
            "FROM ke_performance_monitor " +
            "WHERE cluster_id = #{clusterId} " +
            "ORDER BY collect_time DESC LIMIT 100")
    List<Map<String, Object>> findByClusterId(@Param("clusterId") String clusterId);

    /**
     * 根据集群ID和IP查询性能监控数据
     */
    @Select("SELECT " +
            "cluster_id as clusterId, " +
            "kafka_host as kafkaHost, " +
            "message_in as messageIn, " +
            "byte_in as byteIn, " +
            "byte_out as byteOut, " +
            "time_ms_produce as timeMsProduce, " +
            "time_ms_consumer as timeMsConsumer, " +
            "memory_usage as memoryUsage, " +
            "cpu_usage as cpuUsage, " +
            "collect_time as collectTime " +
            "FROM ke_performance_monitor " +
            "WHERE cluster_id = #{clusterId} AND kafka_host = #{ip} " +
            "ORDER BY collect_time DESC LIMIT 100")
    List<Map<String, Object>> findByClusterIdAndIp(@Param("clusterId") String clusterId, @Param("ip") String ip);
}