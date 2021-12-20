package com.xforceplus.ultraman.oqsengine.devops.om.util;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.utils.TimeUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * 统一数据运维工具.
 *
 * @copyright: 上海云砺信息科技有限公司
 * @author: youyifan
 * @createTime: 11/3/2021 6:20 PM
 * @description:
 * @history:
 */
public class DevOpsOmDataUtils {

    /**
     * 类型转换.
     *
     * @param field 字段
     * @param result 结果
     * @return  返回结果
     */
    public static Object convertDataObject(IEntityField field, Object result) {
        try {
            switch (field.type()) {
                case BOOLEAN: {
                    if (result instanceof String) {
                        result = Boolean.valueOf((String) result);
                    }
                    return result;
                }
                case ENUM: {
                    return result;
                }
                case DATETIME: {
                    if (result instanceof Date) {
                        return TimeUtils.convert((Date) result).toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
                    } else if (result instanceof LocalDateTime) {
                        return ((LocalDateTime) result).toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
                    } else if (result instanceof String) {
                        result = Long.valueOf((String) result);
                    }
                    return TimeUtils.convert((Long) result);
                }
                case LONG: {
                    if (result instanceof Integer) {
                        result = ((Integer) result).longValue();
                    } else if (result instanceof String) {
                        result = Long.parseLong((String) result);
                    }
                    return result;
                }
                case STRING: {
                    return result;
                }
                case STRINGS: {
                    return result;
                }
                case DECIMAL: {
                    if (result instanceof Integer) {
                        result = new BigDecimal(result.toString());
                    } else if (result instanceof String) {
                        result = new BigDecimal((String) result);
                    }
                    return result;
                }
                default: {
                    throw new IllegalArgumentException("unknown field type.");
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("toIValue failed, message [%s]", e.getMessage()));
        }
    }

    /**
     * 操作符转换.
     *
     * @param operation 操作符
     * @return 返回结果
     */
    public static ConditionOperator convertOperation(String operation) {
        switch (operation) {
            case "eq":
                return ConditionOperator.EQUALS;
            case "ne":
                return ConditionOperator.NOT_EQUALS;
            case "like":
                return ConditionOperator.LIKE;
            case "in":
                return ConditionOperator.MULTIPLE_EQUALS;
            case "gt":
                return ConditionOperator.GREATER_THAN;
            case "ge":
                return ConditionOperator.GREATER_THAN_EQUALS;
            case "lt":
                return ConditionOperator.LESS_THAN;
            case "le":
                return ConditionOperator.LESS_THAN_EQUALS;
            default:
                return null;
        }
    }

}
