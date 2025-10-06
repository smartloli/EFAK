package org.kafka.eagle.core.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * <p>
 * 字符串工具类，提供常用的字符串操作方法。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/6/22 18:48
 * @version 5.0.0
 */
@Slf4j
public class StrUtils {

    /**
     * 将字符串转换为数值并进行四舍五入
     *
     * @param str 要转换的字符串值
     * @return 数值（double 类型）
     */
    public static double numberic(String str) {
        try {
            if (str == null || str.trim().isEmpty()) {
                return 0.0;
            }

            BigDecimal bd = new BigDecimal(str.trim());
            return bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
        } catch (NumberFormatException e) {
            log.warn("Failed to parse numeric value: {}", str);
            return 0.0;
        }
    }

    /**
     * 检查字符串是否为空或仅包含空白字符
     *
     * @param str 要检查的字符串
     * @return 如果字符串为空或仅包含空白字符返回 true，否则返回 false
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 检查字符串是否不为空且不仅包含空白字符
     *
     * @param str 要检查的字符串
     * @return 如果字符串不为空且不仅包含空白字符返回 true，否则返回 false
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 将字节数格式化为人类可读的字符串
     *
     * @param bytes 字节数
     * @return 格式化后的字符串（例如："1.5 KB", "2.3 MB"）
     */
    public static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    public static String getUUid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 将百分比格式化为字符串
     *
     * @param percentage 百分比值（0-100）
     * @return 格式化后的百分比字符串（例如："85.5%"）
     */
    public static String formatPercentage(double percentage) {
        return String.format("%.2f%%", percentage);
    }
}