package com.xforceplus.ultraman.oqsengine.calculation;

import static com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils.Scale.ROUND_HALF_UP;

import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public class PrecisionTest {

    @Test
    public void scaleTest() {
        String raw = "11.22385321100917431192660550458716";
        BigDecimal unScale = new BigDecimal(raw).setScale(2, ROUND_HALF_UP.getMode());

        Assertions.assertEquals("11.22", unScale.toString());
    }
}
