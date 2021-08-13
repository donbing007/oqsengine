package com.xforceplus.ultraman.oqsengine.storage.master.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.EntityClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * EntityClassHelper Tester.
 *
 * @author dongbin
 * @version 1.0 02/26/2021
 * @since <pre>Feb 26, 2021</pre>
 */
public class EntityClassHelperTest {
    /**
     * Method: buildEntityClassQuerySql(IEntityClass entityClass).
     */
    @Test
    public void testBuildEntityClassQuerySql() throws Exception {

        IEntityClass l0EntityClass = EntityClass.Builder.anEntityClass()
            .withId(1)
            .withLevel(0)
            .withCode("l0")
            .withField(EntityField.Builder.anEntityField()
                .withId(1000)
                .withFieldType(FieldType.LONG)
                .withName("l0-long").build())
            .build();
        IEntityClass l1EntityClass = EntityClass.Builder.anEntityClass()
            .withId(2)
            .withLevel(1)
            .withCode("l1")
            .withFather(l0EntityClass)
            .withField(EntityField.Builder.anEntityField()
                .withId(1001)
                .withFieldType(FieldType.LONG)
                .withName("l1-long").build())
            .build();
        IEntityClass l2EntityClass = EntityClass.Builder.anEntityClass()
            .withId(3)
            .withLevel(2)
            .withCode("l2")
            .withFather(l1EntityClass)
            .withField(EntityField.Builder.anEntityField()
                .withId(1002)
                .withFieldType(FieldType.LONG)
                .withName("l2-long").build())
            .build();

        Assertions.assertEquals("(entityclassl0 = 1)", EntityClassHelper.buildEntityClassQuerySql(l0EntityClass));
        Assertions.assertEquals("(entityclassl0 = 1 AND entityclassl1 = 2)",
            EntityClassHelper.buildEntityClassQuerySql(l1EntityClass));
        Assertions.assertEquals("(entityclassl0 = 1 AND entityclassl1 = 2 AND entityclassl2 = 3)",
            EntityClassHelper.buildEntityClassQuerySql(l2EntityClass));
    }


} 
