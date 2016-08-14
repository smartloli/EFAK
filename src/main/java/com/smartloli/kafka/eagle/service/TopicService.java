package com.smartloli.kafka.eagle.service;

import com.smartloli.kafka.eagle.utils.KafkaClusterUtils;

/**
 * @Date Aug 14, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
 */
public class TopicService {
	
	public static String list(){
		return KafkaClusterUtils.getNewPartitionInfo();
	}

}
