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
package org.smartloli.kafka.eagle.api.email;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang.StringUtils;
import org.smartloli.kafka.eagle.common.protocol.MailSenderInfo;
import org.smartloli.kafka.eagle.common.protocol.SaAuthenticatorInfo;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;

/**
 * Implements MailService all method.
 * 
 * @author smartloli.
 *
 *         Created by Jan 17, 2017
 * 
 * @see org.smartloli.kafka.eagle.factory.MailService
 */
public class MailServiceImpl implements MailService {

	/** Send alert message by email. */
	@Override
	public boolean send(String subject, String address, String content, String attachment) {
		MailSenderInfo mailInfo = new MailSenderInfo();
		mailInfo.setMailServerHost(SystemConfigUtils.getProperty("kafka.eagle.mail.server.host"));
		mailInfo.setMailServerPort(SystemConfigUtils.getProperty("kafka.eagle.mail.server.port"));
		mailInfo.setValidate(true);
		mailInfo.setUserName(SystemConfigUtils.getProperty("kafka.eagle.mail.sa"));
		mailInfo.setPassword(SystemConfigUtils.getProperty("kafka.eagle.mail.password"));
		mailInfo.setFromAddress(SystemConfigUtils.getProperty("kafka.eagle.mail.username"));
		mailInfo.setToAddress(address);
		mailInfo.setSubject(subject);
		mailInfo.setContent(content);

		List<File> fileList = null;
		if (StringUtils.isNotBlank(attachment)) {
			fileList = new ArrayList<File>();
			String[] attachments = attachment.split(";");
			File file = null;
			for (String fileName : attachments) {
				file = new File(fileName);
				fileList.add(file);
			}
			mailInfo.setFileList(fileList);
		}

		return sendHtmlMail(mailInfo);
	}

	/** Send mail in HTML format */
	private boolean sendHtmlMail(MailSenderInfo mailInfo) {
		boolean enableMailAlert = SystemConfigUtils.getBooleanProperty("kafka.eagle.mail.enable");
		if (enableMailAlert) {
			SaAuthenticatorInfo authenticator = null;
			Properties pro = mailInfo.getProperties();
			if (mailInfo.isValidate()) {
				authenticator = new SaAuthenticatorInfo(mailInfo.getUserName(), mailInfo.getPassword());
			}
			Session sendMailSession = Session.getDefaultInstance(pro, authenticator);
			try {
				Message mailMessage = new MimeMessage(sendMailSession);
				Address from = new InternetAddress(mailInfo.getFromAddress());
				mailMessage.setFrom(from);
				Address[] to = new Address[mailInfo.getToAddress().split(",").length];
				int i = 0;
				for (String e : mailInfo.getToAddress().split(","))
					to[i++] = new InternetAddress(e);
				mailMessage.setRecipients(Message.RecipientType.TO, to);
				mailMessage.setSubject(mailInfo.getSubject());
				mailMessage.setSentDate(new Date());
				Multipart mainPart = new MimeMultipart();
				BodyPart html = new MimeBodyPart();
				html.setContent(mailInfo.getContent(), "text/html; charset=UTF-8");
				mainPart.addBodyPart(html);

				List<File> list = mailInfo.getFileList();
				if (list != null && list.size() > 0) {
					for (File f : list) {
						MimeBodyPart mbp = new MimeBodyPart();
						FileDataSource fds = new FileDataSource(f.getAbsolutePath());
						mbp.setDataHandler(new DataHandler(fds));
						mbp.setFileName(f.getName());
						mainPart.addBodyPart(mbp);
					}

					list.clear();
				}

				mailMessage.setContent(mainPart);
				Transport.send(mailMessage);
				return true;
			} catch (MessagingException ex) {
				ex.printStackTrace();
			}
		}
		return false;
	}

}
