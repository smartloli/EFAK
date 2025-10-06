package org.kafka.eagle.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.broker.BrokerInfo;
import org.kafka.eagle.web.mapper.BrokerMapper;
import org.kafka.eagle.web.service.DatabaseConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 数据库配置读取服务实现类
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/12 01:07:23
 * @version 5.0.0
 */
@Slf4j
@Service
public class DatabaseConfigServiceImpl implements DatabaseConfigService {

    @Autowired
    private BrokerMapper brokerMapper;

    @Override
    public List<BrokerInfo> getBrokerInfosFromDatabase() {
        try {
            List<BrokerInfo> brokerInfos = brokerMapper.queryAllBrokers();
            if (brokerInfos == null || brokerInfos.isEmpty()) {
                log.warn("No broker information found in database");
            } else {
                log.info("Found {} broker(s) in database", brokerInfos.size());
            }
            return brokerInfos;
        } catch (Exception e) {
            log.error("Failed to get broker information from database", e);
            return null;
        }
    }

}