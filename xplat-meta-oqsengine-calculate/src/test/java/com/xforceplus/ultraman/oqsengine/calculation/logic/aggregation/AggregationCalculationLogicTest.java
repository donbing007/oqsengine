package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.context.DefaultCalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.dto.AffectedInfo;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.factory.CalculationLogicFactory;
import com.xforceplus.ultraman.oqsengine.calculation.logic.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.AbstractParticipant;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.CalculationParticipant;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InfuenceConsumer;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Participant;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
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
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.storage.ConditionsSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OqsEngineEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionExclusiveAction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResourceType;
import com.xforceplus.ultraman.oqsengine.storage.transaction.accumulator.TransactionAccumulator;
import com.xforceplus.ultraman.oqsengine.storage.transaction.hint.TransactionHint;
import com.xforceplus.ultraman.oqsengine.task.Task;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import com.xforceplus.ultraman.oqsengine.task.TaskRunner;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试类.
 * <pre>
 *  A
 *  |- B(SUM)
 *  |   |- D(SUM)
 *  |- B_LONG
 *  |   |- D(MAX(condition))
 *  |- C(COUNT)
 *  |- C(SUM)
 *  |- C(SUM(condition))
 *  |- C(COUNT(condition))
 *  |- C(SUM(condition-along))
 *  |- C(MIN(condition)
 *  |- C(MAX(condition)
 * </pre>
 * <em>注意: 所有的测试都基于发起聚合的实体一定早于被聚合的实体被创建.</em>
 */
public class AggregationCalculationLogicTest {

    private AggregationCalculationLogic aggregationCalculationLogic;

    private static IEntityField A_LONG;

    private static IEntityField A_STRING;

    private static IEntityField S_STRING;

    private static IEntityField A_REF;

    private static IEntityField B_REF;

    private static IEntityField B_SUM;

    private static IEntityField B_LONG;

    private static IEntityField C_SUM;

    private static IEntityField C_COUNT;

    private static IEntityField C_SUM_CONDITIONS;

    private static IEntityField C_MIN_CONDITIONS;

    private static IEntityField C_MAX_CONDITIONS;

    private static IEntityField C_COUNT_CONDITIONS;

    private static IEntityField C_SUM_CONDITIONS_ASTRING;

    private static IEntityField D_SUM;

    private static IEntityField D_MAX_CONDITIONS;

    private static IEntityField F_COLLECT;

    private static IEntityClass A_CLASS;

    private static IEntityClass B_CLASS;

    private static IEntityClass C_CLASS;

    private static IEntityClass D_CLASS;

    private static IEntityClass F_CLASS;

    private static IEntityClass S_CLASS;

    private IEntity entityA;

    private IEntity entityB;

    private IEntity entityD;

    private IEntity entityC;

    private IEntity entityS;

    private IEntity entityF;

    private AggregationCalculationLogicTest.MockLogic aggregationLogic;

    private MockMetaManager metaManager;
    private MockMasterStorage masterStorage;

    private MockConditionSelectStorage conditionSelectStorage;

    enum ClassIndex {
        A_CLASS,
        B_CLASS,
        C_CLASS,
        D_CLASS,

        //  collect
        F_CLASS,
        S_CLASS
    }

    enum FieldIndex {
        A_LONG,
        A_STRING,
        A_REF,
        B_REF,
        B_SUM,
        B_LONG,
        C_SUM,
        C_COUNT,
        C_SUM_CONDITIONS,
        C_MIN_CONDITIONS,
        C_MAX_CONDITIONS,
        C_COUNT_CONDITIONS,
        C_SUM_CONDITIONS_ASTRING,
        D_SUM,
        D_MAX_CONDITIONS,
        S_STRING,
        F_COLLECT
    }

    /**
     * 初始化.
     */
    @BeforeEach
    public void before() {

        A_LONG = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.A_LONG))
            .withFieldType(FieldType.LONG)
            .withName("a-long").build();

        A_STRING = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.A_STRING))
            .withFieldType(FieldType.STRING)
            .withName("a-string").build();

        A_REF = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.A_REF))
            .withFieldType(FieldType.LONG)
            .withName("relc").build();

        B_REF = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.B_REF))
            .withFieldType(FieldType.LONG)
            .withName("relb").build();

        B_SUM = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.B_SUM))
            .withFieldType(FieldType.LONG)
            .withName("b-sum-a")
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                        .withClassId(getClassId(ClassIndex.A_CLASS))
                        .withFieldId(getFieldId(FieldIndex.A_LONG))
                        .withRelationId(Long.MAX_VALUE - 10)
                        .withAggregationType(AggregationType.SUM)
                        .build()
                    ).build()
            )
            .build();

        B_LONG = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.B_LONG))
            .withFieldType(FieldType.LONG)
            .withName("b-long")
            .build();

        C_SUM = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.C_SUM))
            .withFieldType(FieldType.LONG)
            .withName("c-sum-a")
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                        .withClassId(getClassId(ClassIndex.A_CLASS))
                        .withFieldId(getFieldId(FieldIndex.A_LONG))
                        .withRelationId(Long.MAX_VALUE - 20)
                        .withAggregationType(AggregationType.SUM)
                        .build()
                    ).build()
            )
            .build();

        C_COUNT = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.C_COUNT))
            .withFieldType(FieldType.LONG)
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                        .withClassId(getClassId(ClassIndex.A_CLASS))
                        .withFieldId(getFieldId(FieldIndex.A_LONG))
                        .withRelationId(Long.MAX_VALUE - 20)
                        .withAggregationType(AggregationType.COUNT)
                        .build()
                    ).build()
            )
            .withName("c-count-a").build();

        // sum的条件聚合
        C_SUM_CONDITIONS = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.C_SUM_CONDITIONS))
            .withFieldType(FieldType.LONG)
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                        .withClassId(getClassId(ClassIndex.A_CLASS))
                        .withFieldId(getFieldId(FieldIndex.A_LONG))
                        .withRelationId(Long.MAX_VALUE - 20)
                        .withConditions(
                            Conditions.buildEmtpyConditions()
                                .addAnd(
                                    new Condition(
                                        A_LONG, ConditionOperator.GREATER_THAN, new LongValue(A_LONG, 100L)
                                    )
                                )
                        )
                        .withAggregationType(AggregationType.SUM)
                        .build()
                    ).build()
            )
            .withName("c-sum-conditions").build();

        C_COUNT_CONDITIONS = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.C_COUNT_CONDITIONS))
            .withFieldType(FieldType.LONG)
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                        .withClassId(getClassId(ClassIndex.A_CLASS))
                        .withFieldId(getFieldId(FieldIndex.A_LONG))
                        .withRelationId(Long.MAX_VALUE - 20)
                        .withConditions(
                            Conditions.buildEmtpyConditions()
                                .addAnd(
                                    new Condition(
                                        A_LONG, ConditionOperator.GREATER_THAN, new LongValue(A_LONG, 100L)
                                    )
                                )
                        )
                        .withAggregationType(AggregationType.COUNT)
                        .build()
                    ).build()
            )
            .withName("c-count-conditions").build();

        C_MIN_CONDITIONS = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.C_MIN_CONDITIONS))
            .withFieldType(FieldType.LONG)
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                        .withClassId(getClassId(ClassIndex.A_CLASS))
                        .withFieldId(getFieldId(FieldIndex.A_LONG))
                        .withRelationId(A_REF.id())
                        .withConditions(
                            Conditions.buildEmtpyConditions()
                                .addAnd(
                                    new Condition(A_LONG, ConditionOperator.GREATER_THAN, new LongValue(A_LONG, 100L))
                                )
                        )
                        .withAggregationType(AggregationType.MIN)
                        .build()
                    ).build()
            )
            .withName("c-min-conditions").build();

        C_MAX_CONDITIONS = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.C_MAX_CONDITIONS))
            .withFieldType(FieldType.LONG)
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                        .withClassId(getClassId(ClassIndex.A_CLASS))
                        .withFieldId(getFieldId(FieldIndex.A_LONG))
                        .withRelationId(A_REF.id())
                        .withConditions(
                            Conditions.buildEmtpyConditions()
                                .addAnd(
                                    new Condition(A_LONG, ConditionOperator.GREATER_THAN, new LongValue(A_LONG, 100L))
                                )
                        )
                        .withAggregationType(AggregationType.MAX)
                        .build()
                    ).build()
            )
            .withName("c-min-conditions").build();

        C_SUM_CONDITIONS_ASTRING = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.C_SUM_CONDITIONS_ASTRING))
            .withFieldType(FieldType.LONG)
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                        .withClassId(getClassId(ClassIndex.A_CLASS))
                        .withFieldId(getFieldId(FieldIndex.A_LONG))
                        .withRelationId(Long.MAX_VALUE - 20)
                        .withConditions(
                            Conditions.buildEmtpyConditions()
                                .addAnd(
                                    new Condition(
                                        A_STRING, ConditionOperator.EQUALS, new StringValue(A_STRING, "v1")
                                    )
                                )
                        )
                        .withAggregationType(AggregationType.SUM)
                        .build()
                    ).build()
            )
            .withName("c-sum-conditoons-astring").build();

        D_SUM = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.D_SUM))
            .withFieldType(FieldType.LONG)
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                        .withClassId(getClassId(ClassIndex.B_CLASS))
                        .withFieldId(getFieldId(FieldIndex.B_SUM))
                        .withRelationId(Long.MAX_VALUE - 30)
                        .withAggregationType(AggregationType.SUM)
                        .build()
                    ).build()
            )
            .withName("d-sum-b").build();

        D_MAX_CONDITIONS = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.D_MAX_CONDITIONS))
            .withFieldType(FieldType.LONG)
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder.anAggregation()
                        .withClassId(getClassId(ClassIndex.B_CLASS))
                        .withFieldId(getFieldId(FieldIndex.B_LONG))
                        .withRelationId(Long.MAX_VALUE - 30)
                        .withAggregationType(AggregationType.MAX)
                        .withConditions(
                            Conditions.buildEmtpyConditions()
                                .addAnd(
                                    new Condition(B_SUM, ConditionOperator.GREATER_THAN, new LongValue(B_SUM, 1000L))
                                )
                        )
                        .build()
                    ).build()
            )
            .withName("d-max-condition-b").build();

        S_STRING = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.S_STRING))
            .withFieldType(FieldType.STRING)
            .withName("s-string").build();

        F_COLLECT = EntityField.Builder.anEntityField()
            .withId(getFieldId(FieldIndex.F_COLLECT))
            .withFieldType(FieldType.STRINGS)
            .withConfig(
                FieldConfig.Builder.anFieldConfig()
                    .withCalculation(Aggregation.Builder
                                        .anAggregation()
                                        .withClassId(getClassId(ClassIndex.S_CLASS))
                                        .withFieldId(getFieldId(FieldIndex.S_STRING))
                                        .withAggregationType(AggregationType.COLLECT)
                                        .withRelationId(Long.MAX_VALUE - 1000)
                                        .build()
                    ).build()
            )
            .withName("f-collect-s").build();

        A_CLASS = EntityClass.Builder.anEntityClass()
            .withId(getClassId(ClassIndex.A_CLASS))
            .withCode("a-class")
            .withFields(Arrays.asList(A_LONG, EntityField.ID_ENTITY_FIELD))
            .withRelations(Arrays.asList(
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE - 100)
                    .withCode("relb")
                    .withLeftEntityClassId(Long.MAX_VALUE)
                    .withLeftEntityClassCode("a-class")
                    .withRightEntityClassId(Long.MAX_VALUE - 1)
                    .withRightEntityClassLoader((id, a) -> Optional.of(B_CLASS))
                    .withEntityField(B_REF)
                    .withBelongToOwner(true)
                    .withIdentity(false)
                    .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                    .build(),
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE - 200)
                    .withCode("relc")
                    .withLeftEntityClassId(Long.MAX_VALUE)
                    .withLeftEntityClassCode("a-class")
                    .withRightEntityClassId(Long.MAX_VALUE - 2)
                    .withRightEntityClassLoader((id, a) -> Optional.of(C_CLASS))
                    .withBelongToOwner(true)
                    .withIdentity(false)
                    .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                    .build(),
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE - 10)
                    .withCode("relb")
                    .withLeftEntityClassId(Long.MAX_VALUE)
                    .withLeftEntityClassCode("b-class")
                    .withRightEntityClassId(Long.MAX_VALUE - 1)
                    .withRightEntityClassLoader((id, a) -> Optional.of(B_CLASS))
                    .withBelongToOwner(false)
                    .withIdentity(false)
                    .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                    .build(),
                Relationship.Builder.anRelationship()
                    .withId(A_REF.id())
                    .withCode("relc")
                    .withLeftEntityClassId(Long.MAX_VALUE)
                    .withLeftEntityClassCode("c-class")
                    .withRightEntityClassId(Long.MAX_VALUE - 2)
                    .withRightEntityClassLoader((id, a) -> Optional.of(C_CLASS))
                    .withBelongToOwner(true)
                    .withEntityField(A_REF)
                    .withIdentity(false)
                    .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                    .build()
            ))
            .build();

        B_CLASS = EntityClass.Builder.anEntityClass()
            .withId(getClassId(ClassIndex.B_CLASS))
            .withCode("b-class")
            .withFields(Arrays.asList(B_SUM, B_LONG, EntityField.ID_ENTITY_FIELD))
            .withRelations(Arrays.asList(
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE - 300)
                    .withCode("relc")
                    .withLeftEntityClassId(Long.MAX_VALUE - 1)
                    .withLeftEntityClassCode("b-class")
                    .withRightEntityClassId(Long.MAX_VALUE - 3)
                    .withRightEntityClassLoader((id, a) -> Optional.of(D_CLASS))
                    .withBelongToOwner(true)
                    .withIdentity(false)
                    .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                    .build(),
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE - 30)
                    .withCode("relc")
                    .withLeftEntityClassId(Long.MAX_VALUE - 1)
                    .withLeftEntityClassCode("d-class")
                    .withRightEntityClassId(Long.MAX_VALUE - 3)
                    .withRightEntityClassLoader((id, a) -> Optional.of(D_CLASS))
                    .withBelongToOwner(false)
                    .withIdentity(false)
                    .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                    .build(),
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE - 10)
                    .withCode("relb")
                    .withLeftEntityClassId(Long.MAX_VALUE - 1)
                    .withLeftEntityClassCode("b-class")
                    .withRightEntityClassId(Long.MAX_VALUE)
                    .withRightEntityClassLoader((id, a) -> Optional.of(A_CLASS))
                    .withBelongToOwner(false)
                    .withIdentity(false)
                    .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                    .build()
            ))
            .build();

        C_CLASS = EntityClass.Builder.anEntityClass()
            .withId(getClassId(ClassIndex.C_CLASS))
            .withCode("c-class")
            .withFields(
                Arrays.asList(
                    C_COUNT,
                    C_SUM,
                    C_COUNT_CONDITIONS,
                    C_SUM_CONDITIONS,
                    C_MIN_CONDITIONS,
                    C_MAX_CONDITIONS,
                    EntityField.ID_ENTITY_FIELD))
            .withRelations(Arrays.asList(
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE - 20)
                    .withCode("relc")
                    .withLeftEntityClassId(Long.MAX_VALUE - 2)
                    .withLeftEntityClassCode("c-class")
                    .withRightEntityClassId(Long.MAX_VALUE)
                    .withRightEntityClassLoader((id, a) -> Optional.of(A_CLASS))
                    .withBelongToOwner(false)
                    .withIdentity(false)
                    .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                    .build()
            ))
            .build();

        D_CLASS = EntityClass.Builder.anEntityClass()
            .withId(getClassId(ClassIndex.D_CLASS))
            .withCode("d-class")
            .withFields(Arrays.asList(D_SUM, D_MAX_CONDITIONS, EntityField.ID_ENTITY_FIELD))
            .withRelations(Arrays.asList(
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE - 30)
                    .withCode("relb")
                    .withLeftEntityClassId(Long.MAX_VALUE - 3)
                    .withLeftEntityClassCode("d-class")
                    .withRightEntityClassId(Long.MAX_VALUE - 1)
                    .withRightEntityClassLoader((id, a) -> Optional.of(B_CLASS))
                    .withBelongToOwner(false)
                    .withIdentity(false)
                    .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                    .build()
            ))
            .build();


        S_CLASS = EntityClass.Builder.anEntityClass()
            .withId(getClassId(ClassIndex.S_CLASS))
            .withCode("s-class")
            .withFields(Arrays.asList(S_STRING, EntityField.ID_ENTITY_FIELD))
            .withRelations(Arrays.asList(
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE - 1000)
                    .withCode("s-to-be-collected")
                    .withLeftEntityClassId(getClassId(ClassIndex.S_CLASS))
                    .withLeftEntityClassCode("s-class")
                    .withRightEntityClassId(getClassId(ClassIndex.F_CLASS))
                    .withRightEntityClassLoader((id, a) -> Optional.of(F_CLASS))
                    .withBelongToOwner(true)
                    .withIdentity(false)
                    .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                    .build()
            ))
            .build();

        F_CLASS = EntityClass.Builder.anEntityClass()
            .withId(getClassId(ClassIndex.F_CLASS))
            .withCode("f-class")
            .withFields(Arrays.asList(F_COLLECT, EntityField.ID_ENTITY_FIELD))
            .withRelations(Arrays.asList(
                Relationship.Builder.anRelationship()
                    .withId(Long.MAX_VALUE - 1001)
                    .withCode("f-collect")
                    .withLeftEntityClassId(getClassId(ClassIndex.F_CLASS))
                    .withLeftEntityClassCode("f-class")
                    .withRightEntityClassId(getClassId(ClassIndex.S_CLASS))
                    .withRightEntityClassLoader((id, a) -> Optional.of(S_CLASS))
                    .withBelongToOwner(false)
                    .withIdentity(false)
                    .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                    .build()
            ))
            .build();


        entityA = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(
                new LongValue(A_LONG, 100L)
            )
            .withValue(
                new StringValue(A_STRING, "v1")
            ).build();

        entityB = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE - 1)
            .withEntityClassRef(B_CLASS.ref())
            .withValue(
                new LongValue(B_SUM, 100L)
            ).build();

        entityD = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE - 2)
            .withEntityClassRef(D_CLASS.ref())
            .withValue(
                new LongValue(D_SUM, 100L)
            ).build();

        entityC = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE - 3)
            .withEntityClassRef(C_CLASS.ref())
            .withValue(
                new LongValue(C_COUNT, 100L)
            ).build();

        entityS = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE - 4)
            .withEntityClassRef(S_CLASS.ref())
            .withValue(
                new StringValue(S_STRING, S_STRING.name())
            )
            .build();

        entityF = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE - 5)
            .withEntityClassRef(F_CLASS.ref())
            .withValue(
                new StringsValue(F_COLLECT, new String[0], "")
            )
            .build();

        aggregationLogic = new AggregationCalculationLogicTest.MockLogic(CalculationType.AGGREGATION);

        Map<IEntityField, IValue> valueChange = new HashMap<>();
        valueChange.put(B_SUM, new LongValue(B_SUM, 200L));
        valueChange.put(D_SUM, new LongValue(D_SUM, 200L));
        valueChange.put(C_COUNT, new LongValue(C_COUNT, 200L));

        aggregationLogic.setValueChanage(valueChange);
        aggregationLogic.setNeedMaintenanceScenarios(
            new CalculationScenarios[] {CalculationScenarios.BUILD, CalculationScenarios.REPLACE,
                CalculationScenarios.DELETE
            });
        Map<AbstractParticipant, AbstractParticipant> scope = new HashMap<>();
        scope.put(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(A_LONG).build(),
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(B_SUM).build()
        );
        scope.put(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(A_LONG).build(),
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(C_CLASS)
                .withField(C_COUNT).build()
        );
        scope.put(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(B_SUM).build(),
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(D_CLASS)
                .withField(D_SUM).build()
        );
        scope.put(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(S_CLASS)
                .withField(S_STRING).build(),
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(F_CLASS)
                .withField(F_COLLECT).build()
        );
        aggregationLogic.setScope(scope);

        Map<AbstractParticipant, Collection<AffectedInfo>> entityIds = new HashMap<>();
        entityIds.put(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(B_CLASS)
                .withField(B_SUM).build(),
            Arrays.asList(
                new AffectedInfo(entityB, entityB.id())
            )
        );

        entityIds.put(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(C_CLASS)
                .withField(C_COUNT).build(),
            Arrays.asList(
                new AffectedInfo(entityC, entityC.id())
            )
        );

        entityIds.put(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(D_CLASS)
                .withField(D_SUM).build(),
            Arrays.asList(
                new AffectedInfo(entityD, entityD.id())
            )
        );

        entityIds.put(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(S_CLASS)
                .withField(S_STRING).build(),
            Arrays.asList(
                new AffectedInfo(entityF, entityF.id())
            )
        );
        aggregationLogic.setEntityIds(entityIds);

        metaManager = new MockMetaManager();
        metaManager.addEntityClass(A_CLASS);
        metaManager.addEntityClass(B_CLASS);
        metaManager.addEntityClass(C_CLASS);
        metaManager.addEntityClass(D_CLASS);
        metaManager.addEntityClass(F_CLASS);
        metaManager.addEntityClass(S_CLASS);

        masterStorage = new MockMasterStorage();
        masterStorage.addIEntity(entityA);
        masterStorage.addIEntity(entityB);
        masterStorage.addIEntity(entityC);
        masterStorage.addIEntity(entityD);
        masterStorage.addIEntity(entityS);
        masterStorage.addIEntity(entityF);

        conditionSelectStorage = new MockConditionSelectStorage();
        conditionSelectStorage.put(entityA);
        conditionSelectStorage.put(entityB);
        conditionSelectStorage.put(entityC);
        conditionSelectStorage.put(entityD);

        aggregationCalculationLogic = new AggregationCalculationLogic();
    }

    @Test
    public void testNoMatchCollect() {
        IEntity aggEntity = Entity.Builder.anEntity()
            .withId(100000L)
            .withEntityClassRef(F_CLASS.ref())
            .withValue(
                new StringsValue(F_COLLECT, new String[]{"no-match"}, "1")
            ).build();

        this.masterStorage.addIEntity(aggEntity);
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.REPLACE)
            .withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(aggEntity, F_CLASS);
        context.focusField(F_COLLECT);

        IEntity triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(S_CLASS.ref())
            .withValue(
                new StringValue(S_STRING, "TestReplace")
            ).build();

        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new EmptyTypedValue(S_STRING), new EmptyTypedValue(S_STRING)));
        context.startMaintenance(triggerEntity);
        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertEquals("no-match", targetValue.get().valueToString());
    }

    @Test
    public void testBuildCollect() {
        IEntity aggEntity = Entity.Builder.anEntity()
            .withId(100000L)
            .withEntityClassRef(F_CLASS.ref())
            .withValue(
                new StringsValue(F_COLLECT, new String[0], "")
            ).build();

        this.masterStorage.addIEntity(aggEntity);
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.REPLACE)
            .withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(aggEntity, F_CLASS);
        context.focusField(F_COLLECT);

        IEntity triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(S_CLASS.ref())
            .withValue(
                new StringValue(S_STRING, "TestReplace")
            ).build();

        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new EmptyTypedValue(S_STRING), new StringValue(S_STRING, "v1")));
        context.startMaintenance(triggerEntity);
        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertEquals("v1", targetValue.get().valueToString());
        Assertions.assertEquals("1", targetValue.get().getAttachment().get());
    }

    @Test
    public void testDeleteCollect() {
        IEntity aggEntity = Entity.Builder.anEntity()
            .withId(100000L)
            .withEntityClassRef(F_CLASS.ref())
            .withValue(
                new StringsValue(F_COLLECT, new String[0], "")
            ).build();

        this.masterStorage.addIEntity(aggEntity);
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.REPLACE)
            .withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(aggEntity, F_CLASS);
        context.focusField(F_COLLECT);

        IEntity triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(S_CLASS.ref())
            .withValue(
                new StringValue(S_STRING, "TestReplace")
            ).build();

        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new EmptyTypedValue(S_STRING), new StringValue(S_STRING, "v1")));
        context.startMaintenance(triggerEntity);
        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertEquals("v1", targetValue.get().valueToString());


        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new StringValue(S_STRING, "v1"), new EmptyTypedValue(S_STRING)));
        context.startMaintenance(triggerEntity);
        targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertEquals("", targetValue.get().valueToString());
        Assertions.assertEquals("", targetValue.get().getAttachment().get());
    }

    @Test
    public void testReplaceCollect() {
        IEntity aggEntity = Entity.Builder.anEntity()
            .withId(100000L)
            .withEntityClassRef(F_CLASS.ref())
            .withValue(
                new StringsValue(F_COLLECT, new String[0], "")
            ).build();

        this.masterStorage.addIEntity(aggEntity);
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.REPLACE)
            .withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(aggEntity, F_CLASS);
        context.focusField(F_COLLECT);

        IEntity triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(S_CLASS.ref())
            .withValue(
                new StringValue(S_STRING, "TestReplace")
            ).build();

        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new StringValue(S_STRING, "v1"), new StringValue(S_STRING, "v2")));
        context.startMaintenance(triggerEntity);
        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertEquals("v2", targetValue.get().valueToString());

    }

    /**
     * 对象原始满足,修改某些字段后不满足.
     * 应该被重新计算.
     */
    @Test
    public void testEntityMatchToNotMatch() {
        IEntity aggEntity = Entity.Builder.anEntity()
            .withId(100000L)
            .withEntityClassRef(C_CLASS.ref())
            .withValue(
                new LongValue(C_SUM_CONDITIONS_ASTRING, 90L)
            ).build();
        this.masterStorage.addIEntity(aggEntity);
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(aggEntity, C_CLASS);
        context.focusField(C_SUM_CONDITIONS_ASTRING);

        IEntity triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(
                new LongValue(A_LONG, 90L)
            )
            .withValue(
                new StringValue(A_STRING, "v2")
            ).build();
        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new StringValue(A_STRING, "v1"), new StringValue(A_STRING, "v2")));
        context.startMaintenance(triggerEntity);
        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertEquals(0L, targetValue.get().getValue());
    }

    /**
     * 对象原始不满足,修改某些字段后满足.
     * 应该被重新计算.
     */
    @Test
    public void testEntityNotMatchToMatch() {
        IEntity aggEntity = Entity.Builder.anEntity()
            .withId(100000L)
            .withEntityClassRef(C_CLASS.ref())
            .withValue(
                new LongValue(C_SUM_CONDITIONS_ASTRING, 0)
            ).build();
        this.masterStorage.addIEntity(aggEntity);
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(aggEntity, C_CLASS);
        context.focusField(C_SUM_CONDITIONS_ASTRING);

        IEntity triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(
                new LongValue(A_LONG, 90L)
            )
            .withValue(
                new StringValue(A_STRING, "v1")
            ).build();
        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new StringValue(A_STRING, "v2"), new StringValue(A_STRING, "v1")));
        context.startMaintenance(triggerEntity);
        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertEquals(90L, targetValue.get().getValue());
    }

    /**
     * max测试为分4种情况.
     * <ul>
     *    <li>旧值符合,新值不符合,需要重新计算.</li>
     *    <li>旧值不符合,新值符合, 需要重新计算.</li>
     *    <li>旧值不符合,新值不符合, 不进行计算.</li>
     *    <li>都符合,重新计算.</li>
     * </ul>
     * 这里测试情况1.
     */
    @Test
    public void testReplaceConditionMaxCase1() {
        IEntity aggEntity = Entity.Builder.anEntity()
            .withId(100000L)
            .withEntityClassRef(C_CLASS.ref())
            .withValue(
                new LongValue(C_MAX_CONDITIONS, 101, "1|101")
            ).build();

        this.masterStorage.addIEntity(aggEntity);
        this.conditionSelectStorage.put(aggEntity);
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withMasterStorage(this.masterStorage)
            .withConditionsSelectStorage(this.conditionSelectStorage)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(aggEntity, C_CLASS);
        context.focusField(C_MAX_CONDITIONS);

        IEntity triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(
                new LongValue(A_LONG, 90L)
            ).build();
        context.focusSourceEntity(triggerEntity);
        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new LongValue(A_LONG, 101L), new LongValue(A_LONG, 90L)));
        context.startMaintenance(triggerEntity);
        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertEquals(0L, targetValue.get().getValue());
    }

    /**
     * max测试为分4种情况.
     * <ul>
     *    <li>旧值符合,新值不符合,需要重新计算.</li>
     *    <li>旧值不符合,新值符合, 需要重新计算.</li>
     *    <li>旧值不符合,新值不符合, 不进行计算.</li>
     *    <li>都符合,重新计算.</li>
     * </ul>
     * 这里测试情况2.
     */
    @Test
    public void testReplaceConditionMaxCase2() {
        IEntity aggEntity = Entity.Builder.anEntity()
            .withId(100000L)
            .withEntityClassRef(C_CLASS.ref())
            .withValue(
                new LongValue(C_MAX_CONDITIONS, 0, "0|0")
            ).build();

        this.masterStorage.addIEntity(aggEntity);
        this.conditionSelectStorage.put(aggEntity);
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withMasterStorage(this.masterStorage)
            .withConditionsSelectStorage(this.conditionSelectStorage)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(aggEntity, C_CLASS);
        context.focusField(C_MAX_CONDITIONS);

        IEntity triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(
                new LongValue(A_LONG, 101L)
            ).build();
        context.focusSourceEntity(triggerEntity);
        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new LongValue(A_LONG, 90L), new LongValue(A_LONG, 101L)));
        context.startMaintenance(triggerEntity);
        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertEquals(101L, targetValue.get().getValue());
    }

    /**
     * max测试为分4种情况.
     * <ul>
     *    <li>旧值符合,新值不符合,需要重新计算最小值.</li>
     *    <li>旧值不符合,新值符合, 需要重新计算最小值.</li>
     *    <li>旧值不符合,新值不符合, 不进行计算.</li>
     *    <li>都符合,重新计算.</li>
     * </ul>
     * 这里测试情况3.
     */
    @Test
    public void testReplaceConditionMaxCase3() {
        IEntity aggEntity = Entity.Builder.anEntity()
            .withId(100000L)
            .withEntityClassRef(C_CLASS.ref())
            .withValue(
                new LongValue(C_MAX_CONDITIONS, 0, "0|0")
            ).build();

        this.masterStorage.addIEntity(aggEntity);
        this.conditionSelectStorage.put(aggEntity);
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withMasterStorage(this.masterStorage)
            .withConditionsSelectStorage(this.conditionSelectStorage)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(aggEntity, C_CLASS);
        context.focusField(C_MAX_CONDITIONS);

        IEntity triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(
                new LongValue(A_LONG, 99L)
            ).build();
        context.focusSourceEntity(triggerEntity);
        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new LongValue(A_LONG, 90L), new LongValue(A_LONG, 99L)));
        context.startMaintenance(triggerEntity);
        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertFalse(targetValue.isPresent());
    }

    /**
     * max测试为分4种情况.
     * <ul>
     *    <li>旧值符合,新值不符合,需要重新计算最小值.</li>
     *    <li>旧值不符合,新值符合, 需要重新计算最小值.</li>
     *    <li>旧值不符合,新值不符合, 不进行计算.</li>
     *    <li>都符合,重新计算.</li>
     * </ul>
     * 这里测试情况4.
     */
    @Test
    public void testReplaceConditionMaxCase4() {
        IEntity aggEntity = Entity.Builder.anEntity()
            .withId(100000L)
            .withEntityClassRef(C_CLASS.ref())
            .withValue(
                new LongValue(C_MIN_CONDITIONS, 101, "1|101")
            ).build();

        this.masterStorage.addIEntity(aggEntity);
        this.conditionSelectStorage.put(aggEntity);
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withMasterStorage(this.masterStorage)
            .withConditionsSelectStorage(this.conditionSelectStorage)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(aggEntity, C_CLASS);
        context.focusField(C_MIN_CONDITIONS);

        IEntity triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(
                new LongValue(A_LONG, 102L)
            ).build();
        this.masterStorage.addIEntity(triggerEntity);
        this.conditionSelectStorage.put(triggerEntity);
        context.focusSourceEntity(triggerEntity);
        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new LongValue(A_LONG, 101L), new LongValue(A_LONG, 102L)));
        context.startMaintenance(triggerEntity);
        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertEquals(102L, targetValue.get().getValue());
    }

    /**
     * min测试为分4种情况.
     * <ul>
     *    <li>旧值符合,新值不符合,需要重新计算最小值.</li>
     *    <li>旧值不符合,新值符合, 需要重新计算最小值.</li>
     *    <li>旧值不符合,新值不符合, 不进行计算.</li>
     *    <li>都符合,重新计算.</li>
     * </ul>
     * 这里测试情况1.
     */
    @Test
    public void testReplaceConditionMinCase1() {
        IEntity aggEntity = Entity.Builder.anEntity()
            .withId(100000L)
            .withEntityClassRef(C_CLASS.ref())
            .withValue(
                new LongValue(C_MIN_CONDITIONS, 101, "1|101")
            ).build();

        this.masterStorage.addIEntity(aggEntity);
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withMasterStorage(this.masterStorage)
            .withConditionsSelectStorage(this.conditionSelectStorage)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(aggEntity, C_CLASS);
        context.focusField(C_MIN_CONDITIONS);

        IEntity triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(
                new LongValue(A_LONG, 90L)
            ).build();
        context.focusSourceEntity(triggerEntity);
        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new LongValue(A_LONG, 101L), new LongValue(A_LONG, 90L)));
        context.startMaintenance(triggerEntity);
        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertEquals(0L, targetValue.get().getValue());
    }

    /**
     * min测试为分4种情况.
     * <ul>
     *    <li>旧值符合,新值不符合,需要重新计算最小值.</li>
     *    <li>旧值不符合,新值符合, 需要重新计算最小值.</li>
     *    <li>旧值不符合,新值不符合, 不进行计算.</li>
     *    <li>都符合,重新计算.</li>
     * </ul>
     * 这里测试情况2.
     */
    @Test
    public void testReplaceConditionMinCase2() {
        IEntity aggEntity = Entity.Builder.anEntity()
            .withId(100000L)
            .withEntityClassRef(C_CLASS.ref())
            .withValue(
                new LongValue(C_MIN_CONDITIONS, 0, "0|0")
            ).build();

        this.masterStorage.addIEntity(aggEntity);
        this.conditionSelectStorage.put(aggEntity);
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withMasterStorage(this.masterStorage)
            .withConditionsSelectStorage(this.conditionSelectStorage)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(aggEntity, C_CLASS);
        context.focusField(C_MIN_CONDITIONS);

        IEntity triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(
                new LongValue(A_LONG, 101L)
            ).build();
        context.focusSourceEntity(triggerEntity);
        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new LongValue(A_LONG, 90L), new LongValue(A_LONG, 101L)));
        context.startMaintenance(triggerEntity);
        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertEquals(101L, targetValue.get().getValue());
    }

    /**
     * min测试为分4种情况.
     * <ul>
     *    <li>旧值符合,新值不符合,需要重新计算最小值.</li>
     *    <li>旧值不符合,新值符合, 需要重新计算最小值.</li>
     *    <li>旧值不符合,新值不符合, 不进行计算.</li>
     *    <li>都符合,重新计算.</li>
     * </ul>
     * 这里测试情况3.
     */
    @Test
    public void testReplaceConditionMinCase3() {
        IEntity aggEntity = Entity.Builder.anEntity()
            .withId(100000L)
            .withEntityClassRef(C_CLASS.ref())
            .withValue(
                new LongValue(C_MIN_CONDITIONS, 0, "0|0")
            ).build();

        this.masterStorage.addIEntity(aggEntity);
        this.conditionSelectStorage.put(aggEntity);
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withMasterStorage(this.masterStorage)
            .withConditionsSelectStorage(this.conditionSelectStorage)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(aggEntity, C_CLASS);
        context.focusField(C_MIN_CONDITIONS);

        IEntity triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(
                new LongValue(A_LONG, 99L)
            ).build();
        context.focusSourceEntity(triggerEntity);
        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new LongValue(A_LONG, 90L), new LongValue(A_LONG, 99L)));
        context.startMaintenance(triggerEntity);
        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertFalse(targetValue.isPresent());
    }

    /**
     * min测试为分4种情况.
     * <ul>
     *    <li>旧值符合,新值不符合,需要重新计算最小值.</li>
     *    <li>旧值不符合,新值符合, 需要重新计算最小值.</li>
     *    <li>旧值不符合,新值不符合, 不进行计算.</li>
     *    <li>都符合,重新计算.</li>
     * </ul>
     * 这里测试情况4.
     */
    @Test
    public void testReplaceConditionMinCase4() {
        IEntity aggEntity = Entity.Builder.anEntity()
            .withId(100000L)
            .withEntityClassRef(C_CLASS.ref())
            .withValue(
                new LongValue(C_MIN_CONDITIONS, 101, "1|101")
            ).build();

        this.masterStorage.addIEntity(aggEntity);
        this.conditionSelectStorage.put(aggEntity);
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withMasterStorage(this.masterStorage)
            .withConditionsSelectStorage(this.conditionSelectStorage)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(aggEntity, C_CLASS);
        context.focusField(C_MIN_CONDITIONS);

        IEntity triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(
                new LongValue(A_LONG, 102L)
            ).build();
        this.masterStorage.addIEntity(triggerEntity);
        this.conditionSelectStorage.put(triggerEntity);
        context.focusSourceEntity(triggerEntity);
        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new LongValue(A_LONG, 101L), new LongValue(A_LONG, 102L)));
        context.startMaintenance(triggerEntity);
        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertEquals(102L, targetValue.get().getValue());
    }

    /**
     * count测试为分3种情况.
     * <ul>
     *    <li>旧值符合,新值不符合,统计数量减1.</li>
     *    <li>旧值不符合,新值符合, 统计数量加1.</li>
     *    <li>旧值不符合,新值不符合/都符合, 不进行计算.</li>
     * </ul>
     * 这里测试情况1.
     */
    @Test
    public void testReplaceConditionCountCase1() {
        IEntity aggEntity = Entity.Builder.anEntity()
            .withId(100000L)
            .withEntityClassRef(C_CLASS.ref())
            .withValue(
                new LongValue(C_COUNT_CONDITIONS, 1)
            ).build();
        this.masterStorage.addIEntity(aggEntity);
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(aggEntity, C_CLASS);
        context.focusField(C_COUNT_CONDITIONS);

        IEntity triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(
                new LongValue(A_LONG, 90L)
            ).build();
        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new LongValue(A_LONG, 200L), new LongValue(A_LONG, 90L)));
        context.startMaintenance(triggerEntity);
        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertEquals(0L, targetValue.get().getValue());
    }

    /**
     * count测试为分3种情况.
     * <ul>
     *    <li>旧值符合,新值不符合,统计数量减1.</li>
     *    <li>旧值不符合,新值符合, 统计数量加1.</li>
     *    <li>旧值不符合,新值不符合/都符合, 不进行计算.</li>
     * </ul>
     * 这里测试情况2.
     */
    @Test
    public void testReplaceConditionCountCase2() {
        IEntity aggEntity = Entity.Builder.anEntity()
            .withId(100000L)
            .withEntityClassRef(C_CLASS.ref())
            .withValue(
                new LongValue(C_COUNT_CONDITIONS, 0)
            ).build();
        this.masterStorage.addIEntity(aggEntity);
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(aggEntity, C_CLASS);
        context.focusField(C_COUNT_CONDITIONS);

        IEntity triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(
                new LongValue(A_LONG, 200L)
            ).build();
        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new LongValue(A_LONG, 90L), new LongValue(A_LONG, 200L)));
        context.startMaintenance(triggerEntity);
        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertEquals(1L, targetValue.get().getValue());
    }

    /**
     * count测试为分3种情况.
     * <ul>
     *    <li>旧值符合,新值不符合,统计数量减1.</li>
     *    <li>旧值不符合,新值符合, 统计数量加1.</li>
     *    <li>旧值不符合,新值不符合/都符合, 不进行计算.</li>
     * </ul>
     * 这里测试情况3.
     */
    @Test
    public void testReplaceConditionCountCase3() {
        IEntity aggEntity = Entity.Builder.anEntity()
            .withId(100000L)
            .withEntityClassRef(C_CLASS.ref())
            .withValue(
                new LongValue(C_COUNT_CONDITIONS, 0)
            ).build();
        this.masterStorage.addIEntity(aggEntity);
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(aggEntity, C_CLASS);
        context.focusField(C_COUNT_CONDITIONS);

        IEntity triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(
                new LongValue(A_LONG, 90L)
            ).build();
        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new LongValue(A_LONG, 30L), new LongValue(A_LONG, 90L)));
        context.startMaintenance(triggerEntity);
        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertFalse(targetValue.isPresent());
    }

    /**
     * sum测试为分4种情况.
     * <ul>
     *    <li>旧值符合,新值不符合,需要减去原有旧值.</li>
     *    <li>旧值不符合,新值符合, 需要增加新值.</li>
     *    <li>旧值不符合,新值不符合, 不进行计算.</li>
     *    <li>都符合,重新计算.</li>
     * </ul>
     * 这里测试情况1.
     */
    @Test
    public void testReplaceConditionSumCase1() {
        IEntity aggEntity = Entity.Builder.anEntity()
            .withId(100000L)
            .withEntityClassRef(C_CLASS.ref())
            .withValue(
                new LongValue(C_SUM_CONDITIONS, 200L)
            ).build();
        this.masterStorage.addIEntity(aggEntity);
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(aggEntity, C_CLASS);
        context.focusField(C_SUM_CONDITIONS);

        IEntity triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(
                new LongValue(A_LONG, 90L)
            ).build();
        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new LongValue(A_LONG, 200L), new LongValue(A_LONG, 90L)));
        context.startMaintenance(triggerEntity);
        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertEquals(0L, targetValue.get().getValue());
    }

    /**
     * sum测试为分4种情况.
     * <ul>
     *    <li>旧值符合,新值不符合,需要减去原有旧值.</li>
     *    <li>旧值不符合,新值符合, 需要增加新值.</li>
     *    <li>旧值不符合,新值不符合, 不进行计算.</li>
     *    <li>都符合,重新计算.</li>
     * </ul>
     * 这里测试情况2.
     */
    @Test
    public void testReplaceConditionSumCase2() {
        IEntity aggEntity = Entity.Builder.anEntity()
            .withId(100000L)
            .withEntityClassRef(C_CLASS.ref())
            .withValue(
                new LongValue(C_SUM_CONDITIONS, 0)
            ).build();
        this.masterStorage.addIEntity(aggEntity);
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(aggEntity, C_CLASS);
        context.focusField(C_SUM_CONDITIONS);

        IEntity triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(
                new LongValue(A_LONG, 200L)
            ).build();
        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new LongValue(A_LONG, 90L), new LongValue(A_LONG, 200L)));
        context.startMaintenance(triggerEntity);
        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertEquals(200L, targetValue.get().getValue());
    }

    /**
     * sum测试为分4种情况.
     * <ul>
     *    <li>旧值符合,新值不符合,需要减去原有旧值.</li>
     *    <li>旧值不符合,新值符合, 需要增加新值.</li>
     *    <li>旧值不符合,新值不符合, 不进行计算.</li>
     *    <li>都符合,重新计算.</li>
     * </ul>
     * 这里测试情况3.
     */
    @Test
    public void testReplaceConditionSumCase3() {
        IEntity aggEntity = Entity.Builder.anEntity()
            .withId(100000L)
            .withEntityClassRef(C_CLASS.ref())
            .withValue(
                new LongValue(C_SUM_CONDITIONS, 0)
            ).build();
        this.masterStorage.addIEntity(aggEntity);
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(aggEntity, C_CLASS);
        context.focusField(C_SUM_CONDITIONS);

        IEntity triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(
                new LongValue(A_LONG, 91L)
            ).build();
        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new LongValue(A_LONG, 90L), new LongValue(A_LONG, 91L)));
        context.startMaintenance(triggerEntity);
        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
        // 应该空值返回,表示不作修改.
        Assertions.assertFalse(targetValue.isPresent());
    }

    @Test
    public void testBuildConditionSum() {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.BUILD).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(entityC, C_CLASS);
        context.focusField(C_SUM_CONDITIONS);

        IEntity triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(
                new LongValue(A_LONG, 100L)
            ).build();

        // 目标是一个不应该被聚合的.
        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new EmptyTypedValue(A_LONG), new LongValue(A_LONG, 100L)));
        context.startMaintenance(triggerEntity);
        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertFalse(targetValue.isPresent());

        /*
        另一次创建,应该被聚合.由于需要保证entityC被targetEntity早创建,所以这里为entityC增加一个 "c_sum_conditions"的默认值1.
         */
        entityC.entityValue().addValue(
            new LongValue(C_SUM_CONDITIONS, 1)
        );
        context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.BUILD).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(entityC, C_CLASS);
        context.focusField(C_SUM_CONDITIONS);

        triggerEntity = Entity.Builder.anEntity()
            .withId(1)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(
                new LongValue(A_LONG, 101L)
            ).build();
        context.putEntityToCache(triggerEntity);
        context.addValueChange(
            ValueChange.build(1, new EmptyTypedValue(A_LONG), new LongValue(A_LONG, 100L)));
        context.startMaintenance(triggerEntity);
        targetValue = aggregationCalculationLogic.calculate(context);
        Assertions.assertEquals(101L, targetValue.get().valueToLong());
    }

    @Test
    public void testBuildCalculation() {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.BUILD).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(entityB, B_CLASS);
        context.focusField(B_SUM);
        context.putEntityToCache(entityA);
        context.addValueChange(
            ValueChange.build(entityA.id(), new EmptyTypedValue(A_LONG), new LongValue(A_LONG, 200L)));

        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);

        Assert.assertNotNull(targetValue);

    }

    @Test
    public void testReplaceCalculation() {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(entityB, B_CLASS);
        context.focusField(B_SUM);
        context.putEntityToCache(entityA);
        context.addValueChange(
            ValueChange.build(entityA.id(), new LongValue(A_LONG, 100L), new LongValue(A_LONG, 200L)));

        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);

        Assert.assertNotNull(targetValue);

    }

    @Test
    public void testRemoveCalculation() {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.DELETE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(entityB, B_CLASS);
        context.focusField(B_SUM);
        context.putEntityToCache(entityA);
        context.addValueChange(
            ValueChange.build(entityA.id(), new LongValue(A_LONG, 10L), new EmptyTypedValue(A_LONG)));

        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);

        Assert.assertNotNull(targetValue);

    }

    /**
     * 测试条件scope.<br />
     * 预计会构造出如下的影响树.
     * <pre>
     *  A_long
     *  |- B(SUM)
     *  |   |- D(SUM)
     *  |   |- D(MAX(condition))  B(SUM) > 1000 虽然不是指向的字段没有改变,但是被影响的字段出现在条件中.
     *  |- C(SUM)
     *  |- C(SUM(condition))   A_long > 100 由于目标是A所以应该出现.
     *  |- C(MIN(condition)    A_long > 100 由于目标是A所以应该出现.
     *  |- C(MAX(condition)    A_long > 100 由于目标是A所以应该出现.
     *
     *  S_String
     *  |- F(COLLECT)          S_string
     * </pre>
     */
    @Test
    public void testConditionScope() {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withScenarios(CalculationScenarios.BUILD)
            .build();
        IEntity sourceEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(
                new LongValue(A_LONG, 2000L)
            ).build();
        Infuence infuence = new Infuence(
            sourceEntity,
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(A_LONG)
                .withAffectedEntities(Arrays.asList(sourceEntity)).build(),
            ValueChange.build(
                sourceEntity.id(),
                new LongValue(A_LONG, 1000L),
                new LongValue(A_LONG, 2000L))
        );
        context.focusEntity(sourceEntity, A_CLASS);
        context.focusField(A_LONG);

        aggregationCalculationLogic.scope(context, infuence);

        String expected = "(a-class,a-long)\n"
            + "   L---(c-class,c-min-conditions)\n"
            + "   L---(c-class,c-min-conditions)\n"
            + "   L---(c-class,c-sum-conditions)\n"
            + "   L---(c-class,c-sum-a)\n"
            + "   L---(b-class,b-sum-a)\n"
            + "      L---(d-class,d-max-condition-b)\n"
            + "      L---(d-class,d-sum-b)";

        Assertions.assertEquals(expected, infuence.toString());
    }

    @Test
    public void scope() {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext().build();

        IEntity targetEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(
                new LongValue(A_LONG, 100)
            ).build();
        Infuence infuence = new Infuence(
            targetEntity,
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(A_LONG)
                .withAffectedEntities(Arrays.asList(targetEntity)).build(),
            ValueChange.build(
                targetEntity.id(),
                new LongValue(A_LONG, 50L),
                new LongValue(A_LONG, 100L))
        );

        context.focusEntity(targetEntity, A_CLASS);
        context.focusField(A_LONG);

        aggregationCalculationLogic.scope(context, infuence);
        List<IEntityField> participants = new ArrayList<>();
        infuence.scan((parentParticipant, participant, infuenceInner) -> {

            participants.add(participant.getField());

            return InfuenceConsumer.Action.CONTINUE;
        });

        Assertions.assertEquals(7, participants.size());
        Assertions.assertEquals(A_LONG, participants.get(0));
        Assertions.assertEquals(B_SUM, participants.get(1));
        Assertions.assertEquals(C_SUM, participants.get(2));
        Assertions.assertEquals(C_SUM_CONDITIONS, participants.get(3));
        Assertions.assertEquals(C_MIN_CONDITIONS, participants.get(4));
        Assertions.assertEquals(C_MAX_CONDITIONS, participants.get(5));
        Assertions.assertEquals(D_SUM, participants.get(6));

        // Count 场景
        context.focusEntity(targetEntity, A_CLASS);
        context.focusField(EntityField.ID_ENTITY_FIELD);

        Infuence infuenceCount = new Infuence(
            targetEntity,
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(EntityField.ID_ENTITY_FIELD)
                .withAffectedEntities(Arrays.asList(targetEntity)).build(),
            ValueChange.build(
                context.getFocusEntity().id(),
                new EmptyTypedValue(EntityField.ID_ENTITY_FIELD),
                new LongValue(EntityField.ID_ENTITY_FIELD, context.getFocusEntity().id())
            )
        );

        context.addValueChange(
            ValueChange.build(
                context.getFocusEntity().id(),
                new EmptyTypedValue(EntityField.ID_ENTITY_FIELD),
                new LongValue(EntityField.ID_ENTITY_FIELD, context.getFocusEntity().id())
            )
        );

        aggregationCalculationLogic.scope(context, infuenceCount);
        List<Participant> participantsCount = new ArrayList<>();
        infuenceCount.scan((parentParticipant, participant, infuenceInner) -> {

            participantsCount.add(participant);

            return InfuenceConsumer.Action.CONTINUE;
        });


    }

    @Test
    public void getMaintainTarget() throws SQLException {
        AggregationCalculationLogicTest.MockTaskCoordinator coordinator =
            new AggregationCalculationLogicTest.MockTaskCoordinator();
        AggregationCalculationLogicTest.MockTransaction tx = new AggregationCalculationLogicTest.MockTransaction();

        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withTransaction(tx)
            .withTaskCoordinator(coordinator)
            .build();

        Relationship relationship = A_CLASS.relationship().stream().filter(r ->
            r.getRightEntityClass("").equals(B_CLASS)).collect(Collectors.toList()).get(0);

        IEntity targetEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(A_CLASS.ref())
            .withValue(new LongValue(A_LONG, 100))
            .withValue(new LongValue(relationship.getEntityField(), 1000))
            .build();

        Infuence infuence = new Infuence(
            targetEntity,
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(A_LONG)
                .withAffectedEntities(Arrays.asList(targetEntity)).build(),
            ValueChange.build(
                targetEntity.id(),
                new LongValue(A_LONG, 50L),
                new LongValue(A_LONG, 100L))
        );

        context.focusEntity(targetEntity, A_CLASS);
        context.focusField(A_LONG);

        aggregationCalculationLogic.scope(context, infuence);
        AtomicReference<Participant> p = new AtomicReference<>();
        infuence.scan((parentParticipant, participant, infuenceInner) -> {
            if (parentParticipant.isPresent()) {
                if (parentParticipant.get().getEntityClass().id() == A_CLASS.id()) {
                    p.set(participant);
                    return InfuenceConsumer.Action.OVER;
                }
            }
            return InfuenceConsumer.Action.CONTINUE;
        });

        Participant participant = p.get();
        Collection<AffectedInfo> affectedInfos =
            aggregationCalculationLogic.getMaintainTarget(context, participant, Arrays.asList(targetEntity));
        Assertions.assertEquals(1, affectedInfos.size());
    }

    static class MockMasterStorage implements MasterStorage {

        private Map<Long, IEntity> entities = new HashMap<>();

        private List<IEntity> replaceEntities = new ArrayList<>();

        @Override
        public void replace(EntityPackage entityPackage) throws SQLException {
            int[] results = new int[entityPackage.size()];
            Arrays.fill(results, 1);

            entityPackage.stream().forEach(e -> {
                replaceEntities.add(e.getKey());
                e.getKey().neat();
            });

        }

        public void addIEntity(IEntity entity) {
            entities.put(entity.id(), entity);
        }

        public List<IEntity> getReplaceEntities() {
            return replaceEntities;
        }

        @Override
        public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, SelectConfig config)
            throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<IEntity> selectOne(long id) throws SQLException {
            return Optional.ofNullable(entities.get(id));
        }

        @Override
        public Optional<IEntity> selectOne(long id, IEntityClass entityClass) throws SQLException {
            return Optional.ofNullable(entities.get(id));
        }

        @Override
        public Collection<IEntity> selectMultiple(long[] ids) throws SQLException {
            return Arrays.stream(ids)
                .mapToObj(id -> Optional.of(entities.get(id)))
                .filter(e -> e.isPresent())
                .map(e -> e.get()).collect(Collectors.toList());
        }

        @Override
        public Collection<IEntity> selectMultiple(long[] ids, IEntityClass entityClass) throws SQLException {
            return selectMultiple(ids);
        }

        @Override
        public int exist(long id) throws SQLException {
            IEntity entity = entities.get(id);
            if (entity == null) {
                return -1;
            } else {
                return entity.version();
            }
        }

        @Override
        public DataIterator<OqsEngineEntity> iterator(IEntityClass entityClass, long startTime, long endTime,
                                                      long lastId) throws SQLException {
            return null;
        }

        @Override
        public DataIterator<OqsEngineEntity> iterator(IEntityClass entityClass, long startTime, long endTime,
                                                      long lastId, int size) throws SQLException {
            return null;
        }
    }

    static class MockLogic implements CalculationLogic {

        // 支持的计算字段类型.
        private CalculationType type;
        // 需要维护的场景.
        private CalculationScenarios[] needMaintenanceScenarios;
        // 计算字段 key为请求计算的IValue实例, value为计算结果.
        private Map<IEntityField, IValue> valueChanage;
        // 指定一个参与者的影响实例id列表.
        private Map<AbstractParticipant, Collection<AffectedInfo>> entityIds;
        // 需要增加的影响范围,当迭代树碰到和key相等的参与者时需要为其增加value影响.
        private Map<AbstractParticipant, AbstractParticipant> scope;

        public MockLogic(CalculationType type) {
            this.type = type;
        }

        public void setNeedMaintenanceScenarios(
            CalculationScenarios[] needMaintenanceScenarios) {
            this.needMaintenanceScenarios = needMaintenanceScenarios;
        }

        public void setValueChanage(
            Map<IEntityField, IValue> valueChanage) {
            this.valueChanage = valueChanage;
        }

        public void setEntityIds(
            Map<AbstractParticipant, Collection<AffectedInfo>> entityIds) {
            this.entityIds = entityIds;
        }

        public void setScope(
            Map<AbstractParticipant, AbstractParticipant> scope) {
            this.scope = scope;
        }

        @Override
        public Optional<IValue> calculate(CalculationContext context) throws CalculationException {
            return Optional.ofNullable(valueChanage.get(context.getFocusField()));
        }

        @Override
        public void scope(CalculationContext context, Infuence infuence) {
            infuence.scan((parentClassOp, participant, infuenceInner) -> {

                AbstractParticipant child = scope.get(participant);

                if (child != null) {
                    infuenceInner.impact(participant, child);
                }

                return InfuenceConsumer.Action.CONTINUE;
            });
        }

        @Override
        public Collection<AffectedInfo> getMaintainTarget(CalculationContext context, Participant participant,
                                                          Collection<IEntity> triggerEntities)
            throws CalculationException {
            Collection<AffectedInfo> affectedInfos = entityIds.get(participant);
            if (affectedInfos == null) {
                return Collections.emptyList();
            } else {
                return affectedInfos;
            }
        }

        @Override
        public CalculationScenarios[] needMaintenanceScenarios() {
            return needMaintenanceScenarios;
        }

        @Override
        public CalculationType supportType() {
            return type;
        }
    }

    private static class MockTaskCoordinator implements TaskCoordinator {

        private List<Task> tasks = new ArrayList<>();

        public List<Task> getTasks() {
            return tasks;
        }

        @Override
        public boolean registerRunner(TaskRunner runner) {
            return false;
        }

        @Override
        public Optional<TaskRunner> getRunner(Class clazz) {
            return Optional.empty();
        }

        @Override
        public boolean addTask(Task task) {
            tasks.add(task);
            return true;
        }
    }

    private static class MockTransaction implements Transaction {

        private List<Consumer<Transaction>> commitHooks = new ArrayList<>();

        @Override
        public long id() {
            return Long.MAX_VALUE;
        }

        @Override
        public Optional<String> message() {
            return Optional.empty();
        }

        @Override
        public void commit() throws SQLException {
            commitHooks.forEach(h -> {
                h.accept(this);
            });
        }

        @Override
        public void rollback() throws SQLException {

        }

        @Override
        public boolean isCommitted() {
            return false;
        }

        @Override
        public boolean isRollback() {
            return false;
        }

        @Override
        public boolean isCompleted() {
            return false;
        }

        @Override
        public void join(TransactionResource transactionResource) throws SQLException {

        }

        @Override
        public Optional<TransactionResource> queryTransactionResource(String key) {
            return Optional.empty();
        }

        @Override
        public Collection<TransactionResource> listTransactionResource(TransactionResourceType type) {
            return null;
        }

        @Override
        public long attachment() {
            return 0;
        }

        @Override
        public void attach(long id) {

        }

        @Override
        public boolean isReadyOnly() {
            return false;
        }

        @Override
        public void focusNotReadOnly() {

        }

        @Override
        public TransactionAccumulator getAccumulator() {
            return null;
        }

        @Override
        public TransactionHint getHint() {
            return null;
        }

        @Override
        public void exclusiveAction(TransactionExclusiveAction action) throws SQLException {

        }

        @Override
        public void registerCommitHook(Consumer<Transaction> hook) {
            this.commitHooks.add(hook);
        }

        @Override
        public void registerRollbackHook(Consumer<Transaction> hook) {

        }
    }

    static class MockConditionSelectStorage implements ConditionsSelectStorage {

        private Map<Long, List<IEntity>> pool = new HashMap();

        /**
         * 装入需要被查询的IEntity实例.
         *
         * @param entity 实例列表.
         */
        public void put(IEntity entity) {
            List<IEntity> entities = pool.get(entity.entityClassRef().getId());
            if (entities == null) {
                entities = new ArrayList<>();
                pool.put(entity.entityClassRef().getId(), entities);
            }
            entities.add(entity);
        }

        /**
         * 获取指定类型的所有实例.
         *
         * @param ref 实例指针.
         * @return 结果列表.
         */
        public Collection<IEntity> getEntities(EntityClassRef ref) {
            List<IEntity> entities = pool.get(ref.getId());
            if (entities == null) {
                return Collections.emptyList();
            } else {
                return new ArrayList<>(entities);
            }
        }

        /**
         * 清空所有实例.
         */
        public void clear() {
            pool.clear();
        }

        /**
         * 条件查询.
         */
        @Override
        public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, SelectConfig config)
            throws SQLException {
            List<IEntity> entities = pool.get(entityClass.id());
            if (entities == null) {
                return Collections.emptyList();
            }

            Collection<IEntity> matchEntities = conditions.match(
                entities.stream().filter(e -> !e.isDeleted()).collect(Collectors.toList()));
            if (matchEntities.isEmpty()) {
                return Collections.emptyList();
            }

            return matchEntities.stream().sorted(
                    (e0, e1) -> {
                        if (config.getSort().isAsc()) {
                            if (e0.id() < e1.id()) {
                                return -1;
                            } else if (e0.id() > e1.id()) {
                                return 1;
                            } else {
                                return 0;
                            }
                        } else {
                            if (e0.id() < e1.id()) {
                                return 1;
                            } else if (e0.id() > e1.id()) {
                                return -1;
                            } else {
                                return 0;
                            }
                        }
                    }).limit(config.getPage().getPageSize())
                .map(
                    e -> EntityRef.Builder.anEntityRef()
                        .withId(e.id()).withOp(OperationType.CREATE.getValue()).build())
                .collect(Collectors.toList());
        }
    }

    private long getFieldId(FieldIndex fieldIndex) {
        return Long.MAX_VALUE - fieldIndex.ordinal();
    }

    private long getClassId(ClassIndex classIndex) {
        return Long.MAX_VALUE - classIndex.ordinal();
    }
}
