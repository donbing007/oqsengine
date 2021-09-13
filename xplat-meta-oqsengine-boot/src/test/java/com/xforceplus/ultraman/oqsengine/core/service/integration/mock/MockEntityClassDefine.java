package com.xforceplus.ultraman.oqsengine.core.service.integration.mock;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;

/**
 * MetaManager的Mock实现,为了单元测试准备.
 *
 * @author dongbin
 * @version 0.1 2021/3/18 15:12
 * @since 1.8
 */
@Disabled("explanation")
public class MockEntityClassDefine {

    private static long DRIVCER_ID_FEILD_ID = Long.MAX_VALUE;
    /**
     * 类型标识的开始值,依次递减.
     */
    private static long baseClassId = Long.MAX_VALUE;
    /**
     * 字段的标识开始值,依次递减.
     */
    private static long baseFieldId = Long.MAX_VALUE - 1;

    private static long l0EntityClassId = baseClassId--;
    private static long l1EntityClassId = baseClassId--;
    private static long l2EntityClassId = baseClassId--;
    private static long driverEntityClassId = baseClassId--;
    private static long lookupEntityClassId = baseClassId--;


    public static IEntityClass l0EntityClass;
    public static IEntityClass l1EntityClass;
    public static IEntityClass l2EntityClass;
    public static IEntityClass driverEntityClass;
    public static IEntityClass lookupEntityClass;

    static {
        l0EntityClass = EntityClass.Builder.anEntityClass()
            .withId(l0EntityClassId)
            .withLevel(0)
            .withCode("l0")
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(baseFieldId--)
                    .withFieldType(FieldType.LONG)
                    .withName("l0-long")
                    .withConfig(FieldConfig.Builder.anFieldConfig().withLen(100).withSearchable(true).build()).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(baseFieldId--)
                .withFieldType(FieldType.STRING)
                .withName("l0-string")
                .withConfig(FieldConfig
                    .Builder.anFieldConfig().withLen(128)
                    .withSearchable(true)
                    .withFuzzyType(FieldConfig.FuzzyType.SEGMENTATION)
                    .build())
                .build()
            )
            .withField(EntityField.Builder.anEntityField()
                .withId(baseFieldId--)
                .withFieldType(FieldType.STRINGS)
                .withName("l0-strings")
                .withConfig(FieldConfig
                    .Builder.anFieldConfig().withLen(128)
                    .withSearchable(true)
                    .withFuzzyType(FieldConfig.FuzzyType.SEGMENTATION)
                    .build())
                .build()
            )
            .build();

        l1EntityClass = EntityClass.Builder.anEntityClass()
            .withId(l1EntityClassId)
            .withLevel(1)
            .withCode("l1")
            .withField(EntityField.Builder.anEntityField()
                .withId(baseFieldId--)
                .withFieldType(FieldType.LONG)
                .withName("l1-long")
                .withConfig(FieldConfig.Builder.anFieldConfig().withLen(100).withSearchable(true).build()).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(baseFieldId--)
                .withFieldType(FieldType.STRING)
                .withName("l1-string")
                .withConfig(FieldConfig.Builder.anFieldConfig()
                    .withSearchable(true)
                    .withFuzzyType(FieldConfig.FuzzyType.WILDCARD)
                    .withWildcardMinWidth(3).withWildcardMaxWidth(7).build()).build())
            .withFather(l0EntityClass)
            .build();

