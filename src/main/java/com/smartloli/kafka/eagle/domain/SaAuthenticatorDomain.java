package com.smartloli.kafka.eagle.domain;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class SaAuthenticatorDomain extends Authenticator {
	String userName = null;
	String password = null;

	public SaAuthenticatorDomain() {
	}

	public SaAuthenticatorDomain(String username, String password) {
		this.userName = username;
		this.password = password;
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(userName, password);
	}

}
