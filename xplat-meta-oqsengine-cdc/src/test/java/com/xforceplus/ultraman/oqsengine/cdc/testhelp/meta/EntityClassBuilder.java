package com.xforceplus.ultraman.oqsengine.cdc.testhelp.meta;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassType;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class EntityClassBuilder {


    // level 1
    public static final IEntityClass ENTITY_CLASS_0 =
        EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE)
            .withType(EntityClassType.DYNAMIC)
            .withVersion(1)
            .withCode("c0")
            .withFields(
                Arrays.asList(EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD)
            ).build();

    // level 2
    public static final IEntityClass ENTITY_CLASS_1 =
        EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE - 1)
            .withType(EntityClassType.DYNAMIC)
            .withVersion(1)
            .withCode("c1")
            .withFather(ENTITY_CLASS_0)
            .withFields(
                Arrays.asList(EntityFieldRepo.BOOL_FIELD, EntityFieldRepo.DATE_TIME_FIELD)
            ).build();

    // level 3
    public static final IEntityClass ENTITY_CLASS_2 =
        EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE - 2)
            .withType(EntityClassType.DYNAMIC)
            .withVersion(1)
            .withCode("c2")
            .withFather(ENTITY_CLASS_1)
            .withFields(
                Arrays.asList(EntityFieldRepo.DECIMAL_FIELD, EntityFieldRepo.STRINGS_FIELD)
            ).build();

    //  static
    public static final IEntityClass ENTITY_CLASS_STATIC =
        EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE - 3)
            .withType(EntityClassType.STATIC)
            .withVersion(1)
            .withCode("code-static")
            .withFields(
                Arrays.asList(EntityFieldRepo.LONG_FIELD, EntityFieldRepo.STRING_FIELD, EntityFieldRepo.BOOL_FIELD,
                    EntityFieldRepo.DATE_TIME_FIELD, EntityFieldRepo.DECIMAL_FIELD, EntityFieldRepo.ENUM_FIELD)
            ).build();

    public static Map<Long, IEntityClass> entityClassMap = new HashMap<>();

    public static IEntityClass getEntityClass(long id) {
        return entityClassMap.get(id);
    }

    static {
        entityClassMap.put(ENTITY_CLASS_0.id(), ENTITY_CLASS_0);
        entityClassMap.put(ENTITY_CLASS_1.id(), ENTITY_CLASS_1);
        entityClassMap.put(ENTITY_CLASS_2.id(), ENTITY_CLASS_2);
        entityClassMap.put(ENTITY_CLASS_STATIC.id(), ENTITY_CLASS_STATIC);
    }
}
