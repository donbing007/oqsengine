package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * 查询条件的可用操作符.
 *
 * @author dongbin
 * @version 0.1 2020/2/20 13:26
 * @since 1.8
 */
public enum ConditionOperator {

    /**
     * 未知.
     */
    UNKNOWN("UNKNOWN", (field, values) -> entity -> false),
    /**
     * 模糊匹配.
     */
    LIKE("LIKE", (field, values) -> {
        if (values.length > 0) {
            return entity -> {
                IEntityValue entityValue = entity.entityValue();
                Optional<IValue> valueOp = entityValue.getValue(field.id());
                if (valueOp.isPresent()) {
                    IValue targetValue = valueOp.get();
                    return targetValue.valueToString()
                        .contains(values[0].valueToString());
                } else {
                    return false;
                }
            };
        } else {
            throw new RuntimeException("err invalid");
        }
    }),

    /**
     * 等于.
     */
    EQUALS("=", (field, values) -> {
        if (values.length > 0) {
            return entity -> {
                IEntityValue entityValue = entity.entityValue();
                Optional<IValue> valueOp = entityValue.getValue(field.id());
                if (valueOp.isPresent()) {
                    IValue targetValue = valueOp.get();
                    return targetValue.getValue().equals(values[0].getValue());
                } else {
                    return false;
                }
            };
        } else {
            throw new RuntimeException("err invalid");
        }
    }),

    /**
     * 不等于.
     */
    NOT_EQUALS("!=", (field, values) -> {
        if (values.length > 0) {
            return entity -> {
                IEntityValue entityValue = entity.entityValue();
                Optional<IValue> valueOp = entityValue.getValue(field.id());
                if (valueOp.isPresent()) {
                    IValue targetValue = valueOp.get();
                    return !targetValue.getValue().equals(values[0].getValue());
                } else {
                    return false;
                }
            };
        } else {
            throw new RuntimeException("err invalid");
        }
    }),

    /**
     * 大于.
     */
    GREATER_THAN(">", (field, values) -> {
        if (values.length > 0) {
            return entity -> {
                IEntityValue entityValue = entity.entityValue();
                Optional<IValue> valueOp = entityValue.getValue(field.id());
                return valueOp.filter(left ->
                    numberValueCompare(left, values[0], i -> i > 0)).isPresent();
            };
        } else {
            throw new RuntimeException("err invalid");
        }
    }),

    /**
     * 大于等于.
     */
    GREATER_THAN_EQUALS(">=", (field, values) -> {
        if (values.length > 0) {
            return entity -> {
                IEntityValue entityValue = entity.entityValue();
                Optional<IValue> valueOp = entityValue.getValue(field.id());
                return valueOp.filter(left ->
                    numberValueCompare(left, values[0], i -> i >= 0)).isPresent();
            };
        } else {
            throw new RuntimeException("err invalid");
        }
    }),

    /**
     * 小于.
     */
    LESS_THAN("<", (field, values) -> {
        if (values.length > 0) {
            return entity -> {
                IEntityValue entityValue = entity.entityValue();
                Optional<IValue> valueOp = entityValue.getValue(field.id());
                return valueOp.filter(left ->
                    numberValueCompare(left, values[0], i -> i < 0)).isPresent();
            };
        } else {
            throw new RuntimeException("err invalid");
        }
    }),

    /**
     * 小于等于.
     */
    LESS_THAN_EQUALS("<=", (field, values) -> {
        if (values.length > 0) {
            return entity -> {
                IEntityValue entityValue = entity.entityValue();
                Optional<IValue> valueOp = entityValue.getValue(field.id());
                return valueOp.filter(left ->
                    numberValueCompare(left, values[0], i -> i <= 0)).isPresent();
            };
        } else {
            throw new RuntimeException("err invalid");
        }
    }),

    /**
     * 等于多个值.
     */
    MULTIPLE_EQUALS("IN", (field, values) -> {
        return entity -> {
            IEntityValue entityValue = entity.entityValue();
            Optional<IValue> valueOp = entityValue.getValue(field.id());
            return valueOp.filter(left -> Arrays.stream(values)
                .anyMatch(x -> x.getValue().equals(left.getValue()))).isPresent();
        };
    }),

    /**
     * 判断属性不存在,此操作符会忽略右值.
     */
    IS_NULL("ISNULL", (field, values) -> {
        return entity -> {
            IEntityValue entityValue = entity.entityValue();
            Optional<IValue> valueOp = entityValue.getValue(field.id());
            return !valueOp.isPresent();
        };
    }),

    /**
     * 判断属性存在,此操作符会忽略右值.
     */
    IS_NOT_NULL("ISNOTNULL", (field, values) -> {
        return entity -> {
            IEntityValue entityValue = entity.entityValue();
            Optional<IValue> valueOp = entityValue.getValue(field.id());
            return valueOp.isPresent();
        };
    });

    private String symbol;

    private BiFunction<IEntityField, IValue[], Predicate<Entity>> predicateFunction;

    private ConditionOperator(String symbol, BiFunction<IEntityField, IValue[], Predicate<Entity>> predicateFunction) {
        this.symbol = symbol;
        this.predicateFunction = predicateFunction;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * number compare.
     *
     * @param left          比较左值
     * @param right         比较右值
     * @param compareResult 比较结果Test
     * @return compareResult的结果
     */
    private static boolean numberValueCompare(IValue<?> left, IValue<?> right, Predicate<Integer> compareResult) {
        if (left instanceof LongValue && right instanceof LongValue) {
            Long leftVal = ((LongValue) left).getValue();
            Long rightVal = ((LongValue) right).getValue();
            return compareResult.test(leftVal.compareTo(rightVal));
        } else if (left instanceof DateTimeValue && right instanceof DateTimeValue) {
            Long leftVal = left.valueToLong();
            Long rightVal = right.valueToLong();
            return compareResult.test(leftVal.compareTo(rightVal));
        } else if (left instanceof DecimalValue && right instanceof DecimalValue) {
            BigDecimal leftVal = ((DecimalValue) left).getValue();
            BigDecimal rightVal = ((DecimalValue) right).getValue();
            return compareResult.test(leftVal.compareTo(rightVal));
        } else {
            return false;
        }
    }

    public Predicate<Entity> getPredicate(IEntityField entityField, IValue[] values) {
        return this.predicateFunction.apply(entityField, values);
    }

    /**
     * 根据字面量获得实例.
     *
     * @param symbol 字面量.
     * @return 实例.
     */
    public static ConditionOperator getInstance(String symbol) {
        String noSpaceSymbol = symbol.trim();
        for (ConditionOperator operator : ConditionOperator.values()) {
            if (operator.getSymbol().equals(noSpaceSymbol)) {
                return operator;
            }
        }

        return null;
    }
}
