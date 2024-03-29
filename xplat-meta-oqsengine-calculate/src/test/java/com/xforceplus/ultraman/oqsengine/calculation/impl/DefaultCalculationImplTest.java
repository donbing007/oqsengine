package com.xforceplus.ultraman.oqsengine.calculation.impl;

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
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InfuenceGraph;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InfuenceGraphConsumer;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Participant;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.lock.LocalResourceLocker;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
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
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OqsEngineEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 计算字段计算器测试. 测试构造如下的一个情况.
 * <pre>
 * A
 * |---B_SUM
 *       |------D_SUM
 * |---B_LOOKUP
 *        |------B_FORMULA
 *            |-------------D_SUM_CONDITION (聚合B_DECIMAL普通字段,条件: B_FORMULA != 7)
 * |---C_LOOKUP
 * </pre>
 *
 * @author dongbin
 * @version 0.1 2021/10/14 10:26:24
 * @since 1.8
 */
public class DefaultCalculationImplTest {

    private static enum FieldIndex {
        A_LONG,
        B_SUM,
        B_LOOKUP,
        B_DECIMAL,
        B_FORMULA,
        D_SUM,
        D_SUM_CONDITION,
        C_LOOKUP,
        F_COLLECT,
        S_STRING
    }

    private static enum ClassIndex {
        A_CLASS,
        B_CLASS,
        C_CLASS,
        D_CLASS,
        F_CLASS,
        S_CLASS
    }

    private static enum RelationIndex {
        B_A,
        A_B,
        B_D,
        D_B,
        A_C,
        C_A,
    }

    private static IEntityClass A_CLASS;
    private static IEntityClass B_CLASS;
    private static IEntityClass C_CLASS;
    private static IEntityClass D_CLASS;
    private static IEntityClass F_CLASS;
    private static IEntityClass S_CLASS;

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

    private static IEntityField A_LONG = EntityField.Builder.anEntityField()
        .withId(getFieldId(FieldIndex.A_LONG))
        .withFieldType(FieldType.LONG)
        .withName("along").build();

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

