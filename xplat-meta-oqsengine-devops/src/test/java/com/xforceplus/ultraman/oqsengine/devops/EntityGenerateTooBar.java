package com.xforceplus.ultraman.oqsengine.devops;

import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.BooleanValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * desc :
 * name : EntityGenerateTooBar
 *
 * @author : xujia
 * date : 2020/11/26
 * @since : 1.8
 */
public class EntityGenerateTooBar {
    public static final IEntityField stringField = new EntityField(Long.MAX_VALUE - 1, "string", FieldType.STRING, FieldConfig.build().searchable(true), null, null);
    public static final IEntityField longField = new EntityField(Long.MAX_VALUE - 2, "long", FieldType.LONG, FieldConfig.build().searchable(true), null, null);
    public static final IEntityField boolField = new EntityField(Long.MAX_VALUE - 3, "bool", FieldType.BOOLEAN, FieldConfig.build().searchable(true), null, null);
    public static final IEntityField dateTimeField = new EntityField(Long.MAX_VALUE - 4, "datetime", FieldType.DATETIME, FieldConfig.build().searchable(true), null, null);
    public static final IEntityField decimalField = new EntityField(Long.MAX_VALUE - 5, "decimal", FieldType.DECIMAL, FieldConfig.build().searchable(true), null, null);
    public static final IEntityField enumField = new EntityField(Long.MAX_VALUE - 6, "enum", FieldType.ENUM, FieldConfig.build().searchable(true), null, null);
    public static final IEntityField stringsField = new EntityField(Long.MAX_VALUE - 7, "strings", FieldType.STRINGS, FieldConfig.build().searchable(true), null, null);

    public static long startPos = 1;
    public static int testVersion = 0;

    public static long longStringStartTime = 0;
    public static long longStringEndTime = 0;
    public static LocalDateTime now = LocalDateTime.now();
    public static long defaultTime = now.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();

    public static final long one_second = 1000;
    /*
        long string entityValue
    */
    public static final long longStringEntityClassId = Long.MAX_VALUE;
    public static final Collection<IEntityField> longStringEntityFields = Arrays.asList(longField, stringField);
    public static final IEntityClass longStringEntityClass =
        new EntityClass(longStringEntityClassId, "LongString", null,
            null, null, longStringEntityFields);

    public static IEntity[] prepareLongStringEntity(int size) {
        IEntity[] entities = new IEntity[size];

        for (int i = 0; i < size; i++) {
            IEntityValue values = new EntityValue(startPos);
            values.addValues(Arrays.asList(new LongValue(longField, startPos),
                new StringValue(stringField, "longString" + startPos)));
            entities[i] = new Entity(startPos, longStringEntityClass, values, new EntityFamily(0, 0), testVersion, OqsVersion.MAJOR);
            entities[i].markTime(defaultTime + startPos * one_second);
            startPos++;
            //  结束时间
            longStringEndTime = entities[i].time();
        }
        longStringStartTime = entities[0].time();
        return entities;
    }

    public static long surPlusStartTime = 0;
    public static long surPlusEndTime = 0;
    /*
        surplus test use
    */
    public static final long surPlusNeedDeleteEntityClassId = Long.MAX_VALUE / 10;
    public static final Collection<IEntityField> surPlusNeedDeleteEntityFields = Arrays.asList(longField, stringField, boolField);
    public static final IEntityClass surPlusNeedDeleteEntityClass =
        new EntityClass(surPlusNeedDeleteEntityClassId, "surPlusNeedDelete", null,
            null, null, surPlusNeedDeleteEntityFields);

