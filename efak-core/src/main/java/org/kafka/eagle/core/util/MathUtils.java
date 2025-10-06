package org.kafka.eagle.core.util;

/**
 * <p>
 * 数学工具类，提供常用的计算方法。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/8/23 21:20:12
 * @version 5.0.0
 */
public class MathUtils {

    /**
     * 计算整数除法的向上取整。
     * 
     * @param dividend 被除数
     * @param divisor  除数
     * @return 大于或等于 dividend/divisor 的最小整数，如果除数为0则返回0
     */
    public static int ceil(int dividend, int divisor) {
        if (divisor == 0) {
            return 0;
        }
        return (dividend + divisor - 1) / divisor;
    }

    /**
     * 计算长整数除法的向上取整。
     * 
     * @param dividend 被除数
     * @param divisor  除数
     * @return 大于或等于 dividend/divisor 的最小长整数，如果除数为0则返回0
     */
    public static long ceil(long dividend, long divisor) {
        if (divisor == 0) {
            return 0;
        }
        return (dividend + divisor - 1) / divisor;
    }

    /**
     * 计算某个值相对于总值的百分比。
     * 
     * @param value 部分值
     * @param total 总值
     * @return 百分比（0-100），如果总值为0则返回0
     */
    public static int percentage(int value, int total) {
        if (total == 0) {
            return 0;
        }
        return value * 100 / total;
    }
}