        l2EntityClass = EntityClass.Builder.anEntityClass()
            .withId(l2EntityClassId)
            .withLevel(2)
            .withCode("l2")
            .withField(EntityField.Builder.anEntityField()
                .withId(baseFieldId--)
                .withFieldType(FieldType.STRING)
                .withName("l2-string")
                .withConfig(FieldConfig.Builder.anFieldConfig().withLen(100).withSearchable(true).build()).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(baseFieldId--)
                .withFieldType(FieldType.DATETIME)
                .withName("l2-time")
                .withConfig(FieldConfig.Builder.anFieldConfig().withLen(Integer.MAX_VALUE).withSearchable(true).build())
                .build())
            .withField(EntityField.Builder.anEntityField()
                .withId(baseFieldId--)
                .withFieldType(FieldType.ENUM)
                .withName("l2-enum")
                .withConfig(FieldConfig.Builder.anFieldConfig().withLen(100).withSearchable(true).build()).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(baseFieldId--)
                .withFieldType(FieldType.DECIMAL)
                .withName("l2-dec")
                .withConfig(
                    FieldConfig.Builder.anFieldConfig().withLen(100).withPrecision(6).withSearchable(true).build())
                .build())
            .withField(EntityField.Builder.anEntityField()
                .withId(DRIVCER_ID_FEILD_ID)
                .withFieldType(FieldType.LONG)
                .withName("l2-driver.id")
                .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
                .build())
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(3)
                        .withCode("l2-one-to-many")
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(true)
                        .withIdentity(false)
                        .withLeftEntityClassId(driverEntityClassId)
                        .withLeftEntityClassCode("l2")
                        .withEntityField(
                            EntityField.Builder.anEntityField()
                                .withId(DRIVCER_ID_FEILD_ID)
                                .withFieldType(FieldType.LONG)
                                .withName("driver.id")
                                .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
                                .build()
                        )
                        .withRightEntityClassId(l2EntityClassId)
                        .withRightEntityClassLoader((id) -> Optional.ofNullable(l2EntityClass))
                        .build(),
                    Relationship.Builder.anRelationship()
                        .withId(4)
                        .withCode("l2-many-to-one")
                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                        .withBelongToOwner(true)
                        .withIdentity(false)
                        .withLeftEntityClassId(l2EntityClassId)
                        .withLeftEntityClassCode("l2")
                        .withEntityField(
                            EntityField.Builder.anEntityField()
                                .withId(DRIVCER_ID_FEILD_ID)
                                .withFieldType(FieldType.LONG)
                                .withName("driver.id")
                                .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
                                .build()
                        )
                        .withRightEntityClassId(driverEntityClassId)
                        .withRightEntityClassLoader((id) -> Optional.ofNullable(driverEntityClass))
                        .build(),
                    Relationship.Builder.anRelationship()
                        .withId(5)
                        .withCode("l2-one-to-many")
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(false)
                        .withIdentity(false)
                        .withStrong(true)
                        .withLeftEntityClassId(l2EntityClassId)
                        .withLeftEntityClassCode("l2")
                        .withRightEntityClassId(lookupEntityClassId)
                        .withRightEntityClassLoader((id) -> Optional.ofNullable(lookupEntityClass))
                        .build()
                )
            )
            .withFather(l1EntityClass)
            .build();

        driverEntityClass = EntityClass.Builder.anEntityClass()
            .withId(driverEntityClassId)
            .withLevel(0)
            .withCode("driver")
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(baseFieldId--)
                    .withFieldType(FieldType.LONG)
                    .withName("driver-long")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withSearchable(true)
                            .withLen(Integer.MAX_VALUE)
                            .build()
                    ).build()
            )
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(1)
                        .withCode("l2-one-to-many")
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(false)
                        .withIdentity(false)
                        .withLeftEntityClassId(driverEntityClassId)
                        .withLeftEntityClassCode("driver")
                        .withEntityField(l2EntityClass.field("l2-driver.id").get())
                        .withRightEntityClassId(l2EntityClass.id())
                        .withRightEntityClassLoader((id) -> Optional.ofNullable(l2EntityClass))
                        .build(),
                    Relationship.Builder.anRelationship()
                        .withId(2)
                        .withCode("l2-many-to-one")
                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                        .withBelongToOwner(true)
                        .withIdentity(false)
                        .withLeftEntityClassId(l2EntityClass.id())
                        .withLeftEntityClassCode(l2EntityClass.code())
                        .withEntityField(l2EntityClass.field("l2-driver.id").get())
                        .withRightEntityClassId(driverEntityClassId)
                        .withRightEntityClassLoader((id) -> Optional.ofNullable(driverEntityClass))
                        .build()
                )
            ).build();

        lookupEntityClass = EntityClass.Builder.anEntityClass()
            .withId(lookupEntityClassId)
            .withLevel(0)
            .withCode("lookup")
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(baseFieldId--)
                    .withName("lookup-l2-string")
                    .withFieldType(FieldType.STRING)
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withCalculation(
                                Lookup.Builder.anLookup()
                                    .withClassId(l2EntityClassId)
                                    .withFieldId(l2EntityClass.field("l2-string").get().id()).build()
                            )
                            .withSearchable(true)
                            .withLen(Integer.MAX_VALUE)
                            .build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(baseFieldId--)
                    .withName("lookup-l0-string")
                    .withFieldType(FieldType.STRING)
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withCalculation(
                                Lookup.Builder.anLookup()
                                    .withClassId(l2EntityClassId)
                                    .withFieldId(l2EntityClass.field("l0-string").get().id()).build()
                            )
                            .withSearchable(true)
                            .withFuzzyType(FieldConfig.FuzzyType.SEGMENTATION)
                            .withLen(Integer.MAX_VALUE)
                            .build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(baseFieldId--)
                    .withName("lookup-l2-dec")
                    .withFieldType(FieldType.DECIMAL)
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withCalculation(
                                Lookup.Builder.anLookup()
                                    .withClassId(l2EntityClassId)
                                    .withFieldId(l2EntityClass.field("l2-dec").get().id()).build()
                            )
                            .withSearchable(true)
                            .withLen(Integer.MAX_VALUE)
                            .withPrecision(Integer.MAX_VALUE)
                            .build()
                    ).build()
            )
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(baseFieldId--)
                    .withName("l2-lookup.id")
                    .withFieldType(FieldType.LONG)
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig().withSearchable(true).withLen(Integer.MAX_VALUE).build()
                    ).build()
            )
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(5)
                        .withCode("l2-one-to-many-lookup")
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(true)
                        .withIdentity(false)
                        .withStrong(true)
                        .withLeftEntityClassId(lookupEntityClassId)
                        .withLeftEntityClassCode("lookup")
                        .withRightEntityClassId(l2EntityClassId)
                        .withRightEntityClassLoader((id) -> Optional.ofNullable(l2EntityClass))
                        .build()
                )
            ).build();
    }
}
