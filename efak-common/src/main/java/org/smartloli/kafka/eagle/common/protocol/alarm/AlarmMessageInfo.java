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
package org.smartloli.kafka.eagle.common.protocol.alarm;

import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.common.protocol.BaseProtocol;
import org.smartloli.kafka.eagle.common.util.KConstants;

/**
 * Alarm message info.
 *
 * @author smartloli.
 * <p>
 * Created by Oct 7, 2019
 */
public class AlarmMessageInfo extends BaseProtocol {

    /**
     * AlarmID : [ 1 ]
     * <p>
     * AlarmCluster : [ Cluster1 ]
     * <p>
     * AlarmStatus : [ PROBLEM ]
     * <p>
     * AlarmLevel : [ P0 ]
     * <p>
     * AlarmProject : [ Kafka ]
     * <p>
     * AlarmTimes : [ current(1), max(7) ]
     * <p>
     * AlarmDate : [ 2019-10-07 21:43:22 ]
     * <p>
     * AlarmContent : [ node.shutdown [ localhost:9092 ] ]
     */

    private String title;
    private String alarmStatus;
    private String alarmProject;
    private String alarmLevel;
    private String alarmTimes;
    private String alarmContent;
    private String alarmDate;
    private int alarmId;
    private String alarmCluster;

    public String toDingDingMarkDown() {
        String titleType = "";
        String color = "";
        if (KConstants.AlarmStatus.NORMAL.equals(alarmStatus)) {
            titleType = "Notice";
            color = "#2a9d8f";
        } else {
            titleType = "Alert";
            color = "#FF0000";
        }
        String error = String.format("<font color='" + color + "'>[" + titleType + "] </font>" + title + " \n\n" +
                " --- \n\n " +
                "<font color='#708090' size=2>Alert ID: " + alarmId + "</font> \n\n " +
                "<font color='#778899' size=2>Alert Cluster: " + alarmCluster + "</font> \n\n " +
                " --- \n\n  " +
                "<font color='#708090' size=2>Alert Project: " + alarmProject + "</font> \n\n " +
                "<font color='#708090' size=2>Alert Status: " + alarmStatus + "</font> \n\n " +
                " --- \n\n  " +
                "<font color='#708090' size=2>Alert Level: " + alarmLevel + "</font> \n\n " +
                "<font color='#708090' size=2>Alert Times: " + alarmTimes + "</font> \n\n " +
                "<font color='#708090' size=2>Alert Content: " + alarmContent + "</font> \n\n " +
                " --- \n\n  " +
                "**Alert Date：" + alarmDate + "**");
        return error;
    }

    public String toWeChatMarkDown() {
        return title + " \n\n>AlarmID : [ **" + alarmId + "** ]\n> AlarmCluster : [ **" + alarmCluster + "** ]\n> AlarmStatus : [ **" + alarmStatus + "** ]\n" + "> AlarmLevel : [ " + alarmLevel + " ]\n" + "> AlarmProject : [ " + alarmProject + " ]\n" + "> AlarmTimes : [ " + alarmTimes
                + " ]\n" + "> AlarmDate : [ " + alarmDate + " ]\n" + "> AlarmContent : [ " + alarmContent + " ]";
    }

    public String toMail() {
        return title + " \n AlarmID : [ " + alarmId + " ]\n AlarmCluster : [ " + alarmCluster + " ]\n AlarmStatus : [ " + alarmStatus + " ]\n" + " AlarmLevel : [ " + alarmLevel + " ]\n" + " AlarmProject : [ " + alarmProject + " ]\n" + " AlarmTimes : [ " + alarmTimes
                + " ]\n AlarmDate : [ " + alarmDate + " ]\n" + " AlarmContent : [ " + alarmContent + " ]";
    }

    public String toMailJSON() {
        return title + " <br/> AlarmID : [ " + alarmId + " ]<br/> AlarmCluster : [ " + alarmCluster + " ]<br/> AlarmStatus : [ " + alarmStatus + " ]<br/>" + " AlarmLevel : [ " + alarmLevel + " ]<br/>" + " AlarmProject : [ " + alarmProject + " ]<br/>" + " AlarmTimes : [ " + alarmTimes
                + " ]<br/> AlarmDate : [ " + alarmDate + " ]<br/>" + " AlarmContent : [ " + alarmContent + " ]";
    }

    public String toJSON() {
        JSONObject json=new JSONObject();
        json.put("title",title);
        json.put("type","kafka");
        json.put("alarmId",alarmId);
        json.put("alarmCluster",alarmCluster);
        json.put("alarmStatus",alarmStatus);
        json.put("alarmLevel",alarmLevel);
        json.put("alarmProject",alarmProject);
        json.put("alarmTimes",alarmTimes);
        json.put("alarmTimes",alarmTimes);
        json.put("alarmTimes",alarmTimes);
        json.put("alarmDate",alarmDate);
        json.put("alarmContent",alarmContent);
        return json.toJSONString();
    }

    public String getAlarmCluster() {
        return alarmCluster;
    }

    public void setAlarmCluster(String alarmCluster) {
        this.alarmCluster = alarmCluster;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlarmStatus() {
        return alarmStatus;
    }

    public void setAlarmStatus(String alarmStatus) {
        this.alarmStatus = alarmStatus;
    }

    public String getAlarmProject() {
        return alarmProject;
    }

    public void setAlarmProject(String alarmProject) {
        this.alarmProject = alarmProject;
    }

    public String getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(String alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    public String getAlarmTimes() {
        return alarmTimes;
    }

    public void setAlarmTimes(String alarmTimes) {
        this.alarmTimes = alarmTimes;
    }

    public String getAlarmContent() {
        return alarmContent;
    }

    public void setAlarmContent(String alarmContent) {
        this.alarmContent = alarmContent;
    }

    public String getAlarmDate() {
        return alarmDate;
    }

    public void setAlarmDate(String alarmDate) {
        this.alarmDate = alarmDate;
    }

    public int getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(int alarmId) {
        this.alarmId = alarmId;
    }

}
