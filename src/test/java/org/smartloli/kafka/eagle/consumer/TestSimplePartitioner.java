package org.smartloli.kafka.eagle.consumer;

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;

/**
 * @Date Mar 9, 2016
 *
 * @Author dengjie
 *
 * @Note TODO
 */
public class TestSimplePartitioner implements Partitioner {

	public TestSimplePartitioner(VerifiableProperties props) {
	}

	public int partition(Object key, int numPartitions) {
		int partition = 0;
		String k = (String) key;
		partition = Math.abs(k.hashCode()) % numPartitions;
		return partition;
	}

}
