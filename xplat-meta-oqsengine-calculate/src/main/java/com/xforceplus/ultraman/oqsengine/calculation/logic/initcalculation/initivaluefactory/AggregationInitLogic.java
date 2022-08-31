package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation.initivaluefactory;

import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunctionFactoryImpl;
import com.xforceplus.ultraman.oqsengine.calculation.utils.infuence.InitCalculationParticipant;
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
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.CombinedSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 聚合初始化.
 *
 * @version 0.1 2021/12/2 14:52
 * @Auther weikai
 * @since 1.8
 */
public class AggregationInitLogic implements InitIvalueLogic {

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private CombinedSelectStorage combinedSelectStorage;

    final Logger logger = LoggerFactory.getLogger(AggregationInitLogic.class);

    @Override
    public CalculationType getCalculationType() {
        return CalculationType.AGGREGATION;
    }

    @Override
    public IEntity init(IEntity entity, InitCalculationParticipant participant) throws RuntimeException, SQLException {

        Optional<IValue> value = entity.entityValue().getValue(participant.getField().id());

        if (!value.isPresent() || (value.get().getValue() instanceof EmptyTypedValue) || participant.isNeedInit()) {
            // 进入此判断说明需要更新，将当前实例标志为需要更新.
            participant.setProcess(entity);

            Aggregation aggregation = (Aggregation) participant.getField().config().getCalculation();

            Conditions conditions = aggregation.getConditions().orElse(Conditions.buildEmtpyConditions());

            Conditions aggCondition = Conditions.buildEmtpyConditions();


            // 获取聚合关系信息
            List<Relationship> relationships = participant.getEntityClass().relationship().stream().filter(relationship ->
                    relationship.getId() == aggregation.getRelationId()).collect(Collectors.toList());
            if (relationships.isEmpty()) {
                throw new CalculationException(String.format(
                        "not found relationShip in aggregation, field id is %s, entityClass id is %s",
                        participant.getField().id(),
                        participant.getEntityClass().id()));
            }
            Relationship relation = relationships.get(0);



            // 构造关系聚合条件
            aggCondition.addAnd(conditions, false)
                .addAnd(new Condition(relation.getEntityField(),
                    ConditionOperator.EQUALS,
                    new LongValue(relation.getEntityField(), entity.id())));

            IEntityClass sourceEntityClass = participant.getSourceEntityClass();


            //按照一页1000条数据查询
            long defaultPageSize = 1000;
            Page page = new Page(1L, defaultPageSize);

            Collection<EntityRef> entityRefs = combinedSelectStorage.select(aggCondition, sourceEntityClass, SelectConfig.Builder.anSelectConfig().withSort(
                    Sort.buildAscSort(EntityField.ID_ENTITY_FIELD)).withPage(page).build());

            // 分页查询全量
            if (page.getTotalCount() > defaultPageSize) {
                while (page.hasNextPage()) {
                    Collection<EntityRef> refs = combinedSelectStorage.select(aggCondition, sourceEntityClass,
                        SelectConfig.Builder.anSelectConfig().withSort(
                            Sort.buildAscSort(EntityField.ID_ENTITY_FIELD)).withPage(page).build());
                    entityRefs.addAll(refs);
                }
            }

            Set<Long> ids = entityRefs.stream().map(EntityRef::getId).collect(Collectors.toSet());

            // 得到所有聚合明细
            Collection<IEntity> entities = masterStorage.selectMultiple(ids.stream().mapToLong(Long::longValue).toArray(), sourceEntityClass);

            IEntityField sourceField = participant.getSourceFields().size() > 0 ? ((ArrayList<IEntityField>) participant.getSourceFields()).get(0) : EntityField.Builder.anEntityField().build();
            //获取符合条件的所有明细值
            List<Optional<IValue>> ivalues = entities.stream().map(i -> i.entityValue().getValue(sourceField.id())).collect(Collectors.toList());

            entities = null;

            // ivalus包含完整明细数据被聚合字段value，占用较大内存,只更新decimal和long类型
            /* if (ivalues.size() <= 0) {
                if (participant.getField().type().equals(FieldType.DECIMAL)) {
                    entity.entityValue().addValue(IValueUtils.toIValue(participant.getField(), new BigDecimal("0.0")));
                } else {
                    entity.entityValue().addValue(IValueUtils.toIValue(participant.getField(), 0));
                }
            }*/

            Optional<IValue> aggMainIValue = doAgg(ivalues, aggregation.getAggregationType(), participant.getField());

            aggMainIValue.ifPresent(ivalue -> entity.entityValue().addValue(ivalue));

        }

        return entity;
    }

    private Optional<IValue> doAgg(List<Optional<IValue>> ivalues, AggregationType aggregationType, IEntityField entityField) {
        // 工厂获取聚合函数，执行运算
        AggregationFunction function = AggregationFunctionFactoryImpl.getAggregationFunction(aggregationType);

        if (entityField.type().equals(FieldType.DATETIME)) {
            return function.init(Optional.of(IValueUtils.toIValue(entityField, LocalDateTime.now())), ivalues);
        } else if (entityField.type().equals(FieldType.DECIMAL)) {
            return function.init(Optional.of(IValueUtils.toIValue(entityField, new BigDecimal("0.0"))), ivalues);
        } else {
            return function.init(Optional.of(IValueUtils.toIValue(entityField, 0)), ivalues);
        }
    }


}
