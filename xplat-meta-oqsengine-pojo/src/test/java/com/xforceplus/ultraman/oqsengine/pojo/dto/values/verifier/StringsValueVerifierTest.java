package com.xforceplus.ultraman.oqsengine.pojo.dto.values.verifier;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 多值字符串的校验测试.
 *
 * @author dongbin
 * @version 0.1 2021/06/21 09:57
 * @since 1.8
 */
public class StringsValueVerifierTest {

    @Test
    public void testTooLong() throws Exception {
        StringsValueVerifier sv = new StringsValueVerifier();

        IEntityField field = EntityField.Builder.anEntityField()
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withLen(6)
                    .build()
            ).build();

        Assertions.assertTrue(sv.isTooLong(field,
            new StringsValue(field, "abc", "def")
        ));
        Assertions.assertFalse(sv.isTooLong(field,
            new StringsValue(field, "abc", "def", "123")
        ));
    }
}