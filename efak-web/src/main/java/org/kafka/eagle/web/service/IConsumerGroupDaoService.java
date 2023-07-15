package org.kafka.eagle.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.kafka.eagle.pojo.consumer.ConsumerGroupInfo;

import java.util.List;
import java.util.Map;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/7/16 00:32
 * @Version: 3.4.0
 */
public interface IConsumerGroupDaoService extends IService<ConsumerGroupInfo> {

    ConsumerGroupInfo consumerById(Long id);
    ConsumerGroupInfo consumerByObject(ConsumerGroupInfo consumerGroupInfo);

    Integer totalOfConsumerGroups(ConsumerGroupInfo consumerGroupInfo);

    boolean insert(ConsumerGroupInfo consumerGroupInfo);

    /**
     * Page limit.
     * @param params
     * @return
     */
    Page<ConsumerGroupInfo> pages(Map<String,Object> params);

    boolean batch(List<ConsumerGroupInfo> createInfos);

    boolean delete(Long id);

}
