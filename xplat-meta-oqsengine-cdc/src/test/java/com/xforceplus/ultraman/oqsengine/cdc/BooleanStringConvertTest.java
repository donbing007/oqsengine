package com.xforceplus.ultraman.oqsengine.cdc;

import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.stringToBoolean;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * desc :.
 * name : BooleanStringConvertTest
 *
 * @author xujia 2020/11/13
 * @since 1.8
 */
public class BooleanStringConvertTest {
    @Test
    public void testBooleanStringConvert() {
        Assertions.assertTrue(stringToBoolean("true"));

        Assertions.assertFalse(stringToBoolean("false"));

        Assertions.assertTrue(stringToBoolean("1"));

        Assertions.assertFalse(stringToBoolean("0"));

        Assertions.assertFalse(stringToBoolean("-1"));

        Assertions.assertTrue(stringToBoolean("2"));

        Assertions.assertFalse(stringToBoolean("-1asa424412xxa"));
    }
}
