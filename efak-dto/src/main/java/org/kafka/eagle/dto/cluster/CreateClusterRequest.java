package org.kafka.eagle.dto.cluster;

import lombok.Data;

import java.util.List;

/**
 * <p>
 * 集群创建请求 DTO，用于封装创建新 Kafka 集群的请求参数。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/7/13 10:00:22
 * @version 5.0.0
 */
@Data
public class CreateClusterRequest {
    /** 集群名称 */
    private String name;
    /** 集群类型（环境/部署类型） */
    private String clusterType;
    /** 是否开启安全认证：Y/N */
    private String auth;
    /** 安全认证配置（原始JSON字符串） */
    private String authConfig;
    /** 创建人 */
    private String creator;
    /** 节点信息列表 */
    private List<Node> nodes;

    @Data
    public static class Node {
        private Integer brokerId;
        private String hostIp;
        private Integer port;
        private Integer jmxPort;
    }
}