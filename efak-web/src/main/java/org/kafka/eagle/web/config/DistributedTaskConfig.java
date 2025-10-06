package org.kafka.eagle.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * 分布式任务调度配置类
 * 配置分布式环境下的任务调度相关参数，包括节点超时、分片结果等待时间等
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/28 21:40:07
 * @version 5.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "efak.distributed.task")
public class DistributedTaskConfig {

    /**
     * 节点离线超时时间（秒）
     */
    private int offlineTimeout = 120;

    /**
     * 分片结果等待时间（秒）
     */
    private int shardResultWaitTime = 30;

    /**
     * 分片结果过期时间（分钟）
     */
    private int shardResultExpireMinutes = 10;
}