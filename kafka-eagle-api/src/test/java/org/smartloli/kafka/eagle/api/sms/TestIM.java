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
package org.smartloli.kafka.eagle.api.sms;

import org.smartloli.kafka.eagle.api.email.module.ClusterContentModule;
import org.smartloli.kafka.eagle.api.email.module.LagContentModule;
import org.smartloli.kafka.eagle.api.im.IMServiceImpl;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;

/**
 * TODO
 * 
 * @author smartloli.
 *
 *         Created by Jan 1, 2019
 */
public class TestIM {
	public static void main(String[] args) {
		testClusterHeathyByDingDing();
		testConsumerHeathyByWeChat();
	}

	private static void testConsumerHeathyByWeChat() {
		ClusterContentModule ccm = new ClusterContentModule();
		ccm.setCluster("cluster2");
		ccm.setServer("127.0.0.1:9093");
		ccm.setTime(CalendarUtils.getDate());
		ccm.setType("Zookeeper");
		ccm.setUser("smartloli.org@gmail.com");
		
		LagContentModule lcm = new LagContentModule();
		lcm.setCluster("cluster2");
		lcm.setConsumerLag("5000");
		lcm.setGroup("ke_storm_group");
		lcm.setLagThreshold("2000");
		lcm.setTime(CalendarUtils.getDate());
		lcm.setTopic("ke_t_storm_money");
		lcm.setType("Consumer");
		lcm.setUser("smartloli.org@gmail.com");
		
		IMServiceImpl im = new IMServiceImpl();
		im.sendJsonMsgByWeChat(lcm.toWeChatMarkDown());
	}

	private static void testClusterHeathyByDingDing() {
		ClusterContentModule ccm = new ClusterContentModule();
		ccm.setCluster("cluster2");
		ccm.setServer("127.0.0.1:9093");
		ccm.setTime(CalendarUtils.getDate());
		ccm.setType("Zookeeper");
		ccm.setUser("smartloli.org@gmail.com");

		LagContentModule lcm = new LagContentModule();
		lcm.setCluster("cluster2");
		lcm.setConsumerLag("5000");
		lcm.setGroup("ke_storm_group");
		lcm.setLagThreshold("2000");
		lcm.setTime(CalendarUtils.getDate());
		lcm.setTopic("ke_t_storm_money");
		lcm.setType("Consumer");
		lcm.setUser("smartloli.org@gmail.com");

		IMServiceImpl im = new IMServiceImpl();
		im.sendJsonMsgByDingDing(ccm.toDingDingMarkDown());
	}
}
