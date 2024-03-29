package com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 字符串校验器测试.
 *
 * @author dongbin
 * @version 0.1 2021/06/18 18:14
 * @since 1.8
 */
public class StringValueVerifierTest {

    @Test
    public void testTooLong() throws Exception {
        StringValueVerifier verifier = new StringValueVerifier();

        IEntityField field = EntityField.Builder.anEntityField()
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                .withLen(2)
                .build()
            ).build();

        StringValue value = new StringValue(field, "abc");

        Assertions.assertFalse(verifier.isTooLong(field, value));

        field = EntityField.Builder.anEntityField()
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withLen(2)
                    .build()
            ).build();

        value = new StringValue(field, "ab");

        Assertions.assertTrue(verifier.isTooLong(field, value));
    }

}