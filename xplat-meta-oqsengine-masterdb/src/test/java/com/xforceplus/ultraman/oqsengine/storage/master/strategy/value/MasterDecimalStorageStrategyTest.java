package com.xforceplus.ultraman.oqsengine.storage.master.strategy.value;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import java.math.BigDecimal;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DecimalStorageStrategy Tester.
 *
 * @author dongbin
 * @version 1.0 04/08/2020
 * @since <pre>Apr 8, 2020</pre>
 */
public class MasterDecimalStorageStrategyTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void test() throws Exception {
        MasterDecimalStorageStrategy storageStrategy = new MasterDecimalStorageStrategy();

        Assert.assertEquals(FieldType.DECIMAL, storageStrategy.fieldType());
        Assert.assertFalse(storageStrategy.isMultipleStorageValue());

        IEntityField field = new EntityField(1L, "test", FieldType.DECIMAL);
        IValue logicValue = new DecimalValue(field, new BigDecimal("123.23"));
        StorageValue storageValue = storageStrategy.toStorageValue(logicValue);
        Assert.assertEquals(StringStorageValue.class, storageValue.getClass());
        Assert.assertFalse(storageValue.next() != null);
        Assert.assertEquals("123.23", storageValue.value().toString());
        Assert.assertEquals("1S", storageValue.storageName());

        IValue newLogicValue = storageStrategy.toLogicValue(field, storageValue);
        Assert.assertEquals(logicValue, newLogicValue);
    }


} 
