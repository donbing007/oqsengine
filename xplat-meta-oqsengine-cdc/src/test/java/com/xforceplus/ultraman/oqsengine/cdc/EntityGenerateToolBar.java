package com.xforceplus.ultraman.oqsengine.cdc;

import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder.*;

/**
 * desc :
 * name : EntityGenerateToolBar
 *
 * @author : xujia
 * date : 2020/11/10
 * @since : 1.8
 */
public class EntityGenerateToolBar {

    public static IEntity[] generateFixedEntities(long startId, int version) {

        IEntity[] entityes = new IEntity[10];

        long id = startId;
        IEntityValue values = EntityValue.build().addValues(
                Arrays.asList(new LongValue(longField, 1L), new StringValue(stringField, "v1"),
                        new BooleanValue(boolField, true))
        );
        entityes[0] = Entity.Builder.anEntity()
                .withId(id)
                .withEntityClassRef(
                        EntityClassRef
                                .Builder
                                .anEntityClassRef()
                                .withEntityClassId(entityClass0.id())
                                .withEntityClassCode(entityClass0.code())
                                .build()
                )
                .withMajor(OqsVersion.MAJOR)
                .withVersion(version)
                .withEntityValue(values)
                .build();



        id = startId + 1;
        values = EntityValue.build().addValues(
                Arrays.asList(new LongValue(longField, 2L), new StringValue(stringField, "v2"),
                        new BooleanValue(boolField, true),
                        new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 2, 1, 9, 0, 1)),
                        new DecimalValue(decimalField, new BigDecimal("1.0")))
        );
        entityes[1] = Entity.Builder.anEntity()
                .withId(id)
                .withEntityClassRef(
                        EntityClassRef
                                .Builder
                                .anEntityClassRef()
                                .withEntityClassId(entityClass1.id())
                                .withEntityClassCode(entityClass1.code())
                                .build()
                )
                .withMajor(OqsVersion.MAJOR)
                .withVersion(version)
                .withEntityValue(values)
                .build();

        id = startId + 2;
        values = EntityValue.build().addValues(
                Arrays.asList(new LongValue(longField, 2L), new StringValue(stringField, "hello world"),
                        new BooleanValue(boolField, false),
                        new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 2, 1, 11, 18, 1)),
                        new DecimalValue(decimalField, new BigDecimal("1.0")))
        );
        entityes[2] = Entity.Builder.anEntity()
                .withId(id)
                .withEntityClassRef(
                        EntityClassRef
                                .Builder
                                .anEntityClassRef()
                                .withEntityClassId(entityClass1.id())
                                .withEntityClassCode(entityClass1.code())
                                .build()
                )
                .withMajor(OqsVersion.MAJOR)
                .withVersion(version)
                .withEntityValue(values)
                .build();


        id = startId + 3;
        values = EntityValue.build().addValues(
                Arrays.asList(new LongValue(longField, 76L), new StringValue(stringField, "中文测试chinese test"),
                        new BooleanValue(boolField, false),
                        new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 3, 1, 0, 0, 1)),
                        new DecimalValue(decimalField, new BigDecimal("1.0")), new EnumValue(enumField, "CODE"),
                        new StringsValue(stringsField, "value1", "value2", "value3"))
        );
        entityes[3] = Entity.Builder.anEntity()
                .withId(id)
                .withEntityClassRef(
                        EntityClassRef
                                .Builder
                                .anEntityClassRef()
                                .withEntityClassId(entityClass2.id())
                                .withEntityClassCode(entityClass2.code())
                                .build()
                )
                .withMajor(OqsVersion.MAJOR)
                .withVersion(version)
                .withEntityValue(values)
                .build();

        id = startId + 4;
        values = EntityValue.build().addValues(
                Arrays.asList(new LongValue(longField, 86L), new StringValue(stringField, "\"@带有符号的中文@\"\'"),
                        new BooleanValue(boolField, false),
                        new DateTimeValue(dateTimeField, LocalDateTime.of(2019, 3, 1, 0, 0, 1)),
                        new DecimalValue(decimalField, new BigDecimal("123.7582193213")), new EnumValue(enumField, "CODE"),
                        new StringsValue(stringsField, "value1", "value2", "value3", "UNKNOWN"))
        );
        entityes[4] = Entity.Builder.anEntity()
                .withId(id)
                .withEntityClassRef(
                        EntityClassRef
                                .Builder
                                .anEntityClassRef()
                                .withEntityClassId(entityClass2.id())
                                .withEntityClassCode(entityClass2.code())
                                .build()
                )
                .withMajor(OqsVersion.MAJOR)
                .withVersion(version)
                .withEntityValue(values)
                .build();

        id = startId + 5;
        values = EntityValue.build().addValues(
                Arrays.asList(new LongValue(longField, 86L), new StringValue(stringField, "A"),
                        new BooleanValue(boolField, true))
        );
        entityes[5] = Entity.Builder.anEntity()
                .withId(id)
                .withEntityClassRef(
                        EntityClassRef
                                .Builder
                                .anEntityClassRef()
                                .withEntityClassId(entityClass0.id())
                                .withEntityClassCode(entityClass0.code())
                                .build()
                )
                .withMajor(OqsVersion.MAJOR)
                .withVersion(version)
                .withEntityValue(values)
                .build();


        id = startId + 6;
        values = EntityValue.build().addValues(
                Arrays.asList(new LongValue(longField, 72L), new StringValue(stringField, "AB"),
                        new BooleanValue(boolField, false))
        );
        entityes[6] = Entity.Builder.anEntity()
                .withId(id)
                .withEntityClassRef(
                        EntityClassRef
                                .Builder
                                .anEntityClassRef()
                                .withEntityClassId(entityClass0.id())
                                .withEntityClassCode(entityClass0.code())
                                .build()
                )
                .withMajor(OqsVersion.MAJOR)
                .withVersion(version)
                .withEntityValue(values)
                .build();

        id = startId + 7;
        values = EntityValue.build().addValues(Arrays.asList(new LongValue(longField, 996L), new StringValue(stringField, "中文测试chinese test1"),
                new BooleanValue(boolField, false),
                new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 4, 1, 0, 0, 1)),
                new DecimalValue(decimalField, new BigDecimal("1.0")), new EnumValue(enumField, "CODE"),
                new StringsValue(stringsField, "value1", "value2", "value3", "value4")));
        entityes[7] = Entity.Builder.anEntity()
                .withId(id)
                .withEntityClassRef(
                        EntityClassRef
                                .Builder
                                .anEntityClassRef()
                                .withEntityClassId(entityClass2.id())
                                .withEntityClassCode(entityClass2.code())
                                .build()
                )
                .withMajor(OqsVersion.MAJOR)
                .withVersion(version)
                .withEntityValue(values)
                .build();

        id = startId + 8;
        values = EntityValue.build().addValues(Arrays.asList(new LongValue(longField, 1996L), new StringValue(stringField, "中文测试chinese test2"),
                new BooleanValue(boolField, false),
                new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 4, 1, 0, 0, 1)),
                new DecimalValue(decimalField, new BigDecimal("1.0")), new EnumValue(enumField, "CODE"),
                new StringsValue(stringsField, "value5", "value6", "value7", "value4")));
        entityes[8] = Entity.Builder.anEntity()
                .withId(id)
                .withEntityClassRef(
                        EntityClassRef
                                .Builder
                                .anEntityClassRef()
                                .withEntityClassId(entityClass2.id())
                                .withEntityClassCode(entityClass2.code())
                                .build()
                )
                .withMajor(OqsVersion.MAJOR)
                .withVersion(version)
                .withEntityValue(values)
                .build();

        id = startId + 9;
        values = EntityValue.build().addValues(Arrays.asList(new LongValue(longField, 445L), new StringValue(stringField, "hello world"),
                new BooleanValue(boolField, false),
                new DateTimeValue(dateTimeField, LocalDateTime.of(1900, 2, 1, 11, 18, 1)),
                new DecimalValue(decimalField, new BigDecimal("1.0")), new EnumValue(enumField, "CODE"),
                new StringsValue(stringsField, "value1", "value2", "value3")));
        entityes[9] = Entity.Builder.anEntity()
                .withId(id)
                .withEntityClassRef(
                        EntityClassRef
                                .Builder
                                .anEntityClassRef()
                                .withEntityClassId(entityClass2.id())
                                .withEntityClassCode(entityClass2.code())
                                .build()
                )
                .withMajor(OqsVersion.MAJOR)
                .withVersion(version)
                .withEntityValue(values)
                .build();

        return entityes;
    }
}
