package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import java.math.BigDecimal;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author dongbin
 * @version 0.1 2021/09/22 17:35
 * @since 1.8
 */
public class DecimalValueTest extends TestCase {

    @Test
    public void testIntTooLong() {
        String value = "72001489717205223771.0000";
        try {
            DecimalValue decimalValue = new DecimalValue(EntityField.UPDATE_TIME_FILED, new BigDecimal(value));
            Assert.fail("Shoud err.");
        } catch(Exception ex) {

        }
    }

    @Test
    public void testDecTooLong() {
        String value = "1.72001489717205223771";
        try {
            DecimalValue decimalValue = new DecimalValue(EntityField.UPDATE_TIME_FILED, new BigDecimal(value));
            Assert.fail("Shoud err.");
        } catch(Exception ex) {

        }
    }

    @Test
    public void testSuccess() {
        String value = "7200148971720522377.8200148971720522377";
        DecimalValue decimalValue = new DecimalValue(EntityField.UPDATE_TIME_FILED, new BigDecimal(value));
        Assert.assertEquals(7200148971720522377L, decimalValue.integerValue());
        Assert.assertEquals(8200148971720522377L, decimalValue.decValue());
    }

}