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
                IEntity oldEntity = buildOldEntity(context, triggerEntity);
                boolean oldEntityMatch = conditions.match(oldEntity);
                boolean newEntityMatch = conditions.match(triggerEntity);

                if (aggregation.getAggregationType().equals(AggregationType.COUNT)) {

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

                    /*
                    每一个分支都需要判断当前聚合的值是否有改变.
                    如果没有改变也不代表不需要聚合,因为目标值可能变化.条件可能变化.
                    所以当前目标字段值没有改变,就直接使用当前值.
                     */
                    if (oldEntityMatch && !newEntityMatch) {
                        Optional<IValue> oldValueOp = findTriggerValue(valueChange, aggregation, triggerEntity, true);
                        if (oldValueOp.isPresent()) {
                            IValue oldValue = oldValueOp.get();
                            valueChange = Optional.of(
                                ValueChange.build(
                                    triggerEntity.id(), oldValue, new EmptyTypedValue(oldValue.getField()))
                            );
                        } else {
                            return Optional.empty();
                        }

                    } else if (!oldEntityMatch && newEntityMatch) {
                        Optional<IValue> newValueOp = findTriggerValue(valueChange, aggregation, triggerEntity, false);
                        if (newValueOp.isPresent()) {
                            IValue newValue = newValueOp.get();
                            valueChange = Optional.of(
                                ValueChange.build(
                                    triggerEntity.id(), new EmptyTypedValue(newValue.getField()), newValue)
                            );
                        } else {
                            return Optional.empty();
                        }

                    } else if (oldEntityMatch && newEntityMatch) {
                        // 原有匹配,新的也匹配,检查有没有valuechange,如果有那么也需要重新计算.
                        if (!valueChange.isPresent()) {
                            return Optional.empty();
                        }

                    } else {
                        // 原有不匹配,新的也不匹配.
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

    @Override
    public void scope(CalculationContext context, Infuence infuence) {
        infuence.scan((parentParticipant, participant, infuenceInner) -> {

            IEntityClass participantClass = participant.getEntityClass();
            IEntityField participantField = participant.getField();

            /*
            迭代所有关系中的字段,判断是否有可能会对当前参与者发起聚合 - MANY_TO_ONE的关系.
             */
            List<Relationship> relationships = participantClass.relationship().stream()
                .filter(r -> r.getRelationType() == Relationship.RelationType.MANY_TO_ONE)
                .collect(Collectors.toList());

            for (Relationship r : relationships) {
                IEntityClass relationshipClass = r.getRightEntityClass(participantClass.ref().getProfile());
                /*
                以下字段会被加入到影响树中.
                1. 是聚合字段.
                2. 聚合目标字段是当前参与者相关字段.
                3. 聚合条件中出现了参与者相关字段.
                4. 是count类型聚合,并且当前参与者相关字段是标识字段.
                 */
                List<IEntityField> fields = relationshipClass.fields().stream()
                    .filter(f -> f.calculationType() == CalculationType.AGGREGATION)
                    .filter(f -> {
                        Aggregation aggregation = (Aggregation) f.config().getCalculation();

                        if (aggregation.getFieldId() == participantField.id()) {
                            //符合条件2.
                            return true;
                        } else if (this.isNeedConditionField(context, participantField, f)) {
                            // 符合条件3.
                            return true;
                        } else if (aggregation.getAggregationType() == AggregationType.COUNT
                            && participantField.config().isIdentifie()) {
                            return true;
                        } else {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
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

    /**
     * 由于聚合字段含有条件,这里会判断目标字段是否出现在了关系对象中其他聚合字段的条件中.
     *
     * @param context     计算上下文.
     * @param entityClass 当前操作目标类型.
     * @param field       当前需要判断的字段.
     * @return true 需要, false不需要.
     */
    @Override
    public boolean need(CalculationContext context, IEntityClass entityClass, IEntityField field) {
        Collection<IEntityClass> relationshipClass = entityClass.relationship().stream()
            .filter(r -> r.getRelationType() == Relationship.RelationType.MANY_TO_ONE)
            .map(r -> r.getRightEntityClass(entityClass.profile()))
            .collect(Collectors.toList());

        /*
          关系中含有条件,并且条中出现目标字段的将返回true.
         */
        for (IEntityClass rec : relationshipClass) {
            for (IEntityField relationField : rec.fields()) {
                if (isNeedRootField(context, relationField)) {
                    return true;
                }
            }
        }
        return false;
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

    /*
    判断指定指定是否应该追随加入某个影响链中.
       A
       |
       B
    这里的字段是B.
    这里处于判定条件改变造成的改变.
    currentField 当前字段.
    targetField        需要判断的字段.
     */
    private boolean isNeedConditionField(CalculationContext context, IEntityField currentField,
                                         IEntityField targetField) {
        if (targetField.calculationType() == CalculationType.AGGREGATION
            && Aggregation.class.isInstance(targetField.config().getCalculation())
            && ((Aggregation) targetField.config().getCalculation()).getConditions().isPresent()) {

            Conditions conditions =
                ((Aggregation) targetField.config().getCalculation()).getConditions().get();
            // 判断当前的条件中出现的字段是否指向上层.
            return conditions.collectField().stream().anyMatch(cf -> cf.id() == currentField.id());
        }

        return false;
    }

    /*
    判断指定的字段是否应该为一个影响树的根结点.
    字段本身可能并未改变,但是可能由于条件改变造成当前字段影响的聚合字段需要重新计算.
         A
         |
         B
      这里判定A是否应该作为一个树的根结点.
     */
    private boolean isNeedRootField(CalculationContext context, IEntityField field) {
        // 聚合指向当前字段且含有条件.判断条件中是否出现的字段有改变.
        if (field.calculationType() == CalculationType.AGGREGATION
            && Aggregation.class.isInstance(field.config().getCalculation())
            && ((Aggregation) field.config().getCalculation()).getConditions().isPresent()
            && field.id() == ((Aggregation) field.config().getCalculation()).getFieldId()) {
            Conditions conditions =
                ((Aggregation) field.config().getCalculation()).getConditions().get();

            /*
              判断关系类型中的聚合字段条件中出现的字段有没有出现在valueChange中.
              如果出现表示需要重新判断是否需要聚合,所以当前字段不论有无改变都需要重新计算.
             */
            Collection<IEntityField> conditionFields = conditions.collectField();

            for (IEntityField conditionField : conditionFields) {
                if (context.getValueChanges().stream().anyMatch(v -> v.getField().id() == conditionField.id())) {
                    return true;
                }
            }
        }

        return false;
    }

    private Optional<IValue> findTriggerValue(
        Optional<ValueChange> valueChange, Aggregation aggregation, IEntity triggerEntity, boolean old) {
        if (old) {
            if (valueChange.isPresent()) {
                return valueChange.get().getOldValue();
            }
        } else {
            if (valueChange.isPresent()) {
                return valueChange.get().getNewValue();
            }
        }

        return triggerEntity.entityValue().getValue(aggregation.getFieldId());
    }

    // 构造目标实例的旧实例,包含这次操作之前的值.
    private IEntity buildOldEntity(CalculationContext context, IEntity entity) {
        IEntity copyEntity = entity.copy();
        context.getValueChanges().stream()
            .filter(vc -> vc.getEntityId() == copyEntity.id())
            .map(vc -> vc.getOldValue())
            .filter(od -> od.isPresent())
            .forEach(od -> {
                if (EmptyTypedValue.class.isInstance(od.get())) {
                    copyEntity.entityValue().remove(od.get().getField());
                } else {
                    copyEntity.entityValue().addValue(od.get());
                }
            });
        return copyEntity;
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

}
