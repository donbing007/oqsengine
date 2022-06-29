package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 对象实例测试.
 *
 * @author dongbin
 * @version 0.1 2022/6/16 17:33
 * @since 1.8
 */
public class EntityTest {

    @Test
    public void testSqueezeEmpty() {
        IEntityField f1 = EntityField.Builder.anEntityField()
            .withId(100)
            .withFieldType(FieldType.STRING)
            .build();
        IEntityField f2 = EntityField.Builder.anEntityField()
            .withId(200)
            .withFieldType(FieldType.BOOLEAN)
            .build();

        Entity entity = Entity.Builder.anEntity()
            .withId(1)
            .withValue(new StringValue(f1, "100"))
            .withValue(new EmptyTypedValue(f2))
            .build();
        entity.squeezeEmpty();

        Assertions.assertEquals(1, entity.entityValue().size());
        Assertions.assertTrue(entity.entityValue().getValue(f1).isPresent());
        Assertions.assertFalse(entity.entityValue().getValue(f2).isPresent());
    }
}