    public static IEntity[] prepareSurPlusNeedDeleteEntity(int size) {
        IEntity[] entities = new IEntity[size];
        for (int i = 0; i < size; i++) {
            IEntityValue values = new EntityValue(startPos);
            values.addValues(Arrays.asList(new LongValue(longField, startPos),
                new StringValue(stringField, "surPlus" + startPos), new BooleanValue(boolField, startPos % 2 == 0)));
            entities[i] = new Entity(startPos, surPlusNeedDeleteEntityClass, values, new EntityFamily(0, 0), testVersion, OqsVersion.MAJOR);
            entities[i].markTime(defaultTime + startPos * one_second);
            startPos++;
            surPlusEndTime = entities[i].time();
        }
        surPlusStartTime = entities[0].time();
        return entities;
    }

    public static long prefCrefStartTime = 0;
    public static long prefCrefEndTime = 0;
    /*
        pref/cref test use
    */
    public static final long prefEntityClassId = Long.MAX_VALUE / 100;
    public static final Collection<IEntityField> prefEntityFields = Arrays.asList(longField);
    public static final IEntityClass prefEntityClass =
        new EntityClass(prefEntityClassId, "pref", null,
            null, null, prefEntityFields);

    public static final long crefEntityClassId = Long.MAX_VALUE / 100 - 1;
    public static final Collection<IEntityField> crefEntityFields = Arrays.asList(stringField, boolField);
    public static final IEntityClass crefEntityClass =
        new EntityClass(crefEntityClassId, "cref", null,
            null, prefEntityClass, crefEntityFields);

    public static List<AbstractMap.SimpleEntry<IEntity, IEntity>> preparePrefCrefEntity(int size) {
        List<AbstractMap.SimpleEntry<IEntity, IEntity>> entities = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            IEntityValue pValues = new EntityValue(startPos);
            pValues.addValues(Collections.singletonList(new LongValue(longField, startPos)));
            IEntity entityF = new Entity(startPos, prefEntityClass, pValues, new EntityFamily(0, startPos + 1), testVersion, OqsVersion.MAJOR);

            startPos++;

            IEntityValue cValues = new EntityValue(startPos);
            pValues.addValues(Arrays.asList(new StringValue(stringField, "prefCref" + startPos), new BooleanValue(boolField, startPos % 2 == 0)));
            IEntity entityC = new Entity(startPos, crefEntityClass, cValues, new EntityFamily(startPos - 1, 0), testVersion, OqsVersion.MAJOR);

            prefCrefEndTime = defaultTime + startPos * one_second;
            entityF.markTime(prefCrefEndTime);
            entityC.markTime(prefCrefEndTime);

            entities.add(new AbstractMap.SimpleEntry<>(entityF, entityC));
            startPos++;
        }

        prefCrefStartTime = entities.isEmpty() ? 0 : entities.get(0).getKey().time();

        return entities;
    }

    public static long pauseResumeStartTime = 0;
    public static long pauseResumeEndTime = 0;
    /*
        resume test use
    */
    public static final long pauseResumeEntityClassId = Long.MAX_VALUE / 1000;
    public static final Collection<IEntityField> pauseResumeEntityFields = Arrays.asList(longField, stringField, boolField, stringsField);
    public static final IEntityClass pauseResumeEntityClass = new EntityClass(pauseResumeEntityClassId, "pauseResume", null,
        null, null, pauseResumeEntityFields);

    public static IEntity[] preparePauseResumeEntity(int size) {
        IEntity[] entities = new IEntity[size];
        for (int i = 0; i < size; i++) {
            IEntityValue values = new EntityValue(startPos);
            values.addValues(Arrays.asList(new LongValue(longField, startPos),
                new StringValue(stringField, "surPlus" + startPos),
                new BooleanValue(boolField, startPos % 2 == 0),
                new StringsValue(stringsField, "value" + startPos, startPos + "value", "value" + startPos + "value")));
            entities[i] = new Entity(startPos, pauseResumeEntityClass, values, new EntityFamily(0, 0), 0, OqsVersion.MAJOR);
            entities[i].markTime(defaultTime + startPos * one_second);
            startPos++;
            pauseResumeEndTime = entities[i].time();
        }
        pauseResumeStartTime = entities[0].time();
        return entities;
    }
}
