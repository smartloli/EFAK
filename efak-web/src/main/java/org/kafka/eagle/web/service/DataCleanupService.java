package org.kafka.eagle.web.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

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
     * 仅允许安全的 SQL 标识符（表名/字段名），避免拼接 SQL 时被注入。
     * 注意：此处不允许 schema 前缀（如 db.table）；如确需支持可按需放宽规则。
     */
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[A-Za-z0-9_]+$");

    /**
     * 清理指定表的过期数据
     * @param tableName 表名
     * @param timeColumn 时间字段名
     * @param retentionDays 数据保留天数
     * @return 删除的记录数
     */
    public int cleanupExpiredData(String tableName, String timeColumn, int retentionDays) {
        try {
            validateIdentifier(tableName, "表名");
            validateIdentifier(timeColumn, "时间字段");

            // 关键逻辑：保留天数必须为正数；非正数直接跳过，避免误删或时间计算异常
            if (retentionDays <= 0) {
                log.warn("保留天数 retentionDays={} 非法，跳过清理 (table={}, column={})", retentionDays, tableName, timeColumn);
                return 0;
            }

            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            // 使用 Timestamp 让 JDBC 驱动按字段类型处理，避免字符串格式/时区差异导致查询异常
            Timestamp cutoffTimestamp = Timestamp.valueOf(cutoffDate);

            // 构建删除SQL
            String deleteSql = String.format("DELETE FROM %s WHERE %s < ?", tableName, timeColumn);

            // 执行删除操作
            int deletedCount = jdbcTemplate.update(deleteSql, cutoffTimestamp);

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
            validateIdentifier(tableName, "表名");
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
            validateIdentifier(tableName, "表名");
            validateIdentifier(timeColumn, "时间字段");
            String minMaxSql = String.format("SELECT MIN(%s), MAX(%s) FROM %s", timeColumn, timeColumn, tableName);
            return jdbcTemplate.queryForObject(minMaxSql, (rs, rowNum) -> {
                String minTime = rs.getString(1);
                String maxTime = rs.getString(2);
                // 空表时 MIN/MAX 可能为 null，这里统一返回 N/A 便于前端展示
                return new String[]{minTime != null ? minTime : "N/A", maxTime != null ? maxTime : "N/A"};
            });
        } catch (Exception e) {
            log.error("获取表 {} 时间范围失败: {}", tableName, e.getMessage(), e);
            return new String[]{"N/A", "N/A"};
        }
    }

    private static void validateIdentifier(String value, String type) {
        // 关键逻辑：防止将外部输入直接拼接进 SQL（表名/字段名无法用 ? 占位符绑定）
        if (value == null || value.isBlank() || !SAFE_IDENTIFIER.matcher(value).matches()) {
            throw new IllegalArgumentException(type + "非法: " + value);
        }
    }
}
