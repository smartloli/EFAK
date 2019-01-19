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
package org.smartloli.kafka.eagle.common.constant;

/**
 * Collector kafka jmx constants vars.
 * 
 * @author smartloli.
 *
 *         Created by Sep 27, 2018
 */
public class JmxConstants {

	public interface KafkaLog {
		public static String size = "kafka.log:type=Log,name=Size,topic=%s,partition=%s";
		public static String value = "Value";
	}

	public interface KafkaNetWork {
		// TODO
	}

	public interface KafkaServer8 {
		public static final String version = "kafka.common:type=AppInfo,name=Version";
		public static final String value = "Value";
		public static final String logSize = "kafka.log:type=Log,name=LogEndOffset,topic=%s,partition=%s";
	}

	public interface KafkaServer {
		class BrokerTopicMetrics {
			public static String bytesInPerSec = "kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec";
			public static String bytesOutPerSec = "kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec";
			public static String bytesRejectedPerSec = "kafka.server:type=BrokerTopicMetrics,name=BytesRejectedPerSec";
			public static String failedFetchRequestsPerSec = "kafka.server:type=BrokerTopicMetrics,name=FailedFetchRequestsPerSec";
			public static String failedProduceRequestsPerSec = "kafka.server:type=BrokerTopicMetrics,name=FailedProduceRequestsPerSec";
			public static String messagesInPerSec = "kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec";
			public static String produceMessageConversionsPerSec = "kafka.server:type=BrokerTopicMetrics,name=ProduceMessageConversionsPerSec";
			public static String replicationBytesInPerSec = "kafka.server:type=BrokerTopicMetrics,name=ReplicationBytesInPerSec";
			public static String replicationBytesOutPerSec = "kafka.server:type=BrokerTopicMetrics,name=ReplicationBytesOutPerSec";
			public static String totalFetchRequestsPerSec = "kafka.server:type=BrokerTopicMetrics,name=TotalFetchRequestsPerSec";
			public static String totalProduceRequestsPerSec = "kafka.server:type=BrokerTopicMetrics,name=TotalProduceRequestsPerSec";
		}

		class Topic {
			public static String bytesInPerSecTopic = "kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec,topic=%s";
			public static String bytesOutPerSecTopic = "kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec,topic=%s";
		}

		class ClusterBusyMetrics {
			// if value < 0.3, we need alert
			public static String requestHandlerAvgIdlePercent = "kafka.server:type=KafkaRequestHandlerPool,name=RequestHandlerAvgIdlePercent";
			// if value < 0.3, we need alert
			public static String networkProcessorAvgIdlePercent = "kafka.network:type=SocketServer,name=NetworkProcessorAvgIdlePercent";
			// if value > 0, we need alert
			public static String underReplicatedPartitions = "kafka.server:type=ReplicaManager,name=UnderReplicatedPartitions";
			// if value > 0, we need alert
			public static String offlinePartitionsCount = "kafka.controller:type=KafkaController,name=OfflinePartitionsCount";
			// if value != 1,we need alert
			public static String activeControllerCount = "kafka.controller:type=KafkaController,name=ActiveControllerCount";
		}

		class ZookeeperClientMetrics {
			public static String zooKeeperRequestLatencyMs = "kafka.server:type=ZooKeeperClientMetrics,name=ZooKeeperRequestLatencyMs";
		}

		public static final String version = "kafka.server:type=app-info,id=%s";
		public static final String value = "Version";
	}

	public interface Hosts {
		public static String load = "load";
		public static String cpu = "cpu";
		public static String free = "free";
		public static String used = "used";
		public static String disk = "disk";
		public static String network = "network";
		public static String tcp = "tcp";
		public static String openfile = "openfile";
		public static String inode = "inode";
	}

}
