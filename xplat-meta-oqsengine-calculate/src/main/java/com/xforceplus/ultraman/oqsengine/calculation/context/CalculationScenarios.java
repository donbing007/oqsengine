package com.xforceplus.ultraman.oqsengine.calculation.context;

/**
 * 计算字段触发场景.
 *
 * @author dongbin
 * @version 0.1 2021/08/19 15:17
 * @since 1.8
 */
public enum CalculationScenarios {
    /**
     * 错误,未知.
     */
    UNKNOWN,
    /**
     * 创建.
     */
    BUILD,
    /**
     * 更新.
     */
    REPLACE,
    /**
     * 删除.
     */
    DELETE,
}
