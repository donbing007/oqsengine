package com.xforceplus.ultraman.oqsengine.storage.value;

import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * AbstractStorageValue Tester.
 *
 * @author dongbin
 * @version 1.0 03/04/2020
 * @since <pre>Mar 4, 2020</pre>
 */
public class TestAbstractStorageValueTest {

    // 测试物理名称构造.
    @Test
    public void testStorageName() throws Exception {
        String logicName = "123456";
        MockLongStorageValue instance = new MockLongStorageValue(logicName, 0L, true);
        Assertions.assertEquals(logicName, instance.logicName());
        Assertions.assertEquals("123456L", instance.storageName());
    }

    @Test
    public void testLogicName() throws Exception {
        String storageName = "123456L1";
        MockLongStorageValue instance = new MockLongStorageValue(storageName, 0L, false);
        Assertions.assertEquals("123456", instance.logicName());
        Assertions.assertEquals("123456L1", instance.storageName());
        Assertions.assertEquals(1, instance.location());

        storageName = "123456L";
        instance = new MockLongStorageValue(storageName, 0L, false);
        Assertions.assertEquals("123456", instance.logicName());
        Assertions.assertEquals("123456L", instance.storageName());
        Assertions.assertEquals(StorageValue.NOT_LOCATION, instance.location());
    }

    @Test
    public void testStick() throws Exception {
        MockLongStorageValue v1 = new MockLongStorageValue("1", 1L, true);
        MockLongStorageValue v2 = new MockLongStorageValue("2", 2L, true);
        MockLongStorageValue v3 = new MockLongStorageValue("3", 3L, true);
        MockLongStorageValue v4 = new MockLongStorageValue("4", 4L, true);

        // 使用中间结点粘贴.
        v1.stick(v2);
        v2.stick(v3);
        v3.stick(v4);

        Assertions.assertEquals(0, v1.location());
        Assertions.assertEquals(1, v2.location());
        Assertions.assertEquals(2, v3.location());
        Assertions.assertEquals(3, v4.location());

        StorageValue point = v1;
        Assertions.assertEquals(v1.value(), point.value());
        point = point.next();
        Assertions.assertEquals(v2.value(), point.value());
        point = point.next();
        Assertions.assertEquals(v3.value(), point.value());
        point = point.next();
        Assertions.assertEquals(v4.value(), point.value());

        // 始终使用首结点来粘贴.
        v1 = new MockLongStorageValue("1", 1L, true);
        v2 = new MockLongStorageValue("2", 2L, true);
        v3 = new MockLongStorageValue("3", 3L, true);
        v4 = new MockLongStorageValue("4", 4L, true);
        v1.stick(v2);
        v1.stick(v3);
        v1.stick(v4);

        Assertions.assertEquals(0, v1.location());
        Assertions.assertEquals(1, v2.location());
        Assertions.assertEquals(2, v3.location());
        Assertions.assertEquals(3, v4.location());

        point = v1;
        Assertions.assertEquals(v1.value(), point.value());
        point = point.next();
        Assertions.assertEquals(v2.value(), point.value());
        point = point.next();
        Assertions.assertEquals(v3.value(), point.value());
        point = point.next();
        Assertions.assertEquals(v4.value(), point.value());

    }

    @Test
    public void testGroupName() throws Exception {
        MockLongStorageValue v1 = new MockLongStorageValue("111", 0L, true);
        Assertions.assertEquals("111L", v1.groupStorageName());
    }

    @Test
    public void testShortStorageName() throws Exception {
        MockLongStorageValue v = new MockLongStorageValue("1", 0L, true);
        ShortStorageName shortStorageName = v.shortStorageName();
        Assertions.assertEquals("1L", shortStorageName.toString());
        Assertions.assertEquals("1", shortStorageName.getPrefix());
        Assertions.assertEquals("L", shortStorageName.getSuffix());


        v = new MockLongStorageValue("1000000000000000000", 0L, true);
        shortStorageName = v.shortStorageName();
        Assertions.assertEquals("7lieexzx4kxsL", shortStorageName.toString());
        Assertions.assertEquals("7lieex", shortStorageName.getPrefix());
        Assertions.assertEquals("zx4kxsL", shortStorageName.getSuffix());
    }

    @Test
    public void testStorageType() throws Exception {
        MockLongStorageValue v = new MockLongStorageValue("1", 0L, true);
        Assertions.assertEquals(StorageType.LONG, v.type());

        v = new MockLongStorageValue("7lieexzx4kxsL", 0L, false);
        Assertions.assertEquals(StorageType.LONG, v.type());

        v = new MockLongStorageValue("7lieexzx4kxsL1", 0L, false);
        Assertions.assertEquals(StorageType.LONG, v.type());
    }

    static class MockLongStorageValue extends AbstractStorageValue<Long> {

        public MockLongStorageValue(String name, Long value, boolean logicName) {
            super(name, value, logicName);
        }
    }

} 
