package org.kafka.eagle.web.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TaskUtils测试类
 */
public class TaskUtilsTest {

    @Test
    public void testValidateCronExpression() {
        // 测试有效的Cron表达式
        assertTrue(TaskUtils.validateCronExpression("0 */5 * * * ?")); // 每5分钟
        assertTrue(TaskUtils.validateCronExpression("0 0 2 * * ?")); // 每天凌晨2点
        assertTrue(TaskUtils.validateCronExpression("0 0 */1 * * ?")); // 每小时
        assertTrue(TaskUtils.validateCronExpression("0 0 1 * * ?")); // 每天凌晨1点
        assertTrue(TaskUtils.validateCronExpression("0 0 3 * * ?")); // 每天凌晨3点
        assertTrue(TaskUtils.validateCronExpression("0 0 12 * * ?")); // 每天中午12点

        // 测试带数字的周字段
        assertTrue(TaskUtils.validateCronExpression("0 0 12 * * 0")); // 周日
        assertTrue(TaskUtils.validateCronExpression("0 0 12 * * 7")); // 周日（另一种表示）
        assertTrue(TaskUtils.validateCronExpression("0 0 12 * * 1")); // 周一

        // 测试无效的Cron表达式
        assertFalse(TaskUtils.validateCronExpression("")); // 空字符串
        assertFalse(TaskUtils.validateCronExpression(null)); // null
        assertFalse(TaskUtils.validateCronExpression("0 */5 * * *")); // 字段数量不足
        assertFalse(TaskUtils.validateCronExpression("0 */5 * * * * *")); // 字段数量过多
        assertFalse(TaskUtils.validateCronExpression("0 60 * * * ?")); // 分钟超出范围
        assertFalse(TaskUtils.validateCronExpression("0 0 25 * * ?")); // 小时超出范围
        assertFalse(TaskUtils.validateCronExpression("0 0 12 32 * ?")); // 日期超出范围
        assertFalse(TaskUtils.validateCronExpression("0 0 12 * 13 ?")); // 月份超出范围
        assertFalse(TaskUtils.validateCronExpression("0 0 12 * * 8")); // 周超出范围
    }

    @Test
    public void testGetDefaultCronExpression() {
        // 测试默认Cron表达式
        assertEquals("0 */5 * * * ?", TaskUtils.getDefaultCronExpression("topic_monitor"));
        assertEquals("0 */5 * * * ?", TaskUtils.getDefaultCronExpression("consumer_monitor"));
        assertEquals("0 */5 * * * ?", TaskUtils.getDefaultCronExpression("cluster_monitor"));
        assertEquals("0 */5 * * * ?", TaskUtils.getDefaultCronExpression("alert_monitor"));
        assertEquals("0 0 2 * * ?", TaskUtils.getDefaultCronExpression("data_cleanup"));
        assertEquals("0 0 */1 * * ?", TaskUtils.getDefaultCronExpression("performance_stats"));
        assertEquals("0 0 1 * * ?", TaskUtils.getDefaultCronExpression("backup_task"));
        assertEquals("0 0 3 * * ?", TaskUtils.getDefaultCronExpression("log_analysis"));
        assertEquals("0 0 12 * * ?", TaskUtils.getDefaultCronExpression("unknown_type"));
    }
}