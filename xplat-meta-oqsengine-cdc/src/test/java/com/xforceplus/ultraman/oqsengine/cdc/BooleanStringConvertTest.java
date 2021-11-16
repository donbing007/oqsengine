package com.xforceplus.ultraman.oqsengine.cdc;

import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.convertStringToBoolean;

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
        Assertions.assertTrue(convertStringToBoolean("true"));

        Assertions.assertFalse(convertStringToBoolean("false"));

        Assertions.assertTrue(convertStringToBoolean("1"));

        Assertions.assertFalse(convertStringToBoolean("0"));

        Assertions.assertFalse(convertStringToBoolean("-1"));

        Assertions.assertTrue(convertStringToBoolean("2"));

        Assertions.assertFalse(convertStringToBoolean("-1asa424412xxa"));
    }
}
