//package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;
//
//import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
//import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
//import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
///**
// * EntityValue Tester.
// *
// * @author <Authors name>
// * @version 1.0 04/20/2020
// * @since <pre>Apr 20, 2020</pre>
// */
//public class EntityValueTest {
//
//    @Before
//    public void before() throws Exception {
//    }
//
//    @After
//    public void after() throws Exception {
//    }
//
//    @Test
//    public void testEquals() throws Exception {
//        EntityValue one = new EntityValue(1);
//        one.addValue(new StringValue(new EntityField(1, "c1", FieldType.STRING), "v1"));
//
//        EntityValue two = new EntityValue(1);
//        two.addValue(new StringValue(new EntityField(1, "c1", FieldType.STRING), "v1"));
//
//        Assert.assertTrue(one.equals(two));
//
//        one.addValue(new LongValue(new EntityField(2, "c2", FieldType.LONG), 2L));
//        Assert.assertFalse(one.equals(two));
//
//        one.clear();
//        two.clear();
//        one.addValue(new StringValue(new EntityField(1, "c1", FieldType.STRING), "v1"));
//        two.addValue(new LongValue(new EntityField(1, "c1", FieldType.STRING), 2L));
//        Assert.assertFalse(one.equals(two));
//
//    }
//
//}
