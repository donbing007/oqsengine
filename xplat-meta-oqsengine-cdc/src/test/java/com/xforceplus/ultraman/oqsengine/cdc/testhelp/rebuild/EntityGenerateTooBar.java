package com.xforceplus.ultraman.oqsengine.cdc.testhelp.rebuild;

import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.BooleanValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * entity生成工具.
 *
 * @author xujia 2020/11/26
 * @since 1.8
 */
public class EntityGenerateTooBar {

    public static int testVersion = 1;

    public static LocalDateTime now = LocalDateTime.now();

    public static long startTime = 0;
    public static long endTime = 0;

    /**
     * 准备数字字段的entity.
     *
     * @param size 需要的数量.
     * @return 实例列表.
     */
    public static List<IEntity> prepareEntities(long startPos, int size, IEntityClass entityClass) {
        List<IEntity> entities = new ArrayList<>();

        startTime = System.currentTimeMillis() - 1_000;

        for (int i = 0; i < size; i++) {

            Collection<IValue> values = new ArrayList<>();
            for (IEntityField entityField : entityClass.fields()) {
                values.add(mockValueGenerate(entityField, startPos));
            }

            entities.add(Entity.Builder.anEntity()
                .withId(startPos)
                .withEntityClassRef(EntityClassRef
                    .Builder.anEntityClassRef()
                    .withEntityClassId(entityClass.id())
                    .withEntityClassCode(entityClass.code())
                    .build()
                )
                .withValues(values)
                .withVersion(testVersion)
                .withMajor(OqsVersion.MAJOR)
                .withTime(System.currentTimeMillis())
                .build());

            startPos++;
        }

        //  结束时间
        endTime = System.currentTimeMillis() + 1_000;

        return entities;
    }

    public static IValue<?> mockValueGenerate(IEntityField entityField, long longValue) {

        switch (entityField.type()) {
            case LONG: {
                return new LongValue(entityField, longValue);
            }
            case STRING: {
                return new StringValue(entityField, "testStringPrefix_" + longValue + "_testStringSuffix");
            }
            case BOOLEAN: {
                return new BooleanValue(entityField, longValue % 2 == 0);
            }
            case ENUM: {
                return new StringValue(entityField, "TEST_ENUM_" + longValue);
            }
            case DECIMAL: {
                return new DecimalValue(entityField, new BigDecimal(((Double) (longValue * 3.1415926)).toString()));
            }
            case STRINGS: {
                return new StringsValue(entityField,
                    String.format("STRINGS_1_%d, STRINGS_2_%d, STRINGS_3_%d", longValue, longValue, longValue));
            }
            case DATETIME: {
                return new DateTimeValue(entityField, LocalDateTime.now());
            }
            default: {
                return new EmptyTypedValue(entityField);
            }
        }
    }
}
