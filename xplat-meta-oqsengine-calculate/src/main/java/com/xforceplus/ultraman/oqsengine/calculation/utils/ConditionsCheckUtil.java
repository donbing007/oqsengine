//package com.xforceplus.ultraman.oqsengine.calculation.utils;
//
//import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
//import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
//import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
//import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
//
//import java.util.Arrays;
//import java.util.concurrent.atomic.AtomicBoolean;
//
///**
// * 数据筛选工具.
// *
// * @className: ConditionsCheckUtil.
// * @package: com.xforceplus.ultraman.oqsengine.calculation.utils.
// * @author: wangzheng.
// * @date: 2021/10/18 15:26.
// */
//public class ConditionsCheckUtil {
//
//    /**
//     * 校验方法.
//     *
//     * @param aggEntity 需要校验的数据.
//     * @param conditions 条件信息.
//     * @return 校验结果.
//     */
//    public boolean check(IEntity aggEntity, Conditions conditions) {
//        AtomicBoolean checkSuccess = new AtomicBoolean(true);
//        if (conditions == null || conditions.size() == 0) {
//            return true;
//        }
//        conditions.collectCondition().stream().forEach(condition -> {
//            ConditionOperator conditionOperator = condition.getOperator();
//            IEntityField entityField = condition.getField();
//            FieldType fieldType = entityField.type();
//            switch (conditionOperator) {
//                case EQUALS:
//                    if (!checkEqualsValues(aggEntity.entityValue(), fieldType, condition.getValues())) {
//                        checkSuccess.set(false);
//                        return;
//                    };
//                case LIKE:
//                    if (!checkLikeValues(aggEntity.entityValue(), fieldType, condition.getValues())) {
//                        checkSuccess.set(false);
//                        return;
//                    };
//                case NOT_EQUALS:
//                    if (!checkNotEqualsValues(aggEntity.entityValue(), fieldType, condition.getValues())) {
//                        checkSuccess.set(false);
//                        return;
//                    };
//                case MULTIPLE_EQUALS:
//                    if (!checkInValues(aggEntity.entityValue(), fieldType, condition.getValues())) {
//                        checkSuccess.set(false);
//                        return;
//                    };
//                default:
//                    return;
//            }
//        });
//        return checkSuccess.get();
//    }
//
//    private boolean checkEqualsValues(IEntityValue entityValue, FieldType fieldType, IValue[] values) {
//        switch (fieldType) {
//
//        }
//        return true;
//    }
//
//    private boolean checkNotEqualsValues(IEntityValue entityValue, FieldType fieldType, IValue[] values) {
//
//        return true;
//    }
//
//    private boolean checkInValues(IEntityValue entityValue, FieldType fieldType, IValue[] values) {
//
//        return true;
//    }
//
//    private boolean checkLikeValues(IEntityValue entityValue, FieldType fieldType, IValue[] values) {
//
//        return true;
//    }
//
//    private boolean valueEq(IValue entityValue, IValue value) {
//
//        return entityValue.getValue().equals(value.getValue());
//    }
//
//    private boolean valueEqSign(IValue entityValue, IValue value) {
//        return entityValue.getValue() == value.getValue();
//    }
//
//    private boolean valueInEq(IEntityValue entityValue, IValue[] values) {
//        AtomicBoolean pass = new AtomicBoolean(true);
//        entityValue.values().forEach(value -> {
//            Arrays.stream(values).forEach(conditionValue -> {
//                if (!value.equals(conditionValue)) {
//                    pass.set(false);
//                }
//            });
//        });
//        return pass.get();
//    }
//
//    private boolean valueInEqSign(IEntityValue entityValue, IValue[] values) {
//        AtomicBoolean pass = new AtomicBoolean(true);
//        entityValue.values().forEach(value -> {
//            Arrays.stream(values).forEach(conditionValue -> {
//                if (!(value == conditionValue)) {
//                    pass.set(false);
//                }
//            });
//        });
//        return pass.get();
//    }
//
//    private boolean valueLike(IValue entityValue, IValue value) {
//
//        return true;
//    }
//}
