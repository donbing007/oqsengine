package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value.SphinxQLDecimalStorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * DecimalStorageStrategy Tester.
 *
 * @author <Authors name>
 * @version 1.0 03/05/2020
 * @since <pre>Mar 5, 2020</pre>
 */
public class SphinxQLDecimalStorageStrategyTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testLogicValue() throws Exception {
        SphinxQLDecimalStorageStrategy sphinxQLDecimalStorageStrategy = new SphinxQLDecimalStorageStrategy();

        StorageValue value = new LongStorageValue("test", 12354435L, true);
        value.locate(0);
        value.stick(new LongStorageValue("test", 878783434L, true));

        IValue logicValue =
            sphinxQLDecimalStorageStrategy.toLogicValue(new EntityField(1, "test", FieldType.DECIMAL), value);

        Assert.assertEquals("12354435.878783434", logicValue.valueToString());

    }

    @Test
    public void testStorageValue() throws Exception {
        SphinxQLDecimalStorageStrategy sphinxQLDecimalStorageStrategy = new SphinxQLDecimalStorageStrategy();
        IValue logicValue = new DecimalValue(
            new EntityField(1, "test", FieldType.DECIMAL), new BigDecimal("123123213213.78887788"));
        StorageValue storageValue = sphinxQLDecimalStorageStrategy.toStorageValue(logicValue);

        Assert.assertEquals(123123213213L, storageValue.value());
        Assert.assertEquals(StorageType.LONG, storageValue.type());

        storageValue = storageValue.next();
        Assert.assertEquals(78887788L, storageValue.value());
        Assert.assertEquals(StorageType.LONG, storageValue.type());
    }

    @Test
    public void testStorageNames() throws Exception {
        SphinxQLDecimalStorageStrategy sphinxQLDecimalStorageStrategy = new SphinxQLDecimalStorageStrategy();
        IEntityField field = new EntityField(1, "test", FieldType.DECIMAL);
        Collection<String> storageNames = sphinxQLDecimalStorageStrategy.toStorageNames(field);
        Assert.assertEquals(2, storageNames.size());
        Assert.assertArrayEquals(new String[] {"1L0", "1L1"}, storageNames.toArray(new String[0]));
    }

} 