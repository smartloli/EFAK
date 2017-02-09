package org.smartloli.kafka.eagle.consumer;

import java.util.Properties;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

/**
 * @Date Mar 9, 2016
 *
 * @Author dengjie
 *
 * @Note TODO
 */
public class TestPartitionerProducer {

	private static JSONArray dataSets = new JSONArray();
	static {
		for (int i = 0; i < 10; i++) {
			JSONObject dataset = new JSONObject();
			dataset.put("_plat", "100" + i);
			dataset.put("_uid", "53622890" + i);
			dataset.put("_tm", 1461309791);
			dataset.put("ip", "1.186.76.34");
			dataSets.add(dataset);
		}
	}

	public static void main(String[] args) {
		sender();
	}

	private static void sender() {
		Properties props = new Properties();
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		props.put("metadata.broker.list", "slave01:9094,slave01:9095");
		props.put("partitioner.class", "org.smartloli.kafka.eagle.consumer.TestSimplePartitioner");
		Producer<String, String> producer = new Producer<String, String>(new ProducerConfig(props));
		String topic = "kv2_topic";
		int i = 0;
		for (Object object : dataSets) {
			String k = "key" + i;
			JSONObject v = (JSONObject) object;
			System.out.println(v.toJSONString());
			producer.send(new KeyedMessage<String, String>(topic, k, v.toJSONString()));
			i++;
		}
		producer.close();
	}
}
