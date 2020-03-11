package com.xforceplus.ultraman.oqsengine.storage.value;

import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.mockito.Mock;

/**
 * AbstractStorageValue Tester.
 *
 * @author <Authors name>
 * @version 1.0 03/04/2020
 * @since <pre>Mar 4, 2020</pre>
 */
public class AbstractStorageValueTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    // 测试物理名称构造.
    @Test
    public void testStorageName() throws Exception {
        String logicName = "123456";
        MockStorageValue instance = new MockStorageValue(logicName, 0L, true);
        Assert.assertEquals(logicName, instance.logicName());
        Assert.assertEquals("123456L", instance.storageName());
    }

    @Test
    public void testLogicName() throws Exception {
        String storageName = "123456L1";
        MockStorageValue instance = new MockStorageValue(storageName, 0L, false);
        Assert.assertEquals("123456", instance.logicName());
        Assert.assertEquals("123456L1", instance.storageName());
        Assert.assertEquals(1, instance.location());

        storageName = "123456L";
        instance = new MockStorageValue(storageName, 0L, false);
        Assert.assertEquals("123456", instance.logicName());
        Assert.assertEquals("123456L", instance.storageName());
        Assert.assertEquals(StorageValue.NOT_LOCATION, instance.location());
    }

    @Test
    public void testStick() throws Exception {
        MockStorageValue v1 = new MockStorageValue("111", 0L, true);
        v1.locate(0);
        MockStorageValue v2 = new MockStorageValue("222", 0L, true);
        StorageValue head = v1.stick(v2);
        StorageValue point = head;

        Assert.assertEquals(point, v1);
        Assert.assertEquals("111", point.logicName());
        Assert.assertEquals("111L0", point.storageName());
        Assert.assertEquals(0, point.location());
        Assert.assertTrue(point.haveNext());

        point = point.next();

        Assert.assertEquals("222", point.logicName());
        Assert.assertEquals("222L1", point.storageName());
        Assert.assertEquals(1, point.location());
        Assert.assertFalse(point.haveNext());

        MockStorageValue v3 = new MockStorageValue("333L4", 0L, false);
        head = head.stick(v3);
        point = head;
        Assert.assertEquals(point, v1);
        Assert.assertEquals("111", point.logicName());
        Assert.assertEquals("111L0", point.storageName());
        Assert.assertEquals(0, point.location());
        Assert.assertTrue(point.haveNext());

        point = point.next();

        Assert.assertEquals("222", point.logicName());
        Assert.assertEquals("222L1", point.storageName());
        Assert.assertEquals(1, point.location());
        Assert.assertTrue(point.haveNext());

        point = point.next();

        Assert.assertEquals("333", point.logicName());
        Assert.assertEquals("333L4", point.storageName());
        Assert.assertEquals(4, point.location());
        Assert.assertFalse(point.haveNext());



        MockStorageValue one = new MockStorageValue("111L1", 0L, false);
        MockStorageValue two = new MockStorageValue("222L0", 0L, false);
        head = one.stick(two);
        Assert.assertEquals(two, head);
        Assert.assertTrue(two.haveNext());
    }

    static class MockStorageValue extends AbstractStorageValue<Long> {

        public MockStorageValue(String name, Long value, boolean logicName) {
            super(name, value, logicName);
        }

        @Override
        public StorageType type() {
            return StorageType.LONG;
        }
    }

} 
