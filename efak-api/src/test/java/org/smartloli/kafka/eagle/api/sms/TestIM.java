/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.api.sms;

import org.smartloli.kafka.eagle.api.im.IMFactory;
import org.smartloli.kafka.eagle.api.im.IMService;
import org.smartloli.kafka.eagle.api.im.IMServiceImpl;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmMessageInfo;

/**
 * TODO
 *
 * @author smartloli.
 * <p>
 * Created by Jan 1, 2019
 */
public class TestIM {
    public static void main(String[] args) {
        testAlarmClusterByDingDingMarkDown();
//		testAlarmClusterByWeChatMarkDown();

    }

    /**
     * New alarm im api.
     */
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
        im.sendPostMsgByWeChat(alarmMsg.toWeChatMarkDown(), "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=7F6VllJEZg3vmJMpUby7EODzLIYO7enPM63upDItIPKj2HQ5scEDtbHX6-Fb2Ruz87qjuiMpTLT0l9gfllXwhN_7gG_4QIhXBsvAx2A9zNwWmGz_v_8VoTHxEM8GK7LMn2_AZValKjB-U7XxIfwrcMXx1Im9wX8rw72gLU_9onCAVKNJVvP5DTDyIPCXAR6B7KuBnaI86mqS8mg3KTT5ug");
    }

    /**
     * New alarm im api.
     */
    private static void testAlarmClusterByDingDingMarkDown() {
        AlarmMessageInfo alarmMsg = new AlarmMessageInfo();
        // FF0000 (red), 008000(green), FFA500(yellow)
        alarmMsg.setTitle("**<font color=\"#FF0000\">Kafka Eagle Alarm Notice</font>** \n\n");
        alarmMsg.setAlarmContent("<font color=\"#FF0000\">node.shutdown [ localhost:9092 ]</font>");
        // alarmMsg.setAlarmContent("<font color=\"#008000\">node.alive [
        // localhost:9092 ]</font>");
        alarmMsg.setAlarmDate("2020-09-18 13:50:00");
        alarmMsg.setAlarmLevel("P0");
        alarmMsg.setAlarmProject("Kafka");
        alarmMsg.setAlarmStatus("<font color=\"#FF0000\">PROBLEM</font>");
        // alarmMsg.setAlarmStatus("<font color=\"#008000\">NORMAL</font>");
        alarmMsg.setAlarmTimes("current(1), max(7)");

        IMService im = new IMFactory().create();
        im.sendPostMsgByDingDing(alarmMsg.toDingDingMarkDown(), "https://oapi.dingtalk.com/robot/send?access_token=3b7b59d17db0145549b1f65f62921b44bacd1701e635e797da45318a94339060");
        //IMServiceImpl im = new IMServiceImpl();
        //im.sendPostMsgByDingDing(alarmMsg.toDingDingMarkDown(),"https://oapi.dingtalk.com/robot/send?access_token=3b7b59d17db0145549b1f65f62921b44bacd1701e635e797da45318a94339060");
    }

}
