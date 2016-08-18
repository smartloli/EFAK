package com.smartloli.kafka.eagle.quartz;

import java.util.Date;

/**
 * @Date Aug 18, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note Per 5 mins to stats offsets to offsets table
 */
public class OffsetsQuartz {

	public void jobQuartz() {
		System.out.println(new Date().toString());
	}

}
