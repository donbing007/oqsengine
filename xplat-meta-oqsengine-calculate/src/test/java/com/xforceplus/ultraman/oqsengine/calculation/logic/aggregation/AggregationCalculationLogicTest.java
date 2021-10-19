package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.context.DefaultCalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.impl.DefaultCalculationImpl;
import com.xforceplus.ultraman.oqsengine.calculation.impl.DefaultCalculationImplTest;
import com.xforceplus.ultraman.oqsengine.calculation.logic.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Participant;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MockMetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.*;
import com.xforceplus.ultraman.oqsengine.storage.ConditionsSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.condition.QueryErrorCondition;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.ErrorStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 测试类.
 *             A
 *          /     \
 *        B(sum)  C(COUNT)
 *        /
 *       D(SUM)
 *
 *
 */
public class AggregationCalculationLogicTest {

    final Logger logger = LoggerFactory.getLogger(AggregationCalculationLogicTest.class);

    public static final int ONE = 1;

    public static final int ZERO = 0;

    private List<IEntityClass> entityClasses = new ArrayList<>();

    private CalculationContext context;

    private AggregationCalculationLogic aggregationCalculationLogic;

    private static IEntityField A_LONG = EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE)
            .withFieldType(FieldType.LONG)
            .withName("a-long").build();

    private static IEntityField B_SUM = EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 1)
            .withFieldType(FieldType.LONG)
            .withName("b-sum-a")
            .withConfig(
                    FieldConfig.Builder.anFieldConfig()
                            .withCalculation(Aggregation.Builder.anAggregation().build()).build()
            )
            .build();

    private static IEntityField C_COUNT = EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 2)
            .withFieldType(FieldType.LONG)
            .withConfig(
                    FieldConfig.Builder.anFieldConfig()
                            .withCalculation(Aggregation.Builder.anAggregation().build()).build()
            )
            .withName("c-lookup-a").build();

    private static IEntityField D_SUM = EntityField.Builder.anEntityField()
            .withId(Long.MAX_VALUE - 3)
            .withFieldType(FieldType.LONG)
            .withConfig(
                    FieldConfig.Builder.anFieldConfig()
                            .withCalculation(Aggregation.Builder.anAggregation().build()).build()
            )
            .withName("d-sum-a").build();

    private static IEntityClass A_CLASS = EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE)
            .withCode("a-class")
            .withField(A_LONG).build();

    private static IEntityClass B_CLASS = EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE - 1)
            .withCode("b-class")
            .withField(B_SUM).build();

    private static IEntityClass C_CLASS = EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE - 2)
            .withCode("c-class")
            .withField(C_COUNT).build();

    private static IEntityClass D_CLASS = EntityClass.Builder.anEntityClass()
            .withId(Long.MAX_VALUE - 3)
            .withCode("d-class")
            .withField(D_SUM).build();

    private IEntity entityA = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE)
            .withEntityClassRef(A_CLASS.ref())
            .withEntityValue(
                    EntityValue.build().addValue(
                            new LongValue(A_LONG, 100L)
                    )
            ).build();

    private IEntity entityB = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE - 1)
            .withEntityClassRef(B_CLASS.ref())
            .withEntityValue(
                    EntityValue.build().addValue(
                            new LongValue(B_SUM, 100L)
                    )
            ).build();

    private IEntity entityD = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE - 2)
            .withEntityClassRef(D_CLASS.ref())
            .withEntityValue(
                    EntityValue.build().addValue(
                            new LongValue(D_SUM, 100L)
                    )
            ).build();

    private IEntity entityC = Entity.Builder.anEntity()
            .withId(Long.MAX_VALUE - 3)
            .withEntityClassRef(C_CLASS.ref())
            .withEntityValue(
                    EntityValue.build().addValue(
                            new LongValue(C_COUNT, 100L)
                    )
            ).build();

    private AggregationCalculationLogicTest.MockLogic aggregationLogic;
    private AggregationCalculationLogicTest.MockLogic lookupLogic;

    private MockMetaManager metaManager;
    private AggregationCalculationLogicTest.MockMasterStorage masterStorage;
    private DefaultCalculationImpl calculation;

    @BeforeEach
    public void before() {
        calculation = new DefaultCalculationImpl();

        aggregationLogic = new AggregationCalculationLogicTest.MockLogic(CalculationType.AGGREGATION);

        Map<IEntityField, IValue> valueChange = new HashMap<>();
        valueChange.put(B_SUM, new LongValue(B_SUM, 200L));
        valueChange.put(D_SUM, new LongValue(D_SUM, 200L));
        valueChange.put(C_COUNT, new LongValue(C_COUNT, 200L));
        aggregationLogic.setValueChanage(valueChange);
        aggregationLogic.setNeedMaintenanceScenarios(new CalculationScenarios[] {
                CalculationScenarios.BUILD,
                CalculationScenarios.REPLACE,
                CalculationScenarios.DELETE
        });
        Map<Participant, Participant> scope = new HashMap<>();
        scope.put(
                Participant.Builder.anParticipant()
                        .withEntityClass(A_CLASS)
                        .withField(A_LONG).build(),
                Participant.Builder.anParticipant()
                        .withEntityClass(B_CLASS)
                        .withField(B_SUM).build()
        );
        scope.put(
                Participant.Builder.anParticipant()
                        .withEntityClass(A_CLASS)
                        .withField(A_LONG).build(),
                Participant.Builder.anParticipant()
                        .withEntityClass(C_CLASS)
                        .withField(C_COUNT).build()
        );
        scope.put(
                Participant.Builder.anParticipant()
                        .withEntityClass(B_CLASS)
                        .withField(B_SUM).build(),
                Participant.Builder.anParticipant()
                        .withEntityClass(D_CLASS)
                        .withField(D_SUM).build()
        );
        aggregationLogic.setScope(scope);

        Map<Participant, long[]> entityIds = new HashMap<>();
        entityIds.put(
                Participant.Builder.anParticipant()
                        .withEntityClass(B_CLASS)
                        .withField(B_SUM).build(),
                new long[] {entityB.id()}
        );

        entityIds.put(
                Participant.Builder.anParticipant()
                        .withEntityClass(C_CLASS)
                        .withField(C_COUNT).build(),
                new long[] {entityC.id()}
        );

        entityIds.put(
                Participant.Builder.anParticipant()
                        .withEntityClass(D_CLASS)
                        .withField(D_SUM).build(),
                new long[] {entityD.id()}
        );
        aggregationLogic.setEntityIds(entityIds);

        metaManager = new MockMetaManager();
        metaManager.addEntityClass(A_CLASS);
        metaManager.addEntityClass(B_CLASS);
        metaManager.addEntityClass(C_CLASS);
        metaManager.addEntityClass(D_CLASS);

        masterStorage = new AggregationCalculationLogicTest.MockMasterStorage();
        masterStorage.addIEntity(entityA);
        masterStorage.addIEntity(entityB);
        masterStorage.addIEntity(entityC);
        masterStorage.addIEntity(entityD);

        aggregationCalculationLogic = new AggregationCalculationLogic();
    }

    @Test
    public void testBuildCalculation() {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
                .withMetaManager(metaManager)
                .withScenarios(CalculationScenarios.BUILD).build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(entityA, A_CLASS);
        context.focusField(A_LONG);
        context.addValueChange(
                ValueChange.build(entityC.id(), new EmptyTypedValue(A_LONG), new LongValue(A_LONG, 200L)));

        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);

        Assert.assertNotNull(targetValue);

    }

    @Test
    public void testReplaceCalculation() {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
                .withMetaManager(metaManager)
                .withScenarios(CalculationScenarios.BUILD).build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(entityA, A_CLASS);
        context.addValueChange(
                ValueChange.build(entityC.id(), new EmptyTypedValue(A_LONG), new LongValue(A_LONG, 200L)));

        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);

        Assert.assertNotNull(targetValue);

    }

    @Test
    public void testRemoveCalculation() {
        CalculationContext context = DefaultCalculationContext.Builder.anCalculationContext()
                .withMetaManager(metaManager)
                .withScenarios(CalculationScenarios.BUILD).build();
        context.getCalculationLogicFactory().get().register(aggregationLogic);
        context.focusEntity(entityA, A_CLASS);
        context.addValueChange(
                ValueChange.build(entityC.id(), new EmptyTypedValue(A_LONG), new LongValue(A_LONG, 200L)));

        Optional<IValue> targetValue = aggregationCalculationLogic.calculate(context);

        Assert.assertNotNull(targetValue);

    }

    /**
     * 根据条件和id来判断这条数据是否符合聚合范围.
     *
     * @param entity 被聚合数据.
     * @param entityClass 被聚合对象.
     * @param conditions 条件信息.
     * @return 是否符合.
     */
    private boolean checkEntityByCondition(IEntity entity, IEntityClass entityClass,
                                           Conditions conditions, ConditionsSelectStorage conditionsSelectStorage) {
        if (conditions == null || conditions.size() == 0) {
            return true;
        }
        conditions.addAnd(new Condition(entityClass.field("id").get(),
                ConditionOperator.EQUALS, entity.entityValue().getValue(entity.id()).get()));
        Collection<EntityRef> entityRefs = null;
        try {
            entityRefs = conditionsSelectStorage.select(conditions, entityClass, SelectConfig.Builder.anSelectConfig().build());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (entityRefs != null && entityRefs.size() > ZERO) {
            return true;
        }
        return false;
    }

    /**
     * 得到统计值.
     *
     * @param aggregation 聚合配置.
     * @param sourceEntity 来源实例.
     * @param entityClass 对象结构.
     * @param metaManager meta.
     * @param conditionsSelectStorage 条件查询.
     * @return 统计数字.
     */
    private int countAggregationEntity(Aggregation aggregation, IEntity sourceEntity, IEntityClass entityClass,
                                       MetaManager metaManager, ConditionsSelectStorage conditionsSelectStorage) {
        // 得到count值
        Optional<IEntityClass> aggEntityClass =
                metaManager.load(aggregation.getClassId(), sourceEntity.entityClassRef().getProfile());
        int count = 1;
        if (aggEntityClass.isPresent()) {
            Conditions conditions = aggregation.getConditions();
            // 根据关系id得到关系字段
            Optional<IEntityField> entityField = entityClass.field(aggregation.getRelationId());
            if (entityField.isPresent()) {
                conditions.addAnd(new Condition(aggEntityClass.get().ref(), entityField.get(),
                        ConditionOperator.EQUALS, aggregation.getRelationId(),
                        sourceEntity.entityValue().getValue(sourceEntity.id()).get()));
            }
            Collection<EntityRef> entityRefs = null;
            try {
                entityRefs = conditionsSelectStorage.select(conditions, entityClass, SelectConfig.Builder.anSelectConfig().build());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (entityRefs != null && entityRefs.size() > ZERO) {
                count = entityRefs.size();
            }
        }
        return count;
    }

    static class MockMasterStorage implements MasterStorage {

        private Map<Long, IEntity> entities = new HashMap<>();

        private List<IEntity> replaceEntities = new ArrayList<>();

        @Override
        public int[] replace(EntityPackage entityPackage) throws SQLException {
            int[] results = new int[entityPackage.size()];
            Arrays.fill(results, 1);

            entityPackage.stream().forEach(e -> replaceEntities.add(e.getKey()));

            return results;
        }

        public void addIEntity(IEntity entity) {
            entities.put(entity.id(), entity);
        }

        public List<IEntity> getReplaceEntities() {
            return replaceEntities;
        }

        @Override
        public DataIterator<OriginalEntity> iterator(IEntityClass entityClass, long startTime, long endTime,
                                                     long lastId) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public DataIterator<OriginalEntity> iterator(IEntityClass entityClass, long startTime, long endTime,
                                                     long lastId, int size) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void writeError(ErrorStorageEntity errorStorageEntity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<ErrorStorageEntity> selectErrors(QueryErrorCondition queryErrorCondition)
                throws SQLException {
            throw new UnsupportedOperationException();
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
        public boolean exist(long id) throws SQLException {
            return entities.containsKey(id);
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
        private Map<Participant, long[]> entityIds;
        // 需要增加的影响范围,当迭代树碰到和key相等的参与者时需要为其增加value影响.
        private Map<Participant, Participant> scope;

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
                Map<Participant, long[]> entityIds) {
            this.entityIds = entityIds;
        }

        public void setScope(
                Map<Participant, Participant> scope) {
            this.scope = scope;
        }

        @Override
        public Optional<IValue> calculate(CalculationContext context) throws CalculationException {
            return Optional.ofNullable(valueChanage.get(context.getFocusField()));
        }

        @Override
        public void scope(CalculationContext context, Infuence infuence) {
            infuence.scan((parentClassOp, participant, infuenceInner) -> {

                Participant child = scope.get(participant);

                if (child != null) {
                    infuenceInner.impact(participant, child);
                }

                return true;
            });
        }

        @Override
        public long[] getMaintainTarget(CalculationContext context, Participant participant,
                                        Collection<IEntity> triggerEntities) throws CalculationException {
            long[] ids = entityIds.get(participant);
            if (ids == null) {
                return new long[0];
            } else {
                return ids;
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
