package com.xforceplus.ultraman.oqsengine.storage.value.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;

/**
 * 条件的 SQL 生成策略.
 * @author dongbin
 * @version 0.1 2020/3/6 13:58
 * @since 1.8
 */
public interface ConditionCompareStrategy<T> {

    /**
     * 生成条件 SQL.
     * @param fieldPrefix 最终每个字段的前辍.
     * @param condition 目标条伯.
     * @param storageStrategy storage转换策略.
     * @return 生成结果.
     */
    T build(String fieldPrefix, Condition condition, StorageStrategy storageStrategy);
}