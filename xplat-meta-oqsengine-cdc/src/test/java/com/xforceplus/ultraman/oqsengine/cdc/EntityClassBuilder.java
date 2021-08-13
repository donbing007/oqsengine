package com.xforceplus.ultraman.oqsengine.cdc;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
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

    public static final IEntityField STRING_FIELD =
        new EntityField(1, "string", FieldType.STRING, FieldConfig.build().searchable(true), null, null);
    public static final IEntityField LONG_FIELD =
        new EntityField(2, "long", FieldType.LONG, FieldConfig.build().searchable(true), null, null);
    public static final IEntityField BOOL_FIELD =
        new EntityField(3, "bool", FieldType.BOOLEAN, FieldConfig.build().searchable(true), null, null);
    public static final IEntityField DATE_TIME_FIELD =
        new EntityField(4, "datetime", FieldType.DATETIME, FieldConfig.build().searchable(true), null, null);
    public static final IEntityField DECIMAL_FIELD =
        new EntityField(5, "decimal", FieldType.DECIMAL, FieldConfig.build().searchable(true), null, null);
    public static final IEntityField STRINGS_FIELD =
        new EntityField(6, "strings", FieldType.STRINGS, FieldConfig.build().searchable(true), null, null);

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