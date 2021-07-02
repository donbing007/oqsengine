package com.xforceplus.ultraman.oqsengine.common.number;


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
    }

}