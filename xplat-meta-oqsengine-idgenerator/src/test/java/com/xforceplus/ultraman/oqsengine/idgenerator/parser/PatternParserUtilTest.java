package com.xforceplus.ultraman.oqsengine.idgenerator.parser;

import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.PatternValue;
import org.junit.Assert;
import org.junit.Test;

/**
 * .
 *
 * @author leo
 * @version 0.1 5/19/21 6:49 PM
 * @since 1.8
 */
public class PatternParserUtilTest {

    @Test
    public void testGetPatternKey() {
        PatternValue value0 = new PatternValue(123,"AP:123");
        String key = PatternParserUtil.getPatternKey(value0);
        Assert.assertEquals("AP:",key);
        PatternValue value1 = new PatternValue(123,"AP-2020-0101-123");
        String key1 = PatternParserUtil.getPatternKey(value1);
        Assert.assertEquals("AP-2020-0101-",key1);

        PatternValue value2 = new PatternValue(123,"AP-2020-0101123");
        String key2 = PatternParserUtil.getPatternKey(value2);
        Assert.assertEquals("AP-2020-",key2);
    }
}
