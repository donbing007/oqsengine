package com.xforceplus.ultraman.oqsengine.storage.value.strategy.common;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * string 物理值处理策略测试.
 *
 * @author dongbin
 * @version 1.0 04/08/2022
 */
public class StringStorageStrategyTest {

    @Test
    public void testToStorageValue() throws Exception {
        StringValue value = new StringValue(EntityField.CREATE_TIME_FILED, "test", "123");

        StringStorageStrategy storageStrategy = new StringStorageStrategy();
        StorageValue sv = storageStrategy.toStorageValue(value);

        Assertions.assertEquals("test", sv.value());
        Assertions.assertEquals("123", sv.getAttachment().value());
    }

    @Test
    public void testToLogicValue() throws Exception {
        StringStorageValue sv = new StringStorageValue(EntityField.CREATE_TIME_FILED.idString(), "test", true);
        sv.setAttachment(new StringStorageValue(EntityField.CREATE_TIME_FILED.idString(), "123", true));

        StringStorageStrategy storageStrategy = new StringStorageStrategy();
        IValue logicValue = storageStrategy.toLogicValue(EntityField.CREATE_TIME_FILED, sv, "123");

        Assertions.assertEquals(StringValue.class, logicValue.getClass());
        Assertions.assertEquals("test", logicValue.valueToString());
        Assertions.assertEquals("123", logicValue.getAttachment().get());
    }
}