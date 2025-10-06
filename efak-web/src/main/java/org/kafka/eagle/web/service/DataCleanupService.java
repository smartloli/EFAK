package org.kafka.eagle.web.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 * DataCleanup 服务接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/30 01:07:44
 * @version 5.0.0
 */
@Slf4j
@Service
public class DataCleanupService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 清理指定表的过期数据
     * @param tableName 表名
     * @param timeColumn 时间字段名
     * @param retentionDays 数据保留天数
     * @return 删除的记录数
     */
    public int cleanupExpiredData(String tableName, String timeColumn, int retentionDays) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            String cutoffDateStr = cutoffDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 构建删除SQL
            String deleteSql = String.format("DELETE FROM %s WHERE %s < ?", tableName, timeColumn);

            // 执行删除操作
            int deletedCount = jdbcTemplate.update(deleteSql, cutoffDateStr);

            return deletedCount;

        } catch (Exception e) {
            log.error("清理表 {} 数据失败: {}", tableName, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 批量清理多个表的过期数据
     * @param tableTimeColumnMap 表名和时间字段的映射
     * @param retentionDays 数据保留天数
     * @return 总删除记录数
     */
    public int batchCleanupExpiredData(java.util.Map<String, String> tableTimeColumnMap, int retentionDays) {
        int totalDeletedCount = 0;

        for (java.util.Map.Entry<String, String> entry : tableTimeColumnMap.entrySet()) {
            String tableName = entry.getKey();
            String timeColumn = entry.getValue();

            try {
                int deletedCount = cleanupExpiredData(tableName, timeColumn, retentionDays);
                totalDeletedCount += deletedCount;
            } catch (Exception e) {
                log.error("清理表 {} 失败: {}", tableName, e.getMessage(), e);
                // 继续清理其他表，不中断整个过程
            }
        }

        return totalDeletedCount;
    }

    /**
     * 获取表的记录数统计
     * @param tableName 表名
     * @return 记录数
     */
    public long getTableRecordCount(String tableName) {
        try {
            String countSql = String.format("SELECT COUNT(*) FROM %s", tableName);
            Long count = jdbcTemplate.queryForObject(countSql, Long.class);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("获取表 {} 记录数失败: {}", tableName, e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * 获取表中指定时间字段的最早和最晚时间
     * @param tableName 表名
     * @param timeColumn 时间字段名
     * @return 包含最早和最晚时间的数组 [earliestTime, latestTime]
     */
    public String[] getTableTimeRange(String tableName, String timeColumn) {
        try {
            String minMaxSql = String.format("SELECT MIN(%s), MAX(%s) FROM %s", timeColumn, timeColumn, tableName);
            return jdbcTemplate.queryForObject(minMaxSql, (rs, rowNum) -> {
                String minTime = rs.getString(1);
                String maxTime = rs.getString(2);
                return new String[]{minTime, maxTime};
            });
        } catch (Exception e) {
            log.error("获取表 {} 时间范围失败: {}", tableName, e.getMessage(), e);
            return new String[]{"N/A", "N/A"};
        }
    }
}