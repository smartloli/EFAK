package org.kafka.eagle.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.kafka.eagle.pojo.cluster.ClusterInfo;

import java.util.List;
import java.util.Map;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/5/28 00:37
 * @Version: 3.4.0
 */
public interface IClusterDaoService extends IService<ClusterInfo> {

    /**
     * Get all cluster info.
     * @return
     */
    List<ClusterInfo> clusters();

    /**
     * Get cluster info by cluster name.
     * @param clusterInfo
     * @return
     */
    boolean insert(ClusterInfo clusterInfo);

    /**
     * Page limit.
     * @param params
     * @return
     */
    Page<ClusterInfo> pages(Map<String,Object> params);

    /**
     * Update cluster info by clusterId.
     * @param clusterInfo
     * @return
     */
    boolean update(ClusterInfo clusterInfo);

}
