package com.xforceplus.ultraman.oqsengine.common.number;

/**
 * 数字工具.
 *
 * @author dongbin
 * @version 0.1 2021/06/18 18:00
 * @since 1.8
 */
public class NumberUtils {

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

    public static int size(int value) {
        return size((long) value);
    }

    /**
     * 判断数字的位数.
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
}
