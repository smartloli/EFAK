package org.kafka.eagle.web.mapper;

import org.apache.ibatis.annotations.*;
import org.kafka.eagle.dto.cluster.KafkaClusterInfo;

import java.util.List;

/**
 * <p>
 * Cluster集群数据访问层接口
 * 提供Kafka集群信息的数据库操作
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/08/31 12:57:36
 * @version 5.0.0
 */
@Mapper
public interface ClusterMapper {

    @Select("<script>" +
            "SELECT " +
            "  id, " +
            "  cluster_id as clusterId, " +
            "  cluster_name as name, " +
            "  cluster_type as clusterType, " +
            "  auth, " +
            "  auth_config as authConfig, " +
            "  availability, " +
            "  created_time as createdAt, " +
            "  updated_time as updatedAt, " +
            "  total_nodes as totalNodes, " +
            "  online_nodes as onlineNodes " +
            "FROM ke_cluster c " +
            "WHERE 1=1 " +
            "<if test=\"search != null and search != ''\">" +
            "  AND (c.cluster_name LIKE CONCAT('%', #{search}, '%') OR c.cluster_id LIKE CONCAT('%', #{search}, '%')) " +
            "</if>" +
            "<if test=\"clusterType != null and clusterType != ''\">" +
            "  AND c.cluster_type = #{clusterType} " +
            "</if>" +
            "<if test=\"auth != null and auth != ''\">" +
            "  AND c.auth = #{auth} " +
            "</if>" +
            "ORDER BY " +
            "<choose>" +
            "  <when test='sortField == \"name\"'>c.cluster_name</when>" +
            "  <when test='sortField == \"clusterType\"'>c.cluster_type</when>" +
            "  <when test='sortField == \"createdAt\"'>c.created_time</when>" +
            "  <when test='sortField == \"updatedAt\"'>c.updated_time</when>" +
            "  <otherwise>c.updated_time</otherwise>" +
            "</choose> " +
            "${sortOrder} " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<KafkaClusterInfo> queryClusters(@Param("search") String search,
                                         @Param("clusterType") String clusterType,
                                         @Param("auth") String auth,
                                         @Param("sortField") String sortField,
                                         @Param("sortOrder") String sortOrder,
                                         @Param("offset") Integer offset,
                                         @Param("pageSize") Integer pageSize);

    @Select("<script>" +
            "SELECT COUNT(*) FROM ke_cluster c WHERE 1=1 " +
            "<if test=\"search != null and search != ''\">" +
            "  AND (c.cluster_name LIKE CONCAT('%', #{search}, '%') OR c.cluster_id LIKE CONCAT('%', #{search}, '%')) " +
            "</if>" +
            "<if test=\"clusterType != null and clusterType != ''\">" +
            "  AND c.cluster_type = #{clusterType} " +
            "</if>" +
            "<if test=\"auth != null and auth != ''\">" +
            "  AND c.auth = #{auth} " +
            "</if>" +
            "</script>")
    Long countClusters(@Param("search") String search,
                       @Param("clusterType") String clusterType,
                       @Param("auth") String auth);

    @Select("SELECT id, cluster_id as clusterId, cluster_name as name, cluster_type as clusterType, auth, auth_config as authConfig, availability, created_time as createdAt, updated_time as updatedAt FROM ke_cluster WHERE id = #{id}")
    KafkaClusterInfo getById(@Param("id") Long id);

    @Select("SELECT id, cluster_id as clusterId, cluster_name as name, cluster_type as clusterType, auth, auth_config as authConfig, availability, created_time as createdAt, updated_time as updatedAt FROM ke_cluster WHERE cluster_id = #{clusterId}")
    KafkaClusterInfo findByClusterId(@Param("clusterId") String clusterId);

    @Insert("INSERT INTO ke_cluster (cluster_id, cluster_name, cluster_type, auth, auth_config, availability, created_time, updated_time) " +
            "VALUES (#{clusterId}, #{name}, #{clusterType}, #{auth}, #{authConfig}, #{availability}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCluster(KafkaClusterInfo info);

    @Update("UPDATE ke_cluster SET cluster_name = #{name}, cluster_type = #{clusterType}, auth = #{auth}, auth_config = #{authConfig}, availability = #{availability}, updated_time = NOW() WHERE id = #{id}")
    int updateCluster(KafkaClusterInfo info);

    @Update("UPDATE ke_cluster SET total_nodes = #{nodes}, online_nodes = #{onlineNodes}, availability = #{availability}, updated_time = NOW() WHERE cluster_id = #{clusterId}")
    int updateSummaryByClusterId(@Param("clusterId") String clusterId,
                                 @Param("nodes") Integer nodes,
                                 @Param("onlineNodes") Integer onlineNodes,
                                 @Param("availability") java.math.BigDecimal availability);

    @Delete("DELETE FROM ke_cluster WHERE id = #{id}")
    int deleteCluster(@Param("id") Long id);
}