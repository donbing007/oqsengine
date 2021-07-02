package com.xforceplus.ultraman.oqsengine.common.string;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 字符串工具测试.
 *
 * @author dongbin
 * @version 0.1 2021/04/20 16:09
 * @since 1.8
 */
public class StringUtilsTest {

    @Test
    public void testFilter() {
        StringBuilder buff = new StringBuilder();
        buff.append("start");

        for (int i = 0; i < 32; i++) {
            buff.append((char) i);
        }

        buff.append((char) 127);

        buff.append("end");

        Assertions.assertEquals("startend", StringUtils.filterCanSeeChar(buff.toString()));
        Assertions.assertEquals(null, StringUtils.filterCanSeeChar(null));
        Assertions.assertEquals("", StringUtils.filterCanSeeChar(""));
        Assertions.assertEquals("abcdef", StringUtils.filterCanSeeChar("abcdef"));
    }

}