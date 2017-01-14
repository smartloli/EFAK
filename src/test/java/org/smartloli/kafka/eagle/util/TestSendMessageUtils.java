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

import java.util.HashSet;
import java.util.Set;

/**
 * Test Mail server whether send message success.
 * 
 * @author smartloli.
 *
 *         Created by Jan 14, 2017
 */
public class TestSendMessageUtils {
	public static void main(String[] args) {
		String subject = "Alarm Lag";
		String content = "Lag exceeds a specified threshold : Msg is somethings test...";
		Set<String> set = new HashSet<String>();
		set.add("smartdengjie@gmail.com");
		int count = 0;
		for (String sender : set) {
			SendMessageUtils.send(sender, subject, content);
			System.out.println("sender count[" + (++count) + "]");
		}
	}
}
