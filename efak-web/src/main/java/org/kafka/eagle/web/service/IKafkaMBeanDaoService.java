package org.kafka.eagle.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.kafka.eagle.pojo.cluster.KafkaMBeanInfo;

import java.util.List;
import java.util.Map;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/8/5 21:32
 * @Version: 3.4.0
 */
public interface IKafkaMBeanDaoService extends IService<KafkaMBeanInfo> {

    List<KafkaMBeanInfo> kafkaMBeanList(String clusterId);

    KafkaMBeanInfo kafkaMBean(KafkaMBeanInfo kafkaMetricInfo);

    boolean insert(KafkaMBeanInfo kafkaMetricInfo);

    boolean update(KafkaMBeanInfo kafkaMetricInfo);

    boolean batch(List<KafkaMBeanInfo> kafkaMetricInfos);

    boolean delete(Long id);

    boolean delete(List<Long> kafkaMBeanIds);

    /**
     * Page limit.
     * @param params
     * @return
     */
    List<KafkaMBeanInfo> pages(Map<String,Object> params);

}
