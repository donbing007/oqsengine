package com.xforceplus.ultraman.oqsengine.metadata.mock;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * metaManager 的mock测试.
 *
 * @author dongbin
 * @version 0.1 2021/04/14 16:09
 * @since 1.8
 */
public class MockMetaManagerTest {

    @Test
    public void testMultiVersion() throws Exception {
        MockMetaManager metaManager = new MockMetaManager();

        metaManager.addEntityClass(
            EntityClass.Builder.anEntityClass()
                .withId(1)
                .withVersion(0)
                .build()
        );

        metaManager.addEntityClass(
            EntityClass.Builder.anEntityClass()
                .withId(1)
                .withVersion(1)
                .build()
        );

        metaManager.addEntityClass(
            EntityClass.Builder.anEntityClass()
                .withId(1)
                .withVersion(6)
                .build()
        );

        IEntityClass targetEntityClass = metaManager.load(1).orElse(null);
        Assertions.assertNotNull(targetEntityClass);
        Assertions.assertEquals(1, targetEntityClass.id());
        Assertions.assertEquals(6, targetEntityClass.version());
    }

    @Test
    public void testFamily() throws Exception {
        MockMetaManager metaManager = new MockMetaManager();

        IEntityClass l0 = EntityClass.Builder.anEntityClass()
            .withId(1)
            .withLevel(0)
            .build();
        IEntityClass l1 = EntityClass.Builder.anEntityClass()
            .withId(2)
            .withLevel(1)
            .withFather(l0)
            .build();
        IEntityClass l2 = EntityClass.Builder.anEntityClass()
            .withId(3)
            .withLevel(2)
            .withFather(l1)
            .build();

        metaManager.addEntityClass(l2);

        Assertions.assertTrue(metaManager.load(1).isPresent());
        Assertions.assertTrue(metaManager.load(2).isPresent());
        Assertions.assertTrue(metaManager.load(3).isPresent());
    }

    @Test
    public void testOneVersion() throws Exception {
        MockMetaManager metaManager = new MockMetaManager();

        metaManager.addEntityClass(
            EntityClass.Builder.anEntityClass()
                .withId(1)
                .withVersion(0)
                .build()
        );
        Assertions.assertTrue(metaManager.load(1).isPresent());
    }
}