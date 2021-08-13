package com.xforceplus.ultraman.oqsengine.core.service.integration.mock;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
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
public class MockMetaManager extends com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManager {

    //-------------level 0--------------------
    public static IEntityClass l0EntityClass = EntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE)
        .withLevel(0)
        .withCode("l0")
        .withField(EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE)
            .withFieldType(FieldType.LONG)
            .withName("l0-long")
            .withConfig(FieldConfig.build().searchable(true)).build())
        .withField(EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 1)
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
            .withId(Long.MAX_VALUE - 2)
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

    //-------------level 1--------------------
    public static IEntityClass l1EntityClass = EntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE - 1)
        .withLevel(1)
        .withCode("l1")
        .withField(EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 3)
            .withFieldType(FieldType.LONG)
            .withName("l1-long")
            .withConfig(FieldConfig.build().searchable(true)).build())
        .withField(EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 4)
            .withFieldType(FieldType.STRING)
            .withName("l1-string")
            .withConfig(FieldConfig.Builder.anFieldConfig()
                .withSearchable(true)
                .withFuzzyType(FieldConfig.FuzzyType.WILDCARD)
                .withWildcardMinWidth(3).withWildcardMaxWidth(7).build()).build())
        .withFather(l0EntityClass)
        .build();

    //-------------level 2--------------------
    public static IEntityClass l2EntityClass = null;
    public static IEntityClass driverEntityClass = null;

    static long l2EntityClassId = Long.MAX_VALUE - 2;
    static String l2EntityClassCode = "l2";
    static long driverEntityClassId = Long.MAX_VALUE - 3;
    static String driverEntityClassCode = "driver";


    static {
        l2EntityClass = EntityClass.Builder.anEntityClass()
            .withId(l2EntityClassId)
            .withLevel(2)
            .withCode(l2EntityClassCode)
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - 5)
                .withFieldType(FieldType.STRING)
                .withName("l2-string")
                .withConfig(FieldConfig.build().searchable(true)).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - 6)
                .withFieldType(FieldType.DATETIME)
                .withName("l2-time")
                .withConfig(FieldConfig.build().searchable(true)).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - 7)
                .withFieldType(FieldType.ENUM)
                .withName("l2-enum")
                .withConfig(FieldConfig.build().searchable(true)).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - 8)
                .withFieldType(FieldType.DECIMAL)
                .withName("l2-dec")
                .withConfig(FieldConfig.build().searchable(true)).build())
            .withField(EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - 9)
                .withFieldType(FieldType.LONG)
                .withName("l2-driver.id")
                .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
                .build())
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anOqsRelation()
                        .withId(3)
                        .withCode("l2-one-to-many")
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(true)
                        .withIdentity(false)
                        .withLeftEntityClassId(driverEntityClassId)
                        .withLeftEntityClassCode(driverEntityClassCode)
                        .withEntityField(
                            EntityField.Builder.anEntityField()
                                .withId(Long.MAX_VALUE - 9)
                                .withFieldType(FieldType.LONG)
                                .withName("driver.id")
                                .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
                                .build()
                        )
                        .withRightEntityClassId(l2EntityClassId)
                        .withRightEntityClassLoader((id) -> Optional.ofNullable(l2EntityClass))
                        .build(),
                    Relationship.Builder.anOqsRelation()
                        .withId(4)
                        .withCode("l2-many-to-one")
                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                        .withBelongToOwner(true)
                        .withIdentity(false)
                        .withLeftEntityClassId(l2EntityClassId)
                        .withLeftEntityClassCode(l2EntityClassCode)
                        .withEntityField(
                            EntityField.Builder.anEntityField()
                                .withId(Long.MAX_VALUE - 9)
                                .withFieldType(FieldType.LONG)
                                .withName("driver.id")
                                .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
                                .build()
                        )
                        .withRightEntityClassId(driverEntityClassId)
                        .withRightEntityClassLoader((id) -> Optional.ofNullable(driverEntityClass))
                        .build()
                )
            )
            .withFather(l1EntityClass)
            .build();

        driverEntityClass = EntityClass.Builder.anEntityClass()
            .withId(driverEntityClassId)
            .withLevel(0)
            .withCode(driverEntityClassCode)
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - 5)
                    .withFieldType(FieldType.LONG)
                    .withName("driver-long")
                    .withConfig(
                        FieldConfig.Builder.anFieldConfig()
                            .withSearchable(true)
                            .build()
                    ).build()
            )
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anOqsRelation()
                        .withId(1)
                        .withCode("l2-one-to-many")
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(false)
                        .withIdentity(false)
                        .withLeftEntityClassId(driverEntityClassId)
                        .withLeftEntityClassCode(driverEntityClassCode)
                        .withEntityField(l2EntityClass.field("l2-driver.id").get())
                        .withRightEntityClassId(l2EntityClass.id())
                        .withRightEntityClassLoader((id) -> Optional.ofNullable(l2EntityClass))
                        .build(),
                    Relationship.Builder.anOqsRelation()
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
            )
            .build();
    }

    public MockMetaManager() {
        super();

        this.addEntityClass(l2EntityClass);
        this.addEntityClass(driverEntityClass);
    }
}
