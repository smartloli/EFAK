package org.kafka.eagle.web.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.kafka.eagle.dto.scheduler.CollectRound;

@Mapper
public interface CollectRoundMapper {

    @Insert("INSERT INTO ke_collect_round (round_id, task_type, node_id, cluster_id, assigned_count, success_count, skipped_count, error_message, started_at) "
            + "VALUES (#{roundId}, #{taskType}, #{nodeId}, #{clusterId}, #{assignedCount}, #{successCount}, #{skippedCount}, #{errorMessage}, #{startedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CollectRound round);

    @Update("UPDATE ke_collect_round SET success_count = #{successCount}, skipped_count = #{skippedCount}, "
            + "error_message = #{errorMessage}, finished_at = #{finishedAt} WHERE round_id = #{roundId} AND node_id = #{nodeId} AND task_type = #{taskType}")
    int finish(CollectRound round);
}
