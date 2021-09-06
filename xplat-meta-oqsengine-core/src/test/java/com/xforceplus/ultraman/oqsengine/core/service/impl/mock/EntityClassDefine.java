package com.xforceplus.ultraman.oqsengine.core.service.impl.mock;

import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
import java.util.Arrays;
import java.util.Optional;

/**
 * metamanager mock.
 *
 * @author dongbin
 * @version 0.1 2021/3/18 15:12
 * @since 1.8
 */
public class EntityClassDefine {

    static long entityClassId = Long.MAX_VALUE;
    static long fieldId = Long.MAX_VALUE;

    static long l0EntityClassId = entityClassId--;
    static long l1EntityClassId = entityClassId--;
    static long l2EntityClassId = entityClassId--;
    static long mustEntityClassId = entityClassId--;
    static long strongRelationshipClassId = entityClassId--;

    public static IEntityClass strongRelationshipClass;

    //-------------level 0--------------------

    public static IEntityClass l0EntityClass = EntityClass.Builder.anEntityClass()
        .withId(l0EntityClassId)
        .withLevel(0)
        .withCode("l0")
        .withField(EntityField.Builder.anEntityField()
            .withId(fieldId--)
            .withFieldType(FieldType.LONG)
            .withName("l0-long")
            .withConfig(FieldConfig.build().searchable(true)).build())
        .withField(EntityField.Builder.anEntityField()
            .withId(fieldId--)
            .withFieldType(FieldType.STRING)
            .withName("l0-string")
            .withConfig(FieldConfig.build().searchable(true).fuzzyType(FieldConfig.FuzzyType.SEGMENTATION)).build())
        .withField(EntityField.Builder.anEntityField()
            .withId(fieldId--)
            .withFieldType(FieldType.STRINGS)
            .withName("l0-strings")
            .withConfig(FieldConfig.build().searchable(true)).build())
        .build();

    //-------------level 1--------------------
    public static IEntityClass l1EntityClass = EntityClass.Builder.anEntityClass()
        .withId(l1EntityClassId)
        .withLevel(1)
        .withCode("l1")
        .withField(EntityField.Builder.anEntityField()
            .withId(fieldId--)
            .withFieldType(FieldType.LONG)
            .withName("l1-long")
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withLen(3)
                    .withSearchable(true).build()
            ).build())
        .withField(EntityField.Builder.anEntityField()
            .withId(fieldId--)
            .withFieldType(FieldType.STRING)
            .withName("l1-string")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                .withSearchable(true)
                .withFuzzyType(FieldConfig.FuzzyType.WILDCARD)
                .withWildcardMinWidth(3).withWildcardMaxWidth(7).build()).build())
        .withFather(l0EntityClass)
        .build();

    //-------------level 2--------------------
    public static IEntityClass l2EntityClass = EntityClass.Builder.anEntityClass()
        .withId(l2EntityClassId)
        .withLevel(2)
        .withCode("l2")
        .withField(EntityField.Builder.anEntityField()
            .withId(fieldId--)
            .withFieldType(FieldType.STRING)
            .withName("l2-string")
            .withConfig(FieldConfig.build().searchable(true)).build())
        .withField(EntityField.Builder.anEntityField()
            .withId(fieldId--)
            .withFieldType(FieldType.DATETIME)
            .withName("l2-time")
            .withConfig(FieldConfig.build().searchable(true)).build())
        .withField(EntityField.Builder.anEntityField()
            .withId(fieldId--)
            .withFieldType(FieldType.ENUM)
            .withName("l2-enum")
            .withConfig(FieldConfig.build().searchable(true)).build())
        .withField(EntityField.Builder.anEntityField()
            .withId(fieldId--)
            .withFieldType(FieldType.DECIMAL)
            .withName("l2-dec")
            .withConfig(FieldConfig.build().searchable(true)).build())
        .withFather(l1EntityClass)
        .withRelations(
            Arrays.asList(
                Relationship.Builder.anRelationship()
                    .withId(0)
                    .withLeftEntityClassId(l2EntityClassId)
                    .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                    .withRightEntityClassId(strongRelationshipClassId)
                    .withRightEntityClassLoader(id -> Optional.of(strongRelationshipClass))
                    .withBelongToOwner(true)
                    .withStrong(true)
                    .withIdentity(true)
                    .build()
            )
        )
        .build();

    public static IEntityClass mustEntityClass = EntityClass.Builder.anEntityClass()
        .withId(mustEntityClassId)
        .withCode("must")
        .withField(
            EntityField.Builder.anEntityField()
                .withId(fieldId--)
                .withFieldType(FieldType.STRING)
                .withName("not-must-field")
                .withConfig(
                    FieldConfig.Builder.anFieldConfig()
                        .withRequired(false).build()
                ).build()
        )
        .withField(
            EntityField.Builder.anEntityField()
                .withId(fieldId--)
                .withFieldType(FieldType.STRING)
                .withName("must-field")
                .withConfig(
                    FieldConfig.Builder.anFieldConfig()
                        .withRequired(true).build()
                ).build()
        ).build();

    static {
        // 和l2 class 强的一对多关系,l2是关系持有者.
        strongRelationshipClass = EntityClass.Builder.anEntityClass()
            .withId(strongRelationshipClassId)
            .withCode("strongRelationshipClass")
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(fieldId--)
                    .withFieldType(FieldType.STRING)
                    .withName("lookup-l2-string")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withCalculation(
                                Lookup.Builder.anLookup()
                                    .withClassId(l2EntityClass.id())
                                    .withFieldId(l2EntityClass.field("l2-string").get().id()).build()
                            ).build()
                    ).build()
            )
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(0)
                        .withLeftEntityClassId(strongRelationshipClassId)
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withRightEntityClassId(l2EntityClassId)
                        .withRightEntityClassLoader(id -> Optional.of(l2EntityClass))
                        .withBelongToOwner(false)
                        .withStrong(true)
                        .withIdentity(true)
                        .build()
                )
            )
            .build();
    }

    /**
     * 获取mock的metamanager.
     *
     * @return metamanager实例.
     */
    public static MetaManager getMockMetaManager() {
        MockMetaManager metaManager = new MockMetaManager();
        metaManager.addEntityClass(l2EntityClass);
        metaManager.addEntityClass(mustEntityClass);
        return metaManager;
    }
}
