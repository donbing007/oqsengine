package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation;

import com.google.protobuf.Value;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunctionFactory;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunctionFactoryImpl;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl.AvgFunction;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 测试类.
 *
 */
public class AggregationCalculationLogicTest {

    private AggregationFunctionFactory aggregationFunctionFactory;

    private List<IEntityClass> entityClasses = new ArrayList<>();

    @BeforeEach
    public void before() {
        // a <- b <- c
        //        <- c1
        // d <- e
        // a [id,longf,bigf,timef,relb.id]
        // b [id,julongf,jubigf,jutimef,relc.id,relc1.id ]
        // c [id,jujulongf]
        // c1[id,jujubigf]
        // d [id,longf,bigf,timef,rele.id]
        // e [id,jutimef]
        IEntityClass a = EntityClass.Builder.anEntityClass()
                .withId(1)
                .withCode("a")
                .withFields(Arrays.asList(
                        EntityField.Builder.anEntityField()
                                .withId(11)
                                .withName("longf")
                                .withFieldType(FieldType.LONG)
                                .withConfig(FieldConfig.Builder.anFieldConfig().build())
                                .build(),
                        EntityField.Builder.anEntityField()
                                .withId(12)
                                .withName("bigf")
                                .withFieldType(FieldType.DECIMAL)
                                .withConfig(FieldConfig.Builder.anFieldConfig().build())
                                .build(),
                        EntityField.Builder.anEntityField()
                                .withId(13)
                                .withName("timef")
                                .withFieldType(FieldType.DATETIME)
                                .withConfig(FieldConfig.Builder.anFieldConfig().build())
                                .build()))
                .withRelations(Arrays.asList(Relationship.Builder.anRelationship()
                        .withId(100)
                        .withCode("relb")
                        .withLeftEntityClassId(1)
                        .withLeftEntityClassCode("a")
                        .withRightEntityClassId(2)
                        .withBelongToOwner(false)
                        .withIdentity(false)
                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                        .build()))
                .build();
        IEntityClass b = EntityClass.Builder.anEntityClass()
                .withId(2)
                .withCode("b")
                .withFields(Arrays.asList(
                        EntityField.Builder.anEntityField()
                                .withId(21)
                                .withName("julongf")
                                .withFieldType(FieldType.LONG)
                                .withConfig(FieldConfig.Builder.anFieldConfig()
                                        .withCalculation(Aggregation.Builder.anAggregation()
                                                .withClassId(1)
                                                .withFieldId(11)
                                                .withRelationId(200)
                                                .withAggregationType(AggregationType.SUM)
                                                .build())
                                        .build())
                                .build(),
                        EntityField.Builder.anEntityField()
                                .withId(22)
                                .withName("jubigf")
                                .withFieldType(FieldType.DECIMAL)
                                .withConfig(FieldConfig.Builder.anFieldConfig()
                                        .withCalculation(Aggregation.Builder.anAggregation()
                                                .withClassId(1)
                                                .withFieldId(12)
                                                .withRelationId(200)
                                                .withAggregationType(AggregationType.MAX)
                                                .build())
                                        .build())
                                .build(),
                        EntityField.Builder.anEntityField()
                                .withId(23)
                                .withName("jutimef")
                                .withFieldType(FieldType.DATETIME)
                                .withConfig(FieldConfig.Builder.anFieldConfig()
                                        .withCalculation(Aggregation.Builder.anAggregation()
                                                .withClassId(1)
                                                .withFieldId(13)
                                                .withRelationId(200)
                                                .withAggregationType(AggregationType.MAX)
                                                .build())
                                        .build())
                                .build()))
                .withRelations(Arrays.asList(Relationship.Builder.anRelationship()
                        .withId(200)
                        .withCode("relb")
                        .withBelongToOwner(true)
                        .withIdentity(false)
                        .withLeftEntityClassId(2)
                        .withLeftEntityClassCode("b")
                        .withRightEntityClassId(1)
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .build()))
                .build();
        IEntityClass c = EntityClass.Builder.anEntityClass()
                .withId(3)
                .withCode("c")
                .withFields(Arrays.asList(
                        EntityField.Builder.anEntityField()
                                .withId(31)
                                .withName("jujulongf")
                                .withFieldType(FieldType.LONG)
                                .withConfig(FieldConfig.Builder.anFieldConfig()
                                        .withCalculation(Aggregation.Builder.anAggregation()
                                                .withClassId(2)
                                                .withFieldId(21)
                                                .withAggregationType(AggregationType.COUNT)
                                                .withRelationId(300)
                                                .build())
                                        .build())
                                .build()))
                .withRelations(Arrays.asList(Relationship.Builder.anRelationship()
                        .withId(300)
                        .withCode("relb")
                        .withBelongToOwner(true)
                        .withIdentity(false)
                        .withLeftEntityClassId(3)
                        .withLeftEntityClassCode("c")
                        .withRightEntityClassId(2)
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .build()))
                .build();
        IEntityClass c1 = EntityClass.Builder.anEntityClass()
                .withId(4)
                .withCode("c1")
                .withFields(Arrays.asList(
                        EntityField.Builder.anEntityField()
                                .withId(41)
                                .withName("jujulongf")
                                .withFieldType(FieldType.LONG)
                                .withConfig(FieldConfig.Builder.anFieldConfig()
                                        .withCalculation(Aggregation.Builder.anAggregation()
                                                .withClassId(2)
                                                .withFieldId(21)
                                                .withAggregationType(AggregationType.SUM)
                                                .withRelationId(400)
                                                .build())
                                        .build())
                                .build()))
                .withRelations(Arrays.asList(Relationship.Builder.anRelationship()
                        .withId(400)
                        .withCode("relb")
                        .withBelongToOwner(true)
                        .withIdentity(false)
                        .withLeftEntityClassId(3)
                        .withLeftEntityClassCode("c")
                        .withRightEntityClassId(2)
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .build()))
                .build();
        IEntityClass d = EntityClass.Builder.anEntityClass()
                .withId(5)
                .withCode("d")
                .withFields(Arrays.asList(
                        EntityField.Builder.anEntityField()
                                .withId(51)
                                .withName("longf")
                                .withFieldType(FieldType.LONG)
                                .withConfig(FieldConfig.Builder.anFieldConfig().build())
                                .build(),
                        EntityField.Builder.anEntityField()
                                .withId(52)
                                .withName("bigf")
                                .withFieldType(FieldType.DECIMAL)
                                .withConfig(FieldConfig.Builder.anFieldConfig().build())
                                .build(),
                        EntityField.Builder.anEntityField()
                                .withId(53)
                                .withName("timef")
                                .withFieldType(FieldType.DATETIME)
                                .withConfig(FieldConfig.Builder.anFieldConfig().build())
                                .build()))
                .withRelations(Arrays.asList(Relationship.Builder.anRelationship()
                        .withId(500)
                        .withCode("rele")
                        .withBelongToOwner(false)
                        .withIdentity(false)
                        .withLeftEntityClassId(5)
                        .withLeftEntityClassCode("d")
                        .withRightEntityClassId(6)
                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                        .build()))
                .build();
        IEntityClass e = EntityClass.Builder.anEntityClass()
                .withId(6)
                .withCode("e")
                .withFields(Arrays.asList(
                        EntityField.Builder.anEntityField()
                                .withId(61)
                                .withName("jujulongf")
                                .withFieldType(FieldType.LONG)
                                .withConfig(FieldConfig.Builder.anFieldConfig()
                                        .withCalculation(Aggregation.Builder.anAggregation()
                                                .withClassId(5)
                                                .withFieldId(51)
                                                .withAggregationType(AggregationType.SUM)
                                                .withRelationId(600)
                                                .build())
                                        .build())
                                .build()))
                .withRelations(Arrays.asList(Relationship.Builder.anRelationship()
                        .withId(600)
                        .withCode("reld")
                        .withBelongToOwner(true)
                        .withIdentity(false)
                        .withLeftEntityClassId(6)
                        .withLeftEntityClassCode("e")
                        .withRightEntityClassId(5)
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .build()))
                .build();
        entityClasses.add(a);
        entityClasses.add(b);
        entityClasses.add(c);
        entityClasses.add(c1);
        entityClasses.add(d);
        entityClasses.add(e);

        // 构建context
    }

//    @Test
//    public void calculate() {
//
//        IEntity entity = Entity.Builder.anEntity()
//                .withId(1)
//                .withVersion(1)
//                .withEntityClassRef(entityClasses.get(0).ref())
//                .withTime(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
//                .withEntityValue(EntityValue.build().addValues(Arrays.asList(
//                        new LongValue(entityClasses.get(0).field(11).get(), 100),
//                        new DecimalValue(entityClasses.get(0).field(12).get(), new BigDecimal("100")),
//                        new DateTimeValue(entityClasses.get(0).field(13).get(), LocalDateTime.now())
//                ))).build();
//        IEntityField targetField = entityClasses.get(1).field(21).get();
//        long targetFieldId = ((Aggregation) targetField.config().getCalculation()).getFieldId();
//        AggregationType aggregationType = ((Aggregation) targetField.config().getCalculation()).getAggregationType();
//
//        Optional<IValue> n = entity.entityValue().getValue(targetFieldId);
//
//        // 获取当前的原始版本.
//        Optional<IValue> o = Optional.empty();
//        Optional<IEntity> entityOptional = Optional.of(Entity.Builder.anEntity()
//                .withId(1)
//                .withVersion(1)
//                .withEntityClassRef(entityClasses.get(0).ref())
//                .withTime(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
//                .withEntityValue(EntityValue.build().addValues(Arrays.asList(
//                        new LongValue(entityClasses.get(0).field(11).get(), 200),
//                        new DecimalValue(entityClasses.get(0).field(12).get(), new BigDecimal("100")),
//                        new DateTimeValue(entityClasses.get(0).field(13).get(), LocalDateTime.now())
//                ))).build());
//        if (entityOptional.isPresent()) {
//            o = entityOptional.get().entityValue().getValue(targetFieldId);
//        }
//        Optional<IValue> targetValue;
//        AggregationFunction function = AggregationFunctionFactoryImpl.getAggregationFunction(aggregationType);
//        if (aggregationType.equals(AggregationType.AVG)) {
//            int count = 1;
//            // 求平均值需要count信息
//            targetValue = ((AvgFunction) function).excuteAvg(o, o, n, count);
//        } else {
//            targetValue = function.excute(o, o, n);
//        }
//        Assertions.assertNotNull(targetValue.get());
//    }

