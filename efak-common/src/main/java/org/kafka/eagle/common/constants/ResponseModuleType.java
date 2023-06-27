package org.kafka.eagle.common.constants;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/6/21 22:10
 * @Version: 3.4.0
 */
public enum ResponseModuleType {

    CREATE_TOPIC_NOBROKERS_ERROR("集群节点不可用，无法创建！"),
    CREATE_TOPIC_DEL_NOBROKERS_ERROR("集群节点不可用，无法删除！"),
    CREATE_PARTITION_NOBROKERS_ERROR("集群节点不可用，无法增加分区！"),
    CREATE_TOPIC_SERVICE_ERROR("服务异常，创建失败！"),
    CREATE_TOPIC_DEL_SERVICE_ERROR("服务异常，删除失败！"),
    CREATE_PARTITION_SERVICE_ERROR("服务异常，增加分区失败！"),
    CREATE_TOPIC_REPLICAS_ERROR("创建的副本数大于可用集群节点数，无法创建！"),
    ADD_TOPIC_NOBROKERS_ERROR("集群节点不可用，无法完成发送！"),
    ADD_TOPCI_RECORD_SERVICE_ERROR("服务异常，发送内容失败！"),
    GET_TOPIC_RECORD_NOBROKERS_ERROR("集群节点不可用，无法获取主题数据！");


    private final String name;

    private ResponseModuleType(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }
}
