package com.xforceplus.ultraman.oqsengine.devops;

import static com.xforceplus.ultraman.oqsengine.devops.EntityClassBuilder.BOOL_FIELD;
import static com.xforceplus.ultraman.oqsengine.devops.EntityClassBuilder.DATE_TIME_FIELD;
import static com.xforceplus.ultraman.oqsengine.devops.EntityClassBuilder.DECIMAL_FIELD;
import static com.xforceplus.ultraman.oqsengine.devops.EntityClassBuilder.LONG_FIELD;
import static com.xforceplus.ultraman.oqsengine.devops.EntityClassBuilder.STRINGS_FIELD;
import static com.xforceplus.ultraman.oqsengine.devops.EntityClassBuilder.STRING_FIELD;
import static com.xforceplus.ultraman.oqsengine.devops.EntityClassBuilder.entityClass0;
import static com.xforceplus.ultraman.oqsengine.devops.EntityClassBuilder.entityClass1;
import static com.xforceplus.ultraman.oqsengine.devops.EntityClassBuilder.entityClass2;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.SECOND;

import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.BooleanValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import org.junit.Ignore;

/**
 * entity生成工具.
 *
 * @author xujia 2020/11/26
 * @since 1.8
 */
@Ignore
public class EntityGenerateTooBar {

    public static long startPos = 1;
    public static int testVersion = 0;

    public static LocalDateTime now = LocalDateTime.now();
    public static long defaultTime = now.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();


    public static long longStringStartTime = 0;
    public static long longStringEndTime = 0;
    public static final IEntityClass LONG_STRING_ENTITY_CLASS = entityClass0;

    /**
     * 准备数字字段的entity.
     *
     * @param size 需要的数量.
     * @return 实例列表.
     */
    public static IEntity[] prepareLongStringEntity(int size) {
        IEntity[] entities = new IEntity[size];

        for (int i = 0; i < size; i++) {

            IEntityValue values = EntityValue.build()
                .addValues(Arrays.asList(new LongValue(LONG_FIELD, startPos),
                    new StringValue(STRING_FIELD, "prepareLongString" + startPos),
                    new BooleanValue(BOOL_FIELD, startPos % 2 == 0)));

            entities[i] = Entity.Builder.anEntity()
                .withId(startPos)
                .withEntityClassRef(EntityClassRef
                    .Builder.anEntityClassRef()
                    .withEntityClassId(LONG_STRING_ENTITY_CLASS.id())
                    .withEntityClassCode(LONG_STRING_ENTITY_CLASS.code())
                    .build()
                )
                .withEntityValue(values)
                .withVersion(testVersion)
                .withMajor(OqsVersion.MAJOR)
                .withTime(defaultTime + startPos * SECOND)
                .build();

            startPos++;
            //  结束时间
            longStringEndTime = entities[i].time();
        }
        longStringStartTime = entities[0].time();
        return entities;
    }

    public static long surPlusStartTime = 0;
    public static long surPlusEndTime = 0;
    public static final IEntityClass SUR_PLUS_ENTITY_CLASS = entityClass1;

    /**
        surplus test use.
    */
    public static IEntity[] prepareSurPlusNeedDeleteEntity(int size) {
        IEntity[] entities = new IEntity[size];
        for (int i = 0; i < size; i++) {

            IEntityValue values = EntityValue.build()
                .addValues(Arrays.asList(new LongValue(LONG_FIELD, startPos),
                    new StringValue(STRING_FIELD, "surPlus" + startPos),
                    new BooleanValue(BOOL_FIELD, startPos % 2 == 0),
                    new DateTimeValue(
                        DATE_TIME_FIELD,
                        LocalDateTime.of(2021, 3, 1, (int) startPos % 24, (int) startPos % 60, (int) startPos % 60))));


            entities[i] = Entity.Builder.anEntity()
                .withId(startPos)
                .withEntityClassRef(EntityClassRef
                    .Builder
                    .anEntityClassRef()
                    .withEntityClassId(SUR_PLUS_ENTITY_CLASS.id())
                    .withEntityClassCode(SUR_PLUS_ENTITY_CLASS.code())
                    .build()
                )
                .withEntityValue(values)
                .withVersion(testVersion)
                .withMajor(OqsVersion.MAJOR)
                .withTime(defaultTime + startPos * SECOND)
                .build();

            startPos++;
            surPlusEndTime = entities[i].time();
        }
        surPlusStartTime = entities[0].time();
        return entities;
    }

    public static long pauseResumeStartTime = 0;
    public static long pauseResumeEndTime = 0;
    public static final IEntityClass PREPARE_PAUSE_RESUME_ENTITY_CLASS = entityClass2;

    /**
        resume test use.
    */
    public static IEntity[] preparePauseResumeEntity(int size) {
        IEntity[] entities = new IEntity[size];
        for (int i = 0; i < size; i++) {

            IEntityValue values = EntityValue.build()
                .addValues(Arrays.asList(new LongValue(LONG_FIELD, Long.MAX_VALUE - startPos),
                    new StringValue(STRING_FIELD, "preparePauseResume" + startPos),
                    new BooleanValue(BOOL_FIELD, startPos % 3 == 0),
                    new DateTimeValue(DATE_TIME_FIELD,
                        LocalDateTime.of(2022, 3, 1, (int) startPos % 24, (int) startPos % 60, (int) startPos % 60)),
                    new DecimalValue(DECIMAL_FIELD, new BigDecimal(i + ".0")),
                    new StringsValue(STRINGS_FIELD, "value" + i, "value" + i + 1, "value" + i + 2)
                    )
                );


            entities[i] = Entity.Builder.anEntity()
                .withId(startPos)
                .withEntityClassRef(EntityClassRef
                    .Builder
                    .anEntityClassRef()
                    .withEntityClassId(PREPARE_PAUSE_RESUME_ENTITY_CLASS.id())
                    .withEntityClassCode(PREPARE_PAUSE_RESUME_ENTITY_CLASS.code())
                    .build()
                )
                .withEntityValue(values)
                .withVersion(testVersion)
                .withMajor(OqsVersion.MAJOR)
                .withTime(defaultTime + startPos * SECOND)
                .build();

            startPos++;
            pauseResumeEndTime = entities[i].time();
        }
        pauseResumeStartTime = entities[0].time();
        return entities;
    }
}
