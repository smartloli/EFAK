package com.smartloli.kafka.eagle.test;

import java.util.HashSet;
import java.util.Set;

import com.smartloli.kafka.eagle.utils.SendMessageUtils;

/**
 * @Date Sep 8, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
 */
public class MailTest {

	public static void main(String[] args) {
		String subject = "Alarm Lag";
		String content = "Lag exceeds a specified threshold : Msg is somethings test...";
		Set<String> set = new HashSet<String>();
		set.add("810371213@qq.com");
		int count = 0;
		for (String sender : set) {
			SendMessageUtils.send(sender, subject, content);
			System.out.println("sender count[" + (++count) + "]");
		}
	}

}
