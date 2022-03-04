package com.xforceplus.ultraman.oqsengine.storage.master.mysql.strategy.value;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * DecimalStorageStrategy Tester.
 *
 * @author dongbin
 * @version 1.0 04/08/2020
 * @since <pre>Apr 8, 2020</pre>
 */
public class MasterDecimalStorageStrategyTest {

    @Test
    public void test() throws Exception {
        MasterDecimalStorageStrategy storageStrategy = new MasterDecimalStorageStrategy();

        Assertions.assertEquals(FieldType.DECIMAL, storageStrategy.fieldType());
        Assertions.assertFalse(storageStrategy.isMultipleStorageValue());

        IEntityField field = new EntityField(1L, "test", FieldType.DECIMAL);
        IValue logicValue = new DecimalValue(field, new BigDecimal("123.23"));
        StorageValue storageValue = storageStrategy.toStorageValue(logicValue);
        Assertions.assertEquals(StringStorageValue.class, storageValue.getClass());
        Assertions.assertNull(storageValue.next());
        Assertions.assertEquals("123.23", storageValue.value().toString());
        Assertions.assertEquals("1S", storageValue.storageName());

        IValue newLogicValue = storageStrategy.toLogicValue(field, storageValue);
        Assertions.assertEquals(logicValue, newLogicValue);
    }
} 
