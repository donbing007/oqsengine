package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.compare;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;

import java.util.HashMap;
import java.util.Map;

/**
 * 条件构造工厂类.
 * @author dongbin
 * @version 0.1 2020/3/6 15:02
 * @since 1.8
 */
public class ConditionCompareStrategyFactory {

    private static Map<FieldType, SphinxQLConditionCompareStrategy> STRATEGIES;
    private static SphinxQLConditionCompareStrategy DEFAULT_STRATEGY = new DefaultSphinxQLConditionCompareStrategy();

    static {
        STRATEGIES = new HashMap();
        STRATEGIES.put(FieldType.DECIMAL, new DecimalSphinxQLConditionCompareStrategy());
    }

    public static SphinxQLConditionCompareStrategy getStrategy(FieldType type) {
        SphinxQLConditionCompareStrategy strategy = STRATEGIES.get(type);
        return strategy == null ? DEFAULT_STRATEGY : strategy;
    }
}