    private static IEntityField B_LOOKUP = EntityField.Builder.anEntityField()
        .withId(getFieldId(FieldIndex.B_LOOKUP))
        .withFieldType(FieldType.LONG)
        .withName("blookupa")
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withCalculation(
                    Lookup.Builder.anLookup()
                        .withClassId(getClassId(ClassIndex.A_CLASS))
                        .withFieldId(getFieldId(FieldIndex.A_LONG))
                        .build()
                ).build()
        ).build();

    private static IEntityField B_DECIMAL = EntityField.Builder.anEntityField()
        .withId(getFieldId(FieldIndex.B_DECIMAL))
        .withFieldType(FieldType.DECIMAL)
        .withName("b-decimal").build();

    private static IEntityField B_FORMULA = EntityField.Builder.anEntityField()
        .withId(getFieldId(FieldIndex.B_FORMULA))
        .withFieldType(FieldType.LONG)
        .withName("bformula")
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withCalculation(
                    Formula.Builder.anFormula()
                        .withLevel(0)
                        .withFailedPolicy(Formula.FailedPolicy.THROW_EXCEPTION)
                        .withExpression("return ${blookupa} + ${bsuma};")
                        .withArgs(Arrays.asList("blookupa", "bsuma"))
                        .build()
                ).build()
        ).build();

    private static IEntityField C_LOOKUP = EntityField.Builder.anEntityField()
        .withId(getFieldId(FieldIndex.C_LOOKUP))
        .withFieldType(FieldType.LONG)
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withCalculation(
                    Lookup.Builder.anLookup()
                        .withClassId(getClassId(ClassIndex.A_CLASS))
                        .withFieldId(getFieldId(FieldIndex.A_LONG))
                        .build()
                ).build()
        ).withName("clookupa").build();

    private static IEntityField D_SUM = EntityField.Builder.anEntityField()
        .withId(getFieldId(FieldIndex.D_SUM))
        .withFieldType(FieldType.LONG)
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withCalculation(
                    Aggregation.Builder.anAggregation()
                        .withClassId(getClassId(ClassIndex.B_CLASS))
                        .withFieldId(getFieldId(FieldIndex.B_SUM))
                        .withRelationId(getRelationIndex(RelationIndex.D_B))
                        .withAggregationType(AggregationType.SUM)
                        .build()
                ).build()
        )
        .withName("dsumb").build();

    private static IEntityField D_SUM_CONDITION = EntityField.Builder.anEntityField()
        .withId(getFieldId(FieldIndex.D_SUM_CONDITION))
        .withFieldType(FieldType.LONG)
        .withName("dsumconditionb")
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withCalculation(
                    Aggregation.Builder.anAggregation()
                        .withClassId(getClassId(ClassIndex.B_CLASS))
                        .withFieldId(getFieldId(FieldIndex.B_DECIMAL))
                        .withAggregationType(AggregationType.SUM)
                        .withRelationId(getRelationIndex(RelationIndex.D_B))
                        .withConditions(
                            Conditions.buildEmtpyConditions()
                                .addAnd(
                                    new Condition(
                                        B_FORMULA,
                                        ConditionOperator.NOT_EQUALS,
                                        new LongValue(B_FORMULA, 7L)
                                    )
                                )
                        ).build()
                ).build()
        ).build();

    private static IEntityField F_COLLECT = EntityField.Builder.anEntityField()
        .withId(getFieldId(FieldIndex.F_COLLECT))
        .withFieldType(FieldType.STRINGS)
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withCalculation(
                    Aggregation.Builder.anAggregation()
                        .withAggregationType(AggregationType.COLLECT)
                        .build()
                ).build()
        )
        .withName("f-collect-s").build();

    private static IEntityField S_STRING = EntityField.Builder.anEntityField()
        .withId(getFieldId(FieldIndex.S_STRING))
        .withFieldType(FieldType.STRING)
        .withName("s-string").build();

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
                        .withRightEntityClassLoader((aLong, s) -> Optional.of(B_CLASS))
                        .withRightFamilyEntityClassLoader(aLong -> Arrays.asList(B_CLASS))
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
                        .withRightEntityClassLoader((aLong, s) -> Optional.of(B_CLASS))
                        .withRightFamilyEntityClassLoader(aLong -> Arrays.asList(B_CLASS))
                        .build(),
                    Relationship.Builder.anRelationship()
                        .withId(getRelationIndex(RelationIndex.A_C))
                        .withCode("a_c")
                        .withBelongToOwner(true)
                        .withLeftEntityClassId(getClassId(ClassIndex.A_CLASS))
                        .withLeftEntityClassCode("a-class")
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withStrong(true)
                        .withRightEntityClassId(getClassId(ClassIndex.C_CLASS))
                        .withRightEntityClassLoader((aLong, s) -> Optional.of(C_CLASS))
                        .withRightFamilyEntityClassLoader(aLong -> Arrays.asList(C_CLASS))
                        .build(),
                    Relationship.Builder.anRelationship()
                        .withId(getRelationIndex(RelationIndex.C_A))
                        .withCode("c_a")
                        .withBelongToOwner(true)
                        .withLeftEntityClassId(getClassId(ClassIndex.A_CLASS))
                        .withLeftEntityClassCode("a-class")
                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                        .withStrong(true)
                        .withRightEntityClassId(getClassId(ClassIndex.C_CLASS))
                        .withRightEntityClassLoader((aLong, s) -> Optional.of(C_CLASS))
                        .withRightFamilyEntityClassLoader(aLong -> Arrays.asList(C_CLASS))
                        .build()
                )
            )
            .withField(A_LONG).build();

        B_CLASS = EntityClass.Builder.anEntityClass()
            .withId(getClassId(ClassIndex.B_CLASS))
            .withCode("b-class")
            .withField(B_SUM)
            .withField(B_FORMULA)
            .withField(B_SUM)
            .withField(B_LOOKUP)
            .withField(B_DECIMAL)
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
                        .withRightEntityClassLoader((aLong, s) -> Optional.of(A_CLASS))
                        .withRightFamilyEntityClassLoader(aLong -> Arrays.asList(A_CLASS))
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
                        .withRightEntityClassLoader((aLong, s) -> Optional.of(A_CLASS))
                        .withRightFamilyEntityClassLoader(aLong -> Arrays.asList(A_CLASS))
                        .build(),
                    Relationship.Builder.anRelationship()
                        .withId(getRelationIndex(RelationIndex.B_D))
                        .withCode("b_d")
                        .withBelongToOwner(true)
                        .withLeftEntityClassId(getClassId(ClassIndex.B_CLASS))
                        .withLeftEntityClassCode("b-class")
                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                        .withStrong(true)
                        .withRightEntityClassId(getClassId(ClassIndex.D_CLASS))
                        .withRightEntityClassLoader((aLong, s) -> Optional.of(D_CLASS))
                        .withRightFamilyEntityClassLoader(aLong -> Arrays.asList(D_CLASS))
                        .build(),
                    Relationship.Builder.anRelationship()
                        .withId(getRelationIndex(RelationIndex.D_B))
                        .withCode("d_b")
                        .withBelongToOwner(true)
                        .withLeftEntityClassId(getClassId(ClassIndex.B_CLASS))
                        .withLeftEntityClassCode("b-class")
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withStrong(true)
                        .withRightEntityClassId(getClassId(ClassIndex.D_CLASS))
                        .withRightEntityClassLoader((aLong, s) -> Optional.of(D_CLASS))
                        .withRightFamilyEntityClassLoader(aLong -> Arrays.asList(D_CLASS))
                        .build()
                )
            )
            .build();

        C_CLASS = EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE - 2)
            .withCode("c-class")
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(getRelationIndex(RelationIndex.C_A))
                        .withCode("c_a")
                        .withBelongToOwner(true)
                        .withLeftEntityClassId(getClassId(ClassIndex.C_CLASS))
                        .withLeftEntityClassCode("c-class")
                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                        .withStrong(true)
                        .withRightEntityClassId(getClassId(ClassIndex.A_CLASS))
                        .withRightEntityClassLoader((aLong, s) -> Optional.of(A_CLASS))
                        .withRightFamilyEntityClassLoader(aLong -> Arrays.asList(A_CLASS))
                        .build(),
                    Relationship.Builder.anRelationship()
                        .withId(getRelationIndex(RelationIndex.A_C))
                        .withCode("a_c")
                        .withBelongToOwner(true)
                        .withLeftEntityClassId(getClassId(ClassIndex.C_CLASS))
                        .withLeftEntityClassCode("c-class")
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withStrong(true)
                        .withRightEntityClassId(getClassId(ClassIndex.A_CLASS))
                        .withRightEntityClassLoader((aLong, s) -> Optional.of(A_CLASS))
                        .withRightFamilyEntityClassLoader(aLong -> Arrays.asList(A_CLASS))
                        .build()
                )
            )
            .withField(C_LOOKUP).build();

        D_CLASS = EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE - 3)
            .withCode("d-class")
            .withField(D_SUM)
            .withField(D_SUM_CONDITION)
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(getRelationIndex(RelationIndex.D_B))
                        .withCode("d_b")
                        .withBelongToOwner(true)
                        .withLeftEntityClassId(getClassId(ClassIndex.D_CLASS))
                        .withLeftEntityClassCode("d-class")
                        .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                        .withStrong(true)
                        .withRightEntityClassId(getClassId(ClassIndex.B_CLASS))
                        .withRightEntityClassLoader((aLong, s) -> Optional.of(B_CLASS))
                        .withRightFamilyEntityClassLoader(aLong -> Arrays.asList(B_CLASS))
                        .build(),
                    Relationship.Builder.anRelationship()
                        .withId(getRelationIndex(RelationIndex.B_D))
                        .withCode("b_d")
                        .withBelongToOwner(true)
                        .withLeftEntityClassId(getClassId(ClassIndex.D_CLASS))
                        .withLeftEntityClassCode("d-class")
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .withStrong(true)
                        .withRightEntityClassId(getClassId(ClassIndex.B_CLASS))
                        .withRightEntityClassLoader((aLong, s) -> Optional.of(B_CLASS))
                        .withRightFamilyEntityClassLoader(aLong -> Arrays.asList(B_CLASS))
                        .build()
                )
            )
            .build();

        F_CLASS = EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE - 4)
            .withCode("f-class")
            .withField(F_COLLECT).build();


        S_CLASS = EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE - 5)
            .withCode("s-class")
            .withField(S_STRING).build();
    }


    private IEntity entityA = Entity.Builder.anEntity()
        .withId(Long.MAX_VALUE)
        .withEntityClassRef(A_CLASS.ref())
        .withValue(new LongValue(A_LONG, 100L))
        .build();

    private IEntity entityB = Entity.Builder.anEntity()
        .withId(Long.MAX_VALUE - 1)
        .withEntityClassRef(B_CLASS.ref())
        .withValue(new LongValue(B_LOOKUP, 100L))
        .withValue(new LongValue(B_SUM, 100L))
        .build();

    private IEntity entityD = Entity.Builder.anEntity()
        .withId(Long.MAX_VALUE - 2)
        .withEntityClassRef(D_CLASS.ref())
        .withValue(new LongValue(D_SUM, 100L))
        .build();

    private IEntity entityC = Entity.Builder.anEntity()
        .withId(Long.MAX_VALUE - 3)
        .withEntityClassRef(C_CLASS.ref())
        .withValue(new LongValue(C_LOOKUP, 100L))
        .build();

    private IEntity entityF = Entity.Builder.anEntity()
        .withId(Long.MAX_VALUE - 4)
        .withEntityClassRef(F_CLASS.ref())
        .withValue(new StringsValue(F_COLLECT, new String[0], ""))
        .build();

    private MockLogic aggregationLogic;
    private MockLogic lookupLogic;

    private MockMetaManager metaManager;
    private MockMasterStorage masterStorage;
    private DefaultCalculationImpl calculation;

    /**
     * 初始化.
     */
    @BeforeEach
    public void before() throws Exception {
        calculation = new DefaultCalculationImpl();

        aggregationLogic = new MockLogic(CalculationType.AGGREGATION);
        Map<IEntityField, IValue> valueChange = new HashMap<>();
        valueChange.put(B_SUM, new LongValue(B_SUM, 200L));
        valueChange.put(D_SUM, new LongValue(D_SUM, 200L));
        valueChange.put(F_COLLECT, new StringValue(F_COLLECT, F_COLLECT.name()));

        aggregationLogic.setValueChanage(valueChange);
        aggregationLogic.setNeedMaintenanceScenarios(new CalculationScenarios[] {
            CalculationScenarios.BUILD,
            CalculationScenarios.REPLACE,
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

        lookupLogic = new MockLogic(CalculationType.LOOKUP);
        valueChange = new HashMap<>();
        valueChange.put(C_LOOKUP, new LongValue(C_LOOKUP, 200L));
        lookupLogic.setValueChanage(valueChange);
        lookupLogic.setNeedMaintenanceScenarios(new CalculationScenarios[] {
            CalculationScenarios.REPLACE,
            CalculationScenarios.DELETE
        });
        scope = new HashMap<>();
        scope.put(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(A_LONG).build(),
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(C_CLASS)
                .withField(C_LOOKUP).build()
        );
        lookupLogic.setScope(scope);

        entityIds = new HashMap<>();
        entityIds.put(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(C_CLASS)
                .withField(C_LOOKUP).build(),
            Arrays.asList(
                new AffectedInfo(entityC, entityC.id())
            )
        );
        lookupLogic.setEntityIds(entityIds);

        metaManager = new MockMetaManager();
        metaManager.addEntityClass(A_CLASS);
        metaManager.addEntityClass(B_CLASS);
        metaManager.addEntityClass(C_CLASS);
        metaManager.addEntityClass(D_CLASS);
        metaManager.addEntityClass(F_CLASS);

        masterStorage = new MockMasterStorage();
        masterStorage.addIEntity(entityA);
        masterStorage.addIEntity(entityB);
        masterStorage.addIEntity(entityC);
        masterStorage.addIEntity(entityD);
        masterStorage.addIEntity(entityF);
    }

    /**
     * 测试影响树的构造.
     */
    @Test
    public void testScope() throws Exception {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.focusEntity(entityA, A_CLASS);
        context.addValueChange(
            ValueChange.build(entityA.id(), new LongValue(A_LONG, 50L), new LongValue(A_LONG, 100L)));
        context.focusSourceEntity(entityA);

        DefaultCalculationImpl calculation = new DefaultCalculationImpl();
        Method scopeMethod = DefaultCalculationImpl.class.getDeclaredMethod(
            "scope", new Class[] {CalculationContext.class});
        scopeMethod.setAccessible(true);
        InfuenceGraph graph = (InfuenceGraph) scopeMethod.invoke(calculation, new Object[] {context});
        List<String> fieldNames = new ArrayList<>();
        graph.scan((parent, participant, inner) -> {
            fieldNames.add(participant.getField().name());
            return InfuenceGraphConsumer.Action.CONTINUE;
        });

        InfuenceGraph expectedGraph = new InfuenceGraph(
            CalculationParticipant.Builder.anParticipant()
                .withEntityClass(A_CLASS)
                .withField(EntityField.ILLUSORY_FIELD)
                .build()
        );

        Participant alongPar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(A_CLASS)
            .withField(A_LONG).build();
        expectedGraph.impact(alongPar);

        Participant idPar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(A_CLASS)
            .withField(EntityField.ID_ENTITY_FIELD)
            .build();
        expectedGraph.impact(idPar);

        Participant bsumaPar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(B_CLASS)
            .withField(B_SUM)
            .build();
        expectedGraph.impact(alongPar, bsumaPar);

        Participant blookupaPar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(B_CLASS)
            .withField(B_LOOKUP)
            .build();
        expectedGraph.impact(alongPar, blookupaPar);

        Participant clookupaPar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(C_CLASS)
            .withField(C_LOOKUP)
            .build();
        expectedGraph.impact(alongPar, clookupaPar);

        Participant dsumbPar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(D_CLASS)
            .withField(D_SUM)
            .build();
        expectedGraph.impact(bsumaPar, dsumbPar);

        Participant bformulaPar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(B_CLASS)
            .withField(B_FORMULA)
            .build();
        expectedGraph.impact(blookupaPar, bformulaPar);
        expectedGraph.impact(bsumaPar, bformulaPar);

        Participant dsumconditionbPar = CalculationParticipant.Builder.anParticipant()
            .withEntityClass(D_CLASS)
            .withField(D_SUM_CONDITION)
            .build();
        expectedGraph.impact(bformulaPar, dsumconditionbPar);

        Assertions.assertEquals(expectedGraph, graph);
    }

    /**
     * 测试创建的时候那些没有改变,但是需要被改变的计算字段.
     */
    @Test
    public void testBuildNotChangeFieldButNeed() throws Exception {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.BUILD).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusSourceEntity(entityB);
        context.focusEntity(entityB, B_CLASS);

        DefaultCalculationImpl calculation = new DefaultCalculationImpl();
        IEntity newEntity = calculation.calculate(context);
        Assertions.assertEquals(200L, newEntity.entityValue().getValue(B_SUM.id()).get().valueToLong());

        //  test collect
        context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.BUILD).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(entityF, F_CLASS);

        calculation = new DefaultCalculationImpl();
        newEntity = calculation.calculate(context);
        Assertions.assertEquals(F_COLLECT.name(),
            newEntity.entityValue().getValue(F_COLLECT.id()).get().valueToString());
    }

    @Test
    public void testBuildCalculation() throws Exception {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.BUILD).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(lookupLogic);
        context.focusEntity(entityC, C_CLASS);
        context.addValueChange(
            ValueChange.build(entityC.id(), new EmptyTypedValue(C_LOOKUP), new LongValue(C_LOOKUP, 200L)));

        DefaultCalculationImpl calculation = new DefaultCalculationImpl();
        IEntity newEntity = calculation.calculate(context);
        Assertions.assertEquals(200L, newEntity.entityValue().getValue(C_LOOKUP.id()).get().valueToLong());
    }

    @Test
    public void testReplaceCalculation() throws Exception {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(lookupLogic);
        context.focusEntity(entityC, C_CLASS);
        context.addValueChange(
            ValueChange.build(entityC.id(), new LongValue(C_LOOKUP, 100L), new LongValue(C_LOOKUP, 200L)));

        DefaultCalculationImpl calculation = new DefaultCalculationImpl();
        IEntity newEntity = calculation.calculate(context);
        Assertions.assertEquals(200L, newEntity.entityValue().getValue(C_LOOKUP.id()).get().valueToLong());
    }

    /**
     * 测试更新entityA造成的entityB,entityC,entityD的变动.
     */
    @Test
    public void testReplaceMaintenance() throws Exception {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withMasterStorage(masterStorage)
            .withResourceLocker(new LocalResourceLocker())
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(lookupLogic);
        context.getCalculationLogicFactory().get().register(aggregationLogic);

        context.focusSourceEntity(entityA);
        context.focusEntity(entityA, A_CLASS);
        context.addValueChange(
            ValueChange.build(entityA.id(), new EmptyTypedValue(A_LONG), new LongValue(A_LONG, 200L))
        );

        calculation.maintain(context);

        /*
        主动设置entityA为干净,因为此对象为实际写事务的目标对象.
        在计算影响对象时,此对象应该已经处于干净状态.
         */
        entityA.neat();

        try {
            Assertions.assertTrue(context.persist());
        } finally {
            context.destroy();
        }

        long[] replaceIds = masterStorage.getReplaceEntities().stream().mapToLong(e -> e.id()).sorted().toArray();

        Assertions.assertTrue(Arrays.binarySearch(replaceIds, entityA.id()) < 0,
            String.format("The target instance (%s) was not expected to be found, but it was.", "entityA"));

        Assertions.assertTrue(Arrays.binarySearch(replaceIds, entityB.id()) >= 0,
            String.format("The target instance (%s) was expected to be found, but was not.", "entityB"));
        Assertions.assertTrue(Arrays.binarySearch(replaceIds, entityD.id()) >= 0,
            String.format("The target instance (%s) was expected to be found, but was not.", "entityD"));
        Assertions.assertTrue(Arrays.binarySearch(replaceIds, entityC.id()) >= 0,
            String.format("The target instance (%s) was expected to be found, but was not.", "entityC"));
    }

    /**
     * 测试持久化部份错误情况.
     * 执行批量持久化,发生错误会针对错误数据进行重试.
     * 这里测试是否进行了重试,同时重试的次数是否有上限.
     */
    @Test
    public void testPersistenceErrReplayMax() throws Exception {
        // entityB实例持久化会一直错误.因为这里指定了判断其是否等于entityB,实际更新的是entityA.
        masterStorage.setReplaceTest(e -> !(e.id() == entityB.id()));

        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
            .withMetaManager(metaManager)
            .withMasterStorage(masterStorage)
            .withResourceLocker(new LocalResourceLocker())
            .withScenarios(CalculationScenarios.REPLACE).withCalculationLogicFactory(new CalculationLogicFactory())
            .build();
        context.getCalculationLogicFactory().get().register(lookupLogic);
        context.getCalculationLogicFactory().get().register(aggregationLogic);

        context.focusEntity(entityA, A_CLASS);
        context.focusSourceEntity(entityA);
        context.addValueChange(
            ValueChange.build(entityA.id(), new EmptyTypedValue(A_LONG), new LongValue(A_LONG, 200L))
        );

        calculation.maintain(context);
        /*
        主动设置entityA为干净,因为此对象为实际写事务的目标对象.
        在计算影响对象时,此对象应该已经处于干净状态.
         */
        entityA.neat();

        try {

            Assertions.assertFalse(context.persist());

        } finally {
            context.destroy();
        }

        // 由于更新被影响状态的对象会加悲观锁,所以这里最多只会进行一次.
        long size = masterStorage.getReplaceEntities().stream()
            .filter(e -> e.id() == entityB.id())
            .count();

        Assertions.assertEquals(1, size);
    }

    static class MockMasterStorage implements MasterStorage {

        private Map<Long, com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity> entities = new HashMap<>();

        private List<IEntity> replaceEntities = new ArrayList<>();

        private Predicate<IEntity> replaceTest;

        public void setReplaceTest(Predicate<IEntity> replaceTest) {
            this.replaceTest = replaceTest;
        }

        @Override
        public void replace(EntityPackage entityPackage) throws SQLException {
            if (replaceTest == null) {
                entityPackage.stream().forEach(e -> {
                    replaceEntities.add(e.getKey());
                    e.getKey().neat();
                });
            } else {
                entityPackage.stream().forEach(e -> {
                    replaceEntities.add(e.getKey());
                    if (replaceTest.test(e.getKey())) {
                        e.getKey().neat();
                    }
                });
            }
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
                                                      long lastId, boolean useSelfClass) throws SQLException {
            return null;
        }

        @Override
        public DataIterator<OqsEngineEntity> iterator(IEntityClass entityClass, long startTime, long endTime,
                                                      long lastId, int size, boolean useSelfClass) throws SQLException {
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
        public void scope(CalculationContext context, InfuenceGraph infuence) {
            infuence.scan((parentParticipants, participant, infuenceInner) -> {

                AbstractParticipant child = scope.get(participant);

                if (child != null) {
                    infuenceInner.impact(participant, child);
                }

                return InfuenceGraphConsumer.Action.CONTINUE;
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
}