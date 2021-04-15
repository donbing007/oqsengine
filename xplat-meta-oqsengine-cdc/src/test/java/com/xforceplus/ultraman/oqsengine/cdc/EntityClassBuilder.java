package com.xforceplus.ultraman.oqsengine.cdc;

import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * desc :
 * name : EntityClassBuilder
 *
 * @author : xujia
 * date : 2021/3/9
 * @since : 1.8
 */
public class EntityClassBuilder implements MetaManager {

    public static Map<Long, IEntityClass> entityClassMap = new HashMap<>();

    public static final IEntityField stringField = new EntityField(1, "string", FieldType.STRING, FieldConfig.build().searchable(true), null, null);
    public static final IEntityField longField = new EntityField(2, "long", FieldType.LONG, FieldConfig.build().searchable(true), null, null);
    public static final IEntityField boolField = new EntityField(3, "bool", FieldType.BOOLEAN, FieldConfig.build().searchable(true), null, null);
    public static final IEntityField dateTimeField = new EntityField(4, "datetime", FieldType.DATETIME, FieldConfig.build().searchable(true), null, null);
    public static final IEntityField decimalField = new EntityField(5, "decimal", FieldType.DECIMAL, FieldConfig.build().searchable(true), null, null);
    public static final IEntityField stringsField = new EntityField(6, "strings", FieldType.STRINGS, FieldConfig.build().searchable(true), null, null);

    // level 1
    public static IEntityClass entityClass0 =
            EntityClass.Builder.anEntityClass()
                    .withId(Long.MAX_VALUE)
                    .withVersion(1)
                    .withCode("c0")
                    .withFields(
                            Arrays.asList(longField, stringField)
                    ).build();

    // level 2
    public static IEntityClass entityClass1 =
            EntityClass.Builder.anEntityClass()
                    .withId(Long.MAX_VALUE - 1)
                    .withVersion(1)
                    .withCode("c1")
                    .withFather(entityClass0)
                    .withFields(
                            Arrays.asList(boolField, dateTimeField)
                    ).build();
    // level 3
    public static IEntityClass entityClass2 =
            EntityClass.Builder.anEntityClass()
                    .withId(Long.MAX_VALUE - 2)
                    .withVersion(1)
                    .withCode("c2")
                    .withFather(entityClass1)
                    .withFields(
                            Arrays.asList(decimalField, stringsField)
                    ).build();

    static {
        entityClassMap.put(entityClass0.id(), entityClass0);
        entityClassMap.put(entityClass1.id(), entityClass1);
        entityClassMap.put(entityClass2.id(), entityClass2);

    }

    public static IEntityClass getEntityClass(long id) {
        return entityClassMap.get(id);
    }

    @Override
    public Optional<IEntityClass> load(long id) {
        return Optional.of(getEntityClass(id));
    }

    @Override
    public Optional<IEntityClass> loadHistory(long id, int version) {
        return Optional.empty();
    }

    @Override
    public int need(String appId, String env) {
        return 0;
    }

    @Override
    public void invalidateLocal() {

    }
}
