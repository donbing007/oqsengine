package com.xforceplus.ultraman.oqsengine.storage.value.strategy.common;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * long 物理值处理策略测试.
 *
 * @author dongbin
 * @version 1.0 04/08/2022
 */
public class LongStorageStrategyTest {

    @Test
    public void testToStorageValue() throws Exception {
        LongValue value = new LongValue(EntityField.CREATE_TIME_FILED, 1000, "123");

        LongStorageStrategy storageStrategy = new LongStorageStrategy();
        StorageValue sv = storageStrategy.toStorageValue(value);

        Assertions.assertEquals(1000L, sv.value());
        Assertions.assertEquals("123", sv.getAttachment().value());
    }

    @Test
    public void testToLogicValue() throws Exception {
        LongStorageValue sv = new LongStorageValue(EntityField.CREATE_TIME_FILED.idString(), 1000, true);
        sv.setAttachment(new StringStorageValue(EntityField.CREATE_TIME_FILED.idString(), "123", true));

        LongStorageStrategy storageStrategy = new LongStorageStrategy();
        IValue logicValue = storageStrategy.toLogicValue(EntityField.CREATE_TIME_FILED, sv, "123");

        Assertions.assertEquals(LongValue.class, logicValue.getClass());
        Assertions.assertEquals(1000L, logicValue.valueToLong());
        Assertions.assertEquals("123", logicValue.getAttachment().get());
    }
}