package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunctionFactoryImpl;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl.AvgFunction;
import com.xforceplus.ultraman.oqsengine.calculation.logic.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Participant;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.ConditionsSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 聚合字段计算.
 *
 * @author wangzheng
 * @version 0.1 2021/08/23 17:52
 * @since 1.8
 */
public class AggregationCalculationLogic implements CalculationLogic {

    final Logger logger = LoggerFactory.getLogger(AggregationCalculationLogic.class);
    public static final int ONE = 1;
    public static final int ZERO = 0;

    @Override
    public Optional<IValue> calculate(CalculationContext context) throws CalculationException {
        //目标实例
        IEntity entity = context.getFocusEntity();
        //焦点字段
        IEntityField aggField = context.getFocusField();
        //聚合字段的值
        Optional<IValue> aggValue = entity.entityValue().getValue(aggField.id());
        long byAggEntityClassId = ((Aggregation) aggField.config().getCalculation()).getClassId();
        long byAggFieldId = ((Aggregation) aggField.config().getCalculation()).getFieldId();
        //获取被聚合的entity信息（修改后的）
        IEntity byAggEntity = null;
        //定义一个修改前的被聚合entity信息
        Optional<ValueChange> byAggEntityBeforChange = null;
        List<IEntity> entities = context.getEntitiesFormCache().stream().filter(e -> {
            return e.entityClassRef().getId() == byAggEntityClassId;
        }).collect(Collectors.toList());
        if (entities.isEmpty()) {
            return aggValue;
        }

        if (entities.size() > 1) {
            //处理两个对象中存在多个一对多，并且都建立了聚合字段-这种情况是比较少见的
            List<IEntity> byAggEntitys = entities.stream().filter(e -> {
                return e.entityValue().values().stream().filter(value ->
                        value.getValue().equals(entity.id())).collect(Collectors.toList()).size() > ZERO;
            }).collect(Collectors.toList());
            if (byAggEntitys.size() == ONE) {
                byAggEntity = byAggEntitys.get(ZERO);
                Optional<IEntityClass> byAggEntityClass = context.getMetaManager().get().load(byAggEntity.entityClassRef().getId());
                if (byAggEntityClass.isPresent()) {
                    byAggEntityBeforChange = context.getValueChange(byAggEntity, byAggEntityClass.get().field(byAggFieldId).get());
                }
            }
        } else {
            // 正常情况两个对象只存在一个一对多，在cache中该对象也只会存在一个实例
            byAggEntity = entities.get(ZERO);
            Optional<IEntityClass> byAggEntityClass = context.getMetaManager().get().load(byAggEntity.entityClassRef().getId());
            if (byAggEntityClass.isPresent()) {
                byAggEntityBeforChange = context.getValueChange(byAggEntity, byAggEntityClass.get().field(byAggFieldId).get());
            }
        }
        //拿到数据后开始进行判断数据是否符合条件
        boolean pass = checkEntityByCondition(byAggEntity, context.getFocusClass(),
                ((Aggregation) aggField.config().getCalculation()).getConditions(), context.getCombindStorage().get());
        if (!pass) {
            return aggValue;
        }

        //拿到数据后开始运算
        Optional<IValue> agg = aggValue;
        Optional<IValue> n = byAggEntityBeforChange.get().getNewValue();;
        Optional<IValue> o = byAggEntityBeforChange.get().getOldValue();
        AggregationType aggregationType = ((Aggregation) aggField.config().getCalculation()).getAggregationType();
        AggregationFunction function = AggregationFunctionFactoryImpl.getAggregationFunction(aggregationType);
        Optional<IValue> targetValue;
        if (aggregationType.equals(AggregationType.AVG)) {
            int count = 1;
            count = countAggregationEntity((Aggregation) aggField.config().getCalculation(), entity,
                    context.getFocusClass(), context.getMetaManager().get(), context.getCombindStorage().get());
            if (count == 0) {
                if (!aggField.type().equals(FieldType.DATETIME)) {
                    aggValue.get().setStringValue("0");
                    return aggValue;
                }
                return aggValue;
            }
            // 求平均值需要count信息
            targetValue = ((AvgFunction) function).excuteAvg(agg, o, n, count);
        } else {
            targetValue = function.excute(agg, o, n);
        }

        return targetValue;
    }

    @Override
    public void scope(CalculationContext context, Infuence infuence) {
        infuence.scan((parent, participant, infuenceInner) -> {
            IEntityClass currentClass = participant.getEntityClass();
            IEntityField currentField = participant.getField();
            Aggregation aggregation = (Aggregation) currentField.config().getCalculation();
            // 被哪些字段聚合的map<fieldId, boId>
            Map<Long, Long> aggByMap = aggregation.getAggregationByFields();
            aggByMap.forEach((fid, bid) -> {
                Optional<IEntityClass> aggEntityClassOp = context.getMetaManager().get().load(bid, currentClass.ref().getProfile());
                if (aggEntityClassOp.isPresent()) {
                    Optional<IEntityField> entityFieldOp = aggEntityClassOp.get().field(fid);
                    if (entityFieldOp.isPresent()) {
                        infuenceInner.impact(
                                Participant.Builder.anParticipant()
                                        .withEntityClass(currentClass)
                                        .withField(currentField).build(),

                                Participant.Builder.anParticipant()
                                        .withEntityClass(aggEntityClassOp.get())
                                        .withField(entityFieldOp.get()).build()
                        );
                    }
                }
            });
            return true;
        });
    }

    @Override
    public long[] getMaintainTarget(CalculationContext context, Participant participant, Collection<IEntity> entities)
            throws CalculationException {
        IEntityField entityField = participant.getField();
        Aggregation aggregation = (Aggregation) entityField.config().getCalculation();
        if (entities.isEmpty()) {
            return new long[ZERO];
        }
        List<Long> longs = new ArrayList<>();
        entities.forEach(entity -> {
            Optional<IValue> aggEntityId = entity.entityValue().getValue(aggregation.getRelationId());
            if (aggEntityId.isPresent()) {
                longs.add(aggEntityId.get().valueToLong());
            }
        });
        if (longs.isEmpty()) {
            return new long[ZERO];
        }
        long[] targetls = new long[longs.size()];
        int i = ZERO;
        for (Long targetLong : longs) {
            targetls[i++] = targetLong.longValue();
        }
        return targetls;
    }

    @Override
    public CalculationType supportType() {
        return CalculationType.AGGREGATION;
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

    /**
     * 需要维护的场景.
     *
     * @return 需要维护的场景列表.
     */
    @Override
    public CalculationScenarios[] needMaintenanceScenarios() {
        return new CalculationScenarios[] {
            CalculationScenarios.BUILD,
            CalculationScenarios.REPLACE,
            CalculationScenarios.DELETE
        };
    }

}
