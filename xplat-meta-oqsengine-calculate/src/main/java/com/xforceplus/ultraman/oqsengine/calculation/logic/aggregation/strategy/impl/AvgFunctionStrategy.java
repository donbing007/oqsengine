package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.strategy.impl;

import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationContext;
import com.xforceplus.ultraman.oqsengine.calculation.context.CalculationScenarios;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.helper.AggregationAttachmentHelper;
import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.strategy.FunctionStrategy;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 平均值运算.
 *
 * @author wangzheng
 * @version 0.1 2021/08/23 17:52
 * @since 1.8
 */
public class AvgFunctionStrategy implements FunctionStrategy {

    final Logger logger = LoggerFactory.getLogger(AvgFunctionStrategy.class);

    @Override
    public Optional<IValue> excute(
        Optional<IValue> currentValue, ValueChange valueChange, CalculationContext context) {
        IValue oldValue = valueChange.getOldValue().orElse(new EmptyTypedValue(valueChange.getField()));
        IValue newValue = valueChange.getNewValue().orElse(new EmptyTypedValue(valueChange.getField()));

        if (logger.isDebugEnabled()) {
            logger.debug("begin excuteAvg agg:{}, o-value:{}, n-value:{}",
                currentValue.get().valueToString(), oldValue.valueToString(), newValue.valueToString());
        }
        //焦点字段
        Aggregation aggregation = ((Aggregation) context.getFocusField().config().getCalculation());
        Optional<IValue> aggValue = Optional.of(currentValue.get().copy());
        long count = AggregationAttachmentHelper.count(aggValue.get(), 0);
        if (count == 0) {
            if (!context.getFocusField().type().equals(FieldType.DATETIME)) {
                Optional<IValue> attAggValue = Optional.of(
                    attachmentReplace(aggValue.get(), oldValue, newValue, CalculationScenarios.BUILD));
                return attAggValue;
            }
            return aggValue;
        }
        // 求平均值需要count信息
        // 判断聚合的对象信息是否是当前来源的数据
        if (aggregation.getClassId() == context.getSourceEntity().entityClassRef().getId()) {
            if (context.getScenariso().equals(CalculationScenarios.BUILD)) {
                Optional<IValue> attAggValue = Optional.of(
                    attachmentReplace(aggValue.get(), oldValue, newValue, CalculationScenarios.BUILD));
                return attAggValue;
            } else if (context.getScenariso().equals(CalculationScenarios.DELETE)) {
                Optional<IValue> attAggValue = Optional.of(
                    attachmentReplace(aggValue.get(), oldValue, newValue, CalculationScenarios.DELETE));
                return attAggValue;
            } else {
                Optional<IValue> attAggValue = Optional.of(
                    attachmentReplace(aggValue.get(), oldValue, newValue, CalculationScenarios.REPLACE));
                return attAggValue;
            }
        } else {
            Optional<IValue> attAggValue = Optional.of(
                attachmentReplace(aggValue.get(), oldValue, newValue, CalculationScenarios.REPLACE));
            return attAggValue;
        }
    }

    /**
     * 替换附件信息.
     *
     * @param value                字段.
     * @param o                    旧值参数.
     * @param n                    新值参数.
     * @param calculationScenarios 操作类型.
     * @return 新的附件.
     */
    private IValue attachmentReplace(IValue value, IValue o, IValue n, CalculationScenarios calculationScenarios) {
        Optional attachmentOp = value.getAttachment();
        if (attachmentOp.isPresent()) {
            String attachment = (String) attachmentOp.get();
            String[] att = StringUtils.split(attachment, "|");
            if (att.length > 1) {
                if (value instanceof DecimalValue) {
                    if (o instanceof EmptyTypedValue) {
                        o = new DecimalValue(o.getField(), BigDecimal.ZERO);
                    }
                    if (n instanceof EmptyTypedValue) {
                        n = new DecimalValue(n.getField(), BigDecimal.ZERO);
                    }
                    BigDecimal sum;
                    long count;
                    if (calculationScenarios.equals(CalculationScenarios.BUILD)) {
                        sum = new BigDecimal(att[1]).add(new BigDecimal(n.valueToString()));
                        count = Long.parseLong(att[0]) + 1;
                    } else if (calculationScenarios.equals(CalculationScenarios.DELETE)) {
                        sum = new BigDecimal(att[1]).subtract(new BigDecimal(o.valueToString()));
                        count = Long.parseLong(att[0]) - 1;
                    } else {
                        count = Long.parseLong(att[0]);
                        sum = new BigDecimal(att[1]).add(new BigDecimal(n.valueToString()))
                            .subtract(new BigDecimal(o.valueToString()));
                    }
                    BigDecimal temp;
                    if (count != 0) {
                        temp = sum.divide(new BigDecimal(count), MathContext.DECIMAL64);
                    } else {
                        temp = BigDecimal.ZERO;
                    }
                    value.setStringValue(temp.toString());
                    return value.copy(count + "|" + sum);
                } else if (value instanceof LongValue) {
                    if (o instanceof EmptyTypedValue) {
                        o = new LongValue(o.getField(), 0L);
                    }
                    if (n instanceof EmptyTypedValue) {
                        n = new LongValue(n.getField(), 0L);
                    }
                    long sum;
                    long count;
                    if (calculationScenarios.equals(CalculationScenarios.BUILD)) {
                        sum = Long.parseLong(att[1]) + Long.parseLong(n.valueToString());
                        count = Long.parseLong(att[0]) + 1;
                    } else if (calculationScenarios.equals(CalculationScenarios.DELETE)) {
                        sum = Long.parseLong(att[1]) - Long.parseLong(o.valueToString());
                        count = Long.parseLong(att[0]) - 1;
                    } else {
                        sum = Long.parseLong(att[1]) + Long.parseLong(n.valueToString()) - Long.parseLong(
                            o.valueToString());
                        count = Long.parseLong(att[0]);
                    }
                    Long temp;
                    if (count != 0) {
                        temp = sum / count;
                    } else {
                        temp = Long.valueOf(0);
                    }
                    value.setStringValue(temp.toString());
                    return value.copy(count + "|" + sum);
                }
            }
        }
        return value;
    }
}
