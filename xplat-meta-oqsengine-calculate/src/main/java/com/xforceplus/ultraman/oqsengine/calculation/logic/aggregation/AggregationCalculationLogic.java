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
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
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
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.ConditionsSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import java.math.BigDecimal;
import java.sql.SQLException;
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

        // 计算相关的字段定义
        Optional<IValue> newValue = null;
        Optional<IValue> oldValue = null;

        // 正常情况两个对象只存在一个一对多，在cache中该对象也只会存在一个实例
        triggerEntity = triggerEntityOp.get();
        if (aggregation.getAggregationType().equals(AggregationType.COUNT)) {
            if (context.getScenariso().equals(CalculationScenarios.BUILD)) {
                newValue = Optional.of(new LongValue(aggregationField, 1));
                oldValue = Optional.of(new EmptyTypedValue(aggregationField));
            } else if (context.getScenariso().equals(CalculationScenarios.DELETE)) {
                oldValue = Optional.of(new LongValue(aggregationField, 1));
                newValue = Optional.of(new EmptyTypedValue(aggregationField));
            } else {
                return Optional.empty();
            }
        } else {
            Optional<IEntityClass> triggerEntityClassOp = context.getMetaManager().get().load(triggerEntity.entityClassRef());
            if (!triggerEntityClassOp.isPresent()) {
                throw new CalculationException(
                    String.format("The expected target object meta information was not found.[%s]",
                        triggerEntity.entityClassRef()));
            }

            IEntityClass triggerEntityClass = triggerEntityClassOp.get();
            Optional<IEntityField> triggerFieldOp = triggerEntityClass.field(aggregation.getFieldId());
            if (!triggerFieldOp.isPresent()) {
                throw new CalculationException(
                    String.format("The expected field (%s) does not exist.", aggregation.getFieldId()));
            }

            Optional<ValueChange> triggerEntityFieldValueChange =
                context.getValueChange(triggerEntity, triggerFieldOp.get());
            if (!triggerEntityClassOp.isPresent()) {
                // 没有改变,原样返回.
                return Optional.empty();
            }

            // 修改前
            oldValue = triggerEntityFieldValueChange.get().getOldValue();
            // 修改后.
            newValue = triggerEntityFieldValueChange.get().getNewValue();

        }
        //拿到数据后开始进行判断数据是否符合条件
        boolean pass = checkEntityByCondition(((Aggregation) aggregationField.config().getCalculation()).getConditions());
        if (!pass) {
            return Optional.empty();
        }

        try {
            //拿到数据后开始运算
            AggregationType aggregationType = aggregation.getAggregationType();
            if (aggregationType.equals(AggregationType.AVG)) {
                FunctionStrategy functionStrategy = new AvgFunctionStrategy();
                return functionStrategy.excute(aggregationValue, oldValue, newValue, context);
            } else if (aggregationType.equals(AggregationType.MAX)) {
                FunctionStrategy functionStrategy = new MaxFunctionStrategy();
                return functionStrategy.excute(aggregationValue, oldValue, newValue, context);
            } else if (aggregationType.equals(AggregationType.MIN)) {
                FunctionStrategy functionStrategy = new MinFunctionStrategy();
                return functionStrategy.excute(aggregationValue, oldValue, newValue, context);
            } else if (aggregationType.equals(AggregationType.SUM)) {
                FunctionStrategy functionStrategy = new SumFunctionStrategy();
                return functionStrategy.excute(aggregationValue, oldValue, newValue, context);
            } else if (aggregationType.equals(AggregationType.COUNT)) {
                FunctionStrategy functionStrategy = new CountFunctionStrategy();
                return functionStrategy.excute(aggregationValue, oldValue, newValue, context);
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
     * 根据条件和id来判断这条数据是否符合聚合范围.
     *
     * @param conditions  条件信息.
     * @return 是否符合.
     */
    private boolean checkEntityByCondition(Conditions conditions) {
        if (conditions == null || conditions.size() == 0) {
            return true;
        }
        return true;
    }

    /**
     * 得到统计值.
     *
     * @param aggregation             聚合配置.
     * @param sourceEntity            来源实例.
     * @param entityClass             对象结构.
     * @param metaManager             meta.
     * @param conditionsSelectStorage 条件查询.
     * @return 统计数字.
     */
    private long countAggregationEntity(Aggregation aggregation, IEntity sourceEntity, IEntityClass entityClass,
                                        MetaManager metaManager, ConditionsSelectStorage conditionsSelectStorage) {
        // 得到count值
        Optional<IEntityClass> aggEntityClass =
            metaManager.load(aggregation.getClassId(), sourceEntity.entityClassRef().getProfile());
        long count = 0;
        if (aggEntityClass.isPresent()) {
            Conditions conditions = aggregation.getConditions();
            // 根据关系id得到关系字段
            Optional<IEntityField> entityField = aggEntityClass.get().field(aggregation.getRelationId());
            if (entityField.isPresent()) {
                conditions.addAnd(new Condition(entityField.get(),
                    ConditionOperator.EQUALS, new LongValue(entityField.get(), sourceEntity.id())));
            }
            Page emptyPage = Page.emptyPage();
            try {
                conditionsSelectStorage.select(conditions, aggEntityClass.get(),
                    SelectConfig.Builder.anSelectConfig().withPage(emptyPage).build());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            count = emptyPage.getTotalCount();
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
