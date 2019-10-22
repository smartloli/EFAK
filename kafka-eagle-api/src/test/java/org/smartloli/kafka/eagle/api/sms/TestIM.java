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
import org.smartloli.kafka.eagle.api.im.IMFactory;
import org.smartloli.kafka.eagle.api.im.IMService;
import org.smartloli.kafka.eagle.api.im.IMServiceImpl;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmMessageInfo;
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
		testAlarmClusterByDingDingMarkDown();
		//testAlarmClusterByWeChatMarkDown();
		//testConsumerHeathyByWeChat();
		
	}
	
	/** New alarm im api. */
	private static void testAlarmClusterByWeChatMarkDown() {
		AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
		// FF0000 (red), 008000(green), FFA500(yellow)
		alarmMsg.setTitle("`Kafka Eagle Alarm Notice`\n");
		alarmMsg.setAlarmContent("<font color=\"warning\">node.shutdown [ localhost:9092 ]</font>");
		// alarmMsg.setAlarmContent("<font color=\"#008000\">node.alive [
		// localhost:9092 ]</font>");
		alarmMsg.setAlarmDate("2019-10-07 21:43:22");
		alarmMsg.setAlarmLevel("P0");
		alarmMsg.setAlarmProject("Kafka");
		alarmMsg.setAlarmStatus("<font color=\"warning\">PROBLEM</font>");
		// alarmMsg.setAlarmStatus("<font color=\"#008000\">NORMAL</font>");
		alarmMsg.setAlarmTimes("current(1), max(7)");

		 IMServiceImpl im = new IMServiceImpl();
		 im.sendJsonMsgByWeChat(alarmMsg.toWeChatMarkDown(),"https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=maMN2krp0GwiyxoA6JCULLk8oLHwfnjnojeYGma_5KG5J_JHqTledeY6AHWE2rwLTF6I5yqu5LJUmUpSn7feXauFySZtnOGlAvRACz33V2UegR596xuyOT4fZIfNzB1cqJi3A-Eahbw7UVG2a8AaHvN0ZrSRPkQiqWola5p71FfCpnuDEAw63THmURdfMIcF3QB5KFzl-qHblqXfQLtpeA");
	}

	/** New alarm im api. */
	private static void testAlarmClusterByDingDingMarkDown() {
		AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
		// FF0000 (red), 008000(green), FFA500(yellow)
		alarmMsg.setTitle("**<font color=\"#FF0000\">Kafka Eagle Alarm Notice</font>** \n\n");
		alarmMsg.setAlarmContent("<font color=\"#FF0000\">node.shutdown [ localhost:9092 ]</font>");
		// alarmMsg.setAlarmContent("<font color=\"#008000\">node.alive [
		// localhost:9092 ]</font>");
		alarmMsg.setAlarmDate("2019-10-07 21:43:22");
		alarmMsg.setAlarmLevel("P0");
		alarmMsg.setAlarmProject("Kafka");
		alarmMsg.setAlarmStatus("<font color=\"#FF0000\">PROBLEM</font>");
		// alarmMsg.setAlarmStatus("<font color=\"#008000\">NORMAL</font>");
		alarmMsg.setAlarmTimes("current(1), max(7)");

		IMService im = new IMFactory().create();
		im.sendPostMsgByDingDing(alarmMsg.toDingDingMarkDown(),"https://oapi.dingtalk.com/robot/send?access_token=3b7b59d17db0145549b1f65f62921b44bacd1701e635e797da45318a94339060");
		//IMServiceImpl im = new IMServiceImpl();
		//im.sendPostMsgByDingDing(alarmMsg.toDingDingMarkDown(),"https://oapi.dingtalk.com/robot/send?access_token=3b7b59d17db0145549b1f65f62921b44bacd1701e635e797da45318a94339060");
	}

	private static void testConsumerHeathyByWeChat() {
		ClusterContentModule ccm = new ClusterContentModule();
		ccm.setCluster("cluster2");
		ccm.setServer("kafka-node-01:9093");
		ccm.setTime(CalendarUtils.getDate());
		ccm.setType("Kafka");
		ccm.setUser("smartloli.org@gmail.com");

		LagContentModule lcm = new LagContentModule();
		lcm.setCluster("cluster2");
		lcm.setConsumerLag("50000");
		lcm.setGroup("ke-storm-group");
		lcm.setLagThreshold("2000");
		lcm.setTime(CalendarUtils.getDate());
		lcm.setTopic("ke-t-storm-money");
		lcm.setType("Consumer");
		lcm.setUser("smartloli.org@gmail.com");

		IMServiceImpl im = new IMServiceImpl();
		im.sendJsonMsgByWeChat(ccm.toWeChatMarkDown(),"https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=maMN2krp0GwiyxoA6JCULLk8oLHwfnjnojeYGma_5KG5J_JHqTledeY6AHWE2rwLTF6I5yqu5LJUmUpSn7feXauFySZtnOGlAvRACz33V2UegR596xuyOT4fZIfNzB1cqJi3A-Eahbw7UVG2a8AaHvN0ZrSRPkQiqWola5p71FfCpnuDEAw63THmURdfMIcF3QB5KFzl-qHblqXfQLtpeA");
		im.sendJsonMsgByWeChat(lcm.toWeChatMarkDown(),"https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=maMN2krp0GwiyxoA6JCULLk8oLHwfnjnojeYGma_5KG5J_JHqTledeY6AHWE2rwLTF6I5yqu5LJUmUpSn7feXauFySZtnOGlAvRACz33V2UegR596xuyOT4fZIfNzB1cqJi3A-Eahbw7UVG2a8AaHvN0ZrSRPkQiqWola5p71FfCpnuDEAw63THmURdfMIcF3QB5KFzl-qHblqXfQLtpeA");
	}

	private static void testClusterHeathyByDingDing() {
		ClusterContentModule ccm = new ClusterContentModule();
		ccm.setCluster("cluster2");
		ccm.setServer("zookeeper-node-01:2183");
		ccm.setTime(CalendarUtils.getDate());
		ccm.setType("Zookeeper");
		ccm.setUser("smartloli.org@gmail.com");

		LagContentModule lcm = new LagContentModule();
		lcm.setCluster("cluster2");
		lcm.setConsumerLag("50000");
		lcm.setGroup("ke-storm-group");
		lcm.setLagThreshold("2000");
		lcm.setTime(CalendarUtils.getDate());
		lcm.setTopic("ke-t-storm-money");
		lcm.setType("Consumer");
		lcm.setUser("smartloli.org@gmail.com");

		IMServiceImpl im = new IMServiceImpl();
		// im.sendJsonMsgByDingDing(ccm.toDingDingMarkDown());
		// im.sendJsonMsgByDingDing(lcm.toDingDingMarkDown());
	}
}
