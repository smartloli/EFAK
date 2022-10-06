package org.smartloli.kafka.eagle.kafka;

import com.alibaba.fastjson.JSON;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import sun.rmi.runtime.Log;

import java.util.Arrays;
import java.util.Properties;

/**
 * kafka消息消费者测试
 */
public class KafkaConsumer {
    public static void main(String[] args) throws Exception {
        String topics="test";
        String groupId="group1";
        Properties properties = KafkaHelper.getKafkaConf();
        properties.setProperty("group.id",groupId);
        org.apache.kafka.clients.consumer.KafkaConsumer<String, String> consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(properties);
        consumer.subscribe(Arrays.asList(topics.split(",")));
        System.out.println("topic:" + topics + ",props:" + JSON.toJSONString(properties));
        while (true) {
            Thread.sleep(1000);
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                String value = record.value();
                System.out.println(String.format("offset = %d, partition = %s, value = %s%n", record.offset(),record.partition(), value));
            }
            Thread.sleep(1000);
        }
    }
}