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

    ConsumerGroupInfo consumerGroups(Long id);

    List<ConsumerGroupInfo> consumerGroups(String clusterId);

    List<ConsumerGroupInfo> consumerGroupList(String clusterId);

    ConsumerGroupInfo consumerGroups(ConsumerGroupInfo consumerGroupInfo);

    List<ConsumerGroupInfo> consumerGroups(String clusterId,String groupId);

    Boolean consumerGroups(String clusterId, String groupId, String topicName);

    Long totalOfConsumerGroups(ConsumerGroupInfo consumerGroupInfo);
    Long totalOfConsumerGroupTopics(String clusterId, String groupId);

    boolean insert(ConsumerGroupInfo consumerGroupInfo);

    /**
     * Page limit.
     * @param params
     * @return
     */
    Page<ConsumerGroupInfo> pages(Map<String,Object> params);

    boolean update(ConsumerGroupInfo consumerGroupInfo);

    boolean batch(List<ConsumerGroupInfo> consumerGroupInfos);

    boolean delete(Long id);

    boolean delete(List<Long> consumerGroupIds);

}
