package org.kafka.eagle.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.kafka.eagle.pojo.cluster.BrokerInfo;

import java.util.List;
import java.util.Map;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/5/28 00:37
 * @Version: 3.4.0
 */
public interface IBrokerDaoService extends IService<BrokerInfo> {

    List<BrokerInfo> list();

    /**
     * Get all cluster info.
     * @return
     */
    List<BrokerInfo> clusters(String clusterId);

    /**
     * Get cluster info by cluster name.
     * @param clusterInfo
     * @return
     */
    boolean insert(BrokerInfo clusterInfo);

    /**
     * Page limit.
     * @param params
     * @return
     */
    Page<BrokerInfo> pages(Map<String,Object> params);

    /**
     * Update cluster info by clusterId.
     * @param clusterInfo
     * @return
     */
    boolean update(BrokerInfo clusterInfo);

    boolean update(List<BrokerInfo> brokerInfos);

    boolean delete(BrokerInfo clusterInfo);

    boolean batch(List<BrokerInfo> clusterInfos);

}
