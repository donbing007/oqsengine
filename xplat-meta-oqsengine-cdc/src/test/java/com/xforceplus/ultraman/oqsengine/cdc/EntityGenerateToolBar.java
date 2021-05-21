package com.xforceplus.ultraman.oqsengine.cdc;

import static com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder.BOOL_FIELD;
import static com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder.DATE_TIME_FIELD;
import static com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder.DECIMAL_FIELD;
import static com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder.LONG_FIELD;
import static com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder.STRINGS_FIELD;
import static com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder.STRING_FIELD;
import static com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder.entityClass0;
import static com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder.entityClass1;
import static com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder.entityClass2;

import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
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
import java.util.Arrays;

/**
 * desc :.
 * name : EntityGenerateToolBar
 *
 * @author : xujia 2020/11/10
 * @since : 1.8
 */
public class EntityGenerateToolBar {

    /**
     * 测试实体生成.
     */
    public static IEntity[] generateWithBadEntities(long id, int version) {
        IEntity[] entityes = new IEntity[11];

        IEntityValue values = EntityValue.build().addValues(
            Arrays.asList(new LongValue(LONG_FIELD, 1L),
                new StringValue(STRING_FIELD, "v1><^^A\n\\0x00\4'$231....\n\\xEF\\xBB\\xBF."))
        );
        IEntity bad = Entity.Builder.anEntity()
            .withId(id * 10)
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

        IEntity[] good = generateFixedEntities(id, version);
        for (int i = 0; i < good.length; i++) {
            if (i < 5) {
                entityes[i] = good[i];
            } else {
                if (i == 5) {
                    entityes[i] = bad;
                }
                entityes[i + 1] = good[i];
            }
        }

        return entityes;
    }

    /**
     * 测试用固定存在的实体生成.
     */
    public static IEntity[] generateFixedEntities(long startId, int version) {

        IEntity[] entityes = new IEntity[10];

        long id = startId;
        IEntityValue values = EntityValue.build().addValues(
            Arrays.asList(new LongValue(LONG_FIELD, 1L), new StringValue(STRING_FIELD, "v1"))
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
            Arrays.asList(new LongValue(LONG_FIELD, 2L), new StringValue(STRING_FIELD, "v2"),
                new BooleanValue(BOOL_FIELD, true),
                new DateTimeValue(DATE_TIME_FIELD, LocalDateTime.of(2020, 2, 1, 9, 0, 1)))
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
            Arrays.asList(new LongValue(LONG_FIELD, 2L), new StringValue(STRING_FIELD, "hello world"),
                new BooleanValue(BOOL_FIELD, false),
                new DateTimeValue(DATE_TIME_FIELD, LocalDateTime.of(2020, 2, 1, 11, 18, 1)))
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
            Arrays.asList(new LongValue(LONG_FIELD, 76L),
                new StringValue(STRING_FIELD, "中文测试chinese test"),
                new BooleanValue(BOOL_FIELD, false),
                new DateTimeValue(DATE_TIME_FIELD, LocalDateTime.of(2020, 3, 1, 0, 0, 1)),
                new DecimalValue(DECIMAL_FIELD, new BigDecimal("1.0")),
                new StringsValue(STRINGS_FIELD, "value1", "value2", "value3"))
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
            Arrays.asList(new LongValue(LONG_FIELD, 86L), new StringValue(STRING_FIELD, "\"@带有符号的中文@\"\'"),
                new BooleanValue(BOOL_FIELD, false),
                new DateTimeValue(DATE_TIME_FIELD, LocalDateTime.of(2019, 3, 1, 0, 0, 1)),
                new DecimalValue(DECIMAL_FIELD, new BigDecimal("123.7582193213")),
                new StringsValue(STRINGS_FIELD, "value1", "value2", "value3", "UNKNOWN"))
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
            Arrays.asList(new LongValue(LONG_FIELD, 86L), new StringValue(STRING_FIELD, "A"),
                new BooleanValue(BOOL_FIELD, true))
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
            Arrays.asList(new LongValue(LONG_FIELD, 72L), new StringValue(STRING_FIELD, "AB"),
                new BooleanValue(BOOL_FIELD, false))
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
        values = EntityValue.build().addValues(Arrays.asList(new LongValue(LONG_FIELD, 996L), new StringValue(
                STRING_FIELD, "中文测试chinese test1"),
            new BooleanValue(BOOL_FIELD, false),
            new DateTimeValue(DATE_TIME_FIELD, LocalDateTime.of(2020, 4, 1, 0, 0, 1)),
            new DecimalValue(DECIMAL_FIELD, new BigDecimal("1.0")),
            new StringsValue(STRINGS_FIELD, "value1", "value2", "value3", "value4")));
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
        values = EntityValue.build().addValues(Arrays.asList(new LongValue(LONG_FIELD, 1996L), new StringValue(
                STRING_FIELD, "中文测试chinese test2"),
            new BooleanValue(BOOL_FIELD, false),
            new DateTimeValue(DATE_TIME_FIELD, LocalDateTime.of(2020, 4, 1, 0, 0, 1)),
            new DecimalValue(DECIMAL_FIELD, new BigDecimal("1.0")),
            new StringsValue(STRINGS_FIELD, "value5", "value6", "value7", "value4")));
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
        values = EntityValue.build().addValues(Arrays.asList(new LongValue(LONG_FIELD, 445L), new StringValue(
                STRING_FIELD, "hello world"),
            new BooleanValue(BOOL_FIELD, false),
            new DateTimeValue(DATE_TIME_FIELD, LocalDateTime.of(1900, 2, 1, 11, 18, 1)),
            new DecimalValue(DECIMAL_FIELD, new BigDecimal("1.0")),
            new StringsValue(STRINGS_FIELD, "value1", "value2", "value3")));
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
