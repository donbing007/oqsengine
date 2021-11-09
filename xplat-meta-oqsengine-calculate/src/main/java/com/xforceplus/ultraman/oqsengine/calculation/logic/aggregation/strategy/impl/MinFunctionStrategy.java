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
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 最小值运算.
 *
 * @author wangzheng
 * @version 0.1 2021/08/23 17:52
 * @since 1.8
 */
public class MinFunctionStrategy implements FunctionStrategy {

    final Logger logger = LoggerFactory.getLogger(MinFunctionStrategy.class);

    @Override
    public Optional<IValue> excute(Optional<IValue> agg, Optional<IValue> o, Optional<IValue> n, CalculationContext context) {
        logger.info("begin excuteMin agg:{}, o-value:{}, n-value:{}",
                agg.get().valueToString(), o.get().valueToString(), n.get().valueToString());
        Optional<IValue> aggValue = Optional.of(agg.get().copy());
        //焦点字段
        Aggregation aggregation = ((Aggregation) context.getFocusField().config().getCalculation());
        AggregationFunction function = AggregationFunctionFactoryImpl.getAggregationFunction(aggregation.getAggregationType());
        long count;
        if (aggValue.get().valueToLong() == 0 || aggValue.get().valueToString().equals("0.0")
            || aggValue.get().getValue().equals(DateTimeValue.MIN_DATE_TIME)) {
            count = countAggregationEntity(aggregation, context);
            logger.info("minExcute Count:{}, agg-value:{}, n-value:{}", count,
                    aggValue.get().valueToString(), n.get().valueToString());
            if ((context.getScenariso()).equals(CalculationScenarios.BUILD)) {
                if (count == 1) {
                    aggValue.get().setStringValue(n.get().valueToString());
                    logger.info("第一条数据计算 - return agg-value:{}, n-value:{}",
                            aggValue.get().valueToString(), n.get().valueToString());
                    return aggValue;
                }
            }
        }
        // 当聚合值和操作数据的旧值相同，则需要特殊处理 - 这里已经过滤掉第一条数据的特殊场景
        if (aggValue.get().valueToString().equals(o.get().valueToString())) {
            if (aggregation.getClassId() == context.getSourceEntity().entityClassRef().getId()) {
                if (context.getScenariso().equals(CalculationScenarios.BUILD)) {
                    logger.info("后续数据计算，聚合和老数据相同 - return agg-value:{}, n-value:{}, o-value:{}",
                        aggValue.get().valueToString(), n.get().valueToString(), o.get().valueToString());
                    return function.excute(aggValue, o, n);
                } else if (context.getScenariso().equals(CalculationScenarios.DELETE)) {
                    // 删除最小值，需要重新查找最小值-将最小值返回
                    Optional<IValue> minValue = null;
                    try {
                        minValue = minAggregationEntity(aggregation, context, CalculationScenarios.DELETE);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    if (minValue.isPresent()) {
                        logger.info("找到最小数据 - minValue:{}", minValue.get().valueToString());
                        aggValue.get().setStringValue(minValue.get().valueToString());
                        return aggValue;
                    } else {
                        aggValue.get().setStringValue("0");
                        return aggValue;
                    }
                } else {
                    // 聚合值和该数据的老数据相同，则进行特殊判断
                    if (checkMaxValue(o.get(), n.get())) {
                        // 如果新数据小于老数据，在求最小值的时候，直接用该值替换聚合信息
                        aggValue.get().setStringValue(n.get().valueToString());
                        return aggValue;
                    } else {
                        // 如果新数据大于老数据，则需要在数据库中进行一次检索，查出最小数据，用该数据和新值进行比对，然后进行替换
                        Optional<IValue> minValue = null;
                        try {
                            minValue = minAggregationEntity(aggregation, context, CalculationScenarios.REPLACE);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        if (minValue.isPresent()) {
                            logger.info("找到最小数据 - minValue:{}", minValue.get().valueToString());
                            if (checkMaxValue(minValue.get(), n.get())) {
                                // 如果新数据小于老数据，在求最小值的时候，直接用该值替换聚合信息
                                aggValue.get().setStringValue(n.get().valueToString());
                                return aggValue;
                            } else {
                                aggValue.get().setStringValue(minValue.get().valueToString());
                                return aggValue;
                            }
                        } else {
                            aggValue.get().setStringValue(n.get().valueToString());
                            return aggValue;
                        }
                    }
                }
            } else {
                //属于第二层以上树的操作，都按replace来计算
                // 聚合值和该数据的老数据相同，则进行特殊判断
                if (checkMaxValue(o.get(), n.get())) {
                    // 如果新数据小于老数据，在求最小值的时候，直接用该值替换聚合信息
                    aggValue.get().setStringValue(n.get().valueToString());
                    return aggValue;
                } else {
                    // 如果新数据大于老数据，则需要在数据库中进行一次检索，查出最小数据，用该数据和新值进行比对，然后进行替换
                    Optional<IValue> minValue = null;
                    try {
                        minValue = minAggregationEntity(aggregation, context, CalculationScenarios.REPLACE);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    if (minValue.isPresent()) {
                        if (checkMaxValue(minValue.get(), n.get())) {
                            // 如果新数据小于老数据，在求最小值的时候，直接用该值替换聚合信息
                            aggValue.get().setStringValue(n.get().valueToString());
                            return aggValue;
                        } else {
                            aggValue.get().setStringValue(minValue.get().valueToString());
                            return aggValue;
                        }
                    } else {
                        aggValue.get().setStringValue(n.get().valueToString());
                        return aggValue;
                    }
                }
            }
        }
        if (context.getScenariso().equals(CalculationScenarios.DELETE)) {
            // 如果不是删除最小的数据，无需额外判断，直接返回当前聚合值
            return aggValue;
        }

        logger.info("无特殊情况数据计算 - return agg-value:{}, n-value:{}, o-value:{}",
            aggValue.get().valueToString(), n.get().valueToString(), o.get().valueToString());
        return function.excute(aggValue, o, n);
    }

    /**
     * 得到统计值.
     *
     * @param aggregation 聚合配置.
     * @param context     上下文信息.
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
                logger.info("min count relationId:{}, relationValue:{}",
                        entityField.get().id(), context.getFocusEntity().id());
                conditions.addAnd(new Condition(entityField.get(),
                        ConditionOperator.EQUALS, new LongValue(entityField.get(), context.getFocusEntity().id())));
            }
            Page emptyPage = Page.emptyPage();
            try {
                logger.info("min count conditions:{}", conditions.toString());
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
     * 得到最小值.
     *
     * @param aggregation 聚合配置.
     * @param context     上下文信息.
     * @return 统计数字.
     */
    private Optional<IValue> minAggregationEntity(Aggregation aggregation, CalculationContext context,
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
                            .withSort(Sort.buildAscSort(aggEntityClass.get().field(aggregation.getFieldId()).get()))
                            .build()
            );
            logger.info("minAggregationEntity:entityRefs:{}", entityRefs.size());
            if (!entityRefs.isEmpty()) {
                if (entityRefs.size() < 2) {
                    if (entityRefs.size() == 1) {
                        // 只剩下一条数据
                        if (calculationScenarios.equals(CalculationScenarios.REPLACE)) {
                            return Optional.empty();
                        }
                        Optional<IEntity> entity = context.getMasterStorage().get().selectOne(entityRefs.get(0).getId());
                        logger.info("minAggregationEntity:entityRefs:{}", entity.get().entityValue().values().stream().toArray());
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
                logger.info("minAggregationEntity:entityRefs:{}", entity.get().entityValue().values().stream().toArray());
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
        logger.info("checkMaxValue - o-value:{}, n-value:{}", o.valueToString(), n.valueToString());
        if (o instanceof EmptyTypedValue) {
            return false;
        }
        if (n instanceof EmptyTypedValue) {
            return true;
        }
        if (o instanceof DecimalValue) {
            double temp = Math.max(((DecimalValue) o).getValue().doubleValue(), ((DecimalValue) n).getValue().doubleValue());
            return Double.compare(temp, ((DecimalValue) o).getValue().doubleValue()) == 0;
        } else if (o instanceof LongValue) {
            long temp = Math.max(o.valueToLong(), n.valueToLong());
            return temp == o.valueToLong();
        } else if (o instanceof DateTimeValue) {
            long temp = Math.max(o.valueToLong(),
                    n.valueToLong());
            return temp == o.valueToLong();
        }
        return false;
    }
}
