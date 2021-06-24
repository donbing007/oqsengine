package com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import java.math.BigDecimal;
import org.junit.Assert;
import org.junit.Test;

/**
 * 浮点数校验测试.
 *
 * @author dongbin
 * @version 0.1 2021/06/18 20:24
 * @since 1.8
 */
public class DecimalValueVerifierTest {

    @Test
    public void testTooLong() throws Exception {
        DecimalValueVerifier verifier = new DecimalValueVerifier();

        IEntityField field = EntityField.Builder.anEntityField()
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withLen(3)
                    .build()
            ).build();

        Assert.assertTrue(verifier.isTooLong(field,
            new DecimalValue(field, new BigDecimal("1"))
        ));
        Assert.assertTrue(verifier.isTooLong(field,
            new DecimalValue(field, new BigDecimal("1.1"))
        ));
        Assert.assertFalse(verifier.isTooLong(field,
            new DecimalValue(field, new BigDecimal("1.13"))
        ));
    }

    @Test
    public void testHighPrecision() throws Exception {
        DecimalValueVerifier verifier = new DecimalValueVerifier();
        IEntityField field = EntityField.Builder.anEntityField()
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withLen(3)
                    .withPrecision(3)
                    .build()
            ).build();

        Assert.assertTrue(verifier.isHighPrecision(field,
            new DecimalValue(field, new BigDecimal("1"))
        ));
        Assert.assertTrue(verifier.isHighPrecision(field,
            new DecimalValue(field, new BigDecimal("1.0"))
        ));
        Assert.assertTrue(verifier.isHighPrecision(field,
            new DecimalValue(field, new BigDecimal("1.11"))
        ));
        Assert.assertTrue(verifier.isHighPrecision(field,
            new DecimalValue(field, new BigDecimal("1.113"))
        ));
        Assert.assertFalse(verifier.isHighPrecision(field,
            new DecimalValue(field, new BigDecimal("1.1143"))
        ));

        field = EntityField.Builder.anEntityField()
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withLen(3)
                    .withPrecision(0)
                    .build()
            ).build();

        Assert.assertTrue(verifier.isHighPrecision(field,
            new DecimalValue(field, new BigDecimal("1"))
        ));
    }
}