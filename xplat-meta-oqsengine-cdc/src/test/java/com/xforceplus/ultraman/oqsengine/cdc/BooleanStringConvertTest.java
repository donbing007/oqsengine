package com.xforceplus.ultraman.oqsengine.cdc;

import org.junit.Assert;
import org.junit.Test;

import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.convertStringToBoolean;

/**
 * desc :
 * name : BooleanStringConvertTest
 *
 * @author : xujia
 * date : 2020/11/13
 * @since : 1.8
 */
public class BooleanStringConvertTest {
    @Test
    public void testBooleanStringConvert() {
        Assert.assertTrue(convertStringToBoolean("true"));

        Assert.assertFalse(convertStringToBoolean("false"));

        Assert.assertTrue(convertStringToBoolean("1"));

        Assert.assertFalse(convertStringToBoolean("0"));

        Assert.assertFalse(convertStringToBoolean("-1"));

        Assert.assertTrue(convertStringToBoolean("2"));
    }
}
