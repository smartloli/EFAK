/**
 * StrUtils.java
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

import com.alibaba.fastjson2.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * String conversion tool and null convert.
 *
 * @Author: smartloli
 * @Date: 2023/6/7 21:17
 * @Version: 3.4.0
 */
public class StrUtils {
    private final static long KB_IN_BYTES = 1024;

    private final static long MB_IN_BYTES = 1024 * KB_IN_BYTES;

    private final static long GB_IN_BYTES = 1024 * MB_IN_BYTES;

    private final static long TB_IN_BYTES = 1024 * GB_IN_BYTES;

    private final static DecimalFormat df = new DecimalFormat("0.00");

    private static String SYSTEM_ENCODING = System.getProperty("file.encoding");

    static {
        if (SYSTEM_ENCODING == null) {
            SYSTEM_ENCODING = "UTF-8";
        }
    }

    private StrUtils() {
    }

    /**
     * Formatter byte to kb,mb or gb etc.
     */
    public static String stringify(long byteNumber) {
        if (byteNumber / TB_IN_BYTES > 0) {
            return df.format((double) byteNumber / (double) TB_IN_BYTES) + "TB";
        } else if (byteNumber / GB_IN_BYTES > 0) {
            return df.format((double) byteNumber / (double) GB_IN_BYTES) + "GB";
        } else if (byteNumber / MB_IN_BYTES > 0) {
            return df.format((double) byteNumber / (double) MB_IN_BYTES) + "MB";
        } else if (byteNumber / KB_IN_BYTES > 0) {
            return df.format((double) byteNumber / (double) KB_IN_BYTES) + "KB";
        } else {
            return String.valueOf(byteNumber) + "B";
        }
    }

    /**
     * Formatter byte to kb,mb or gb etc.
     */
    public static JSONObject stringifyByObject(long byteNumber) {
        return stringifyByObject((double) byteNumber);
    }

    public static JSONObject stringifyByObject(String byteNumber) {
        return stringifyByObject(Double.valueOf(byteNumber));
    }

    public static JSONObject stringifyByObject(double byteNumber) {
        JSONObject object = new JSONObject();
        if (byteNumber / TB_IN_BYTES > 1) {
            String size = df.format(byteNumber / (double) TB_IN_BYTES);
            String type = "TB";
            object.put("size", size);
            object.put("type", type);
            object.put("value", size + type);
            return object;
        } else if (byteNumber / GB_IN_BYTES > 1) {
            String size = df.format(byteNumber / (double) GB_IN_BYTES);
            String type = "GB";
            object.put("size", size);
            object.put("type", type);
            object.put("value", size + type);
            return object;
        } else if (byteNumber / MB_IN_BYTES > 1) {
            String size = df.format(byteNumber / (double) MB_IN_BYTES);
            String type = "MB";
            object.put("size", size);
            object.put("type", type);
            object.put("value", size + type);
            return object;
        } else if (byteNumber / KB_IN_BYTES > 1) {
            String size = df.format(byteNumber / (double) KB_IN_BYTES);
            String type = "KB";
            object.put("size", size);
            object.put("type", type);
            object.put("value", size + type);
            return object;
        } else {
            String size = String.valueOf(byteNumber);
            String type = "B";
            object.put("size", size);
            object.put("type", type);
            object.put("value", size + type);
            return object;
        }
    }

    /**
     * Formatter string number.
     */
    public static double numberic(String number) {
        DecimalFormat formatter = new DecimalFormat("###.##");
        return Double.valueOf(formatter.format(Double.valueOf(number)));
    }

    /**
     * Formatter number.
     */
    public static double numberic(double number) {
        DecimalFormat formatter = new DecimalFormat("###.##");
        return Double.valueOf(formatter.format(number));
    }

    /**
     * Formatter customer string number.
     */
    public static double numberic(String number, String format) {
        DecimalFormat formatter = new DecimalFormat(format);
        return Double.valueOf(formatter.format(Double.valueOf(number)));
    }

    /**
     * Convert string number to double.
     */
    public static long integer(double number) {
        return Math.round(number);
    }

    /**
     * Assembly number to string.
     */
    public static String assembly(String number) {
        return stringify(integer(numberic(number)));
    }

    /**
     * whether string is empty.
     */
    public static boolean isNull(String value) {
        if (value == null || value.length() == 0 || "".equals(value)) {
            return true;
        }
        return false;
    }

    /**
     * whether string[] is empty.
     */
    public static boolean isNull(String[] value) {
        if (value == null || value.length == 0) {
            return true;
        }
        return false;
    }

    /**
     * whether list is empty.
     */
    public static boolean isListNull(String value) {
        if ("[]".equals(value)) {
            return true;
        }
        return false;
    }

    /**
     * Convert null to string.
     */
    public static String convertNull(String value) {
        if (isNull(value)) {
            return "";
        }
        return value;
    }

    /**
     * Convert strings to integers.
     */
    public static List<Integer> stringsConvertIntegers(String[] values) {
        List<Integer> integers = new ArrayList<Integer>();
        for (String value : values) {
            integers.add(Integer.parseInt(value.trim()));
        }
        return integers;
    }

    /**
     * Convert strings to list.
     */
    public static List<String> stringListConvertListStrings(String strings) {
        return Arrays.asList(strings.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\"", "").split(","));
    }

    public static String getUUid() {
        return java.util.UUID.randomUUID().toString().replaceAll("-", "");
    }
}
