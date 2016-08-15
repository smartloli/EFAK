package com.smartloli.kafka.eagle.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartloli.kafka.eagle.domain.KafkaBrokerDomain;
import com.smartloli.kafka.eagle.domain.KafkaMetaDomain;

import kafka.javaapi.PartitionMetadata;
import kafka.javaapi.TopicMetadata;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.TopicMetadataResponse;
import kafka.javaapi.consumer.SimpleConsumer;

/**
 * @Date Aug 15, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note As kafka low api to get meta data
 */
public class KafkaMetaUtils {

	private static Logger LOG = LoggerFactory.getLogger(KafkaMetaUtils.class);

	public static List<KafkaMetaDomain> findLeader(String topic) {
		List<KafkaMetaDomain> list = new ArrayList<>();

		SimpleConsumer consumer = null;
		for (KafkaBrokerDomain broker : getBrokers()) {
			try {
				consumer = new SimpleConsumer(broker.getHost(), broker.getPort(), 100000, 64 * 1024, "leaderLookup");
				if (consumer != null) {
					break;
				}
			} catch (Exception ex) {
				LOG.error(ex.getMessage());
			}
		}

		List<String> topics = Collections.singletonList(topic);
		TopicMetadataRequest req = new TopicMetadataRequest(topics);
		TopicMetadataResponse resp = consumer.send(req);

		List<TopicMetadata> metaData = resp.topicsMetadata();
		for (TopicMetadata item : metaData) {
			for (PartitionMetadata part : item.partitionsMetadata()) {
				KafkaMetaDomain kMeta = new KafkaMetaDomain();
				kMeta.setIsr(part.isr().size() == 0 ? -1 : part.isr().get(0).id());
				kMeta.setLeader(part.leader() == null ? -1 : part.leader().id());
				kMeta.setPartitionId(part.partitionId());
				kMeta.setReplicas(part.replicas().size() == 0 ? -1 : part.replicas().get(0).id());
				list.add(kMeta);
			}
		}
		return list;
	}

	private static List<KafkaBrokerDomain> getBrokers() {
		String brokersStr = KafkaClusterUtils.getAllBrokersInfo();
		List<KafkaBrokerDomain> brokers = new ArrayList<KafkaBrokerDomain>();
		JSONArray arr = JSON.parseArray(brokersStr);
		for (Object object : arr) {
			JSONObject obj = (JSONObject) object;
			KafkaBrokerDomain broker = new KafkaBrokerDomain();
			broker.setHost(obj.getString("host"));
			broker.setPort(obj.getInteger("port"));
			brokers.add(broker);
		}
		return brokers;
	}

	public static void main(String[] args) {
		System.out.println(findLeader("test_data2"));
	}

}
