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
package org.smartloli.kafka.eagle.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Convert the date or time to the specified format.
 *
 * @author smartloli.
 *
 *         Created by Nov 6, 2015
 */
public class CalendarUtils {

	/** Get the end of the day, accurate to minute. */
	public static String getCurrentEndDate() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return df.format(new Date());
	}

	/** Get the start of the day, accurate to minute. */
	public static String getCurrentStartDate() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:00");
		return df.format(new Date());
	}

	/** Get the corresponding string per minute. */
	public static String getStatsPerDate() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return df.format(new Date());
	}

	/** Get the date of yesterday, accurate to seconds. */
	public static String getYestoday() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
		Calendar calendar = Calendar.getInstance();
		Date curr = new Date();
		calendar.setTime(curr);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		return df.format(calendar.getTime());
	}

	/** Gets the current time stamp. */
	public static long getTime() {
		return new Date().getTime();
	}

	/** Get the date of yesterday. */
	public static String getLastDay() {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		Calendar calendar = Calendar.getInstance();
		Date curr = new Date();
		calendar.setTime(curr);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		return df.format(calendar.getTime());
	}

	/** Get the date of last month */
	public static String[] getLastMonth() {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		Calendar calendarFirstDay = Calendar.getInstance();
		calendarFirstDay.add(Calendar.MONTH, -1);
		calendarFirstDay.set(Calendar.DAY_OF_MONTH, 1);
		Calendar calendarLastDay = Calendar.getInstance();
		calendarLastDay.set(Calendar.DAY_OF_MONTH, 1);
		calendarLastDay.add(Calendar.DATE, -1);
		return new String[] { df.format(calendarFirstDay.getTime()), df.format(calendarLastDay.getTime()) };
	}

	/**
	 * Convert time stamp into Chinese time.
	 * 
	 * @param date
	 *            Time stamp.
	 * @return Character.
	 */
	public static String time2StrDate(long date) {
		long day = date / (3600 * 24);
		long hour = (date - 3600 * 24 * day) / (60 * 60);
		long min = (date - 3600 * 24 * day - 3600 * hour) / 60;
		long sec = date - 3600 * 24 * day - 3600 * hour - 60 * min;
		return day + "天" + hour + "时" + min + "分" + sec + "秒";
	}

	/**  */
	public static String timeSpan2StrDate(long date) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(new Date(date));
	}

	/**
	 * yyyy-MM-dd HH:mm:ss
	 * 
	 * @return
	 */
	public static String getNormalDate() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(new Date());
	}

	public static String getZkHour() {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHH");
		return df.format(new Date());
	}

	public static void main(String[] args) {
		System.out.println(getLastDay());// 2505600
	}

}
