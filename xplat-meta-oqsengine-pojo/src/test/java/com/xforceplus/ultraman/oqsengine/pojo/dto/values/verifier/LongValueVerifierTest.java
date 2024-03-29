package com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * 数值型的校验器测试.
 *
 * @author dongbin
 * @version 0.1 2021/06/18 18:19
 * @since 1.8
 */
public class LongValueVerifierTest {

    @Test
    public void testTooLong() throws Exception {
        LongValueVerifier verifier = new LongValueVerifier();

        IEntityField field = EntityField.Builder.anEntityField()
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withLen(2)
                    .build()
            ).build();

        LongValue value = new LongValue(field, 123);

        Assertions.assertFalse(verifier.isTooLong(field, value));

        field = EntityField.Builder.anEntityField()
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withLen(2)
                    .build()
            ).build();

        value = new LongValue(field, 12);

        Assertions.assertTrue(verifier.isTooLong(field, value));

        field = EntityField.Builder.anEntityField()
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withLen(2)
                    .build()
            ).build();

        value = new LongValue(field, -12);

        Assertions.assertTrue(verifier.isTooLong(field, value));

        field = EntityField.Builder.anEntityField()
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withLen(2)
                    .build()
            ).build();

        value = new LongValue(field, -123);

        Assertions.assertFalse(verifier.isTooLong(field, value));
    }

}