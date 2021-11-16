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
        PatternValue value0 = new PatternValue(123, "AP:123");
        String key = PatternParserUtil.getPatternKey("AP:{000}", value0);
        Assert.assertEquals("AP:", key);
        PatternValue value1 = new PatternValue(123, "AP-2020-0101-123");
        String key1 = PatternParserUtil.getPatternKey("AP-2020-0101-{000}", value1);
        Assert.assertEquals("AP-2020-0101-", key1);

        PatternValue value2 = new PatternValue(123, "AP:123");
        String key2 = PatternParserUtil.getPatternKey("AP:{000}", value2);
        Assert.assertEquals("AP:", key2);

        PatternValue value3 = new PatternValue(1003, "AP:1003");
        String key3 = PatternParserUtil.getPatternKey("AP:{000}", value3);
        Assert.assertEquals("AP:", key3);
    }

    @Test
    public void testNeedResetTrue() {
        PatternValue value1 = new PatternValue(123, "1999-01-01-00123");
        PatternValue value2 = new PatternValue(124, "1999-01-02-00124");
        boolean result = PatternParserUtil.needReset("{yyyy}-{MM}-{dd}-{00000}", value1, value2);
        Assert.assertEquals(result, true);
    }

    @Test
    public void testNeedResetFalse() {
        PatternValue value1 = new PatternValue(123, "1999-01-01-00123");
        PatternValue value2 = new PatternValue(124, "1999-01-01-00124");
        boolean result = PatternParserUtil.needReset("{yyyy}-{MM}-{dd}-{00000}", value1, value2);
        Assert.assertEquals(result, false);
    }

    @Test
    public void testNeedResetMiddle() {
        PatternValue value1 = new PatternValue(123, "1999-01-00123:01");
        PatternValue value2 = new PatternValue(124, "1999-01-00124:02");
        boolean result = PatternParserUtil.needReset("{yyyy}-{MM}-{00000}:{dd}", value1, value2);
        Assert.assertEquals(true, result);
    }

    @Test
    public void testNeedResetFalse1() {
        PatternValue value1 = new PatternValue(123, "1999010100123");
        PatternValue value2 = new PatternValue(124, "1999010100124");
        boolean result = PatternParserUtil.needReset("{yyyy}{MM}{dd}{00000}", value1, value2);
        Assert.assertEquals(false, result);
    }

    @Test
    public void testNeedResetFalse2() {
        PatternValue value1 = new PatternValue(123, "1999010100123");
        PatternValue value2 = new PatternValue(199890, "19990101199890");
        boolean result = PatternParserUtil.needReset("{yyyy}{MM}{dd}{00000}", value1, value2);
        Assert.assertEquals(false, result);
    }

    @Test
    public void testNeedResetTrue2() {
        PatternValue value1 = new PatternValue(123, "1999010100123");
        PatternValue value2 = new PatternValue(199890, "19990103199890");
        boolean result = PatternParserUtil.needReset("{yyyy}{MM}{dd}{00000}", value1, value2);
        Assert.assertEquals(true, result);
    }

    @Test
    public void testNeedResetTrue4() {
        PatternValue value1 = new PatternValue(12, "00012");
        PatternValue value2 = new PatternValue(13, "00013");
        boolean result = PatternParserUtil.needReset("{00000}", value1, value2);
        Assert.assertEquals(false, result);
    }

    @Test
    public void testNeedResetTrue5() {
        PatternValue current = new PatternValue(36278, "20210812036278");
        PatternValue next = new PatternValue(36279, "20210813036279");
        boolean result = PatternParserUtil.needReset("{yyyy}{MM}{dd}{000000}", current, next);
        System.out.println(result);
    }


}
