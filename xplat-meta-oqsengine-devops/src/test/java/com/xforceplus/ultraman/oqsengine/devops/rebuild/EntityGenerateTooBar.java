package com.xforceplus.ultraman.oqsengine.devops.rebuild;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.MILL_SECOND;

import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.BooleanValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * entity生成工具.
 *
 * @author xujia 2020/11/26
 * @since 1.8
 */
public class EntityGenerateTooBar {

    public static final IEntityField
        STRING_FIELD = new EntityField(1, "string", FieldType.STRING, FieldConfig.build().searchable(true), null, null);
    public static final IEntityField
        LONG_FIELD = new EntityField(2, "long", FieldType.LONG, FieldConfig.build().searchable(true), null, null);
    public static final IEntityField
        BOOL_FIELD = new EntityField(3, "bool", FieldType.BOOLEAN, FieldConfig.build().searchable(true), null, null);
    public static final IEntityField
        DATE_TIME_FIELD =
        new EntityField(4, "datetime", FieldType.DATETIME, FieldConfig.build().searchable(true), null, null);
    public static final IEntityField
        DECIMAL_FIELD =
        new EntityField(5, "decimal", FieldType.DECIMAL, FieldConfig.build().searchable(true), null, null);
    public static final IEntityField
        STRINGS_FIELD =
        new EntityField(6, "strings", FieldType.STRINGS, FieldConfig.build().searchable(true), null, null);

    // level 1
    public static final IEntityClass ENTITY_CLASS_0 =
        EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE)
            .withVersion(1)
            .withLevel(0)
            .withCode("c0")
            .withFields(
                Arrays.asList(LONG_FIELD, STRING_FIELD)
            ).build();

    // level 2
    public static final IEntityClass ENTITY_CLASS_1 =
        EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE - 1)
            .withVersion(1)
            .withLevel(1)
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
            .withLevel(2)
            .withCode("c2")
            .withFather(ENTITY_CLASS_1)
            .withFields(
                Arrays.asList(DECIMAL_FIELD, STRINGS_FIELD)
            ).build();

    public static long startPos = 1;
    public static int testVersion = 0;

    public static long longStringStartTime = 0;
    public static long longStringEndTime = 0;
    public static final IEntityClass LONG_STRING_ENTITY_CLASS = ENTITY_CLASS_0;

    /**
     * 准备数字字段的entity.
     *
     * @param size 需要的数量.
     * @return 实例列表.
     */
    public static List<IEntity> prepareLongStringEntity(int size, int startPos) {
        List<IEntity> entities = new ArrayList<>();

        long defaultTime = LocalDateTime.now().toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();

        for (int i = 0; i < size; i++) {
            Entity entity = Entity.Builder.anEntity()
                .withId(startPos)
                .withEntityClassRef(EntityClassRef
                    .Builder.anEntityClassRef()
                    .withEntityClassId(LONG_STRING_ENTITY_CLASS.id())
                    .withEntityClassCode(LONG_STRING_ENTITY_CLASS.code())
                    .build()
                )
                .withValues(Arrays.asList(new LongValue(LONG_FIELD, startPos),
                        new StringValue(STRING_FIELD, "prepareLongString" + startPos),
                        new BooleanValue(BOOL_FIELD, startPos % 2 == 0)))
                .withVersion(testVersion)
                .withMajor(OqsVersion.MAJOR)
                .withTime(defaultTime + startPos * MILL_SECOND)
                .build();

            entities.add(entity);

            startPos++;
            //  结束时间
            longStringEndTime = entity.time();
        }
        longStringStartTime = entities.get(0).time();
        return entities;
    }

    public static long surPlusStartTime = 0;
    public static long surPlusEndTime = 0;
    public static final IEntityClass SUR_PLUS_ENTITY_CLASS = ENTITY_CLASS_1;

    /**
     * surplus test use.
     */
    public static IEntity[] prepareSurPlusNeedDeleteEntity(int size) {
        IEntity[] entities = new IEntity[size];

        long defaultTime = LocalDateTime.now().toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();

        for (int i = 0; i < size; i++) {
            entities[i] = Entity.Builder.anEntity()
                .withId(startPos)
                .withEntityClassRef(EntityClassRef
                    .Builder
                    .anEntityClassRef()
                    .withEntityClassId(SUR_PLUS_ENTITY_CLASS.id())
                    .withEntityClassCode(SUR_PLUS_ENTITY_CLASS.code())
                    .build()
                )
                .withValues(Arrays.asList(new LongValue(LONG_FIELD, startPos),
                        new StringValue(STRING_FIELD, "surPlus" + startPos),
                        new BooleanValue(BOOL_FIELD, startPos % 2 == 0),
                        new DateTimeValue(
                                DATE_TIME_FIELD,
                                LocalDateTime.of(2021, 3, 1, (int) startPos % 24, (int) startPos % 60, (int) startPos % 60))))
                .withVersion(testVersion)
                .withMajor(OqsVersion.MAJOR)
                .withTime(defaultTime)
                .build();

            startPos++;
            surPlusEndTime = entities[i].time();
        }
        surPlusStartTime = entities[0].time();
        return entities;
    }

    public static long pauseResumeStartTime = 0;
    public static long pauseResumeEndTime = 0;
    public static final IEntityClass PREPARE_PAUSE_RESUME_ENTITY_CLASS = ENTITY_CLASS_2;

    /**
     * resume test use.
     */
    public static IEntity[] preparePauseResumeEntity(int size) {
        IEntity[] entities = new IEntity[size];

        long defaultTime = LocalDateTime.now().toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();

        for (int i = 0; i < size; i++) {
            entities[i] = Entity.Builder.anEntity()
                .withId(startPos)
                .withEntityClassRef(EntityClassRef
                    .Builder
                    .anEntityClassRef()
                    .withEntityClassId(PREPARE_PAUSE_RESUME_ENTITY_CLASS.id())
                    .withEntityClassCode(PREPARE_PAUSE_RESUME_ENTITY_CLASS.code())
                    .build()
                )
                .withValues(Arrays.asList(new LongValue(LONG_FIELD, Long.MAX_VALUE - startPos),
                        new StringValue(STRING_FIELD, "preparePauseResume" + startPos),
                        new BooleanValue(BOOL_FIELD, startPos % 3 == 0),
                        new DateTimeValue(DATE_TIME_FIELD,
                                LocalDateTime.of(2022, 3, 1, (int) startPos % 24, (int) startPos % 60, (int) startPos % 60)),
                        new DecimalValue(DECIMAL_FIELD, new BigDecimal(i + ".0")),
                        new StringsValue(STRINGS_FIELD, "value" + i, "value" + i + 1, "value" + i + 2)
                ))
                .withVersion(testVersion)
                .withMajor(OqsVersion.MAJOR)
                .withTime(defaultTime + startPos * MILL_SECOND)
                .build();

            startPos++;
            pauseResumeEndTime = entities[i].time();
        }
        pauseResumeStartTime = entities[0].time();
        return entities;
    }
}
