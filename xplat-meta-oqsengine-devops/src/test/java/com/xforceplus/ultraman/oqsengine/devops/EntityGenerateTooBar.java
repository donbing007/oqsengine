package com.xforceplus.ultraman.oqsengine.devops;

import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

import static com.xforceplus.ultraman.oqsengine.devops.EntityClassBuilder.*;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.SECOND;

/**
 * desc :
 * name : EntityGenerateTooBar
 *
 * @author : xujia
 * date : 2020/11/26
 * @since : 1.8
 */
public class EntityGenerateTooBar {

    public static long startPos = 1;
    public static int testVersion = 0;

    public static LocalDateTime now = LocalDateTime.now();
    public static long defaultTime = now.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();


    public static long longStringStartTime = 0;
    public static long longStringEndTime = 0;
    public static final IEntityClass longStringEntityClass = entityClass0;
    public static IEntity[] prepareLongStringEntity(int size) {
        IEntity[] entities = new IEntity[size];

        for (int i = 0; i < size; i++) {

            IEntityValue values = EntityValue.build()
                                        .addValues(Arrays.asList(new LongValue(longField, startPos),
                                                new StringValue(stringField, "prepareLongString" + startPos),
                                                new BooleanValue(boolField, startPos % 2 == 0)));

            entities[i] = Entity.Builder.anEntity()
                                    .withId(startPos)
                                    .withEntityClassRef(EntityClassRef
                                        .Builder.anEntityClassRef()
                                            .withEntityClassId(longStringEntityClass.id())
                                            .withEntityClassCode(longStringEntityClass.code())
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
    public static final IEntityClass surPlusEntityClass = entityClass1;
    /*
        surplus test use
    */
    public static IEntity[] prepareSurPlusNeedDeleteEntity(int size) {
        IEntity[] entities = new IEntity[size];
        for (int i = 0; i < size; i++) {

            IEntityValue values = EntityValue.build()
                    .addValues(Arrays.asList(new LongValue(longField, startPos),
                            new StringValue(stringField, "surPlus" + startPos),
                            new BooleanValue(boolField, startPos % 2 == 0),
                            new DateTimeValue(dateTimeField, LocalDateTime.of(2021, 3, 1, (int) startPos % 24, (int) startPos % 60, (int) startPos % 60))));


            entities[i] = Entity.Builder.anEntity()
                    .withId(startPos)
                    .withEntityClassRef(EntityClassRef
                            .Builder
                            .anEntityClassRef()
                            .withEntityClassId(surPlusEntityClass.id())
                            .withEntityClassCode(surPlusEntityClass.code())
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
    public static final IEntityClass preparePauseResumeEntityClass = entityClass2;
    /*
        resume test use
    */
    public static IEntity[] preparePauseResumeEntity(int size) {
        IEntity[] entities = new IEntity[size];
        for (int i = 0; i < size; i++) {

            IEntityValue values = EntityValue.build()
                    .addValues(Arrays.asList(new LongValue(longField, Long.MAX_VALUE - startPos),
                            new StringValue(stringField, "preparePauseResume" + startPos),
                            new BooleanValue(boolField, startPos % 3 == 0),
                            new DateTimeValue(dateTimeField,
                                    LocalDateTime.of(2022, 3, 1, (int) startPos % 24, (int) startPos % 60, (int) startPos % 60)),
                            new DecimalValue(decimalField, new BigDecimal(i + ".0")),
                            new StringsValue(stringsField, "value" + i, "value" + i + 1, "value" + i + 2)
                            )
                    );


            entities[i] = Entity.Builder.anEntity()
                    .withId(startPos)
                    .withEntityClassRef(EntityClassRef
                            .Builder
                            .anEntityClassRef()
                            .withEntityClassId(preparePauseResumeEntityClass.id())
                            .withEntityClassCode(preparePauseResumeEntityClass.code())
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
