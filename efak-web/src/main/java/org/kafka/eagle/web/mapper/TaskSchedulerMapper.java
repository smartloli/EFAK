package org.kafka.eagle.web.mapper;

import org.apache.ibatis.annotations.*;
import org.kafka.eagle.dto.scheduler.TaskScheduler;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 任务调度Mapper
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/08/06 01:30:56
 * @version 5.0.0
 */
@Mapper
public interface TaskSchedulerMapper {

        /**
         * 查询任务调度列表
         */
        @Select("<script>" +
                        "SELECT * FROM ke_task_scheduler " +
                        "<where>" +
                        "<if test=\"taskName != null and taskName != ''\">" +
                        "AND task_name LIKE CONCAT('%', #{taskName}, '%') " +
                        "</if>" +
                        "<if test=\"taskType != null and taskType != ''\">" +
                        "AND task_type = #{taskType} " +
                        "</if>" +
                        "<if test=\"status != null and status != ''\">" +
                        "AND status = #{status} " +
                        "</if>" +
                        "<if test=\"clusterName != null and clusterName != ''\">" +
                        "AND cluster_name = #{clusterName} " +
                        "</if>" +
                        "</where>" +
                        "ORDER BY create_time DESC, id ASC " +
                        "LIMIT #{offset}, #{size}" +
                        "</script>")
        List<TaskScheduler> selectTaskSchedulerList(Map<String, Object> params);

        /**
         * 查询任务调度总数
         */
        @Select("<script>" +
                        "SELECT COUNT(*) FROM ke_task_scheduler " +
                        "<where>" +
                        "<if test=\"taskName != null and taskName != ''\">" +
                        "AND task_name LIKE CONCAT('%', #{taskName}, '%') " +
                        "</if>" +
                        "<if test=\"taskType != null and taskType != ''\">" +
                        "AND task_type = #{taskType} " +
                        "</if>" +
                        "<if test=\"status != null and status != ''\">" +
                        "AND status = #{status} " +
                        "</if>" +
                        "<if test=\"clusterName != null and clusterName != ''\">" +
                        "AND cluster_name = #{clusterName} " +
                        "</if>" +
                        "</where>" +
                        "</script>")
        int selectTaskSchedulerCount(Map<String, Object> params);

        /**
         * 根据ID查询任务调度
         */
        @Select("SELECT * FROM ke_task_scheduler WHERE id = #{id}")
        TaskScheduler selectTaskSchedulerById(Long id);

        /**
         * 根据任务名称查询任务调度
         */
        @Select("SELECT * FROM ke_task_scheduler WHERE task_name = #{taskName}")
        TaskScheduler selectTaskSchedulerByName(String taskName);

