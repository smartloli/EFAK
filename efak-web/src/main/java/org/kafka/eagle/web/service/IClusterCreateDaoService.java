package org.kafka.eagle.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.kafka.eagle.pojo.cluster.ClusterCreateInfo;

import java.util.List;
import java.util.Map;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/5/28 00:37
 * @Version: 3.4.0
 */
public interface IClusterCreateDaoService extends IService<ClusterCreateInfo> {

    /**
     * Get all cluster info.
     * @return
     */
    List<ClusterCreateInfo> clusters();

    /**
     * Get cluster info by cluster name.
     * @param clusterInfo
     * @return
     */
    boolean insert(ClusterCreateInfo clusterInfo);

    /**
     * Page limit.
     * @param params
     * @return
     */
    Page<ClusterCreateInfo> pages(Map<String,Object> params);

    /**
     * Update cluster info by clusterId.
     * @param clusterInfo
     * @return
     */
    boolean update(ClusterCreateInfo clusterInfo);

    boolean delete(String clusterId);

    boolean batch(List<ClusterCreateInfo> createInfos);

}
