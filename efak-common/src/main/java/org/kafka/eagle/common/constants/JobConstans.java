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


    public static final Map<String,String> JOBS = new HashMap<String,String>(){{
        put("org.kafka.eagle.web.quartz.job.ClusterJob","多集群定时任务");
        put("org.kafka.eagle.web.quartz.job.BrokerJob","Kafka节点定时任务");
        put("org.kafka.eagle.web.quartz.job.TopicMetaJob","Topic消息定时任务");
        put("org.kafka.eagle.web.quartz.job.TopicSummaryJob","Topic总览定时任务");
        put("org.kafka.eagle.web.quartz.job.DeleteJob","数据清理定时任务");
        put("org.kafka.eagle.web.quartz.job.ConsumerGroupJob","消费者总览定时任务");
    }};

}
