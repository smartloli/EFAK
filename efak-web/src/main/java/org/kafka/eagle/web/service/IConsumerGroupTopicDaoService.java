package org.kafka.eagle.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.kafka.eagle.pojo.consumer.ConsumerGroupTopicInfo;

import java.util.List;
import java.util.Map;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/7/26 22:32
 * @Version: 3.4.0
 */
public interface IConsumerGroupTopicDaoService extends IService<ConsumerGroupTopicInfo> {

    List<ConsumerGroupTopicInfo> consumerGroupTopicList(String clusterId);

    ConsumerGroupTopicInfo consumerGroupTopic(ConsumerGroupTopicInfo consumerGroupTopicInfo);

    Boolean consumerGroupTopic(String clusterId, String groupId, String topicName);

    boolean insert(ConsumerGroupTopicInfo consumerGroupTopicInfo);

    boolean update(ConsumerGroupTopicInfo consumerGroupTopicInfo);

    boolean batch(List<ConsumerGroupTopicInfo> consumerGroupTopicInfos);

    boolean delete(Long id);

    boolean delete(List<Long> consumerGroupIds);

    /**
     * Page limit.
     * @param params
     * @return
     */
    List<ConsumerGroupTopicInfo> pages(Map<String,Object> params);

    /**
     * get consumer and producer rate
     *
     * @return
     */
    ConsumerGroupTopicInfo consumersOfLatest(String clusterId, String group, String topic);

}
