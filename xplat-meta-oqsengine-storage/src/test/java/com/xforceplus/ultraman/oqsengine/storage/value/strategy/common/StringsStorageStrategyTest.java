package com.xforceplus.ultraman.oqsengine.storage.value.strategy.common;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * EnumStorageStrategy Tester.
 *
 * @author dongbin
 * @version 1.0 03/27/2020
 * @since <pre>Mar 27, 2020</pre>
 */
public class StringsStorageStrategyTest {

    private StringsStorageStrategy storageStrategy = new StringsStorageStrategy();
    private IEntityField field = new EntityField(1, "enum", FieldType.ENUM);

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

        Assertions.assertEquals(StringStorageValue.class, storageValue.getClass());
        Assertions.assertNotNull(storageValue, "Unexpected null value.");

        int i = 0;
        while (storageValue != null) {
            Assertions.assertEquals(expectedValues[i], storageValue.value());

            i++;
            storageValue = storageValue.next();
        }

        Assertions.assertEquals(expectedValues.length, i);
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

        Assertions.assertEquals(String.join(",", expectedValues), String.join(",", stringsValue.getValue()));

    }

    @Test
    public void testOneValue() throws Exception {
        String[] expectedValues = {"one"};
        StringsValue logicValue = new StringsValue(field, expectedValues);
        StorageValue storageValue = storageStrategy.toStorageValue(logicValue);

        Assertions.assertEquals(0, storageValue.location());
        Assertions.assertNull(storageValue.next(), "Unexpected value.");
        Assertions.assertEquals("1S0", storageValue.storageName());
    }
} 
