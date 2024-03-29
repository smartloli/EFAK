/**
 * CalendarUtil.java
 * <p>
 * Copyright 2023 smartloli
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kafka.eagle.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Convert the date or time to the specified format.
 *
 * @Author: smartloli
 * @Date: 2023/6/27 16:26
 * @Version: 3.4.0
 */
public class CalendarUtil {
    private static final String DATA_FORMAT_YEAN_MON_DAY_HOUR_MIN_SEC = "yyyy-MM-dd HH:mm:ss";
    private static final String DATA_FORMAT_YEAR = "yyyyMMdd";
    private static final String DATA_FORMAT_SPLIT_YEAR = "yyyy-MM-dd";

    private CalendarUtil() {

    }

    /**
     * Convert date time to unix time,default is yyyy-MM-dd HH:mm:ss.
     *
     * @param date
     * @return 1498443597
     * @throws ParseException
     */
    public static long convertDate2UnixTime(String date) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(DATA_FORMAT_YEAN_MON_DAY_HOUR_MIN_SEC);
        return df.parse(date).getTime();
    }

    /**
     * Convert time mill into ? day ? hour ? min ? sec.
     *
     * @param timeMill
     *            Time mill.
     * @return Character,from "3600 sec" to "0 Day 1 Hour 0 Min 0 Sec".
     */
    public static String convertTimeMill2Date(long timeMill) {
        long day = timeMill / (3600 * 24);
        long hour = (timeMill - 3600 * 24 * day) / (60 * 60);
        long min = (timeMill - 3600 * 24 * day - 3600 * hour) / 60;
        long sec = timeMill - 3600 * 24 * day - 3600 * hour - 60 * min;
        return day + "Day" + hour + "Hour" + min + "min" + sec + "sec";
    }

    /**
     * Convert unix time to date,default is yyyy-MM-dd HH:mm:ss.
     *
     * @param unixtime
     * @return Date String.
     */
    public static String convertUnixTime(long unixtime) {
        return convertUnixTime(unixtime, DATA_FORMAT_YEAN_MON_DAY_HOUR_MIN_SEC);
    }

    /**
     * Convert unix time to formatter date.
     *
     * @param unixtime
     * @param formatter
     * @return Date String.
     */
    public static String convertUnixTime(long unixtime, String formatter) {
        SimpleDateFormat df = new SimpleDateFormat(formatter);
        return df.format(new Date(unixtime));
    }

    /**
     * Convert unix time to date,default is yyyy-MM-dd HH:mm:ss.
     *
     * @param unixtime
     * @return 1907-01-01 00:00:00
     */
    public static String convertUnixTime2Date(long unixtime) {
        SimpleDateFormat df = new SimpleDateFormat(DATA_FORMAT_YEAN_MON_DAY_HOUR_MIN_SEC);
        return df.format(new Date(unixtime));
    }

    /** Get the date of the day,accurate to seconds. */
    public static String getDate() {
        SimpleDateFormat df = new SimpleDateFormat(DATA_FORMAT_YEAN_MON_DAY_HOUR_MIN_SEC);
        return df.format(new Date());
    }

    /** Get unix time. */
    public static long getTimeSpan() {
        return new Date().getTime();
    }

    /** Get custom date,like yyyy/mm/dd etc. */
    public static String getCustomDate(String formatter) {
        SimpleDateFormat df = new SimpleDateFormat(formatter);
        return df.format(new Date());
    }

    /** Get custom day. */
    public static String getCustomLastDay(int day) {
        SimpleDateFormat df = new SimpleDateFormat(DATA_FORMAT_YEAR);
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -day);
        return df.format(calendar.getTime());
    }

    /** Get custom day. */
    public static String getCustomLastDay(String formatter, int day) {
        SimpleDateFormat df = new SimpleDateFormat(formatter);
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -day);
        return df.format(calendar.getTime());
    }

    /** Get custom hour. */
    public static long getCustomLastHourUnix(int hour) {
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + hour);
        return calendar.getTime().getTime();
    }

    /** Convert date to date. */
    public static String convertDate2Date(String date) throws ParseException {
        SimpleDateFormat newly = new SimpleDateFormat(DATA_FORMAT_SPLIT_YEAR);
        SimpleDateFormat oldly = new SimpleDateFormat(DATA_FORMAT_YEAR);
        return newly.format(oldly.parse(date));
    }

    public static int getDiffDay(String beforeDate, String afterDate) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(DATA_FORMAT_YEAR);
        return Integer.parseInt((df.parse(afterDate).getTime() - df.parse(beforeDate).getTime()) / (1000 * 3600 * 24) + "");
    }

}
