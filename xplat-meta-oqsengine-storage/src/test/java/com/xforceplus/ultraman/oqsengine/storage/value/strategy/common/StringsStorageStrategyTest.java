package com.xforceplus.ultraman.oqsengine.storage.value.strategy.common;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EnumValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * EnumStorageStrategy Tester.
 *
 * @author <Authors name>
 * @version 1.0 03/27/2020
 * @since <pre>Mar 27, 2020</pre>
 */
public class StringsStorageStrategyTest {

    private EnumStorageStrategy storageStrategy = new EnumStorageStrategy();
    private IEntityField field = new Field(1, "enum", FieldType.ENUM);

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testToStorage() throws Exception {

        String[] expectedValues = {
            "one",
            "two",
            "three",
            "1",
            "2",
            "3",
        };

        StringsValue logicValue = new StringsValue(field, expectedValues);

        StorageValue storageValue = storageStrategy.toStorageValue(logicValue);

        Assert.assertEquals(StringStorageValue.class, storageValue.getClass());
        Assert.assertNotNull("Unexpected null value.", storageValue);

        int i = 0;
        while (storageValue != null) {
            Assert.assertEquals(expectedValues[i], storageValue.value());

            i++;
            storageValue = storageValue.next();
        }

        Assert.assertEquals(expectedValues.length, i);
    }

    @Test
    public void testToLogic() throws Exception {
        String[] expectedValues = {
            "one",
            "two",
            "three",
            "1",
            "2",
            "3",
        };
        StorageValue head = null;
        for (int i = 0; i < expectedValues.length; i++) {
            if (head == null) {
                head = new StringStorageValue("F1S" + i, expectedValues[i], false);
                head.locate(0);
            } else {

                head = head.stick(new StringStorageValue("F1S" + i, expectedValues[i], false));
            }
        }

        StringsValue stringsValue = (StringsValue) storageStrategy.toLogicValue(field, head);

        Assert.assertEquals(String.join(",", expectedValues), String.join(",", stringsValue.getValue()));

    }

    @Test
    public void testOneValue() throws Exception {
        String[] expectedValues = {"one"};
        StringsValue logicValue = new StringsValue(field, expectedValues);
        StorageValue storageValue = storageStrategy.toStorageValue(logicValue);

        Assert.assertEquals(0, storageValue.location());
        Assert.assertNull("Unexpected value.", storageValue.next());
        Assert.assertEquals("1S0", storageValue.storageName());
    }


} 