    @Test
    public void calculate() {

        IEntity entity = Entity.Builder.anEntity()
                .withId(1)
                .withVersion(1)
                .withEntityClassRef(entityClasses.get(0).ref())
                .withTime(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .withEntityValue(EntityValue.build().addValues(Arrays.asList(
                        new LongValue(entityClasses.get(0).field(11).get(), 100),
                        new DecimalValue(entityClasses.get(0).field(12).get(), new BigDecimal("100")),
                        new DateTimeValue(entityClasses.get(0).field(13).get(), LocalDateTime.now())
                ))).build();
        IEntityField targetField = entityClasses.get(1).field(21).get();
        long targetFieldId = ((Aggregation) targetField.config().getCalculation()).getFieldId();
        AggregationType aggregationType = ((Aggregation) targetField.config().getCalculation()).getAggregationType();

        Optional<IValue> n = entity.entityValue().getValue(targetFieldId);

        // 获取当前的原始版本.
        Optional<IValue> o = Optional.empty();
        Optional<IEntity> entityOptional = Optional.of(Entity.Builder.anEntity()
                .withId(1)
                .withVersion(1)
                .withEntityClassRef(entityClasses.get(0).ref())
                .withTime(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .withEntityValue(EntityValue.build().addValues(Arrays.asList(
                        new LongValue(entityClasses.get(0).field(11).get(), 200),
                        new DecimalValue(entityClasses.get(0).field(12).get(), new BigDecimal("100")),
                        new DateTimeValue(entityClasses.get(0).field(13).get(), LocalDateTime.now())
                ))).build());
        if (entityOptional.isPresent()) {
            o = entityOptional.get().entityValue().getValue(targetFieldId);
        }
        Optional<IValue> targetValue;
        AggregationFunction function = AggregationFunctionFactoryImpl.getAggregationFunction(aggregationType);
        if (aggregationType.equals(AggregationType.AVG)) {
            int count = 1;
            // 求平均值需要count信息
            targetValue = ((AvgFunction) function).excuteAvg(o, o, n, count);
        } else {
            targetValue = function.excute(o, o, n);
        }
        Assertions.assertNotNull(targetValue.get());
    }

}
