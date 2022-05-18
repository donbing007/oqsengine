package com.xforceplus.ultraman.oqsengine.cdc.testhelp.meta;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class EntityFieldRepo {

    public static final IEntityField ID_FIELD =
        EntityField.Builder
            .anEntityField()
            .withId(1)
            .withName("id")
            .withFieldType(FieldType.LONG)
            .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
            .build();

    public static final IEntityField LONG_FIELD =
        EntityField.Builder
            .anEntityField()
            .withId(1)
            .withName("long_col")
            .withFieldType(FieldType.LONG)
            .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
            .build();

    public static final IEntityField STRING_FIELD =
        EntityField.Builder
            .anEntityField()
            .withId(2)
            .withName("string_col")
            .withFieldType(FieldType.STRING)
            .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
            .build();

    public static final IEntityField BOOL_FIELD =
        EntityField.Builder
            .anEntityField()
            .withId(3)
            .withName("bool_col")
            .withFieldType(FieldType.BOOLEAN)
            .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
            .build();

    public static final IEntityField DATE_TIME_FIELD =
        EntityField.Builder
            .anEntityField()
            .withId(4)
            .withName("datetime_col")
            .withFieldType(FieldType.DATETIME)
            .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
            .build();

    public static final IEntityField DECIMAL_FIELD =
        EntityField.Builder
            .anEntityField()
            .withId(5)
            .withName("decimal_col")
            .withFieldType(FieldType.DECIMAL)
            .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
            .build();

    public static final IEntityField ENUM_FIELD =
        EntityField.Builder
            .anEntityField()
            .withId(6)
            .withName("enum_col")
            .withFieldType(FieldType.ENUM)
            .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
            .build();

    public static final IEntityField STRINGS_FIELD =
        EntityField.Builder
            .anEntityField()
            .withId(7)
            .withName("strings_col")
            .withFieldType(FieldType.STRINGS)
            .withConfig(FieldConfig.Builder.anFieldConfig().withSearchable(true).build())
            .build();
}
