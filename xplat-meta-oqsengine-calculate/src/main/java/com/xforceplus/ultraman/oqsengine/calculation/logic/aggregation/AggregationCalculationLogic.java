package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.dto.AffectedInfo;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.logic.CalculationLogic;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.strategy.FunctionStrategy;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.strategy.impl.AvgFunctionStrategy;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.strategy.impl.CountFunctionStrategy;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.strategy.impl.MaxFunctionStrategy;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.strategy.impl.MinFunctionStrategy;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.strategy.impl.SumFunctionStrategy;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.CalculationParticipant;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Infuence;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InfuenceConsumer;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.Participant;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 聚合字段计算.
 *
 * @author wangzheng.
 * @version 0.1 2021/08/23 17:52.
 * @since 1.8.
 */
public class AggregationCalculationLogic implements CalculationLogic {

    final Logger logger = LoggerFactory.getLogger(AggregationCalculationLogic.class);

    @Override
    public Optional<IValue> calculate(CalculationContext context) throws CalculationException {

        //目标实例
        IEntity entity = context.getFocusEntity();
        if (logger.isDebugEnabled()) {

            logger.info("begin aggregation entity:{}, field:{}",
                context.getFocusClass().name(), context.getFocusField().name());

        }
        //当前需要计算的聚合字段
        IEntityField aggregationField = context.getFocusField();
        //聚合字段的值
        Optional<IValue> aggregationValue = entity.entityValue().getValue(aggregationField.id());
        // 如果当前字段不是聚合字段,那原样返回.
        if (!aggregationField.calculationType().equals(CalculationType.AGGREGATION)) {
            return Optional.empty();
        }
        Aggregation aggregation = ((Aggregation) aggregationField.config().getCalculation());

        //获取被聚合的entity信息（修改后的）
        IEntity triggerEntity = null;
        Optional<IEntity> triggerEntityOp = context.getMaintenanceTriggerEntity();
        /*
        没有发现触发维护的目标,认定为创建场景.
        使用默认值处理.
         */
        if (!triggerEntityOp.isPresent()) {
            if (context.getScenariso().equals(CalculationScenarios.BUILD)) {
                FieldType fieldType = aggregationField.type();
                switch (fieldType) {
                    case LONG:
                        return Optional.of(new LongValue(aggregationField, 0, "0|0"));
                    case DECIMAL:
                        return Optional.of(new DecimalValue(aggregationField, BigDecimal.ZERO, "0|0.0"));
                    default:
                        return Optional.of(new DateTimeValue(aggregationField, DateTimeValue.MIN_DATE_TIME, "0|0"));
                }
            }
            return aggregationValue;
        }

        // 正常情况两个对象只存在一个一对多，在cache中该对象也只会存在一个实例
        triggerEntity = triggerEntityOp.get();

        // 计算相关的字段定义
        Optional<ValueChange> valueChange;

        if (aggregation.getAggregationType().equals(AggregationType.COUNT)) {

            if (context.getScenariso().equals(CalculationScenarios.BUILD)) {
                valueChange = Optional.of(
                    ValueChange.build(
                        triggerEntity.id(),
                        new EmptyTypedValue(aggregationField),
                        new LongValue(aggregationField, 1)));
            } else if (context.getScenariso().equals(CalculationScenarios.DELETE)) {
                valueChange = Optional.of(
                    ValueChange.build(
                        triggerEntity.id(),
                        new LongValue(aggregationField, 1),
                        new EmptyTypedValue(aggregationField)));
            } else {
                /*
                如果不含条件,更新忽略操作.因为数量没有变化.
                否则不退出,设置改为为空.
                 */
                if (aggregation.getConditions().isPresent()) {
                    valueChange = Optional.empty();
                } else {
                    return Optional.empty();
                }
            }

        } else {

            valueChange = findChange(context, aggregation, triggerEntity);

        }

        /*
           如果设置了条件,表示这是一个条件聚合.
           创建和删除场景最为简单,直接判断当前触发对象是否符合条件.
           更新场景分为如下情况.
           1. 旧值符合,新值不符合,需要减去原有旧值.
           2. 旧值不符合,新值符合, 需要增加新值.
           3. 旧值不符合,新值不符合, 不进行计算.
           4. 都符合,重新计算.
           注意: count 这里较为特殊.因为count不指定具体的值,只关心实例数量.也分为4种情况.
           1. 旧实例符合,新实例不符合,统计值减1..
           2. 旧实例不符合,新实例符合, 统计值加1.
           3. 旧值不符合,新值不符合, 不进行计算.
           4. 都符合,不进行计算.
        */
        Optional<Conditions> conditionsOp = aggregation.getConditions();
        if (conditionsOp.isPresent()) {
            if (CalculationScenarios.BUILD == context.getScenariso()
                || CalculationScenarios.DELETE == context.getScenariso()) {
                // 状态改变才进行判定.
                if (valueChange.isPresent()) {
                    if (!conditionsOp.get().match(triggerEntity)) {
                        return Optional.empty();
                    }
                } else {
                    return Optional.empty();
                }
            } else if (CalculationScenarios.REPLACE == context.getScenariso()) {
                Conditions conditions = conditionsOp.get();
                if (aggregation.getAggregationType().equals(AggregationType.COUNT)) {
                    /*
                    由于count不指向任何一个实际的字段,所以必须获取改变前的实体和改变后的实体才能判断4种场景中的那种.
                    从ValueChange中当前触发实例之前的值,替换回去.
                    如果是EmptyTypeValue的将进行删除,表示原来没这个值.
                     */
                    IEntity oldEntity = triggerEntity.copy();
                    context.getValueChanges().stream()
                        .filter(vc -> vc.getEntityId() == oldEntity.id())
                        .map(vc -> vc.getOldValue())
                        .filter(od -> od.isPresent())
                        .forEach(od -> {
                            if (EmptyTypedValue.class.isInstance(od.get())) {
                                oldEntity.entityValue().remove(od.get().getField());
                            } else {
                                oldEntity.entityValue().addValue(od.get());
                            }
                        });
                    boolean oldEntityMatch = conditions.match(oldEntity);
                    boolean newEntityMatch = conditions.match(triggerEntity);

                    if (oldEntityMatch && !newEntityMatch) {
                        // 原有匹配,新对象不匹配,需要减1.
                        valueChange = Optional.of(
                            ValueChange.build(
                                triggerEntity.id(),
                                new LongValue(aggregationField, 1),
                                new EmptyTypedValue(aggregationField))
                        );
                    } else if (!oldEntityMatch && newEntityMatch) {
                        valueChange = Optional.of(
                            ValueChange.build(
                                triggerEntity.id(),
                                new EmptyTypedValue(aggregationField),
                                new LongValue(aggregationField, 1))
                        );
                    } else {
                        // 原有不匹配,现在不匹配 或者 原有匹配,现在匹配 都不需要重新计算.
                        return Optional.empty();
                    }

                } else {

                    if (valueChange.isPresent()) {
                        ValueChange vc = valueChange.get();

                        IValue oldValue = vc.getOldValue().get();
                        IValue newValue = vc.getNewValue().get();
                        ;

                        IEntity copyEntity = triggerEntity.copy();
                        copyEntity.entityValue().addValue(oldValue);

                        boolean oldValueMatch = conditions.match(copyEntity);

                        copyEntity.entityValue().addValue(newValue);

                        boolean newValueMatch = conditions.match(copyEntity);

                        if (oldValueMatch && !newValueMatch) {
                            // 原有匹配,新值不匹配.
                            valueChange = Optional.of(
                                ValueChange.build(vc.getEntityId(), oldValue, new EmptyTypedValue(vc.getField()))
                            );

                        } else if (!oldValueMatch && newValueMatch) {
                            // 旧有不匹配,新值匹配.
                            valueChange = Optional.of(
                                ValueChange.build(vc.getEntityId(), new EmptyTypedValue(vc.getField()), newValue)
                            );

                        } else if (!oldValueMatch && !newValueMatch) {

                            // 前后都不匹配,过滤.

                            return Optional.empty();
                        }
                        // 都匹配,以
                    } else {
                        return Optional.empty();
                    }

                }
            }
        } else {
            if (!valueChange.isPresent()) {
                return Optional.empty();
            }
        }

        try {
            //拿到数据后开始运算
            AggregationType aggregationType = aggregation.getAggregationType();
            if (aggregationType.equals(AggregationType.AVG)) {
                FunctionStrategy functionStrategy = new AvgFunctionStrategy();
                return functionStrategy.excute(aggregationValue, valueChange.get(), context);
            } else if (aggregationType.equals(AggregationType.MAX)) {
                FunctionStrategy functionStrategy = new MaxFunctionStrategy();
                return functionStrategy.excute(aggregationValue, valueChange.get(), context);
            } else if (aggregationType.equals(AggregationType.MIN)) {
                FunctionStrategy functionStrategy = new MinFunctionStrategy();
                return functionStrategy.excute(aggregationValue, valueChange.get(), context);
            } else if (aggregationType.equals(AggregationType.SUM)) {
                FunctionStrategy functionStrategy = new SumFunctionStrategy();
                return functionStrategy.excute(aggregationValue, valueChange.get(), context);
            } else if (aggregationType.equals(AggregationType.COUNT)) {
                FunctionStrategy functionStrategy = new CountFunctionStrategy();
                return functionStrategy.excute(aggregationValue, valueChange.get(), context);
            }
        } catch (Exception ex) {
            throw new CalculationException(
                String.format(
                    "Aggregation calculation exception. [focus-field :%d-%s, message :%s]",
                    aggregationField.id(), aggregationField.name(), ex.getMessage()),
                ex);
        }

        return Optional.empty();
    }

