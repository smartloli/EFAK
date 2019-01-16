/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.web.quartz;

import java.util.ArrayList;
import java.util.List;

import org.smartloli.kafka.eagle.common.protocol.topic.TopicLagInfo;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.core.factory.KafkaFactory;
import org.smartloli.kafka.eagle.core.factory.KafkaService;
import org.smartloli.kafka.eagle.web.controller.StartupListener;
import org.smartloli.kafka.eagle.web.service.impl.MetricsServiceImpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Collector kafka consumer topic lag trend, metrics broker topic.
 * 
 * @author smartloli.
 *
 *         Created by Jan 15, 2019
 */
public class MetricsQuartz {

	/** Kafka service interface. */
	private KafkaService kafkaService = new KafkaFactory().create();

	public void metricsJobQuartz() {
		List<TopicLagInfo> topicLags = new ArrayList<>();
		String[] clusterAliass = SystemConfigUtils.getPropertyArray("kafka.eagle.zk.cluster.alias", ",");
		for (String clusterAlias : clusterAliass) {
			JSONArray consumerGroups = JSON.parseArray(kafkaService.getKafkaConsumer(clusterAlias));
			for (Object object : consumerGroups) {
				TopicLagInfo topicLag = new TopicLagInfo();
				topicLag.setCluster(clusterAlias);
				JSONObject consumerGroup = (JSONObject) object;
				String group = consumerGroup.getString("group");
				topicLag.setGroup(group);
				JSONObject objectTopicLag = JSON.parseObject(kafkaService.getLag(clusterAlias, group));
				topicLag.setTopic(objectTopicLag.getString("topic"));
				topicLag.setLag(objectTopicLag.getLong("lag"));
				topicLag.setTimespan(CalendarUtils.getTimeSpan());
				topicLag.setTm(CalendarUtils.getCustomDate("yyyyMMdd"));
				topicLags.add(topicLag);
			}
		}
		MetricsServiceImpl metricsServiceImpl = StartupListener.getBean("metricsServiceImpl", MetricsServiceImpl.class);
		metricsServiceImpl.setConsumerLag(topicLags);
	}
}
