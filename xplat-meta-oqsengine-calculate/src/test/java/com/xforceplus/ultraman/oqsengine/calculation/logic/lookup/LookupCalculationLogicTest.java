package com.xforceplus.ultraman.oqsengine.calculation.logic.lookup;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xforceplus.ultraman.oqsengine.calculation.context.DefaultCalculationLogicContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.Scenarios;
import com.xforceplus.ultraman.oqsengine.calculation.helper.LookupHelper;
import com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.task.LookupMaintainingTask;
import com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.task.LookupMaintainingTaskRunner;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.StaticCalculation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.kv.memory.MemoryKeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.task.TaskCoordinator;
import com.xforceplus.ultraman.oqsengine.task.TaskRunner;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * lookup 字段计算逻辑.
 *
 * @author dongbin
 * @version 0.1 2021/08/02 15:49
 * @since 1.8
 */
public class LookupCalculationLogicTest {

    private long targetClassId = Long.MAX_VALUE;
    private long strongLookupClassId = Long.MAX_VALUE - 1;
    private long weakLookupClassId = Long.MAX_VALUE - 2;
    private long fieldId = Long.MAX_VALUE;

    private IEntityClass targetEntityClass;
    private IEntityClass strongLookupEntityClass;
    private IEntityClass weakLookupEntityClass;

    // 目标对象.
    private IEntityField targetLongField = EntityField.Builder.anEntityField()
        .withId(fieldId--)
        .withFieldType(FieldType.LONG)
        .withName("target-long")
        .withConfig(FieldConfig.build().searchable(true)).build();
    private IEntityField targetStringField = EntityField.Builder.anEntityField()
        .withId(fieldId--)
        .withFieldType(FieldType.STRING)
        .withName("target-string")
        .withConfig(FieldConfig.build().searchable(true)).build();


