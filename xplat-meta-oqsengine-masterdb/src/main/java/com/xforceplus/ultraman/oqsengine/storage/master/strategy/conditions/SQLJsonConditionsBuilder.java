package com.xforceplus.ultraman.oqsengine.storage.master.strategy.conditions;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.condition.SQLConditionQueryBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionBuilder;
import com.xforceplus.ultraman.oqsengine.storage.query.ConditionsBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactoryAble;

/**
 * 基于json的搜索构造器.
 *
 * @author dongbin
 * @version 0.1 2020/11/4 15:49
 * @since 1.8
 */
public class SQLJsonConditionsBuilder implements ConditionsBuilder<String>, StorageStrategyFactoryAble {

    private StorageStrategyFactory storageStrategyFactory;
    private SQLConditionQueryBuilderFactory sqlConditionQueryBuilderFactory;

    @Override
    public String build(Conditions conditions) {
        StringBuilder sql = new StringBuilder();
        ConditionBuilder<String> cb;
        for (Condition condition : conditions.collectCondition()) {
            cb = sqlConditionQueryBuilderFactory.getQueryBuilder(condition);

            if (sql.length() > 0) {
                sql.append(" AND ");
            }

            sql.append(cb.build(condition));
        }

        return sql.toString();
    }

    @Override
    public void setStorageStrategy(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;

        this.sqlConditionQueryBuilderFactory = new SQLConditionQueryBuilderFactory(storageStrategyFactory);
    }
}
