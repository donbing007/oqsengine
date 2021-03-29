package com.xforceplus.ultraman.oqsengine.core.service.integration.mock;

import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsRelation;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * @author dongbin
 * @version 0.1 2021/3/18 15:12
 * @since 1.8
 */
@Ignore
public class MockMetaManager implements MetaManager {


    //-------------level 0--------------------
    public static IEntityClass l0EntityClass = OqsEntityClass.Builder.anEntityClass()
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
            .withConfig(FieldConfig.build().searchable(true).fuzzyType(FieldConfig.FuzzyType.SEGMENTATION)).build())
        .withField(EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 2)
            .withFieldType(FieldType.STRINGS)
            .withName("l0-strings")
            .withConfig(FieldConfig.build().searchable(true)).build())
        .build();

    //-------------level 1--------------------
    public static IEntityClass l1EntityClass = OqsEntityClass.Builder.anEntityClass()
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
            .withConfig(FieldConfig.Builder.aFieldConfig()
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
        l2EntityClass = OqsEntityClass.Builder.anEntityClass()
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
                .withConfig(FieldConfig.Builder.aFieldConfig().withSearchable(true).build())
                .build())
            .withRelations(
                Arrays.asList(
                    OqsRelation.Builder.anOqsRelation()
                        .withId(3)
                        .withCode("l2-one-to-many")
                        .withRelationType(OqsRelation.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(true)
                        .withIdentity(false)
                        .withLeftEntityClassId(driverEntityClassId)
                        .withLeftEntityClassCode(driverEntityClassCode)
                        .withEntityField(
                            EntityField.Builder.anEntityField()
                                .withId(Long.MAX_VALUE - 9)
                                .withFieldType(FieldType.LONG)
                                .withName("driver.id")
                                .withConfig(FieldConfig.Builder.aFieldConfig().withSearchable(true).build())
                                .build()
                        )
                        .withRightEntityClassId(l2EntityClassId)
                        .withRightEntityClassLoader((id) -> Optional.ofNullable(l2EntityClass))
                        .build(),
                    OqsRelation.Builder.anOqsRelation()
                        .withId(4)
                        .withCode("l2-many-to-one")
                        .withRelationType(OqsRelation.RelationType.MANY_TO_ONE)
                        .withBelongToOwner(true)
                        .withIdentity(false)
                        .withLeftEntityClassId(l2EntityClassId)
                        .withLeftEntityClassCode(l2EntityClassCode)
                        .withEntityField(
                            EntityField.Builder.anEntityField()
                                .withId(Long.MAX_VALUE - 9)
                                .withFieldType(FieldType.LONG)
                                .withName("driver.id")
                                .withConfig(FieldConfig.Builder.aFieldConfig().withSearchable(true).build())
                                .build()
                        )
                        .withRightEntityClassId(driverEntityClassId)
                        .withRightEntityClassLoader((id) -> Optional.ofNullable(driverEntityClass))
                        .build()
                )
            )
            .withFather(l1EntityClass)
            .build();

        driverEntityClass = OqsEntityClass.Builder.anEntityClass()
            .withId(driverEntityClassId)
            .withLevel(0)
            .withCode(driverEntityClassCode)
            .withField(
                EntityField.Builder.anEntityField()
                    .withId(Long.MAX_VALUE - 5)
                    .withFieldType(FieldType.LONG)
                    .withName("driver-long")
                    .withConfig(
                        FieldConfig.Builder.aFieldConfig()
                            .withSearchable(true)
                            .build()
                    ).build()
            )
            .withRelations(
                Arrays.asList(
                    OqsRelation.Builder.anOqsRelation()
                        .withId(1)
                        .withCode("l2-one-to-many")
                        .withRelationType(OqsRelation.RelationType.ONE_TO_MANY)
                        .withBelongToOwner(false)
                        .withIdentity(false)
                        .withLeftEntityClassId(driverEntityClassId)
                        .withLeftEntityClassCode(driverEntityClassCode)
                        .withEntityField(l2EntityClass.field("l2-driver.id").get())
                        .withRightEntityClassId(l2EntityClass.id())
                        .withRightEntityClassLoader((id) -> Optional.ofNullable(l2EntityClass))
                        .build(),
                    OqsRelation.Builder.anOqsRelation()
                        .withId(2)
                        .withCode("l2-many-to-one")
                        .withRelationType(OqsRelation.RelationType.MANY_TO_ONE)
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


    private Collection<IEntityClass> entities;

    public MockMetaManager() {
        entities = Arrays.asList(
            l0EntityClass, l1EntityClass, l2EntityClass, driverEntityClass
        );
    }

    @Override
    public Optional<IEntityClass> load(long id) {
        return entities.stream().filter(e -> e.id() == id).findFirst();
    }

    @Override
    public IEntityClass loadHistory(long id, int version) {
        return null;
    }

    @Override
    public int need(String appId, String env) {
        return 0;
    }
}
