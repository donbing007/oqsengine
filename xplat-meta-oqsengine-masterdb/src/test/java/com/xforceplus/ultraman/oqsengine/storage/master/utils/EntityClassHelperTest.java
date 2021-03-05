package com.xforceplus.ultraman.oqsengine.storage.master.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsEntityClass;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * EntityClassHelper Tester.
 *
 * @author <Authors name>
 * @version 1.0 02/26/2021
 * @since <pre>Feb 26, 2021</pre>
 */
public class EntityClassHelperTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: buildEntityClassQuerySql(IEntityClass entityClass)
     */
    @Test
    public void testBuildEntityClassQuerySql() throws Exception {

        IEntityClass l0EntityClass = OqsEntityClass.Builder.anEntityClass()
            .withId(1)
            .withLevel(0)
            .withCode("l0")
            .withField(EntityField.Builder.anEntityField()
                .withId(1000)
                .withFieldType(FieldType.LONG)
                .withName("l0-long").build())
            .build();
        IEntityClass l1EntityClass = OqsEntityClass.Builder.anEntityClass()
            .withId(2)
            .withLevel(1)
            .withCode("l1")
            .withFather(l0EntityClass)
            .withField(EntityField.Builder.anEntityField()
                .withId(1001)
                .withFieldType(FieldType.LONG)
                .withName("l1-long").build())
            .build();
        IEntityClass l2EntityClass = OqsEntityClass.Builder.anEntityClass()
            .withId(3)
            .withLevel(2)
            .withCode("l2")
            .withFather(l1EntityClass)
            .withField(EntityField.Builder.anEntityField()
                .withId(1002)
                .withFieldType(FieldType.LONG)
                .withName("l2-long").build())
            .build();

        Assert.assertEquals("(entityclassl0 = 1)", EntityClassHelper.buildEntityClassQuerySql(l0EntityClass));
        Assert.assertEquals("(entityclassl0 = 1 AND entityclassl1 = 2)",
            EntityClassHelper.buildEntityClassQuerySql(l1EntityClass));
        Assert.assertEquals("(entityclassl0 = 1 AND entityclassl1 = 2 AND entityclassl2 = 3)",
            EntityClassHelper.buildEntityClassQuerySql(l2EntityClass));
    }


} 
