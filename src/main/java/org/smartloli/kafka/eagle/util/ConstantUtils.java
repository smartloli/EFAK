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
package org.smartloli.kafka.eagle.util;

/**
 * TODO
 * 
 * @author smartloli.
 *
 *         Created by Jan 3, 2017
 */
public class ConstantUtils {

	public interface D3 {
		public static int SIZE = 50;
	}

	public interface Separator {
		public static String EIGHT = "________";
	}

	public interface Kafka {
		public final static String CONSUMER_OFFSET_TOPIC = "__consumer_offsets";
		public final static int SINGLE_THREAD = 1;
	}

}
