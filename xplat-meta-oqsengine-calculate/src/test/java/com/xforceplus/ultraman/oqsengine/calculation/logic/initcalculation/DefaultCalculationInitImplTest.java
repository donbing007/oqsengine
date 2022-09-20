package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation;

import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory.AggregationInitLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory.FormulaInitLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory.InitIvalueFactory;
import com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory.InitIvalueLogic;
import com.xforceplus.ultraman.oqsengine.calculation.utils.CalculationComparator;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InitCalculationParticipant;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Formula;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultCalculationInitImplTest {
    private DefaultCalculationInitImpl defaultCalculationInit;

    private InitIvalueFactory initIvalueFactory;

    private MockMetaManager metaManager;


    private static enum FieldIndex {
        A_LONG,
        B_SUM_CONDITION,
        B_SUM,
        B_LONG,
        B_FORMULA1,
        B_FORMULA2
    }

    private static enum ClassIndex {
        A_CLASS,
        B_CLASS,
    }

    private static enum RelationIndex {
        B_A,
        A_B,
    }

    private static IEntityClass A_CLASS;

    private static IEntityClass B_CLASS;

    private static long getFieldId(FieldIndex fieldIndex) {
        // 避免和默认ID系统字段冲突,因为默认的EntityField.ID_ENTITY_FIELD的id是 Long.MAX_VALUE.
        return Long.MAX_VALUE - 1000 - fieldIndex.ordinal();
    }

    private static long getClassId(ClassIndex classIndex) {
        return Long.MAX_VALUE - classIndex.ordinal();
    }

    private static long getRelationIndex(RelationIndex relationIndex) {
        return Long.MAX_VALUE - relationIndex.ordinal();
    }



    // 被聚合
    private static IEntityField A_LONG = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.A_LONG))
            .withFieldType(FieldType.LONG)
            .withName("along").build();

    private static IEntityField B_SUM_CONDITION = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.B_SUM_CONDITION))
            .withFieldType(FieldType.LONG)
            .withName("bsumacondition")
            .withConfig(
                    FieldConfig.Builder.anFieldConfig()
                            .withCalculation(
                                    Aggregation.Builder.anAggregation()
                                            .withAggregationType(AggregationType.SUM)
                                            .withRelationId(getRelationIndex(RelationIndex.B_A))
                                            .withClassId(getClassId(ClassIndex.A_CLASS))
                                            .withFieldId(getFieldId(FieldIndex.A_LONG))
                                            .withConditions(
                                                    Conditions.buildEmtpyConditions()
                                                            .addAnd(
                                                                    new Condition(
                                                                            A_LONG,
                                                                            ConditionOperator.NOT_EQUALS,
                                                                            new LongValue(A_LONG, 0L)
                                                                    )
                                                            )
                                            )
                                            .build()
                            ).build()
            )
            .build();

    private static IEntityField B_SUM = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.B_SUM))
            .withFieldType(FieldType.LONG)
            .withName("bsuma")
            .withConfig(
                    FieldConfig.Builder.anFieldConfig()
                            .withCalculation(
                                    Aggregation.Builder.anAggregation()
                                            .withAggregationType(AggregationType.SUM)
                                            .withRelationId(getRelationIndex(RelationIndex.B_A))
                                            .withClassId(getClassId(ClassIndex.A_CLASS))
                                            .withFieldId(getFieldId(FieldIndex.A_LONG))
                                            .build()
                            ).build()
            )
            .build();

    private static IEntityField B_LONG = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.B_LONG))
            .withFieldType(FieldType.LONG)
            .withName("b-long").build();

    private static IEntityField B_FORMULA1 = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.B_FORMULA1))
            .withFieldType(FieldType.LONG)
            .withName("bformula1")
            .withConfig(
                    FieldConfig.Builder.anFieldConfig()
                            .withCalculation(
                                    Formula.Builder.anFormula()
                                            .withLevel(0)
                                            .withFailedPolicy(Formula.FailedPolicy.THROW_EXCEPTION)
                                            .withExpression("return ${b-long} + ${bsuma};")
                                            .withArgs(Arrays.asList("b-long", "bsuma"))
                                            .build()
                            ).build()
            ).build();


    private static IEntityField B_FORMULA2 = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.B_FORMULA2))
            .withFieldType(FieldType.LONG)
            .withName("bformula2")
            .withConfig(
                    FieldConfig.Builder.anFieldConfig()
                            .withCalculation(
                                    Formula.Builder.anFormula()
                                            .withLevel(1)
                                            .withFailedPolicy(Formula.FailedPolicy.THROW_EXCEPTION)
                                            .withExpression("return ${b-long} + ${bformula1};")
                                            .withArgs(Arrays.asList("b-long", "bformula1"))
                                            .build()
                            ).build()
            ).build();



    static {
        A_CLASS = EntityClass.Builder.anEntityClass()
                .withId(getClassId(ClassIndex.A_CLASS))
                .withCode("a-class")
                .withRelations(
                        Arrays.asList(
                                Relationship.Builder.anRelationship()
                                        .withId(getRelationIndex(RelationIndex.B_A))
                                        .withCode("b_a")
                                        .withBelongToOwner(true)
                                        .withLeftEntityClassId(getClassId(ClassIndex.A_CLASS))
                                        .withLeftEntityClassCode("a-class")
                                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                                        .withStrong(true)
                                        .withRightEntityClassId(getClassId(ClassIndex.B_CLASS))
                                        .withRightEntityClassLoader((along, s) -> Optional.of(B_CLASS))
                                        .withRightFamilyEntityClassLoader(along -> Arrays.asList(B_CLASS))
                                        .build(),
                                Relationship.Builder.anRelationship()
                                        .withId(getRelationIndex(RelationIndex.A_B))
                                        .withCode("a_b")
                                        .withBelongToOwner(true)
                                        .withLeftEntityClassId(getClassId(ClassIndex.A_CLASS))
                                        .withLeftEntityClassCode("a-class")
                                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                                        .withStrong(true)
                                        .withRightEntityClassId(getClassId(ClassIndex.B_CLASS))
                                        .withRightEntityClassLoader((along, s) -> Optional.of(B_CLASS))
                                        .withRightFamilyEntityClassLoader(along -> Arrays.asList(B_CLASS))
                                        .build()
                        )
                )
                .withField(A_LONG).build();


        B_CLASS = EntityClass.Builder.anEntityClass()
                .withId(getClassId(ClassIndex.B_CLASS))
                .withCode("b-class")
                .withField(B_SUM)
                .withField(B_LONG)
                .withField(B_SUM_CONDITION)
                .withField(B_FORMULA1)
                .withField(B_FORMULA2)
                .withRelations(
                        Arrays.asList(
                                Relationship.Builder.anRelationship()
                                        .withId(getRelationIndex(RelationIndex.B_A))
                                        .withCode("b_a")
                                        .withBelongToOwner(false)
                                        .withLeftEntityClassId(getClassId(ClassIndex.B_CLASS))
                                        .withLeftEntityClassCode("b-class")
                                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                                        .withStrong(true)
                                        .withRightEntityClassId(getClassId(ClassIndex.A_CLASS))
                                        .withRightEntityClassLoader((along, s) -> Optional.of(A_CLASS))
                                        .withRightFamilyEntityClassLoader(along -> Arrays.asList(A_CLASS))
                                        .build(),
                                Relationship.Builder.anRelationship()
                                        .withId(getRelationIndex(RelationIndex.A_B))
                                        .withCode("a_b")
                                        .withBelongToOwner(false)
                                        .withLeftEntityClassId(getClassId(ClassIndex.B_CLASS))
                                        .withLeftEntityClassCode("b-class")
                                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                                        .withStrong(true)
                                        .withRightEntityClassId(getClassId(ClassIndex.A_CLASS))
                                        .withRightEntityClassLoader((along, s) -> Optional.of(A_CLASS))
                                        .withRightFamilyEntityClassLoader(along -> Arrays.asList(A_CLASS))
                                        .build()
                        )
                ).build();


    }

    private IEntity entityA = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(new LongValue(A_LONG, 100L))
            .build();


    private IEntity entityB1 = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE - 1)
            .withEntityClassRef(B_CLASS.ref())
            .withValue(new LongValue(B_LONG, 100L))
            .build();


    private IEntity entityB2 = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE - 2)
            .withEntityClassRef(B_CLASS.ref())
            .withValue(new LongValue(B_LONG, 100L))
            .withValue(new LongValue(B_SUM, 100L))
            .build();


    private IEntity entityB3 = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE - 3)
            .withEntityClassRef(B_CLASS.ref())
            .withValue(new LongValue(B_SUM, 100L))
            .build();

    private static InitInstance B_INSTANCE1;

    private static InitInstance B_INSTANCE2;



    @BeforeEach
    void before() throws NoSuchFieldException, IllegalAccessException {
        metaManager = new MockMetaManager();
        metaManager.addEntityClass(A_CLASS);
        metaManager.addEntityClass(B_CLASS);

        initIvalueFactory = new InitIvalueFactory();
        Map<CalculationType, InitIvalueLogic> logicMap = new HashMap<>();
        logicMap.put(CalculationType.FORMULA, new MockFormulaInitLogic());
        logicMap.put(CalculationType.AGGREGATION, new MockAggregationInitLogic());
        initIvalueFactory.setInitIvalueLogicMap(logicMap);

        B_INSTANCE1 = buildInstance(entityB1, B_CLASS, (List<IEntityField>) B_CLASS.fields()).get();
        B_INSTANCE2 = buildInstance(entityB2, B_CLASS, (List<IEntityField>) B_CLASS.fields()).get();


        defaultCalculationInit = new DefaultCalculationInitImpl();

        Field ivalueFactory = DefaultCalculationInitImpl.class.getDeclaredField("initIvalueFactory");
        ivalueFactory.setAccessible(true);
        ivalueFactory.set(defaultCalculationInit, initIvalueFactory);

        Field manager = DefaultCalculationInitImpl.class.getDeclaredField("metaManager");
        manager.setAccessible(true);
        manager.set(defaultCalculationInit, metaManager);
    }
    
    
    @Test
    void init() {
        IEntity init = defaultCalculationInit.init(B_INSTANCE1);
        Assertions.assertEquals(init, entityB1);
    }

    @Test
    void testInit() {
        List<IEntity> init = defaultCalculationInit.init(Arrays.asList(B_INSTANCE1, B_INSTANCE2));
        Assertions.assertEquals(2, init.size());
    }

    private Optional<InitInstance> buildInstance(IEntity entity, IEntityClass entityClass, List<IEntityField> fields) {
        // 计算字段排序
        List<IEntityField> sortedFields = fields.stream().filter(field -> field.calculationType().equals(CalculationType.FORMULA)
                        || field.calculationType().equals(CalculationType.AGGREGATION))
                .sorted(CalculationComparator.getInstance()).collect(Collectors.toList());

        // 最小重算单元排序
        List<InitInstanceUnit> initInstanceUnits = sortedFields.stream()
                .map(field -> new InitInstanceUnit(entity, entityClass, field))
                .collect(Collectors.toList());

        // 构建重算实例单元
        return initInstanceUnits.isEmpty() ? Optional.empty() : Optional.of(new InitInstance(entity, entityClass, initInstanceUnits));
    }


    static class MockAggregationInitLogic extends AggregationInitLogic {
        @Override
        public IEntity init(IEntity entity, InitCalculationParticipant participant) throws RuntimeException {
            return entity;
        }
    }

    static class MockFormulaInitLogic extends FormulaInitLogic {
        @Override
        public IEntity init(IEntity entity, InitCalculationParticipant participant) throws RuntimeException {
            return entity;
        }
    }
}