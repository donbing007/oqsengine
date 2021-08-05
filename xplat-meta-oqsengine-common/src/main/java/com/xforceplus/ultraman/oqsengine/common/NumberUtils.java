package com.xforceplus.ultraman.oqsengine.common;

/**
 * 数字工具.
 *
 * @author dongbin
 * @version 0.1 2021/06/18 18:00
 * @since 1.8
 */
public final class NumberUtils {

    // 负数的位数阶梯.
    static long[] NEGATIVE_STEPS = new long[] {
        -10L,
        -100L,
        -1000L,
        -10000L,
        -100000L,
        -1000000L,
        -10000000L,
        -100000000L,
        -1000000000L,
        -10000000000L,
        -100000000000L,
        -1000000000000L,
        -10000000000000L,
        -100000000000000L,
        -1000000000000000L,
        -10000000000000000L,
        -100000000000000000L,
        -1000000000000000000L
    };

    // 正数的位数阶梯.
    static long[] POSITIVE_STEPS = new long[] {
        10L,
        100L,
        1000L,
        10000L,
        100000L,
        1000000L,
        10000000L,
        100000000L,
        1000000000L,
        10000000000L,
        100000000000L,
        1000000000000L,
        10000000000000L,
        100000000000000L,
        1000000000000000L,
        10000000000000000L,
        100000000000000000L,
        1000000000000000000L
    };

    private NumberUtils() {
    }

    /**
     * 判断指定整数的位数.
     *
     * @param value 目标值.
     * @return 位数.
     */
    public static int size(int value) {
        return size((long) value);
    }

    /**
     * 判断长整形数字的位数.
     *
     * @param value 目标数字.
     * @return 位数.
     */
    public static int size(long value) {
        if (value == 0) {
            return 1;
        }

        if (value < 0) {
            for (int i = 0; i < NEGATIVE_STEPS.length; i++) {
                if (value > NEGATIVE_STEPS[i]) {
                    return i + 1;
                }
            }

            return NEGATIVE_STEPS.length + 1;
        } else {
            for (int i = 0; i < POSITIVE_STEPS.length; i++) {
                if (value < POSITIVE_STEPS[i]) {
                    return i + 1;
                }
            }
            return POSITIVE_STEPS.length + 1;
        }
    }

    /**
     * 将整形转换成字符串,同时不足指定位数补0.
     *
     * @param value 目标数字.
     * @param maxLen 目标最大位数.
     * @return 结果.
     */
    public static String zeroFill(int value, int maxLen) {
        return zeroFill((long) value, maxLen);
    }

    /**
     * 转换成字符串,同时如果位数达不到指定的上限在左边补0.
     *
     * @param value  目标值.
     * @param maxLen 需要的最大长度.
     * @return 结果.
     */
    public static String zeroFill(long value, int maxLen) {
        if (value < 0) {
            throw new IllegalArgumentException("Cannot use negative numbers.");
        }

        int len = NumberUtils.size(value);
        int gap = maxLen - len;

        if (gap <= 0) {
            return Long.toString(value);
        } else {
            StringBuilder buff = new StringBuilder();
            for (int i = 0; i < gap; i++) {
                buff.append('0');
            }
            buff.append(value);
            return buff.toString();
        }
    }
}