        /**
         * 插入任务调度
         */
        @Insert("INSERT INTO ke_task_scheduler (task_name, task_type, cron_expression, cluster_name, " +
                        "description, status, execute_count, success_count, fail_count, last_execute_time, " +
                        "next_execute_time, last_execute_result, error_message, created_by, create_time, update_time, timeout, config) "
                        +
                        "VALUES (#{taskName}, #{taskType}, #{cronExpression}, #{clusterName}, " +
                        "#{description}, #{status}, #{executeCount}, #{successCount}, #{failCount}, " +
                        "#{lastExecuteTime}, #{nextExecuteTime}, #{lastExecuteResult}, #{lastErrorMessage}, " +
                        "#{createdBy}, #{createTime}, #{updateTime}, #{timeout}, #{config})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insertTaskScheduler(TaskScheduler taskScheduler);

        /**
         * 更新任务调度
         */
        @Update("UPDATE ke_task_scheduler SET task_name = #{taskName}, task_type = #{taskType}, " +
                        "cron_expression = #{cronExpression}, cluster_name = #{clusterName}, " +
                        "description = #{description}, status = #{status}, " +
                        "last_execute_time = #{lastExecuteTime}, next_execute_time = #{nextExecuteTime}, " +
                        "last_execute_result = #{lastExecuteResult}, error_message = #{lastErrorMessage}, " +
                        "timeout = #{timeout}, config = #{config}, update_time = #{updateTime} WHERE id = #{id}")
        int updateTaskScheduler(TaskScheduler taskScheduler);

        /**
         * 更新任务调度的可编辑字段（安全更新）
         */
        @Update("UPDATE ke_task_scheduler SET " +
                        "cron_expression = #{cronExpression}, " +
                        "description = #{description}, " +
                        "timeout = #{timeout}, " +
                        "next_execute_time = #{nextExecuteTime}, " +
                        "update_time = #{updateTime} " +
                        "WHERE id = #{id}")
        int updateTaskSchedulerSafe(TaskScheduler taskScheduler);

        /**
         * 根据ID删除任务调度
         */
        @Delete("DELETE FROM ke_task_scheduler WHERE id = #{id}")
        int deleteTaskSchedulerById(Long id);

        /**
         * 根据ID列表批量删除任务调度
         */
        @Delete("<script>" +
                        "DELETE FROM ke_task_scheduler WHERE id IN " +
                        "<foreach collection='list' item='id' open='(' separator=',' close=')'>" +
                        "#{id}" +
                        "</foreach>" +
                        "</script>")
        int deleteTaskSchedulerByIds(List<Long> ids);

        /**
         * 更新任务状态
         */
        @Update("UPDATE ke_task_scheduler SET status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
        int updateTaskStatus(Long id, String status, String updateTime);

        /**
         * 更新任务执行结果
         */
        @Update("UPDATE ke_task_scheduler SET last_execute_time = #{lastExecuteTime}, " +
                        "next_execute_time = #{nextExecuteTime}, execute_count = execute_count + 1, " +
                        "success_count = success_count + #{successIncrement}, " +
                        "fail_count = fail_count + #{failIncrement}, " +
                        "last_execute_result = #{lastExecuteResult}, " +
                        "error_message = #{lastErrorMessage}, " +
                        "update_time = #{updateTime} WHERE id = #{id}")
        int updateTaskExecuteResult(Long id, String lastExecuteTime, String nextExecuteTime,
                        int successIncrement, int failIncrement, String lastExecuteResult, String lastErrorMessage,
                        String updateTime);

        /**
         * 更新任务的下次执行时间
         */
        @Update("UPDATE ke_task_scheduler SET next_execute_time = #{nextExecuteTime}, " +
                        "update_time = #{updateTime} WHERE id = #{id}")
        int updateNextExecuteTime(Long id, String nextExecuteTime, String updateTime);

        /**
         * 查询任务统计信息
         */
        @Select("SELECT " +
                        "COUNT(*) as totalTasks, " +
                        "SUM(CASE WHEN status = 'enabled' THEN 1 ELSE 0 END) as enabledTasks, " +
                        "SUM(CASE WHEN status = 'disabled' THEN 1 ELSE 0 END) as disabledTasks, " +
                        "SUM(CASE WHEN status = 'running' THEN 1 ELSE 0 END) as runningTasks, " +
                        "SUM(CASE WHEN status = 'error' THEN 1 ELSE 0 END) as errorTasks, " +
                        "SUM(execute_count) as totalExecutions, " +
                        "SUM(success_count) as totalSuccess, " +
                        "SUM(fail_count) as totalFail " +
                        "FROM ke_task_scheduler")
        Map<String, Object> selectTaskStats();

        /**
         * 查询启用的任务
         */
        @Select("SELECT * FROM ke_task_scheduler WHERE status = 'enabled' ORDER BY create_time DESC, id ASC")
        List<TaskScheduler> selectEnabledTasks();

        /**
         * 查询所有任务
         */
        @Select("SELECT * FROM ke_task_scheduler ORDER BY create_time DESC, id ASC")
        List<TaskScheduler> selectAllTasks();

        /**
         * 根据任务名称统计数量
         */
        @Select("SELECT COUNT(*) FROM ke_task_scheduler WHERE task_name = #{taskName}")
        int countByTaskName(String taskName);

        /**
         * 根据任务类型查询任务
         */
        @Select("SELECT * FROM ke_task_scheduler WHERE task_type = #{taskType} ORDER BY create_time DESC, id ASC")
        List<TaskScheduler> selectTasksByType(String taskType);

        /**
         * 根据集群名称查询任务
         */
        @Select("SELECT * FROM ke_task_scheduler WHERE cluster_name = #{clusterName} ORDER BY create_time DESC, id ASC")
        List<TaskScheduler> selectTasksByCluster(String clusterName);

        /**
         * 查询所有任务类型
         */
        @Select("SELECT DISTINCT task_type FROM ke_task_scheduler ORDER BY task_type")
        List<String> selectTaskTypes();

        /**
         * 查询所有集群名称
         */
        @Select("SELECT DISTINCT cluster_name FROM ke_task_scheduler WHERE cluster_name IS NOT NULL ORDER BY cluster_name")
        List<String> selectClusterNames();

        /**
         * 根据过期天数删除任务
         */
        @Delete("DELETE FROM ke_task_scheduler WHERE create_time < DATE_SUB(NOW(), INTERVAL #{daysToKeep} DAY)")
        int deleteExpiredTasks(int daysToKeep);

        /**
         * 重置所有任务的执行统计（每天0点执行）
         */
        @Update("UPDATE ke_task_scheduler SET execute_count = 0, success_count = 0, fail_count = 0, " +
                        "update_time = NOW()")
        int resetDailyTaskStats();
}