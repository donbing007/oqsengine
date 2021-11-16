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
        MockLongStorageValue v1 = new MockLongStorageValue("111", 0L, true);
        v1.locate(0);
        MockLongStorageValue v2 = new MockLongStorageValue("222", 0L, true);
        StorageValue head = v1.stick(v2);
        StorageValue point = head;

        Assertions.assertEquals(point, v1);
        Assertions.assertEquals("111", point.logicName());
        Assertions.assertEquals("111L0", point.storageName());
        Assertions.assertEquals(0, point.location());
        Assertions.assertTrue(point.haveNext());

        point = point.next();

        Assertions.assertEquals("222", point.logicName());
        Assertions.assertEquals("222L1", point.storageName());
        Assertions.assertEquals(1, point.location());
        Assertions.assertFalse(point.haveNext());

        MockLongStorageValue v3 = new MockLongStorageValue("333L4", 0L, false);
        head = head.stick(v3);
        point = head;
        Assertions.assertEquals(point, v1);
        Assertions.assertEquals("111", point.logicName());
        Assertions.assertEquals("111L0", point.storageName());
        Assertions.assertEquals(0, point.location());
        Assertions.assertTrue(point.haveNext());

        point = point.next();

        Assertions.assertEquals("222", point.logicName());
        Assertions.assertEquals("222L1", point.storageName());
        Assertions.assertEquals(1, point.location());
        Assertions.assertTrue(point.haveNext());

        point = point.next();

        Assertions.assertEquals("333", point.logicName());
        Assertions.assertEquals("333L4", point.storageName());
        Assertions.assertEquals(4, point.location());
        Assertions.assertFalse(point.haveNext());


        MockLongStorageValue one = new MockLongStorageValue("111L1", 0L, false);
        MockLongStorageValue two = new MockLongStorageValue("222L0", 0L, false);
        head = one.stick(two);
        Assertions.assertEquals(two, head);
        Assertions.assertTrue(two.haveNext());
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
