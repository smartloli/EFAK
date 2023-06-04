package org.kafka.eagle.common.constants;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/6/4 23:11
 * @Version: 3.4.0
 */
public enum EfakClusterType {


    CLUSTER("cluster"),
    BROKER("broker"),

    NEW_CREATE("newCreate"),
    OLD_CREATE("oldCreate");


    private final String name;

    private EfakClusterType(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

}