    // 强关系对象.
    private IEntityField strongLookLongField = EntityField.Builder.anEntityField()
        .withId(fieldId--)
        .withFieldType(FieldType.LONG)
        .withName("strong-long")
        .withConfig(
            FieldConfig.Builder.anFieldConfig().withCalculation(
                StaticCalculation.Builder.anStaticCalculation().build()
            ).build()
        ).build();
    private IEntityField strongLookStringField = EntityField.Builder.anEntityField()
        .withId(fieldId--)
        .withFieldType(FieldType.STRING)
        .withName("strong-string")
        .withConfig(
            FieldConfig.Builder.anFieldConfig().withCalculation(
                StaticCalculation.Builder.anStaticCalculation().build()
            ).build()
        ).build();
    private IEntityField strongStringLookupField = EntityField.Builder.anEntityField()
        .withId(fieldId--)
        .withFieldType(FieldType.STRING)
        .withName("strong-string-lookup")
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withSearchable(true)
                .withCalculation(
                    Lookup.Builder.anLookup()
                        .withClassId(targetClassId)
                        .withFieldId(targetStringField.id()).build()
                )
                .build()
        ).build();

    // 弱关系对象.
    private IEntityField weakLongField = EntityField.Builder.anEntityField()
        .withId(fieldId--)
        .withFieldType(FieldType.LONG)
        .withName("weak-look-long")
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withCalculation(StaticCalculation.Builder.anStaticCalculation().build()).build()
        ).build();
    private IEntityField weakLongLookupField = EntityField.Builder.anEntityField()
        .withId(fieldId--)
        .withFieldType(FieldType.LONG)
        .withName("weak-long-lookup")
        .withConfig(
            FieldConfig.Builder.anFieldConfig().withCalculation(
                Lookup.Builder.anLookup()
                    .withFieldId(targetLongField.id())
                    .withClassId(targetClassId).build()

            ).build()
        ).build();


    private KeyValueStorage kv;

    /**
     * 初始化.
     */
    @BeforeEach
    public void before() throws Exception {
        kv = new MemoryKeyValueStorage();

        targetEntityClass = EntityClass.Builder.anEntityClass()
            .withId(targetClassId)
            .withCode("targetClass")
            .withField(targetLongField)
            .withField(targetStringField)
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(0)
                        .withLeftEntityClassId(targetClassId)
                        .withRightEntityClassId(strongLookupClassId)
                        .withRightEntityClassLoader(id -> Optional.of(strongLookupEntityClass))
                        .withIdentity(true)
                        .withBelongToOwner(true)
                        .withStrong(true)
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .build(),
                    Relationship.Builder.anRelationship()
                        .withId(1)
                        .withLeftEntityClassId(targetClassId)
                        .withRightEntityClassId(weakLookupClassId)
                        .withRightEntityClassLoader(id -> Optional.of(weakLookupEntityClass))
                        .withIdentity(true)
                        .withBelongToOwner(true)
                        .withStrong(false)
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .build()
                )
            )
            .build();

        strongLookupEntityClass = EntityClass.Builder.anEntityClass()
            .withId(strongLookupClassId)
            .withLevel(0)
            .withCode("strongLookupClass")
            .withField(strongLookLongField)
            .withField(strongLookStringField)
            .withField(strongStringLookupField)
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(0)
                        .withLeftEntityClassId(strongLookupClassId)
                        .withRightEntityClassId(targetClassId)
                        .withRightEntityClassLoader(id -> Optional.of(targetEntityClass))
                        .withIdentity(true)
                        .withBelongToOwner(false)
                        .withStrong(true)
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .build()
                )
            ).build();

        weakLookupEntityClass = EntityClass.Builder.anEntityClass()
            .withId(weakLookupClassId)
            .withLevel(0)
            .withCode("weakLookupClass")
            .withField(weakLongField)
            .withField(weakLongLookupField)
            .withRelations(
                Arrays.asList(
                    Relationship.Builder.anRelationship()
                        .withId(0)
                        .withLeftEntityClassId(weakLookupClassId)
                        .withRightEntityClassId(targetClassId)
                        .withRightEntityClassLoader(id -> Optional.of(targetEntityClass))
                        .withIdentity(true)
                        .withBelongToOwner(false)
                        .withStrong(false)
                        .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                        .build()
                )
            ).build();
    }

    @AfterEach
    public void after() throws Exception {
        kv = null;
    }

    /**
     * 非replace场景不需要维护.
     */
    @Test
    public void testMaintainOnlyReplace() throws Exception {

        DefaultCalculationLogicContext context = DefaultCalculationLogicContext.Builder.anCalculationLogicContext()
            .withScenarios(Scenarios.BUILD).build();
        context.focusField(strongStringLookupField);
        LookupCalculationLogic logic = new LookupCalculationLogic();

        // 非replace场景,跳过.
        Assertions.assertFalse(logic.maintain(context));
    }

    /**
     * 本身就是lookup字段,不需要维护.
     */
    @Test
    public void testMaintainFourceLookupFiled() throws Exception {
        DefaultCalculationLogicContext context = DefaultCalculationLogicContext.Builder.anCalculationLogicContext()
            .withScenarios(Scenarios.REPLACE).build();
        context.focusField(strongStringLookupField);
        LookupCalculationLogic logic = new LookupCalculationLogic();

        Assertions.assertFalse(logic.maintain(context));
    }

    /**
     * 非 lookup 字段,replace场景需要对字段进行维护.
     * 当前测试强关系的情况.
     */
    @Test
    public void testMaintainStrong() throws Exception {
        IEntity targetEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(targetEntityClass.ref())
            .withVersion(0)
            .withTime(System.currentTimeMillis())
            .withEntityValue(
                EntityValue.build().addValues(
                    Arrays.asList(
                        new StringValue(targetStringField, "targetValue"),
                        new LongValue(targetLongField, 100L)
                    )
                )
            ).build();

        TaskCoordinator mockCoordinator = mock(TaskCoordinator.class);
        TaskRunner mockRunner = mock(TaskRunner.class);

        String iterKey = LookupHelper.buildIteratorPrefixLinkKey(
            targetEntity,
            targetStringField,
            strongLookupEntityClass,
            strongStringLookupField).toString();

        doNothing().when(mockRunner).run(mockCoordinator, new LookupMaintainingTask(iterKey));


        when(mockCoordinator.getRunner(LookupMaintainingTaskRunner.class)).thenReturn(Optional.of(mockRunner));


        DefaultCalculationLogicContext context = DefaultCalculationLogicContext.Builder.anCalculationLogicContext()
            .withScenarios(Scenarios.REPLACE)
            .withEntityClass(targetEntityClass)
            .withTaskCoordinator(mockCoordinator)
            .withEntity(targetEntity)
            .build();
        context.focusField(targetStringField);

        LookupCalculationLogic logic = new LookupCalculationLogic();
        Assertions.assertTrue(logic.maintain(context));

        verify(mockRunner, times(1)).run(mockCoordinator, new LookupMaintainingTask(iterKey));
        verify(mockCoordinator, times(1)).getRunner(LookupMaintainingTaskRunner.class);
    }

    @Test
    public void testMaintainWeak() throws Exception {
        IEntity targetEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(targetEntityClass.ref())
            .withVersion(0)
            .withTime(System.currentTimeMillis())
            .withEntityValue(
                EntityValue.build().addValues(
                    Arrays.asList(
                        new StringValue(targetStringField, "targetValue"),
                        new LongValue(targetLongField, 100L)
                    )
                )
            ).build();

        TaskCoordinator mockCoordinator = mock(TaskCoordinator.class);

        String iterKey = LookupHelper.buildIteratorPrefixLinkKey(
            targetEntity,
            targetLongField,
            weakLookupEntityClass,
            weakLongLookupField).toString();

        when(mockCoordinator.addTask(new LookupMaintainingTask(iterKey))).thenReturn(true);


        DefaultCalculationLogicContext context = DefaultCalculationLogicContext.Builder.anCalculationLogicContext()
            .withScenarios(Scenarios.REPLACE)
            .withEntityClass(targetEntityClass)
            .withTaskCoordinator(mockCoordinator)
            .withEntity(targetEntity)
            .build();
        context.focusField(targetLongField);

        LookupCalculationLogic logic = new LookupCalculationLogic();
        Assertions.assertTrue(logic.maintain(context));

        LookupMaintainingTask task = new LookupMaintainingTask(iterKey);

        verify(mockCoordinator, times(1)).addTask(task);

    }

    @Test
    public void testCalculate() throws Exception {
        // 目标被lookup的实体.
        IEntity targetEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(targetEntityClass.ref())
            .withVersion(0)
            .withTime(System.currentTimeMillis())
            .withEntityValue(
                EntityValue.build().addValues(
                    Arrays.asList(
                        new StringValue(targetStringField, "targetValue"),
                        new LongValue(targetLongField, 100L)
                    )
                )
            ).build();

        // 发起lookup的实体.
        IEntity lookupEntity = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE - 1)
            .withEntityClassRef(strongLookupEntityClass.ref())
            .withTime(System.currentTimeMillis())
            .withEntityValue(
                EntityValue.build().addValues(
                    Arrays.asList(
                        new LongValue(strongStringLookupField, 1000),
                        new LongValue(strongLookLongField, 2000L)
                    )
                )
            ).build();

        MetaManager metaManager = mock(MetaManager.class);
        when(
            metaManager.load(
                ((Lookup) (strongStringLookupField.config().getCalculation())).getClassId()
            )
        ).thenReturn(Optional.of(targetEntityClass));

        MasterStorage masterStorage = mock(MasterStorage.class);
        when(
            masterStorage.selectOne(1000, targetEntityClass)
        ).thenReturn(Optional.of(targetEntity));


        DefaultCalculationLogicContext context = DefaultCalculationLogicContext.Builder.anCalculationLogicContext()
            .withScenarios(Scenarios.BUILD)
            .withMasterStorage(masterStorage)
            .withMetaManager(metaManager)
            .withKeyValueStorage(kv)
            .withEntity(lookupEntity).build();
        context.focusField(strongStringLookupField);
        LookupCalculationLogic logic = new LookupCalculationLogic();
        Optional<IValue> actualValueOp = logic.calculate(context);

        Assertions.assertTrue(actualValueOp.isPresent());

        IValue actualValue = actualValueOp.get();
        Assertions.assertEquals("targetValue", actualValue.getValue());
        Assertions.assertEquals(StringValue.class, actualValue.getClass());
        Assertions.assertEquals(strongStringLookupField.id(), actualValue.getField().id());
    }

    /**
     * lookup 不存在的实体.
     * 应该不产生任何字段.
     */
    @Test
    public void testNoTarget() throws Exception {
        // 发起lookup的实体.
        IEntity lookupEntity = Entity.Builder.anEntity()
            .withId(100)
            .withEntityClassRef(strongLookupEntityClass.ref())
            .withTime(System.currentTimeMillis())
            .withEntityValue(
                EntityValue.build().addValues(
                    Arrays.asList(
                        new LongValue(strongStringLookupField, 1000),
                        new LongValue(strongLookLongField, 2000L)
                    )
                )
            ).build();
        MetaManager metaManager = mock(MetaManager.class);
        when(
            metaManager.load(
                ((Lookup) (strongStringLookupField.config().getCalculation())).getClassId()
            )
        ).thenReturn(Optional.of(targetEntityClass));

        MasterStorage masterStorage = mock(MasterStorage.class);
        when(
            masterStorage.selectOne(1000, targetEntityClass)
        ).thenReturn(Optional.empty());

        DefaultCalculationLogicContext context = DefaultCalculationLogicContext.Builder.anCalculationLogicContext()
            .withScenarios(Scenarios.BUILD)
            .withMasterStorage(masterStorage)
            .withMetaManager(metaManager)
            .withKeyValueStorage(kv)
            .withEntity(lookupEntity).build();
        context.focusField(strongStringLookupField);
        LookupCalculationLogic logic = new LookupCalculationLogic();

        try {
            logic.calculate(context);
            Assertions.fail("An exception should be thrown.");
        } catch (Exception ex) {
            // do nothing.
        }
    }

    /**
     * 有目标实体,但是目标实体没有具体字段属性.
     */
    @Test
    public void testHaveTargetNoFieldValue() throws Exception {
        // 目标被lookup的实体.
        IEntity targetEntity = Entity.Builder.anEntity()
            .withId(1000)
            .withEntityClassRef(targetEntityClass.ref())
            .withVersion(0)
            .withTime(System.currentTimeMillis())
            .withEntityValue(
                EntityValue.build().addValues(
                    Arrays.asList(
                        new LongValue(targetLongField, 100L)
                    )
                )
            ).build();

        // 发起lookup的实体.
        IEntity lookupEntity = Entity.Builder.anEntity()
            .withId(100)
            .withEntityClassRef(strongLookupEntityClass.ref())
            .withTime(System.currentTimeMillis())
            .withEntityValue(
                EntityValue.build().addValues(
                    Arrays.asList(
                        new LongValue(strongStringLookupField, 1000),
                        new LongValue(strongLookLongField, 2000L)
                    )
                )
            ).build();

        MetaManager metaManager = mock(MetaManager.class);
        when(
            metaManager.load(
                ((Lookup) (strongStringLookupField.config().getCalculation())).getClassId()
            )
        ).thenReturn(Optional.of(targetEntityClass));

        MasterStorage masterStorage = mock(MasterStorage.class);
        when(
            masterStorage.selectOne(1000, targetEntityClass)
        ).thenReturn(Optional.of(targetEntity));


        DefaultCalculationLogicContext context = DefaultCalculationLogicContext.Builder.anCalculationLogicContext()
            .withScenarios(Scenarios.BUILD)
            .withMasterStorage(masterStorage)
            .withMetaManager(metaManager)
            .withKeyValueStorage(kv)
            .withEntity(lookupEntity).build();
        context.focusField(strongStringLookupField);
        LookupCalculationLogic logic = new LookupCalculationLogic();
        Optional<IValue> actualValueOp = logic.calculate(context);
        Assertions.assertFalse(actualValueOp.isPresent());
    }
}