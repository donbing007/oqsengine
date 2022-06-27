package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.strategy.impl;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunction;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.AggregationFunctionFactoryImpl;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.helper.AggregationAttachmentHelper;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.strategy.FunctionStrategy;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
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
import com.xforceplus.ultraman.oqsengine.pojo.utils.IValueUtils;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
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
    public Optional<IValue> excute(
        Optional<IValue> currentValue, ValueChange valueChange, CalculationContext context) {
        IValue oldValue = valueChange.getOldValue().orElse(new EmptyTypedValue(valueChange.getField()));
        IValue newValue = valueChange.getNewValue().orElse(new EmptyTypedValue(valueChange.getField()));

        /*
        如果为EmptyValue,那么将设置为最小值,之后的任何值都会大于这个最大值.
         */
        if (oldValue instanceof EmptyTypedValue) {
            oldValue = IValueUtils.min(oldValue.getField());
        }

        if (newValue instanceof EmptyTypedValue) {
            newValue = IValueUtils.min(newValue.getField());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("begin excuteMax agg:{}, o-value:{}, n-value:{}",
                currentValue.get().valueToString(), oldValue.valueToString(), newValue.valueToString());
        }
        Optional<IValue> aggValue = Optional.of(currentValue.get().copy());
        //焦点字段
        Aggregation aggregation = ((Aggregation) context.getFocusField().config().getCalculation());
        AggregationFunction function =
            AggregationFunctionFactoryImpl.getAggregationFunction(aggregation.getAggregationType());
        long count;
        if (aggValue.get().valueToLong() == 0 || aggValue.get().valueToString().equals("0.0")
            || aggValue.get().getValue().equals(DateTimeValue.MIN_DATE_TIME)) {
            count = AggregationAttachmentHelper.count(aggValue.get(), 0);
            if (context.getScenariso().equals(CalculationScenarios.BUILD)) {
                if (count == 0) {
                    aggValue.get().setStringValue(newValue.valueToString());
                    Optional<IValue> attAggValue = Optional.of(attachmentReplace(aggValue.get(), "1", "0"));
                    return attAggValue;
                }
            }
        }
        // 当聚合值和操作数据的旧值相同，则需要特殊处理 - 这里已经过滤掉第一条数据的特殊场景
        if (aggValue.get().valueToString().equals(oldValue.valueToString())) {
            if (aggregation.getClassId() == context.getSourceEntity().entityClassRef().getId()) {
                //属于第二层树的操作，按实际操作方式判断
                if (context.getScenariso().equals(CalculationScenarios.BUILD)) {
                    Optional<IValue> attAggValue = Optional.of(attachmentReplace(aggValue.get(), "1", "0"));
                    return function.excute(attAggValue, valueChange);
                } else if (context.getScenariso().equals(CalculationScenarios.DELETE)) {
                    // 删除最大值，需要重新查找最大值-将最大值返回
                    Optional<IValue> maxValue = null;
                    try {
                        maxValue = maxAggregationEntity(aggregation, context, CalculationScenarios.DELETE);
                    } catch (SQLException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                    if (maxValue.isPresent()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Found max value {}", maxValue.get().valueToString());
                        }
                        aggValue.get().setStringValue(maxValue.get().valueToString());
                        Optional<IValue> attAggValue = Optional.of(attachmentReplace(aggValue.get(), "-1", "0"));
                        return attAggValue;
                    } else {
                        aggValue.get().setStringValue("0");
                        Optional<IValue> attAggValue = Optional.of(attachmentReplace(aggValue.get(), "-1", "0"));
                        return attAggValue;
                    }
                } else {
                    // 如果新数据小于老数据，则需要在数据库中进行一次检索，查出最大数据，用该数据和新值进行比对，然后进行替换
                    if (checkMaxValue(oldValue, newValue)) {
                        Optional<IValue> maxValue = null;
                        try {
                            maxValue = maxAggregationEntity(aggregation, context, CalculationScenarios.REPLACE);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        if (maxValue.isPresent()) {
                            if (logger.isDebugEnabled()) {
                                logger.info("Found max value {}", maxValue.get().valueToString());
                            }
                            if (checkMaxValue(maxValue.get(), newValue)) {
                                aggValue.get().setStringValue(maxValue.get().valueToString());
                                return aggValue;
                            } else {
                                // 如果新数据大于老数据，在求最大值的时候，直接用该值替换聚合信息
                                aggValue.get().setStringValue(newValue.valueToString());
                                return aggValue;
                            }
                        } else {
                            aggValue.get().setStringValue(newValue.valueToString());
                            return aggValue;
                        }
                    } else {
                        // 如果新数据大于老数据，在求最大值的时候，直接用该值替换聚合信息
                        aggValue.get().setStringValue(newValue.valueToString());
                        return aggValue;
                    }
                }
            } else {
                //属于第二层以上树的操作，都按replace来计算
                // 如果新数据小于老数据，则需要在数据库中进行一次检索，查出最大数据，用该数据和新值进行比对，然后进行替换
                if (checkMaxValue(oldValue, newValue)) {
                    Optional<IValue> maxValue = null;
                    try {
                        maxValue = maxAggregationEntity(aggregation, context, CalculationScenarios.REPLACE);
                    } catch (SQLException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                    if (maxValue.isPresent()) {
                        if (checkMaxValue(maxValue.get(), newValue)) {
                            aggValue.get().setStringValue(maxValue.get().valueToString());
                            return aggValue;
                        } else {
                            // 如果新数据大于老数据，在求最大值的时候，直接用该值替换聚合信息
                            aggValue.get().setStringValue(newValue.valueToString());
                            return aggValue;
                        }
                    } else {
                        aggValue.get().setStringValue(newValue.valueToString());
                        return aggValue;
                    }
                } else {
                    // 如果新数据大于老数据，在求最大值的时候，直接用该值替换聚合信息
                    aggValue.get().setStringValue(newValue.valueToString());
                    return aggValue;
                }
            }
        }
        if (context.getScenariso().equals(CalculationScenarios.DELETE)) {
            // 如果不是删除最大的数据，无需额外判断，直接返回当前聚合值
            Optional<IValue> attAggValue = Optional.of(attachmentReplace(aggValue.get(), "-1", "0"));
            return attAggValue;
        } else if (context.getScenariso().equals(CalculationScenarios.BUILD)) {
            Optional<IValue> attAggValue = Optional.of(attachmentReplace(aggValue.get(), "1", "0"));
            return function.excute(attAggValue, valueChange);
        }
        return function.excute(aggValue, valueChange);
    }

    /**
     * 替换附件信息.
     *
     * @param value 字段.
     * @param count 统计数量.
     * @param sum 求和参数.
     * @return 新的附件.
     */
    private IValue attachmentReplace(IValue value, String count, String sum) {
        Optional attachmentOp = value.getAttachment();
        if (attachmentOp.isPresent()) {
            String attachment = (String) attachmentOp.get();
            String[] att = StringUtils.split(attachment, "|");
            if (att.length > 1) {
                if (value instanceof DecimalValue) {
                    return value.copy((Long.parseLong(att[0]) + Long.parseLong(count))
                            + "|" + (new BigDecimal(att[1]).add(new BigDecimal(sum))));
                } else if (value instanceof LongValue) {
                    return value.copy((Long.parseLong(att[0]) + Long.parseLong(count))
                            + "|" + (Long.parseLong(att[1]) + Long.parseLong(sum)));
                } else if (value instanceof DateTimeValue) {
                    return value.copy(Long.valueOf(att[0]) + Long.valueOf(count) + "|" + att[1]);
                }
            }
        }
        return value;
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

            IEntityClass entityClass = aggEntityClass.get();

            Conditions conditions = Conditions.buildEmtpyConditions();
            // 根据关系id得到关系字段
            Optional<IEntityField> entityField = entityClass.field(aggregation.getRelationId());
            if (entityField.isPresent()) {
                conditions.addAnd(new Condition(entityField.get(),
                        ConditionOperator.EQUALS, new LongValue(entityField.get(), context.getFocusEntity().id())));

            }
            Page emptyPage = Page.newSinglePage(2);
            List<EntityRef> entityRefs = new ArrayList(context.getConditionsSelectStorage().get().select(
                conditions, aggEntityClass.get(),
                    SelectConfig.Builder.anSelectConfig()
                            .withPage(emptyPage)
                            .withSort(Sort.buildDescSort(aggEntityClass.get().field(aggregation.getFieldId()).get()))
                            .build()
            ));

            MasterStorage masterStorage = context.getResourceWithEx(() -> context.getMasterStorage());
            if (!entityRefs.isEmpty()) {
                if (entityRefs.size() < 2) {
                    if (entityRefs.size() == 1) {
                        // 只剩下一条数据
                        if (calculationScenarios.equals(CalculationScenarios.REPLACE)) {
                            return Optional.empty();
                        }
                        Optional<IEntity> entity = masterStorage.selectOne(entityRefs.get(0).getId(), entityClass);
                        if (entity.isPresent()) {
                            return entity.get().entityValue().getValue(aggregation.getFieldId());
                        }
                    }
                    return Optional.empty();
                }
                if (calculationScenarios.equals(CalculationScenarios.DELETE)) {
                    Optional<IEntity> entity = masterStorage.selectOne(entityRefs.get(0).getId(), entityClass);
                    if (entity.isPresent()) {
                        return entity.get().entityValue().getValue(aggregation.getFieldId());
                    }
                }
                Optional<IEntity> entity = masterStorage.selectOne(entityRefs.get(1).getId(), entityClass);
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
            long temp = Math.max(o.valueToLong(),
                n.valueToLong());
            return temp == o.valueToLong();
        }
        return false;
    }

}
