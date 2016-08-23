package com.smartloli.kafka.eagle.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @Date Nov 6, 2015
 *
 * @Author dengjie
 *
 * @Note Get stats date,include day|month
 */
public class CalendarUtils {

	public static String getCurrentEndDate() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(new Date());
	}

	public static String getCurrentStartDate() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
		return df.format(new Date());
	}

	public static String getStatsPerDate() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return df.format(new Date());
	}

	public static String getYestoday() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
		Calendar calendar = Calendar.getInstance();
		Date curr = new Date();
		calendar.setTime(curr);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		return df.format(calendar.getTime());
	}

	public static long getTime() {
		return new Date().getTime();
	}

	public static String getLastDay() {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		Calendar calendar = Calendar.getInstance();
		Date curr = new Date();
		calendar.setTime(curr);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		return df.format(calendar.getTime());
	}

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

	public static String time2StrDate(long date) {
		long day = date / (3600 * 24);
		long hour = (date - 3600 * 24 * day) / (60 * 60);
		long min = (date - 3600 * 24 * day - 3600 * hour) / 60;
		long sec = date - 3600 * 24 * day - 3600 * hour - 60 * min;
		return day + "天" + hour + "时" + min + "分" + sec + "秒";
	}

	public static String timeSpan2StrDate(long date) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(new Date(date));
	}

	public static void main(String[] args) {
		System.out.println(getYestoday());// 2505600
	}

}
