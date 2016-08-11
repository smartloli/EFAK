package com.webank.cms.utils;

import java.security.MessageDigest;

import org.apache.commons.codec.binary.Base64;

/**
 * @Date Aug 5, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note English : Encode login password
 * 
 *       中文：加密登录密码
 */
public class MD5Utils {

	public static String md5(String message) {
		try {
			MessageDigest md = MessageDigest.getInstance("md5");
			byte md5[] = md.digest(message.getBytes());

			return Base64.encodeBase64String(md5);
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
	}

	public static void main(String[] args) {

		System.out.println(md5("123456"));

	}
}
