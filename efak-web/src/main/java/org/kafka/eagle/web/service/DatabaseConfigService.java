package org.kafka.eagle.web.service;

import org.kafka.eagle.dto.broker.BrokerInfo;

import java.util.List;

/**
 * <p>
 * 数据库配置读取服务接口
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/09/12 01:07:17
 * @version 5.0.0
 */
public interface DatabaseConfigService {

    /**
     * 从数据库获取Broker信息
     *
     * @return BrokerInfo列表
     */
    List<BrokerInfo> getBrokerInfosFromDatabase();

}