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
 * Get the performance index of broker server.
 * 
 * @author smartloli.
 *
 *         Created by Sep 27, 2018
 */
public class JmxConstants {

	private static final String KAFKA_COMMON_VALUE = "Value";

	public enum KafkaLog {
		SIZE("kafka.log:type=Log,name=Size,topic=%s,partition=%s"),
		VALUE(KAFKA_COMMON_VALUE);
		private String value;

		private KafkaLog(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	public enum KafkaServer8 {
		VERSION("kafka.common:type=AppInfo,name=Version"),
		VALUE(KAFKA_COMMON_VALUE),
		END_LOG_SIZE("kafka.log:type=Log,name=LogEndOffset,topic=%s,partition=%s"),
		START_LOG_SIZE("kafka.log:type=Log,name=LogStartOffset,topic=%s,partition=%s");
		private String value;

		public String getValue() {
			return value;
		}

		private KafkaServer8(String value) {
			this.value = value;
		}
	}

	public enum BrokerServer {
		BYTES_IN_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec"),
		BYTES_OUT_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec"),
		BYTES_REJECTED_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=BytesRejectedPerSec"),
		FAILED_FETCH_REQUESTS_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=FailedFetchRequestsPerSec"),
		FAILED_PRODUCE_REQUESTS_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=FailedProduceRequestsPerSec"),
		MESSAGES_IN_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec"),
		PRODUCE_MESSAGE_CONVERSIONS_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=ProduceMessageConversionsPerSec"),
		REPLICATION_BYTES_IN_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=ReplicationBytesInPerSec"),
		REPLICATION_BYTES_OUT_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=ReplicationBytesOutPerSec"),
		TOTAL_FETCH_REQUESTS_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=TotalFetchRequestsPerSec"),
		TOTAL_PRODUCE_REQUESTS_PER_SEC("kafka.server:type=BrokerTopicMetrics,name=TotalProduceRequestsPerSec"),
		BYTES_IN_PER_SEC_TOPIC("kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec,topic=%s"),
		BYTES_OUT_PER_SEC_TOPIC("kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec,topic=%s"),
		JMX_PERFORMANCE_TYPE("java.lang:type=OperatingSystem"),
		TOTAL_PHYSICAL_MEMORY_SIZE("TotalPhysicalMemorySize"),
		FREE_PHYSICAL_MEMORY_SIZE("FreePhysicalMemorySize"),
		BROKER_VERSION("kafka.server:type=app-info,id=%s"),
		BROKER_VERSION_VALUE("Version");

		private String value;

		public String getValue() {
			return value;
		}

		private BrokerServer(String value) {
			this.value = value;
		}
	}

}
