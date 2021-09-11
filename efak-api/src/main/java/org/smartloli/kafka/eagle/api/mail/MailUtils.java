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
package org.smartloli.kafka.eagle.api.mail;

/**
 * The mail utility class used to send notifications.
 *
 * @author smartloli.
 * <p>
 * Created by Nov 03, 2020
 */
public class MailUtils extends Thread {

    private String subject = "";
    private String address = "";
    private String content = "";
    private MailServerInfo mailServerInfo;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MailServerInfo getMailServerInfo() {
        return mailServerInfo;
    }

    public void setMailServerInfo(MailServerInfo mailServerInfo) {
        this.mailServerInfo = mailServerInfo;
    }

    public void run() {
        send(subject, address, content, mailServerInfo);
    }

    /**
     * @param subject mail theme
     * @param content mail content
     * @param address mail address, Support multiple people send, such as
     *                "name1@email.com;name2@email.com"
     */
    private boolean send(String subject, String address, String content, MailServerInfo mailServer) {
        MailSenderInfo mailInfo = new MailSenderInfo();
        mailInfo.setMailServerHost(mailServer.getHost());
        mailInfo.setMailServerPort(mailServer.getPort());
        mailInfo.setValidate(true);
        mailInfo.setUserName(mailServer.getSa());
        mailInfo.setPassword(mailServer.getPassword());
        mailInfo.setFromAddress(mailServer.getUsername());
        mailInfo.setToAddress(address);
        mailInfo.setSubject(subject);
        mailInfo.setContent(content);
        mailInfo.setEnableSsl(mailServer.isEnableSsl());

        SimpleMailSender sms = new SimpleMailSender();
        return sms.sendHtmlMail(mailInfo);// Send html format
    }

}
