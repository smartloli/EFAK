package org.smartloli.kafka.eagle.ipc;

import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Date;
import java.util.Properties;

/**
 * TODO
 *
 * @author smartloli.
 * <p>
 * Created by Sep 11, 2020
 */
public class TestSampleProducer extends Thread {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "127.0.0.1:9092");
        props.put("acks", "1");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("partitioner.class", "org.smartloli.kafka.eagle.ipc.TestSimplePartitioner");

        Producer<String, String> producer = new KafkaProducer<>(props);
        long id = 0;
        while (true) {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("time", new Date().getTime());
            id++;
            producer.send(new ProducerRecord<String, String>("ke0920", "k_" + new Date().getTime(), json.toJSONString()));
            try {
                sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // producer.close();
    }

}
