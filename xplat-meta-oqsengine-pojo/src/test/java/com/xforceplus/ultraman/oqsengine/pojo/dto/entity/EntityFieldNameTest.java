package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import java.sql.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 字段名称测试.
 *
 * @author dongbin
 * @version 0.1 2021/06/24 15:35
 * @since 1.8
 */
public class EntityFieldNameTest {

    private IEntityField dynamicField = EntityField.Builder.anEntityField()
        .withId(4005)
        .withFieldType(FieldType.ENUM)
        .withName("fieldName")
        .withConfig(FieldConfig.Builder.anFieldConfig().withJdbcType(Types.NULL).withSearchable(true).build())
        .build();
    private IEntityField originalRelationField = EntityField.Builder.anEntityField()
        .withId(4006)
        .withFieldType(FieldType.DATETIME)
        .withName("relation.id")
        .withConfig(FieldConfig.Builder.anFieldConfig().withJdbcType(Types.BIGINT).withSearchable(true).build())
        .build();
    private IEntityField originalField = EntityField.Builder.anEntityField()
        .withId(4006)
        .withFieldType(FieldType.DATETIME)
        .withName("createtime.time")
        .withConfig(FieldConfig.Builder.anFieldConfig().withJdbcType(Types.BIGINT).withSearchable(true).build())
        .build();

    @Test
    public void testDynamicField() {
        EntityFieldName entityFieldName = new EntityFieldName(dynamicField);
        Assertions.assertEquals("fieldName", entityFieldName.dynamicName());
        Assertions.assertFalse(entityFieldName.originalName().isPresent());
    }

    @Test
    public void testOriginalField() {
        EntityFieldName entityFieldName = new EntityFieldName(originalRelationField);
        Assertions.assertEquals("relation.id", entityFieldName.dynamicName());
        Assertions.assertTrue(entityFieldName.originalName().isPresent());
        Assertions.assertEquals("relation_id", entityFieldName.originalName().get());

        entityFieldName = new EntityFieldName(originalField);
        Assertions.assertEquals("createtime.time", entityFieldName.dynamicName());
        Assertions.assertTrue(entityFieldName.originalName().isPresent());
        Assertions.assertEquals("createtime.time", entityFieldName.originalName().get());
    }
}