    private Optional<ValueChange> findChange(CalculationContext context, Aggregation aggregation, IEntity entity) {
        Optional<IEntityClass> triggerEntityClassOp =
            context.getMetaManager().get().load(entity.entityClassRef());
        if (!triggerEntityClassOp.isPresent()) {
            throw new CalculationException(
                String.format("The expected target object meta information was not found.[%s]",
                    entity.entityClassRef()));
        }

        IEntityClass triggerEntityClass = triggerEntityClassOp.get();
        Optional<IEntityField> triggerFieldOp = triggerEntityClass.field(aggregation.getFieldId());
        if (!triggerFieldOp.isPresent()) {
            throw new CalculationException(
                String.format("The expected field (%s) does not exist.", aggregation.getFieldId()));
        }

        return context.getValueChange(entity, triggerFieldOp.get());
    }

    @Override
    public void scope(CalculationContext context, Infuence infuence) {
        infuence.scan((parentParticipant, participant, infuenceInner) -> {

            IEntityClass participantClass = participant.getEntityClass();
            IEntityField participantField = participant.getField();

            /*
            迭代所有关系中的字段,判断是否有可能会对当前参与者发起聚合 - MANY_TO_ONE的关系.
             */
            List<Relationship> relationships = participantClass.relationship().stream()
                .filter(relationship ->
                    relationship.getRelationType().equals(Relationship.RelationType.MANY_TO_ONE))
                .collect(Collectors.toList());

            for (Relationship r : relationships) {
                IEntityClass relationshipClass = r.getRightEntityClass(participantClass.ref().getProfile());
                List<IEntityField> fields = relationshipClass.fields().stream()
                    .filter(f -> f.calculationType() == CalculationType.AGGREGATION)
                    .filter(f -> ((((Aggregation) f.config().getCalculation()).getFieldId() == participantField.id())
                        || (((Aggregation) f.config().getCalculation()).getAggregationType()
                        .equals(AggregationType.COUNT))
                        || (participantField.name().equals(EntityField.ID_ENTITY_FIELD.name()))
                    )).collect(Collectors.toList());
                if (fields != null && fields.size() > 0) {
                    fields.forEach(f -> {
                        Aggregation aggregation = (Aggregation) f.config().getCalculation();
                        EntityField countId = (EntityField) participantField;
                        EntityField fieldId = (EntityField) f;
                        if (countId.name().equals(EntityField.ID_ENTITY_FIELD.name())) {
                            if (aggregation.getAggregationType().equals(AggregationType.COUNT)) {
                                infuenceInner.impact(
                                    participant,
                                    CalculationParticipant.Builder.anParticipant()
                                        .withEntityClass(relationshipClass)
                                        .withField(f)
                                        .build()
                                );
                            } else {
                                infuenceInner.impact(
                                    participant,
                                    CalculationParticipant.Builder.anParticipant()
                                        .withEntityClass(relationshipClass)
                                        .withField(fieldId.ID_ENTITY_FIELD)
                                        .build()
                                );
                            }
                        } else {
                            if (!aggregation.getAggregationType().equals(AggregationType.COUNT)) {
                                infuenceInner.impact(
                                    participant,
                                    CalculationParticipant.Builder.anParticipant()
                                        .withEntityClass(relationshipClass)
                                        .withField(f)
                                        .build()
                                );
                            }
                        }
                    });
                }
            }

            return InfuenceConsumer.Action.CONTINUE;
        });
    }

    @Override
    public Collection<AffectedInfo> getMaintainTarget(CalculationContext context, Participant participant,
                                                      Collection<IEntity> entities)
        throws CalculationException {
        IEntityField entityField = participant.getField();
        Aggregation aggregation = (Aggregation) entityField.config().getCalculation();
        if (entities.isEmpty()) {
            return Collections.emptyList();
        }

        Collection<AffectedInfo> affectedEntityIds = new ArrayList<>(entities.size());
        for (IEntity entity : entities) {
            Optional<IValue> aggEntityId = entity.entityValue().getValue(aggregation.getRelationId());
            if (aggEntityId.isPresent()) {
                affectedEntityIds.add(new AffectedInfo(entity, aggEntityId.get().valueToLong()));
            }
        }

        return affectedEntityIds;
    }

    @Override
    public CalculationType supportType() {
        return CalculationType.AGGREGATION;
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
