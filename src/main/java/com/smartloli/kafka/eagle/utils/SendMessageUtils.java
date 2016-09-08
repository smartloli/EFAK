package com.smartloli.kafka.eagle.utils;

import com.smartloli.kafka.eagle.domain.MailSenderDomain;
import com.smartloli.kafka.eagle.service.MailSenderService;

public class SendMessageUtils {

	public static void send(String toAddress, String subject, String content) {

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
		MailSenderService sms = new MailSenderService();
		sms.sendHtmlMail(mailInfo);// send html format

	}

}
