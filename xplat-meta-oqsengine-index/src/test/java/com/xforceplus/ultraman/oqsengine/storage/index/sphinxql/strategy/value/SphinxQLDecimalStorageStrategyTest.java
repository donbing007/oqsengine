package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.value;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import java.math.BigDecimal;
import java.util.Collection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * DecimalStorageStrategy Tester.
 *
 * @author dongbin
 * @version 1.0 03/05/2020
 * @since <pre>Mar 5, 2020</pre>
 */
public class SphinxQLDecimalStorageStrategyTest {

    @Test
    public void testLogicValue() throws Exception {
        SphinxQLDecimalStorageStrategy sphinxQLDecimalStorageStrategy = new SphinxQLDecimalStorageStrategy();

        StorageValue value = new LongStorageValue("test", 12354435L, true);
        value.locate(0);
        value.stick(new LongStorageValue("test", 878783434000000000L, true));

        IValue logicValue =
            sphinxQLDecimalStorageStrategy.toLogicValue(new EntityField(1, "test", FieldType.DECIMAL), value);

        Assertions.assertEquals("12354435.878783434000000000", logicValue.valueToString());
    }

    @Test
    public void testStorageValue() throws Exception {
        SphinxQLDecimalStorageStrategy sphinxQLDecimalStorageStrategy = new SphinxQLDecimalStorageStrategy();
        IValue logicValue = new DecimalValue(
            new EntityField(1, "test", FieldType.DECIMAL), new BigDecimal("123123213213.78887788"));
        StorageValue storageValue = sphinxQLDecimalStorageStrategy.toStorageValue(logicValue);

        Assertions.assertEquals(123123213213L, storageValue.value());
        Assertions.assertEquals(StorageType.LONG, storageValue.type());

        storageValue = storageValue.next();
        Assertions.assertEquals(788877880000000000L, storageValue.value());
        Assertions.assertEquals(StorageType.LONG, storageValue.type());

        logicValue = new DecimalValue(new EntityField(1, "test", FieldType.DECIMAL), new BigDecimal("123.03"));
        storageValue = sphinxQLDecimalStorageStrategy.toStorageValue(logicValue);

        Assertions.assertEquals(123L, storageValue.value());
        Assertions.assertEquals(StorageType.LONG, storageValue.type());
        storageValue = storageValue.next();
        Assertions.assertEquals(30000000000000000L, storageValue.value());
        Assertions.assertEquals(StorageType.LONG, storageValue.type());
    }

    @Test
    public void testStorageNames() throws Exception {
        SphinxQLDecimalStorageStrategy sphinxQLDecimalStorageStrategy = new SphinxQLDecimalStorageStrategy();
        IEntityField field = new EntityField(1, "test", FieldType.DECIMAL);
        Collection<String> storageNames = sphinxQLDecimalStorageStrategy.toStorageNames(field);
        Assertions.assertEquals(2, storageNames.size());
        Assertions.assertArrayEquals(new String[] {"1L0", "1L1"}, storageNames.toArray(new String[0]));
    }

    /**
     * 测试转换来自主库的数据.
     */
    @Test
    public void testConvert() throws Exception {
        SphinxQLDecimalStorageStrategy storageStrategy = new SphinxQLDecimalStorageStrategy();
        StorageValue storageValue = storageStrategy.convertIndexStorageValue("123456S", "789.123");
        StorageValue intStorageValue = storageValue;
        StorageValue decStorageValue = storageValue.next();
        Assertions.assertNotNull(intStorageValue);
        Assertions.assertNotNull(decStorageValue);

        Assertions.assertEquals(789L, intStorageValue.value());
        Assertions.assertEquals(123000000000000000L, decStorageValue.value());

        Assertions.assertEquals(StorageType.LONG, intStorageValue.type());
        Assertions.assertEquals(StorageType.LONG, decStorageValue.type());

        Assertions.assertEquals(0, intStorageValue.location());
        Assertions.assertEquals(1, decStorageValue.location());
    }

}
