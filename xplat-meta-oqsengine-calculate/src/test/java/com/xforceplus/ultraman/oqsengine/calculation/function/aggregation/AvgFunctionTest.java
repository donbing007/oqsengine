package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl.AvgFunction;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * 平均值测试.
 *
 * @className: AvgFunctionTest
 * @package: com.xforceplus.ultraman.oqsengine.calculation.function
 * @author: wangzheng
 * @date: 2021/8/26 19:40
 */
public class AvgFunctionTest {

    //-------------level 0--------------------
    private IEntityField l0LongField = EntityField.Builder.anEntityField()
        .withId(1000)
        .withFieldType(FieldType.LONG)
        .withName("l0-long")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l0StringField = EntityField.Builder.anEntityField()
        .withId(1001)
        .withFieldType(FieldType.STRING)
        .withName("l0-string")
        .withConfig(FieldConfig.Builder.anFieldConfig()
            .withSearchable(true)
            .withFuzzyType(FieldConfig.FuzzyType.WILDCARD)
            .withWildcardMinWidth(3).withWildcardMaxWidth(7).build()).build();
    private IEntityField l0StringsField = EntityField.Builder.anEntityField()
        .withId(1002)
        .withFieldType(FieldType.STRINGS)
        .withName("l0-strings")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l0EnumField = EntityField.Builder.anEntityField()
        .withId(1003)
        .withFieldType(FieldType.ENUM)
        .withName("l0-enum")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l0DecimalField = EntityField.Builder.anEntityField()
        .withId(1004)
        .withFieldType(FieldType.DECIMAL)
        .withName("l0-decimal")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l0DatetimeField = EntityField.Builder.anEntityField()
        .withId(1005)
        .withFieldType(FieldType.DATETIME)
        .withName("l0-datetime")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityClass l0EntityClass = EntityClass.Builder.anEntityClass()
        .withId(1)
        .withLevel(0)
        .withCode("l0")
        .withField(l0LongField)
        .withField(l0StringField)
        .withField(l0StringsField)
        .withField(l0EnumField)
        .withField(l0DecimalField)
        .withField(l0DatetimeField)
        .build();

    //-------------level 1--------------------
    private IEntityField l1LongField = EntityField.Builder.anEntityField()
        .withId(2000)
        .withFieldType(FieldType.LONG)
        .withName("l1-long")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l1StringField = EntityField.Builder.anEntityField()
        .withId(2001)
        .withFieldType(FieldType.STRING)
        .withName("l1-string")
        .withConfig(FieldConfig.Builder.anFieldConfig()
            .withSearchable(true)
            .withFuzzyType(FieldConfig.FuzzyType.WILDCARD)
            .withWildcardMinWidth(3).withWildcardMaxWidth(7).build()).build();
    private IEntityClass l1EntityClass = EntityClass.Builder.anEntityClass()
        .withId(2)
        .withLevel(1)
        .withCode("l1")
        .withField(l1LongField)
        .withField(l1StringField)
        .withFather(l0EntityClass)
        .build();

    //-------------level 2--------------------
    private IEntityField l2LongField = EntityField.Builder.anEntityField()
        .withId(3000)
        .withFieldType(FieldType.LONG)
        .withName("l2-long")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l2StringField = EntityField.Builder.anEntityField()
        .withId(3001)
        .withFieldType(FieldType.STRING)
        .withName("l2-string")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l2bigintField = EntityField.Builder.anEntityField()
        .withId(3002)
        .withFieldType(FieldType.LONG)
        .withName("l2-bigint")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField l2StringSegmentationField = EntityField.Builder.anEntityField()
        .withId(3003)
        .withFieldType(FieldType.STRING)
        .withName("l2-string-segmentation")
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withFuzzyType(FieldConfig.FuzzyType.SEGMENTATION).withSearchable(true).build()).build();
    private IEntityClass l2EntityClass = EntityClass.Builder.anEntityClass()
        .withId(3)
        .withLevel(2)
        .withCode("l2")
        .withField(l2LongField)
        .withField(l2StringField)
        .withField(l2bigintField)
        .withField(l2StringSegmentationField)
        .withFather(l1EntityClass)
        .build();

    @Test
    public void excute() {
        AvgFunction avgFunction = new AvgFunction();
        DecimalValue agg = new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("1000.10"));
        DecimalValue o = new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("2000.10"));
        DecimalValue n = new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("3000.10"));
        Optional<IValue> a = avgFunction.excute(agg, o, n);
        System.out.println(a.get().getValue());


        LongValue agg1 = new LongValue(l2EntityClass.field("l1-long").get(), 1000);
        LongValue o1 = new LongValue(l2EntityClass.field("l1-long").get(), 2000);
        LongValue n1 = new LongValue(l2EntityClass.field("l1-long").get(), 3000);
        Optional<IValue> a1 = avgFunction.excute(agg1, o1, n1);
        System.out.println(a1.get().getValue());
    }

    @Test
    public void init() {
        AvgFunction avgFunction = new AvgFunction();
        DecimalValue agg = new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("1000.10"));
        DecimalValue o = new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("2000.10"));
        DecimalValue n = new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("3000.10"));
        Optional<IValue> a = avgFunction.init(agg, Arrays.asList(o, n));
        System.out.println(a.get().getValue());


        LongValue agg1 = new LongValue(l2EntityClass.field("l1-long").get(), 1000);
        LongValue o1 = new LongValue(l2EntityClass.field("l1-long").get(), 2000);
        LongValue n1 = new LongValue(l2EntityClass.field("l1-long").get(), 3000);
        Optional<IValue> a1 = avgFunction.init(agg1, Arrays.asList(o1, n1));
        System.out.println(a1.get().getValue());
    }

    @Test
    public void excuteCount() {
        AvgFunction avgFunction = new AvgFunction();
        DecimalValue agg = new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("1000.10"));
        DecimalValue o = new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("2000.10"));
        DecimalValue n = new DecimalValue(l2EntityClass.field("l0-decimal").get(), new BigDecimal("3000.10"));
        Optional<IValue> a = avgFunction.excuteAvg(agg, o, n, 3);
        System.out.println(a.get().getValue());


        LongValue agg1 = new LongValue(l2EntityClass.field("l1-long").get(), 1000);
        LongValue o1 = new LongValue(l2EntityClass.field("l1-long").get(), 2000);
        LongValue n1 = new LongValue(l2EntityClass.field("l1-long").get(), 3000);
        Optional<IValue> a1 = avgFunction.excuteAvg(agg1, o1, n1, 3);
        System.out.println(a1.get().getValue());
    }

}
