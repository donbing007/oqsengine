package com.xforceplus.ultraman.oqsengine.core.service.impl.mock;

import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsEntityClass;

/**
 * metamanager mock.
 *
 * @author dongbin
 * @version 0.1 2021/3/18 15:12
 * @since 1.8
 */
public class EntityClassDefine {


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
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withLen(3)
                    .withSearchable(true).build()
            ).build())
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
    public static IEntityClass l2EntityClass = OqsEntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE - 2)
        .withLevel(2)
        .withCode("l2")
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
        .withFather(l1EntityClass)
        .build();

    public static IEntityClass mustEntityClass = OqsEntityClass.Builder.anEntityClass()
        .withId(Long.MAX_VALUE - 3)
        .withCode("must")
        .withField(
            EntityField.Builder.anEntityField()
                .withId(Long.MAX_VALUE - 9)
                .withFieldType(FieldType.STRING)
                .withName("not-must-field")
                .withConfig(
                    FieldConfig.Builder.anFieldConfig()
                        .withRequired(false).build()
                ).build()
        )
        .withField(
            EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 10)
            .withFieldType(FieldType.STRING)
            .withName("must-field")
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                .withRequired(true).build()
            ).build()
        ).build();

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
