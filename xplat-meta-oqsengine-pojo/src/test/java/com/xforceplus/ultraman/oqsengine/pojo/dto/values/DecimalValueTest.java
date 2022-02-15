package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * 浮点数测试.
 *
 * @author dongbin
 * @version 0.1 2021/06/24 15:35
 * @since 1.8
 */
public class DecimalValueTest {

    /**
     * 测试0精度的.
     */
    @Test
    public void testZeroPrecision() throws Exception {
        IEntityField field = EntityField.Builder.anEntityField()
            .withId(123L)
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withPrecision(0).build()
            ).build();

        DecimalValue value = new DecimalValue(field, new BigDecimal("123"));
        Assertions.assertEquals("123.0", value.valueToString());

        value = new DecimalValue(field, new BigDecimal("123.789"));
        Assertions.assertEquals("123.789", value.valueToString());
    }

    @Test
    public void testOverflow() throws Exception {
        IEntityField field = EntityField.Builder.anEntityField()
            .withId(123L)
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withPrecision(0).build()
            ).build();

        BigDecimal value = new BigDecimal("1.11111111111111111111");
        try {
            DecimalValue decimalValue = new DecimalValue(field, value);
            Assertions.fail("No expected exception is obtained, "
                + "and neither decimal or integer digits exceed the length of long.MAX_VALUE.");
        } catch (Exception ex) {
            // success
        }

        value = new BigDecimal("11111111111111111111.1");
        try {
            DecimalValue decimalValue = new DecimalValue(field, value);
            Assertions.fail("No expected exception is obtained, "
                + "and neither decimal or integer digits exceed the length of long.MAX_VALUE.");
        } catch (Exception ex) {
            // success
        }

        value = new BigDecimal("11111111111111111111.11111111111111111111");
        try {
            DecimalValue decimalValue = new DecimalValue(field, value);
            Assertions.fail("No expected exception is obtained, "
                + "and neither decimal or integer digits exceed the length of long.MAX_VALUE.");
        } catch (Exception ex) {
            // success
        }
    }
}