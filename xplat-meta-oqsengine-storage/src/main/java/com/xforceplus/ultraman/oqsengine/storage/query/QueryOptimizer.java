package com.xforceplus.ultraman.oqsengine.storage.query;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;

/**
 * 查询优化器.
 *
 * @param <V> 条件表示类型.
 * @author dongbin
 * @version 0.1 2020/2/22 16:54
 * @since 1.8
 */
public interface QueryOptimizer<V> {

    /**
     * 查询条件优化.给出最合适的条件构造器.
     * @param conditions 查询条件.
     * @return 构造器.
     */
    ConditionsBuilder<V> optimizeConditions(Conditions conditions);
}
