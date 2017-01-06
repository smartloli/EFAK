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
package org.smartloli.kafka.eagle.test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * TODO
 * 
 * @author smartloli.
 *
 *         Created by Jan 4, 2017
 */
public class ObjectTest {

	public static void main(String[] args) throws UnknownHostException {
		// Mill => 1483670359006
		// GetMill => 1483670352215
		// GetMill => 1483670351214
		// Mill => 1483670358005
		// GetMill => 1483670350212
		// Mill => 1483670358004
		UUID uuid = UUID.randomUUID();
		String consumerUuid = String.format("%s-%d-%s", InetAddress.getLocalHost().getHostName(), System.currentTimeMillis(), (uuid.getMostSignificantBits() + "").substring(0, 8));
		System.out.println("Mill => " + consumerUuid);
		// System.out.println("GetMill =>
		// "+CalendarUtils.timeSpan2StrDate(1483670351214L));
		// System.out.println(ConsumerService.getConsumer("kafka"));
	}

}
