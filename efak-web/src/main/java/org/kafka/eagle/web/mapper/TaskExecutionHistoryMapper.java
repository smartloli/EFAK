package org.kafka.eagle.web.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 任务执行历史Mapper
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/08/20 01:09:29
 * @version 5.0.0
 */
@Mapper
public interface TaskExecutionHistoryMapper {

        /**
         * 查询任务执行历史（分页）
         */
        @Select("SELECT *, " +
                        "CASE " +
                        "  WHEN start_time IS NOT NULL AND end_time IS NOT NULL THEN " +
                        "    TIMESTAMPDIFF(MICROSECOND, start_time, end_time) / 1000 " +
                        "  WHEN duration IS NOT NULL THEN duration " +
                        "  ELSE NULL " +
                        "END as calculated_duration " +
                        "FROM ke_task_execution_history " +
                        "WHERE task_id = #{taskId} " +
                        "ORDER BY start_time DESC " +
                        "LIMIT #{offset}, #{size}")
        List<Map<String, Object>> selectTaskExecutionHistory(Map<String, Object> params);

        /**
         * 查询任务执行历史总数
         */
        @Select("SELECT COUNT(*) FROM ke_task_execution_history WHERE task_id = #{taskId}")
        int selectTaskExecutionHistoryCount(Long taskId);

        /**
         * 插入任务执行历史
         */
        @Insert("INSERT INTO ke_task_execution_history (task_id, task_name, task_type, execution_status, " +
                        "start_time, end_time, duration, result_message, error_message, executor_node, " +
                        "trigger_type, trigger_user, input_params, output_result, created_time) " +
                        "VALUES (#{taskId}, #{taskName}, #{taskType}, #{executionStatus}, " +
                        "#{startTime}, #{endTime}, #{duration}, #{resultMessage}, #{errorMessage}, " +
                        "#{executorNode}, #{triggerType}, #{triggerUser}, #{inputParams}, #{outputResult}, " +
                        "#{createdTime})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insertTaskExecutionHistory(Map<String, Object> history);

        /**
         * 更新任务执行历史
         */
        @Update("UPDATE ke_task_execution_history SET end_time = #{endTime}, " +
                        "duration = #{duration}, execution_status = #{executionStatus}, " +
                        "result_message = #{resultMessage}, error_message = #{errorMessage}, " +
                        "updated_time = NOW() WHERE id = #{id}")
        int updateTaskExecutionHistory(Map<String, Object> history);

        /**
         * 根据ID查询执行历史详情
         */
        @Select("SELECT *, " +
                        "CASE " +
                        "  WHEN start_time IS NOT NULL AND end_time IS NOT NULL THEN " +
                        "    TIMESTAMPDIFF(MICROSECOND, start_time, end_time) / 1000 " +
                        "  WHEN duration IS NOT NULL THEN duration " +
                        "  ELSE NULL " +
                        "END as calculated_duration " +
                        "FROM ke_task_execution_history WHERE id = #{id}")
        Map<String, Object> selectTaskExecutionHistoryById(Long id);

        /**
         * 根据任务ID查询最新的执行历史
         */
        @Select("SELECT *, " +
                        "CASE " +
                        "  WHEN start_time IS NOT NULL AND end_time IS NOT NULL THEN " +
                        "    TIMESTAMPDIFF(MICROSECOND, start_time, end_time) / 1000 " +
                        "  WHEN duration IS NOT NULL THEN duration " +
                        "  ELSE NULL " +
                        "END as calculated_duration " +
                        "FROM ke_task_execution_history WHERE task_id = #{taskId} " +
                        "ORDER BY start_time DESC LIMIT 1")
        Map<String, Object> selectLatestTaskExecutionHistory(Long taskId);

        /**
         * 查询任务执行统计信息
         */
        @Select("SELECT " +
                        "COUNT(*) as total_executions, " +
                        "SUM(CASE WHEN execution_status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count, " +
                        "SUM(CASE WHEN execution_status = 'FAILED' THEN 1 ELSE 0 END) as fail_count, " +
                        "SUM(CASE WHEN execution_status = 'RUNNING' THEN 1 ELSE 0 END) as running_count, " +
                        "AVG(duration) as avg_duration, " +
                        "MAX(duration) as max_duration, " +
                        "MIN(duration) as min_duration " +
                        "FROM ke_task_execution_history WHERE task_id = #{taskId}")
        Map<String, Object> selectTaskExecutionStats(Long taskId);

        /**
         * 删除过期的执行历史记录
         */
        @Delete("DELETE FROM ke_task_execution_history WHERE start_time < DATE_SUB(NOW(), INTERVAL #{daysToKeep} DAY)")
        int deleteExpiredTaskExecutionHistory(int daysToKeep);

        /**
         * 查询最近的执行历史
         */
        @Select("SELECT * FROM ke_task_execution_history " +
                        "ORDER BY start_time DESC " +
                        "LIMIT #{limit}")
        List<Map<String, Object>> selectRecentTaskExecutionHistory(int limit);

        /**
         * 查询失败的执行历史
         */
        @Select("SELECT * FROM ke_task_execution_history " +
                        "WHERE execution_status = 'FAILED' " +
                        "ORDER BY start_time DESC " +
                        "LIMIT #{limit}")
        List<Map<String, Object>> selectFailedTaskExecutionHistory(int limit);

        /**
         * 查询所有任务执行历史（支持筛选）
         */
        @Select("<script>" +
                        "SELECT *, " +
                        "CASE " +
                        "  WHEN start_time IS NOT NULL AND end_time IS NOT NULL THEN " +
                        "    TIMESTAMPDIFF(MICROSECOND, start_time, end_time) / 1000 " +
                        "  WHEN duration IS NOT NULL THEN duration " +
                        "  ELSE NULL " +
                        "END as calculated_duration " +
                        "FROM ke_task_execution_history " +
                        "<where>" +
                        "<if test='taskName != null and taskName != \"\"'>" +
                        "task_name LIKE CONCAT('%', #{taskName}, '%') " +
                        "</if>" +
                        "<if test='status != null and status != \"\"'>" +
                        "AND execution_status = #{status} " +
                        "</if>" +
                        "<if test='startDate != null and startDate != \"\"'>" +
                        "AND start_time >= #{startDate} " +
                        "</if>" +
                        "<if test='endDate != null and endDate != \"\"'>" +
                        "AND start_time &lt;= #{endDate} " +
                        "</if>" +
                        "</where>" +
                        "ORDER BY start_time DESC " +
                        "LIMIT #{offset}, #{size}" +
                        "</script>")
        List<Map<String, Object>> selectAllTaskExecutionHistory(Map<String, Object> params);

        /**
         * 查询所有任务执行历史总数（支持筛选）
         */
        @Select("<script>" +
                        "SELECT COUNT(*) FROM ke_task_execution_history " +
                        "<where>" +
                        "<if test='taskName != null and taskName != \"\"'>" +
                        "task_name LIKE CONCAT('%', #{taskName}, '%') " +
                        "</if>" +
                        "<if test='status != null and status != \"\"'>" +
                        "AND execution_status = #{status} " +
                        "</if>" +
                        "<if test='startDate != null and startDate != \"\"'>" +
                        "AND start_time >= #{startDate} " +
                        "</if>" +
                        "<if test='endDate != null and endDate != \"\"'>" +
                        "AND start_time &lt;= #{endDate} " +
                        "</if>" +
                        "</where>" +
                        "</script>")
        int selectAllTaskExecutionHistoryCount(Map<String, Object> params);
}