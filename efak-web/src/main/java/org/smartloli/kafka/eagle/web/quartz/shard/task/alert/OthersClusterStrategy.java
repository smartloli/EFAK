package org.smartloli.kafka.eagle.web.quartz.shard.task.alert;

import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmClusterInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmConfigInfo;
import org.smartloli.kafka.eagle.common.util.LoggerUtils;
import org.smartloli.kafka.eagle.common.util.NetUtils;
import org.smartloli.kafka.eagle.core.metrics.KafkaMetricsService;
import org.smartloli.kafka.eagle.web.service.impl.AlertServiceImpl;

import java.util.ArrayList;
import java.util.List;

public class OthersClusterStrategy implements ClusterStrategy {

    @Override
    public void execute(AlarmClusterInfo cluster, AlarmConfigInfo alarmConfig, KafkaMetricsService kafkaMetricsService, AlertServiceImpl alertService) {
        String[] servers = cluster.getServer().split(",");
        List<String> errorServers = new ArrayList<String>();
        List<String> normalServers = new ArrayList<String>();
        for (String server : servers) {
            String host = server.split(":")[0];
            int port = 0;
            try {
                port = Integer.parseInt(server.split(":")[1]);
                boolean status = NetUtils.telnet(host, port);
                if (!status) {
                    errorServers.add(server);
                } else {
                    normalServers.add(server);
                }
            } catch (Exception e) {
                LoggerUtils.print(this.getClass()).error("Alarm cluster has error, msg is ", e);
            }
        }
        if (errorServers.size() > 0 && (cluster.getAlarmTimes() < cluster.getAlarmMaxTimes() || cluster.getAlarmMaxTimes() == -1)) {
            cluster.setAlarmTimes(cluster.getAlarmTimes() + 1);
            cluster.setIsNormal("N");
            alertService.modifyClusterStatusAlertById(cluster);
            try {
                ClusterStrategyContext.sendAlarmClusterError(alarmConfig, cluster, errorServers.toString());
            } catch (Exception e) {
                LoggerUtils.print(this.getClass()).error("Send alarm cluster exception has error, msg is ", e);
            }
        } else if (errorServers.size() == 0) {
            if (cluster.getIsNormal().equals("N")) {
                cluster.setIsNormal("Y");
                // clear error alarm and reset
                cluster.setAlarmTimes(0);
                // notify the cancel of the alarm
                alertService.modifyClusterStatusAlertById(cluster);
                try {
                    ClusterStrategyContext.sendAlarmClusterNormal(alarmConfig, cluster, normalServers.toString());
                } catch (Exception e) {
                    LoggerUtils.print(this.getClass()).error("Send alarm cluster normal has error, msg is ", e);
                }
            }
        }
    }

}
