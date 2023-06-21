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
    CREATE_TOPIC_SERVICE_ERROR("服务异常，创建失败！"),
    CREATE_TOPIC_REPLICAS_ERROR("创建的副本数大于可用集群节点数，无法创建！");


    private final String name;

    private ResponseModuleType(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }
}
