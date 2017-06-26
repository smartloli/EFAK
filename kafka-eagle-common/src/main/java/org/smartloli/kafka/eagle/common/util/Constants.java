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
package org.smartloli.kafka.eagle.common.util;

/**
 * Define constants in the system.
 * 
 * @author smartloli.
 *
 *         Created by Jan 3, 2017
 */
public class Constants {

	/** D3 data plugin size. */
	public interface D3 {
		public final static int SIZE = 40;
	}

	/** Kafka parameter setting. */
	public interface Kafka {
		public final static String CONSUMER_OFFSET_TOPIC = "__consumer_offsets";
		public final static String KAFKA_EAGLE_SYSTEM_GROUP = "kafka.eagle.system.group";
		public final static String JAVA_SECURITY = "java.security.auth.login.config";
		public final static int TIME_OUT = 100;
		public final static long POSITION = 5000;// default 5000
		public final static String PARTITION_CLASS = "partitioner.class";
		public final static String KEY_SERIALIZER = "key.serializer";
		public final static String VALUE_SERIALIZER = "value.serializer";
	}

	/** Mail args setting. */
	public interface Mail {
		public final static String[] ARGS = new String[]{"toAddress", "subject", "content"};
	}

	/** Zookeeper session. */
	public interface SessionAlias {
		public final static String CLUSTER_ALIAS = "clusterAlias";
	}

	/** Login session. */
	public interface Login {
		public static String SESSION_USER = "LOGIN_USER_SESSION";
		public static String UNKNOW_USER = "__unknow__";
		public static String ERROR_LOGIN = "error_msg";
	}

	/** Role Administrator. */
	public interface Role {
		public static String ADMIN = "admin";
	}
}
