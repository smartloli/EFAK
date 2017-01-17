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
package org.smartloli.kafka.eagle.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.domain.MailSenderDomain;
import org.smartloli.kafka.eagle.service.impl.MailSenderServiceImpl;
import org.smartloli.kafka.eagle.util.ConstantUtils;
import org.smartloli.kafka.eagle.util.SystemConfigUtils;

import com.google.gson.Gson;

/**
 * TODO
 * 
 * @author smartloli.
 *
 *         Created by Jan 17, 2017
 */
public class MailSender implements Sender {

	private final Logger LOG = LoggerFactory.getLogger(MailSender.class);

	@Override
	public void send(String... args) {
		if (args.length != ConstantUtils.Mail.ARGS.length) {
			LOG.error("Args length is error,correct is " + new Gson().toJson(ConstantUtils.Mail.ARGS) + "");
			return;
		}
		String toAddress = args[0];
		String subject = args[1];
		String content = args[2];
		MailSenderDomain mailInfo = new MailSenderDomain();
		mailInfo.setMailServerHost(SystemConfigUtils.getProperty("kafka.eagel.mail.server.host"));
		mailInfo.setMailServerPort(SystemConfigUtils.getProperty("kafka.eagel.mail.server.port"));
		mailInfo.setValidate(true);
		mailInfo.setUserName(SystemConfigUtils.getProperty("kafka.eagel.mail.username"));
		mailInfo.setPassword(SystemConfigUtils.getProperty("kafka.eagel.mail.password"));
		mailInfo.setFromAddress(SystemConfigUtils.getProperty("kafka.eagel.mail.username"));
		mailInfo.setToAddress(toAddress);
		mailInfo.setSubject(subject);
		mailInfo.setContent(content);
		MailSenderServiceImpl sms = new MailSenderServiceImpl();
		sms.sendHtmlMail(mailInfo);// send html format
	}

}
