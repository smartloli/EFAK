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

import org.smartloli.kafka.eagle.api.email.MailFactory;
import org.smartloli.kafka.eagle.api.email.MailProvider;
import org.smartloli.kafka.eagle.api.email.module.ClusterContentModule;
import org.smartloli.kafka.eagle.common.util.CalendarUtils;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;
import org.smartloli.kafka.eagle.common.util.KConstants.WeChat;

/**
 * Test mail interface.
 * 
 * @author smartloli.
 *
 *         Created by Oct 31, 2017
 */
public class TestMailFactory {

	public static void main(String[] args) {
		
		System.out.println(SystemConfigUtils.getLongProperty("kafka.eagle.im.wechat.agentid",WeChat.AGENTID));
		
	}

	public static void test() {
		MailProvider provider = new MailFactory();
		String subject = "Kafka Eagle Consumer Alert";
		//String[] address = new String[] { "smartloli.org@gmail.com", "810371213@qq.com" };
		
		String addr = "smartloli.org@gmail.com,810371213@qq.com";
		
		ClusterContentModule cluster = new ClusterContentModule();
		cluster.setUser(addr);
		cluster.setCluster("cluster1");
		cluster.setServer("127.0.0.1:9092");
		cluster.setType("Kafka");
		cluster.setTime(CalendarUtils.getDate());
		provider.create().send(subject, addr, cluster.toString(), "");
		
//		for (String addr : address) {
//			// String content = "Hi " + addr.split("@")[0] + " , <br/>Group Name
//			// is
//			// [Test],Topic is [ke_test2],current lag is [15000],expired lag is
//			// [10000].";
//			// LagContentModule lag = new LagContentModule();
//			// lag.setUser(addr.split("@")[0]);
//			// lag.setCluster("cluseter2");
//			// lag.setGroup("ke_group");
//			// lag.setTopic("ke_topic");
//			// lag.setConsumerLag("3200");
//			// lag.setLagThreshold("2000");
//			// lag.setType("Consumer");
//			// lag.setTime(CalendarUtils.getDate());
//
//			ClusterContentModule cluster = new ClusterContentModule();
//			cluster.setUser(addr.split("@")[0]);
//			cluster.setCluster("cluster1");
//			cluster.setServer("127.0.0.1:9092");
//			cluster.setType("Kafka");
//			cluster.setTime(CalendarUtils.getDate());
//			provider.create().send(subject, addr, cluster.toString(), "");
//		}
	}

}
