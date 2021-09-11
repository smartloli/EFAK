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
package org.smartloli.kafka.eagle.api.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.api.mail.MailServerInfo;
import org.smartloli.kafka.eagle.api.mail.MailUtils;
import org.smartloli.kafka.eagle.common.protocol.alarm.AlarmEmailJsonInfo;

/**
 * Send mail by server address.
 *
 * @author smartloli.
 * <p>
 * Created by Nov 07, 2020
 */
public class MailFactoryUtils {

    /**
     * Send msg by mail address.
     */
    public static void send(AlarmEmailJsonInfo email, String data) {
        JSONObject jsonData = JSON.parseObject(data);
        MailUtils mail = new MailUtils();
        mail.setAddress(jsonData.getString("address"));
        mail.setContent(jsonData.getString("msg"));
        mail.setSubject(jsonData.getString("title"));
        MailServerInfo mailServerInfo = new MailServerInfo();
        mailServerInfo.setHost(email.getHost());
        mailServerInfo.setPort(email.getPort());
        mailServerInfo.setSa(email.getSa());
        mailServerInfo.setUsername(email.getUsername());
        mailServerInfo.setPassword(email.getPassword());
        mailServerInfo.setEnableSsl(email.isEnableSsl());
        mail.setMailServerInfo(mailServerInfo);
        mail.start();
    }

}
