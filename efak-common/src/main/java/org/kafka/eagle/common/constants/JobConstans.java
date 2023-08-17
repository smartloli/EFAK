package org.kafka.eagle.common.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/7/15 10:51
 * @Version: 3.4.0
 */
public class JobConstans {

    public static final String UNKOWN = "未知任务";
    public static final String DELETE = "delete";
    public static final String PAUSE = "pause";
    public static final String RESUME = "resume";


    public static final Map<String, String> JOBS = new HashMap<String, String>() {{
        put("org.kafka.eagle.web.quartz.job.ClusterJob", "多集群定时任务");
        put("org.kafka.eagle.web.quartz.job.BrokerJob", "Kafka节点定时任务");
        put("org.kafka.eagle.web.quartz.job.TopicMetaJob", "Topic消息定时任务");
        put("org.kafka.eagle.web.quartz.job.TopicSummaryJob", "Topic总览定时任务");
        put("org.kafka.eagle.web.quartz.job.DeleteJob", "数据清理定时任务");
        put("org.kafka.eagle.web.quartz.job.ConsumerGroupJob", "消费者总览定时任务");
        put("org.kafka.eagle.web.quartz.job.ConsumerGroupTopicJob", "消费者主题明细定时任务");
        put("org.kafka.eagle.web.quartz.job.KafkaMetricJob", "Kafka集群性能指标定时任务");
        put("org.kafka.eagle.web.quartz.job.TopicLogSizeRankJob", "Topic消息记录正排定时任务");
        put("org.kafka.eagle.web.quartz.job.TopicCapacityRankJob", "Topic容量大小正排定时任务");
        put("org.kafka.eagle.web.quartz.job.TopicByteInRankJob", "Topic消息写入正排定时任务");
        put("org.kafka.eagle.web.quartz.job.TopicByteOutRankJob", "Topic消息读取正排定时任务");
        put("org.kafka.eagle.web.quartz.job.MockJob", "测试生产者应用定时任务"); // if release version, this feature must be remove
    }};

}
