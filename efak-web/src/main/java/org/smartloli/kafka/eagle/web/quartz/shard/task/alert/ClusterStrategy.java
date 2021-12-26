package org.smartloli.kafka.eagle.web.quartz.shard.task.alert;

import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmClusterInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmConfigInfo;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsService;
import org.smartloli.kafka.eagle.web.service.impl.AlertServiceImpl;

public interface ClusterStrategy {

    void execute(AlarmClusterInfo cluster, AlarmConfigInfo alarmConfig, KafkaMetricsService kafkaMetricsService, AlertServiceImpl alertService);

}
