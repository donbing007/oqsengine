//package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation;
//
//import com.google.protobuf.Value;
//import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
//import com.xforceplus.ultraman.oqsengine.calculation.context.DefaultCalculationContext;
//import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
//import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunctionFactory;
//import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunctionFactoryImpl;
//import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl.AvgFunction;
//import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
//import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
//import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
//import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
//import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
//import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
//import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
//import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.*;
//import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
//import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
//import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
//import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
//import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
//import com.xforceplus.ultraman.oqsengine.storage.ConditionsSelectStorage;
//import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
//import org.junit.Assert;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.math.BigDecimal;
//import java.sql.SQLException;
//import java.time.LocalDateTime;
//import java.time.ZoneOffset;
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * 测试类.
// *
// */
//public class AggregationCalculationLogicTest {
//
//    final Logger logger = LoggerFactory.getLogger(AggregationCalculationLogicTest.class);
//
//    public static final int ONE = 1;
//
//    public static final int ZERO = 0;
//
//    private List<IEntityClass> entityClasses = new ArrayList<>();
//
//    private CalculationContext context;
//
//    private AggregationCalculationLogic aggregationCalculationLogic;
//
//    @BeforeEach
//    public void before() {
//        // a <- b <- c
//        //        <- c1
//        // d <- e
//        // a [id,longf,bigf,timef,relb.id]
//        // b [id,julongf,jubigf,jutimef,relc.id,relc1.id ]
//        // c [id,jujulongf]
//        // c1[id,jujubigf]
//        // d [id,longf,bigf,timef,rele.id]
//        // e [id,jutimef]
//        IEntityClass a = EntityClass.Builder.anEntityClass()
//                .withId(1)
//                .withCode("a")
//                .withFields(Arrays.asList(
//                        EntityField.Builder.anEntityField()
//                                .withId(11)
//                                .withName("longf")
//                                .withFieldType(FieldType.LONG)
//                                .withConfig(FieldConfig.Builder.anFieldConfig().build())
//                                .build(),
//                        EntityField.Builder.anEntityField()
//                                .withId(12)
//                                .withName("bigf")
//                                .withFieldType(FieldType.DECIMAL)
//                                .withConfig(FieldConfig.Builder.anFieldConfig().build())
//                                .build(),
//                        EntityField.Builder.anEntityField()
//                                .withId(13)
//                                .withName("timef")
//                                .withFieldType(FieldType.DATETIME)
//                                .withConfig(FieldConfig.Builder.anFieldConfig().build())
//                                .build()))
//                .withRelations(Arrays.asList(Relationship.Builder.anRelationship()
//                        .withId(100)
//                        .withCode("relb")
//                        .withLeftEntityClassId(1)
//                        .withLeftEntityClassCode("a")
//                        .withRightEntityClassId(2)
//                        .withBelongToOwner(false)
//                        .withIdentity(false)
//                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
//                        .build()))
//                .build();
//        IEntityClass b = EntityClass.Builder.anEntityClass()
//                .withId(2)
//                .withCode("b")
//                .withFields(Arrays.asList(
//                        EntityField.Builder.anEntityField()
//                                .withId(21)
//                                .withName("julongf")
//                                .withFieldType(FieldType.LONG)
//                                .withConfig(FieldConfig.Builder.anFieldConfig()
//                                        .withCalculation(Aggregation.Builder.anAggregation()
//                                                .withClassId(1)
//                                                .withFieldId(11)
//                                                .withRelationId(200)
//                                                .withAggregationType(AggregationType.SUM)
//                                                .build())
//                                        .build())
//                                .build(),
//                        EntityField.Builder.anEntityField()
//                                .withId(22)
//                                .withName("jubigf")
//                                .withFieldType(FieldType.DECIMAL)
//                                .withConfig(FieldConfig.Builder.anFieldConfig()
//                                        .withCalculation(Aggregation.Builder.anAggregation()
//                                                .withClassId(1)
//                                                .withFieldId(12)
//                                                .withRelationId(200)
//                                                .withAggregationType(AggregationType.MAX)
//                                                .build())
//                                        .build())
//                                .build(),
//                        EntityField.Builder.anEntityField()
//                                .withId(23)
//                                .withName("jutimef")
//                                .withFieldType(FieldType.DATETIME)
//                                .withConfig(FieldConfig.Builder.anFieldConfig()
//                                        .withCalculation(Aggregation.Builder.anAggregation()
//                                                .withClassId(1)
//                                                .withFieldId(13)
//                                                .withRelationId(200)
//                                                .withAggregationType(AggregationType.MAX)
//                                                .build())
//                                        .build())
//                                .build()))
//                .withRelations(Arrays.asList(Relationship.Builder.anRelationship()
//                        .withId(200)
//                        .withCode("relb")
//                        .withBelongToOwner(true)
//                        .withIdentity(false)
//                        .withLeftEntityClassId(2)
//                        .withLeftEntityClassCode("b")
//                        .withRightEntityClassId(1)
//                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
//                        .build()))
//                .build();
//        IEntityClass c = EntityClass.Builder.anEntityClass()
//                .withId(3)
//                .withCode("c")
//                .withFields(Arrays.asList(
//                        EntityField.Builder.anEntityField()
//                                .withId(31)
//                                .withName("jujulongf")
//                                .withFieldType(FieldType.LONG)
//                                .withConfig(FieldConfig.Builder.anFieldConfig()
//                                        .withCalculation(Aggregation.Builder.anAggregation()
//                                                .withClassId(2)
//                                                .withFieldId(21)
//                                                .withAggregationType(AggregationType.COUNT)
//                                                .withRelationId(300)
//                                                .build())
//                                        .build())
//                                .build()))
//                .withRelations(Arrays.asList(Relationship.Builder.anRelationship()
//                        .withId(300)
//                        .withCode("relb")
//                        .withBelongToOwner(true)
//                        .withIdentity(false)
//                        .withLeftEntityClassId(3)
//                        .withLeftEntityClassCode("c")
//                        .withRightEntityClassId(2)
//                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
//                        .build()))
//                .build();
//        IEntityClass c1 = EntityClass.Builder.anEntityClass()
//                .withId(4)
//                .withCode("c1")
//                .withFields(Arrays.asList(
//                        EntityField.Builder.anEntityField()
//                                .withId(41)
//                                .withName("jujulongf")
//                                .withFieldType(FieldType.LONG)
//                                .withConfig(FieldConfig.Builder.anFieldConfig()
//                                        .withCalculation(Aggregation.Builder.anAggregation()
//                                                .withClassId(2)
//                                                .withFieldId(21)
//                                                .withAggregationType(AggregationType.SUM)
//                                                .withRelationId(400)
//                                                .build())
//                                        .build())
//                                .build()))
//                .withRelations(Arrays.asList(Relationship.Builder.anRelationship()
//                        .withId(400)
//                        .withCode("relb")
//                        .withBelongToOwner(true)
//                        .withIdentity(false)
//                        .withLeftEntityClassId(3)
//                        .withLeftEntityClassCode("c")
//                        .withRightEntityClassId(2)
//                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
//                        .build()))
//                .build();
//        IEntityClass d = EntityClass.Builder.anEntityClass()
//                .withId(5)
//                .withCode("d")
//                .withFields(Arrays.asList(
//                        EntityField.Builder.anEntityField()
//                                .withId(51)
//                                .withName("longf")
//                                .withFieldType(FieldType.LONG)
//                                .withConfig(FieldConfig.Builder.anFieldConfig().build())
//                                .build(),
//                        EntityField.Builder.anEntityField()
//                                .withId(52)
//                                .withName("bigf")
//                                .withFieldType(FieldType.DECIMAL)
//                                .withConfig(FieldConfig.Builder.anFieldConfig().build())
//                                .build(),
//                        EntityField.Builder.anEntityField()
//                                .withId(53)
//                                .withName("timef")
//                                .withFieldType(FieldType.DATETIME)
//                                .withConfig(FieldConfig.Builder.anFieldConfig().build())
//                                .build()))
//                .withRelations(Arrays.asList(Relationship.Builder.anRelationship()
//                        .withId(500)
//                        .withCode("rele")
//                        .withBelongToOwner(false)
//                        .withIdentity(false)
//                        .withLeftEntityClassId(5)
//                        .withLeftEntityClassCode("d")
//                        .withRightEntityClassId(6)
//                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
//                        .build()))
//                .build();
//        IEntityClass e = EntityClass.Builder.anEntityClass()
//                .withId(6)
//                .withCode("e")
//                .withFields(Arrays.asList(
//                        EntityField.Builder.anEntityField()
//                                .withId(61)
//                                .withName("jujulongf")
//                                .withFieldType(FieldType.LONG)
//                                .withConfig(FieldConfig.Builder.anFieldConfig()
//                                        .withCalculation(Aggregation.Builder.anAggregation()
//                                                .withClassId(5)
//                                                .withFieldId(51)
//                                                .withAggregationType(AggregationType.SUM)
//                                                .withRelationId(600)
//                                                .build())
//                                        .build())
//                                .build()))
//                .withRelations(Arrays.asList(Relationship.Builder.anRelationship()
//                        .withId(600)
//                        .withCode("reld")
//                        .withBelongToOwner(true)
//                        .withIdentity(false)
//                        .withLeftEntityClassId(6)
//                        .withLeftEntityClassCode("e")
//                        .withRightEntityClassId(5)
//                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
//                        .build()))
//                .build();
//        entityClasses.add(a);
//        entityClasses.add(b);
//        entityClasses.add(c);
//        entityClasses.add(c1);
//        entityClasses.add(d);
//        entityClasses.add(e);
//
//        // 构建context
//        context = new DefaultCalculationContext();
//        aggregationCalculationLogic = new AggregationCalculationLogic();
//    }
//
//    @Test
//    public void calculate() {
//        IEntity aggEntity = Entity.Builder.anEntity()
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
//        context.focusEntity(aggEntity, entityClasses.get(0));
//        context.focusField(targetField);
//
//        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
//
//        Assert.assertNotNull(targetValue);
//
//    }
//
//    /**
//     * 根据条件和id来判断这条数据是否符合聚合范围.
//     *
//     * @param entity 被聚合数据.
//     * @param entityClass 被聚合对象.
//     * @param conditions 条件信息.
//     * @return 是否符合.
//     */
//    private boolean checkEntityByCondition(IEntity entity, IEntityClass entityClass,
//                                           Conditions conditions, ConditionsSelectStorage conditionsSelectStorage) {
//        if (conditions == null || conditions.size() == 0) {
//            return true;
//        }
//        conditions.addAnd(new Condition(entityClass.field("id").get(),
//                ConditionOperator.EQUALS, entity.entityValue().getValue(entity.id()).get()));
//        Collection<EntityRef> entityRefs = null;
//        try {
//            entityRefs = conditionsSelectStorage.select(conditions, entityClass, SelectConfig.Builder.anSelectConfig().build());
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        if (entityRefs != null && entityRefs.size() > ZERO) {
//            return true;
//        }
//        return false;
//    }
//
//    /**
//     * 得到统计值.
//     *
//     * @param aggregation 聚合配置.
//     * @param sourceEntity 来源实例.
//     * @param entityClass 对象结构.
//     * @param metaManager meta.
//     * @param conditionsSelectStorage 条件查询.
//     * @return 统计数字.
//     */
//    private int countAggregationEntity(Aggregation aggregation, IEntity sourceEntity, IEntityClass entityClass,
//                                       MetaManager metaManager, ConditionsSelectStorage conditionsSelectStorage) {
//        // 得到count值
//        Optional<IEntityClass> aggEntityClass =
//                metaManager.load(aggregation.getClassId(), sourceEntity.entityClassRef().getProfile());
//        int count = 1;
//        if (aggEntityClass.isPresent()) {
//            Conditions conditions = aggregation.getConditions();
//            // 根据关系id得到关系字段
//            Optional<IEntityField> entityField = entityClass.field(aggregation.getRelationId());
//            if (entityField.isPresent()) {
//                conditions.addAnd(new Condition(aggEntityClass.get().ref(), entityField.get(),
//                        ConditionOperator.EQUALS, aggregation.getRelationId(),
//                        sourceEntity.entityValue().getValue(sourceEntity.id()).get()));
//            }
//            Collection<EntityRef> entityRefs = null;
//            try {
//                entityRefs = conditionsSelectStorage.select(conditions, entityClass, SelectConfig.Builder.anSelectConfig().build());
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//            if (entityRefs != null && entityRefs.size() > ZERO) {
//                count = entityRefs.size();
//            }
//        }
//        return count;
//    }
//
//}
