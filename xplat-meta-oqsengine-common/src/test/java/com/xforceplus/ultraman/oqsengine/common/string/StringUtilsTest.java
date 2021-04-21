package com.xforceplus.ultraman.oqsengine.common.string;

import org.junit.Assert;
import org.junit.Test;

/**
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

        Assert.assertEquals("startend", StringUtils.filterCanSeeChar(buff.toString()));
        Assert.assertEquals(null, StringUtils.filterCanSeeChar(null));
        Assert.assertEquals("", StringUtils.filterCanSeeChar(""));
        Assert.assertEquals("abcdef", StringUtils.filterCanSeeChar("abcdef"));
    }

}