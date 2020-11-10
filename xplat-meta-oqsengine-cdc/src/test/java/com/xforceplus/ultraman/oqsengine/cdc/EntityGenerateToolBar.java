package com.xforceplus.ultraman.oqsengine.cdc;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * desc :
 * name : EntityGenerateToolBar
 *
 * @author : xujia
 * date : 2020/11/10
 * @since : 1.8
 */
public class EntityGenerateToolBar {
    private static final IEntityField stringField = new EntityField(Long.MAX_VALUE - 1, "string", FieldType.STRING, FieldConfig.build().searchable(true), null, null);
    private static final IEntityField longField = new EntityField(Long.MAX_VALUE - 2, "long", FieldType.LONG, FieldConfig.build().searchable(true), null, null);
    private static final IEntityField boolField = new EntityField(Long.MAX_VALUE - 3, "bool", FieldType.BOOLEAN, FieldConfig.build().searchable(true), null, null);
    private static final IEntityField dateTimeField = new EntityField(Long.MAX_VALUE - 4, "datetime", FieldType.DATETIME, FieldConfig.build().searchable(true), null, null);
    private static final IEntityField decimalField = new EntityField(Long.MAX_VALUE - 5, "decimal", FieldType.DECIMAL, FieldConfig.build().searchable(true), null, null);
    private static final IEntityField enumField = new EntityField(Long.MAX_VALUE - 6, "enum", FieldType.ENUM, FieldConfig.build().searchable(true), null, null);
    private static final IEntityField stringsField = new EntityField(Long.MAX_VALUE - 7, "strings", FieldType.STRINGS, FieldConfig.build().searchable(true), null, null);


    private static IEntityClass entityClass = new EntityClass(Long.MAX_VALUE, "t1",
            Arrays.asList(longField, stringField, boolField, dateTimeField, decimalField, enumField, stringsField));


    public static IEntity[] generateFixedEntities(int version) {

        IEntity[] entityes = new IEntity[10];


        long id = Long.MAX_VALUE;
        IEntityValue values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 1L), new StringValue(stringField, "v1"),
                new BooleanValue(boolField, true),
                new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 1, 1, 0, 0, 1)),
                new DecimalValue(decimalField, BigDecimal.ZERO), new EnumValue(enumField, "1"),
                new StringsValue(stringsField, "value1", "value2")));
        entityes[0] = new Entity(id, entityClass, values, new EntityFamily(0, Long.MAX_VALUE - 1), version);

        id = Long.MAX_VALUE - 1;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 2L), new StringValue(stringField, "v2"),
                new BooleanValue(boolField, true),
                new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 2, 1, 9, 0, 1)),
                new DecimalValue(decimalField, BigDecimal.ONE), new EnumValue(enumField, "CODE"),
                new StringsValue(stringsField, "value1", "value2", "value3")));
        entityes[1] = new Entity(id, entityClass, values, new EntityFamily(Long.MAX_VALUE, 0), version);

        id = Long.MAX_VALUE - 2;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 2L), new StringValue(stringField, "hello world"),
                new BooleanValue(boolField, false),
                new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 2, 1, 11, 18, 1)),
                new DecimalValue(decimalField, BigDecimal.ONE), new EnumValue(enumField, "CODE"),
                new StringsValue(stringsField, "value1", "value2", "value3")));
        entityes[2] = new Entity(id, entityClass, values, new EntityFamily(0, 0), version);

        id = Long.MAX_VALUE - 3;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 76L), new StringValue(stringField, "中文测试chinese test"),
                new BooleanValue(boolField, false),
                new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 3, 1, 0, 0, 1)),
                new DecimalValue(decimalField, BigDecimal.ONE), new EnumValue(enumField, "CODE"),
                new StringsValue(stringsField, "value1", "value2", "value3")));
        entityes[3] = new Entity(id, entityClass, values, new EntityFamily(Long.MAX_VALUE - 4, 0), version);

        id = Long.MAX_VALUE - 4;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 86L), new StringValue(stringField, "\"@带有符号的中文@\"\'"),
                new BooleanValue(boolField, false),
                new DateTimeValue(dateTimeField, LocalDateTime.of(2019, 3, 1, 0, 0, 1)),
                new DecimalValue(decimalField, new BigDecimal("123.7582193213")), new EnumValue(enumField, "CODE"),
                new StringsValue(stringsField, "value1", "value2", "value3", "UNKNOWN")));
        entityes[4] = new Entity(id, entityClass, values, new EntityFamily(0, Long.MAX_VALUE - 3), version);

        id = Long.MAX_VALUE - 5;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new StringValue(stringField, "A"),
                new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 3, 1, 0, 0, 1))));
        entityes[5] = new Entity(id, entityClass, values, new EntityFamily(0, 0), version);

        id = Long.MAX_VALUE - 6;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new StringValue(stringField, "B"),
                new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 4, 1, 0, 0, 1))));
        entityes[6] = new Entity(id, entityClass, values, new EntityFamily(0, 0), version);

        id = Long.MAX_VALUE - 7;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 996L), new StringValue(stringField, "中文测试chinese test1"),
                new BooleanValue(boolField, false),
                new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 4, 1, 0, 0, 1)),
                new DecimalValue(decimalField, BigDecimal.ONE), new EnumValue(enumField, "CODE"),
                new StringsValue(stringsField, "value1", "value2", "value3", "value4")));
        entityes[7] = new Entity(id, entityClass, values, new EntityFamily(0, 0), version);

        id = Long.MAX_VALUE - 8;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 1996L), new StringValue(stringField, "中文测试chinese test2"),
                new BooleanValue(boolField, false),
                new DateTimeValue(dateTimeField, LocalDateTime.of(2020, 4, 1, 0, 0, 1)),
                new DecimalValue(decimalField, BigDecimal.ONE), new EnumValue(enumField, "CODE"),
                new StringsValue(stringsField, "value5", "value6", "value7", "value4")));
        entityes[8] = new Entity(id, entityClass, values, new EntityFamily(0, 0), version);

        id = Long.MAX_VALUE - 9;
        values = new EntityValue(id);
        values.addValues(Arrays.asList(new LongValue(longField, 445L), new StringValue(stringField, "hello world"),
                new BooleanValue(boolField, false),
                new DateTimeValue(dateTimeField, LocalDateTime.of(1900, 2, 1, 11, 18, 1)),
                new DecimalValue(decimalField, BigDecimal.ONE), new EnumValue(enumField, "CODE"),
                new StringsValue(stringsField, "value1", "value2", "value3")));
        entityes[9] = new Entity(id, entityClass, values, new EntityFamily(0, 0), version);


        return entityes;
    }
}
