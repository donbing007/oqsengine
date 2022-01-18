package com.xforceplus.ultraman.oqsengine.cdc;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * desc :.
 * name : EntityClassBuilder
 *
 * @author : xujia 2021/3/9
 * @since : 1.8
 */
public class EntityClassBuilder {

    public static final IEntityField LONG_FIELD =
        EntityField.Builder
            .anEntityField()
            .withId(1)
            .withName("long")
            .withFieldType(FieldType.LONG)
            .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
            .build();

    public static final IEntityField STRING_FIELD =
        EntityField.Builder
            .anEntityField()
            .withId(2)
            .withName("string")
            .withFieldType(FieldType.STRING)
            .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
            .build();

    public static final IEntityField BOOL_FIELD =
        EntityField.Builder
            .anEntityField()
            .withId(3)
            .withName("bool")
            .withFieldType(FieldType.BOOLEAN)
            .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
            .build();

    public static final IEntityField DATE_TIME_FIELD =
        EntityField.Builder
            .anEntityField()
            .withId(4)
            .withName("datetime")
            .withFieldType(FieldType.DATETIME)
            .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
            .build();

    public static final IEntityField DECIMAL_FIELD =
        EntityField.Builder
            .anEntityField()
            .withId(5)
            .withName("decimal")
            .withFieldType(FieldType.DECIMAL)
            .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
            .build();

    public static final IEntityField ENUM_FIELD =
        EntityField.Builder
            .anEntityField()
            .withId(6)
            .withName("enum")
            .withFieldType(FieldType.ENUM)
            .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
            .build();


    public static final IEntityField STRINGS_FIELD =
        EntityField.Builder
            .anEntityField()
            .withId(7)
            .withName("strings")
            .withFieldType(FieldType.STRINGS)
            .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
            .build();

    // level 1
    public static final IEntityClass ENTITY_CLASS_0 =
        EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE)
            .withVersion(1)
            .withCode("c0")
            .withFields(
                Arrays.asList(LONG_FIELD, STRING_FIELD)
            ).build();

    // level 2
    public static final IEntityClass ENTITY_CLASS_1 =
        EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE - 1)
            .withVersion(1)
            .withCode("c1")
            .withFather(ENTITY_CLASS_0)
            .withFields(
                Arrays.asList(BOOL_FIELD, DATE_TIME_FIELD)
            ).build();

    // level 3
    public static final IEntityClass ENTITY_CLASS_2 =
        EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE - 2)
            .withVersion(1)
            .withCode("c2")
            .withFather(ENTITY_CLASS_1)
            .withFields(
                Arrays.asList(DECIMAL_FIELD, STRINGS_FIELD)
            ).build();

    public static Map<Long, IEntityClass> entityClassMap = new HashMap<>();

    public static IEntityClass getEntityClass(long id) {
        return entityClassMap.get(id);
    }

    static {
        entityClassMap.put(ENTITY_CLASS_0.id(), ENTITY_CLASS_0);
        entityClassMap.put(ENTITY_CLASS_1.id(), ENTITY_CLASS_1);
        entityClassMap.put(ENTITY_CLASS_2.id(), ENTITY_CLASS_2);
    }
}