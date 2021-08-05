package com.xforceplus.ultraman.oqsengine.common;


import com.xforceplus.ultraman.oqsengine.common.NumberUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 数字工具测试.
 *
 * @author dongbin
 * @version 0.1 2021/06/18 18:02
 * @since 1.8
 */
public class NumberUtilsTest {

    @Test
    public void testSize() {
        String value = "1";
        for (int i = 1; i <= 19; i++) {
            Assertions.assertEquals(i, NumberUtils.size(Long.parseLong(value)));

            value = value + "0";
        }

        value = "-1";
        for (int i = 1; i <= 19; i++) {
            Assertions.assertEquals(i, NumberUtils.size(Long.parseLong(value)));

            value = value + "0";
        }

        Assertions.assertEquals(1, NumberUtils.size(0));
    }

    @Test
    public void testZeroFill() {
        int maxLen = 19;
        Assertions.assertEquals("0000000000000000100", NumberUtils.zeroFill(100, maxLen));
        Assertions.assertEquals(Long.toString(Long.MAX_VALUE), NumberUtils.zeroFill(Long.MAX_VALUE, maxLen));

        try {
            NumberUtils.zeroFill(-20, maxLen);
            Assertions.fail("Negative numbers should throw exceptions, but they don't.");
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }
}