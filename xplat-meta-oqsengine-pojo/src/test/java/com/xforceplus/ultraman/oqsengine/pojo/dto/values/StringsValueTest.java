package com.xforceplus.ultraman.oqsengine.pojo.dto.values;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 多值字符串测试.
 */
public class StringsValueTest {

    @Test
    public void testEquals() throws Exception {
        StringsValue one = new StringsValue(EntityField.CREATE_TIME_FILED, "a", "b");
        StringsValue two = new StringsValue(EntityField.CREATE_TIME_FILED, "a", "c");

        Assertions.assertFalse(one.equals(two));

        one = new StringsValue(EntityField.CREATE_TIME_FILED, "a", "b");
        two = new StringsValue(EntityField.CREATE_TIME_FILED, "a", "b");

        Assertions.assertTrue(one.equals(two));

        one = new StringsValue(EntityField.CREATE_TIME_FILED, new String[] {"a", "b"}, "a1");
        two = new StringsValue(EntityField.CREATE_TIME_FILED, new String[] {"a", "b"}, "a2");

        Assertions.assertFalse(one.equals(two));

        one = new StringsValue(EntityField.CREATE_TIME_FILED, new String[] {"a", "b"}, "a");
        two = new StringsValue(EntityField.CREATE_TIME_FILED, new String[] {"a", "b"}, "a");

        Assertions.assertTrue(one.equals(two));
    }
}