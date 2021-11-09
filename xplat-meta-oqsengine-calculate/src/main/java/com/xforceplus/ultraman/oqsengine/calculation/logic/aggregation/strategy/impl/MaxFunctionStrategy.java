package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.strategy.impl;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunctionFactoryImpl;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.strategy.FunctionStrategy;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 最大值运算.
 *
 * @author wangzheng
 * @version 0.1 2021/08/23 17:52
 * @since 1.8
 */
public class MaxFunctionStrategy implements FunctionStrategy {

    final Logger logger = LoggerFactory.getLogger(MaxFunctionStrategy.class);

    @Override
    public Optional<IValue> excute(Optional<IValue> agg, Optional<IValue> o, Optional<IValue> n, CalculationContext context) {
        logger.info("begin excuteMax agg:{}, o-value:{}, n-value:{}",
                agg.get().valueToString(), o.get().valueToString(), n.get().valueToString());
        //焦点字段
        Aggregation aggregation = ((Aggregation) context.getFocusField().config().getCalculation());
        AggregationFunction function = AggregationFunctionFactoryImpl.getAggregationFunction(aggregation.getAggregationType());
        long count;
        if (agg.get().valueToLong() == 0 || agg.get().valueToString().equals("0.0")) {
            count = countAggregationEntity(aggregation, context);
            if (context.getScenariso().equals(CalculationScenarios.BUILD)) {
                if (count == 1) {
                    agg.get().setStringValue(n.get().valueToString());
                    return agg;
                }
            }
        }
        // 当聚合值和操作数据的旧值相同，则需要特殊处理  - 这里已经过滤掉初始值为0的特殊场景
        if (agg.get().valueToString().equals(o.get().valueToString())) {
            if (aggregation.getClassId() == context.getSourceEntity().entityClassRef().getId()) {
                //属于第二层树的操作，按实际操作方式判断
                if (context.getScenariso().equals(CalculationScenarios.BUILD)) {
                    return function.excute(agg, o, n);
                } else if (context.getScenariso().equals(CalculationScenarios.DELETE)) {
                    // 删除最大值，需要重新查找最大值-将最大值返回
                    Optional<IValue> maxValue = null;
                    try {
                        maxValue = maxAggregationEntity(aggregation, context, CalculationScenarios.DELETE);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    if (maxValue.isPresent()) {
                        logger.info("找到最大数据 - maxValue:{}", maxValue.get().valueToString());
                        agg.get().setStringValue(maxValue.get().valueToString());
                        return agg;
                    } else {
                        agg.get().setStringValue("0");
                        return agg;
                    }
                } else {
                    // 如果新数据小于老数据，则需要在数据库中进行一次检索，查出最大数据，用该数据和新值进行比对，然后进行替换
                    if (checkMaxValue(o.get(), n.get())) {
                        Optional<IValue> maxValue = null;
                        try {
                            maxValue = maxAggregationEntity(aggregation, context, CalculationScenarios.REPLACE);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        if (maxValue.isPresent()) {
                            logger.info("找到最大数据 - maxValue:{}", maxValue.get().valueToString());
                            if (checkMaxValue(maxValue.get(), n.get())) {
                                agg.get().setStringValue(maxValue.get().valueToString());
                                return agg;
                            } else {
                                // 如果新数据大于老数据，在求最大值的时候，直接用该值替换聚合信息
                                agg.get().setStringValue(n.get().valueToString());
                                return agg;
                            }
                        } else {
                            agg.get().setStringValue(n.get().valueToString());
                            return agg;
                        }
                    } else {
                        // 如果新数据大于老数据，在求最大值的时候，直接用该值替换聚合信息
                        agg.get().setStringValue(n.get().valueToString());
                        return agg;
                    }
                }
            } else {
                //属于第二层以上树的操作，都按replace来计算
                // 如果新数据小于老数据，则需要在数据库中进行一次检索，查出最大数据，用该数据和新值进行比对，然后进行替换
                if (checkMaxValue(o.get(), n.get())) {
                    Optional<IValue> maxValue = null;
                    try {
                        maxValue = maxAggregationEntity(aggregation, context, CalculationScenarios.REPLACE);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    if (maxValue.isPresent()) {
                        if (checkMaxValue(maxValue.get(), n.get())) {
                            agg.get().setStringValue(maxValue.get().valueToString());
                            return agg;
                        } else {
                            // 如果新数据大于老数据，在求最大值的时候，直接用该值替换聚合信息
                            agg.get().setStringValue(n.get().valueToString());
                            return agg;
                        }
                    } else {
                        agg.get().setStringValue(n.get().valueToString());
                        return agg;
                    }
                } else {
                    // 如果新数据大于老数据，在求最大值的时候，直接用该值替换聚合信息
                    agg.get().setStringValue(n.get().valueToString());
                    return agg;
                }
            }
        }
        if (context.getScenariso().equals(CalculationScenarios.DELETE)) {
            // 如果不是删除最大的数据，无需额外判断，直接返回当前聚合值
            return agg;
        }
        return function.excute(agg, o, n);
    }

    /**
     * 得到统计值.
     *
     * @param aggregation             聚合配置.
     * @param context            上下文信息.
     * @return 统计数字.
     */
    private long countAggregationEntity(Aggregation aggregation, CalculationContext context) {
        // 得到count值
        Optional<IEntityClass> aggEntityClass =
                context.getMetaManager().get().load(aggregation.getClassId(), context.getFocusEntity().entityClassRef().getProfile());
        long count = 0;
        if (aggEntityClass.isPresent()) {
            Conditions conditions = Conditions.buildEmtpyConditions();
            // 根据关系id得到关系字段
            Optional<IEntityField> entityField = aggEntityClass.get().field(aggregation.getRelationId());
            if (entityField.isPresent()) {
                logger.info("max count relationId:{}, relationValue:{}",
                        entityField.get().id(), context.getFocusEntity().id());
                conditions.addAnd(new Condition(entityField.get(),
                        ConditionOperator.EQUALS, new LongValue(entityField.get(), context.getFocusEntity().id())));
            }
            Page emptyPage = Page.emptyPage();
            try {
                context.getCombindStorage().get().select(conditions, aggEntityClass.get(),
                        SelectConfig.Builder.anSelectConfig().withPage(emptyPage).build());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            count = emptyPage.getTotalCount();
        }
        return count;
    }

    /**
     * 得到最大值.
     *
     * @param aggregation         聚合配置.
     * @param context            上下文信息.
     * @return 统计数字.
     */
    private Optional<IValue> maxAggregationEntity(Aggregation aggregation, CalculationContext context,
                                                  CalculationScenarios calculationScenarios) throws SQLException {
        // 得到最大值
        Optional<IEntityClass> aggEntityClass =
                context.getMetaManager().get().load(aggregation.getClassId(), context.getFocusEntity().entityClassRef().getProfile());
        if (aggEntityClass.isPresent()) {
            Conditions conditions = Conditions.buildEmtpyConditions();
            // 根据关系id得到关系字段
            Optional<IEntityField> entityField = aggEntityClass.get().field(aggregation.getRelationId());
            if (entityField.isPresent()) {
                conditions.addAnd(new Condition(entityField.get(),
                        ConditionOperator.EQUALS, new LongValue(entityField.get(), context.getFocusEntity().id())));

            }
            Page emptyPage = Page.newSinglePage(2);
            List<EntityRef> entityRefs = (List<EntityRef>) context.getCombindStorage().get().select(conditions, aggEntityClass.get(),
                    SelectConfig.Builder.anSelectConfig()
                            .withPage(emptyPage)
                            .withSort(Sort.buildDescSort(aggEntityClass.get().field(aggregation.getFieldId()).get()))
                            .build()
            );
            if (!entityRefs.isEmpty()) {
                if (entityRefs.size() < 2) {
                    if (entityRefs.size() == 1) {
                        // 只剩下一条数据
                        if (calculationScenarios.equals(CalculationScenarios.REPLACE)) {
                            return Optional.empty();
                        }
                        Optional<IEntity> entity = context.getMasterStorage().get().selectOne(entityRefs.get(0).getId());
                        if (entity.isPresent()) {
                            return entity.get().entityValue().getValue(aggregation.getFieldId());
                        }
                    }
                    return Optional.empty();
                }
                if (calculationScenarios.equals(CalculationScenarios.DELETE)) {
                    Optional<IEntity> entity = context.getMasterStorage().get().selectOne(entityRefs.get(0).getId());
                    if (entity.isPresent()) {
                        return entity.get().entityValue().getValue(aggregation.getFieldId());
                    }
                }
                Optional<IEntity> entity = context.getMasterStorage().get().selectOne(entityRefs.get(1).getId());
                if (entity.isPresent()) {
                    return entity.get().entityValue().getValue(aggregation.getFieldId());
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 比较两个数据的大小.
     *
     * @param o 旧值.
     * @param n 新值.
     * @return 旧值大返回true，旧值小返回false.
     */
    private boolean checkMaxValue(IValue o, IValue n) {
        if (o instanceof EmptyTypedValue) {
            return false;
        }
        if (n instanceof EmptyTypedValue) {
            return true;
        }
        if (o instanceof DecimalValue) {
            double temp = Math.max(((DecimalValue) o).getValue().doubleValue(), ((DecimalValue) n).getValue().doubleValue());
            return Double.compare(temp, ((DecimalValue) o).getValue().doubleValue()) == 0 ? true : false;
        } else if (o instanceof LongValue) {
            long temp = Math.max(o.valueToLong(), n.valueToLong());
            return temp == o.valueToLong();
        } else if (o instanceof DateTimeValue) {
            ZoneOffset zone = ZoneOffset.of(ZoneOffset.systemDefault().getId());
            ((DateTimeValue) o).getValue().toEpochSecond(zone);
            long temp = Math.max(((DateTimeValue) o).getValue().toEpochSecond(zone),
                    ((DateTimeValue) n).getValue().toEpochSecond(zone));
            return temp == ((DateTimeValue) o).getValue().toEpochSecond(zone);
        }
        return false;
    }
}