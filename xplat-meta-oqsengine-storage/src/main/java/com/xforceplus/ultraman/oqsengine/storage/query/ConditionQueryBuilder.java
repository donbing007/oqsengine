package com.xforceplus.ultraman.oqsengine.storage.query;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;

/**
 * 条件构造器.
 *
 * @param <T>
 * @author dongbin
 * @version 0.1 2020/3/26 09:49
 * @since 1.8
 */
public interface ConditionQueryBuilder<T> {

    /**
     * 支持的字段类型.
     *
     * @return 字段类型.
     */
    public FieldType fieldType();

    /**
     * 支持的操作符.
     *
     * @return 操作符.
     */
    public ConditionOperator operator();

    /**
     * 构造条件.
     *
     * @param condition 目标条件.
     * @return 构造结果.
     */
    public T build(Condition condition);
}
