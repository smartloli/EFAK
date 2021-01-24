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
package org.smartloli.kafka.eagle.api.im;

import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmCrontabInfo;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmMessageInfo;

/**
 * IM provider IMService visitor enter.
 *
 * @author smartloli.
 * <p>
 * Created by Jan 1, 2019
 */
public interface IMService {

    /**
     * Send post request alert message by dingding.
     */
    public void sendPostMsgByDingDing(AlarmMessageInfo alarmMessageInfo, String url, AlarmCrontabInfo alarmCrontabInfo, String isNormal);

    /**
     * Stop IM quartz tasks.
     */
    public void removePostMsgByIM(String id, String type, String isNormal);

    /**
     * Send alert message by wechat.
     */
    public void sendPostMsgByWeChat(String data, String url);

    /**
     * Send alert message by mail.
     */
    public void sendPostMsgByMail(String data, String url);

}
